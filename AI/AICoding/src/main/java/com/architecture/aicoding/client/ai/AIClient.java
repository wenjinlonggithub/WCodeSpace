package com.architecture.aicoding.client.ai;

/**
 * AI客户端抽象接口
 * 支持多种AI服务提供商（OpenAI, Claude等）
 */
public interface AIClient {

    /**
     * 生成代码或文本
     * @param request 通用请求
     * @return 通用响应
     */
    AIResponse generate(AIRequest request);

    /**
     * 检查连接状态
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取提供商名称
     * @return 提供商名称 (OpenAI, Claude等)
     */
    String getProviderName();

    /**
     * 获取支持的模型列表
     * @return 模型名称数组
     */
    default String[] getSupportedModels() {
        return new String[]{};
    }
}
