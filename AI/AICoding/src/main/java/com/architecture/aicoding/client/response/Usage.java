package com.architecture.aicoding.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Token使用统计模型
 * 用于跟踪API调用的token消耗
 */
@Data
public class Usage {

    /**
     * 提示词使用的token数
     */
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    /**
     * 完成内容使用的token数
     */
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    /**
     * 总token数
     */
    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
