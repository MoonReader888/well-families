package com.eldercare.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端活动报名列表视图（联表带手机号/姓名）
 */
@Data
public class AdminRegistrationVO {

    private Long id;
    private Long userId;
    private String phone;
    private String realName;

    /** 签到状态 NOT_CHECKED_IN/CHECKED_IN */
    private String checkInStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
