package com.eldercare.core.enums;

/**
 * 用户角色（RBAC 权限模型）
 */
public enum UserRole {
    /** 会员：仅可访问 /api/member/** */
    MEMBER("MEMBER"),
    /** 管理员：可访问 /api/admin/** 和 /api/member/** */
    ADMIN("ADMIN");

    private final String code;

    UserRole(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
