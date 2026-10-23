package com.eldercare.core.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 仪表盘统计增量事件
 *
 * 正向业务（新增会员、预约、活动报名）落库成功后发布，
 * 由 {@code DashboardStatEventListener} 在事务提交后执行 Redis INCR。
 */
@Getter
@RequiredArgsConstructor
public class DashboardStatIncrEvent {

    /**
     * 指标类型，取 {@link com.eldercare.core.constant.DashboardStatKeys} 中的 METRIC_* 常量
     */
    private final String metric;
}
