package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康指导实体（health_guidance 表）
 */
@Data
public class HealthGuidance implements Serializable {

    private Long id;

    private Long userId;

    /** 类型 DIET/EXERCISE/DAILY/DATA_SUMMARY */
    // enums/GuidanceType 规范
    private String type;

    /** 指导内容 */
    private String content;

    /** 是否已读 0未读/1已读 */
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
