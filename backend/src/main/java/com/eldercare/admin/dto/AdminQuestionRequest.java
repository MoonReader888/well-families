package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 题目新增/修改请求
 */
@Data
public class AdminQuestionRequest {

    @NotBlank(message = "题目内容不能为空")
    private String content;

    /** 类型 SINGLE/MULTIPLE/TEXT */
    @NotBlank(message = "题目类型不能为空")
    private String type;

    /** 选项列表（SINGLE/MULTIPLE 必填，TEXT 忽略） */
    private List<QuestionOptionRequest> options;

    /** 排序号 */
    private Integer sortOrder;
}
