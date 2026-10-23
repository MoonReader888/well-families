package com.eldercare.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * 客户端 IP 获取工具（用于登录/注册限流）
 */
public final class IpUtil {

    private static final String UNKNOWN = "unknown";

    private IpUtil() {
    }
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多个代理时取第一个真实 IP
            return ip.split(",")[0].trim();
        }
        //其次取 X-Real-IP（Nginx 常用）
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        //最后取请求的 RemoteAddr（直接连接客户端）
        return request.getRemoteAddr();
    }
}
