package com.eldercare.admin.controller;

import com.eldercare.admin.dto.AdminBatchPushMessageRequest;
import com.eldercare.admin.dto.AdminPushMessageRequest;
import com.eldercare.admin.service.AdminMessageService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.vo.AdminMessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 管理端消息管理接口（/api/admin/message）
 */
@RestController
@RequestMapping("/api/admin/message")
@RequiredArgsConstructor
public class AdminMessageController {

    private final AdminMessageService adminMessageService;

    /** 消息分页列表 */
    @GetMapping
    public Result<PageResult<AdminMessageVO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) String phone) {
        return Result.success(adminMessageService.page(pageNum, pageSize, phone));
    }

    /** 消息详情 */
    @GetMapping("/{messageId}")
    public Result<AdminMessageVO> detail(@PathVariable Long messageId) {
        return Result.success(adminMessageService.detail(messageId));
    }

    /** 删除消息 */
    @DeleteMapping("/{messageId}")
    public Result<Void> delete(@PathVariable Long messageId) {
        adminMessageService.delete(messageId);
        return Result.success();
    }

    /** 向单个用户推送 */
    @PostMapping("/push")
    public Result<Void> pushSingle(@Valid @RequestBody AdminPushMessageRequest request) {
        adminMessageService.pushSingle(request);
        return Result.success();
    }

    /** 批量推送 */
    @PostMapping("/push/batch")
    public Result<Void> pushBatch(@Valid @RequestBody AdminBatchPushMessageRequest request) {
        adminMessageService.pushBatch(request);
        return Result.success();
    }

    /** 批量推送目标人数预览（按条件筛选） */
    @GetMapping("/push/count")
    public Result<Long> pushCount(@RequestParam(required = false) String memberLevel,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(adminMessageService.countTarget(memberLevel, status, startTime, endTime));
    }
}
