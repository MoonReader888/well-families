package com.eldercare.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 编辑预约时段请求
 */
@Data
public class AdminSlotUpdateRequest {

    /** 预约日期 */
    @NotNull(message = "请选择预约日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointDate;

    /** 时间段，如 "09:00-10:00" */
    @NotBlank(message = "请填写时间段")
    private String timeRange;

    /** 最大预约人数 */
    @NotNull(message = "最大预约人数不能为空")
    @Min(value = 1, message = "最大预约人数至少为 1")
    @Max(value = 1000, message = "最大预约人数过大")
    private Integer maxCount;

    /** 状态 AVAILABLE/CLOSED（FULL 由系统按人数自动判定） */
    @NotBlank(message = "请选择时段状态")
    private String status;
}
