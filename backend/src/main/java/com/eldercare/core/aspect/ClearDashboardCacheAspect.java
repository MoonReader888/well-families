package com.eldercare.core.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 清除仪表盘缓存切面
 */
@Slf4j
@Aspect
@Component
public class ClearDashboardCacheAspect {

    @Autowired
    private CacheManager cacheManager;

    @Around("@annotation(com.eldercare.core.annnotation.ClearDashBoardCache)")
    public Object clearCache(ProceedingJoinPoint joinPoint) throws Throwable {
        //1. 执行原方法
        Object result = joinPoint.proceed();

        //2. 方法执行成功后，清除仪表盘缓存
        try{
            Cache cache = cacheManager.getCache("dashboard");
            if(cache!=null){
                cache.evict("overview");
                log.info("清除仪表盘缓存：方法={}",joinPoint.getSignature().getName());
            }
        }catch (Exception e){
            log.warn("清除仪表盘缓存失败：{}",e.getMessage());
        }

        return result;

    }

}
