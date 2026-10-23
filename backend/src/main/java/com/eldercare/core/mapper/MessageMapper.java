package com.eldercare.core.mapper;

import com.eldercare.core.entity.Message;
import com.eldercare.core.vo.AdminMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内消息表 Mapper
 */
@Mapper
public interface MessageMapper {

    Message selectById(@Param("id") Long id);

    List<Message> selectByUserId(@Param("userId") Long userId);

    /** 系统公告列表（SYSTEM 类型，首页社区公告） */
    List<Message> selectAnnouncements(@Param("userId") Long userId);

    /** 未读消息数 */
    long countUnread(@Param("userId") Long userId);

    int insert(Message message);

    /** 批量推送（消息管理） */
    int batchInsert(@Param("userIds") List<Long> userIds,
                    @Param("title") String title,
                    @Param("content") String content,
                    @Param("type") String type);

    /** 标记已读 */
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    /** 管理端消息分页列表（联表带手机号，按手机号筛选，配合 PageHelper） */
    List<AdminMessageVO> selectAdminPage(@Param("phone") String phone);

    int logicDelete(@Param("id") Long id);
}
