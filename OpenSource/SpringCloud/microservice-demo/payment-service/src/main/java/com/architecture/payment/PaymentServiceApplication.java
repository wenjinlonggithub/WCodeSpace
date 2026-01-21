package com.architecture.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 支付服务启动类
 *
 * 核心功能:
 * 1. 创建支付单
 * 2. 支付处理（对接第三方支付平台）
 * 3. 支付回调处理
 * 4. 退款处理
 *
 * 技术要点:
 * - 异步通知: 支付完成后异步通知订单服务
 * - 幂等性: 支付回调可能重复，需要保证幂等
 * - 安全性: 签名验证，防止伪造回调
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("支付服务启动成功 - Payment Service Started");
        System.out.println("端口: 8004");
        System.out.println("========================================");
    }
}
