package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目实体（question 表）
 */
@Data
public class Question implements Serializable {

    private Long id;

    private Long questionnaireId;

    /** 题目内容 */
    private String content;

    /** 类型 SINGLE/MULTIPLE/TEXT */
    // enums/QuestionType 规范
    private String type;

    /** 选项 JSON 数组（TEXT 类型为 null） */
    private String options;

    /** 排序号 */
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
