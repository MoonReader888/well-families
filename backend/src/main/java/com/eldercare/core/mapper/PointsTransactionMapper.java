package com.eldercare.core.mapper;

import com.eldercare.core.entity.PointsTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分流水表 Mapper（设计文档外新增，见 README 决策说明）
 */
@Mapper
public interface PointsTransactionMapper {

    int insert(PointsTransaction transaction);

    List<PointsTransaction> selectByUserId(@Param("userId") Long userId);

    /** 查询已过期的正向积分（用于每日过期清理） */
    List<PointsTransaction> selectExpired();

    int deleteById(@Param("id") Long id);
}
