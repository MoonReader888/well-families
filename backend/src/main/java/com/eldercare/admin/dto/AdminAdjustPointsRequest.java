package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手动调整积分请求
 */
@Data
public class AdminAdjustPointsRequest {

    /** 变动值：正数增加 / 负数扣减 */
    @NotNull(message = "积分变动值不能为空")
    private Integer points;

    /** 调整原因 */
    @NotBlank(message = "调整原因不能为空")
    private String reason;
}
