package com.architecture.aicoding.model;

import lombok.Builder;
import lombok.Data;

/**
 * 代码生成请求模型
 * 封装用户的代码生成需求
 */
@Data
@Builder
public class CodeGenerationRequest {

    /**
     * 需求描述（自然语言）
     */
    private String requirement;

    /**
     * Prompt策略（zero-shot, few-shot, cot）
     */
    @Builder.Default
    private String promptStrategy = "zero-shot";

    /**
     * 使用的模型（gpt-4, gpt-3.5-turbo）
     */
    @Builder.Default
    private String model = "gpt-4";

    /**
     * 项目上下文
     */
    private ProjectContext projectContext;

    /**
     * 代码风格
     */
    private CodeStyle codeStyle;

    /**
     * 是否验证生成的代码
     */
    @Builder.Default
    private boolean validateCode = true;

    /**
     * 是否支持多轮对话优化
     */
    @Builder.Default
    private boolean multiRound = false;

    /**
     * 温度参数（0-2）
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    @Builder.Default
    private Integer maxTokens = 2000;
}
