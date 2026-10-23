package com.eldercare.core.enums;

/**
 * 健康指导类型
 */
public enum GuidanceType {
    /** 饮食 */
    DIET("DIET"),
    /** 运动 */
    EXERCISE("EXERCISE"),
    /** 作息 */
    DAILY("DAILY"),
    /** 数据小结 */
    DATA_SUMMARY("DATA_SUMMARY");
    private final String code;

    GuidanceType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
