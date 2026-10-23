package com.eldercare.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评测结果响应
 */
@Data
public class AssessmentVO {

    private Long id;

    private Long questionnaireId;

    /** 问卷标题 */
    private String questionnaireTitle;

    /** 规则评分（百分制），全 TEXT 问卷为 null */
    private Integer score;

    /** 健康分档（需关注/良好/优秀） */
    private String level;

    /** AI 个性化健康建议 */
    private String aiSuggestion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String status;

    private String errorMsg;
}
