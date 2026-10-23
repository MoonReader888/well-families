package com.eldercare.core.enums;

import com.eldercare.core.exception.BusinessException;

/**
 * 用户状态
 */
public enum UserStatus {
    /** 正常 */
    ENABLED("ENABLED"),
    /** 已禁用（禁用后无法登录） */
    DISABLED("DISABLED");

    private final String code;

    UserStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static void validate(String status) {
        if (status == null) {
            throw new BusinessException("用户状态不能为空");
        }
        for (UserStatus u : UserStatus.values()) {
            if (u.getCode().equals(status)) {
                return;
            }
        }
        throw new BusinessException("非法的用户状态"+status);
    }



}
