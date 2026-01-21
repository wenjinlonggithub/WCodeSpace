package com.architecture.gateway.filter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 * 限流配置
 *
 * 核心原理：
 * 1. Redis + Lua脚本实现令牌桶算法
 * 2. 支持多维度限流：IP、用户、API
 * 3. 分布式限流：多个网关实例共享限流数据
 *
 * 令牌桶算法：
 * - replenishRate: 令牌生成速率（每秒放入多少令牌）
 * - burstCapacity: 桶容量（最多存储多少令牌）
 * - requestedTokens: 每次请求消耗的令牌数
 *
 * 业务场景：
 * 1. 防止API被刷：限制单个IP的访问频率
 * 2. 保护系统：防止突发流量压垮服务
 * 3. 公平使用：防止个别用户占用过多资源
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 基于IP的限流
     *
     * 场景：防止恶意刷接口
     * 策略：同一IP每秒最多10个请求，峰值20
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * 基于用户的限流
     *
     * 场景：VIP用户和普通用户不同的限流策略
     * 策略：通过用户等级设置不同的限流参数
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * 基于API的限流
     *
     * 场景：不同API设置不同的限流阈值
     * 策略：
     * - 查询接口：100次/秒
     * - 写入接口：10次/秒
     * - 批量接口：1次/秒
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            return Mono.just(path);
        };
    }

    /**
     * 组合限流策略
     *
     * 场景：IP + 用户双重限流
     * 策略：既限制IP的总访问量，也限制单个用户的访问量
     */
    @Bean
    public KeyResolver compositeKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            userId = userId != null ? userId : "anonymous";

            return Mono.just(ip + ":" + userId);
        };
    }
}

/**
 * 使用示例（在application.yml中配置）:
 *
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *         - id: rate_limit_route
 *           uri: lb://user-service
 *           predicates:
 *             - Path=/api/user/**
 *           filters:
 *             - name: RequestRateLimiter
 *               args:
 *                 # 令牌生成速率：每秒10个
 *                 redis-rate-limiter.replenishRate: 10
 *                 # 令牌桶容量：最多20个
 *                 redis-rate-limiter.burstCapacity: 20
 *                 # 每次请求消耗1个令牌
 *                 redis-rate-limiter.requestedTokens: 1
 *                 # 使用IP限流
 *                 key-resolver: "#{@ipKeyResolver}"
 *
 *   redis:
 *     host: localhost
 *     port: 6379
 *
 * 业务使用案例：电商秒杀场景
 *
 * 1. 活动页面访问：
 *    - IP限流：防止单个IP疯狂刷新
 *    - replenishRate: 100, burstCapacity: 200
 *
 * 2. 秒杀下单接口：
 *    - 用户限流：每个用户最多1次/秒
 *    - replenishRate: 1, burstCapacity: 1
 *
 * 3. 查询订单接口：
 *    - API限流：总体控制QPS
 *    - replenishRate: 1000, burstCapacity: 2000
 *
 * 限流响应处理：
 * - 状态码：429 Too Many Requests
 * - 返回头：X-RateLimit-Remaining（剩余令牌数）
 * - 提示信息：请求过于频繁，请稍后再试
 */
