package com.eldercare.core.enums;

/**
 * 站内消息类型
 * 注：健康提醒使用 type=HEALTH_REMINDER，
 */
public enum MessageType {
    /** 预约 */
    APPOINTMENT("APPOINTMENT"),
    /** 活动 */
    ACTIVITY("ACTIVITY"),
    /** 系统 */
    SYSTEM("SYSTEM"),
    /** 健康提醒 */
    HEALTH_REMINDER("HEALTH_REMINDER");

    private final String code;

    MessageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
