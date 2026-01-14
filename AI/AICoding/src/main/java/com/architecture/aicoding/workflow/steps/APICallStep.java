package com.architecture.aicoding.workflow.steps;

import com.architecture.aicoding.client.ai.AIClient;
import com.architecture.aicoding.client.ai.AIClientFactory;
import com.architecture.aicoding.client.ai.AIRequest;
import com.architecture.aicoding.client.ai.AIResponse;
import com.architecture.aicoding.retry.RetryHandler;
import com.architecture.aicoding.workflow.WorkflowContext;
import com.architecture.aicoding.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API调用步骤
 * 调用AI API获取生成结果（支持OpenAI, Claude等多个提供商）
 */
public class APICallStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(APICallStep.class);

    private final AIClient client;
    private final RetryHandler retryHandler;

    public APICallStep() {
        this.client = AIClientFactory.createDefaultClient();
        this.retryHandler = new RetryHandler();
        logger.info("使用AI提供商: {}", client.getProviderName());
    }

    /**
     * 自定义AI客户端的构造函数（用于测试）
     */
    public APICallStep(AIClient client) {
        this.client = client;
        this.retryHandler = new RetryHandler();
    }

    @Override
    public void execute(WorkflowContext context) {
        logger.info("开始调用AI API - 提供商: {}", client.getProviderName());

        // 构建请求
        AIRequest request = buildRequest(context);

        // 带重试的API调用
        long startTime = System.currentTimeMillis();

        AIResponse response = retryHandler.executeWithRetry(() -> {
            logger.debug("执行API调用...");
            return client.generate(request);
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 提取响应内容
        if (response.getContent() == null || response.getContent().isEmpty()) {
            throw new IllegalStateException("API响应中没有内容");
        }

        context.setRawResponse(response.getContent());

        // 记录提供商信息
        context.setAttribute("aiProvider", response.getProvider());
        context.setAttribute("aiModel", response.getModel());

        // 记录token使用情况
        if (response.getTokenUsage() != null) {
            context.setAttribute("promptTokens", response.getTokenUsage().getInputTokens());
            context.setAttribute("completionTokens", response.getTokenUsage().getOutputTokens());
            context.setAttribute("totalTokens", response.getTokenUsage().getTotalTokens());

            logger.info("API调用成功 - 提供商: {}, 模型: {}, 耗时: {}ms, Token使用 - Input: {}, Output: {}, Total: {}",
                response.getProvider(),
                response.getModel(),
                duration,
                response.getTokenUsage().getInputTokens(),
                response.getTokenUsage().getOutputTokens(),
                response.getTokenUsage().getTotalTokens()
            );
        } else {
            logger.info("API调用成功 - 提供商: {}, 模型: {}, 耗时: {}ms",
                response.getProvider(), response.getModel(), duration);
        }

        context.setAttribute("apiCallDuration", duration);
    }

    /**
     * 构建AI请求（通用格式）
     */
    private AIRequest buildRequest(WorkflowContext context) {
        AIRequest.AIRequestBuilder builder = AIRequest.builder();

        // 设置系统提示
        builder.systemPrompt("你是一个专业的Java开发工程师，擅长编写高质量、符合最佳实践的代码。");

        // 设置用户消息
        builder.userMessage(context.getBuiltPrompt());

        // 设置模型参数
        String model = context.getRequest().getModel();
        if (model == null || model.isEmpty()) {
            model = (String) context.getAttribute("recommendedModel");
        }
        builder.model(model);

        // 设置temperature
        Double temperature = context.getRequest().getTemperature();
        builder.temperature(temperature != null ? temperature : 0.7);

        // 设置maxTokens
        Integer maxTokens = context.getRequest().getMaxTokens();
        builder.maxTokens(maxTokens != null ? maxTokens : 2000);

        AIRequest request = builder.build();

        logger.debug("构建的AI请求 - Model: {}, Temperature: {}, MaxTokens: {}",
            request.getModel(), request.getTemperature(), request.getMaxTokens());

        return request;
    }

    @Override
    public String getStepName() {
        return "APICall";
    }
}
