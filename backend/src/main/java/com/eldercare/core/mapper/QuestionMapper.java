package com.eldercare.core.mapper;

import com.eldercare.core.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目表 Mapper
 */
@Mapper
public interface QuestionMapper {

    Question selectById(@Param("id") Long id);

    List<Question> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    int insert(Question question);

    int update(Question question);

    int logicDelete(@Param("id") Long id);
}
