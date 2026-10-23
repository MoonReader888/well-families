package com.eldercare.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问卷实体（questionnaire 表）
 */
@Data
public class Questionnaire implements Serializable {

    private Long id;

    private String title;

    private String description;

    /** 状态 DRAFT/PUBLISHED */
    // enums/QuestionnairStatus 规范
    private String status;

    //是否计分
    private Integer isRated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除 0/1 */
    private Integer deleted;
}
