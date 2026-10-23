package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体检套餐实体（appointment_package 表）
 */
@Data
public class AppointmentPackage implements Serializable {

    private Long id;

    private String name;

    private String description;

    /** 价格（积分抵扣） */
    private Integer price;

    /** 适合人群 */
    private String suitablePeople;

    /** 包含项目列表 JSON */
    private String items;

    /** 状态 ENABLED/DISABLED */
    // enums/BaseStatus 规范
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
