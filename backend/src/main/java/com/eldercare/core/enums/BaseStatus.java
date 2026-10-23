package com.eldercare.core.enums;

/**
 * 非流程表状态
 */
public enum BaseStatus {
    ENABLED("ENABLED"),
    DISABLED("DISABLED");
    private final String code;

    BaseStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
