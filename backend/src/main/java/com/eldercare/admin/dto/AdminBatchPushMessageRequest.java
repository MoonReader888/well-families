package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量消息推送请求
 * 目标人群支持两种方式：手动指定 userIds，或按条件筛选（memberLevel/status/注册时间范围），可叠加并去重
 */
@Data
public class AdminBatchPushMessageRequest {

    /** 手动指定的用户 ID（可选，与条件筛选叠加） */
    private List<Long> userIds;

    /** 按条件筛选：会员等级 */
    private String memberLevel;

    /** 按条件筛选：账号状态 ENABLED/DISABLED */
    private String status;

    /** 按条件筛选：注册时间范围起 */
    private LocalDateTime startTime;

    /** 按条件筛选：注册时间范围止 */
    private LocalDateTime endTime;

    @NotBlank(message = "消息标题不能为空")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 类型，默认 SYSTEM */
    private String type;
}
