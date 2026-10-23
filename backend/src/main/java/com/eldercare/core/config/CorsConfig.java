package com.eldercare.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Web 配置：跨域 + 静态资源映射
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    //可能有多个域名
    private final List<String> allowedOrigins;

    //一个路径
    private final String uploadDir;

    public CorsConfig(
            @Value("${eldercare.cors.allowed-origins}") String allowedOrigins,
            @Value("${eldercare.file.upload-dir}") String uploadDir) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s->!s.isEmpty()).toList();
        this.uploadDir = uploadDir;
    }

    //跨域配置方法
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);//预检缓存一小时
    }

    //静态资源映射方法
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //转成绝对路径
        Path dir = Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(dir.toUri().toString());
    }
}
