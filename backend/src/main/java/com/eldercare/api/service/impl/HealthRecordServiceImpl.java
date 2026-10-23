package com.eldercare.api.service.impl;

import com.eldercare.api.dto.CreateHealthRecordRequest;
import com.eldercare.api.service.HealthRecordService;
import com.eldercare.api.vo.HealthTrendVO;
import com.eldercare.api.vo.TrendPointVO;
import com.eldercare.core.entity.HealthGuidance;
import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.entity.Message;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.GuidanceType;
import com.eldercare.core.enums.MessageType;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.HealthGuidanceMapper;
import com.eldercare.core.mapper.HealthRecordMapper;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.AiService;
import com.eldercare.core.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 健康记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordServiceImpl implements HealthRecordService {

    private static final int TREND_MONTHS = 6;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final HealthRecordMapper healthRecordMapper;
    private final UserMapper userMapper;
    private final HealthGuidanceMapper healthGuidanceMapper;
    private final MessageMapper messageMapper;
    private final AiService aiService;

    /**
     * 注入自身代理：@Async / @Transactional 必须经过 Spring 代理才生效，
     * 同类内 this 直接调用会绕过代理，导致异步与独立事务失效。
     */
    @Lazy
    @Autowired
    private HealthRecordServiceImpl self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecord create(CreateHealthRecordRequest request) {
        Long userId = UserContext.currentUserId();
        User user = userMapper.selectSafeById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setSystolic(request.getSystolic());
        record.setDiastolic(request.getDiastolic());
        record.setBloodSugar(request.getBloodSugar());
        record.setHeartRate(request.getHeartRate());
        record.setWeight(request.getWeight());
        record.setMemo(request.getMemo());
        record.setRecordedAt(LocalDateTime.now());

        // 计算 BMI
        if (request.getWeight() != null && user.getHeight() != null) {
            BigDecimal heightM = user.getHeight().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal bmi = request.getWeight()
                    .divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
            record.setBmi(bmi);
        }

        healthRecordMapper.insert(record);
        log.info("录入健康数据: userId={}, recordId={}", userId, record.getId());

        // 指标异常时触发健康提醒（走自身代理，异步独立事务）
        self.generateHealthReminderAsync(userId, record);
        return record;
    }

    @Override
    public List<HealthRecord> history() {
        Long userId = UserContext.currentUserId();
        return healthRecordMapper.selectByUserId(userId);
    }

    @Override
    public HealthRecord detail(Long id) {
        Long userId = UserContext.currentUserId();
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("健康记录不存在");
        }
        return record;
    }

    /**
     * 健康趋势分析
     */
    @Override
    public List<HealthTrendVO> trend() {
        Long userId = UserContext.currentUserId();
        LocalDateTime start = LocalDateTime.now().minusMonths(TREND_MONTHS);
        List<HealthRecord> records = healthRecordMapper.selectByUserAndTime(userId, start);

        // key: 指标名称, value: 该指标的所有值列表
        Map<String, List<BigDecimal>> valuesMap = new HashMap<>();
        // key: 指标名称, value: Map<月份, 该月所有值列表>
        Map<String, Map<String, List<BigDecimal>>> monthMap = new HashMap<>();

        for (HealthRecord record : records) {
            collectIndicatorData(record, "systolic", record.getSystolic(), valuesMap, monthMap);
            collectIndicatorData(record, "diastolic", record.getDiastolic(), valuesMap, monthMap);
            collectIndicatorData(record, "bloodSugar", record.getBloodSugar(), valuesMap, monthMap);
            collectIndicatorData(record, "heartRate", record.getHeartRate(), valuesMap, monthMap);
            collectIndicatorData(record, "weight", record.getWeight(), valuesMap, monthMap);
            collectIndicatorData(record, "bmi", record.getBmi(), valuesMap, monthMap);
        }

        List<HealthTrendVO> result = new ArrayList<>();
        String[] indicators = {"systolic", "diastolic", "bloodSugar", "heartRate", "weight", "bmi"};
        for (String indicator : indicators) {
            result.add(buildTrendFromMap(indicator, valuesMap.get(indicator), monthMap.get(indicator)));
        }
        return result;
    }


    private void collectIndicatorData(HealthRecord record, String indicator, Number value,
                                      Map<String, List<BigDecimal>> valuesMap,
                                      Map<String, Map<String, List<BigDecimal>>> monthMap) {
        if (value == null) {
            return;
        }
        BigDecimal v = new BigDecimal(value.toString());

        // 添加到总列表
        valuesMap.computeIfAbsent(indicator, k -> new ArrayList<>()).add(v);

        // 添加到月份分组
        String month = record.getRecordedAt().format(MONTH_FORMAT);
        Map<String, List<BigDecimal>> indicatorMonthMap = monthMap.computeIfAbsent(indicator, k -> new HashMap<>());
        indicatorMonthMap.computeIfAbsent(month, k -> new ArrayList<>()).add(v);
    }

    /**
     * 构建健康趋势VO
     */
    private HealthTrendVO buildTrendFromMap(String indicator,
                                            List<BigDecimal> values,
                                            Map<String, List<BigDecimal>> monthData) {
        HealthTrendVO vo = new HealthTrendVO();
        vo.setIndicator(indicator);

        if (values == null || values.isEmpty()) {
            vo.setPoints(List.of());
            return vo;
        }

        vo.setAvg(average(values));
        vo.setMax(values.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO));
        vo.setMin(values.stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO));

        if (monthData != null && !monthData.isEmpty()) {
            List<TrendPointVO> points = monthData.entrySet().stream()
                    .map(e -> new TrendPointVO(e.getKey(), average(e.getValue())))
                    .sorted(Comparator.comparing(TrendPointVO::getMonth))
                    .collect(Collectors.toList());
            vo.setPoints(points);
        } else {
            vo.setPoints(List.of());
        }
        return vo;
    }

    private BigDecimal average(List<BigDecimal> values) {
        if(values==null||values.isEmpty()){
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }

    /**
     * 异步生成健康提醒
     *
     * @param userId
     * @param record
     */
    @Async("healthReminder")
    @Transactional(propagation = Propagation.REQUIRES_NEW) //独立事务
    public void generateHealthReminderAsync(Long userId, HealthRecord record) {
        List<String> abnormal = new ArrayList<>();
        if (record.getSystolic() != null) {
            int v = record.getSystolic();
            if (v < 90 || v >= 140) abnormal.add("收缩压 " + v + " mmHg");
        }
        if (record.getDiastolic() != null) {
            int v = record.getDiastolic();
            if (v < 60 || v >= 90) abnormal.add("舒张压 " + v + " mmHg");
        }
        if (record.getBloodSugar() != null) {
            BigDecimal v = record.getBloodSugar();
            if (v.compareTo(new BigDecimal("3.9")) < 0 || v.compareTo(new BigDecimal("6.1")) > 0) {
                abnormal.add("空腹血糖 " + v + " mmol/L");
            }
        }
        if (record.getHeartRate() != null) {
            int v = record.getHeartRate();
            if (v < 60 || v > 100) abnormal.add("心率 " + v + " 次/分");
        }
        if (record.getBmi() != null) {
            BigDecimal v = record.getBmi();
            if (v.compareTo(new BigDecimal("18.5")) < 0 || v.compareTo(new BigDecimal("24")) >= 0) {
                abnormal.add("BMI " + v);
            }
        }
        // 没有异常指标，不生成健康提醒
        if (abnormal.isEmpty()) {
            return;
        }
        //如果当天已经推送过健康提醒，不重复推送
        if (healthGuidanceMapper.countToday(userId) > 0) {
            return;
        }

        // 生成健康提醒
        try {
            String prompt = "用户健康指标异常：" + String.join("、", abnormal) +
                    "。请给出简单易懂的个性化健康建议，包括需要注意的事项和就医建议（100 字以内，尽量简短，不用多余格式符号）。";
            String advice = aiService.chat("你是一位专业的健康顾问，请用亲切、易懂的语言回答。",
                    List.of(new AiService.ChatMessage("user", prompt)));
            if (advice == null || advice.isBlank()) {
                log.warn("AI 健康建议返回为空，userId={}", userId);
                return;
            }

            // 健康指导记录生成
            HealthGuidance guidance = new HealthGuidance();
            guidance.setUserId(userId);
            guidance.setType(GuidanceType.DATA_SUMMARY.name());
            guidance.setContent(advice);
            guidance.setIsRead(0);
            healthGuidanceMapper.insert(guidance);

            // 推送健康提醒
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle("健康提醒");
            message.setContent(advice);
            message.setType(MessageType.HEALTH_REMINDER.name());
            message.setIsRead(0);
            messageMapper.insert(message);
            log.info("健康提醒已生成: userId={}, recordId={}", userId, record.getId());
        } catch (Exception e) {
            // AI 异常不影响健康数据保存
            log.error("健康提醒生成失败: userId={}", userId, e);
        }
    }
}
