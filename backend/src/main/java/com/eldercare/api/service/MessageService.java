package com.eldercare.api.service;

import com.eldercare.core.entity.Message;

import java.util.List;

/**
 * 会员端消息通知服务
 */
public interface MessageService {

    /**
     * 我的消息列表（倒序，不含系统公告）
     */
    List<Message> list();

    /**
     * 系统公告列表（SYSTEM 类型，首页社区公告，倒序）
     */
    List<Message> announcements();

    /**
     * 消息详情（校验归属）
     */
    Message detail(Long messageId);

    /**
     * 标记已读
     */
    void markRead(Long messageId);

    /**
     * 未读消息数
     */
    long unreadCount();
}
