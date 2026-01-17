package com.architecture.batchprocess.service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 批处理监控组件
 * 用于监控处理进度和性能指标
 */
public class BatchProcessMonitor {

    private final AtomicLong processedCounter = new AtomicLong(0);
    private final AtomicLong successCounter = new AtomicLong(0);
    private final AtomicLong failureCounter = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);

    private long startTime;

    public void start() {
        this.startTime = System.currentTimeMillis();
        reset();
    }

    /**
     * 记录处理结果
     */
    public void recordProcess(boolean success, long latencyMs) {
        processedCounter.incrementAndGet();

        if (success) {
            successCounter.incrementAndGet();
        } else {
            failureCounter.incrementAndGet();
        }

        totalLatency.addAndGet(latencyMs);
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        long total = processedCounter.get();
        if (total == 0) return 0;
        return (double) successCounter.get() / total * 100;
    }

    /**
     * 获取平均延迟
     */
    public double getAvgLatency() {
        long total = processedCounter.get();
        if (total == 0) return 0;
        return (double) totalLatency.get() / total;
    }

    /**
     * 获取TPS（每秒处理数）
     */
    public double getTps() {
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedSeconds == 0) return 0;
        return (double) processedCounter.get() / elapsedSeconds;
    }

    /**
     * 打印统计信息
     */
    public void printStats() {
        long total = processedCounter.get();
        long success = successCounter.get();
        long failure = failureCounter.get();

        System.out.println("\n========== 批处理统计 ==========");
        System.out.println("总处理数: " + total);
        System.out.println("成功数: " + success);
        System.out.println("失败数: " + failure);
        System.out.println("成功率: " + String.format("%.2f%%", getSuccessRate()));
        System.out.println("平均延迟: " + String.format("%.2fms", getAvgLatency()));
        System.out.println("TPS: " + String.format("%.2f", getTps()));
        System.out.println("================================\n");
    }

    /**
     * 重置计数器
     */
    public void reset() {
        processedCounter.set(0);
        successCounter.set(0);
        failureCounter.set(0);
        totalLatency.set(0);
    }

    // Getters
    public long getProcessedCount() {
        return processedCounter.get();
    }

    public long getSuccessCount() {
        return successCounter.get();
    }

    public long getFailureCount() {
        return failureCounter.get();
    }
}
