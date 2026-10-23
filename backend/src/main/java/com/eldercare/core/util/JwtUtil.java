package com.eldercare.core.util;

import com.eldercare.core.common.LoginUser;
import com.eldercare.core.config.JwtProperties;
import com.eldercare.core.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具
 */
@Component
public class JwtUtil {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_PHONE = "phone";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long accessTokenExpire;
    private final long refreshTokenExpire;

    public JwtUtil(JwtProperties properties) {
        //将yml配置的secret字符串转换成字节数组，生成HMAC-SHA的SecretKey
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpire = properties.getAccessTokenExpire();
        this.refreshTokenExpire = properties.getRefreshTokenExpire();
    }

    public String generateAccessToken(LoginUser user){
        return generateToken(user,accessTokenExpire);
    }

    public String generateRefreshToken(LoginUser user){
        return generateToken(user,refreshTokenExpire);
    }

    private String generateToken(LoginUser user,long expire){
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.userId()))
                .id(UUID.randomUUID().toString().replace("-",""))
                .claim(CLAIM_USER_ID, user.userId())
                .claim(CLAIM_PHONE, user.phone())
                .claim(CLAIM_ROLE, user.role().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expire * 1000))
                .signWith(key)
                .compact();
    }

    // 解析Token的Claims 校验格式、签名、过期时间
    public Claims parse(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public LoginUser toLoginUser(Claims claims){
        return new LoginUser(
                Long.valueOf(claims.get(CLAIM_USER_ID).toString()),
                claims.get(CLAIM_PHONE,String.class),
                UserRole.valueOf(claims.get(CLAIM_ROLE,String.class))
        );
    }

    public long getAccessTokenExpire(){
        return accessTokenExpire;
    }
    public long getRefreshTokenExpire(){
        return refreshTokenExpire;
    }
}
