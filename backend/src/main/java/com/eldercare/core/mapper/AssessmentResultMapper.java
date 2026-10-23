package com.eldercare.core.mapper;

import com.eldercare.api.vo.AssessmentVO;
import com.eldercare.core.entity.AssessmentResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评测结果表 Mapper
 */
@Mapper
public interface AssessmentResultMapper {

    AssessmentResult selectById(@Param("id") Long id);

    List<AssessmentResult> selectByUserId(@Param("userId") Long userId);

    // 联表查询评测历史
    List<AssessmentVO> selectHistoryByUserId(Long userId);

    int insert(AssessmentResult result);

    int logicDelete(@Param("id") Long id);

    /** 评测结果归档：超过保留期（2 年）逻辑删除 */
    int archiveBefore(@Param("before") LocalDateTime before);

    int updateSuggestion(@Param("id") Long id,
                         @Param("status") String status,
                         @Param("aiSuggestion") String aiSuggestion,
                         @Param("errorMsg") String errorMsg);
}
