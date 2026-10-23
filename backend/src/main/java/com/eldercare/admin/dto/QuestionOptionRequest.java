package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 题目选项请求（含分值，用于规则计分）
 */
@Data
public class QuestionOptionRequest {

    /** 选项文本 */
    @NotBlank(message = "选项文本不能为空")
    private String label;

    /** 分值 0-100（不填默认 0） */
    private Integer score;
}
