package com.eldercare.core.enums;

import com.eldercare.core.exception.BusinessException;

/**
 * 问卷状态
 */
public enum QuestionnaireStatus {
    /** 草稿 */
    DRAFT("DRAFT"),
    /** 已发布 */
    PUBLISHED("PUBLISHED");

    private final String code;

    QuestionnaireStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static void validate(String status) {
        if (status == null) {
            throw new BusinessException("问卷状态不能为空");
        }
        for (QuestionnaireStatus e : values()) {
            if (e.getCode().equals(status)) {
                return;
            }
        }
        throw new BusinessException("非法的问卷状态: " + status);
    }
}
