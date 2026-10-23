package com.eldercare.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区活动响应（含当前用户报名/签到状态）
 */
@Data
public class ActivityVO {

    private Long id;

    private String title;

    private String coverUrl;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEnd;

    /** 人数上限 */
    private Integer maxParticipants;

    /** 当前报名人数 */
    private Integer currentParticipants;

    /** 状态 DRAFT/NOT_STARTED/REGISTRATING/IN_PROGRESS/ENDED（NOT_STARTED 按当前时间动态计算） */
    private String status;

    /** 当前用户是否已报名 */
    private Boolean registered;

    /** 当前用户签到状态：NOT_CHECKED_IN/CHECKED_IN（未报名为 null） */
    private String checkInStatus;
}
