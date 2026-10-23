package com.eldercare.api.service;

import com.eldercare.api.dto.CreateHealthRecordRequest;
import com.eldercare.api.vo.HealthTrendVO;
import com.eldercare.core.entity.HealthRecord;

import java.util.List;

/**
 * 健康记录服务
 */
public interface HealthRecordService {

    /**
     * 录入健康数据（自动计算 BMI，指标异常时触发健康提醒）
     */
    HealthRecord create(CreateHealthRecordRequest request);

    /**
     * 历史健康记录（倒序）
     */
    List<HealthRecord> history();

    /**
     * 记录详情（校验归属）
     */
    HealthRecord detail(Long id);

    /**
     * 最近 6 个月趋势分析
     */
    List<HealthTrendVO> trend();
}
