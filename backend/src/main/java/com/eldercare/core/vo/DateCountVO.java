package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 日期计数 VO（仪表盘近7日趋势统计用）
 */
@Data
public class DateCountVO implements Serializable {

    private LocalDate statDate;

    private long count;
}
