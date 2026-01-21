package com.architecture.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * API Gateway 启动类
 *
 * 核心功能:
 * 1. 路由转发: 根据请求路径转发到对应的微服务
 * 2. 负载均衡: 自动负载均衡到多个服务实例
 * 3. 统一认证: 集中处理用户认证和授权
 * 4. 限流降级: 防止服务过载,保护后端服务
 * 5. 日志追踪: 统一记录请求日志,方便问题排查
 * 6. 跨域处理: 统一处理CORS跨域请求
 *
 * Gateway核心概念:
 * - Route(路由): 网关的基本构建块,包含ID、目标URI、断言集合、过滤器集合
 * - Predicate(断言): 匹配HTTP请求的条件,如路径、方法、Header等
 * - Filter(过滤器): 在请求转发前后对请求和响应进行修改
 *
 * 执行流程:
 * 1. 客户端请求到达Gateway
 * 2. Gateway Handler Mapping匹配路由
 * 3. 通过Predicate判断是否匹配
 * 4. 执行Pre Filter(前置过滤器)
 * 5. 转发请求到目标服务
 * 6. 执行Post Filter(后置过滤器)
 * 7. 返回响应给客户端
 *
 * 常用断言:
 * - Path: 路径匹配
 * - Method: HTTP方法匹配
 * - Header: 请求头匹配
 * - Query: 请求参数匹配
 * - Cookie: Cookie匹配
 * - After/Before/Between: 时间匹配
 *
 * 常用过滤器:
 * - AddRequestHeader: 添加请求头
 * - AddRequestParameter: 添加请求参数
 * - StripPrefix: 去除路径前缀
 * - PrefixPath: 添加路径前缀
 * - RewritePath: 重写路径
 * - CircuitBreaker: 熔断器
 * - RequestRateLimiter: 限流器
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("==================================================");
        System.out.println("API Gateway 启动成功!");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("==================================================");
    }
}
