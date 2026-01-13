package com.architecture.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流算法
 *
 * 原理：
 * 1. 以固定速率（如每秒10个）生成令牌放入桶中
 * 2. 桶有容量上限（如20个令牌）
 * 3. 请求需要消耗令牌才能通过
 * 4. 桶满时令牌溢出丢弃
 * 5. 桶空时请求被拒绝
 *
 * 特点：
 * - 允许一定程度的突发流量（桶内积累的令牌）
 * - 长期平均速率受限于令牌生成速率
 *
 * 应用：
 * - Guava RateLimiter
 * - Nginx ngx_http_limit_req_module
 */
public class TokenBucketRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    // 桶容量（最多存储多少令牌）
    private final long capacity;

    // 令牌生成速率（每秒生成多少令牌）
    private final long tokensPerSecond;

    // 当前令牌数量
    private final AtomicLong tokens;

    // 上次生成令牌的时间戳
    private final AtomicLong lastRefillTime;

    public TokenBucketRateLimiter(long tokensPerSecond, long capacity) {
        this.tokensPerSecond = tokensPerSecond;
        this.capacity = capacity;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTime = new AtomicLong(System.nanoTime());
    }

    /**
     * 尝试获取令牌（非阻塞）
     *
     * @param permits 需要的令牌数量
     * @return true-成功，false-失败
     */
    public boolean tryAcquire(long permits) {
        // 先补充令牌
        refillTokens();

        // 尝试消费令牌
        while (true) {
            long currentTokens = tokens.get();

            if (currentTokens < permits) {
                // 令牌不足
                logger.debug("限流：令牌不足, current={}, need={}", currentTokens, permits);
                return false;
            }

            // CAS更新令牌数
            if (tokens.compareAndSet(currentTokens, currentTokens - permits)) {
                logger.debug("获取令牌成功, consumed={}, remaining={}", permits, currentTokens - permits);
                return true;
            }

            // CAS失败，重试
        }
    }

    /**
     * 尝试获取1个令牌
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 阻塞获取令牌
     *
     * @param permits 需要的令牌数量
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true-成功，false-超时
     */
    public boolean acquire(long permits, long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);

        while (System.nanoTime() < deadline) {
            if (tryAcquire(permits)) {
                return true;
            }

            // 等待一段时间后重试
            Thread.sleep(10);
        }

        return false;
    }

    /**
     * 补充令牌
     *
     * 根据时间流逝，计算应该生成的令牌数量
     */
    private void refillTokens() {
        long now = System.nanoTime();
        long lastTime = lastRefillTime.get();

        // 计算时间流逝（纳秒）
        long elapsedNanos = now - lastTime;

        // 计算应该生成的令牌数
        long tokensToAdd = (elapsedNanos * tokensPerSecond) / TimeUnit.SECONDS.toNanos(1);

        if (tokensToAdd > 0) {
            // 更新时间戳
            if (lastRefillTime.compareAndSet(lastTime, now)) {
                // 更新令牌数（不超过容量）
                while (true) {
                    long currentTokens = tokens.get();
                    long newTokens = Math.min(capacity, currentTokens + tokensToAdd);

                    if (tokens.compareAndSet(currentTokens, newTokens)) {
                        logger.debug("补充令牌, added={}, current={}", tokensToAdd, newTokens);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 获取当前令牌数
     */
    public long getAvailableTokens() {
        refillTokens();
        return tokens.get();
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) throws InterruptedException {
        // 创建限流器：每秒10个令牌，桶容量20
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 20);

        // 模拟请求
        for (int i = 0; i < 100; i++) {
            if (limiter.tryAcquire()) {
                logger.info("请求{}：通过", i);
            } else {
                logger.warn("请求{}：被限流", i);
            }

            Thread.sleep(50); // 每50ms一个请求（每秒20个）
        }

        // 结果：
        // 前20个请求通过（消耗桶内令牌）
        // 之后每秒通过10个（令牌生成速率）
        // 其余请求被限流
    }
}
