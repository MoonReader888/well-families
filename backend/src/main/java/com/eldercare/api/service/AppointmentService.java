package com.eldercare.api.service;

import com.eldercare.api.dto.BookAppointmentRequest;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.vo.AppointmentVO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 体检预约服务
 */
public interface AppointmentService {

    /**
     * 可预约套餐列表
     */
    List<AppointmentPackage> listPackages();

    /**
     * 套餐详情
     */
    AppointmentPackage getPackageDetail(Long packageId);

    /**
     * 某套餐某日的可预约时段
     */
    List<AppointmentSlot> listSlots(Long packageId, LocalDate date);

    /**
     * 预约（并发安全：行锁占名额 + 原子扣积分，超卖回滚）
     */
    AppointmentVO book(BookAppointmentRequest request);

    /**
     * 取消预约（退还积分 + 释放名额）
     */
    void cancel(Long appointmentId);

    /**
     * 我的预约列表
     */
    List<AppointmentVO> myAppointments();

    /**
     * 预约详情
     */
    AppointmentVO detail(Long appointmentId);

    /**
     * 下载体检报告（校验签名 + 归属）
     */
    ResponseEntity<Resource> downloadReport(Long appointmentId, long expires, String sign);
}
