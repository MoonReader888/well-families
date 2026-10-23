package com.eldercare.core.mapper;

import com.eldercare.core.entity.ActivityRegistration;
import com.eldercare.core.vo.AdminRegistrationVO;
import com.eldercare.core.vo.DateCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 活动报名表 Mapper（uk_user_activity 防重复报名）
 */
@Mapper
public interface ActivityRegistrationMapper {

    ActivityRegistration selectById(@Param("id") Long id);

    List<ActivityRegistration> selectByUserId(@Param("userId") Long userId);

    ActivityRegistration selectByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    List<ActivityRegistration> selectByActivityId(@Param("activityId") Long activityId);

    int insert(ActivityRegistration registration);

    /** 管理端活动报名列表 */
    List<AdminRegistrationVO> selectAdminByActivityId(@Param("activityId") Long activityId);


    /** 签到 */
    int checkIn(@Param("id") Long id);

    /** 今日报名数（仪表盘） */
    long countToday();

    /** 仪表盘：指定日期区间内每日报名数 */
    List<DateCountVO> countBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    int logicDelete(@Param("id") Long id);
}
