package com.eldercare.api.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录/刷新响应（双 Token + 用户信息）
 */
@Data
@Builder
public class TokenVO {

    /** Access Token（2 小时） */
    private String accessToken;

    /** Refresh Token（7 天） */
    private String refreshToken;

    /** Token 类型 */
    private String tokenType;

    /** Access Token 有效期（秒） */
    private long accessExpiresIn;

    /** 用户信息 */
    private UserVO user;
}
