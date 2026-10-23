package com.eldercare.core.util;

import com.eldercare.core.common.LoginUser;
import com.eldercare.core.enums.UserRole;
import com.eldercare.core.exception.UnauthorizedException;

/**
 * 当前登录用户上下文（ThreadLocal 持有，请求结束时清理）
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录时抛出 401
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        return user;
    }

    public static Long currentUserId() {
        return require().userId();
    }

    public static UserRole currentRole() {
        return require().role();
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && user.isAdmin();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
