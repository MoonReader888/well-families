package com.eldercare.api.controller;

import com.eldercare.api.dto.BookAppointmentRequest;
import com.eldercare.api.service.AppointmentService;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.vo.AppointmentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 体检预约接口（/api/member/appointment）
 */
@RestController
@RequestMapping("/api/member/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /** 可预约套餐列表 */
    @GetMapping("/packages")
    public Result<List<AppointmentPackage>> listPackages() {
        return Result.success(appointmentService.listPackages());
    }

    /** 套餐详情 */
    @GetMapping("/packages/{packageId}")
    public Result<AppointmentPackage> getPackageDetail(@PathVariable Long packageId) {
        return Result.success(appointmentService.getPackageDetail(packageId));
    }

    /** 某套餐某日的可预约时段 */
    @GetMapping("/packages/{packageId}/slots")
    public Result<List<AppointmentSlot>> listSlots(@PathVariable Long packageId,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(appointmentService.listSlots(packageId, date));
    }

    /** 预约（并发安全） */
    @PostMapping
    public Result<AppointmentVO> book(@Valid @RequestBody BookAppointmentRequest request) {
        return Result.success(appointmentService.book(request));
    }

    /** 取消预约 */
    @PostMapping("/{appointmentId}/cancel")
    public Result<Void> cancel(@PathVariable Long appointmentId) {
        appointmentService.cancel(appointmentId);
        return Result.success();
    }

    /** 我的预约列表 */
    @GetMapping("/my")
    public Result<List<AppointmentVO>> myAppointments() {
        return Result.success(appointmentService.myAppointments());
    }

    /** 预约详情 */
    @GetMapping("/{appointmentId}")
    public Result<AppointmentVO> detail(@PathVariable Long appointmentId) {
        return Result.success(appointmentService.detail(appointmentId));
    }

    /** 下载体检报告（短期签名 URL） */
    @GetMapping("/{appointmentId}/report")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long appointmentId,
                                                   @RequestParam long expires,
                                                   @RequestParam String sign) {
        return appointmentService.downloadReport(appointmentId, expires, sign);
    }
}
