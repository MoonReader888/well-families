package com.eldercare.core.constant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 仪表盘统计 Redis Key 常量
 *
 * key 规范：
 *   dashboard:stat:{yyyyMMdd}:{metric}   —— 按天指标
 *   dashboard:stat:total:{metric}        —— 总量指标
 *
 */
public final class DashboardStatKeys {

    private DashboardStatKeys() {
    }

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** key 统一前缀 */
    public static final String PREFIX = "dashboard:stat:";

    /* ===== 按天指标（metric 段）===== */
    /** 当日累计预约数 */
    public static final String METRIC_APPOINTMENT = "appointment";
    /** 当日累计活动报名数 */
    public static final String METRIC_REGISTRATION = "registration";
    /** 当日累计新增会员 */
    public static final String METRIC_NEW_MEMBER = "newMember";

    /* ===== 总量指标（metric 段）===== */
    /** 会员总数 */
    public static final String METRIC_MEMBER_TOTAL = "total:member";
    /** 待完成预约数 */
    public static final String METRIC_PENDING_APPOINTMENT = "total:pendingAppointment";

    /**
     * 按天指标完整 key
     */
    public static String daily(String metric, LocalDate date) {
        return PREFIX + date.format(DAY_FMT) + ":" + metric;
    }

    /**
     * 按天指标完整 key（默认今天）
     */
    public static String daily(String metric) {
        return daily(metric, LocalDate.now());
    }

    /**
     * 总量指标完整 key
     */
    public static String total(String metric) {
        return PREFIX + metric;
    }
}
