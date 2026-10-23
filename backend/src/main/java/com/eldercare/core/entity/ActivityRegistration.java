package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名实体（activity_registration 表）
 */
@Data
public class ActivityRegistration implements Serializable {

    private Long id;

    private Long userId;

    private Long activityId;

    /** 签到状态 NOT_CHECKED_IN/CHECKED_IN */
    // enums/CheckInStatus 规范
    private String checkInStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
