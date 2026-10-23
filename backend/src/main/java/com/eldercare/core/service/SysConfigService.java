package com.eldercare.core.service;

import com.eldercare.core.entity.SysConfig;
import com.eldercare.core.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 系统配置服务
 * 热点配置先查 Redis 缓存（key: config:{configKey}），未命中查数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private static final String CACHE_KEY_PREFIX = "config:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(60);

    private final SysConfigMapper sysConfigMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public String get(String configKey) {
        // 1. Redis 缓存
        String cached = stringRedisTemplate.opsForValue().get(CACHE_KEY_PREFIX + configKey);
        if (cached != null) {
            return cached; //缓存命中，直接返回
        }
        // 2. 查数据库（缓存未命中）
        SysConfig config = sysConfigMapper.selectByKey(configKey);
        if (config == null) {
            return null;
        }
        // 3. 写入缓存，设置60分钟ttl
        stringRedisTemplate.opsForValue().set(CACHE_KEY_PREFIX + configKey, config.getConfigValue(), CACHE_TTL);
        return config.getConfigValue();
    }

    // 获取数据库的初始积分配置
    public int getInt(String configKey, int defaultValue) {
        String value = get(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 不是合法整数: {}", configKey, value);
            return defaultValue;
        }
    }

    //数据更新删除缓存
    public void update(String configKey, String configValue) {
        sysConfigMapper.updateValue(configKey, configValue);
        stringRedisTemplate.delete(CACHE_KEY_PREFIX + configKey);  // 删除缓存
    }

    // 获取系统配置项
    public List<SysConfig> list() {
        return sysConfigMapper.selectAll();
    }
}
