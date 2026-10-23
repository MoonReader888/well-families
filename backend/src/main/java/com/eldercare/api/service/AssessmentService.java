package com.eldercare.api.service;

import com.eldercare.api.dto.SubmitAssessmentRequest;
import com.eldercare.api.vo.AssessmentVO;
import com.eldercare.api.vo.QuestionnaireDetailVO;
import com.eldercare.core.entity.Questionnaire;

import java.util.List;

/**
 * 健康评测服务
 */
public interface AssessmentService {

    /**
     * 已发布问卷列表
     */
    List<Questionnaire> listQuestionnaires();

    /**
     * 问卷详情（含题目）
     */
    QuestionnaireDetailVO getQuestionnaireDetail(Long questionnaireId);

    /**
     * 提交评测：AI 评分 + 生成健康建议，完成赠送积分
     */
    AssessmentVO submit(SubmitAssessmentRequest request);

    /**
     * 我的评测历史
     */
    List<AssessmentVO> history();

    /**
     * 评测详情（校验归属）
     */
    AssessmentVO detail(Long assessmentId);
}
