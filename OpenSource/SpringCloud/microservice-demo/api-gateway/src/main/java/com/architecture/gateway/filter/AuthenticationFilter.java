package com.architecture.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局认证过滤器
 *
 * 功能说明:
 * 1. 拦截所有请求,验证JWT Token
 * 2. 白名单路径直接放行
 * 3. Token验证成功后,提取用户信息添加到请求头
 * 4. Token验证失败返回401未授权
 *
 * 执行顺序:
 * - 实现Ordered接口,order越小优先级越高
 * - 返回-100表示最高优先级,在其他过滤器之前执行
 *
 * JWT工作流程:
 * 1. 用户登录成功后,服务端生成JWT Token
 * 2. 客户端每次请求携带Token(Header: Authorization: Bearer {token})
 * 3. Gateway验证Token的签名和有效期
 * 4. 提取Token中的用户信息(userId、username等)
 * 5. 将用户信息添加到请求头,传递给下游服务
 *
 * @author Architecture Team
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 白名单 - 这些路径不需要Token验证
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/captcha",
        "/actuator",
        "/fallback"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();

        // 白名单放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 获取Token
        String token = extractToken(request);

        // Token为空,返回401
        if (!StringUtils.hasText(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.just(
                response.bufferFactory().wrap("未授权访问".getBytes(StandardCharsets.UTF_8))
            ));
        }

        try {
            // 验证Token并提取用户信息
            Claims claims = parseToken(token);

            // 将用户信息添加到请求头,传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Name", claims.get("username", String.class))
                .header("X-User-Roles", claims.get("roles", String.class))
                .build();

            // 放行请求
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            // Token验证失败
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.just(
                response.bufferFactory().wrap(("Token验证失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))
            ));
        }
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求头中提取Token
     * 支持两种格式:
     * 1. Authorization: Bearer {token}
     * 2. Authorization: {token}
     */
    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authorization)) {
            if (authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
            return authorization;
        }
        return null;
    }

    /**
     * 解析JWT Token
     * 验证签名和有效期,提取用户信息
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    @Override
    public int getOrder() {
        return -100; // 最高优先级
    }
}
