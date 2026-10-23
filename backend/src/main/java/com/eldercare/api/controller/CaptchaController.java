package com.eldercare.api.controller;

import com.eldercare.core.service.RateLimitService;
import com.eldercare.core.util.IpUtil;
import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;

/**
 * 图形验证码接口
 * 答案小写存入 Redis（key: captcha:{id}）
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CaptchaController {

    public static final String CAPTCHA_PREFIX = "captcha:";
    public static final String CAPTCHA_IP_PREFIX = "rl:captcha:ip:";
    public static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitService rateLimitService;

    @GetMapping("/captcha")
    public void captcha(@RequestParam("id") String id,
                        HttpServletRequest request,
                        HttpServletResponse resp) throws IOException {
        // 按 IP 限流，避免验证码接口被刷
        rateLimitService.check(CAPTCHA_IP_PREFIX + IpUtil.getClientIp(request), 10, 60);

        // 生成验证码：4 位，宽 130，高 48
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);

        // 答案小写存入 Redis，TTL 5 分钟
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + id, captcha.text().toLowerCase(), CAPTCHA_TTL);

        // 设置响应头：告诉浏览器这是一张图片，并禁止缓存
        resp.setContentType("image/gif");
        resp.setHeader("Pragma", "No-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0L);

        // 将验证码图片输出到响应流
        captcha.out(resp.getOutputStream());
    }
}
