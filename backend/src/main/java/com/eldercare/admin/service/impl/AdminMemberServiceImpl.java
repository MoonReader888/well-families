package com.eldercare.admin.service.impl;

import com.eldercare.admin.service.AdminMemberService;
import com.eldercare.admin.vo.MemberDetailVO;
import com.eldercare.core.annnotation.ClearDashBoardCache;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.MemberLevel;
import com.eldercare.core.enums.UserRole;
import com.eldercare.core.enums.UserStatus;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ForbiddenException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.HealthRecordMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.PointService;
import com.eldercare.core.util.PasswordUtil;
import com.github.pagehelper.PageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eldercare.core.mapper.RefreshTokenMapper;
import com.eldercare.core.service.TokenBlacklistService;
import com.eldercare.core.util.JwtUtil;

/**
 * 管理端会员管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberServiceImpl implements AdminMemberService {

    private final UserMapper userMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final PointService pointService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenMapper refreshTokenMapper;
    private final TokenBlacklistService blacklistService;
    private final JwtUtil jwtUtil;

    @Override
    public PageResult<User> page(int pageNum, int pageSize, String phone, String realName, String status, String memberLevel) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(userMapper.selectMemberPage(phone, realName, status, memberLevel));
    }

    @Override
    public MemberDetailVO detail(Long memberId) {
        getMember(memberId);
        MemberDetailVO vo = new MemberDetailVO();
        vo.setUser(userMapper.selectSafeById(memberId));
        vo.setHealthRecords(healthRecordMapper.selectByUserId(memberId).stream().limit(10).toList());
        vo.setAppointments(appointmentMapper.selectVOByUserId(memberId));
        return vo;
    }

    @Override
    public void updateStatus(Long memberId, String status) {
        getMember(memberId);

        UserStatus.validate(status);

        userMapper.updateStatus(memberId, status);

        if (UserStatus.DISABLED.getCode().equals(status)) {
            refreshTokenMapper.deleteByUserId(memberId);
            blacklistService.blacklistUser(memberId, jwtUtil.getRefreshTokenExpire());
        }
        log.info("管理员调整会员状态: memberId={}, status={}", memberId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatusBatch(List<Long> memberIds, String status) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new BusinessException("请选择要操作的会员");
        }

        List<Long> distinctIds = new ArrayList<>(new HashSet<>(memberIds));

        UserStatus.validate(status);

        int affected = userMapper.updateStatusBatch(distinctIds, status);
        if (UserStatus.DISABLED.getCode().equals(status)) {
            for (Long id : distinctIds) {
                refreshTokenMapper.deleteByUserId(id);
                blacklistService.blacklistUser(id, jwtUtil.getRefreshTokenExpire());
            }
        }
        log.info("管理员批量调整会员状态: count={}, status={}", affected, status);
        return affected;
    }

    @Override
    @ClearDashBoardCache
    public void updateLevel(Long memberId, String memberLevel) {
        getMember(memberId);
        MemberLevel.validate(memberLevel);
        userMapper.updateMemberLevel(memberId, memberLevel);
        log.info("管理员调整会员等级: memberId={}, level={}", memberId, memberLevel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustPoints(Long memberId, int points, String reason) {
        getMember(memberId);
        pointService.adjust(memberId, points, reason);
        log.info("管理员调整积分: memberId={}, points={}, reason={}", memberId, points, reason);
    }

    @Override
    public void resetPassword(Long memberId, String newPassword) {
        getMember(memberId);
        PasswordUtil.assertStrong(newPassword);
        userMapper.updatePassword(memberId, passwordEncoder.encode(newPassword));

        refreshTokenMapper.deleteByUserId(memberId);
        blacklistService.blacklistUser(memberId, jwtUtil.getRefreshTokenExpire());
        log.info("管理员重置会员密码: memberId={}", memberId);
    }

    //业务校验
    private User getMember(Long memberId) {
        User user = userMapper.selectById(memberId);
        if (user == null) {
            throw new ResourceNotFoundException("会员不存在");
        }
        if (!UserRole.MEMBER.getCode().equals(user.getRole())) {
            throw new ForbiddenException("只能管理会员账号");
        }
        return user;
    }
}
