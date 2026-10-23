package com.eldercare.core.service.impl;

import com.eldercare.core.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * 用 AI Mock 实现：返回固定回复，流式按字逐个下发模拟打字机效果
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "eldercare.ai.mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockAiServiceImpl implements AiService {

    @Override
    public String chat(String systemPrompt, List<ChatMessage> messages) {
        log.info("[AI Mock] systemPrompt={}, messages={}", systemPrompt, messages);
        return "您好！这是 AI 的模拟回复。系统已收到您的健康咨询，正式接入 DeepSeek 后将提供专业建议。";
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, List<ChatMessage> messages) {
        log.info("[AI Mock] systemPrompt={}, messages={}", systemPrompt, messages);
        String reply = "您好！这是 AI 的模拟流式回复，用于本地验证 SSE 打字机效果。";
        return Flux.fromIterable(java.util.Arrays.asList(reply.split("")))
                .delayElements(Duration.ofMillis(50));
    }
}
