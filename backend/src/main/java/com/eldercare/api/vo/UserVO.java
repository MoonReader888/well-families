package com.eldercare.api.vo;

import com.eldercare.core.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户信息响应（不含密码等敏感字段）
 */
@Data
public class UserVO {

    private Long id;
    private String phone;
    private String realName;
    private String avatar;
    private String memberLevel;
    private Integer points;
    private String role;

    /** 性别 */
    private String gender;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 身高（cm） */
    private BigDecimal height;

    /** 紧急联系人电话 */
    private String emergencyContact;

    public static UserVO from(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setPoints(user.getPoints());
        vo.setRole(user.getRole());
        vo.setGender(user.getGender());
        vo.setBirthDate(user.getBirthDate());
        vo.setHeight(user.getHeight());
        vo.setEmergencyContact(user.getEmergencyContact());
        return vo;
    }
}
