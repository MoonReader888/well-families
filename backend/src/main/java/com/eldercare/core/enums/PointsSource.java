package com.eldercare.core.enums;

/**
 * 积分流水来源（对应积分流水表 source 字段）
 */
public enum PointsSource {
    /** 注册赠送 */
    REGISTER("REGISTER"),
    /** 活动签到 */
    CHECK_IN("CHECK_IN"),
    /** 完成健康评测 */
    ASSESSMENT("ASSESSMENT"),
    /** 体检预约扣减 */
    APPOINTMENT("APPOINTMENT"),
    /** 管理员手动调整 */
    ADMIN("ADMIN"),
    /** 到期清理 */
    EXPIRE("EXPIRE");

    private final String code;

    PointsSource(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
