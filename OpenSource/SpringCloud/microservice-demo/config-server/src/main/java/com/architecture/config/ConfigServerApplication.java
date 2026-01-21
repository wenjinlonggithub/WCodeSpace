package com.architecture.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Config Server 启动类
 *
 * 核心功能:
 * 1. 集中配置管理: 统一管理所有微服务的配置文件
 * 2. 环境隔离: 支持dev、test、prod等多环境配置
 * 3. 动态刷新: 配置修改后可动态刷新,无需重启服务
 * 4. 版本管理: 基于Git实现配置的版本控制和回滚
 *
 * 工作原理:
 * 1. Config Server从Git仓库拉取配置文件
 * 2. 微服务启动时从Config Server获取配置
 * 3. 配置文件支持占位符和加密
 * 4. 支持多种存储后端: Git、SVN、本地文件系统
 *
 * 配置文件命名规则:
 * - {application}-{profile}.yml
 * - 例如: user-service-dev.yml, order-service-prod.yml
 *
 * 访问规则:
 * - /{application}/{profile}[/{label}]
 * - /{application}-{profile}.yml
 * - /{label}/{application}-{profile}.yml
 *
 * 示例:
 * http://localhost:8888/user-service/dev
 * http://localhost:8888/user-service-dev.yml
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableConfigServer // 启用Config Server功能
@EnableEurekaClient // 注册到Eureka
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        System.out.println("==================================================");
        System.out.println("Config Server 启动成功!");
        System.out.println("访问地址: http://localhost:8888");
        System.out.println("配置查询: http://localhost:8888/{application}/{profile}");
        System.out.println("==================================================");
    }
}
