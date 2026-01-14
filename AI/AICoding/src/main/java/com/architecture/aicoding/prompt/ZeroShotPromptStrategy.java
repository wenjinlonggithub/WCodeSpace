package com.architecture.aicoding.prompt;

import com.architecture.aicoding.model.CodeGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zero-shot Prompt策略
 * 直接提供任务描述，不提供示例
 * 适用于简单、明确的代码生成任务
 */
public class ZeroShotPromptStrategy implements PromptStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ZeroShotPromptStrategy.class);

    @Override
    public String buildPrompt(CodeGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        // 系统角色定义
        prompt.append("你是一个专业的Java开发工程师，擅长编写高质量、符合最佳实践的代码。\n\n");

        // 任务描述
        prompt.append("## 任务\n");
        prompt.append(request.getRequirement()).append("\n\n");

        // 项目上下文
        if (request.getProjectContext() != null) {
            prompt.append("## 项目上下文\n");
            if (request.getProjectContext().getPackageName() != null) {
                prompt.append("- 包名: ").append(request.getProjectContext().getPackageName()).append("\n");
            }
            if (request.getProjectContext().getJavaVersion() != null) {
                prompt.append("- Java版本: ").append(request.getProjectContext().getJavaVersion()).append("\n");
            }
            if (request.getProjectContext().getFrameworks() != null) {
                prompt.append("- 使用框架: ").append(request.getProjectContext().getFrameworks()).append("\n");
            }
            prompt.append("\n");
        }

        // 代码风格要求
        if (request.getCodeStyle() != null) {
            prompt.append("## 代码风格要求\n");
            if (request.getCodeStyle().getNamingConvention() != null) {
                prompt.append("- 命名规范: ").append(request.getCodeStyle().getNamingConvention()).append("\n");
            }
            if (request.getCodeStyle().getCommentStyle() != null) {
                prompt.append("- 注释风格: ").append(request.getCodeStyle().getCommentStyle()).append("\n");
            }
            if (request.getCodeStyle().isUseLombok()) {
                prompt.append("- 使用Lombok注解简化代码\n");
            }
            prompt.append("\n");
        }

        // 输出格式要求
        prompt.append("## 输出要求\n");
        prompt.append("请直接输出完整的Java代码，使用Markdown代码块格式：\n");
        prompt.append("```java\n");
        prompt.append("// 你的代码\n");
        prompt.append("```\n");
        prompt.append("不需要额外的解释说明。");

        logger.debug("构建的Zero-shot Prompt:\n{}", prompt);
        return prompt.toString();
    }

    @Override
    public String getStrategyName() {
        return "Zero-Shot";
    }

    @Override
    public String getRecommendedModel() {
        return "gpt-4";
    }
}
