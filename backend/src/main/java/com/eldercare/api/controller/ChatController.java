package com.eldercare.api.controller;

import com.eldercare.api.dto.ChatMessageRequest;
import com.eldercare.api.service.ChatService;
import com.eldercare.api.vo.ChatSessionVO;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.AiConversationMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 对话接口（/api/member/chat）
 */
@RestController
@RequestMapping("/api/member/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** 创建会话 */
    @PostMapping("/session")
    public Result<ChatSessionVO> createSession(@RequestParam(required = false) String sessionName) {
        return Result.success(chatService.createSession(sessionName));
    }

    /** 会话列表 */
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> listSessions() {
        return Result.success(chatService.listSessions());
    }

    /** 对话历史 */
    @GetMapping("/session/{sessionId}/messages")
    public Result<List<AiConversationMessage>> history(@PathVariable Long sessionId) {
        return Result.success(chatService.history(sessionId));
    }

    /** 删除会话 */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        chatService.deleteSession(sessionId);
        return Result.success();
    }

    /** 流式对话（SSE 打字机效果） */
    @PostMapping(value = "/{sessionId}/stream", produces = "text/event-stream")
    public SseEmitter stream(@PathVariable Long sessionId,
                             @Valid @RequestBody ChatMessageRequest request) {
        return chatService.stream(sessionId, request.getMessage());
    }

    /** 非流式发送（兜底方案） */
    @PostMapping("/{sessionId}/message")
    public Result<String> sendMessage(@PathVariable Long sessionId,
                                      @Valid @RequestBody ChatMessageRequest request) {
        return Result.success(chatService.sendMessage(sessionId, request.getMessage()));
    }
}
