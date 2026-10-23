package com.eldercare.core.exception;

/**
 * 资源不存在异常（对应 HTTP 404）
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
