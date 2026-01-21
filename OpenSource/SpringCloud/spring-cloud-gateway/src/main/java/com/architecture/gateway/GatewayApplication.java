package com.architecture.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Spring Cloud Gateway 核心应用
 *
 * 核心概念：
 * 1. Route（路由）：网关的基本构建块，包含ID、目标URI、断言集合、过滤器集合
 * 2. Predicate（断言）：匹配HTTP请求的条件，如路径、方法、请求头等
 * 3. Filter（过滤器）：对请求和响应进行修改
 *
 * 核心原理：
 * - 基于WebFlux实现的响应式网关
 * - 非阻塞式I/O，性能优于Zuul
 * - 支持动态路由、限流、熔断等功能
 *
 * 请求处理流程：
 * Client -> Gateway Handler Mapping -> Gateway Web Handler
 *   -> Filter Chain -> Proxied Service
 *
 * 优势：
 * 1. 性能高 - 基于Netty和WebFlux
 * 2. 功能强 - 内置多种断言和过滤器
 * 3. 易扩展 - 支持自定义断言和过滤器
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("=================================");
        System.out.println("Gateway Started Successfully!");
        System.out.println("Port: 8080");
        System.out.println("=================================");
    }

    /**
     * 编程式路由配置
     *
     * 业务场景：API网关统一入口
     * - /api/user/** -> 用户服务
     * - /api/order/** -> 订单服务
     * - /api/product/** -> 商品服务
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务路由
                .route("user_service_route", r -> r
                        .path("/api/user/**")
                        .filters(f -> f
                                .stripPrefix(2)  // 去掉 /api/user 前缀
                                .addRequestHeader("X-Gateway", "Spring-Cloud-Gateway")
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                        )
                        .uri("lb://user-service")  // lb = LoadBalancer
                )
                // 订单服务路由（带断言）
                .route("order_service_route", r -> r
                        .path("/api/order/**")
                        .and()
                        .method("GET", "POST")  // 只允许GET和POST
                        .and()
                        .header("Authorization")  // 必须有Authorization头
                        .filters(f -> f
                                .stripPrefix(2)
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                                )
                        )
                        .uri("lb://order-service")
                )
                // 商品服务路由（路径重写）
                .route("product_service_route", r -> r
                        .path("/api/product/**")
                        .filters(f -> f
                                .rewritePath("/api/product/(?<segment>.*)", "/product/${segment}")
                        )
                        .uri("lb://product-service")
                )
                // 外部API代理
                .route("external_api_route", r -> r
                        .path("/external/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-API-Key", "your-api-key")
                        )
                        .uri("https://api.external.com")
                )
                .build();
    }
}
