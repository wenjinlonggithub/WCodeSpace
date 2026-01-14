package com.architecture.aicoding.client.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI请求通用模型
 * 抽象不同AI提供商的请求格式
 */
@Data
@Builder
public class AIRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 系统提示（角色定义）
     */
    private String systemPrompt;

    /**
     * 用户消息（主要内容）
     */
    private String userMessage;

    /**
     * 历史消息（用于多轮对话）
     */
    private List<Message> messageHistory;

    /**
     * 温度参数 (0-1或0-2，取决于提供商)
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    @Builder.Default
    private Integer maxTokens = 2000;

    /**
     * Top-P参数
     */
    private Double topP;

    /**
     * 停止序列
     */
    private List<String> stopSequences;

    /**
     * 消息内部类
     */
    @Data
    @Builder
    public static class Message {
        private String role;    // system, user, assistant
        private String content;
    }
}
