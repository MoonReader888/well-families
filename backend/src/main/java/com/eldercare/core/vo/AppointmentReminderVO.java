package com.eldercare.core.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 预约提醒视图对象（定时任务查询次日预约，定时任务）
 */
@Data
public class AppointmentReminderVO {

    private Long appointmentId;
    private Long userId;
    private String phone;
    private String packageName;
    private LocalDate appointDate;
    private String timeRange;
}
