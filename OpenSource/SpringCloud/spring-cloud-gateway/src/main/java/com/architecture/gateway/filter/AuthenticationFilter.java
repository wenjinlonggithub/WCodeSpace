package com.architecture.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局认证过滤器
 *
 * 核心原理：
 * - GlobalFilter：应用于所有路由
 * - Ordered：控制过滤器执行顺序，数字越小优先级越高
 *
 * 执行顺序：
 * Order -1 (认证) -> Order 0 (日志) -> Order 1 (限流)
 *
 * 业务场景：
 * - 统一认证：验证JWT Token
 * - 权限校验：检查用户权限
 * - 签名验证：防止API被恶意调用
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    /**
     * 过滤器执行逻辑
     *
     * 处理流程：
     * 1. 获取请求头中的Token
     * 2. 验证Token有效性
     * 3. 解析用户信息并传递给下游服务
     * 4. 无效Token返回401
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getPath().toString();

        // 白名单：登录、注册等接口不需要认证
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 获取Token
        String token = request.getHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 验证Token（实际应该调用认证服务）
        if (!validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 解析用户信息并添加到请求头，传递给下游服务
        String userId = extractUserId(token);
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Name", extractUserName(token))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;  // 优先级最高，最先执行
    }

    /**
     * 白名单判断
     */
    private boolean isWhiteList(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/public");
    }

    /**
     * Token验证
     * 实际应该：
     * 1. 解析JWT Token
     * 2. 验证签名
     * 3. 检查过期时间
     * 4. 查询Redis缓存
     */
    private boolean validateToken(String token) {
        // 简化示例
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token.length() > 10;
    }

    /**
     * 提取用户ID
     */
    private String extractUserId(String token) {
        // 实际应该解析JWT获取
        return "user_123456";
    }

    /**
     * 提取用户名
     */
    private String extractUserName(String token) {
        // 实际应该解析JWT获取
        return "john_doe";
    }
}
