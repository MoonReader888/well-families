package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新预约状态请求
 */
@Data
public class AdminAppointmentStatusRequest {

    /** PENDING/CONFIRMED/COMPLETED/CANCELED */
    @NotBlank(message = "预约状态不能为空")
    private String status;
}
