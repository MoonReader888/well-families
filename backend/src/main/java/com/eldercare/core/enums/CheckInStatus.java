package com.eldercare.core.enums;

/**
 * 活动签到状态
 */
public enum CheckInStatus {
    /** 未签到 */
    NOT_CHECKED_IN("NOT_CHECKED_IN"),
    /** 已签到 */
    CHECKED_IN("CHECKED_IN");

    private final String code;

    CheckInStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
