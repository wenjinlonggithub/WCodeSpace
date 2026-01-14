package com.architecture.aicoding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Token计数服务
 * 用于估算token数量和计算API调用成本
 */
public class TokenCounterService {
    private static final Logger logger = LoggerFactory.getLogger(TokenCounterService.class);

    // GPT-4 价格（2026年1月参考价，每1000 tokens）
    private static final double GPT4_INPUT_PRICE = 0.03;   // $0.03 per 1K tokens
    private static final double GPT4_OUTPUT_PRICE = 0.06;  // $0.06 per 1K tokens

    // GPT-3.5-turbo 价格（2026年1月参考价，每1000 tokens）
    private static final double GPT35_INPUT_PRICE = 0.0005;   // $0.0005 per 1K tokens
    private static final double GPT35_OUTPUT_PRICE = 0.0015;  // $0.0015 per 1K tokens

    /**
     * 估算文本的token数量
     * 注意：这是简单估算，实际应使用tiktoken等专业库
     *
     * @param text 文本内容
     * @return 估算的token数
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // 简单估算规则：
        // - 英文：约4个字符 = 1 token
        // - 中文：约1.5个字符 = 1 token
        // - 代码：约3个字符 = 1 token（代码通常更密集）

        int englishChars = countEnglishChars(text);
        int chineseChars = countChineseChars(text);
        int totalChars = text.length();
        int otherChars = totalChars - englishChars - chineseChars;

        // 估算token数
        int tokens = (int) (
            englishChars / 4.0 +
            chineseChars / 1.5 +
            otherChars / 3.5
        );

        logger.debug("Token估算 - 总字符: {}, 英文: {}, 中文: {}, 估算Token: {}",
            totalChars, englishChars, chineseChars, tokens);

        return Math.max(tokens, 1); // 至少1个token
    }

    /**
     * 计算API调用成本（美元）
     *
     * @param promptTokens 输入token数
     * @param completionTokens 输出token数
     * @param model 模型名称
     * @return 成本（美元）
     */
    public double calculateCost(int promptTokens, int completionTokens, String model) {
        double inputCost;
        double outputCost;

        if (model == null) {
            model = "gpt-4";
        }

        // 根据模型选择价格
        if (model.toLowerCase().contains("gpt-4")) {
            inputCost = (promptTokens / 1000.0) * GPT4_INPUT_PRICE;
            outputCost = (completionTokens / 1000.0) * GPT4_OUTPUT_PRICE;
        } else if (model.toLowerCase().contains("gpt-3.5")) {
            inputCost = (promptTokens / 1000.0) * GPT35_INPUT_PRICE;
            outputCost = (completionTokens / 1000.0) * GPT35_OUTPUT_PRICE;
        } else {
            // 默认使用GPT-4价格
            inputCost = (promptTokens / 1000.0) * GPT4_INPUT_PRICE;
            outputCost = (completionTokens / 1000.0) * GPT4_OUTPUT_PRICE;
        }

        double totalCost = inputCost + outputCost;

        logger.debug("成本计算 - 模型: {}, 输入: {} tokens (${:.4f}), 输出: {} tokens (${:.4f}), 总计: ${:.4f}",
            model, promptTokens, inputCost, completionTokens, outputCost, totalCost);

        return totalCost;
    }

    /**
     * 统计英文字符数
     */
    private int countEnglishChars(String text) {
        return text.replaceAll("[^a-zA-Z0-9\\s]", "").length();
    }

    /**
     * 统计中文字符数
     */
    private int countChineseChars(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FA5) { // 中文Unicode范围
                count++;
            }
        }
        return count;
    }

    /**
     * 格式化成本显示
     */
    public String formatCost(double cost) {
        return String.format("$%.4f", cost);
    }
}
