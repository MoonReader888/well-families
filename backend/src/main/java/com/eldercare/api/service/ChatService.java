package com.eldercare.api.service;

import com.eldercare.api.vo.ChatSessionVO;
import com.eldercare.core.entity.AiConversationMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 对话服务
 */
public interface ChatService {

    /**
     * 创建会话
     */
    ChatSessionVO createSession(String sessionName);

    /**
     * 会话列表
     */
    List<ChatSessionVO> listSessions();

    /**
     * 会话内对话历史（按时间正序）
     */
    List<AiConversationMessage> history(Long sessionId);

    /**
     * 删除会话（会话及消息逻辑删除）
     */
    void deleteSession(Long sessionId);

    /**
     * 流式对话：保存用户消息后建立 SSE 连接推送 AI 回复
     */
    SseEmitter stream(Long sessionId, String content);

    /**
     * 非流式发送：返回完整回复（兼容无 SSE 的场景）
     */
    String sendMessage(Long sessionId, String content);
}
