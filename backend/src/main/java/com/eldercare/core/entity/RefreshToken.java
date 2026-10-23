package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷新令牌实体（refresh_token 表）
 * 登出/注销时物理删除，无逻辑删除字段
 */
@Data
public class RefreshToken implements Serializable {

    private Long id;

    private Long userId;

    /** Refresh Token 值 */
    private String token;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
