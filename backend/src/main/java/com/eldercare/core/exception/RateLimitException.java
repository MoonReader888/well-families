package com.eldercare.core.exception;

import lombok.Getter;

/**
 * 限流异常（对应 HTTP 429）
 * 携带 Retry-After 冷却秒数
 */
@Getter
public class RateLimitException extends BusinessException {

    /** 建议重试等待秒数 */
    private final long retryAfter;

    public RateLimitException(long retryAfter) {
        super(ErrorCode.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS.getMessage());
        this.retryAfter = retryAfter;
    }

    public RateLimitException(String message, long retryAfter) {
        super(ErrorCode.TOO_MANY_REQUESTS, message);
        this.retryAfter = retryAfter;
    }
}
