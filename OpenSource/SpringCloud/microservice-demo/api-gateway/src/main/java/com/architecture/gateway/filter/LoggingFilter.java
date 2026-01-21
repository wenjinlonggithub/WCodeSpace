package com.architecture.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 全局日志过滤器
 *
 * 功能说明:
 * 1. 记录每个请求的基本信息(方法、路径、参数等)
 * 2. 生成唯一的TraceId,用于全链路追踪
 * 3. 记录请求处理耗时
 * 4. 将TraceId传递给下游服务
 *
 * 使用场景:
 * - 排查问题时,通过TraceId追踪整个调用链路
 * - 性能监控,分析慢请求
 * - 统计接口调用情况
 *
 * @author Architecture Team
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 生成TraceId
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 记录请求信息
        logger.info("===> [{}] {} {} from {}",
            traceId,
            request.getMethod(),
            request.getURI().getPath(),
            request.getRemoteAddress()
        );

        // 将TraceId添加到请求头,传递给下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-Trace-Id", traceId)
            .build();

        // 继续执行过滤器链
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
            .then(Mono.fromRunnable(() -> {
                // 计算耗时
                long duration = System.currentTimeMillis() - startTime;

                // 记录响应信息
                logger.info("<=== [{}] {} {} completed in {}ms, status: {}",
                    traceId,
                    request.getMethod(),
                    request.getURI().getPath(),
                    duration,
                    exchange.getResponse().getStatusCode()
                );
            }));
    }

    @Override
    public int getOrder() {
        return -99; // 在认证过滤器之后执行
    }
}
