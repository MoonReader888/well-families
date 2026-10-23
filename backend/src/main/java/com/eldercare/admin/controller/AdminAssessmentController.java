package com.eldercare.admin.controller;

import com.eldercare.admin.dto.AdminQuestionRequest;
import com.eldercare.admin.dto.AdminQuestionnaireRequest;
import com.eldercare.admin.service.AdminAssessmentService;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端评测管理接口（/api/admin/assessment）
 */
@RestController
@RequestMapping("/api/admin/assessment")
@RequiredArgsConstructor
public class AdminAssessmentController {

    private final AdminAssessmentService adminAssessmentService;

    /** 问卷列表 */
    @GetMapping("/questionnaires")
    public Result<List<Questionnaire>> listQuestionnaires() {
        return Result.success(adminAssessmentService.listQuestionnaires());
    }

    /** 新增问卷 */
    @PostMapping("/questionnaires")
    public Result<Questionnaire> createQuestionnaire(@Valid @RequestBody AdminQuestionnaireRequest request) {
        return Result.success(adminAssessmentService.createQuestionnaire(request));
    }

    /** 修改问卷 */
    @PutMapping("/questionnaires/{questionnaireId}")
    public Result<Void> updateQuestionnaire(@PathVariable Long questionnaireId,
                                            @Valid @RequestBody AdminQuestionnaireRequest request) {
        adminAssessmentService.updateQuestionnaire(questionnaireId, request);
        return Result.success();
    }

    /** 发布/下架问卷 */
    @PutMapping("/questionnaires/{questionnaireId}/status")
    public Result<Void> updateQuestionnaireStatus(@PathVariable Long questionnaireId,
                                                  @RequestParam String status) {
        adminAssessmentService.updateQuestionnaireStatus(questionnaireId, status);
        return Result.success();
    }

    /** 删除问卷 */
    @DeleteMapping("/questionnaires/{questionnaireId}")
    public Result<Void> deleteQuestionnaire(@PathVariable Long questionnaireId) {
        adminAssessmentService.deleteQuestionnaire(questionnaireId);
        return Result.success();
    }

    /** 问卷题目列表 */
    @GetMapping("/questionnaires/{questionnaireId}/questions")
    public Result<List<Question>> listQuestions(@PathVariable Long questionnaireId) {
        return Result.success(adminAssessmentService.listQuestions(questionnaireId));
    }

    /** 新增题目 */
    @PostMapping("/questionnaires/{questionnaireId}/questions")
    public Result<Question> createQuestion(@PathVariable Long questionnaireId,
                                           @Valid @RequestBody AdminQuestionRequest request) {
        return Result.success(adminAssessmentService.createQuestion(questionnaireId, request));
    }

    /** 修改题目 */
    @PutMapping("/questions/{questionId}")
    public Result<Void> updateQuestion(@PathVariable Long questionId,
                                       @Valid @RequestBody AdminQuestionRequest request) {
        adminAssessmentService.updateQuestion(questionId, request);
        return Result.success();
    }

    /** 删除题目 */
    @DeleteMapping("/questions/{questionId}")
    public Result<Void> deleteQuestion(@PathVariable Long questionId) {
        adminAssessmentService.deleteQuestion(questionId);
        return Result.success();
    }
}
