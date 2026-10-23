package com.eldercare.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量生成预约时段请求
 */
@Data
public class AdminSlotBatchRequest {

    /** 日期列表 */
    @NotEmpty(message = "请选择日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> dates;

    /** 时间段列表，如 ["09:00-10:00", "10:00-11:00"] */
    @NotEmpty(message = "请选择时间段")
    private List<String> timeRanges;

    /** 每时段最大预约人数 */
    @NotNull(message = "最大预约人数不能为空")
    @Min(value = 1, message = "最大预约人数至少为 1")
    @Max(value = 1000, message = "最大预约人数过大")
    private Integer maxCount;
}
