package com.architecture.batchprocess.service;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多级限流器
 * 支持全局限流和API级别限流
 */
public class MultiLevelRateLimiter {

    // 全局限流器：每秒100个请求
    private final RateLimiter globalLimiter;

    // 单个API限流器：每秒30个请求
    private final Map<String, RateLimiter> apiLimiters;

    private final double globalQps;
    private final double apiQps;

    public MultiLevelRateLimiter() {
        this(100.0, 30.0);
    }

    public MultiLevelRateLimiter(double globalQps, double apiQps) {
        this.globalQps = globalQps;
        this.apiQps = apiQps;
        this.globalLimiter = RateLimiter.create(globalQps);
        this.apiLimiters = new ConcurrentHashMap<>();
    }

    /**
     * 获取令牌（阻塞等待）
     * @param apiName API名称
     */
    public void acquire(String apiName) {
        // 先通过全局限流
        globalLimiter.acquire();

        // 再通过API级别限流
        RateLimiter apiLimiter = apiLimiters.computeIfAbsent(
            apiName, k -> RateLimiter.create(apiQps)
        );
        apiLimiter.acquire();
    }

    /**
     * 尝试获取令牌（非阻塞）
     * @param apiName API名称
     * @return 是否成功获取令牌
     */
    public boolean tryAcquire(String apiName) {
        // 先尝试全局限流
        if (!globalLimiter.tryAcquire()) {
            return false;
        }

        // 再尝试API级别限流
        RateLimiter apiLimiter = apiLimiters.computeIfAbsent(
            apiName, k -> RateLimiter.create(apiQps)
        );
        return apiLimiter.tryAcquire();
    }

    /**
     * 动态调整全局QPS
     */
    public void setGlobalQps(double qps) {
        globalLimiter.setRate(qps);
    }

    /**
     * 动态调整指定API的QPS
     */
    public void setApiQps(String apiName, double qps) {
        RateLimiter apiLimiter = apiLimiters.get(apiName);
        if (apiLimiter != null) {
            apiLimiter.setRate(qps);
        }
    }

    public double getGlobalQps() {
        return globalQps;
    }

    public double getApiQps() {
        return apiQps;
    }
}
