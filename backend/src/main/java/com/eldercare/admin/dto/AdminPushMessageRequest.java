package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 单个消息推送请求
 */
@Data
public class AdminPushMessageRequest {

    @NotNull(message = "接收用户不能为空")
    private Long userId;

    @NotBlank(message = "消息标题不能为空")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 类型，默认 SYSTEM */
    private String type;
}
