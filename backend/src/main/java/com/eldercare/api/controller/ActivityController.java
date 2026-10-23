package com.eldercare.api.controller;

import com.eldercare.api.service.ActivityService;
import com.eldercare.api.vo.ActivityVO;
import com.eldercare.core.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区活动接口（/api/member/activity）
 */
@RestController
@RequestMapping("/api/member/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /** 活动列表 */
    @GetMapping
    public Result<List<ActivityVO>> list() {
        return Result.success(activityService.list());
    }

    /** 活动详情 */
    @GetMapping("/{activityId}")
    public Result<ActivityVO> detail(@PathVariable Long activityId) {
        return Result.success(activityService.detail(activityId));
    }

    /** 报名 */
    @PostMapping("/{activityId}/register")
    public Result<Void> register(@PathVariable Long activityId) {
        activityService.register(activityId);
        return Result.success();
    }

    /** 取消报名 */
    @DeleteMapping("/{activityId}/register")
    public Result<Void> cancelRegister(@PathVariable Long activityId) {
        activityService.cancelRegister(activityId);
        return Result.success();
    }

    /** 我的活动 */
    @GetMapping("/my")
    public Result<List<ActivityVO>> myActivities() {
        return Result.success(activityService.myActivities());
    }

    /** 签到状态 */
    @GetMapping("/{activityId}/checkin")
    public Result<ActivityVO> checkInStatus(@PathVariable Long activityId) {
        return Result.success(activityService.checkInStatus(activityId));
    }

    /** 签到 */
    @PostMapping("/{activityId}/checkin")
    public Result<Void> checkIn(@PathVariable Long activityId) {
        activityService.checkIn(activityId);
        return Result.success();
    }
}
