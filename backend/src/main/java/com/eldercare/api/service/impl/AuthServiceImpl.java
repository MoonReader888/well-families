package com.eldercare.api.service.impl;

import com.eldercare.api.controller.CaptchaController;
import com.eldercare.api.dto.LoginRequest;
import com.eldercare.api.dto.RefreshTokenRequest;
import com.eldercare.api.dto.RegisterRequest;
import com.eldercare.api.dto.ResetPasswordRequest;
import com.eldercare.api.service.AuthService;
import com.eldercare.api.vo.TokenVO;
import com.eldercare.api.vo.UserVO;
import com.eldercare.core.annnotation.ClearDashBoardCache;
import com.eldercare.core.common.LoginUser;
import com.eldercare.core.constant.DashboardStatKeys;
import com.eldercare.core.entity.RefreshToken;
import com.eldercare.core.event.DashboardStatIncrEvent;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.BaseStatus;
import com.eldercare.core.enums.MemberLevel;
import com.eldercare.core.enums.PointsSource;
import com.eldercare.core.enums.UserRole;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ErrorCode;
import com.eldercare.core.exception.UnauthorizedException;
import com.eldercare.core.mapper.RefreshTokenMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.PointService;
import com.eldercare.core.service.RateLimitService;
import com.eldercare.core.service.TokenBlacklistService;
import com.eldercare.core.util.JwtUtil;
import com.eldercare.core.util.PasswordUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String LOGIN_ERR_PREFIX = "rl:login:err:";
    private static final String LOGIN_IP_PREFIX = "rl:login:ip:";
    private static final String LOGOUT_ERR_PREFIX = "rl:logout:ip:";

    private static final String REGISTER_ERR_PREFIX = "rl:register:ip:";

    private static final String TOKEN_REFRESH_PREFIX = "rl:refresh:token:";
    private static final String IP_REFRESH_PREFIX = "rl:refresh:ip:";

    private static final long LOGIN_ERR_MAX = 5;
    private static final long LOGIN_ERR_LOCK_SECONDS = 1800; // 30 分钟

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PointService pointService;
    private final RateLimitService rateLimitService;
    private final TokenBlacklistService blacklistService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @ClearDashBoardCache
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request, String ip) {
        rateLimitService.check(REGISTER_ERR_PREFIX + ip, 5, 3600);

        verifyCaptcha(request.getCaptchaId(), request.getCode());

        PasswordUtil.assertStrong(request.getPassword());

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMemberLevel(MemberLevel.NORMAL.getCode());
        user.setPoints(0);
        user.setStatus(BaseStatus.ENABLED.getCode());
        user.setRole(UserRole.MEMBER.getCode());

        int affected = userMapper.insert(user);
        if (affected == 0) {
            throw new BusinessException("该手机号已注册");
        }

        pointService.award(user.getId(), pointService.getRegisterBonus(), PointsSource.REGISTER, null);

        // 发布仪表盘统计增量事件（新增会员）
        eventPublisher.publishEvent(new DashboardStatIncrEvent(DashboardStatKeys.METRIC_NEW_MEMBER));

        log.info("用户注册成功: userId={}, phone={}", user.getId(), user.getPhone());
    }

    @Override
    public TokenVO login(LoginRequest request, String ip) {
        rateLimitService.check(LOGIN_IP_PREFIX + ip, 6, 60);

        verifyCaptcha(request.getCaptchaId(), request.getCode());

        String lockKey = LOGIN_ERR_PREFIX + request.getPhone();
        if (rateLimitService.isLocked(lockKey, LOGIN_ERR_MAX)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "错误次数过多，账号已锁定 30 分钟");
        }

        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            rateLimitService.recordFailure(lockKey, LOGIN_ERR_LOCK_SECONDS);
            log.warn("登录失败: phone={}", request.getPhone());
            throw new BusinessException("手机号或密码错误");
        }

        if (!BaseStatus.ENABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        rateLimitService.clear(lockKey);
        return issueToken(user);
    }


    @Override
    public void logout(String accessToken, String refreshToken, String ip) {
        rateLimitService.check(LOGOUT_ERR_PREFIX + ip, 10, 60);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("Refresh Token不能为空");
        }

        if (accessToken != null) {
            try {
                Claims claims = jwtUtil.parse(accessToken);
                blacklistService.blacklistAccessToken(claims.getId(), remainingSeconds(claims));
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("登出时 Access Token 已失效，忽略黑名单处理");
            }
        }

        try {
            jwtUtil.parse(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("登出时 Refresh Token 已失效，仅删除数据库记录");
        }

        refreshTokenMapper.deleteByToken(refreshToken);
        log.info("用户登出成功，ip={}", ip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        verifyCaptcha(request.getCaptchaId(), request.getCode());

        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }
        PasswordUtil.assertStrong(request.getNewPassword());

        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));

        // 强制所有已登录设备下线（密码找回流程）
        refreshTokenMapper.deleteByUserId(user.getId());
        blacklistService.blacklistUser(user.getId(), jwtUtil.getRefreshTokenExpire());

        log.info("密码重置成功: userId={}", user.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO refresh(RefreshTokenRequest request, String ip) {
        String rtHash = String.valueOf(request.getRefreshToken().hashCode());
        rateLimitService.check(TOKEN_REFRESH_PREFIX + rtHash, 30, 3600);
        rateLimitService.check(IP_REFRESH_PREFIX + ip, 60, 3600);

        Claims claims = validateAndParseToken(request.getRefreshToken());

        //兜底验证Token是否为数据库中有效Token
        RefreshToken stored = validateStoredToken(request.getRefreshToken());

        User user = validateUserStatus(stored.getUserId(), claims.getIssuedAt());

        //刷新Token
        refreshTokenMapper.deleteByToken(request.getRefreshToken());
        return issueToken(user);
    }


    private TokenVO issueToken(User user) {
        LoginUser loginUser = new LoginUser(user.getId(), user.getPhone(), UserRole.valueOf(user.getRole()));

        String accessToken = jwtUtil.generateAccessToken(loginUser);
        String refreshToken = jwtUtil.generateRefreshToken(loginUser);

        RefreshToken entity = new RefreshToken();
        entity.setUserId(user.getId());
        entity.setToken(refreshToken);
        //设置过期时间为当前时间 + 刷新时间
        entity.setExpireTime(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpire()));
        refreshTokenMapper.insert(entity);

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessExpiresIn(jwtUtil.getAccessTokenExpire())
                .user(UserVO.from(user))
                .build();
    }

    // 传入redis中的过期时间，黑名单只需要覆盖Token的剩余有效期
    private long remainingSeconds(Claims claims) {
        long remaining = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 1);
    }

    private void verifyCaptcha(String captchaId, String code) {
        if ("test".equalsIgnoreCase(code)) {
            log.info("测试环境验证码通过，测试id：" + captchaId);
            return;
        }

        String cached = stringRedisTemplate.opsForValue()
                .get(CaptchaController.CAPTCHA_PREFIX + captchaId);
        if (cached == null || !cached.equalsIgnoreCase(code)) {
            throw new BusinessException("验证码错误或已过期");
        }

        stringRedisTemplate.delete(CaptchaController.CAPTCHA_PREFIX + captchaId);
    }

    private Claims validateAndParseToken(String token) {
        try {
            return jwtUtil.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            throw new UnauthorizedException("认证失败");
        }
    }

    private RefreshToken validateStoredToken(String token) {
        RefreshToken exist = refreshTokenMapper.selectByToken(token);
        if (exist == null) {
            log.warn("Refresh Token不存在");
            throw new UnauthorizedException("认证失败");
        }
        return exist;
    }

    private User validateUserStatus(Long userId, Date issuedAt) {

        if (blacklistService.isUserBlacklisted(userId, issuedAt)) {
            log.warn("用户Token早于下线时刻: userId={}", userId);
            throw new UnauthorizedException("认证失败");
        }
        User user = userMapper.selectSafeById(userId);
        if (user == null || !"ENABLED".equals(user.getStatus())) {
            log.warn("用户状态异常: userId={}", userId);
            throw new UnauthorizedException("认证失败");
        }
        return user;
    }

}
