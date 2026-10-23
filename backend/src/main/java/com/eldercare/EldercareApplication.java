package com.eldercare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI 智能养老社区管理系统
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class EldercareApplication {

    public static void main(String[] args) {
        SpringApplication.run(EldercareApplication.class, args);
    }
}
