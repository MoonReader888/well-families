package com.eldercare.core.enums;

/**
 * 预约时段状态
 */
public enum SlotStatus {
    /** 可预约 */
    AVAILABLE("AVAILABLE"),
    /** 已满 */
    FULL("FULL"),
    /** 已关闭 */
    CLOSED("CLOSED");

    private final String code;

    SlotStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
