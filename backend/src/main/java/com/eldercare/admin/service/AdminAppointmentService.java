   package com.eldercare.admin.service;

import com.eldercare.admin.dto.AdminPackageRequest;
import com.eldercare.admin.dto.AdminSlotBatchRequest;
import com.eldercare.admin.dto.AdminSlotUpdateRequest;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.vo.AdminAppointmentVO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 管理端体检管理服务
 */
public interface AdminAppointmentService {

    /**
     * 套餐分页列表（按名称搜索，默认按 id 升序）
     */
    PageResult<AppointmentPackage> pagePackages(int pageNum, int pageSize, String name);

    /**
     * 套餐详情
     */
    AppointmentPackage getPackage(Long packageId);

    /**
     * 新增套餐
     */
    AppointmentPackage createPackage(AdminPackageRequest request);

    /**
     * 修改套餐
     */
    void updatePackage(Long packageId, AdminPackageRequest request);

    /**
     * 删除套餐（逻辑删除）
     */
    void deletePackage(Long packageId);

    /**
     * 批量生成预约时段（按 日期 × 时间段，已存在则跳过）
     */
    int batchGenerateSlots(Long packageId, AdminSlotBatchRequest request);

    /**
     * 套餐下的全部时段
     */
    List<AppointmentSlot> listSlots(Long packageId);

    /**
     * 编辑时段
     */
    void updateSlot(Long slotId, AdminSlotUpdateRequest request);

    /**
     * 删除时段（已有预约的时段禁止删除）
     */
    void deleteSlot(Long slotId);

    /**
     * 预约分页列表（按状态/手机号筛选）
     */
    PageResult<AdminAppointmentVO> pageAppointments(int pageNum, int pageSize, String status, String phone);


    /**
     * 预约详情
     */
    AdminAppointmentVO getAppointment(Long appointmentId);

    /**
     * 更新预约状态
     */
    void updateAppointmentStatus(Long appointmentId, String status);

    /**
     * 批量更新预约状态，返回实际更新的条数
     */
    int updateAppointmentStatusBatch(List<Long> appointmentIds, String status);

    /**
     * 上传体检报告（PDF ≤ 20MB），完成后预约置为 COMPLETED
     */
    void uploadReport(Long appointmentId, MultipartFile file);

    /**
     * 下载体检报告（管理端，走 JWT/RBAC 鉴权，无需签名 URL）
     */
    ResponseEntity<Resource> downloadReport(Long appointmentId);
}
