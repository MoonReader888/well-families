package com.eldercare.core.mapper;

import com.eldercare.core.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 刷新令牌表 Mapper
 * 注销/登出时物理删除，不设逻辑删除字段
 */
@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);

    RefreshToken selectByToken(@Param("token") String token);

    int deleteByToken(@Param("token") String token);

    /** 强制该用户所有设备下线 */
    int deleteByUserId(@Param("userId") Long userId);

    /** 清理过期令牌（每日定时任务） */
    int deleteExpired();
}
