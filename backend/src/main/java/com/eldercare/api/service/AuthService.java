package com.eldercare.api.service;

import com.eldercare.api.dto.LoginRequest;
import com.eldercare.api.dto.RefreshTokenRequest;
import com.eldercare.api.dto.RegisterRequest;
import com.eldercare.api.dto.ResetPasswordRequest;
import com.eldercare.api.vo.TokenVO;

/**
 * 认证服务（认证授权模块）
 */
public interface AuthService {

    /**
     * 注册（赠送初始积分）
     */
    void register(RegisterRequest request, String ip);

    /**
     * 登录，返回双 Token
     */
    TokenVO login(LoginRequest request, String ip);

    /**
     * 刷新 Access Token（旋转 Refresh Token）
     */
    TokenVO refresh(RefreshTokenRequest request, String ip);

    /**
     * 登出：Access Token 加入黑名单，Refresh Token 删除
     *
     * @param accessToken  当前的 Access Token（可为空/过期，容错）
     * @param refreshToken 当前的 Refresh Token（必须提供）
     * @param ip           客户端 IP（用于限流）
     */
    void logout(String accessToken, String refreshToken, String ip);

    /**
     * 密码找回：重置后强制所有已登录设备下线
     */
    void resetPassword(ResetPasswordRequest request);
}
