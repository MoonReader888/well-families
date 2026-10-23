package com.eldercare.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评测答案项（answers JSON Schema）
 * value：SINGLE 存选项下标数字 / MULTIPLE 存下标数字数组 / TEXT 存字符串
 */
@Data
public class AssessmentAnswerItem {

    /** 题目 ID（对应 question.id） */
    @NotNull(message = "题目 ID 不能为空")
    private Long qid;

    /** 题目类型 SINGLE/MULTIPLE/TEXT */
    @NotNull(message = "题目类型不能为空")
    private String type;

    /** 答案值 */
    @NotNull(message = "答案不能为空")
    private Object value;
}
