package com.eldercare.core.enums;

import com.eldercare.core.exception.BusinessException;

/**
 * 题目类型
 */
public enum QuestionType {
    /** 单选 */
    SINGLE("SINGLE"),
    /** 多选 */
    MULTIPLE("MULTIPLE"),
    /** 文本 */
    TEXT("TEXT");

    private final String code;

    QuestionType(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static void validate(String status) {
        if (status == null) {
            throw new BusinessException("问题类型不能为空");
        }
        for (QuestionType q : values()) {
            if (q.getCode().equals(status)) {
                return;
            }
        }
        throw new BusinessException("非法的问题类型: " + status);
    }

    public static QuestionType fromCode(String code) {
        if (code == null) {
            throw new BusinessException("问题类型不能为空");
        }
        for (QuestionType value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new BusinessException("非法的问题类型: " + code);
    }
}
