package com.pale.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenChartByAiRequest implements Serializable {
    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 分析⽬标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}
