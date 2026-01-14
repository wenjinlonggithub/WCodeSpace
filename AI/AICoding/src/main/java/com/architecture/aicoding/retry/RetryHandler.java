package com.architecture.aicoding.retry;

import com.architecture.aicoding.exception.OpenAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 重试处理器
 * 实现指数退避重试策略，用于处理临时性错误（网络抖动、API限流等）
 */
public class RetryHandler {
    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);

    private final int maxRetries;
    private final long initialDelayMs;
    private final double backoffMultiplier;

    /**
     * 使用默认参数构造
     * 默认: 最多重试3次，初始延迟1秒，指数倍数2.0
     */
    public RetryHandler() {
        this(3, 1000, 2.0);
    }

    /**
     * 自定义参数构造
     * @param maxRetries 最大重试次数
     * @param initialDelayMs 初始延迟（毫秒）
     * @param backoffMultiplier 退避倍数
     */
    public RetryHandler(int maxRetries, long initialDelayMs, double backoffMultiplier) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    /**
     * 执行带重试的操作
     * @param callable 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     * @throws OpenAIException 当所有重试都失败时抛出
     */
    public <T> T executeWithRetry(Callable<T> callable) {
        int attempt = 0;
        long delay = initialDelayMs;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                logger.debug("执行第 {} 次尝试", attempt);
                return callable.call();

            } catch (Exception e) {
                lastException = e;
                logger.warn("第 {} 次尝试失败: {}", attempt, e.getMessage());

                if (attempt >= maxRetries) {
                    logger.error("已达到最大重试次数 {}，操作失败", maxRetries);
                    break;
                }

                // 判断是否需要重试（某些错误不应重试）
                if (!shouldRetry(e)) {
                    logger.error("遇到不可重试的错误，停止重试: {}", e.getMessage());
                    break;
                }

                // 等待后再重试
                try {
                    logger.info("等待 {}ms 后重试...", delay);
                    Thread.sleep(delay);
                    delay = (long) (delay * backoffMultiplier);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new OpenAIException("重试被中断", ie);
                }
            }
        }

        // 所有尝试都失败
        throw new OpenAIException(
            String.format("操作失败，已重试 %d 次", maxRetries),
            lastException
        );
    }

    /**
     * 判断异常是否应该重试
     * @param e 异常
     * @return true 如果应该重试
     */
    private boolean shouldRetry(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return true;
        }

        // 某些错误不应重试
        // - 401/403: 认证/授权错误
        // - 400: 请求参数错误
        if (message.contains("401") || message.contains("403") ||
            message.contains("Unauthorized") || message.contains("Forbidden")) {
            return false;
        }

        if (message.contains("400") || message.contains("Bad Request")) {
            return false;
        }

        // 以下错误应该重试
        // - 429: 速率限制
        // - 500/502/503/504: 服务器错误
        // - 网络错误
        return message.contains("429") ||
               message.contains("500") ||
               message.contains("502") ||
               message.contains("503") ||
               message.contains("504") ||
               message.contains("timeout") ||
               message.contains("connect") ||
               e instanceof java.io.IOException;
    }

    /**
     * 执行带重试的操作（Runnable版本，无返回值）
     * @param runnable 要执行的操作
     */
    public void executeWithRetry(Runnable runnable) {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        });
    }
}
