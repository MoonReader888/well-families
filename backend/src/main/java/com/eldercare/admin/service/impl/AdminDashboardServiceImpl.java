package com.eldercare.admin.service.impl;

import com.eldercare.admin.service.AdminDashboardService;
import com.eldercare.admin.vo.DashboardVO;
import com.eldercare.core.constant.DashboardStatKeys;
import com.eldercare.core.enums.AppointmentStatus;
import com.eldercare.core.enums.MemberLevel;
import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.DateCountVO;
import com.eldercare.core.vo.NameValueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端仪表盘服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    /** 趋势统计近 7 天（含今天） */
    private static final int TREND_DAYS = 7;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private final UserMapper userMapper;
    private final AppointmentMapper appointmentMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取仪表盘概览
     *
     * 数据来源：优先读 Redis 计数（写侧 INCR 维护 + 每小时对账校准），
     * 缺失或读取异常时回表重建，保证看板可用性。
     */
    @Cacheable(value = "dashboard", key = "'overview'")
    @Override
    public DashboardVO getOverview() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(TREND_DAYS - 1L);
        LocalDate end = today.plusDays(1); // 统计到今天

        // 优先读 Redis 计数，失败回退 DB
        long todayNewMembers = readDailyCount(DashboardStatKeys.METRIC_NEW_MEMBER,
                () -> userMapper.countTodayNewMembers());
        long todayAppointments = readDailyCount(DashboardStatKeys.METRIC_APPOINTMENT,
                () -> appointmentMapper.countToday());
        long todayActivityRegistrations = readDailyCount(DashboardStatKeys.METRIC_REGISTRATION,
                () -> activityRegistrationMapper.countToday());

        // 总量指标（会员总数、待完成预约数）由每小时对账任务刷新，读侧直接读 Redis，缺失回表
        long memberTotal = readTotalCount(DashboardStatKeys.METRIC_MEMBER_TOTAL,
                () -> userMapper.countMembers());
        long pendingAppointments = readTotalCount(DashboardStatKeys.METRIC_PENDING_APPOINTMENT,
                () -> appointmentMapper.countPending());

        // 近 7 日趋势：逐日读按天 key，缺失回表补
        Map<LocalDate, Long> newMemberMap = toMap(userMapper.countNewMembersBetween(start, end));
        Map<LocalDate, Long> appointmentMap = toMap(appointmentMapper.countBetween(start, end));
        Map<LocalDate, Long> registrationMap = toMap(activityRegistrationMapper.countBetween(start, end));

        List<String> days = new ArrayList<>();
        List<Long> newMemberTrend = new ArrayList<>();
        List<Long> appointmentTrend = new ArrayList<>();
        List<Long> registrationTrend = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate d = start.plusDays(i);
            days.add(d.format(DAY_FMT));
            newMemberTrend.add(readDailyCount(DashboardStatKeys.METRIC_NEW_MEMBER, d,
                    newMemberMap.getOrDefault(d, 0L)));
            appointmentTrend.add(readDailyCount(DashboardStatKeys.METRIC_APPOINTMENT, d,
                    appointmentMap.getOrDefault(d, 0L)));
            registrationTrend.add(readDailyCount(DashboardStatKeys.METRIC_REGISTRATION, d,
                    registrationMap.getOrDefault(d, 0L)));
        }

        return DashboardVO.builder()
                .memberTotal(memberTotal)
                .todayNewMembers(todayNewMembers)
                .todayAppointments(todayAppointments)
                .todayActivityRegistrations(todayActivityRegistrations)
                .pendingAppointments(pendingAppointments)
                .last7Days(days)
                .newMemberTrend(newMemberTrend)
                .appointmentTrend(appointmentTrend)
                .registrationTrend(registrationTrend)
                .appointmentStatus(translate(appointmentMapper.countByStatus(), AppointmentStatus.getLabelMap()))
                .memberLevels(translate(userMapper.countByMemberLevel(), MemberLevel.getLabelMap()))
                .build();
    }

    /**
     * 读今日按天计数：优先 Redis，缺失/异常回退 DB。
     */
    private long readDailyCount(String metric, java.util.function.LongSupplier dbFallback) {
        return readDailyCount(metric, LocalDate.now(), dbFallback.getAsLong());
    }

    /**
     * 读指定日期按天计数：优先 Redis，缺失/异常回退给定默认值（通常来自 DB 区间统计）。
     */
    private long readDailyCount(String metric, LocalDate date, long fallback) {
        String key = DashboardStatKeys.daily(metric, date);
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            log.warn("读取仪表盘计数失败，回退 DB，key={}", key, e);
        }
        return fallback;
    }

    /**
     * 读总量计数：优先 Redis，缺失/异常回退 DB。
     */
    private long readTotalCount(String metric, java.util.function.LongSupplier dbFallback) {
        String key = DashboardStatKeys.total(metric);
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            log.warn("读取仪表盘总量计数失败，回退 DB，key={}", key, e);
        }
        return dbFallback.getAsLong();
    }

    /**
     * 转换日期统计列表为日期映射
     */
    private Map<LocalDate, Long> toMap(List<DateCountVO> list) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (DateCountVO vo : list) {
            map.put(vo.getStatDate(), vo.getCount());
        }
        return map;
    }

    /**
     * 翻译名称值对列表，根据标签映射
     */
    private List<NameValueVO> translate(List<NameValueVO> list, Map<String, String> labels) {
        return list.stream().map(v -> {
            NameValueVO nv = new NameValueVO();
            nv.setName(labels.getOrDefault(v.getName(), v.getName()));
            nv.setValue(v.getValue());
            return nv;
        }).toList();
    }

}
