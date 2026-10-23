package com.eldercare.core.service;

import com.eldercare.core.entity.PointsTransaction;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.PointsSource;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.mapper.PointsTransactionMapper;
import com.eldercare.core.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 积分服务
 * 积分变更使用数据库原子操作，同时记录积分流水用于过期清理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    /** 积分有效期：1 年 */
    public static final long POINTS_VALID_DAYS = 365;

    private final UserMapper userMapper;
    private final PointsTransactionMapper pointsTransactionMapper;
    private final SysConfigService sysConfigService;

    /**
     * 赠送积分：原子增加余额 + 记录带过期时间的流水
     */
    @Transactional(rollbackFor = Exception.class)
    public void award(Long userId, int amount, PointsSource source, Long refId) {
        if (amount <= 0) {
            throw new BusinessException("赠送积分必须为正数");
        }
        User user  = userMapper.selectSafeById(userId);
        if(user ==null){
            throw new BusinessException("用户不存在");
        }

        int oldBalance = user.getPoints();
        int newBalance = oldBalance +amount;

        int updated = userMapper.changePoints(userId, amount);
        if(updated ==0){
            throw new BusinessException("更新积分失败");
        }
        insertTransaction(userId, amount, newBalance, source, refId);
    }

    /**
     * 扣减积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long userId, int amount, PointsSource source, Long refId) {
        if (amount < 0) {
            throw new BusinessException("扣减积分不能为负数");
        }
        User user  = userMapper.selectSafeById(userId);
        if(user ==null){
            throw new BusinessException("用户不存在");
        }

        int oldBalance = user.getPoints();
        int newBalance = oldBalance - amount;

        int updated = userMapper.deductPoints(userId, amount);
        if (updated == 0) {
            throw new BusinessException("积分不足，无法操作");
        }

        insertTransaction(userId, -amount, newBalance, source, refId);
    }

    /**
     * 获取注册赠送积分（积分规则配置）
     */
    public int getRegisterBonus() {
        return sysConfigService.getInt("register_bonus_points", 100);
    }

    /**
      * 管理员手动调整积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long userId, int delta, String reason) {
        if (delta > 0) {
            award(userId, delta, PointsSource.ADMIN, null);
        } else if (delta < 0) {
            deduct(userId, -delta, PointsSource.ADMIN, null);
        }
        if (reason != null && !reason.isBlank()) {
            log.info("管理员调整积分: userId={}, delta={}, reason={}", userId, delta, reason);
        }
    }

    /*
    记录增加积分流水
     */
    private void insertTransaction(Long userId, int changeAmount,
                                   int balanceAfter, PointsSource source, Long refId) {
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(userId);
        transaction.setChangeAmount(changeAmount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setSource(source.getCode());
        transaction.setRefId(refId);
        if (changeAmount > 0) {
            transaction.setExpireTime(LocalDateTime.now().plusDays(POINTS_VALID_DAYS));
        } else {
            transaction.setExpireTime(LocalDateTime.now());
        }
        pointsTransactionMapper.insert(transaction);
    }
}
