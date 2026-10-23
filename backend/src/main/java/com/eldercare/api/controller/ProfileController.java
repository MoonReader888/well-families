package com.eldercare.api.controller;

import com.eldercare.api.dto.ChangePasswordRequest;
import com.eldercare.api.dto.UpdateProfileRequest;
import com.eldercare.api.service.ProfileService;
import com.eldercare.api.vo.UploadFileVO;
import com.eldercare.api.vo.UserVO;
import com.eldercare.core.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 会员中心接口（/api/member/profile）
 */
@RestController
@RequestMapping("/api/member/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /** 获取个人信息 */
    @GetMapping
    public Result<UserVO> getInfo() {
        return Result.success(profileService.getUserInfo());
    }

    /** 更新个人信息 */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UpdateProfileRequest request) {
        profileService.updateProfile(request);
        return Result.success();
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return Result.success();
    }

    /** 上传头像（返回相对路径与访问 URL，前端再通过 PUT /api/member/profile 持久化） */
    @PostMapping("/upload/avatar")
    public Result<UploadFileVO> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return Result.success(profileService.uploadAvatar(file));
    }
}
