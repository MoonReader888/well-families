package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评测结果实体（assessment_result 表）
 */
@Data
public class AssessmentResult implements Serializable {

    private Long id;

    private Long userId;

    private Long questionnaireId;

    /** 答案快照 JSON */
    private String answers;

    /** 规则评分（百分制） */
    private Integer score;

    /** AI 建议 */
    private String aiSuggestion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;

    private String status; // AssessmentStatus规范

    private String errorMsg;
}
