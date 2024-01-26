package com.pale.springbootinit.constant;

/**
 * 图表常量
 */
public interface ChartConstant {
    /**
     * AI生成的内容分隔符
     */
    String GEN_CONTENT_SPLITS = "【【【【【";

    /**
     * AI 生成的内容的元素为3个
     */
    int GEN_ITEM_NUM = 3;

    /**
     * 生成图表的数据下标
     */
    int GEN_CHART_IDX = 1;

    /**
     * 生成图表的分析结果的下标
     */
    int GEN_RESULT_IDX = 2;

    /**
     * 提取生成的图表的Echarts配置的正则
     */
    String GEN_CHART_REGEX = "\\{(?>[^{}]*(?:\\{[^{}]*}[^{}]*)*)}";

    /**
     * 图表默认名称的前缀
     */
    String DEFAULT_CHART_NAME_PREFIX = "分析图表_";

    /**
     * 图表默认名称的后缀长度
     */
    int DEFAULT_CHART_NAME_SUFFIX_LEN = 10;
}