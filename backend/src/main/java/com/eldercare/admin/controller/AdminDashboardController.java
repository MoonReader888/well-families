package com.eldercare.admin.controller;

import com.eldercare.admin.service.AdminDashboardService;
import com.eldercare.admin.vo.DashboardVO;
import com.eldercare.core.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端仪表盘接口（/api/admin/dashboard）
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /** 数据概览统计 */
    @GetMapping
    public Result<DashboardVO> getOverview() {
        return Result.success(adminDashboardService.getOverview());
    }
}
