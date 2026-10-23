package com.eldercare.core.mapper;

import com.eldercare.core.entity.CommunityActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社区活动表 Mapper
 */
@Mapper
public interface CommunityActivityMapper {

    CommunityActivity selectById(@Param("id") Long id);

    //用户端联表查询活动列表
    List<CommunityActivity> selectVisibleWithRegistration(@Param("userId")Long UserId);

    List<CommunityActivity> selectMyActivitiesWithRegistration(@Param("userId") Long userId);

    List<CommunityActivity> selectList();

    /** 管理端分页查询活动（按标题模糊搜索，默认按 id 升序） */
    List<CommunityActivity> selectActivityPage(@Param("title") String title);

    int insert(CommunityActivity activity);

    int update(CommunityActivity activity);

    /** 并发报名名额占用：行锁保证不超员 */
    int occupyParticipant(@Param("id") Long id);

    /** 取消报名释放名额 */
    int releaseParticipant(@Param("id") Long id);

    int logicDelete(@Param("id") Long id);
}
