package com.eldercare.api.service;

import com.eldercare.api.vo.ActivityVO;

import java.util.List;

/**
 * 社区活动服务
 */
public interface ActivityService {

    /**
     * 活动列表
     */
    List<ActivityVO> list();

    /**
     * 活动详情
     */
    ActivityVO detail(Long activityId);

    /**
     * 报名（并发名额控制，防超员）
     */
    void register(Long activityId);

    /**
     * 取消报名
     */
    void cancelRegister(Long activityId);

    /**
     * 我的活动列表
     */
    List<ActivityVO> myActivities();

    /**
     * 签到状态
     */
    ActivityVO checkInStatus(Long activityId);

    /**
     * 签到（赠送积分）
     */
    void checkIn(Long activityId);
}
