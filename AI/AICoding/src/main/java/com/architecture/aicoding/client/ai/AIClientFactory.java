package com.architecture.aicoding.client.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI客户端工厂
 * 根据配置创建不同的AI服务提供商客户端
 */
public class AIClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(AIClientFactory.class);

    /**
     * AI提供商枚举
     */
    public enum Provider {
        OPENAI("openai"),
        CLAUDE("claude"),
        AUTO("auto");  // 自动选择可用的提供商

        private final String name;

        Provider(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Provider fromString(String name) {
            if (name == null) {
                return AUTO;
            }

            for (Provider provider : Provider.values()) {
                if (provider.name.equalsIgnoreCase(name)) {
                    return provider;
                }
            }

            logger.warn("未知的AI提供商: {}, 使用AUTO模式", name);
            return AUTO;
        }
    }

    /**
     * 创建AI客户端
     * @param provider 提供商名称（openai, claude, auto）
     * @return AI客户端实例
     */
    public static AIClient createClient(String provider) {
        return createClient(Provider.fromString(provider));
    }

    /**
     * 创建AI客户端
     * @param provider 提供商枚举
     * @return AI客户端实例
     */
    public static AIClient createClient(Provider provider) {
        logger.info("创建AI客户端 - 提供商: {}", provider);

        switch (provider) {
            case OPENAI:
                return createOpenAIClient();

            case CLAUDE:
                return createClaudeClient();

            case AUTO:
                return createAutoClient();

            default:
                logger.warn("未知的提供商: {}, 使用OpenAI", provider);
                return createOpenAIClient();
        }
    }

    /**
     * 创建默认客户端（从环境变量读取配置）
     * @return AI客户端实例
     */
    public static AIClient createDefaultClient() {
        String provider = System.getenv("AI_PROVIDER");
        if (provider == null || provider.isEmpty()) {
            provider = System.getProperty("ai.provider", "auto");
        }

        logger.info("创建默认AI客户端 - 配置的提供商: {}", provider);
        return createClient(provider);
    }

    /**
     * 创建OpenAI客户端
     */
    private static AIClient createOpenAIClient() {
        logger.info("初始化OpenAI客户端");
        return new OpenAIClientAdapter();
    }

    /**
     * 创建Claude客户端
     */
    private static AIClient createClaudeClient() {
        logger.info("初始化Claude客户端");
        return new ClaudeClientAdapter();
    }

    /**
     * 自动选择可用的客户端
     * 优先级: Claude > OpenAI
     */
    private static AIClient createAutoClient() {
        logger.info("自动选择AI提供商...");

        // 1. 检查是否配置了Claude API Key
        String claudeKey = System.getenv("CLAUDE_API_KEY");
        if (claudeKey == null || claudeKey.isEmpty()) {
            claudeKey = System.getenv("ANTHROPIC_API_KEY");
        }

        if (claudeKey != null && !claudeKey.isEmpty() && !claudeKey.equals("your-api-key-here")) {
            logger.info("检测到Claude API Key，使用Claude");
            AIClient claudeClient = createClaudeClient();
            if (claudeClient.isAvailable()) {
                return claudeClient;
            }
        }

        // 2. 检查是否配置了OpenAI API Key
        String openaiKey = System.getenv("OPENAI_API_KEY");
        if (openaiKey != null && !openaiKey.isEmpty() && !openaiKey.equals("your-api-key-here")) {
            logger.info("检测到OpenAI API Key，使用OpenAI");
            AIClient openAIClient = createOpenAIClient();
            if (openAIClient.isAvailable()) {
                return openAIClient;
            }
        }

        // 3. 如果都没有配置，默认使用OpenAI（但可能会失败）
        logger.warn("未检测到任何AI提供商的API Key，使用OpenAI作为默认选项");
        return createOpenAIClient();
    }

    /**
     * 检查提供商是否可用
     * @param provider 提供商
     * @return 是否可用
     */
    public static boolean isProviderAvailable(Provider provider) {
        try {
            AIClient client = createClient(provider);
            return client.isAvailable();
        } catch (Exception e) {
            logger.warn("检查提供商 {} 可用性时发生错误: {}", provider, e.getMessage());
            return false;
        }
    }

    /**
     * 获取推荐的模型
     * @param provider 提供商
     * @param complexity 复杂度 (simple, medium, complex)
     * @return 推荐的模型名称
     */
    public static String getRecommendedModel(Provider provider, String complexity) {
        switch (provider) {
            case OPENAI:
                return complexity.equals("simple") ? "gpt-3.5-turbo" : "gpt-4";

            case CLAUDE:
                switch (complexity) {
                    case "simple":
                        return "claude-3-haiku-20240307";
                    case "complex":
                        return "claude-3-5-sonnet-20241022";
                    default:
                        return "claude-3-sonnet-20240229";
                }

            default:
                return "gpt-4";
        }
    }
}
