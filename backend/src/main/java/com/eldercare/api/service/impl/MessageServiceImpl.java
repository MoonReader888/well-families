package com.eldercare.api.service.impl;

import com.eldercare.api.service.MessageService;
import com.eldercare.core.entity.Message;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员端消息通知服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public List<Message> list() {
        return messageMapper.selectByUserId(UserContext.currentUserId());
    }

    /**
     * 公告
     */
    @Override
    public List<Message> announcements() {
        return messageMapper.selectAnnouncements(UserContext.currentUserId());
    }

    @Override
    public Message detail(Long messageId) {
        Long userId =UserContext.currentUserId();
        Message message = getOwnedMessage(messageId);
        if(message.getIsRead()==0){
            messageMapper.markRead(messageId,userId);
            message.setIsRead(1);
        }
        return message;
    }

    @Override
    public void markRead(Long messageId) {
        Long userId = UserContext.currentUserId();

        Message message = messageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("消息不存在");
        }

        if (message.getIsRead() == 1) {
            return;
        }

        messageMapper.markRead(messageId, userId);
    }

    @Override
    public long unreadCount() {
        Long userId = UserContext.currentUserId();
        return messageMapper.countUnread(userId);
    }

    /**
     * 获取用户自己的消息
     */
    private Message getOwnedMessage(Long messageId) {
        Message message = messageMapper.selectById(messageId);
        Long userId = UserContext.currentUserId();
        if (message == null || !message.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("消息不存在");
        }
        return message;
    }
}
