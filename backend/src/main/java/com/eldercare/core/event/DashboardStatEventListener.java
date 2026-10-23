package com.eldercare.core.event;

import com.eldercare.core.constant.DashboardStatKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

/**
 * 仪表盘统计增量监听器
 *
 * 在正向业务事务提交成功后（AFTER_COMMIT）对 Redis 按天计数器执行 INCR。
 * 回滚时监听器不会触发，从而避免「事务未提交即计数」导致的计数虚高。
 * INCR 失败不阻塞主流程，由每小时对账任务兜底修正。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardStatEventListener {

    /** 按天计数 key 保留 8 天（覆盖近 7 日趋势窗口 + 1 天缓冲） */
    private static final Duration DAILY_TTL = Duration.ofDays(8);

    private final StringRedisTemplate stringRedisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIncr(DashboardStatIncrEvent event) {
        try {
            String key = DashboardStatKeys.daily(event.getMetric());
            Long count = stringRedisTemplate.opsForValue().increment(key);
            // 首次写入（increment 返回 1）时设置 TTL，避免 key 永久残留
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, DAILY_TTL);
            }
        } catch (Exception e) {
            // INCR 失败不影响主流程，交由每小时对账任务兜底修正
            log.warn("仪表盘统计 INCR 失败，metric={}，将由对账任务修正", event.getMetric(), e);
        }
    }
}
