package com.architecture.consistency.delay_retry;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 延迟消费 + 重试机制示例
 *
 * 场景说明：
 * 1. 订单服务创建订单，模拟数据库写入延迟2秒
 * 2. IM服务收到通知，延迟3秒后查询订单并发送消息
 * 3. 如果订单未落库，则按照指数退避策略重试
 *
 * 预期结果：
 * - 第一次尝试（延迟3秒）：订单已落库，发送成功
 * 或
 * - 第一次尝试（延迟1秒）：订单未落库，触发重试
 * - 第N次重试：订单已落库，发送成功
 */
@Slf4j
public class DelayRetryExample {

    public static void main(String[] args) throws InterruptedException {
        log.info("=== 延迟消费 + 重试机制示例开始 ===\n");

        // 场景1：延迟时间足够，一次成功
        scenario1_SuccessWithInitialDelay();

        Thread.sleep(3000);
        log.info("\n" + "=".repeat(80) + "\n");

        // 场景2：延迟时间不够，需要重试
        scenario2_SuccessAfterRetry();

        Thread.sleep(10000);
        log.info("\n" + "=".repeat(80) + "\n");

        // 场景3：订单永远不存在，进入死信队列
        scenario3_DeadLetter();

        Thread.sleep(15000);
        log.info("\n=== 延迟消费 + 重试机制示例结束 ===");
        System.exit(0);
    }

    /**
     * 场景1：初始延迟时间足够，订单已落库，一次发送成功
     */
    private static void scenario1_SuccessWithInitialDelay() throws InterruptedException {
        log.info("【场景1】初始延迟时间足够，一次发送成功");
        log.info("配置: 订单落库延迟=2s, IM消息延迟=3s\n");

        // 订单服务：2秒延迟
        OrderService orderService = new OrderService(2000);

        // IM服务：使用默认重试策略
        RetryStrategy strategy = RetryStrategy.defaultStrategy();
        strategy.printRetryPlan();
        IMMessageService imService = new IMMessageService(orderService, strategy);

        // 创建订单
        String orderId = orderService.createOrder("user001", "iPhone 15 Pro", new BigDecimal("7999.00"));

        // 发送IM消息（延迟3秒）
        imService.sendOrderCreatedNotification(orderId, "user001", 3);

        // 等待消息处理完成
        Thread.sleep(5000);

        // 检查结果
        printResult(imService, orderId);

        imService.shutdown();
    }

    /**
     * 场景2：初始延迟时间不够，需要重试后成功
     */
    private static void scenario2_SuccessAfterRetry() throws InterruptedException {
        log.info("【场景2】初始延迟时间不够，需要重试后成功");
        log.info("配置: 订单落库延迟=3s, IM消息延迟=1s\n");

        // 订单服务：3秒延迟
        OrderService orderService = new OrderService(3000);

        // IM服务：使用激进重试策略（更快的重试）
        RetryStrategy strategy = RetryStrategy.aggressiveStrategy();
        strategy.printRetryPlan();
        IMMessageService imService = new IMMessageService(orderService, strategy);

        // 创建订单
        String orderId = orderService.createOrder("user002", "MacBook Pro", new BigDecimal("19999.00"));

        // 发送IM消息（延迟1秒，此时订单还未落库）
        imService.sendOrderCreatedNotification(orderId, "user002", 1);

        // 等待消息处理完成（需要更长时间，因为有重试）
        Thread.sleep(8000);

        // 检查结果
        printResult(imService, orderId);

        imService.shutdown();
    }

    /**
     * 场景3：订单永远不存在，超过最大重试次数，进入死信队列
     */
    private static void scenario3_DeadLetter() throws InterruptedException {
        log.info("【场景3】订单不存在，进入死信队列");
        log.info("配置: 订单不创建, 最大重试5次\n");

        // 订单服务
        OrderService orderService = new OrderService(1000);

        // IM服务
        RetryStrategy strategy = new RetryStrategy(0, 2, 1.5, 5);
        strategy.printRetryPlan();
        IMMessageService imService = new IMMessageService(orderService, strategy);

        // 不创建订单，直接发送消息
        String fakeOrderId = "ORD999999";
        imService.sendOrderCreatedNotification(fakeOrderId, "user003", 1);

        // 等待所有重试完成
        Thread.sleep(12000);

        // 检查死信队列
        log.info("死信队列大小: {}", imService.getDeadLetterQueueSize());

        imService.shutdown();
    }

    /**
     * 打印结果
     */
    private static void printResult(IMMessageService imService, String orderId) {
        log.info("\n【处理结果】");
        log.info("订单ID: {}", orderId);
        log.info("死信队列大小: {}", imService.getDeadLetterQueueSize());
    }
}
