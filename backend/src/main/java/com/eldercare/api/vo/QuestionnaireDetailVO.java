package com.eldercare.api.vo;

import com.eldercare.core.entity.Question;
import lombok.Data;

import java.util.List;

/**
 * 问卷详情响应（含题目）
 */
@Data
public class QuestionnaireDetailVO {

    private Long id;
    private String title;
    private String description;
    private String status;

    /** 题目列表（按排序号） */
    private List<Question> questions;
}
