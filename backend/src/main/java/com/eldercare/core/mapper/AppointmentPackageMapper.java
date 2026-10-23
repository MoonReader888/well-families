package com.eldercare.core.mapper;

import com.eldercare.core.entity.AppointmentPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 体检套餐表 Mapper
 */
@Mapper
public interface AppointmentPackageMapper {

    AppointmentPackage selectById(@Param("id") Long id);

    /** 会员端可见的启用套餐 */
    List<AppointmentPackage> selectEnabled();

    List<AppointmentPackage> selectList();

    /** 管理端分页查询套餐（按名称模糊搜索，默认按 id 升序） */
    List<AppointmentPackage> selectPackagePage(@Param("name") String name);

    int insert(AppointmentPackage appointmentPackage);

    int update(AppointmentPackage appointmentPackage);

    int logicDelete(@Param("id") Long id);
}
