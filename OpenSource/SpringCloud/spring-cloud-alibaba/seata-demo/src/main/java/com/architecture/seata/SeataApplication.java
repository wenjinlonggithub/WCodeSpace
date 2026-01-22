package com.architecture.seata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Seata分布式事务示例应用
 *
 * 核心原理：
 * 1. AT模式：自动拦截SQL，记录前后镜像，实现自动回滚
 * 2. TCC模式：Try-Confirm-Cancel三阶段提交
 * 3. SAGA模式：长事务解决方案，支持事务编排
 * 4. XA模式：传统两阶段提交协议
 *
 * 业务应用场景：
 * - 电商订单：订单创建、库存扣减、账户扣款的分布式事务
 * - 金融转账：跨账户转账的事务一致性保证
 * - 物流系统：订单、库存、配送状态的一致性管理
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SeataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataApplication.class, args);
    }
}
