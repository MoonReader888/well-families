package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 启用/禁用会员账号请求
 */
@Data
public class AdminUpdateStatusRequest {

    /** ENABLED/DISABLED */
    @NotBlank(message = "账号状态不能为空")
    private String status;
}
