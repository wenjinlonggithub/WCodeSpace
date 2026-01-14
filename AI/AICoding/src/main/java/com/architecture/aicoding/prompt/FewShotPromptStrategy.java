package com.architecture.aicoding.prompt;

import com.architecture.aicoding.model.CodeGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Few-shot Prompt策略
 * 提供1-3个示例来引导AI生成期望的代码
 * 适用于需要特定格式或风格的代码生成
 */
public class FewShotPromptStrategy implements PromptStrategy {
    private static final Logger logger = LoggerFactory.getLogger(FewShotPromptStrategy.class);

    @Override
    public String buildPrompt(CodeGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        // 系统角色
        prompt.append("你是一个专业的Java开发工程师。\n\n");

        // 提供示例
        prompt.append("## 示例\n\n");

        // 示例1: 简单POJO
        prompt.append("### 示例1\n");
        prompt.append("需求: 创建一个用户实体类User，包含id和name字段\n\n");
        prompt.append("代码:\n");
        prompt.append("```java\n");
        prompt.append("package com.example.entity;\n\n");
        prompt.append("import lombok.Data;\n\n");
        prompt.append("@Data\n");
        prompt.append("public class User {\n");
        prompt.append("    private Long id;\n");
        prompt.append("    private String name;\n");
        prompt.append("}\n");
        prompt.append("```\n\n");

        // 示例2: 接口定义
        prompt.append("### 示例2\n");
        prompt.append("需求: 创建一个用户服务接口UserService，包含根据ID查询用户的方法\n\n");
        prompt.append("代码:\n");
        prompt.append("```java\n");
        prompt.append("package com.example.service;\n\n");
        prompt.append("import com.example.entity.User;\n\n");
        prompt.append("public interface UserService {\n");
        prompt.append("    User findById(Long id);\n");
        prompt.append("}\n");
        prompt.append("```\n\n");

        // 实际任务
        prompt.append("## 现在请完成以下任务\n\n");
        prompt.append("需求: ").append(request.getRequirement()).append("\n\n");

        // 项目上下文
        if (request.getProjectContext() != null && request.getProjectContext().getPackageName() != null) {
            prompt.append("包名: ").append(request.getProjectContext().getPackageName()).append("\n\n");
        }

        prompt.append("请按照上面示例的格式和风格，生成完整的Java代码。使用Markdown代码块格式。");

        logger.debug("构建的Few-shot Prompt:\n{}", prompt);
        return prompt.toString();
    }

    @Override
    public String getStrategyName() {
        return "Few-Shot";
    }

    @Override
    public String getRecommendedModel() {
        return "gpt-3.5-turbo";
    }
}
