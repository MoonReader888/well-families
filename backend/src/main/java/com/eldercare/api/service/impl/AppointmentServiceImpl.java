package com.eldercare.api.service.impl;

import com.eldercare.api.dto.BookAppointmentRequest;
import com.eldercare.api.service.AppointmentService;
import com.eldercare.core.annnotation.ClearDashBoardCache;
import com.eldercare.core.config.JwtProperties;
import com.eldercare.core.constant.DashboardStatKeys;
import com.eldercare.core.entity.Appointment;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.AppointmentStatus;
import com.eldercare.core.enums.BaseStatus;
import com.eldercare.core.enums.PointsSource;
import com.eldercare.core.enums.UserStatus;
import com.eldercare.core.event.DashboardStatIncrEvent;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ForbiddenException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.AppointmentPackageMapper;
import com.eldercare.core.mapper.AppointmentSlotMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.FileStorageService;
import com.eldercare.core.vo.AppointmentVO;
import com.eldercare.core.service.PointService;
import com.eldercare.core.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

/**
 * 体检预约服务实现
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    /** 报告下载签名有效期：5 分钟 */
    private static final long SIGN_EXPIRE_MS = 5 * 60 * 1000L;

    private final AppointmentMapper appointmentMapper;
    private final AppointmentPackageMapper packageMapper;
    private final AppointmentSlotMapper slotMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final FileStorageService fileStorageService;
    private final JwtProperties jwtProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<AppointmentPackage> listPackages() {
        return packageMapper.selectEnabled();
    }

    @Override
    public AppointmentPackage getPackageDetail(Long packageId) {
        AppointmentPackage pkg = packageMapper.selectById(packageId);
        if (pkg == null || !BaseStatus.ENABLED.getCode().equals(pkg.getStatus())) {
            throw new ResourceNotFoundException("体检套餐不存在或已下架");
        }
        return pkg;
    }

    @Override
    public List<AppointmentSlot> listSlots(Long packageId, LocalDate date) {
        getPackageDetail(packageId);
        LocalDate queryDate = date == null ? LocalDate.now() : date;
        return slotMapper.selectByPackageAndDate(packageId, queryDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ClearDashBoardCache
    public AppointmentVO book(BookAppointmentRequest request) {
        Long userId = UserContext.currentUserId();

        User user = userMapper.selectById(userId);
        if(user==null){
            throw new BusinessException("用户不存在");
        }

        if (!UserStatus.ENABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，无法预约");
        }

        AppointmentPackage pkg = packageMapper.selectById(request.getPackageId());
        if (pkg == null || !BaseStatus.ENABLED.getCode().equals(pkg.getStatus())) {
            throw new BusinessException("体检套餐不存在或已下架");
        }

        AppointmentSlot slot = slotMapper.selectById(request.getSlotId());
        if (slot == null || !slot.getPackageId().equals(request.getPackageId())) {
            throw new BusinessException("预约时段不存在或与套餐不匹配");
        }
        if (slot.getAppointDate().isBefore(LocalDate.now())) {
            throw new BusinessException("该时段已过期，无法预约");
        }

        // 原子扣减积分
        pointService.deduct(userId, pkg.getPrice(), PointsSource.APPOINTMENT, null);

        // 行锁防止同一时段超卖
        int occupied = slotMapper.occupySlot(slot.getId());
        if (occupied == 0) {
            throw new BusinessException("该时段名额已满，请选择其他时段");
        }

        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setPackageId(pkg.getId());
        appointment.setSlotId(slot.getId());
        appointment.setStatus(AppointmentStatus.PENDING.getCode());
        appointmentMapper.insert(appointment);

        // 发布仪表盘统计增量事件（新增预约）
        eventPublisher.publishEvent(new DashboardStatIncrEvent(DashboardStatKeys.METRIC_APPOINTMENT));

        return appointmentMapper.selectVOById(appointment.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ClearDashBoardCache
    public void cancel(Long appointmentId) {
        Long userId = UserContext.currentUserId();
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        AppointmentStatus from = AppointmentStatus.fromCode(appointment.getStatus());
        if (AppointmentStatus.CANCELED.equals(from) || AppointmentStatus.COMPLETED.equals(from)) {
            throw new BusinessException("该预约已取消或已完成，无法取消");
        }
        if (AppointmentStatus.CONFIRMED.equals(from)) {
            throw new BusinessException("当前预约已确认，不可取消");
        }
        AppointmentStatus.checkTransition(from, AppointmentStatus.CANCELED);

        AppointmentPackage pkg = packageMapper.selectById(appointment.getPackageId());

        //原子退还积分
        if (pkg != null && pkg.getPrice() != null && pkg.getPrice() > 0) {
            pointService.award(userId, pkg.getPrice(), PointsSource.APPOINTMENT, appointmentId);
        }

        // 释放名额
        slotMapper.releaseSlot(appointment.getSlotId());
        appointmentMapper.cancel(appointmentId);
    }

    @Override
    public List<AppointmentVO> myAppointments() {
        Long userId = UserContext.currentUserId();
        return withSignedUrl(appointmentMapper.selectVOByUserId(userId));
    }

    @Override
    public AppointmentVO detail(Long appointmentId) {
        Long userId = UserContext.currentUserId();

        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        boolean owner = appointment.getUserId().equals(userId);
        if (!owner && !UserContext.isAdmin()) {
            throw new ForbiddenException("无权查看该预约");
        }
        return withSignedUrl(List.of(appointmentMapper.selectVOById(appointmentId))).get(0);
    }

    @Override
    public ResponseEntity<Resource> downloadReport(Long appointmentId, long expires, String sign) {
        Long userId = UserContext.currentUserId();
        // 校验签名有效期
        if (System.currentTimeMillis() > expires) {
            throw new BusinessException("下载链接已过期，请重新获取");
        }
        String expected = signReport(appointmentId, expires);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sign.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException("非法的下载链接");
        }

        // 归属校验
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        if (!appointment.getUserId().equals(userId) && !UserContext.isAdmin()) {
            throw new ForbiddenException("无权下载该报告");
        }
        if (appointment.getReportUrl() == null || appointment.getReportUrl().isBlank()) {
            throw new BusinessException("暂无体检报告");
        }

        Resource resource = fileStorageService.load(appointment.getReportUrl());
        String filename = "health-report-" + appointmentId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }


    /**
     * 为所有报告的预约生成 5 分钟有效的签名下载地址
     */
    private List<AppointmentVO> withSignedUrl(List<AppointmentVO> list) {
        for (AppointmentVO vo : list) {
            if (vo.getReportUrl() != null && !vo.getReportUrl().isBlank()) {
                long expires = System.currentTimeMillis() + SIGN_EXPIRE_MS;
                String sign = signReport(vo.getId(), expires);
                vo.setReportDownloadUrl("/api/member/appointment/" + vo.getId()
                        + "/report?expires=" + expires + "&sign=" + sign);
            }
        }
        return list;
    }

    private String signReport(Long appointmentId, long expires) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal((appointmentId + ":" + expires).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("报告签名失败", e);
        }
    }
}
