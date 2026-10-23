package com.eldercare.core.common;

import com.eldercare.core.enums.UserRole;

/**
 * 当前登录用户上下文信息
 *
 * @param userId  用户 ID
 * @param phone   手机号
 * @param role    角色
 */
public record LoginUser(Long userId,
                        String phone,
                        UserRole role) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isMember() {
        return role == UserRole.MEMBER;
    }
}
