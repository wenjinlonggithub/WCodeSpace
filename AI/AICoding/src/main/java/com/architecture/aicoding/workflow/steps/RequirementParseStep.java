package com.architecture.aicoding.workflow.steps;

import com.architecture.aicoding.workflow.WorkflowContext;
import com.architecture.aicoding.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 需求解析步骤
 * 分析用户输入的自然语言需求，识别代码类型和设计模式
 */
public class RequirementParseStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(RequirementParseStep.class);

    @Override
    public void execute(WorkflowContext context) {
        String requirement = context.getRequest().getRequirement();

        // 需求验证
        if (requirement == null || requirement.trim().isEmpty()) {
            throw new IllegalArgumentException("需求描述不能为空");
        }

        logger.info("开始解析需求: {}", requirement);

        // 识别代码类型（类、接口、枚举等）
        String codeType = identifyCodeType(requirement);
        context.setAttribute("codeType", codeType);
        logger.info("识别代码类型: {}", codeType);

        // 识别设计模式
        String pattern = identifyDesignPattern(requirement);
        if (pattern != null) {
            context.setAttribute("designPattern", pattern);
            logger.info("识别设计模式: {}", pattern);
        }

        // 识别复杂度
        String complexity = identifyComplexity(requirement);
        context.setAttribute("complexity", complexity);
        logger.info("需求复杂度: {}", complexity);

        logger.info("需求解析完成");
    }

    /**
     * 识别代码类型
     */
    private String identifyCodeType(String requirement) {
        String lower = requirement.toLowerCase();

        if (lower.contains("接口") || lower.contains("interface")) {
            return "interface";
        } else if (lower.contains("枚举") || lower.contains("enum")) {
            return "enum";
        } else if (lower.contains("抽象类") || lower.contains("abstract")) {
            return "abstract_class";
        } else if (lower.contains("注解") || lower.contains("annotation")) {
            return "annotation";
        }

        return "class";
    }

    /**
     * 识别设计模式
     */
    private String identifyDesignPattern(String requirement) {
        String lower = requirement.toLowerCase();

        if (lower.contains("单例") || lower.contains("singleton")) {
            return "singleton";
        } else if (lower.contains("工厂") || lower.contains("factory")) {
            return "factory";
        } else if (lower.contains("建造者") || lower.contains("builder")) {
            return "builder";
        } else if (lower.contains("观察者") || lower.contains("observer")) {
            return "observer";
        } else if (lower.contains("策略") || lower.contains("strategy")) {
            return "strategy";
        } else if (lower.contains("装饰者") || lower.contains("decorator")) {
            return "decorator";
        } else if (lower.contains("适配器") || lower.contains("adapter")) {
            return "adapter";
        }

        return null;
    }

    /**
     * 识别需求复杂度
     */
    private String identifyComplexity(String requirement) {
        int length = requirement.length();

        // 简单规则：根据描述长度和关键词数量判断
        if (length < 50) {
            return "simple";
        } else if (length < 150) {
            return "medium";
        } else {
            return "complex";
        }
    }

    @Override
    public String getStepName() {
        return "RequirementParse";
    }
}
