package com.eldercare.admin.controller;

import com.eldercare.admin.dto.AdminAdjustPointsRequest;
import com.eldercare.admin.dto.AdminResetPasswordRequest;
import com.eldercare.admin.dto.AdminUpdateLevelRequest;
import com.eldercare.admin.dto.AdminMemberBatchStatusRequest;
import com.eldercare.admin.dto.AdminUpdateStatusRequest;
import com.eldercare.admin.service.AdminMemberService;
import com.eldercare.admin.vo.MemberDetailVO;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端会员管理接口（/api/admin/members）
 */
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    /** 分页查询会员列表 */
    @GetMapping
    public Result<PageResult<User>> page(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String realName,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String memberLevel) {
        return Result.success(adminMemberService.page(pageNum, pageSize, phone, realName, status, memberLevel));
    }

    /**
     * 会员详情
     */
    @GetMapping("/{memberId}")
    public Result<MemberDetailVO> detail(@PathVariable Long memberId) {
        return Result.success(adminMemberService.detail(memberId));
    }

    /**
     * 启用/禁用会员
     */
    @PutMapping("/{memberId}/status")
    public Result<Void> updateStatus(@PathVariable Long memberId,
                                     @Valid @RequestBody AdminUpdateStatusRequest request) {
        adminMemberService.updateStatus(memberId, request.getStatus());
        return Result.success();
    }

    /**
     * 批量启用/禁用会员
     */
    @PutMapping("/status/batch")
    public Result<Integer> updateStatusBatch(@Valid @RequestBody AdminMemberBatchStatusRequest request) {
        return Result.success(adminMemberService.updateStatusBatch(request.getIds(), request.getStatus()));
    }

    /** 调整会员等级 */
    @PutMapping("/{memberId}/level")
    public Result<Void> updateLevel(@PathVariable Long memberId,
                                    @Valid @RequestBody AdminUpdateLevelRequest request) {
        adminMemberService.updateLevel(memberId, request.getMemberLevel());
        return Result.success();
    }

    /** 手动调整积分 */
    @PutMapping("/{memberId}/points")
    public Result<Void> adjustPoints(@PathVariable Long memberId,
                                     @Valid @RequestBody AdminAdjustPointsRequest request) {
        adminMemberService.adjustPoints(memberId, request.getPoints(), request.getReason());
        return Result.success();
    }

    /** 重置会员密码 */
    @PutMapping("/{memberId}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long memberId,
                                      @Valid @RequestBody AdminResetPasswordRequest request) {
        adminMemberService.resetPassword(memberId, request.getNewPassword());
        return Result.success();
    }
}
