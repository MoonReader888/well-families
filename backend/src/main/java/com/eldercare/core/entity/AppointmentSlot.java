package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约时段实体（appointment_slot 表）
 */
@Data
public class AppointmentSlot implements Serializable {

    private Long id;

    private Long packageId;

    /** 预约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointDate;

    /** 时间段，需要管理端手动输入，
     * 如 "09:00-10:00" */
    private String timeRange;

    /** 最大预约人数 */
    private Integer maxCount;

    /** 当前已预约人数 */
    private Integer currentCount;

    /** 状态 AVAILABLE/FULL/CLOSED */
    // enums/SlotStatus 规范
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
