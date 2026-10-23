package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康记录实体（health_record 表）
 */
@Data
public class HealthRecord implements Serializable {

    private Long id;

    private Long userId;

    /** 收缩压（mmHg） */
    private Integer systolic;

    /** 舒张压（mmHg） */
    private Integer diastolic;

    /** 血糖（mmol/L） */
    private BigDecimal bloodSugar;

    /** 心率（次/分） */
    private Integer heartRate;

    /** 体重（kg） */
    private BigDecimal weight;

    /** BMI 指数 */
    private BigDecimal bmi;

    /** 备注 */
    private String memo;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
