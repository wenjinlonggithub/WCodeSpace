package com.architecture.aicoding;

import com.architecture.aicoding.model.*;
import com.architecture.aicoding.service.CodeGenerationService;
import com.architecture.aicoding.service.CodeGenerationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI代码生成工作流主应用
 * 演示各种代码生成场景和使用方式
 */
public class AICodingApplication {
    private static final Logger logger = LoggerFactory.getLogger(AICodingApplication.class);

    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("AI代码生成工作流系统 v1.0");
        logger.info("基于 OpenAI GPT-4/GPT-3.5 的智能代码生成工具");
        logger.info("=".repeat(80));

        // 创建服务实例
        CodeGenerationService service = new CodeGenerationServiceImpl();

        // 运行示例（根据需要注释/取消注释）
        try {
            // 示例1: 生成单例模式类（自动选择AI提供商）
            example1_SingletonPattern(service);

            // 示例2: 生成Builder模式类
            // example2_BuilderPattern(service);

            // 示例3: 生成数据访问层接口
            // example3_DAOInterface(service);

            // 示例4: 使用Few-shot策略生成实体类
            // example4_FewShotEntity(service);

            // 示例5: 使用Chain-of-Thought策略生成复杂类
            // example5_CoTStrategy(service);

            // 示例6: 使用Claude生成代码（需要配置CLAUDE_API_KEY）
            // example6_UsingClaude(service);

            // 示例7: 比较OpenAI和Claude的代码生成效果
            // example7_CompareProviders(service);

        } catch (Exception e) {
            logger.error("执行示例时发生错误", e);
        }

        logger.info("\n" + "=".repeat(80));
        logger.info("所有示例执行完毕");
        logger.info("=".repeat(80));
    }

    /**
     * 示例1: 生成单例模式类
     */
    private static void example1_SingletonPattern(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例1: 生成单例模式类 (Zero-shot + GPT-4)");
        logger.info("=".repeat(80));

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个线程安全的单例模式类DatabaseConnection，用于管理数据库连接池")
            .promptStrategy("zero-shot")
            .model("gpt-4")
            .validateCode(true)
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.db")
                .javaVersion("17")
                .frameworks("使用HikariCP连接池")
                .build())
            .codeStyle(CodeStyle.builder()
                .namingConvention("CamelCase")
                .commentStyle("JavaDoc")
                .useLombok(false)
                .build())
            .build();

        executeAndPrint(service, request, "SingletonPattern");
    }

    /**
     * 示例2: 生成Builder模式类
     */
    private static void example2_BuilderPattern(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例2: 生成Builder模式类 (Zero-shot + GPT-3.5)");
        logger.info("=".repeat(80));

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个User实体类使用Builder模式，包含id, username, email, phone, address字段")
            .promptStrategy("zero-shot")
            .model("gpt-3.5-turbo")
            .validateCode(true)
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.entity")
                .javaVersion("17")
                .build())
            .codeStyle(CodeStyle.builder()
                .useLombok(true)
                .build())
            .build();

        executeAndPrint(service, request, "BuilderPattern");
    }

    /**
     * 示例3: 生成数据访问层接口
     */
    private static void example3_DAOInterface(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例3: 生成数据访问层接口 (Zero-shot + GPT-4)");
        logger.info("=".repeat(80));

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个UserRepository接口，包含CRUD方法：保存、根据ID查询、查询全部、更新、删除")
            .promptStrategy("zero-shot")
            .model("gpt-4")
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.repository")
                .javaVersion("17")
                .frameworks("Spring Data JPA")
                .build())
            .build();

        executeAndPrint(service, request, "DAOInterface");
    }

    /**
     * 示例4: 使用Few-shot策略生成实体类
     */
    private static void example4_FewShotEntity(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例4: 使用Few-shot策略生成实体类 (Few-shot + GPT-3.5)");
        logger.info("=".repeat(80));

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个Product商品实体类，包含id, name, price, stock, description字段")
            .promptStrategy("few-shot")
            .model("gpt-3.5-turbo")
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.entity")
                .javaVersion("17")
                .build())
            .codeStyle(CodeStyle.builder()
                .useLombok(true)
                .commentStyle("JavaDoc")
                .build())
            .build();

        executeAndPrint(service, request, "FewShotEntity");
    }

    /**
     * 示例5: 使用Chain-of-Thought策略生成复杂类
     */
    private static void example5_CoTStrategy(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例5: 使用Chain-of-Thought策略生成复杂类 (CoT + GPT-4)");
        logger.info("=".repeat(80));

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个OrderService服务类，实现订单创建、订单查询、订单取消功能，需要注入OrderRepository和PaymentService")
            .promptStrategy("chain-of-thought")
            .model("gpt-4")
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.service")
                .javaVersion("17")
                .frameworks("Spring Boot")
                .build())
            .codeStyle(CodeStyle.builder()
                .useLombok(false)
                .commentStyle("JavaDoc")
                .build())
            .build();

        executeAndPrint(service, request, "CoTStrategy");
    }

    /**
     * 示例6: 使用Claude生成代码
     */
    private static void example6_UsingClaude(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例6: 使用Claude生成代码 (Claude Sonnet 3.5)");
        logger.info("=".repeat(80));

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个简单的缓存管理器CacheManager，使用单例模式，支持get、put、remove操作")
            .promptStrategy("zero-shot")
            .model("claude-3-5-sonnet-20241022")  // 使用Claude最新模型
            .validateCode(true)
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.cache")
                .javaVersion("17")
                .build())
            .build();

        executeAndPrint(service, request, "UsingClaude");
    }

    /**
     * 示例7: 比较OpenAI和Claude的代码生成效果
     */
    private static void example7_CompareProviders(CodeGenerationService service) {
        logger.info("\n" + "=".repeat(80));
        logger.info("示例7: 比较OpenAI和Claude的代码生成效果");
        logger.info("=".repeat(80));

        String requirement = "创建一个简单的日志工具类Logger，支持info、warn、error三种日志级别";

        // 使用OpenAI
        logger.info("\n--- 使用 OpenAI GPT-4 ---");
        CodeGenerationRequest openaiRequest = CodeGenerationRequest.builder()
            .requirement(requirement)
            .promptStrategy("zero-shot")
            .model("gpt-4")
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.util")
                .build())
            .build();
        executeAndPrint(service, openaiRequest, "OpenAI-GPT4");

        // 使用Claude
        logger.info("\n--- 使用 Claude Sonnet 3.5 ---");
        CodeGenerationRequest claudeRequest = CodeGenerationRequest.builder()
            .requirement(requirement)
            .promptStrategy("zero-shot")
            .model("claude-3-5-sonnet-20241022")
            .projectContext(ProjectContext.builder()
                .packageName("com.architecture.aicoding.example.util")
                .build())
            .build();
        executeAndPrint(service, claudeRequest, "Claude-Sonnet");
    }

    /**
     * 执行代码生成并打印结果
     */
    private static void executeAndPrint(CodeGenerationService service,
                                       CodeGenerationRequest request,
                                       String scenarioName) {
        try {
            // 执行代码生成
            CodeGenerationResponse response = service.generateCode(request);

            // 打印结果
            if (response.isSuccess()) {
                logger.info("\n✓ 代码生成成功！\n");

                // 打印生成的代码
                logger.info("生成的代码:");
                logger.info("-".repeat(80));
                System.out.println(response.getGeneratedCode().getCode());
                logger.info("-".repeat(80));

                // 打印元数据
                logger.info("\n代码信息:");
                logger.info("  - 类名: {}", response.getGeneratedCode().getClassName());
                logger.info("  - 包名: {}", response.getGeneratedCode().getPackageName());
                logger.info("  - 代码类型: {}", response.getGeneratedCode().getCodeType());
                logger.info("  - 行数: {}", response.getGeneratedCode().getLineCount());
                logger.info("  - 验证状态: {}", response.getGeneratedCode().isValid() ? "通过" : "失败");

                // 打印性能和成本信息
                logger.info("\n性能和成本:");
                if (response.getMetadata().containsKey("aiProvider")) {
                    logger.info("  - AI提供商: {}", response.getMetadata("aiProvider"));
                    logger.info("  - 使用模型: {}", response.getMetadata("aiModel"));
                }
                if (response.getMetadata().containsKey("totalTokens")) {
                    logger.info("  - Input Tokens: {}", response.getMetadata("promptTokens"));
                    logger.info("  - Output Tokens: {}", response.getMetadata("completionTokens"));
                    logger.info("  - Total Tokens: {}", response.getMetadata("totalTokens"));
                }
                if (response.getMetadata().containsKey("estimatedCostUSD")) {
                    logger.info("  - 估算成本: {}", response.getMetadata("estimatedCostUSD"));
                }
                logger.info("  - 生成耗时: {} ms", response.getDurationMs());

            } else {
                logger.error("\n✗ 代码生成失败！");
                logger.error("错误: {}", response.getError());
            }

        } catch (Exception e) {
            logger.error("执行示例 {} 时发生异常", scenarioName, e);
        }
    }
}
