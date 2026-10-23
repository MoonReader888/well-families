package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约实体（appointment 表）
 */
@Data
public class Appointment implements Serializable {

    private Long id;

    private Long userId;

    /** 时间段 ID */
    private Long slotId;

    /** 套餐 ID */
    private Long packageId;

    /** 状态 PENDING/CONFIRMED/CANCELED/COMPLETED */
    // enums/AppointmentStatus 规范
    private String status;

    /** 体检报告 URL */
    private String reportUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
