package com.pale.springbootinit.model.vo;

import lombok.Data;

/**
 * Bi返回结果
 */
@Data
public class BiResponse {
    //生成的Echart图表代码
    private String genChart;

    //生成的分析结论
    private String genResult;

    //⽣成的图表id
    private long chartId;
}