package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新预约状态请求
 */
@Data
public class AdminAppointmentBatchStatusRequest {

    /** 预约 ID 集合 */
    @NotEmpty(message = "请选择要操作的预约")
    private List<Long> ids;

    /** PENDING/CONFIRMED/COMPLETED/CANCELED */
    @NotBlank(message = "预约状态不能为空")
    private String status;
}
