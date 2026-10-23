package com.eldercare.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预约请求
 */
@Data
public class BookAppointmentRequest {

    @NotNull(message = "体检套餐不能为空")
    private Long packageId;

    @NotNull(message = "预约时段不能为空")
    private Long slotId;
}
