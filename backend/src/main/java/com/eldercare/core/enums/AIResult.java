package com.eldercare.core.enums;

public enum AIResult {
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    CANCELED("CANCELED");
    private final String code;

    AIResult(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
