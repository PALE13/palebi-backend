package com.pale.springbootinit.bizmq;

import com.pale.springbootinit.common.ErrorCode;
import com.pale.springbootinit.constant.BiMqConstant;
import com.pale.springbootinit.exception.BusinessException;
import com.pale.springbootinit.manager.AiManager;
import com.pale.springbootinit.model.entity.Chart;
import com.pale.springbootinit.model.enums.ChartStatusEnum;
import com.pale.springbootinit.service.ChartService;
import com.pale.springbootinit.utils.ChartUtils;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.pale.springbootinit.constant.ChartConstant.*;

/**
 * BI项目 消费者
 */
@Component
@Slf4j
public class BiMqMessageConsumer {

    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;

    /**
     * 指定程序监听的消息队列和确认机制
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE}, ackMode = "MANUAL")
    private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message={}", message);
        if (StringUtils.isBlank(message)) {
            // 消息为空，则拒绝掉消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接受到的消息为空");
        }
        // 获取到图表的id
        long chartId = Long.parseLong(message);
        // 从数据库中取出id
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表为空");
        }
        // 等待-->执行中--> 成功/失败
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
        boolean updateChartById = chartService.updateById(updateChart);
        if (!updateChartById) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setStatus(ChartStatusEnum.FAILED.getValue());
            chartService.updateById(updateChartFailed);
            chartService.handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
            return;
        }
        // 调用AI
        String userInput = buildUserInput(chart);
        System.out.printf(userInput);
        String chartResult = aiManager.doChat(userInput);
        System.out.println(chartResult);


        // 解析内容
        String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
        if (splits.length < GEN_ITEM_NUM) {
            //throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
            chartService.handleChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        // 生成前的内容
        String preGenChart = splits[GEN_CHART_IDX].trim();
        String genResult = splits[GEN_RESULT_IDX].trim();
        // 生成后端检验
        String validGenChart = ChartUtils.getValidGenChart(preGenChart);

        // 生成的最终结果-成功
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(preGenChart);
        //updateChartResult.setGenChart(validGenChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setStatus(ChartStatusEnum.FAILED.getValue());
            chartService.updateById(updateChartFailed);
            chartService.handleChartUpdateError(chart.getId(), "更新图表·成功状态·失败");
        }

        // 成功，则确认消息
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 构建用户的输入信息
     *
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartData = chart.getChartData();

        // 无需Prompt，直接调用现有模型
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(chartData).append("\n");
        return userInput.toString();
    }
}