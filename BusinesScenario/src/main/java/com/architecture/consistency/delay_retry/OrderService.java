package com.architecture.consistency.delay_retry;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单服务
 * 模拟订单创建流程，包含数据库写入延迟
 */
@Slf4j
public class OrderService {

    /**
     * 模拟数据库存储
     */
    private final Map<String, Order> orderDatabase = new ConcurrentHashMap<>();

    /**
     * 模拟数据库写入延迟（毫秒）
     */
    private final long dbWriteDelayMs;

    public OrderService(long dbWriteDelayMs) {
        this.dbWriteDelayMs = dbWriteDelayMs;
    }

    /**
     * 创建订单
     * 模拟真实场景：开始事务 -> 业务处理 -> 提交事务（有延迟）
     *
     * @param userId 用户ID
     * @param productName 商品名称
     * @param amount 金额
     * @return 订单ID
     */
    public String createOrder(String userId, String productName, BigDecimal amount) {
        String orderId = generateOrderId();

        log.info("开始创建订单: orderId={}, userId={}, product={}, amount={}",
                orderId, userId, productName, amount);

        Order order = Order.builder()
                .orderId(orderId)
                .userId(userId)
                .productName(productName)
                .amount(amount)
                .status(Order.OrderStatus.CREATING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 异步写入数据库（模拟真实场景的事务提交延迟）
        new Thread(() -> {
            try {
                // 模拟数据库写入延迟
                Thread.sleep(dbWriteDelayMs);

                // 更新状态为已创建
                order.setStatus(Order.OrderStatus.CREATED);
                order.setUpdateTime(LocalDateTime.now());
                orderDatabase.put(orderId, order);

                log.info("订单创建成功并落库: orderId={}, 延迟={}ms", orderId, dbWriteDelayMs);
            } catch (InterruptedException e) {
                log.error("订单创建失败: orderId={}", orderId, e);
                Thread.currentThread().interrupt();
            }
        }).start();

        log.info("订单创建请求已发起（未落库）: orderId={}", orderId);
        return orderId;
    }

    /**
     * 查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息，不存在返回null
     */
    public Order getOrder(String orderId) {
        Order order = orderDatabase.get(orderId);
        if (order != null) {
            log.info("查询订单成功: orderId={}, status={}", orderId, order.getStatus());
        } else {
            log.warn("查询订单失败，订单不存在: orderId={}", orderId);
        }
        return order;
    }

    /**
     * 生成订单ID
     */
    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * 获取数据库中的订单数量（用于测试）
     */
    public int getOrderCount() {
        return orderDatabase.size();
    }
}
