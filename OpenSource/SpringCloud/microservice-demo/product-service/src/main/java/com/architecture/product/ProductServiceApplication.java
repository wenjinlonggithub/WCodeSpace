package com.architecture.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 商品服务启动类
 *
 * 核心注解:
 * - @SpringBootApplication: Spring Boot自动配置
 * - @EnableDiscoveryClient: 启用服务发现客户端
 *
 * 启动流程:
 * 1. Spring Boot启动
 * 2. 连接Eureka Server注册服务
 * 3. 定期发送心跳维持注册状态
 * 4. 暴露REST接口供其他服务调用
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("商品服务启动成功 - Product Service Started");
        System.out.println("端口: 8003");
        System.out.println("========================================");
    }
}
