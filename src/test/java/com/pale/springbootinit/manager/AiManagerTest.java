package com.pale.springbootinit.manager;

import com.pale.springbootinit.common.ErrorCode;
import com.pale.springbootinit.exception.BusinessException;
import com.pale.springbootinit.utils.ChartUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static com.pale.springbootinit.constant.ChartConstant.*;
import static com.pale.springbootinit.constant.ChartConstant.GEN_RESULT_IDX;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String answer = aiManager.doChat("分析需求：\n" +
                "分析网站用户增长情况，请使用柱状图\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30\n" +
                "4号,50\n" +
                "5号,0");

        System.out.println(answer);
//        // 解析内容
//        String[] splits = answer.split(GEN_CONTENT_SPLITS);
//        // 首次生成的内容
//        String preGenChart = splits[GEN_CHART_IDX].trim();
//        String genResult = splits[GEN_RESULT_IDX].trim();
//        System.out.println(preGenChart);
//        System.out.println(genResult);

    }
}