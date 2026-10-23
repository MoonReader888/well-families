package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 调整会员等级请求
 */
@Data
public class AdminUpdateLevelRequest {

    /** NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    @NotBlank(message = "会员等级不能为空")
    private String memberLevel;
}
