package com.architecture.config.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 多环境配置示例
 *
 * 配置文件结构：
 * config-repo/
 *   ├── application.yml          # 公共配置
 *   ├── application-dev.yml      # 开发环境
 *   ├── application-test.yml     # 测试环境
 *   ├── application-prod.yml     # 生产环境
 *   └── user-service-dev.yml     # 特定服务配置
 *
 * URL访问规则：
 * http://config-server:8888/{application}/{profile}/{label}
 *
 * 示例：
 * http://config-server:8888/user-service/dev/master
 * http://config-server:8888/user-service/prod/v1.0
 *
 * 优先级（从高到低）：
 * 1. /{application}-{profile}.yml
 * 2. /{application}.yml
 * 3. /application-{profile}.yml
 * 4. /application.yml
 */
@Configuration
public class MultiEnvironmentExample {

    /**
     * 开发环境配置
     */
    @Configuration
    @Profile("dev")
    static class DevConfig {

        @Value("${database.url:jdbc:mysql://localhost:3306/dev_db}")
        private String databaseUrl;

        @Value("${redis.host:localhost}")
        private String redisHost;

        @Value("${logging.level:DEBUG}")
        private String logLevel;

        public void printConfig() {
            System.out.println("=== Development Environment ===");
            System.out.println("Database URL: " + databaseUrl);
            System.out.println("Redis Host: " + redisHost);
            System.out.println("Log Level: " + logLevel);
        }
    }

    /**
     * 生产环境配置
     */
    @Configuration
    @Profile("prod")
    static class ProdConfig {

        @Value("${database.url:jdbc:mysql://prod-db:3306/prod_db}")
        private String databaseUrl;

        @Value("${redis.cluster:redis-cluster:6379}")
        private String redisCluster;

        @Value("${logging.level:INFO}")
        private String logLevel;

        @Value("${feature.toggle.newUI:false}")
        private boolean enableNewUI;

        public void printConfig() {
            System.out.println("=== Production Environment ===");
            System.out.println("Database URL: " + databaseUrl);
            System.out.println("Redis Cluster: " + redisCluster);
            System.out.println("Log Level: " + logLevel);
            System.out.println("New UI Enabled: " + enableNewUI);
        }
    }
}

/**
 * 业务使用案例：电商系统配置管理
 *
 * 场景描述：
 * 电商平台有多个微服务（用户服务、订单服务、商品服务等），
 * 需要在不同环境下使用不同的配置。
 *
 * 配置示例：
 *
 * # application.yml（所有环境共享）
 * spring:
 *   application:
 *     name: ecommerce-platform
 *
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health,info,refresh
 *
 * # order-service-dev.yml（开发环境）
 * database:
 *   url: jdbc:mysql://dev-db:3306/order_dev
 *   pool-size: 10
 *
 * payment:
 *   gateway: sandbox
 *   timeout: 30000
 *
 * # order-service-prod.yml（生产环境）
 * database:
 *   url: jdbc:mysql://prod-db:3306/order_prod
 *   pool-size: 100
 *
 * payment:
 *   gateway: production
 *   timeout: 5000
 *
 * 使用流程：
 * 1. 开发环境启动：java -jar order-service.jar --spring.profiles.active=dev
 * 2. 生产环境启动：java -jar order-service.jar --spring.profiles.active=prod
 * 3. 配置更新：修改Git仓库配置文件
 * 4. 刷新配置：POST /actuator/refresh 或使用Spring Cloud Bus广播
 */
