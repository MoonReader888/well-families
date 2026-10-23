package com.eldercare.core.enums;

/**
 * 社区活动状态
 */
public enum ActivityStatus {
    /** 草稿 */
    DRAFT("DRAFT"),
    /** 未开始（报名前 / 报名结束后待活动开始，按当前时间动态计算） */
    NOT_STARTED("NOT_STARTED"),
    /** 报名中 */
    REGISTRATING("REGISTRATING"),
    /** 进行中 */
    IN_PROGRESS("IN_PROGRESS"),
    /** 已结束 */
    ENDED("ENDED");

    private final String code;

    ActivityStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
