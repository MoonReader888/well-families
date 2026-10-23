package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 对话消息实体（ai_conversation_message 表）
 */
@Data
public class AiConversationMessage implements Serializable {

    private Long id;

    private Long sessionId;

    private Long userId;

    /** 角色 user/assistant */
    private String role;

    /** 消息内容 */
    private String message;

    /** 状态 SUCCESS/FAILED AIResult规范 */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
