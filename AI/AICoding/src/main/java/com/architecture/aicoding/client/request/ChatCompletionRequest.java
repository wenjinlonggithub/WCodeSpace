package com.architecture.aicoding.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OpenAI聊天完成请求模型
 * 用于构建发送给OpenAI API的请求
 */
@Data
public class ChatCompletionRequest {

    /**
     * 使用的模型名称，如 gpt-4, gpt-3.5-turbo
     */
    @JsonProperty("model")
    private String model;

    /**
     * 消息列表
     */
    @JsonProperty("messages")
    private List<Message> messages;

    /**
     * 采样温度 (0-2)
     * 较高的值(如0.8)使输出更随机，较低的值(如0.2)使输出更确定
     */
    @JsonProperty("temperature")
    private Double temperature;

    /**
     * 最大token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 核采样概率 (0-1)
     * 与temperature类似，控制输出的随机性
     */
    @JsonProperty("top_p")
    private Double topP;

    /**
     * 频率惩罚 (-2.0 to 2.0)
     * 正值会根据新token在文本中的现有频率来惩罚它们
     */
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    /**
     * 存在惩罚 (-2.0 to 2.0)
     * 正值会根据新token是否出现在文本中来惩罚它们
     */
    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    /**
     * 停止序列
     */
    @JsonProperty("stop")
    private List<String> stop;

    /**
     * 用户标识
     */
    @JsonProperty("user")
    private String user;
}
