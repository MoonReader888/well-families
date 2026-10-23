package com.eldercare.admin.service.impl;

import com.eldercare.admin.dto.AdminActivityRequest;
import com.eldercare.admin.service.AdminActivityService;
import com.eldercare.api.vo.UploadFileVO;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.enums.ActivityStatus;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.core.mapper.CommunityActivityMapper;
import com.eldercare.core.service.FileStorageService;
import com.eldercare.core.vo.AdminRegistrationVO;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 管理端活动管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityServiceImpl implements AdminActivityService {

    /** 封面允许的类型 */
    private static final Set<String> ALLOWED_COVER_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /** 封面大小上限：5MB */
    private static final long COVER_MAX_SIZE = 5 * 1024 * 1024L;

    /** 静态资源访问前缀（与 CorsConfig 中 /uploads/** 映射对应） */
    private static final String UPLOADS_URL_PREFIX = "/uploads/";

    private final CommunityActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final FileStorageService fileStorageService;

    @Override
    public PageResult<CommunityActivity> page(int pageNum, int pageSize, String title) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(activityMapper.selectActivityPage(title));
    }

    @Override
    public CommunityActivity detail(Long activityId) {
        return getActivity(activityId);
    }

    @Override
    public CommunityActivity create(AdminActivityRequest request) {
        validateTimes(request);
        CommunityActivity activity = new CommunityActivity();
        applyRequest(activity, request);
        activity.setCurrentParticipants(0);
        if (activity.getStatus() == null) {
            activity.setStatus(ActivityStatus.DRAFT.getCode());
        }
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    public void update(Long activityId, AdminActivityRequest request) {
        CommunityActivity activity = getActivity(activityId);
        applyRequest(activity, request);
        activityMapper.update(activity);
    }

    @Override
    public void delete(Long activityId) {
        getActivity(activityId);
        activityMapper.logicDelete(activityId);
    }

    @Override
    public PageResult<AdminRegistrationVO> registrations(Long activityId, int pageNum, int pageSize) {
        getActivity(activityId);
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(registrationMapper.selectAdminByActivityId(activityId));
    }

    @Override
    public UploadFileVO uploadCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_COVER_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("封面仅支持 JPG/PNG/GIF/WEBP 格式");
        }
        if (file.getSize() > COVER_MAX_SIZE) {
            throw new BusinessException("封面不能超过 5MB");
        }

        String relativePath = fileStorageService.store(file, "cover");
        String url = UPLOADS_URL_PREFIX + relativePath;
        log.info("上传活动封面成功: path={}, size={}", relativePath, file.getSize());
        return new UploadFileVO(relativePath, url);
    }


    //校验活动存在
    private CommunityActivity getActivity(Long activityId) {
        CommunityActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ResourceNotFoundException("活动不存在");
        }
        return activity;
    }

    private void validateTimes(AdminActivityRequest request) {
        LocalDateTime regStart = request.getRegistrationStart();
        LocalDateTime regEnd = request.getRegistrationEnd();
        LocalDateTime actStart = request.getActivityStart();
        LocalDateTime actEnd = request.getActivityEnd();
        LocalDateTime now = LocalDateTime.now();
        if(regEnd !=null && regEnd.isBefore(now)){
            throw new BusinessException("报名结束时间必须不早于当前时间");
        }
        if (regStart != null && regEnd != null && !regStart.isBefore(regEnd)) {
            throw new BusinessException("报名开始时间必须早于报名结束时间");
        }
        if (actStart != null && actEnd != null && !actStart.isBefore(actEnd)) {
            throw new BusinessException("活动开始时间必须早于活动结束时间");
        }
        if (regEnd != null && actStart != null && regEnd.isAfter(actStart)) {
            throw new BusinessException("报名结束时间必须不晚于活动开始时间");
        }
    }


    private void applyRequest(CommunityActivity activity, AdminActivityRequest request) {
        activity.setTitle(request.getTitle());
        activity.setCoverUrl(request.getCoverUrl());
        activity.setContent(request.getContent());
        activity.setRegistrationStart(request.getRegistrationStart());
        activity.setRegistrationEnd(request.getRegistrationEnd());
        activity.setActivityStart(request.getActivityStart());
        activity.setActivityEnd(request.getActivityEnd());
        activity.setMaxParticipants(request.getMaxParticipants());
        if (request.getStatus() != null) {
            activity.setStatus(request.getStatus());
        }
    }
}
