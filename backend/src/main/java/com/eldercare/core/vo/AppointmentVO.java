package com.eldercare.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约响应（联表带套餐/时段信息）
 */
@Data
public class AppointmentVO {

    private Long id;

    private Long packageId;

    /** 套餐名称 */
    private String packageName;

    /** 套餐价格（积分） */
    private Integer price;

    private Long slotId;

    /** 预约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointDate;

    /** 时间段，如 09:00-10:00 */
    private String timeRange;

    /** 状态 PENDING/CONFIRMED/CANCELED/COMPLETED */
    private String status;

    /** 体检报告相对路径 */
    private String reportUrl;

    /** 报告下载地址（短期签名，5 分钟有效） */
    private String reportDownloadUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
