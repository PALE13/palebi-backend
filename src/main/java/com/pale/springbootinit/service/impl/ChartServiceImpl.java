package com.pale.springbootinit.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pale.springbootinit.bizmq.BiMqMessageProducer;
import com.pale.springbootinit.common.ErrorCode;
import com.pale.springbootinit.exception.BusinessException;
import com.pale.springbootinit.exception.ThrowUtils;
import com.pale.springbootinit.manager.AiManager;
import com.pale.springbootinit.manager.RedisLimiterManager;
import com.pale.springbootinit.mapper.ChartMapper;
import com.pale.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.pale.springbootinit.model.entity.Chart;
import com.pale.springbootinit.model.entity.User;
import com.pale.springbootinit.model.enums.ChartStatusEnum;
import com.pale.springbootinit.model.vo.BiResponse;
import com.pale.springbootinit.service.ChartService;
import com.pale.springbootinit.service.UserService;
import com.pale.springbootinit.utils.ChartUtils;
import com.pale.springbootinit.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.pale.springbootinit.constant.ChartConstant.*;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {


    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMqMessageProducer biMqMessageProducer;


    /**
     * 图表生成（同步）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @Override
    public BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        //校验文件
        checkMultipartFile(multipartFile);

        // 对用户进行限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

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

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        System.out.println(userInput);


        // 调用AI
        String chartResult = aiManager.doChat(userInput.toString());
        // 解析内容
        String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
        if (splits.length < GEN_ITEM_NUM) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
        }
        // 首次生成的内容
        String genChart = splits[GEN_CHART_IDX].trim();
        String genResult = splits[GEN_RESULT_IDX].trim();


        // 插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setChartId(chart.getId());
        biResponse.setGenResult(genResult);
        return biResponse;
    }

    /**
     * 图表生成（异步）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @Override
    public BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        //校验文件
        checkMultipartFile(multipartFile);

        // 对用户进行限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

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

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        System.out.println(userInput);

        /**
         * 异步化改造
         */
        // 先将图表数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //AI异步生成图表分析结果
        // todo 任务队列满了，抛个异常
        CompletableFuture.runAsync(() -> {
            // todo 等待太久了，抛异常，超时时间
            // 等待-->执行中--> 成功/失败
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            boolean updateChartById = this.updateById(updateChart);
            // todo 修改数据库当中的字段为XXX失败
            if (!updateChartById) {
                handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
                return;
            }

            // 调用AI接口
            String chartResult = aiManager.doChat(userInput.toString());
            // 解析AI生成内容
            String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
            if (splits.length < GEN_ITEM_NUM) {
                //throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
                handleChartUpdateError(chart.getId(), "AI生成错误");
                return;
            }
            String genChart = splits[GEN_CHART_IDX].trim();
            String genResult = splits[GEN_RESULT_IDX].trim();

            // 生成的最终结果
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(ChartStatusEnum.SUCCEED.getValue());

            //更新失败情况
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                handleChartUpdateError(chart.getId(), "更新图表·成功状态·失败");
            }

        }, threadPoolExecutor);
        // 结果返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }


    /**
     * 异步RabbitMQ 消息队列 图表生成
     *
     * @param multipartFile       用户上传的文件信息
     * @param genChartByAiRequest 用户的需求
     * @param request             http request
     * @return
     */
    @Override
    public BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");


        //校验文件
        this.checkMultipartFile(multipartFile);

        // 用户每秒限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

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

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 先插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 任务队列已满
        if (threadPoolExecutor.getQueue().size() > threadPoolExecutor.getMaximumPoolSize()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前任务队列已满");
        }

        Long newChartId = chart.getId();
        //生产者向消息队列发送消息，为图表的id，消费者获取到该id就可以进行消费
        biMqMessageProducer.sendMessage(String.valueOf(newChartId));

        // 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }



    /**
     * 图表状态更新失败，将图表状态更新为failed
     * @param chartId
     * @param execMessage
     */
    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(ChartStatusEnum.FAILED.getValue());
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }


    /**
     * 校验文件是否正确
     * @param multipartFile
     */
    @Override
    public void checkMultipartFile(MultipartFile multipartFile){
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
    }



}




