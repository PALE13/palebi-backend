package com.pale.springbootinit.manager;


import com.pale.springbootinit.common.ErrorCode;
import com.pale.springbootinit.config.AiModelConfig;
import com.pale.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AiManager {


    @Resource
    private YuCongMingClient yuCongMingClient;


    @Resource
    private AiModelConfig aiModelConfig;


    /**
     * AI对话
     *
     * @param message
     * @return
     */

    public String doChat(String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(aiModelConfig.getModelId());
        devChatRequest.setMessage(message);

        // 获取响应
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if(response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应错误");
        }
        return response.getData().getContent();

    }


}
