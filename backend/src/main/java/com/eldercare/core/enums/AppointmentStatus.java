package com.eldercare.core.enums;

import com.eldercare.core.exception.BusinessException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 预约状态（枚举状态机）
 *
 * 流转规则：
 *   PENDING   -> CONFIRMED / CANCELED
 *   CONFIRMED -> COMPLETED
 *   CANCELED  -> （终态）
 *   COMPLETED -> （终态）
 *
 */
public enum AppointmentStatus {

    PENDING("PENDING", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    CANCELED("CANCELED", "已取消"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    /** 状态机邻接表：允许的流转目标 */
    private static final Map<AppointmentStatus, EnumSet<AppointmentStatus>> TRANSITIONS =
            new EnumMap<>(AppointmentStatus.class);

    static {
        TRANSITIONS.put(PENDING, EnumSet.of(CONFIRMED, CANCELED));
        TRANSITIONS.put(CONFIRMED, EnumSet.of(COMPLETED));
    }

    AppointmentStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 是否终态
     */
    public boolean isTerminal() {
        return !TRANSITIONS.containsKey(this);
    }

    /**
     * 校验 from -> to 的流转是否合法
     */
    public static void checkTransition(AppointmentStatus from, AppointmentStatus to) {
        EnumSet<AppointmentStatus> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BusinessException(
                    String.format("当前预约状态为[%s]，无法执行到[%s]的操作，请重试", from.getDesc(), to.getDesc()));
        }
    }

    /**
     * 由 code 解析枚举，空/非法抛 BusinessException
     */
    public static AppointmentStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("预约状态不能为空");
        }
        for (AppointmentStatus value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new BusinessException("非法的预约状态" + code);
    }

    public static Map<String, String> getLabelMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (AppointmentStatus value : values()) {
            map.put(value.getCode(), value.getDesc());
        }
        return map;
    }
}
