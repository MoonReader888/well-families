package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 名称-数值 VO（仪表盘分布统计用，如预约状态/会员等级）
 */
@Data
public class NameValueVO implements Serializable {

    private String name;

    private long value;
}
