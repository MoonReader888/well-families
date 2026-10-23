package com.eldercare.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端消息列表视图（联表带手机号）
 */
@Data
public class AdminMessageVO {

    private Long id;
    private Long userId;
    private String phone;
    private String title;
    private String content;

    /** 类型 APPOINTMENT/ACTIVITY/SYSTEM/HEALTH_REMINDER */
    private String type;

    /** 是否已读 0/1 */
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
