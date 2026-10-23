package com.eldercare.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新个人信息请求（个人中心）
 * 字段均可选，仅更新非空字段
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "姓名长度不能超过 50 字")
    private String realName;

    @Size(max = 10)
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 身高（cm） */
    private BigDecimal height;

    @Size(max = 500)
    private String avatar;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "紧急联系人电话格式不正确")
    private String emergencyContact;
}
