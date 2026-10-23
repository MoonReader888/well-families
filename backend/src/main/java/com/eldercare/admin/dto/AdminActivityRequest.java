package com.eldercare.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动新增/修改请求
 */
@Data
public class AdminActivityRequest {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String coverUrl;

    private String content;

    /** 报名开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStart;

    /** 报名结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEnd;

    /** 活动开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityStart;

    /** 活动结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEnd;

    /** 人数上限 */
    @NotNull(message = "人数上限不能为空")
    private Integer maxParticipants;

    /** 状态 DRAFT/REGISTRATING/IN_PROGRESS/ENDED */
    private String status;
}
