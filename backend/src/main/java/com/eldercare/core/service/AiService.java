package com.eldercare.core.service;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 服务（DeepSeek）
 */
public interface AiService {

    /**
     * 对话消息
     *
     * @param role 角色：user/assistant
     * @param content 内容
     */
    record ChatMessage(String role, String content) {
    }

    /**
     * 非流式调用：返回完整回复（评测评分、健康建议）
     *
     * @param systemPrompt 系统提示词
     * @param messages 对话历史
     * @return AI 完整回复
     */
    String chat(String systemPrompt, List<ChatMessage> messages);

    /**
     * 流式调用：逐块返回回复（AI 对话 SSE 场景）
     *
     * @param systemPrompt 系统提示词
     * @param messages 最近 10 轮上下文
     * @return 内容增量 Flux
     */
    Flux<String> chatStream(String systemPrompt, List<ChatMessage> messages);
}
