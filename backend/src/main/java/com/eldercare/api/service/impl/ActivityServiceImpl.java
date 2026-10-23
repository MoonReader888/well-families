package com.eldercare.api.service.impl;

import com.eldercare.api.service.ActivityService;
import com.eldercare.api.vo.ActivityVO;
import com.eldercare.core.annnotation.ClearDashBoardCache;
import com.eldercare.core.constant.DashboardStatKeys;
import com.eldercare.core.entity.ActivityRegistration;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.enums.ActivityStatus;
import com.eldercare.core.enums.CheckInStatus;
import com.eldercare.core.enums.PointsSource;
import com.eldercare.core.event.DashboardStatIncrEvent;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.core.mapper.CommunityActivityMapper;
import com.eldercare.core.service.PointService;
import com.eldercare.core.service.SysConfigService;
import com.eldercare.core.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 社区活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final CommunityActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final PointService pointService;
    private final SysConfigService sysConfigService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<ActivityVO> list() {
        Long userId = UserContext.currentUserId();

        List<CommunityActivity> activities = activityMapper.selectVisibleWithRegistration(userId);

        List<ActivityVO> result = new ArrayList<>();
        for (CommunityActivity activity : activities) {
            result.add(toVO(activity));
        }
        return result;
    }

    @Override
    public ActivityVO detail(Long activityId) {
        Long userId =UserContext.currentUserId();
        CommunityActivity activity = getVisibleActivity(activityId);

        ActivityRegistration reg = registrationMapper.selectByUserAndActivity(userId, activityId);
        return toVO(activity, reg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ClearDashBoardCache
    public void register(Long activityId) {
        Long userId = UserContext.currentUserId();
        CommunityActivity activity = getVisibleActivity(activityId);

        if (!ActivityStatus.REGISTRATING.getCode().equals(activity.getStatus())) {
            throw new BusinessException("活动当前不在报名状态");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getRegistrationStart() != null && now.isBefore(activity.getRegistrationStart())) {
            throw new BusinessException("活动尚未开始报名");
        }
        if (activity.getRegistrationEnd() != null && now.isAfter(activity.getRegistrationEnd())) {
            throw new BusinessException("活动报名已结束");
        }
        if (registrationMapper.selectByUserAndActivity(userId, activityId) != null) {
            throw new BusinessException("您已报名该活动");
        }

        int occupied = activityMapper.occupyParticipant(activityId);
        if (occupied == 0) {
            throw new BusinessException("活动人数已满，无法报名");
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setUserId(userId);
        registration.setActivityId(activityId);
        registration.setCheckInStatus(CheckInStatus.NOT_CHECKED_IN.name());
        registrationMapper.insert(registration);

        // 发布仪表盘统计增量事件（新增活动报名）
        eventPublisher.publishEvent(new DashboardStatIncrEvent(DashboardStatKeys.METRIC_REGISTRATION));

        log.info("活动报名成功: userId={}, activityId={}", userId, activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ClearDashBoardCache
    public void cancelRegister(Long activityId) {
        Long userId = UserContext.currentUserId();
        ActivityRegistration registration = getMyRegistration(userId, activityId);
        CommunityActivity activity = activityMapper.selectById(registration.getActivityId());
        LocalDateTime now = LocalDateTime.now();
        if (activity.getActivityStart().isBefore(now)) {
            throw new BusinessException("活动已开始，无法取消报名");
        }
        if (activity.getActivityEnd().isBefore(now)) {
            throw new BusinessException("活动已结束，无法取消报名");
        }
        if (CheckInStatus.CHECKED_IN.getCode().equals(registration.getCheckInStatus())) {
            throw new BusinessException("您已签到，无法取消报名");
        }
        registrationMapper.logicDelete(registration.getId());
        activityMapper.releaseParticipant(activityId);
        log.info("取消活动报名: userId={}, activityId={}", userId, activityId);
    }

    @Override
    public List<ActivityVO> myActivities() {
        Long userId = UserContext.currentUserId();

        List<CommunityActivity> activities = activityMapper.selectMyActivitiesWithRegistration(userId);

        List<ActivityVO> result = new ArrayList<>();
        for (CommunityActivity activity : activities) {
            result.add(toVO(activity));
        }
        return result;
    }

    @Override
    public ActivityVO checkInStatus(Long activityId) {
        Long userId = UserContext.currentUserId();
        CommunityActivity activity = getVisibleActivity(activityId);
        ActivityRegistration reg = registrationMapper.selectByUserAndActivity(userId, activityId);
        return toVO(activity,reg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long activityId) {
        Long userId = UserContext.currentUserId();
        CommunityActivity activity = getVisibleActivity(activityId);
        LocalDateTime now = LocalDateTime.now();
        if (activity.getActivityStart() != null && now.isBefore(activity.getActivityStart())) {
            throw new BusinessException("活动尚未开始，无法签到");
        }
        if (activity.getActivityEnd() != null && now.isAfter(activity.getActivityEnd())) {
            throw new BusinessException("活动已结束，无法签到");
        }

        ActivityRegistration registration = getMyRegistration(userId, activityId);
        int updated = registrationMapper.checkIn(registration.getId());
        if (updated == 0) {
            throw new BusinessException("您已签到，请勿重复签到");
        }

        // 签到赠送积分
        int bonus = sysConfigService.getInt("checkin_bonus_points", 50);
        pointService.award(userId, bonus, PointsSource.CHECK_IN, activityId);
        log.info("活动签到成功: userId={}, activityId={}", userId, activityId);
    }


    private CommunityActivity getVisibleActivity(Long activityId) {
        CommunityActivity activity = activityMapper.selectById(activityId);

        if (activity == null || "DRAFT".equals(activity.getStatus())) {
            throw new ResourceNotFoundException("活动不存在");
        }
        return activity;
    }

    private ActivityRegistration getMyRegistration(Long userId, Long activityId) {
        ActivityRegistration registration = registrationMapper.selectByUserAndActivity(userId, activityId);
        if (registration == null) {
            throw new BusinessException("您未报名该活动");
        }
        return registration;
    }

    private ActivityVO toVO(CommunityActivity activity) {
        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setStatus(resolveStatus(activity));
        vo.setRegistered(activity.getRegId()!=null);
        vo.setCheckInStatus(activity.getRegCheckInStatus());
        return vo;
    }

    private ActivityVO toVO(CommunityActivity activity, ActivityRegistration registration) {
        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setStatus(resolveStatus(activity));
        vo.setRegistered(registration != null);
        vo.setCheckInStatus(registration != null ? registration.getCheckInStatus() : null);
        return vo;
    }

    /**
     * 会员端活动状态检验
     */
    private String resolveStatus(CommunityActivity activity) {
        String status = activity.getStatus();
        if (ActivityStatus.DRAFT.getCode().equals(status)) {
            return status;
        }
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime regStart = activity.getRegistrationStart();
        LocalDateTime regEnd = activity.getRegistrationEnd();
        LocalDateTime actStart = activity.getActivityStart();
        LocalDateTime actEnd = activity.getActivityEnd();


        if (actEnd != null && now.isAfter(actEnd)) {
            return ActivityStatus.ENDED.getCode();
        }
        if (actStart != null && !now.isBefore(actStart)) {
            return ActivityStatus.IN_PROGRESS.getCode();
        }
        if (regStart != null && now.isBefore(regStart)) {
            return ActivityStatus.NOT_STARTED.getCode();
        }
        if (regEnd != null && now.isAfter(regEnd)) {
            return ActivityStatus.NOT_STARTED.getCode();
        }
        return ActivityStatus.REGISTRATING.getCode();
    }
}
