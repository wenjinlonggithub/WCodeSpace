package com.architecture.aicoding.client.ai;

import com.architecture.aicoding.config.OpenAIConfig;
import com.architecture.aicoding.exception.OpenAIException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claude (Anthropic) 客户端适配器
 * 使用Anthropic Messages API
 */
public class ClaudeClientAdapter implements AIClient {
    private static final Logger logger = LoggerFactory.getLogger(ClaudeClientAdapter.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String CLAUDE_API_VERSION = "2023-06-01";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiBaseUrl;

    public ClaudeClientAdapter() {
        this.apiKey = getClaudeApiKey();
        this.apiBaseUrl = getClaudeApiBaseUrl();
        this.objectMapper = createObjectMapper();
        this.httpClient = createHttpClient();

        logger.info("Claude客户端适配器初始化完成 - BaseURL: {}", apiBaseUrl);
    }

    /**
     * 创建HTTP客户端
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))  // Claude可能需要更长的超时
            .writeTimeout(Duration.ofSeconds(30))
            .build();
    }

    /**
     * 创建JSON映射器
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    @Override
    public AIResponse generate(AIRequest request) {
        logger.info("使用Claude生成代码 - 模型: {}", request.getModel());

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            throw new OpenAIException("Claude API Key未配置，请设置环境变量 CLAUDE_API_KEY");
        }

        try {
            // 构建Claude请求
            Map<String, Object> claudeRequest = buildClaudeRequest(request);

            String jsonBody = objectMapper.writeValueAsString(claudeRequest);
            logger.debug("Claude请求体: {}", jsonBody);

            // 构建HTTP请求
            Request httpRequest = new Request.Builder()
                .url(apiBaseUrl + "/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", CLAUDE_API_VERSION)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON))
                .build();

            // 发送请求
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    logger.error("Claude API调用失败 - 状态码: {}, 响应: {}", response.code(), responseBody);
                    throw new OpenAIException(
                        String.format("Claude API调用失败 (状态码: %d): %s", response.code(), responseBody)
                    );
                }

                logger.debug("Claude响应体: {}", responseBody);

                // 解析响应
                return parseClaudeResponse(responseBody, request.getModel());
            }

        } catch (IOException e) {
            logger.error("Claude API调用异常", e);
            throw new OpenAIException("Claude API调用异常: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            logger.warn("Claude API Key未配置");
            return false;
        }

        try {
            // 发送一个简单的测试请求
            AIRequest testRequest = AIRequest.builder()
                .model("claude-3-5-sonnet-20241022")
                .systemPrompt("You are a helpful assistant.")
                .userMessage("Hello")
                .maxTokens(10)
                .build();

            generate(testRequest);
            logger.info("Claude API连接正常");
            return true;

        } catch (Exception e) {
            logger.warn("Claude API连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Claude (Anthropic)";
    }

    @Override
    public String[] getSupportedModels() {
        return new String[]{
            "claude-3-5-sonnet-20241022",    // 最新Sonnet 3.5
            "claude-3-opus-20240229",         // Opus 3
            "claude-3-sonnet-20240229",       // Sonnet 3
            "claude-3-haiku-20240307",        // Haiku 3
            "claude-2.1",
            "claude-2.0"
        };
    }

    /**
     * 构建Claude请求
     */
    private Map<String, Object> buildClaudeRequest(AIRequest request) {
        Map<String, Object> claudeRequest = new HashMap<>();

        // 设置模型（默认使用最新的Sonnet 3.5）
        String model = request.getModel() != null ? request.getModel() : "claude-3-5-sonnet-20241022";
        claudeRequest.put("model", model);

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加历史消息
        if (request.getMessageHistory() != null) {
            for (AIRequest.Message msg : request.getMessageHistory()) {
                if (!"system".equals(msg.getRole())) {  // Claude将system单独处理
                    Map<String, String> message = new HashMap<>();
                    message.put("role", msg.getRole());
                    message.put("content", msg.getContent());
                    messages.add(message);
                }
            }
        }

        // 添加用户消息
        if (request.getUserMessage() != null && !request.getUserMessage().isEmpty()) {
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", request.getUserMessage());
            messages.add(message);
        }

        claudeRequest.put("messages", messages);

        // 设置系统提示（Claude 3使用独立的system字段）
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            claudeRequest.put("system", request.getSystemPrompt());
        }

        // 设置参数
        claudeRequest.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 2000);
        claudeRequest.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);

        if (request.getTopP() != null) {
            claudeRequest.put("top_p", request.getTopP());
        }

        if (request.getStopSequences() != null && !request.getStopSequences().isEmpty()) {
            claudeRequest.put("stop_sequences", request.getStopSequences());
        }

        return claudeRequest;
    }

    /**
     * 解析Claude响应
     */
    private AIResponse parseClaudeResponse(String responseBody, String requestedModel) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

        AIResponse.AIResponseBuilder builder = AIResponse.builder()
            .provider("Claude")
            .model(requestedModel);

        // 提取内容
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content != null && !content.isEmpty()) {
            String text = (String) content.get(0).get("text");
            builder.content(text);
        }

        // 提取完成原因
        String stopReason = (String) response.get("stop_reason");
        builder.finishReason(stopReason);

        // 提取token使用情况
        @SuppressWarnings("unchecked")
        Map<String, Integer> usage = (Map<String, Integer>) response.get("usage");
        if (usage != null) {
            AIResponse.TokenUsage tokenUsage = AIResponse.TokenUsage.builder()
                .inputTokens(usage.get("input_tokens"))
                .outputTokens(usage.get("output_tokens"))
                .totalTokens(usage.get("input_tokens") + usage.get("output_tokens"))
                .build();

            builder.tokenUsage(tokenUsage);

            logger.info("Claude API调用成功 - Token使用: Input={}, Output={}, Total={}",
                tokenUsage.getInputTokens(),
                tokenUsage.getOutputTokens(),
                tokenUsage.getTotalTokens()
            );
        }

        return builder.build();
    }

    /**
     * 获取Claude API Key
     */
    private String getClaudeApiKey() {
        // 首先尝试从环境变量获取
        String key = System.getenv("CLAUDE_API_KEY");
        if (key != null && !key.isEmpty()) {
            return key;
        }

        // 尝试从ANTHROPIC_API_KEY环境变量获取
        key = System.getenv("ANTHROPIC_API_KEY");
        if (key != null && !key.isEmpty()) {
            return key;
        }

        logger.warn("Claude API Key未配置，请设置环境变量 CLAUDE_API_KEY 或 ANTHROPIC_API_KEY");
        return "";
    }

    /**
     * 获取Claude API Base URL
     */
    private String getClaudeApiBaseUrl() {
        String url = System.getenv("CLAUDE_API_BASE_URL");
        return url != null && !url.isEmpty() ? url : "https://api.anthropic.com";
    }
}
