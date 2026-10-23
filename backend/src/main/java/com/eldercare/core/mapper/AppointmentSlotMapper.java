package com.eldercare.core.mapper;

import com.eldercare.core.entity.AppointmentSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约时段表 Mapper
 */
@Mapper
public interface AppointmentSlotMapper {

    AppointmentSlot selectById(@Param("id") Long id);

    List<AppointmentSlot> selectByPackageAndDate(@Param("packageId") Long packageId, @Param("appointDate") LocalDate appointDate);

    /** 某套餐下的全部时段（按日期、时间段排序） */
    List<AppointmentSlot> selectByPackage(@Param("packageId") Long packageId);

    int insert(AppointmentSlot slot);

    /** 编辑时段基本信息 */
    int update(AppointmentSlot slot);

    /** 时段是否已存在 */
    int countExists(@Param("packageId") Long packageId,
                    @Param("appointDate") LocalDate appointDate,
                    @Param("timeRange") String timeRange);

    /** 并发预约名额占用：行锁保证不超卖 */
    int occupySlot(@Param("id") Long id);

    /** 取消预约回退名额 */
    int releaseSlot(@Param("id") Long id);

    int logicDelete(@Param("id") Long id);
}
