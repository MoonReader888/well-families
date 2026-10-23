package com.eldercare.core.mapper;

import com.eldercare.core.entity.Questionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 问卷表 Mapper
 */
@Mapper
public interface QuestionnaireMapper {

    //获取单个问卷
    Questionnaire selectById(@Param("id") Long id);

    //会员端问卷
    List<Questionnaire> selectPublished();

    //管理员端问卷
    List<Questionnaire> selectList();

    int insert(Questionnaire questionnaire);

    int update(Questionnaire questionnaire);

    /** 发布/下架 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int logicDelete(@Param("id") Long id);
}
