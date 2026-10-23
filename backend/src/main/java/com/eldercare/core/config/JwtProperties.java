package com.eldercare.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥（HS256，长度 ≥ 32 字节） */
    private String secret;

    /** Access Token 有效期（秒） */
    private long accessTokenExpire;

    /** Refresh Token 有效期（秒） */
    private long refreshTokenExpire;
}
