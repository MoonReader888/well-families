package com.eldercare.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 趋势数据点（按月份聚合，趋势分析）
 */
@Data
@AllArgsConstructor//自动生成全参构造函数
public class TrendPointVO {

    /** 月份，如 2026-03 */
    private String month;

    /** 当月平均值 */
    private BigDecimal avg;
}
