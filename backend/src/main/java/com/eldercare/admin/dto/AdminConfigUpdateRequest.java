package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新系统配置请求
 */
@Data
public class AdminConfigUpdateRequest {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;
}
