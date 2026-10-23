package com.eldercare.core.mapper;

import com.eldercare.core.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康记录表 Mapper
 */
@Mapper
public interface HealthRecordMapper {

    HealthRecord selectById(@Param("id") Long id);

    List<HealthRecord> selectByUserId(@Param("userId") Long userId);

    /** 趋势分析：最近 6 个月 */
    List<HealthRecord> selectByUserAndTime(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    int insert(HealthRecord record);

    int logicDelete(@Param("id") Long id);

    /** 每日统计：某天健康录入条数（同步健康数据每日统计） */
    long countByDate(@Param("date") LocalDate date);
}
