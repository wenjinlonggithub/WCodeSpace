package com.architecture.aicoding.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OpenAI聊天完成响应模型
 * 用于解析OpenAI API的响应
 */
@Data
public class ChatCompletionResponse {

    /**
     * 响应ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 对象类型，通常是 "chat.completion"
     */
    @JsonProperty("object")
    private String object;

    /**
     * 创建时间戳
     */
    @JsonProperty("created")
    private Long created;

    /**
     * 使用的模型
     */
    @JsonProperty("model")
    private String model;

    /**
     * 响应选择列表
     */
    @JsonProperty("choices")
    private List<Choice> choices;

    /**
     * Token使用统计
     */
    @JsonProperty("usage")
    private Usage usage;

    /**
     * 系统指纹
     */
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
}
