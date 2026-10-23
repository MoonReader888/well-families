package com.eldercare.core.mapper;

import com.eldercare.core.entity.AiConversationMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 对话消息表 Mapper
 */
@Mapper
public interface AiConversationMessageMapper {

    AiConversationMessage selectById(@Param("id") Long id);

    /** 会话全部消息（按时间正序） */
    List<AiConversationMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    /** 最近 N 条消息（取最近 10 轮上下文用） */
    List<AiConversationMessage> selectRecent(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    int insert(AiConversationMessage message);

    int logicDeleteBySession(@Param("sessionId") Long sessionId);

    /** 清理 6 个月前的历史对话 */
    int deleteBefore(@Param("before") java.time.LocalDateTime before);
}
