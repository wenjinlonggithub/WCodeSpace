package com.architecture.aicoding.client.ai;

import lombok.Builder;
import lombok.Data;

/**
 * AI响应通用模型
 * 抽象不同AI提供商的响应格式
 */
@Data
@Builder
public class AIResponse {

    /**
     * 生成的内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 提供商名称
     */
    private String provider;

    /**
     * 完成原因 (stop, length, content_filter等)
     */
    private String finishReason;

    /**
     * Token使用统计
     */
    private TokenUsage tokenUsage;

    /**
     * Token使用统计内部类
     */
    @Data
    @Builder
    public static class TokenUsage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
