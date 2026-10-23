package com.eldercare.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * Token 黑名单服务
 *
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_KEY_PREFIX = "jwt:blacklist:";
    private static final String USER_KEY_PREFIX = "jwt:blacklist:user:";

    /**
     * 将单个 Access Token加入黑名单
     */
    public void blacklistAccessToken(String jti, long remainingSeconds) {
        if (jti == null || remainingSeconds <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
                TOKEN_KEY_PREFIX + jti, "1", Duration.ofSeconds(remainingSeconds));
    }

    /**
     * 将用户加入黑名单
     */
    public void blacklistUser(Long userId, long ttlSeconds) {
        long blacklistAt = System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(
                USER_KEY_PREFIX + userId, String.valueOf(blacklistAt), Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 检查用户是否在黑名单中
     */
    public boolean isUserBlacklisted(Long userId, Date issuedAt) {
        if (userId == null || issuedAt == null) {
            return true;
        }
        String value = stringRedisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
        if (value == null) {
            return false;
        }
        return issuedAt.getTime() < Long.parseLong(value);
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isAccessTokenBlacklisted(String jti) {
        return jti != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(TOKEN_KEY_PREFIX + jti));
    }
}
