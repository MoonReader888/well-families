package com.eldercare.admin.controller;

import com.eldercare.admin.dto.AdminConfigUpdateRequest;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.SysConfig;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.service.SysConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端系统配置接口（/api/admin/config）
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SysConfigService sysConfigService;

    /** 配置项列表 */
    @GetMapping
    public Result<List<SysConfig>> list() {
        return Result.success(sysConfigService.list());
    }

    /** 获取单个配置项数值 */
    @GetMapping("/{configKey}")
    public Result<SysConfig> get(@PathVariable String configKey) {
        SysConfig config = sysConfigService.list().stream()
                .filter(c -> c.getConfigKey().equals(configKey))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("配置项不存在"));
        return Result.success(config);
    }

    /** 更新配置项 */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AdminConfigUpdateRequest request) {
        sysConfigService.update(request.getConfigKey(), request.getConfigValue());
        return Result.success();
    }
}
