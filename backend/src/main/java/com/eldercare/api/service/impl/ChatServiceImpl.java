package com.eldercare.api.service.impl;

import com.eldercare.api.service.ChatService;
import com.eldercare.api.vo.ChatSessionVO;
import com.eldercare.core.entity.AiConversationMessage;
import com.eldercare.core.entity.AiConversationSession;
import com.eldercare.core.enums.AIResult;
import com.eldercare.core.enums.MessageRole;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AiConversationMessageMapper;
import com.eldercare.core.mapper.AiConversationSessionMapper;
import com.eldercare.core.service.AiService;
import com.eldercare.core.service.SysConfigService;
import com.eldercare.core.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * AI 对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /**
     * 最近 10 轮对话上下文 = 20 条消息
     */
    private static final int CONTEXT_MESSAGES = 20;

    /**
     * AI 接口超时上限 60 秒
     */
    private static final long SSE_TIMEOUT_MS = 60_000L;

    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是一位专业的健康顾问，请用亲切、易懂的语言回答用户的健康问题。";

    private final AiConversationSessionMapper sessionMapper;
    private final AiConversationMessageMapper messageMapper;
    private final AiService aiService;
    private final SysConfigService sysConfigService;

    /**
     * 创建对话
     */
    @Override
    public ChatSessionVO createSession(String sessionName) {
        Long userId = UserContext.currentUserId();
        AiConversationSession session = new AiConversationSession();
        session.setUserId(userId);
        session.setSessionName(sessionName == null || sessionName.isBlank() ? "新的对话" : sessionName);
        sessionMapper.insert(session);
        return toVO(session);
    }

    /**
     * 列出个人所有对话
     */
    @Override
    public List<ChatSessionVO> listSessions() {
        return sessionMapper.selectByUserId(UserContext.currentUserId()).stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 历史消息
     */
    @Override
    public List<AiConversationMessage> history(Long sessionId) {
        getOwnedSession(sessionId);
        return messageMapper.selectBySessionId(sessionId);
    }

    @Override
    public void deleteSession(Long sessionId) {
        getOwnedSession(sessionId);
        sessionMapper.logicDelete(sessionId);
        messageMapper.logicDeleteBySession(sessionId);
    }

    /**
     * 流式对话
     */
    @Override
    public SseEmitter stream(Long sessionId, String content) {
        Long userId = UserContext.currentUserId();
        getOwnedSession(sessionId);

        saveUserMessage(sessionId, userId, content);
        sessionMapper.touch(sessionId);

        String systemPrompt = getSystemPrompt();
        List<AiService.ChatMessage> context = buildContext(sessionId);

        // 创建 SSE 发送器和缓冲区
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS); //60秒超时
        // buffer攒完整回答最后存库
        StringBuilder builder = new StringBuilder();
        // 原子操作，保证只存一次
        AtomicBoolean finished = new AtomicBoolean(false);

        AtomicBoolean connectionAlive = new AtomicBoolean(true);
        Disposable.Swap disposableSwap = Disposables.swap();

        Consumer<String> saveOnce = status -> {
            if (finished.compareAndSet(false, true)) {
                String text = builder.toString();
                //超时且未收到内容
                if (status.equals(AIResult.FAILED.getCode()) && text.isEmpty()) {
                    log.warn("AI 流式响应超时：sessionId={}", sessionId);
                    return;
                }
                saveAssistantMessage(sessionId, userId, text, status);
                log.info("AI 流式收尾完成: sessionId={}, status={}, len={}",
                        sessionId, status, text.length());
            }
        };

        Runnable abort = ()->{
            if(connectionAlive.compareAndSet(true,false)){
                saveOnce.accept(AIResult.CANCELED.getCode());
                disposableSwap.dispose();
                safeComplete(emitter);
                log.warn("主动停掉 AI ，连接关闭：sessionId={}", sessionId);
            }
        };

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时：sessionId={}", sessionId);
            if(connectionAlive.compareAndSet(true,false)){
                saveOnce.accept(AIResult.FAILED.getCode());
                disposableSwap.dispose();
            }
            safeComplete(emitter);
        });

        emitter.onError(error -> {
            log.error("SSE 连接错误: sessionId={}", sessionId, error);
            if (connectionAlive.compareAndSet(true, false)) {
                saveOnce.accept(AIResult.FAILED.getCode());
                disposableSwap.dispose();
            }
        });

        emitter.onCompletion(() -> {
            saveOnce.accept(AIResult.SUCCESS.getCode());
        });

        Disposable disposable = aiService.chatStream(systemPrompt, context)
                .doOnCancel(() -> {
                    log.info("AI 流被取消，sessionId={}", sessionId);
                    saveOnce.accept(AIResult.CANCELED.getCode());
                })
                .subscribe(
                        chunk -> {
                            if(chunk==null||chunk.isEmpty()){
                                return;
                            }
                            builder.append(chunk);

                            if(!connectionAlive.get()){
                                return;
                            }

                            try{
                                emitter.send(SseEmitter.event().data(chunk));
                            }catch (IOException |  IllegalStateException e){
                                log.info("SSE 推送失败，客户端可能已断开: sessionId={}", sessionId);
                                abort.run();
                            }
                        },
                        error -> {
                            log.error("AI 流式响应异常: sessionId={}", sessionId, error);
                            saveOnce.accept(AIResult.FAILED.getCode());
                            safeCompleteWithError(emitter,error);
                        },
                        () -> {
                            log.info("AI 流式响应正常完成：sessionId={}",sessionId);
                            saveOnce.accept(AIResult.SUCCESS.getCode());
                            safeComplete(emitter);
                        }
                );
        disposableSwap.update(disposable);
        return emitter;
    }

    /**
     * 非流式对话
     */
    @Override
    public String sendMessage(Long sessionId, String content) {
        Long userId = UserContext.currentUserId();
        getOwnedSession(sessionId);

        saveUserMessage(sessionId, userId, content);
        sessionMapper.touch(sessionId);

        String systemPrompt = getSystemPrompt();
        List<AiService.ChatMessage> context = buildContext(sessionId);
        try {
            String reply = aiService.chat(systemPrompt, context);
            saveAssistantMessage(sessionId, userId, reply, AIResult.SUCCESS.getCode());
            return reply;
        } catch (Exception e) {
            log.error("AI 非流式响应异常: sessionId={}", sessionId, e);
            saveAssistantMessage(sessionId, userId, "AI 响应异常：" + e.getMessage(), AIResult.FAILED.getCode());
            return null;
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // 连接可能已断开，忽略
        }
    }

    private void safeCompleteWithError(SseEmitter emitter, Throwable error) {
        try {
            emitter.completeWithError(error);
        } catch (Exception ignored) {
            // 连接可能已断开，忽略
        }
    }

    // 校验会话归属权限
    private AiConversationSession getOwnedSession(Long sessionId) {
        AiConversationSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(UserContext.currentUserId())) {
            throw new ResourceNotFoundException("会话不存在");
        }
        return session;
    }

    private void saveUserMessage(Long sessionId, Long userId, String content) {
        AiConversationMessage userMessage = new AiConversationMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setRole(MessageRole.user.name());
        userMessage.setMessage(content);
        userMessage.setStatus(AIResult.SUCCESS.getCode());
        messageMapper.insert(userMessage);
    }

    private void saveAssistantMessage(Long sessionId, Long userId, String content, String status) {
        AiConversationMessage assistantMessage = new AiConversationMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole(MessageRole.assistant.name());
        assistantMessage.setMessage((content == null || content.isBlank()) && AIResult.FAILED.getCode().equals(status)
                ? "AI 响应异常，请稍后重试" : content);
        assistantMessage.setStatus(status);
        messageMapper.insert(assistantMessage);
    }

    // 查最近的20条消息，转成AiService需要的格式
    private List<AiService.ChatMessage> buildContext(Long sessionId) {
        return messageMapper.selectRecent(sessionId, CONTEXT_MESSAGES).stream()
                .map(m -> new AiService.ChatMessage(m.getRole(), m.getMessage()))
                .toList();
    }

    private String getSystemPrompt() {
        String prompt = sysConfigService.get("ai_chat_system_prompt");
        return prompt == null || prompt.isBlank() ? DEFAULT_SYSTEM_PROMPT : prompt;
    }

    private ChatSessionVO toVO(AiConversationSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setSessionName(session.getSessionName());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());
        return vo;
    }
}
