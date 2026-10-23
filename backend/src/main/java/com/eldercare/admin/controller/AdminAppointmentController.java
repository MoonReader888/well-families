package com.eldercare.admin.controller;

import com.eldercare.admin.dto.AdminAppointmentBatchStatusRequest;
import com.eldercare.admin.dto.AdminAppointmentStatusRequest;
import com.eldercare.admin.dto.AdminPackageRequest;
import com.eldercare.admin.dto.AdminSlotBatchRequest;
import com.eldercare.admin.dto.AdminSlotUpdateRequest;
import com.eldercare.admin.service.AdminAppointmentService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.vo.AdminAppointmentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 管理端体检管理接口（/api/admin/appointment）
 */
@RestController
@RequestMapping("/api/admin/appointment")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AdminAppointmentService adminAppointmentService;

    /**
     * 套餐分页列表（按名称搜索）
     */
    @GetMapping("/packages")
    public Result<PageResult<AppointmentPackage>> listPackages(@RequestParam(defaultValue = "1") int pageNum,
                                                               @RequestParam(defaultValue = "10") int pageSize,
                                                               @RequestParam(required = false) String name) {
        return Result.success(adminAppointmentService.pagePackages(pageNum, pageSize, name));
    }

    /**
     * 套餐详情
     */
    @GetMapping("/packages/{packageId}")
    public Result<AppointmentPackage> getPackage(@PathVariable Long packageId) {
        return Result.success(adminAppointmentService.getPackage(packageId));
    }

    /**
     * 新增套餐
     */
    @PostMapping("/packages")
    public Result<AppointmentPackage> createPackage(@Valid @RequestBody AdminPackageRequest request) {
        return Result.success(adminAppointmentService.createPackage(request));
    }

    /**
     * 修改套餐
     */
    @PutMapping("/packages/{packageId}")
    public Result<Void> updatePackage(@PathVariable Long packageId,
                                      @Valid @RequestBody AdminPackageRequest request) {
        adminAppointmentService.updatePackage(packageId, request);
        return Result.success();
    }

    /**
     * 删除套餐
     */
    @DeleteMapping("/packages/{packageId}")
    public Result<Void> deletePackage(@PathVariable Long packageId) {
        adminAppointmentService.deletePackage(packageId);
        return Result.success();
    }

    /**
     * 批量生成预约时段
     */
    @PostMapping("/packages/{packageId}/slots/batch")
    public Result<Integer> batchGenerateSlots(@PathVariable Long packageId,
                                              @Valid @RequestBody AdminSlotBatchRequest request) {
        return Result.success(adminAppointmentService.batchGenerateSlots(packageId, request));
    }

    /**
     * 套餐时段列表
     */
    @GetMapping("/packages/{packageId}/slots")
    public Result<List<AppointmentSlot>> listSlots(@PathVariable Long packageId) {
        return Result.success(adminAppointmentService.listSlots(packageId));
    }

    /**
     * 编辑时段
     */
    @PutMapping("/packages/slots/{slotId}")
    public Result<Void> updateSlot(@PathVariable Long slotId,
                                   @Valid @RequestBody AdminSlotUpdateRequest request) {
        adminAppointmentService.updateSlot(slotId, request);
        return Result.success();
    }

    /**
     * 删除时段（已有预约的时段禁止删除）
     */
    @DeleteMapping("/packages/slots/{slotId}")
    public Result<Void> deleteSlot(@PathVariable Long slotId) {
        adminAppointmentService.deleteSlot(slotId);
        return Result.success();
    }

    /**
     * 预约分页列表
     */
    @GetMapping("/appointments")
    public Result<PageResult<AdminAppointmentVO>> listResult(@RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(required = false) String phone) {
        return Result.success(adminAppointmentService.pageAppointments(pageNum,pageSize,status,phone));
    }


    /**
     * 预约详情
     */
    @GetMapping("/appointments/{appointmentId}")
    public Result<AdminAppointmentVO> getAppointment(@PathVariable Long appointmentId) {
        return Result.success(adminAppointmentService.getAppointment(appointmentId));
    }

    /**
     * 更新预约状态
     */
    @PutMapping("/appointments/{appointmentId}/status")
    public Result<Void> updateAppointmentStatus(@PathVariable Long appointmentId,
                                                @Valid @RequestBody AdminAppointmentStatusRequest request) {
        adminAppointmentService.updateAppointmentStatus(appointmentId, request.getStatus());
        return Result.success();
    }

    /**
     * 批量更新预约状态
     */
    @PutMapping("/appointments/status/batch")
    public Result<Integer> updateAppointmentStatusBatch(@Valid @RequestBody AdminAppointmentBatchStatusRequest request) {
        return Result.success(adminAppointmentService.updateAppointmentStatusBatch(request.getIds(), request.getStatus()));
    }

    /**
     * 上传体检报告
     */
    @PostMapping("/appointments/{appointmentId}/report")
    public Result<Void> uploadReport(@PathVariable Long appointmentId,
                                     @RequestPart("file") MultipartFile file) {
        adminAppointmentService.uploadReport(appointmentId, file);
        return Result.success();
    }

    /**
     * 下载体检报告
     */
    @GetMapping("/appointments/{appointmentId}/report")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long appointmentId) {
        return adminAppointmentService.downloadReport(appointmentId);
    }
}
