package com.eldercare.admin.service;

import com.eldercare.admin.dto.AdminBatchPushMessageRequest;
import com.eldercare.admin.dto.AdminPushMessageRequest;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.vo.AdminMessageVO;

import java.time.LocalDateTime;

/**
 * 管理端消息管理服务
 */
public interface AdminMessageService {

    /**
     * 消息分页列表
     */
    PageResult<AdminMessageVO> page(int pageNum, int pageSize, String phone);

    /**
     * 消息详情
     */
    AdminMessageVO detail(Long messageId);

    /**
     * 删除消息
     */
    void delete(Long messageId);

    /**
     * 向单个用户推送站内消息
     */
    void pushSingle(AdminPushMessageRequest request);

    /**
     * 向批量用户推送站内消息
     */
    void pushBatch(AdminBatchPushMessageRequest request);

    /**
     * 按条件统计目标会员数（推送前预览）
     */
    long countTarget(String memberLevel, String status, LocalDateTime startTime, LocalDateTime endTime);
}
