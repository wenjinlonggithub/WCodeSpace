package com.concurrency.aqs.enterprise;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 阿里巴巴 - Dubbo框架限流器实现
 *
 * 业务场景：
 * 在微服务架构中，服务提供者需要限制并发请求数，防止服务过载。
 * Dubbo框架使用Semaphore实现服务端限流保护。
 *
 * 技术方案：
 * - 使用Semaphore控制并发请求数
 * - 基于AQS的共享模式，动态调整许可数量
 * - 支持超时获取，避免请求长时间等待
 *
 * 业务价值：
 * - 防止服务过载，保障系统稳定性
 * - 提供优雅降级能力
 * - 支持动态调整限流阈值
 */
public class DubboRateLimiter {

    private final Semaphore semaphore;
    private final int maxConcurrent;
    private final long timeout;

    public DubboRateLimiter(int maxConcurrent, long timeout) {
        this.maxConcurrent = maxConcurrent;
        this.timeout = timeout;
        this.semaphore = new Semaphore(maxConcurrent);
    }

    /**
     * 执行RPC调用（带限流保护）
     */
    public <T> T invoke(RpcInvocation<T> invocation) throws Exception {
        // 尝试获取许可
        boolean acquired = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);

        if (!acquired) {
            // 限流触发，返回降级响应
            throw new RateLimitException("服务繁忙，请稍后重试");
        }

        try {
            // 执行实际的RPC调用
            long startTime = System.currentTimeMillis();
            T result = invocation.call();
            long duration = System.currentTimeMillis() - startTime;

            System.out.printf("[Dubbo限流器] 请求成功 - 耗时: %dms, 可用许可: %d/%d%n",
                    duration, semaphore.availablePermits(), maxConcurrent);

            return result;
        } finally {
            // 释放许可
            semaphore.release();
        }
    }

    /**
     * 动态调整限流阈值（运维能力）
     */
    public void adjustLimit(int newLimit) {
        int diff = newLimit - maxConcurrent;
        if (diff > 0) {
            semaphore.release(diff);
            System.out.printf("[Dubbo限流器] 扩容: %d -> %d%n", maxConcurrent, newLimit);
        } else if (diff < 0) {
            try {
                semaphore.acquire(-diff);
                System.out.printf("[Dubbo限流器] 缩容: %d -> %d%n", maxConcurrent, newLimit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 获取当前限流状态
     */
    public LimiterStatus getStatus() {
        int available = semaphore.availablePermits();
        int active = maxConcurrent - available;
        double usage = (double) active / maxConcurrent * 100;

        return new LimiterStatus(maxConcurrent, active, available, usage);
    }

    // RPC调用接口
    @FunctionalInterface
    public interface RpcInvocation<T> {
        T call() throws Exception;
    }

    // 限流异常
    public static class RateLimitException extends Exception {
        public RateLimitException(String message) {
            super(message);
        }
    }

    // 限流器状态
    public static class LimiterStatus {
        private final int maxConcurrent;
        private final int activeCalls;
        private final int availablePermits;
        private final double usagePercent;

        public LimiterStatus(int maxConcurrent, int activeCalls, int availablePermits, double usagePercent) {
            this.maxConcurrent = maxConcurrent;
            this.activeCalls = activeCalls;
            this.availablePermits = availablePermits;
            this.usagePercent = usagePercent;
        }

        @Override
        public String toString() {
            return String.format("限流状态 [最大并发: %d, 活跃请求: %d, 可用许可: %d, 使用率: %.2f%%]",
                    maxConcurrent, activeCalls, availablePermits, usagePercent);
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        // 创建限流器：最大并发10，超时500ms
        DubboRateLimiter limiter = new DubboRateLimiter(10, 500);

        System.out.println("=== 阿里Dubbo限流器演示 ===\n");

        // 模拟20个并发请求
        for (int i = 0; i < 20; i++) {
            final int requestId = i + 1;
            new Thread(() -> {
                try {
                    limiter.invoke(() -> {
                        System.out.printf("[请求-%d] 开始处理%n", requestId);
                        Thread.sleep(200); // 模拟业务处理
                        return "Success";
                    });
                } catch (RateLimitException e) {
                    System.err.printf("[请求-%d] 被限流: %s%n", requestId, e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Request-" + requestId).start();

            Thread.sleep(50); // 控制请求速率
        }

        // 等待所有请求完成
        Thread.sleep(2000);

        // 查看限流器状态
        System.out.println("\n" + limiter.getStatus());

        // 动态调整限流阈值
        System.out.println("\n=== 动态调整限流阈值 ===");
        limiter.adjustLimit(15);
        System.out.println(limiter.getStatus());
    }
}
