package com.architecture.aicoding.client;

import com.architecture.aicoding.client.request.ChatCompletionRequest;
import com.architecture.aicoding.client.response.ChatCompletionResponse;

/**
 * OpenAI API客户端接口
 * 定义与OpenAI API交互的核心方法
 */
public interface OpenAIClient {

    /**
     * 发送聊天完成请求
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatCompletionResponse chatCompletion(ChatCompletionRequest request);

    /**
     * 检查API连接状态
     * @return 是否连接成功
     */
    boolean checkConnection();

    /**
     * 获取客户端名称
     * @return 客户端名称
     */
    default String getClientName() {
        return "OpenAI";
    }
}
