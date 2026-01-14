package com.architecture.aicoding.prompt;

import com.architecture.aicoding.model.CodeGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chain-of-Thought Prompt策略
 * 引导AI逐步思考，分步骤完成复杂任务
 * 适用于复杂的架构设计和代码生成
 */
public class ChainOfThoughtPromptStrategy implements PromptStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ChainOfThoughtPromptStrategy.class);

    @Override
    public String buildPrompt(CodeGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的Java架构师。请按照以下步骤思考并生成代码:\n\n");

        // 步骤1: 需求分析
        prompt.append("## 步骤1 - 需求分析\n");
        prompt.append("需求: ").append(request.getRequirement()).append("\n\n");
        prompt.append("请分析:\n");
        prompt.append("- 需要创建哪些类或接口?\n");
        prompt.append("- 涉及哪些设计模式?\n");
        prompt.append("- 有哪些关键的方法和属性?\n\n");

        // 步骤2: 架构设计
        prompt.append("## 步骤2 - 架构设计\n");
        prompt.append("描述:\n");
        prompt.append("- 类之间的关系（继承、组合、依赖）\n");
        prompt.append("- 主要的交互流程\n");
        prompt.append("- 关键的设计决策\n\n");

        // 项目上下文
        if (request.getProjectContext() != null) {
            prompt.append("## 项目环境\n");
            if (request.getProjectContext().getPackageName() != null) {
                prompt.append("- 包名: ").append(request.getProjectContext().getPackageName()).append("\n");
            }
            if (request.getProjectContext().getJavaVersion() != null) {
                prompt.append("- Java版本: ").append(request.getProjectContext().getJavaVersion()).append("\n");
            }
            prompt.append("\n");
        }

        // 步骤3: 代码实现
        prompt.append("## 步骤3 - 代码实现\n");
        prompt.append("基于以上分析和设计，生成完整的Java代码。\n");
        prompt.append("使用Markdown代码块格式:\n");
        prompt.append("```java\n");
        prompt.append("// 你的代码\n");
        prompt.append("```\n\n");

        // 步骤4: 代码说明
        prompt.append("## 步骤4 - 关键说明\n");
        prompt.append("简要说明代码的关键设计点（1-2句话）。\n");

        logger.debug("构建的Chain-of-Thought Prompt:\n{}", prompt);
        return prompt.toString();
    }

    @Override
    public String getStrategyName() {
        return "Chain-of-Thought";
    }

    @Override
    public String getRecommendedModel() {
        return "gpt-4";
    }
}
