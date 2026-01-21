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

/**
 * 请求日志过滤器
 *
 * 核心功能：
 * 1. 记录请求信息：路径、方法、参数、IP
 * 2. 记录响应信息：状态码、耗时
 * 3. 链路追踪：生成TraceId
 *
 * 业务价值：
 * - 问题排查：通过日志定位问题
 * - 性能监控：统计API响应时间
 * - 安全审计：记录所有访问记录
 */
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 生成请求ID（链路追踪）
        String requestId = generateRequestId();

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 记录请求信息
        logger.info("Gateway Request [{}] - Method: {}, Path: {}, IP: {}",
                requestId,
                request.getMethod(),
                request.getPath(),
                getClientIp(request));

        // 添加TraceId到请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .header("X-Request-Time", String.valueOf(startTime))
                .build();

        // 继续执行过滤器链，并在响应时记录日志
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    logger.info("Gateway Response [{}] - Status: {}, Duration: {}ms",
                            requestId,
                            exchange.getResponse().getStatusCode(),
                            duration);

                    // 慢请求告警（超过3秒）
                    if (duration > 3000) {
                        logger.warn("Slow Request Detected [{}] - Duration: {}ms, Path: {}",
                                requestId, duration, request.getPath());
                    }
                }));
    }

    @Override
    public int getOrder() {
        return -99;  // 在认证之后执行
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "GW-" + System.currentTimeMillis() + "-" +
               (int) (Math.random() * 10000);
    }

    /**
     * 获取客户端真实IP
     * 考虑了代理和负载均衡的情况
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ?
                 request.getRemoteAddress().getAddress().getHostAddress() : "";
        }
        // 多个IP取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
