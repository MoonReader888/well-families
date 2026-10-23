package com.eldercare.admin.vo;

import com.eldercare.core.vo.NameValueVO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 管理端仪表盘数据概览
 */
@Data
@Builder
public class DashboardVO implements Serializable {

    /** 会员总数 */
    private long memberTotal;

    /** 今日新增会员 */
    private long todayNewMembers;

    /** 今日预约数 */
    private long todayAppointments;

    /** 今日活动报名数 */
    private long todayActivityRegistrations;

    /** 待完成预约数 */
    private long pendingAppointments;

    /** 近7日日期（MM-dd，含今天） */
    private List<String> last7Days;

    /** 近7日新增会员 */
    private List<Long> newMemberTrend;

    /** 近7日预约数 */
    private List<Long> appointmentTrend;

    /** 近7日活动报名数 */
    private List<Long> registrationTrend;

    /** 预约状态分布 */
    private List<NameValueVO> appointmentStatus;

    /** 会员等级分布 */
    private List<NameValueVO> memberLevels;
}
