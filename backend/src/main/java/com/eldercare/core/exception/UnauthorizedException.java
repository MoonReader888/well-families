package com.eldercare.core.exception;

/**
 * 认证异常（对应 HTTP 401）
 * 未登录、Token 无效/过期、Token 已被加入黑名单
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
