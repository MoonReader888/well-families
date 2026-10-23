package com.eldercare.core.enums;

import com.eldercare.core.exception.BusinessException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 会员等级
 */
public enum MemberLevel {


    NORMAL("NORMAL", "普通"),
    SILVER("SILVER", "白银"),
    GOLD("GOLD", "黄金"),
    PLATINUM("PLATINUM", "铂金"),
    DIAMOND("DIAMOND", "钻石");

    private final String code;
    private final String desc;

    MemberLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static void validate(String code) {
        if (code == null) {
            throw new BusinessException("会员等级不能为空");
        }
        for (MemberLevel value : values()) {
            if (value.getCode().equals(code)) {
                return;
            }
        }
        throw new BusinessException("非法的会员等级: " + code);
    }

    /**
     * 根据 code 获取枚举对象
     *
     * @param code 等级代码
     * @return 枚举对象
     * @throws BusinessException 如果等级代码非法
     */
    public static MemberLevel fromCode(String code) {
        if (code == null) {
            throw new BusinessException("会员等级不能为空");
        }
        for (MemberLevel value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new BusinessException("非法的会员等级: " + code);
    }

    /**
     * 获取所有等级的 code -> desc 映射（供仪表盘/前端下拉框使用）
     * @return 映射表
     */
    public static Map<String, String> getLabelMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (MemberLevel value : values()) {
            map.put(value.getCode(), value.getDesc());
        }
        return map;
    }
}