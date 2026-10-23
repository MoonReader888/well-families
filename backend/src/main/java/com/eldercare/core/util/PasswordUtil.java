package com.eldercare.core.util;

import com.eldercare.core.exception.BusinessException;

/**
 * 密码强度校验工具（长度≥8，同时包含字母和数字）
 */
public final class PasswordUtil {

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    private PasswordUtil() {
    }

    /**
     * 校验密码强度，不满足时抛出业务异常
     */
    public static void assertStrong(String password) {
        if (password == null || !password.matches(PASSWORD_PATTERN)) {
            throw new BusinessException("密码长度需不少于 8 位，且同时包含字母和数字");
        }
    }
}
