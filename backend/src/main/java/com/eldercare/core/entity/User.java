package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体（user 表）
 */
@Data
public class User implements Serializable {

    private Long id;

    /** 手机号 */
    private String phone;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 性别 */
    private String gender;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 身高（cm），用于计算 BMI */
    private java.math.BigDecimal height;

    /** 头像 URL */
    private String avatar;

    /** 紧急联系人电话 */
    private String emergencyContact;

    /** 会员等级 NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    private String memberLevel;

    /** 积分 */
    private Integer points;

    /** 状态 ENABLED/DISABLED */
    // enums/BaseStatus 规范
    private String status;

    /** 角色 MEMBER/ADMIN */
    private String role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
