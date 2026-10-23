package com.eldercare.api.controller;

import com.eldercare.api.dto.LoginRequest;
import com.eldercare.api.dto.RefreshTokenRequest;
import com.eldercare.api.dto.RegisterRequest;
import com.eldercare.api.dto.ResetPasswordRequest;
import com.eldercare.api.service.AuthService;
import com.eldercare.api.vo.TokenVO;
import com.eldercare.core.common.Result;
import com.eldercare.core.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（/api/auth）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册 */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request,
                                 HttpServletRequest servletRequest) {
        authService.register(request, IpUtil.getClientIp(servletRequest));
        return Result.success();
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginRequest request,
                                 HttpServletRequest servletRequest) {
        return Result.success(authService.login(request, IpUtil.getClientIp(servletRequest)));
    }

    /** 刷新 Token */
    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                   HttpServletRequest servletRequest) {
        return Result.success(authService.refresh(request, IpUtil.getClientIp(servletRequest)));
    }

    /** 登出（已放白名单，Access Token 过期也能调用；Refresh Token 必传作为身份锚点） */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                               @Valid @RequestBody RefreshTokenRequest request,
                               HttpServletRequest servletRequest) {
        String accessToken = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : null;
        authService.logout(accessToken, request.getRefreshToken(), IpUtil.getClientIp(servletRequest));
        return Result.success();
    }

    /** 密码找回 */
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success();
    }
}
