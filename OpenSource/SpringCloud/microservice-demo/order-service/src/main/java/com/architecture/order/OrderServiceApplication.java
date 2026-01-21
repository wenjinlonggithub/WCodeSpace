package com.architecture.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 *
 * 技术栈:
 * - Spring Boot: 微服务基础框架
 * - Eureka Client: 服务注册与发现
 * - OpenFeign: 声明式HTTP客户端
 * - Resilience4j: 熔断降级
 *
 * @author architecture
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现
@EnableFeignClients     // 启用Feign客户端
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Order Service Started Successfully   ");
        System.out.println("   Port: 8002                           ");
        System.out.println("========================================");
    }
}
