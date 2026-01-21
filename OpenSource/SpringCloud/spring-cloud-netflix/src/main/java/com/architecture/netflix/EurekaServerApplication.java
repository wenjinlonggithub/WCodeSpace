package com.architecture.netflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - 服务注册中心
 *
 * 核心功能：
 * 1. 服务注册：微服务启动时注册到Eureka
 * 2. 服务发现：客户端从Eureka获取服务列表
 * 3. 健康检查：定期检查服务实例健康状态
 * 4. 服务剔除：自动剔除不健康的实例
 *
 * 核心原理：
 * - 基于REST的服务注册与发现
 * - AP模型（可用性 + 分区容错性）
 * - 自我保护机制
 * - 多级缓存机制
 *
 * 数据结构：
 * Registry(注册表)
 *   └─ Application (服务名)
 *       └─ InstanceInfo (实例信息)
 *           ├─ instanceId
 *           ├─ hostName
 *           ├─ ipAddr
 *           ├─ port
 *           └─ status (UP/DOWN/STARTING/OUT_OF_SERVICE)
 *
 * 三级缓存：
 * 1. registry: 实时注册表
 * 2. readWriteCacheMap: 读写缓存（Guava Cache）
 * 3. readOnlyCacheMap: 只读缓存（定时同步）
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
        System.out.println("=================================");
        System.out.println("Eureka Server Started!");
        System.out.println("Dashboard: http://localhost:8761");
        System.out.println("=================================");
    }
}
