package com.eldercare.core.mapper;

import com.eldercare.core.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置表 Mapper
 */
@Mapper
public interface SysConfigMapper {

    SysConfig selectByKey(@Param("configKey") String configKey);

    List<SysConfig> selectAll();

    int insert(SysConfig sysConfig);

    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
