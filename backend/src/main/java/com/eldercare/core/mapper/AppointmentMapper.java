package com.eldercare.core.mapper;

import com.eldercare.core.entity.Appointment;
import com.eldercare.core.vo.AdminAppointmentVO;
import com.eldercare.core.vo.AppointmentReminderVO;
import com.eldercare.core.vo.AppointmentVO;
import com.eldercare.core.vo.DateCountVO;
import com.eldercare.core.vo.NameValueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约表 Mapper
 */
@Mapper
public interface AppointmentMapper {

    Appointment selectById(@Param("id") Long id);

    List<Appointment> selectByUserId(@Param("userId") Long userId);

    int insert(Appointment appointment);

    /** 更新预约状态（带状态前置条件，乐观锁：仅当当前状态 = expectStatus 时更新） */
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("expectStatus") String expectStatus);

    /** 批量更新预约状态（仅更新状态处于 fromStatuses 集合内的记录，乐观锁过滤） */
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status, @Param("fromStatuses") List<String> fromStatuses);

    /** 上传体检报告（非取消态即可上传并置为 COMPLETED，乐观锁：status != CANCELED） */
    int updateReport(@Param("id") Long id, @Param("reportUrl") String reportUrl);

    /** 取消预约 */
    int cancel(@Param("id") Long id);

    int logicDelete(@Param("id") Long id);

    /** 预约提醒：查询指定日期待预约/已确认的预约（预约提醒推送） */
    List<AppointmentReminderVO> selectForReminder(@Param("date") LocalDate date);

    /** 历史预约归档：超过保留期（2 年）标记为逻辑删除 */
    int archiveBefore(@Param("before") LocalDateTime before);

    /** 我的预约（联表带套餐名/时段信息） */
    List<AppointmentVO> selectVOByUserId(@Param("userId") Long userId);

    /** 今日新增预约数（仪表盘） */
    long countToday();

    /** 待完成预约数（仪表盘，PENDING/CONFIRMED） */
    long countPending();

    /** 仪表盘：指定日期区间内每日预约数（按天分组，不包含 end） */
    List<DateCountVO> countBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 仪表盘：预约状态分布 */
    List<NameValueVO> countByStatus();

    /** 管理端预约分页列表（联表，按状态/手机号筛选，配合 PageHelper） */
    List<AdminAppointmentVO> selectAdminPage(@Param("status") String status, @Param("phone") String phone);

    /** 管理端预约详情（联表） */
    AdminAppointmentVO selectAdminById(@Param("id") Long id);

    /** 预约详情（联表，校验归属用） */
    AppointmentVO selectVOById(@Param("id") Long id);
}
