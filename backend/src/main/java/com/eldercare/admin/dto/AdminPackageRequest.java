package com.eldercare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 体检套餐新增/修改请求
 */
@Data
public class AdminPackageRequest {

    @NotBlank(message = "套餐名称不能为空")
    private String name;

    private String description;

    /** 价格（积分） */
    @NotNull(message = "套餐价格不能为空")
    private Integer price;

    /** 适合人群 */
    private String suitablePeople;

    /** 包含项目列表 */
    private List<String> items;

    /** 状态 ENABLED/DISABLED */
    private String status;
}
