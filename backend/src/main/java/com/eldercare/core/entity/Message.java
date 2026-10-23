package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体（message 表）
 */
@Data
public class Message implements Serializable {

    private Long id;

    private Long userId;

    private String title;

    private String content;

    /** 类型 APPOINTMENT/ACTIVITY/SYSTEM/HEALTH_REMINDER */
    // enums/MessageType 规范
    private String type;

    /** 是否已读 0未读/1已读 */
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
