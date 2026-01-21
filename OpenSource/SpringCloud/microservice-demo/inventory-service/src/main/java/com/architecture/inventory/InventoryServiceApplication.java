package com.architecture.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 库存服务启动类
 *
 * 核心功能:
 * 1. 库存查询
 * 2. 库存扣减（创建订单时调用）
 * 3. 库存归还（取消订单时调用）
 *
 * 并发控制:
 * - 使用同步锁保证库存扣减的线程安全
 * - 生产环境应使用Redis分布式锁或数据库行锁
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("库存服务启动成功 - Inventory Service Started");
        System.out.println("端口: 8005");
        System.out.println("========================================");
    }
}
