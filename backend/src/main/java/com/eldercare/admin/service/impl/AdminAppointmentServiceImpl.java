package com.eldercare.admin.service.impl;

import com.eldercare.admin.dto.AdminPackageRequest;
import com.eldercare.admin.dto.AdminSlotBatchRequest;
import com.eldercare.admin.dto.AdminSlotUpdateRequest;
import com.eldercare.admin.service.AdminAppointmentService;
import com.eldercare.core.annnotation.ClearDashBoardCache;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.Appointment;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.enums.AppointmentStatus;
import com.eldercare.core.enums.SlotStatus;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.AppointmentPackageMapper;
import com.eldercare.core.mapper.AppointmentSlotMapper;
import com.eldercare.core.service.FileStorageService;
import com.eldercare.core.vo.AdminAppointmentVO;
import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端体检管理服务实现
 */
@Slf4j
@Service
public class AdminAppointmentServiceImpl implements AdminAppointmentService {

    /**
     * 报告最大大小 20MB
     */
    private final long reportMaxSize;

    private final AppointmentPackageMapper packageMapper;
    private final AppointmentSlotMapper slotMapper;
    private final AppointmentMapper appointmentMapper;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public AdminAppointmentServiceImpl(
            @Value("${eldercare.file.report-max-size:20971520}")
            long reportMaxSize,
            AppointmentPackageMapper packageMapper,
            AppointmentSlotMapper slotMapper,
            AppointmentMapper appointmentMapper,
            FileStorageService fileStorageService,
            ObjectMapper objectMapper) {
        this.reportMaxSize = reportMaxSize;
        this.packageMapper = packageMapper;
        this.slotMapper = slotMapper;
        this.appointmentMapper = appointmentMapper;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<AppointmentPackage> pagePackages(int pageNum, int pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(packageMapper.selectPackagePage(name));
    }

    @Override
    public AppointmentPackage getPackage(Long packageId) {
        AppointmentPackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new ResourceNotFoundException("套餐不存在");
        }
        return pkg;
    }

    private AppointmentSlot getSlot(Long slotId) {
        AppointmentSlot slot = slotMapper.selectById(slotId);
        if (slot == null) {
            throw new ResourceNotFoundException("时段不存在");
        }
        return slot;
    }

    @Override
    public AppointmentPackage createPackage(AdminPackageRequest request) {
        AppointmentPackage pkg = new AppointmentPackage();
        applyRequest(pkg, request);
        pkg.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
        packageMapper.insert(pkg);
        return pkg;
    }

    @Override
    public void updatePackage(Long packageId, AdminPackageRequest request) {
        AppointmentPackage pkg = getPackage(packageId);
        applyRequest(pkg, request);
        packageMapper.update(pkg);
    }

    @Override
    public void deletePackage(Long packageId) {
        getPackage(packageId);
        packageMapper.logicDelete(packageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerateSlots(Long packageId, AdminSlotBatchRequest request) {
        getPackage(packageId);
        if (request.getDates().isEmpty() || request.getTimeRanges().isEmpty()) {
            throw new BusinessException("请选择日期和时间段");
        }
        // 校验日期不早于今天
        LocalDate today = LocalDate.now();
        LocalDate earliestPast = null;

        for (LocalDate date : request.getDates()) {
            if (date.isBefore(today)) {
                if (earliestPast == null || date.isBefore(earliestPast)) {
                    earliestPast = date;
                }
            }
        }

        if (earliestPast != null) {
            throw new BusinessException("所选日期不能早于今天（最早为 " + earliestPast + "），请重新选择日期");
        }
        // 校验重复时段
        for (LocalDate date : request.getDates()) {
            for (String timeRange : request.getTimeRanges()) {
                if (slotMapper.countExists(packageId, date, timeRange) > 0) {
                    throw new BusinessException("所选时段已存在（" + date + " " + timeRange + "），请勿重复生成");
                }
            }
        }
        int created = 0;
        for (LocalDate date : request.getDates()) {
            for (String timeRange : request.getTimeRanges()) {
                AppointmentSlot slot = new AppointmentSlot();
                slot.setPackageId(packageId);
                slot.setAppointDate(date);
                slot.setTimeRange(timeRange);
                slot.setMaxCount(request.getMaxCount());
                slot.setCurrentCount(0);
                slot.setStatus(SlotStatus.AVAILABLE.getCode());
                slotMapper.insert(slot);
                created++;
            }
        }
        log.info("批量生成预约时段: packageId={}, created={}", packageId, created);
        return created;
    }

    @Override
    public List<AppointmentSlot> listSlots(Long packageId) {
        getPackage(packageId);
        return slotMapper.selectByPackage(packageId);
    }

    @Override
    public void updateSlot(Long slotId, AdminSlotUpdateRequest request) {
        AppointmentSlot slot = getSlot(slotId);
        if (!SlotStatus.AVAILABLE.getCode().equals(request.getStatus())
                && !SlotStatus.CLOSED.getCode().equals(request.getStatus())) {
            throw new BusinessException("非法的时段状态");
        }
        if (request.getMaxCount() < slot.getCurrentCount()) {
            throw new BusinessException("最大预约人数不能小于当前已预约人数（" + slot.getCurrentCount() + "）");
        }
        // 同套餐同日期同时段查重（排除自身）
        List<AppointmentSlot> existingSlots = slotMapper.selectByPackageAndDate(
                slot.getPackageId(),
                request.getAppointDate()
        );

        for (AppointmentSlot s : existingSlots) {
            if (!s.getId().equals(slotId) && request.getTimeRange().equals(s.getTimeRange())) {
                throw new BusinessException("该日期时段已存在，请勿重复创建");
            }
        }

        slot.setAppointDate(request.getAppointDate());
        slot.setTimeRange(request.getTimeRange());
        slot.setMaxCount(request.getMaxCount());
        String status = request.getStatus();
        if (SlotStatus.AVAILABLE.getCode().equals(status) && slot.getCurrentCount() >= request.getMaxCount()) {
            status = SlotStatus.FULL.getCode();
        }
        slot.setStatus(status);
        slotMapper.update(slot);
        log.info("编辑预约时段: slotId={}, packageId={}", slotId, slot.getPackageId());
    }

    @Override
    public void deleteSlot(Long slotId) {
        AppointmentSlot slot = getSlot(slotId);
        if (slot.getCurrentCount() != null && slot.getCurrentCount() > 0) {
            throw new BusinessException("该时段已有 " + slot.getCurrentCount() + " 人预约，不能删除");
        }
        slotMapper.logicDelete(slotId);
        log.info("删除预约时段: slotId={}, packageId={}", slotId, slot.getPackageId());
    }

    @Override
    public PageResult<AdminAppointmentVO> pageAppointments(int pageNum, int pageSize, String status, String phone) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(appointmentMapper.selectAdminPage(status, phone));
    }

    @Override
    public AdminAppointmentVO getAppointment(Long appointmentId) {
        appointmentMapper.selectById(appointmentId);
        AdminAppointmentVO vo = appointmentMapper.selectAdminById(appointmentId);
        if (vo == null) {
            throw new ResourceNotFoundException("预约不存在");
        }
        return vo;
    }

    @ClearDashBoardCache
    @Override
    public void updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约不存在");
        }
        AppointmentStatus from = AppointmentStatus.fromCode(appointment.getStatus());
        AppointmentStatus to = AppointmentStatus.fromCode(status);
        // 状态机校验：终态不可再改，且 from -> to 必须合法
        AppointmentStatus.checkTransition(from, to);

        // 乐观锁：带状态前置条件更新，失败说明被并发修改
        int updated = appointmentMapper.updateStatus(appointmentId, to.getCode(), from.getCode());
        if (updated == 0) {
            throw new BusinessException("预约状态已被其他操作变更，请刷新后重试");
        }
    }

    @ClearDashBoardCache
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAppointmentStatusBatch(List<Long> appointmentIds, String status) {
        if (appointmentIds == null || appointmentIds.isEmpty()) {
            throw new BusinessException("请选择要操作的预约");
        }
        AppointmentStatus to = AppointmentStatus.fromCode(status);
        if (to.isTerminal()) {
            throw new BusinessException("预约状态不允许流转到【" + to.getDesc() + "】");
        }

        List<Long> distinctIds = appointmentIds.stream().distinct().toList();

        // 方案 B：按目标态反推允许的前置状态集，用 SQL 前置条件过滤终态/非法记录
        List<String> fromStatuses = allowedFromStatuses(to);
        int affected = appointmentMapper.updateStatusBatch(distinctIds, to.getCode(), fromStatuses);

        // 提示未成功修改的条数
        int skipped = distinctIds.size() - affected;
        if (skipped > 0) {
            log.warn("管理员批量调整预约状态，{} 条因状态不符未修改: target={}", skipped, to.getCode());
            throw new BusinessException("有 " + skipped + " 条预约因状态不符未能修改，请检查后重试");
        }
        log.info("管理员批量调整预约状态: count={}, status={}", affected, to.getCode());
        return affected;
    }

    /**
     * 反推：能流转到目标态 to 的所有前置状态（用于批量乐观锁过滤）
     */
    private List<String> allowedFromStatuses(AppointmentStatus to) {
        List<String> result = new java.util.ArrayList<>();
        for (AppointmentStatus from : AppointmentStatus.values()) {
            if (!from.isTerminal()) {
                try {
                    AppointmentStatus.checkTransition(from, to);
                    result.add(from.getCode());
                } catch (BusinessException ignored) {
                    // 不合法前置状态，跳过
                }
            }
        }
        return result;
    }

    @Override
    public void uploadReport(Long appointmentId, MultipartFile file) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约不存在");
        }
        // 上传报告不属于状态机流转：仅「已取消」的预约禁止上传，
        // 其余状态（PENDING/CONFIRMED/COMPLETED）均可上传，并顺带收敛为 COMPLETED。
        AppointmentStatus from = AppointmentStatus.fromCode(appointment.getStatus());
        if (AppointmentStatus.CANCELED.equals(from)) {
            throw new BusinessException("已取消的预约无法上传报告");
        }

        // 校验：仅允许 PDF，大小不超过 20MB
        String filename = file.getOriginalFilename();
        if (file.isEmpty() || filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException("仅支持 PDF 格式的体检报告");
        }
        if (file.getSize() > reportMaxSize) {
            throw new BusinessException("体检报告不能超过 20MB");
        }

        String relativePath = fileStorageService.store(file, "report");
        // 乐观锁：SQL 内带 status != 'CANCELED' 前置条件，0 行说明状态已被并发变更
        int updated = appointmentMapper.updateReport(appointmentId, relativePath);
        if (updated == 0) {
            throw new BusinessException("预约状态已被其他操作变更，请刷新后重试");
        }
        log.info("上传体检报告: appointmentId={}, path={}, size={}",
                appointmentId, relativePath, file.getSize());
    }

    @Override
    public ResponseEntity<Resource> downloadReport(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约不存在");
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

    private void applyRequest(AppointmentPackage pkg, AdminPackageRequest request) {
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        pkg.setSuitablePeople(request.getSuitablePeople());
        pkg.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : pkg.getStatus());
        if (request.getItems() != null) {
            pkg.setItems(objectMapper.writeValueAsString(request.getItems()));
        }
    }
}
