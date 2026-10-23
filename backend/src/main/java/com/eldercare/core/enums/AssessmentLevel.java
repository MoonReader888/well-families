package com.eldercare.core.enums;

/**
 * 健康评测分档
 */
public enum AssessmentLevel {

    NEEDS_ATTENTION("NEEDS_ATTENTION","需关注"),
    GOOD("GOOD","良好"),
    EXCELLENT("EXCELLENT","优秀");

    private final String code;
    private final String desc;

    AssessmentLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDescByScore(Integer score) {
        if (score == null) {
            return null;
        }
        if (score < 60) {
            return NEEDS_ATTENTION.getDesc();
        }
        if (score < 80) {
           return GOOD.getDesc();
        }
        return EXCELLENT.getDesc();
    }
}
