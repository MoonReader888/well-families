package com.eldercare.core.mapper;

import com.eldercare.core.entity.HealthGuidance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 健康指导表 Mapper
 */
@Mapper
public interface HealthGuidanceMapper {

    HealthGuidance selectById(@Param("id") Long id);

    List<HealthGuidance> selectByUserId(@Param("userId") Long userId);

    int insert(HealthGuidance guidance);

    /** 标记已读 */
    int markRead(@Param("id") Long id);

    /** 当日是否已生成健康指导（健康提醒去重） */
    int countToday(@Param("userId") Long userId);

    int logicDelete(@Param("id") Long id);
}
