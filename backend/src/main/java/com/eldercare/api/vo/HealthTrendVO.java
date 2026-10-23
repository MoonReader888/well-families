package com.eldercare.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 健康趋势响应（趋势分析）
 * 最近 6 个月各项指标的平均值、最大值、最小值及按月聚合数据
 */
@Data
public class HealthTrendVO {

    /** 指标：systolic/diastolic/bloodSugar/heartRate/weight/bmi */
    private String indicator;

    /** 6 个月平均值 */
    private BigDecimal avg;

    /** 6 个月最大值 */
    private BigDecimal max;

    /** 6 个月最小值 */
    private BigDecimal min;

    /** 按月聚合平均值（趋势图） */
    private List<TrendPointVO> points;
}
