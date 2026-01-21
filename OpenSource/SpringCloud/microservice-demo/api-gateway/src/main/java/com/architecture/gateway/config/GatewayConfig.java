package com.architecture.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Gateway配置类
 *
 * 功能说明:
 * 1. 配置限流策略的Key解析器
 * 2. 支持多种限流维度:IP、用户、接口等
 *
 * @author Architecture Team
 */
@Configuration
public class GatewayConfig {

    /**
     * 基于IP的限流Key解析器
     * 相同IP共享限流配额
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress()
                .getHostAddress()
        );
    }

    /**
     * 基于用户的限流Key解析器
     * 相同用户共享限流配额
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(
                exchange.getRequest().getHeaders().getFirst("X-User-Id"),
                "anonymous"
            )
        );
    }

    /**
     * 基于接口路径的限流Key解析器
     * 相同接口共享限流配额
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getURI().getPath()
        );
    }
}
