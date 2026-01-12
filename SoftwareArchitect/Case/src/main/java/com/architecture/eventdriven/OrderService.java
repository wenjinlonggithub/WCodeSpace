package com.architecture.eventdriven;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 订单服务 - 事件生产者
 * 执行业务操作后发布事件
 */
public class OrderService {

    private final EventBus eventBus;

    public OrderService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 创建订单
     */
    public String createOrder(String userId, BigDecimal totalAmount) {
        System.out.println("\n>>> OrderService: 创建订单");
        System.out.println("用户ID: " + userId);
        System.out.println("订单金额: " + totalAmount);

        // 1. 执行业务逻辑
        String orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8);

        // 2. 发布事件
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, totalAmount);
        eventBus.publish(event);

        System.out.println("✓ 订单创建成功: " + orderId);
        return orderId;
    }

    /**
     * 支付订单
     */
    public void payOrder(String orderId, BigDecimal amount) {
        System.out.println("\n>>> OrderService: 支付订单");
        System.out.println("订单ID: " + orderId);
        System.out.println("支付金额: " + amount);

        // 1. 执行支付逻辑
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);

        // 2. 发布事件
        OrderPaidEvent event = new OrderPaidEvent(orderId, paymentId, amount);
        eventBus.publish(event);

        System.out.println("✓ 支付成功: " + paymentId);
    }
}
