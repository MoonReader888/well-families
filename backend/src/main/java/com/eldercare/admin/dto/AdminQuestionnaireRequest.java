package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 问卷新增/修改请求
 */
@Data
public class AdminQuestionnaireRequest {

    @NotBlank(message = "问卷标题不能为空")
    private String title;

    private String description;

    //状态
    private String status;

    //是否计分
    private Integer isRated;
}
