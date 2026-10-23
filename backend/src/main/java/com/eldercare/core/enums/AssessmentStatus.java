package com.eldercare.core.enums;


public enum AssessmentStatus {
    PROCESSING("PROCESSING"),  // 处理中（已提交，AI 生成建议中）
    SUCCESS("SUCCESS"),     // 成功
    FAILED("FAILED");      // 失败
    private final String code;

    AssessmentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}