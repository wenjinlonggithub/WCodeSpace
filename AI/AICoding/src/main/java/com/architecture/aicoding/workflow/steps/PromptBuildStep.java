package com.architecture.aicoding.workflow.steps;

import com.architecture.aicoding.prompt.*;
import com.architecture.aicoding.workflow.WorkflowContext;
import com.architecture.aicoding.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt构建步骤
 * 根据选择的策略构建Prompt
 */
public class PromptBuildStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(PromptBuildStep.class);

    @Override
    public void execute(WorkflowContext context) {
        logger.info("开始构建Prompt");

        // 根据请求选择Prompt策略
        PromptStrategy strategy = selectStrategy(context);
        logger.info("选择策略: {}", strategy.getStrategyName());

        // 构建Prompt
        String prompt = strategy.buildPrompt(context.getRequest());
        context.setBuiltPrompt(prompt);

        // 记录推荐模型
        String recommendedModel = strategy.getRecommendedModel();
        context.setAttribute("recommendedModel", recommendedModel);

        // 如果请求中没有指定模型，使用推荐模型
        if (context.getRequest().getModel() == null || context.getRequest().getModel().isEmpty()) {
            context.getRequest().setModel(recommendedModel);
        }

        logger.info("Prompt构建完成，长度: {} 字符", prompt.length());
        logger.debug("构建的Prompt:\n{}", prompt);
    }

    /**
     * 选择Prompt策略
     */
    private PromptStrategy selectStrategy(WorkflowContext context) {
        String strategyType = context.getRequest().getPromptStrategy();

        if (strategyType == null) {
            strategyType = "zero-shot";
        }

        // 策略工厂（可使用工厂模式优化）
        switch (strategyType.toLowerCase()) {
            case "few-shot":
            case "fewshot":
                return new FewShotPromptStrategy();

            case "cot":
            case "chain-of-thought":
            case "chainofthought":
                return new ChainOfThoughtPromptStrategy();

            case "zero-shot":
            case "zeroshot":
            default:
                return new ZeroShotPromptStrategy();
        }
    }

    @Override
    public String getStepName() {
        return "PromptBuild";
    }
}
