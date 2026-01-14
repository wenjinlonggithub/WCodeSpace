package com.architecture.aicoding.service;

import com.architecture.aicoding.model.CodeGenerationRequest;
import com.architecture.aicoding.model.CodeGenerationResponse;
import com.architecture.aicoding.workflow.CodeGenerationWorkflow;
import com.architecture.aicoding.workflow.DefaultCodeGenerationWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代码生成服务实现
 * 封装代码生成工作流，提供额外的业务逻辑
 */
public class CodeGenerationServiceImpl implements CodeGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerationServiceImpl.class);

    private final CodeGenerationWorkflow workflow;
    private final TokenCounterService tokenCounterService;

    /**
     * 构造函数
     */
    public CodeGenerationServiceImpl() {
        this.workflow = new DefaultCodeGenerationWorkflow();
        this.tokenCounterService = new TokenCounterService();
        logger.info("代码生成服务初始化完成");
    }

    /**
     * 自定义工作流的构造函数（用于测试和扩展）
     */
    public CodeGenerationServiceImpl(CodeGenerationWorkflow workflow) {
        this.workflow = workflow;
        this.tokenCounterService = new TokenCounterService();
    }

    @Override
    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        logger.info("收到代码生成请求: {}", request.getRequirement());

        // 参数验证
        validateRequest(request);

        // 执行工作流
        CodeGenerationResponse response = workflow.execute(request);

        // 添加成本信息（如果有token统计）
        if (response.getMetadata().containsKey("totalTokens")) {
            int totalTokens = (Integer) response.getMetadata("totalTokens");
            int promptTokens = (Integer) response.getMetadata("promptTokens");
            int completionTokens = (Integer) response.getMetadata("completionTokens");

            double cost = tokenCounterService.calculateCost(
                promptTokens,
                completionTokens,
                request.getModel()
            );

            response.addMetadata("estimatedCost", cost);
            response.addMetadata("estimatedCostUSD", String.format("$%.4f", cost));

            logger.info("成本估算: ${}", String.format("%.4f", cost));
        }

        return response;
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(CodeGenerationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }

        if (request.getRequirement() == null || request.getRequirement().trim().isEmpty()) {
            throw new IllegalArgumentException("需求描述不能为空");
        }

        // 设置默认值
        if (request.getPromptStrategy() == null) {
            request.setPromptStrategy("zero-shot");
        }

        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel("gpt-4");
        }

        logger.debug("请求验证通过");
    }
}
