package com.eldercare.admin.vo;

import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.entity.User;
import com.eldercare.core.vo.AppointmentVO;
import lombok.Data;

import java.util.List;

/**
 * 会员详情响应（基本信息 + 健康记录 + 预约记录）
 */
@Data
public class MemberDetailVO {

    /** 基本信息（不含密码） */
    private User user;

    /** 近期健康记录 */
    private List<HealthRecord> healthRecords;

    /** 预约记录 */
    private List<AppointmentVO> appointments;
}
