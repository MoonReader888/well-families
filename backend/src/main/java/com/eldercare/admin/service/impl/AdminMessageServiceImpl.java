package com.eldercare.admin.service.impl;

import com.eldercare.admin.dto.AdminBatchPushMessageRequest;
import com.eldercare.admin.dto.AdminPushMessageRequest;
import com.eldercare.admin.service.AdminMessageService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.Message;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.MessageType;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.AdminMessageVO;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理端消息管理服务实现
 * 站内消息推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<AdminMessageVO> page(int pageNum, int pageSize, String phone) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(messageMapper.selectAdminPage(phone));
    }

    @Override
    public AdminMessageVO detail(Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ResourceNotFoundException("消息不存在");
        }
        AdminMessageVO vo = new AdminMessageVO();
        vo.setId(message.getId());
        vo.setUserId(message.getUserId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        User user = userMapper.selectSafeById(message.getUserId());
        if (user != null) {
            vo.setPhone(user.getPhone());
        }
        return vo;
    }

    @Override
    public void delete(Long messageId) {
        if (messageMapper.selectById(messageId) == null) {
            throw new ResourceNotFoundException("消息不存在");
        }
        messageMapper.logicDelete(messageId);
    }

    @Override
    public void pushSingle(AdminPushMessageRequest request) {
        User user = userMapper.selectSafeById(request.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("接收用户不存在");
        }
        insertMessage(user.getId(), request.getTitle(), request.getContent(), request.getType());
    }

    @Override
    public void pushBatch(AdminBatchPushMessageRequest request) {
        Set<Long> targets = new LinkedHashSet<>();
        if (request.getUserIds() != null) {
            for (Long id : request.getUserIds()) {
                if (id != null) {
                    targets.add(id);
                }
            }
        }
        targets.addAll(userMapper.selectMemberIdsByCondition(
                request.getStatus(), request.getMemberLevel(), request.getStartTime(), request.getEndTime()));

        if (targets.isEmpty()) {
            throw new BusinessException("请选择目标用户或填写筛选条件");
        }
        List<Long> userIds = new ArrayList<>(targets);
        messageMapper.batchInsert(userIds, request.getTitle(), request.getContent(),
                resolveType(request.getType()));

        log.info("批量消息推送完成: count={}", userIds.size());
    }

    @Override
    public long countTarget(String memberLevel, String status, LocalDateTime startTime, LocalDateTime endTime) {
        return userMapper.countMemberByCondition(status, memberLevel, startTime, endTime);
    }

    private void insertMessage(Long userId, String title, String content, String type) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(resolveType(type));
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    private String resolveType(String type) {
        return type == null || type.isBlank() ? MessageType.SYSTEM.getCode() : type;
    }
}
