package com.eldercare.api.controller;

import com.eldercare.api.service.MessageService;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员端消息接口（/api/member/message）
 */
@RestController
@RequestMapping("/api/member/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 消息列表（预约/活动等，不含系统公告） */
    @GetMapping
    public Result<List<Message>> list() {
        return Result.success(messageService.list());
    }

    /** 系统公告列表（首页社区公告） */
    @GetMapping("/announcements")
    public Result<List<Message>> announcements() {
        return Result.success(messageService.announcements());
    }

    /** 消息详情 */
    @GetMapping("/{messageId}")
    public Result<Message> detail(@PathVariable Long messageId) {
        return Result.success(messageService.detail(messageId));
    }

    /** 标记已读 */
    @PutMapping("/{messageId}/read")
    public Result<Void> markRead(@PathVariable Long messageId) {
        messageService.markRead(messageId);
        return Result.success();
    }

    /** 未读消息统计 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount());
    }
}
