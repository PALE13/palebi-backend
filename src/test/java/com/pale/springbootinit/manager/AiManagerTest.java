package com.pale.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String answer = aiManager.doChat("分析需求：\n" +
                "分析⽹站⽤户的增⻓情况\n" +
                "原始数据：\n" +
                "⽇期，⽤户数\n" +
                "1号，10\n" +
                "2号，20\n" +
                "3号，30");
        System.out.println(answer);

    }
}