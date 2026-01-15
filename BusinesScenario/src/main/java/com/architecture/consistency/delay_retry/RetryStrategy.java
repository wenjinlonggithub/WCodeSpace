package com.architecture.consistency.delay_retry;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 重试策略
 * 实现指数退避算法
 */
@Slf4j
public class RetryStrategy {

    /**
     * 初始延迟时间（秒）
     */
    private final int initialDelaySeconds;

    /**
     * 最大延迟时间（秒）
     */
    private final int maxDelaySeconds;

    /**
     * 退避因子
     */
    private final double backoffMultiplier;

    /**
     * 最大重试次数
     */
    private final int maxRetryCount;

    public RetryStrategy(int initialDelaySeconds, int maxDelaySeconds,
                        double backoffMultiplier, int maxRetryCount) {
        this.initialDelaySeconds = initialDelaySeconds;
        this.maxDelaySeconds = maxDelaySeconds;
        this.backoffMultiplier = backoffMultiplier;
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 默认策略：1s, 3s, 10s, 30s, 60s
     */
    public static RetryStrategy defaultStrategy() {
        return new RetryStrategy(1, 60, 3.0, 5);
    }

    /**
     * 激进策略：100ms, 500ms, 1s, 2s, 5s
     */
    public static RetryStrategy aggressiveStrategy() {
        return new RetryStrategy(0, 5, 2.0, 5);
    }

    /**
     * 保守策略：5s, 15s, 45s, 135s, 300s
     */
    public static RetryStrategy conservativeStrategy() {
        return new RetryStrategy(5, 300, 3.0, 5);
    }

    /**
     * 计算下次重试时间
     *
     * @param currentRetryCount 当前重试次数
     * @return 下次重试时间
     */
    public LocalDateTime calculateNextRetryTime(int currentRetryCount) {
        if (currentRetryCount >= maxRetryCount) {
            log.warn("已达到最大重试次数: {}", maxRetryCount);
            return null;
        }

        // 指数退避算法: delay = min(initialDelay * (backoffMultiplier ^ retryCount), maxDelay)
        long delaySeconds = (long) Math.min(
            initialDelaySeconds * Math.pow(backoffMultiplier, currentRetryCount),
            maxDelaySeconds
        );

        LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(delaySeconds);
        log.info("计算重试时间: retryCount={}, delay={}s, nextRetryTime={}",
                currentRetryCount, delaySeconds, nextRetryTime);

        return nextRetryTime;
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry(int currentRetryCount) {
        return currentRetryCount < maxRetryCount;
    }

    /**
     * 获取最大重试次数
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 打印重试计划
     */
    public void printRetryPlan() {
        log.info("重试策略配置:");
        log.info("  初始延迟: {}s", initialDelaySeconds);
        log.info("  最大延迟: {}s", maxDelaySeconds);
        log.info("  退避因子: {}", backoffMultiplier);
        log.info("  最大重试次数: {}", maxRetryCount);
        log.info("重试计划:");
        for (int i = 0; i < maxRetryCount; i++) {
            long delaySeconds = (long) Math.min(
                initialDelaySeconds * Math.pow(backoffMultiplier, i),
                maxDelaySeconds
            );
            log.info("  第{}次重试: 延迟{}秒", i + 1, delaySeconds);
        }
    }
}
