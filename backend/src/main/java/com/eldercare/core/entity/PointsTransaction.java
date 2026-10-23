package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分流水实体（points_transaction表）
 *
 */
@Data
public class PointsTransaction implements Serializable {

    private Long id;

    private Long userId;

    /** 变动数量（正数增加/负数扣减） */
    private Integer changeAmount;

    /** 变动后余额 */
    private Integer balanceAfter;

    /** 来源 REGISTER/CHECK_IN/ASSESSMENT/APPOINTMENT/ADMIN/EXPIRE */
    // enums/PointsTransaction 规范
    private String source;

    /** 关联业务 ID（如预约 ID） */
    private Long refId;

    /** 本笔积分过期时间（获取时间 + 1 年） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
