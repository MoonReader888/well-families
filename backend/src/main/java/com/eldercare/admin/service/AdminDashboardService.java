package com.eldercare.admin.service;

import com.eldercare.admin.vo.DashboardVO;

/**
 * 管理端仪表盘服务
 */
public interface AdminDashboardService {

    /**
     * 系统数据概览
     */
    DashboardVO getOverview();
}
