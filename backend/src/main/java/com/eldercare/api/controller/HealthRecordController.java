package com.eldercare.api.controller;

import com.eldercare.api.dto.CreateHealthRecordRequest;
import com.eldercare.api.service.HealthRecordService;
import com.eldercare.api.vo.HealthTrendVO;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.HealthRecord;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健康记录接口（/api/member/health）
 */
@RestController
@RequestMapping("/api/member/health")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    /** 录入健康数据 */
    @PostMapping
    public Result<HealthRecord> create(@Valid @RequestBody CreateHealthRecordRequest request) {
        return Result.success(healthRecordService.create(request));
    }

    /** 历史健康记录 */
    @GetMapping
    public Result<List<HealthRecord>> history() {
        return Result.success(healthRecordService.history());
    }

    /** 历史记录详情 */
    @GetMapping("/{id}")
    public Result<HealthRecord> detail(@PathVariable Long id) {
        return Result.success(healthRecordService.detail(id));
    }

    /** 最近 6 个月趋势分析 */
    @GetMapping("/trend")
    public Result<List<HealthTrendVO>> trend() {
        return Result.success(healthRecordService.trend());
    }
}
