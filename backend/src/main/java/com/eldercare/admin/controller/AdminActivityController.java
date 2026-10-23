package com.eldercare.admin.controller;

import com.eldercare.admin.dto.AdminActivityRequest;
import com.eldercare.admin.service.AdminActivityService;
import com.eldercare.api.vo.UploadFileVO;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.vo.AdminRegistrationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端活动管理接口（/api/admin/activity）
 */
@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    /** 活动分页列表（按标题搜索） */
    @GetMapping
    public Result<PageResult<CommunityActivity>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) String title) {
        return Result.success(adminActivityService.page(pageNum, pageSize, title));
    }

    /** 活动详情 */
    @GetMapping("/{activityId}")
    public Result<CommunityActivity> detail(@PathVariable Long activityId) {
        return Result.success(adminActivityService.detail(activityId));
    }

    /** 新增活动 */
    @PostMapping
    public Result<CommunityActivity> create(@Valid @RequestBody AdminActivityRequest request) {
        return Result.success(adminActivityService.create(request));
    }

    /** 修改活动 */
    @PutMapping("/{activityId}")
    public Result<Void> update(@PathVariable Long activityId,
                               @Valid @RequestBody AdminActivityRequest request) {
        adminActivityService.update(activityId, request);
        return Result.success();
    }

    /** 删除活动 */
    @DeleteMapping("/{activityId}")
    public Result<Void> delete(@PathVariable Long activityId) {
        adminActivityService.delete(activityId);
        return Result.success();
    }

    /** 上传活动封面图 */
    @PostMapping("/upload/cover")
    public Result<UploadFileVO> uploadCover(@RequestPart("file") MultipartFile file) {
        return Result.success(adminActivityService.uploadCover(file));
    }

    /** 活动报名分页列表（含签到情况） */
    @GetMapping("/{activityId}/registrations")
    public Result<PageResult<AdminRegistrationVO>> registrations(@PathVariable Long activityId,
                                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminActivityService.registrations(activityId, pageNum, pageSize));
    }
}
