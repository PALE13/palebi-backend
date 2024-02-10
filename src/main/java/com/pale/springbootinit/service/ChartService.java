package com.pale.springbootinit.service;

import com.pale.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.pale.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pale.springbootinit.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface ChartService extends IService<Chart> {

    BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    void handleChartUpdateError(long chartId, String execMessage);

    void checkMultipartFile(MultipartFile multipartFile);
}
