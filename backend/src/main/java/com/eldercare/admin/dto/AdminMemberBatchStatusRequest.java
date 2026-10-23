package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量启用/禁用会员账号请求
 */
@Data
public class AdminMemberBatchStatusRequest {

    /** 会员 ID 集合 */
    @NotEmpty(message = "请选择要操作的会员")
    private List<Long> ids;

    /** ENABLED/DISABLED */
    @NotBlank(message = "账号状态不能为空")
    private String status;
}
