package com.eldercare.core.filter;

import com.eldercare.core.common.LoginUser;
import com.eldercare.core.common.Result;
import com.eldercare.core.exception.RateLimitException;
import com.eldercare.core.service.RateLimitService;
import com.eldercare.core.service.TokenBlacklistService;
import com.eldercare.core.util.JwtUtil;
import com.eldercare.core.util.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final RateLimitService rateLimitService;

    private static final long API_LIMIT = 60;

    /**
     * 无需登录的路径
     */
    private static final String[] PUBLIC_PREFIXES = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/password/reset",
            "/api/auth/logout",
            "/api/captcha",
            "/actuator",
            "/uploads"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = UriUtils.decode(request.getRequestURI(), StandardCharsets.UTF_8);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            writeError(response, 401, "未登录或登录已过期");
            return;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            if (blacklistService.isAccessTokenBlacklisted(claims.getId())) {
                writeError(response, 401, "Token 已失效，请重新登录");
                return;
            }

            LoginUser user = jwtUtil.toLoginUser(claims);

            if (blacklistService.isUserBlacklisted(user.userId(), claims.getIssuedAt())) {
                writeError(response, 401, "账号已下线，请重新登录");
                return;
            }

            if (path.startsWith("/api/admin/") && !user.isAdmin()) {
                writeError(response, 403, "无权限访问管理端接口");
                return;
            }

            UserContext.set(user);
            try {
                rateLimitService.check("rl:api:user:" + user.userId(), API_LIMIT, 60);
            } catch (RateLimitException e) {
                response.setHeader("Retry-After", String.valueOf(e.getRetryAfter()));
                writeError(response, 429, e.getMessage());
                return;
            }
            filterChain.doFilter(request,response);

        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            writeError(response, 401, "登录状态已失效，请重新登录");
        } finally {
            UserContext.clear();
        }
    }


    private boolean isPublicPath(String path) {
        if ("/error".equals(path)) {
            return true;
        }
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(Result.error(code, message)));
    }

}
