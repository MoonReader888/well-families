package com.eldercare.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置
 */
@Configuration
@MapperScan("com.eldercare.core.mapper")
public class MyBatisConfig {
}
