package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 会话实体（ai_conversation_session 表）
 */
@Data
public class AiConversationSession implements Serializable {

    private Long id;

    private Long userId;

    /** 会话名称 */
    private String sessionName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
