package com.eldercare.core.mapper;

import com.eldercare.core.entity.AiConversationSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 会话表 Mapper
 */
@Mapper
public interface AiConversationSessionMapper {

    AiConversationSession selectById(@Param("id") Long id);

    List<AiConversationSession> selectByUserId(@Param("userId") Long userId);

    int insert(AiConversationSession session);

    int update(AiConversationSession session);

    /** 更新会话时间 */
    int touch(@Param("id") Long id);

    int logicDelete(@Param("id") Long id);
}
