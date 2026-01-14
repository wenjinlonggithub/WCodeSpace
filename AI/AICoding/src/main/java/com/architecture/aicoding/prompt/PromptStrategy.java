package com.architecture.aicoding.prompt;

import com.architecture.aicoding.model.CodeGenerationRequest;

/**
 * Prompt策略接口
 * 使用策略模式支持多种Prompt工程技术
 */
public interface PromptStrategy {

    /**
     * 构建Prompt
     * @param request 代码生成请求
     * @return 构建的Prompt
     */
    String buildPrompt(CodeGenerationRequest request);

    /**
     * 获取策略名称
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 获取推荐的模型
     * @return 模型名称
     */
    String getRecommendedModel();
}
