package com.eldercare.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交健康评测请求
 */
@Data
public class SubmitAssessmentRequest {

    @NotNull(message = "问卷不能为空")
    private Long questionnaireId;

    @Valid
    @NotEmpty(message = "答案不能为空")
    private List<AssessmentAnswerItem> answers;
}
