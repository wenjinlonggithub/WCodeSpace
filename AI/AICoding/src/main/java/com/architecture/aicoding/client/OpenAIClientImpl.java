package com.architecture.aicoding.client;

import com.architecture.aicoding.client.request.ChatCompletionRequest;
import com.architecture.aicoding.client.response.ChatCompletionResponse;
import com.architecture.aicoding.config.OpenAIConfig;
import com.architecture.aicoding.exception.OpenAIException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * OpenAI API客户端实现
 * 使用OkHttp进行HTTP通信，Jackson进行JSON序列化
 */
public class OpenAIClientImpl implements OpenAIClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIClientImpl.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenAIConfig config;

    /**
     * 构造函数，初始化HTTP客户端和JSON映射器
     */
    public OpenAIClientImpl() {
        this.config = OpenAIConfig.getInstance();
        this.objectMapper = createObjectMapper();
        this.httpClient = createHttpClient();

        logger.info("OpenAI客户端初始化完成 - BaseURL: {}", config.getApiBaseUrl());
    }

    /**
     * 创建HTTP客户端
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(config.getTimeout()))
            .readTimeout(Duration.ofSeconds(config.getTimeout()))
            .writeTimeout(Duration.ofSeconds(config.getTimeout()))
            .addInterceptor(chain -> {
                Request original = chain.request();
                logger.debug("发送请求: {} {}", original.method(), original.url());

                Response response = chain.proceed(original);
                logger.debug("收到响应: {} (状态码: {})", original.url(), response.code());

                return response;
            })
            .build();
    }

    /**
     * 创建JSON映射器
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Override
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        if (!config.isValid()) {
            throw new OpenAIException("OpenAI API Key 未配置或无效，请设置环境变量 OPENAI_API_KEY");
        }

        try {
            // 序列化请求
            String jsonBody = objectMapper.writeValueAsString(request);
            logger.debug("请求体: {}", jsonBody);

            // 构建HTTP请求
            Request httpRequest = new Request.Builder()
                .url(config.getApiBaseUrl() + "/v1/chat/completions")
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON))
                .build();

            // 发送请求
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    logger.error("API调用失败 - 状态码: {}, 响应: {}", response.code(), responseBody);
                    throw new OpenAIException(
                        String.format("API调用失败 (状态码: %d): %s", response.code(), responseBody)
                    );
                }

                logger.debug("响应体: {}", responseBody);

                // 解析响应
                ChatCompletionResponse chatResponse = objectMapper.readValue(
                    responseBody,
                    ChatCompletionResponse.class
                );

                logger.info("API调用成功 - Model: {}, Tokens: {}",
                    chatResponse.getModel(),
                    chatResponse.getUsage() != null ? chatResponse.getUsage().getTotalTokens() : "N/A"
                );

                return chatResponse;
            }

        } catch (IOException e) {
            logger.error("OpenAI API调用异常", e);
            throw new OpenAIException("API调用异常: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkConnection() {
        try {
            Request request = new Request.Builder()
                .url(config.getApiBaseUrl() + "/v1/models")
                .header("Authorization", "Bearer " + config.getApiKey())
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                if (success) {
                    logger.info("OpenAI API连接正常");
                } else {
                    logger.warn("OpenAI API连接失败 - 状态码: {}", response.code());
                }
                return success;
            }

        } catch (IOException e) {
            logger.error("检查连接时发生异常", e);
            return false;
        }
    }
}
