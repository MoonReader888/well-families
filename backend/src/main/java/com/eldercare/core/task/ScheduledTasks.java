package com.eldercare.core.task;

import com.eldercare.core.constant.DashboardStatKeys;
import com.eldercare.core.entity.PointsTransaction;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.PointsSource;
import com.eldercare.core.mapper.*;
import com.eldercare.core.vo.AppointmentReminderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务（定时任务列表）
 * 使用 Spring @Scheduled，任务失败需在错误日志中留痕便于人工介入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private static final int AI_MESSAGE_RETENTION_MONTHS = 6;
    private static final int APPOINTMENT_RETENTION_YEARS = 2;

    /** 仪表盘按天计数 key 保留 8 天 */
    private static final Duration DAILY_TTL = Duration.ofDays(8);

    private final PointsTransactionMapper pointsTransactionMapper;
    private final UserMapper userMapper;
    private final AiConversationMessageMapper aiConversationMessageMapper;
    private final AppointmentMapper appointmentMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheManager cacheManager;

    /**
     * 每天凌晨 2 点：清理 1 年前获取且未使用的积分
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredPoints() {
        List<PointsTransaction> expired = pointsTransactionMapper.selectExpired();
        int cleanedCount = 0;
        for (PointsTransaction tx : expired) {
            User user = userMapper.selectSafeById(tx.getUserId());
            if (user == null) {
                pointsTransactionMapper.deleteById(tx.getId());
                continue;
            }
            int balance = user.getPoints() == null ? 0 : user.getPoints();
            int deduct = Math.min(tx.getChangeAmount(), balance);
            if (deduct > 0) {
                userMapper.deductPoints(tx.getUserId(), deduct);
                recordExpire(tx.getUserId(), deduct, user.getPoints() - deduct);
                cleanedCount++;
            }
            pointsTransactionMapper.deleteById(tx.getId());
        }
        log.info("[定时任务] 清理过期积分完成，处理 {} 笔，共清理 {} 笔", expired.size(), cleanedCount);
    }

    /**
     * 每天凌晨 3 点：清理 6 个月前的 AI 对话消息
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldAiMessages() {
        LocalDateTime before = LocalDateTime.now().minusMonths(AI_MESSAGE_RETENTION_MONTHS);
        int count = aiConversationMessageMapper.deleteBefore(before);
        if (count > 0) {
            log.info("[定时任务] 清理历史 AI 对话消息 {} 条", count);
        }
    }

    /**
     * 每天凌晨 4 点：归档 2 年前的历史预约
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void archiveOldAppointments() {
        LocalDateTime before = LocalDateTime.now().minusYears(APPOINTMENT_RETENTION_YEARS);
        int appointments = appointmentMapper.archiveBefore(before);
        int assessments = assessmentResultMapper.archiveBefore(before);
        log.info("[定时任务] 归档历史预约 {} 条、历史评测 {} 条", appointments, assessments);
    }


    /**
     * 每天 0:30：汇总昨日健康数据录入
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void syncHealthDailyStats() {
        long count = healthRecordMapper.countByDate(LocalDate.now().minusDays(1));
        log.info("[定时任务] 昨日健康数据录入统计：{} 条", count);
    }

    private void recordExpire(Long userId, int amount, int balanceAfter) {
        PointsTransaction expireTx = new PointsTransaction();
        expireTx.setUserId(userId);
        expireTx.setChangeAmount(-amount);
        expireTx.setBalanceAfter(balanceAfter);
        expireTx.setSource(PointsSource.EXPIRE.name());
        expireTx.setExpireTime(LocalDateTime.now());
        pointsTransactionMapper.insert(expireTx);
    }


    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨 3 点
    public void cleanExpiredRefreshTokens() {
        int deleted = refreshTokenMapper.deleteExpired();
        log.info("清理过期 Refresh Token: {} 条", deleted);
    }

    /**
     * 每小时整点：仪表盘统计对账
     *
     * 以 DB 为唯一事实源，校准 Redis 按天计数与总量计数，消除
     * 写侧 INCR 因异常/重启/漏写等造成的漂移，保障最终一致性。
     * 校准完成后强制刷新看板整块缓存，下次访问回表重建。
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void reconcileDashboardStats() {
        LocalDate today = LocalDate.now();

        // 1. 按天指标（当日累计）
        reconcileDaily(DashboardStatKeys.METRIC_APPOINTMENT, today, appointmentMapper.countToday());
        reconcileDaily(DashboardStatKeys.METRIC_REGISTRATION, today, activityRegistrationMapper.countToday());
        reconcileDaily(DashboardStatKeys.METRIC_NEW_MEMBER, today, userMapper.countTodayNewMembers());

        // 2. 总量指标（只在此时刷新）
        reconcileTotal(DashboardStatKeys.METRIC_MEMBER_TOTAL, userMapper.countMembers());
        reconcileTotal(DashboardStatKeys.METRIC_PENDING_APPOINTMENT, appointmentMapper.countPending());

        // 3. 强制刷新看板整块缓存
        evictOverviewCache();

        log.info("[定时任务] 仪表盘统计对账完成");
    }

    /**
     * 校准按天计数：与 DB 真实值比对，不一致则覆盖并留痕。
     */
    private void reconcileDaily(String metric, LocalDate date, long dbValue) {
        String key = DashboardStatKeys.daily(metric, date);
        String cached = stringRedisTemplate.opsForValue().get(key);
        long cachedValue = 0L;
        boolean hasCached = cached != null;
        if (hasCached) {
            try {
                cachedValue = Long.parseLong(cached);
            } catch (NumberFormatException e) {
                log.warn("仪表盘计数格式异常，覆盖修正，key={}, value={}", key, cached);
            }
        }
        if (!hasCached || cachedValue != dbValue) {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(dbValue), DAILY_TTL);
            log.warn("仪表盘对账修正：key={}, redis={}, db={}", key, cachedValue, dbValue);
        }
    }

    /**
     * 校准总量计数：与 DB 真实值比对，不一致则覆盖并留痕。
     */
    private void reconcileTotal(String metric, long dbValue) {
        String key = DashboardStatKeys.total(metric);
        String cached = stringRedisTemplate.opsForValue().get(key);
        long cachedValue = 0L;
        boolean hasCached = cached != null;
        if (hasCached) {
            try {
                cachedValue = Long.parseLong(cached);
            } catch (NumberFormatException e) {
                log.warn("仪表盘总量计数格式异常，覆盖修正，key={}, value={}", key, cached);
            }
        }
        if (!hasCached || cachedValue != dbValue) {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(dbValue));
            log.warn("仪表盘总量对账修正：key={}, redis={}, db={}", key, cachedValue, dbValue);
        }
    }

    /**
     * 清除看板整块缓存，使下次访问回表重建。
     */
    private void evictOverviewCache() {
        try {
            Cache cache = cacheManager.getCache("dashboard");
            if (cache != null) {
                cache.evict("overview");
            }
        } catch (Exception e) {
            log.warn("清除仪表盘缓存失败：{}", e.getMessage());
        }
    }
}
