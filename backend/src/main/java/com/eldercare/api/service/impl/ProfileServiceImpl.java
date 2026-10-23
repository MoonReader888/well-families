package com.eldercare.api.service.impl;

import com.eldercare.api.dto.ChangePasswordRequest;
import com.eldercare.api.dto.UpdateProfileRequest;
import com.eldercare.api.service.ProfileService;
import com.eldercare.api.vo.UploadFileVO;
import com.eldercare.api.vo.UserVO;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.FileStorageService;
import com.eldercare.core.util.PasswordUtil;
import com.eldercare.core.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.eldercare.core.mapper.RefreshTokenMapper;
import com.eldercare.core.service.TokenBlacklistService;
import com.eldercare.core.util.JwtUtil;
import org.springframework.transaction.annotation.Transactional;


import java.util.Set;

/**
 * 会员中心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    /** 头像允许的 MIME 类型 */
    private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /** 头像大小上限：5MB（与前端 Profile.vue max-size 一致） */
    private static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024L;

    /** 静态资源访问前缀（与 CorsConfig 中 /uploads/** 映射对应） */
    private static final String UPLOADS_URL_PREFIX = "/uploads/";

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final RefreshTokenMapper refreshTokenMapper;
    private final TokenBlacklistService blacklistService;
    private final JwtUtil jwtUtil;



    @Override
    public UserVO getUserInfo() {
        User user = userMapper.selectSafeById(UserContext.currentUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.from(user);
    }

    @Override
    public void updateProfile(UpdateProfileRequest request) {
        Long userId = UserContext.currentUserId();
        int affected = userMapper.updateProfile(userId, request);
        if (affected == 0) {
            throw new BusinessException("更新个人信息失败");
        }
        log.info("更新个人信息: userId={}", userId);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Long userId = UserContext.currentUserId();
    
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        PasswordUtil.assertStrong(request.getNewPassword());
        userMapper.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));

        refreshTokenMapper.deleteByUserId(userId);
        blacklistService.blacklistUser(userId, jwtUtil.getRefreshTokenExpire());
        log.info("修改密码成功: userId={}", userId);
    }

    @Override
    public UploadFileVO uploadAvatar(MultipartFile file) {
        Long userId = UserContext.currentUserId();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("头像仅支持 JPG/PNG/GIF/WEBP 格式");
        }
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new BusinessException("头像不能超过 5MB");
        }

        // 存储文件，返回相对路径（如 avatar/202608/xxx.jpg），落库用
        String relativePath = fileStorageService.store(file, "avatar");
        String url = UPLOADS_URL_PREFIX + relativePath;

        log.info("上传头像成功: userId={}, path={}, size={}", userId, relativePath, file.getSize());
        return new UploadFileVO(relativePath, url);
    }
}
