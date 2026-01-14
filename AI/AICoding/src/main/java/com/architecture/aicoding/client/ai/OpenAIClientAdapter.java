package com.architecture.aicoding.client.ai;

import com.architecture.aicoding.client.OpenAIClient;
import com.architecture.aicoding.client.OpenAIClientImpl;
import com.architecture.aicoding.client.request.ChatCompletionRequest;
import com.architecture.aicoding.client.request.Message;
import com.architecture.aicoding.client.response.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI客户端适配器
 * 将OpenAI特定的API适配到通用AIClient接口
 */
public class OpenAIClientAdapter implements AIClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIClientAdapter.class);

    private final OpenAIClient openAIClient;

    public OpenAIClientAdapter() {
        this.openAIClient = new OpenAIClientImpl();
        logger.info("OpenAI客户端适配器初始化完成");
    }

    public OpenAIClientAdapter(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @Override
    public AIResponse generate(AIRequest request) {
        logger.info("使用OpenAI生成代码 - 模型: {}", request.getModel());

        // 转换为OpenAI请求格式
        ChatCompletionRequest openAIRequest = convertToOpenAIRequest(request);

        // 调用OpenAI API
        ChatCompletionResponse openAIResponse = openAIClient.chatCompletion(openAIRequest);

        // 转换为通用响应格式
        return convertToAIResponse(openAIResponse);
    }

    @Override
    public boolean isAvailable() {
        return openAIClient.checkConnection();
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    @Override
    public String[] getSupportedModels() {
        return new String[]{
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4-turbo-preview",
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-16k"
        };
    }

    /**
     * 转换为OpenAI请求格式
     */
    private ChatCompletionRequest convertToOpenAIRequest(AIRequest request) {
        ChatCompletionRequest openAIRequest = new ChatCompletionRequest();

        // 设置模型
        openAIRequest.setModel(request.getModel() != null ? request.getModel() : "gpt-4");

        // 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 添加系统消息
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(new Message("system", request.getSystemPrompt()));
        }

        // 添加历史消息
        if (request.getMessageHistory() != null) {
            for (AIRequest.Message msg : request.getMessageHistory()) {
                messages.add(new Message(msg.getRole(), msg.getContent()));
            }
        }

        // 添加用户消息
        if (request.getUserMessage() != null && !request.getUserMessage().isEmpty()) {
            messages.add(new Message("user", request.getUserMessage()));
        }

        openAIRequest.setMessages(messages);

        // 设置参数
        openAIRequest.setTemperature(request.getTemperature());
        openAIRequest.setMaxTokens(request.getMaxTokens());
        openAIRequest.setTopP(request.getTopP());
        openAIRequest.setStop(request.getStopSequences());

        return openAIRequest;
    }

    /**
     * 转换为通用响应格式
     */
    private AIResponse convertToAIResponse(ChatCompletionResponse openAIResponse) {
        AIResponse.AIResponseBuilder builder = AIResponse.builder()
            .provider("OpenAI")
            .model(openAIResponse.getModel());

        // 提取内容
        if (openAIResponse.getChoices() != null && !openAIResponse.getChoices().isEmpty()) {
            String content = openAIResponse.getChoices().get(0).getMessage().getContent();
            String finishReason = openAIResponse.getChoices().get(0).getFinishReason();

            builder.content(content);
            builder.finishReason(finishReason);
        }

        // 提取token使用情况
        if (openAIResponse.getUsage() != null) {
            AIResponse.TokenUsage tokenUsage = AIResponse.TokenUsage.builder()
                .inputTokens(openAIResponse.getUsage().getPromptTokens())
                .outputTokens(openAIResponse.getUsage().getCompletionTokens())
                .totalTokens(openAIResponse.getUsage().getTotalTokens())
                .build();

            builder.tokenUsage(tokenUsage);
        }

        return builder.build();
    }
}
