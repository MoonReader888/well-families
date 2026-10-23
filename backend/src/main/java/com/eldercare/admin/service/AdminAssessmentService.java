package com.eldercare.admin.service;

import com.eldercare.admin.dto.AdminQuestionRequest;
import com.eldercare.admin.dto.AdminQuestionnaireRequest;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;

import java.util.List;

/**
 * 管理端评测管理服务
 */
public interface AdminAssessmentService {

    /**
     * 问卷列表
     */
    List<Questionnaire> listQuestionnaires();

    /**
     * 新增问卷
     */
    Questionnaire createQuestionnaire(AdminQuestionnaireRequest request);

    /**
     * 修改问卷
     */
    void updateQuestionnaire(Long questionnaireId, AdminQuestionnaireRequest request);

    /**
     * 发布/下架问卷
     */
    void updateQuestionnaireStatus(Long questionnaireId, String status);

    /**
     * 删除问卷（含题目）
     */
    void deleteQuestionnaire(Long questionnaireId);

    /**
     * 问卷题目列表
     */
    List<Question> listQuestions(Long questionnaireId);

    /**
     * 新增题目
     */
    Question createQuestion(Long questionnaireId, AdminQuestionRequest request);

    /**
     * 修改题目
     */
    void updateQuestion(Long questionId, AdminQuestionRequest request);

    /**
     * 删除题目
     */
    void deleteQuestion(Long questionId);
}
