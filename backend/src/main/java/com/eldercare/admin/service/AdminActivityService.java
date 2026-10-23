package com.eldercare.admin.service;

import com.eldercare.admin.dto.AdminActivityRequest;
import com.eldercare.api.vo.UploadFileVO;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.vo.AdminRegistrationVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端活动管理服务
 */
public interface AdminActivityService {

    /**
     * 活动分页列表（按标题搜索，默认按 id 升序）
     */
    PageResult<CommunityActivity> page(int pageNum, int pageSize, String title);

    /**
     * 活动详情
     */
    CommunityActivity detail(Long activityId);

    /**
     * 新增活动
     */
    CommunityActivity create(AdminActivityRequest request);

    /**
     * 修改活动
     */
    void update(Long activityId, AdminActivityRequest request);

    /**
     * 删除活动（逻辑删除）
     */
    void delete(Long activityId);

    /**
     * 活动报名分页列表（含签到情况）
     */
    PageResult<AdminRegistrationVO> registrations(Long activityId, int pageNum, int pageSize);

    /**
     * 上传活动封面图
     */
    UploadFileVO uploadCover(MultipartFile file);
}
