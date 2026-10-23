package com.eldercare.api.service;

import com.eldercare.api.dto.ChangePasswordRequest;
import com.eldercare.api.dto.UpdateProfileRequest;
import com.eldercare.api.vo.UploadFileVO;
import com.eldercare.api.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 会员中心服务
 */
public interface ProfileService {

    /**
     * 获取当前登录用户信息
     */
    UserVO getUserInfo();

    /**
     * 更新个人信息（仅更新非空字段）
     */
    void updateProfile(UpdateProfileRequest request);

    /**
     * 修改密码（校验原密码）
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 上传头像（仅存储文件并返回路径，不写库；前端再通过 updateProfile 持久化）
     */
    UploadFileVO uploadAvatar(MultipartFile file);
}
