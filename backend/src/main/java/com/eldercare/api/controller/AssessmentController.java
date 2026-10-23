package com.eldercare.api.controller;

import com.eldercare.api.dto.SubmitAssessmentRequest;
import com.eldercare.api.service.AssessmentService;
import com.eldercare.api.vo.AssessmentVO;
import com.eldercare.api.vo.QuestionnaireDetailVO;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.Questionnaire;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健康评测接口（/api/member/assessment）
 */
@RestController
@RequestMapping("/api/member/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    /** 已发布问卷列表 */
    @GetMapping("/questionnaires")
    public Result<List<Questionnaire>> listQuestionnaires() {
        return Result.success(assessmentService.listQuestionnaires());
    }

    /** 问卷详情（含题目） */
    @GetMapping("/questionnaire/{questionnaireId}")
    public Result<QuestionnaireDetailVO> getQuestionnaire(@PathVariable Long questionnaireId) {
        return Result.success(assessmentService.getQuestionnaireDetail(questionnaireId));
    }

    /** 提交评测（AI 评分） */
    @PostMapping("/submit")
    public Result<AssessmentVO> submit(@Valid @RequestBody SubmitAssessmentRequest request) {
        return Result.success(assessmentService.submit(request));
    }

    /** 我的评测历史 */
    @GetMapping("/history")
    public Result<List<AssessmentVO>> history() {
        return Result.success(assessmentService.history());
    }

    /** 评测详情 */
    @GetMapping("/history/{assessmentId}")
    public Result<AssessmentVO> detail(@PathVariable Long assessmentId) {
        return Result.success(assessmentService.detail(assessmentId));
    }
}
