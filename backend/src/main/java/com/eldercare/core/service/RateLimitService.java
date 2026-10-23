package com.eldercare.core.service;

import com.eldercare.core.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 接口限流服务
 *
 * Key 命名：
 *   图形验证码：rl:captcha:ip:{ip}（10 次/分钟）
 *   登录：rl:login:ip:{ip}（6 次/分钟）、rl:login:err:{phone}（5 次锁定 30 分钟）
 *   注册：rl:register:ip:{ip}（5 次/小时）
 *   token：rl:refresh:ip:{ip}（60次/小时） rl:refresh:token:{rtHash}（30次/小时）
 *   通用 API：rl:api:user:{userId}（60 次/分钟）
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 检查限流，超限时抛出 429
     */
    public void check(String key, long limit, long windowSeconds) {
        long retryAfter = tryAcquire(key, limit, windowSeconds);
        if (retryAfter > 0) {
            throw new RateLimitException(retryAfter);
        }
    }

    /**
     * 记录一次失败（用于登录错误计数），达到上限锁定
     */
    public void recordFailure(String key, long lockSeconds) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(lockSeconds));
        }
    }

    /**
     * 清除计数
     */
    public void clear(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 是否已被锁定
     */
    public boolean isLocked(String key, long maxFailures) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }
        try {
            return Long.parseLong(value) >= maxFailures;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 尝试获取配额，返回 0 表示成功，>0 表示需等待秒数
     */
    private long tryAcquire(String key, long limit, long windowSeconds) {
        Long result = stringRedisTemplate.execute(
                SLIDING_WINDOW_SCRIPT,
                List.of(key),
                String.valueOf(windowSeconds * 1000),
                String.valueOf(limit),
                String.valueOf(System.currentTimeMillis()),
                java.util.UUID.randomUUID().toString()
        );
        return result == null ? 0 : result;
    }


    /**
     * 滑动窗口限流脚本：返回 0 表示放行，>0 表示需等待的秒数
     */
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>("""
            local key = KEYS[1]
            local windowMs = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local member = ARGV[4]
            redis.call('ZREMRANGEBYSCORE', key, 0, now - windowMs)
            local count = redis.call('ZCARD', key)
            if count >= limit then
              local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
              if oldest[2] then
                local retryAfter = math.ceil((tonumber(oldest[2]) + windowMs - now) / 1000)
                if retryAfter < 1 then retryAfter = 1 end
                return retryAfter
              end
              return 1
            end
            redis.call('ZADD', key, now, member)
            redis.call('PEXPIRE', key, windowMs * 2)
            return 0
            """, Long.class);
}
