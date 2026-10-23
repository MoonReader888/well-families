package com.eldercare.core.exception;

/**
 * 权限异常（对应 HTTP 403）
 * 角色权限不足、访问非本人资源
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}
