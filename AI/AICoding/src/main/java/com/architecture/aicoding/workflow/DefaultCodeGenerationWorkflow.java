package com.architecture.aicoding.workflow;

import com.architecture.aicoding.model.CodeGenerationRequest;
import com.architecture.aicoding.model.CodeGenerationResponse;
import com.architecture.aicoding.workflow.steps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认代码生成工作流实现
 * 按照标准的5个步骤顺序执行代码生成流程
 */
public class DefaultCodeGenerationWorkflow implements CodeGenerationWorkflow {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCodeGenerationWorkflow.class);

    private final List<WorkflowStep> steps;

    /**
     * 构造函数，初始化工作流步骤
     */
    public DefaultCodeGenerationWorkflow() {
        this.steps = new ArrayList<>();
        initializeSteps();
    }

    /**
     * 初始化工作流步骤
     */
    private void initializeSteps() {
        // 按顺序添加5个步骤
        steps.add(new RequirementParseStep());      // 1. 需求解析
        steps.add(new PromptBuildStep());           // 2. Prompt构建
        steps.add(new APICallStep());               // 3. API调用
        steps.add(new CodeExtractionStep());        // 4. 代码提取
        steps.add(new CodeValidationStep());        // 5. 代码验证

        logger.info("工作流初始化完成，共 {} 个步骤", steps.size());
    }

    @Override
    public CodeGenerationResponse execute(CodeGenerationRequest request) {
        logger.info("========================================");
        logger.info("开始执行代码生成工作流");
        logger.info("需求: {}", request.getRequirement());
        logger.info("策略: {}", request.getPromptStrategy());
        logger.info("模型: {}", request.getModel());
        logger.info("========================================");

        // 创建工作流上下文
        WorkflowContext context = new WorkflowContext(request);

        // 顺序执行各个步骤
        for (int i = 0; i < steps.size(); i++) {
            WorkflowStep step = steps.get(i);

            try {
                logger.info(">>> 步骤 {}/{}: {} <<<", i + 1, steps.size(), step.getStepName());

                // 执行步骤
                step.execute(context);

                logger.info("步骤 {} 执行成功", step.getStepName());

            } catch (Exception e) {
                logger.error("步骤 {} 执行失败: {}", step.getStepName(), e.getMessage(), e);
                context.setError(e);
                break; // 中断流程
            }
        }

        // 构建响应
        CodeGenerationResponse response = context.buildResponse();

        logger.info("========================================");
        if (response.isSuccess()) {
            logger.info("工作流执行成功！");
            logger.info("生成代码: {} 行", response.getGeneratedCode().getLineCount());
            logger.info("总耗时: {} ms", response.getDurationMs());
        } else {
            logger.error("工作流执行失败: {}", response.getError());
        }
        logger.info("========================================");

        return response;
    }

    @Override
    public String getWorkflowName() {
        return "DefaultCodeGenerationWorkflow";
    }

    /**
     * 获取步骤列表（用于测试和调试）
     */
    public List<WorkflowStep> getSteps() {
        return new ArrayList<>(steps);
    }
}
