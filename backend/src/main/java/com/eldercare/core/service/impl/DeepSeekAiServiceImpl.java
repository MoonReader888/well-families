package com.eldercare.core.service.impl;

import com.eldercare.core.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek AI 实现
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "eldercare.ai.mock-enabled", havingValue = "false")
public class DeepSeekAiServiceImpl implements AiService {


    private final ChatModel chatModel;

    public DeepSeekAiServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // 非流式
    @Override
    public String chat(String systemPrompt, List<ChatMessage> messages) {
        ChatResponse response = chatModel.call(new Prompt(buildMessages(systemPrompt, messages)));
        String content = response.getResult() != null ?
                response.getResult().getOutput().getText() : null;
        if (content == null || content.isBlank()) {
            log.warn("AI 返回空内容");
        }
        return content;
    }

    // 流式
    @Override
    public Flux<String> chatStream(String systemPrompt, List<ChatMessage> messages) {
        return chatModel.stream(new Prompt(buildMessages(systemPrompt, messages)))
                .map(response -> response.getResult() != null ?
                        response.getResult().getOutput().getText() : null)
                .filter(text -> text != null && !text.isEmpty());
    }

    private List<Message> buildMessages(String systemPrompt, List<ChatMessage> messages) {
        List<Message> result = new ArrayList<>();

        //1. 先加系统提示词
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            result.add(new SystemMessage(systemPrompt));
        }

        //2. 遍历业务消息
        for (ChatMessage m : messages) {
            switch (m.role()) {
                case "user" -> result.add(new UserMessage(m.content()));
                case "assistant" -> result.add(new AssistantMessage(m.content()));
                default -> result.add(new UserMessage(m.content()));
            }
        }
        return result;
    }
}
