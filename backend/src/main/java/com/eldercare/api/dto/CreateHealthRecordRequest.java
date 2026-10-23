package com.eldercare.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 录入健康数据请求（各指标均可选，至少填写一项）
 */
@Data
public class CreateHealthRecordRequest {

    /** 收缩压（mmHg） */
    @Min(value = 50, message = "收缩压数值不合理")
    @Max(value = 250, message = "收缩压数值不合理")
    private Integer systolic;

    /** 舒张压（mmHg） */
    @Min(value = 30, message = "舒张压数值不合理")
    @Max(value = 150, message = "舒张压数值不合理")
    private Integer diastolic;

    /** 血糖（mmol/L） */
    @DecimalMin(value = "1.0", message = "血糖数值不合理")
    @DecimalMax(value = "30.0", message = "血糖数值不合理")
    private BigDecimal bloodSugar;

    /** 心率（次/分） */
    @Min(value = 30, message = "心率数值不合理")
    @Max(value = 200, message = "心率数值不合理")
    private Integer heartRate;

    /** 体重（kg） */
    @DecimalMin(value = "20.0", message = "体重数值不合理")
    @DecimalMax(value = "300.0", message = "体重数值不合理")
    private BigDecimal weight;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过 500 字")
    private String memo;
}
