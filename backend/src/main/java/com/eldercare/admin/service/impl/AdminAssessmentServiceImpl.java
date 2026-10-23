package com.eldercare.admin.service.impl;

import com.eldercare.admin.dto.AdminQuestionRequest;
import com.eldercare.admin.dto.AdminQuestionnaireRequest;
import com.eldercare.admin.dto.QuestionOptionRequest;
import com.eldercare.admin.service.AdminAssessmentService;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;
import com.eldercare.core.enums.QuestionType;
import com.eldercare.core.enums.QuestionnaireStatus;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.QuestionMapper;
import com.eldercare.core.mapper.QuestionnaireMapper;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端评测管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAssessmentServiceImpl implements AdminAssessmentService {


    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Questionnaire> listQuestionnaires() {
        return questionnaireMapper.selectList();
    }

    @Override
    public Questionnaire createQuestionnaire(AdminQuestionnaireRequest request) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        questionnaire.setStatus(request.getStatus() != null ? request.getStatus() : QuestionnaireStatus.DRAFT.getCode());
        questionnaire.setIsRated(request.getIsRated() != null ? request.getIsRated() : 0);
        questionnaireMapper.insert(questionnaire);
        return questionnaire;
    }

    @Override
    public void updateQuestionnaire(Long questionnaireId, AdminQuestionnaireRequest request) {
        Questionnaire questionnaire = getQuestionnaire(questionnaireId);
        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            questionnaire.setStatus(request.getStatus());
        }
        if(request.getIsRated()!=null){
            questionnaire.setIsRated(request.getIsRated());
        }
        questionnaireMapper.update(questionnaire);
    }

    @Override
    public void updateQuestionnaireStatus(Long questionnaireId, String status) {
        getQuestionnaire(questionnaireId);
        QuestionnaireStatus.validate(status);
        questionnaireMapper.updateStatus(questionnaireId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestionnaire(Long questionnaireId) {
        getQuestionnaire(questionnaireId);
        questionnaireMapper.logicDelete(questionnaireId);

        for (Question question : questionMapper.selectByQuestionnaireId(questionnaireId)) {
            questionMapper.logicDelete(question.getId());
        }
    }

    @Override
    public List<Question> listQuestions(Long questionnaireId) {
        getQuestionnaire(questionnaireId);
        return questionMapper.selectByQuestionnaireId(questionnaireId);
    }

    @Override
    public Question createQuestion(Long questionnaireId, AdminQuestionRequest request) {
        getQuestionnaire(questionnaireId);
        Question question = new Question();
        question.setQuestionnaireId(questionnaireId);
        applyRequest(question, request);
        questionMapper.insert(question);
        return question;
    }

    @Override
    public void updateQuestion(Long questionId, AdminQuestionRequest request) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new ResourceNotFoundException("题目不存在");
        }
        applyRequest(question, request);
        questionMapper.update(question);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        if (questionMapper.selectById(questionId) == null) {
            throw new ResourceNotFoundException("题目不存在");
        }
        questionMapper.logicDelete(questionId);
    }

    /**
     * 获取问卷
     * @param questionnaireId
     * @return
     */
    private Questionnaire getQuestionnaire(Long questionnaireId) {
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new ResourceNotFoundException("问卷不存在");
        }
        return questionnaire;
    }

    /**
     * 应用题目请求参数到题目实体
     * @param question
     * @param request
     */
    private void applyRequest(Question question, AdminQuestionRequest request) {

        QuestionType.validate(request.getType());

        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        if (QuestionType.TEXT.getCode().equals(request.getType())) {
            question.setOptions(null);
        } else {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw new BusinessException("单选/多选题必须提供选项");
            }

            List<Map<String, Object>> optionObjs = new ArrayList<>();
            for (QuestionOptionRequest option : request.getOptions()) {
                if (option.getLabel() == null || option.getLabel().isBlank()) {
                    throw new BusinessException("选项文本不能为空");
                }
                int score = option.getScore() == null ? 0 : option.getScore();
                if (score < 0 || score > 100) {
                    throw new BusinessException("选项分值需在 0-100 之间");
                }
                Map<String, Object> obj = new LinkedHashMap<>();
                obj.put("label", option.getLabel().trim());
                obj.put("score", score);
                optionObjs.add(obj);
            }
            question.setOptions(objectMapper.writeValueAsString(optionObjs));
        }
    }
}
