package com.architecture.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server
 *
 * 核心功能：
 * 1. 集中化配置管理 - 统一管理所有微服务的配置
 * 2. 配置版本控制 - 基于Git/SVN等版本控制系统
 * 3. 环境隔离 - 支持dev/test/prod等多环境配置
 * 4. 动态刷新 - 配合Bus实现配置动态更新
 *
 * 核心原理：
 * - @EnableConfigServer 开启配置服务器功能
 * - 从Git仓库读取配置文件
 * - 通过HTTP接口暴露配置信息
 * - 支持加密/解密敏感信息
 *
 * 访问模式：
 * /{application}/{profile}[/{label}]
 * /{application}-{profile}.yml
 * /{label}/{application}-{profile}.yml
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        System.out.println("=================================");
        System.out.println("Config Server Started Successfully!");
        System.out.println("Port: 8888");
        System.out.println("=================================");
    }
}
