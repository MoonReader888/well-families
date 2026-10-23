package com.eldercare.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端预约列表视图（联表带手机号/套餐/时段）
 */
@Data
public class AdminAppointmentVO {

    private Long id;
    private Long userId;
    private String phone;
    private String packageName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointDate;

    private String timeRange;
    private String status;
    private String reportUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
