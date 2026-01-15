package com.architecture.consistency.local_message;

import com.architecture.consistency.delay_retry.Order;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持事务的订单服务
 * 在本地事务中同时写入订单表和本地消息表
 */
@Slf4j
public class TransactionalOrderService {

    private final Map<String, Order> orderDatabase = new ConcurrentHashMap<>();
    private final LocalMessageDAO localMessageDAO;
    private final Gson gson = new Gson();

    public TransactionalOrderService(LocalMessageDAO localMessageDAO) {
        this.localMessageDAO = localMessageDAO;
    }

    /**
     * 创建订单（事务方法）
     * 在同一个事务中：
     * 1. 写入订单表
     * 2. 写入本地消息表
     *
     * 这样保证了订单和消息的强一致性
     */
    public String createOrderWithMessage(String userId, String productName, BigDecimal amount) {
        String orderId = generateOrderId();

        try {
            // 开始事务（模拟）
            beginTransaction();

            // 1. 写入订单表
            Order order = Order.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .productName(productName)
                    .amount(amount)
                    .status(Order.OrderStatus.CREATED)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            orderDatabase.put(orderId, order);
            log.info("订单表插入成功: orderId={}", orderId);

            // 2. 写入本地消息表
            LocalMessage message = createLocalMessage(order);
            localMessageDAO.insert(message);

            // 提交事务（模拟）
            commitTransaction();

            log.info("订单创建成功（包含本地消息）: orderId={}, messageId={}",
                    orderId, message.getMessageId());

            return orderId;

        } catch (Exception e) {
            // 回滚事务（模拟）
            rollbackTransaction();
            log.error("订单创建失败，事务已回滚: orderId={}", orderId, e);
            throw new RuntimeException("订单创建失败", e);
        }
    }

    /**
     * 创建本地消息记录
     */
    private LocalMessage createLocalMessage(Order order) {
        String messageId = "MSG" + System.currentTimeMillis();

        // 构建消息内容
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("orderId", order.getOrderId());
        messageContent.put("userId", order.getUserId());
        messageContent.put("productName", order.getProductName());
        messageContent.put("amount", order.getAmount());
        messageContent.put("status", order.getStatus().name());

        return LocalMessage.builder()
                .messageId(messageId)
                .businessId(order.getOrderId())
                .businessType("ORDER_CREATED")
                .content(gson.toJson(messageContent))
                .targetService("IM_SERVICE")
                .status(LocalMessage.MessageStatus.PENDING)
                .retryCount(0)
                .maxRetryCount(5)
                .nextExecuteTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 查询订单
     */
    public Order getOrder(String orderId) {
        return orderDatabase.get(orderId);
    }

    /**
     * 开始事务（模拟）
     */
    private void beginTransaction() {
        log.debug("开始事务");
    }

    /**
     * 提交事务（模拟）
     */
    private void commitTransaction() {
        log.debug("提交事务");
    }

    /**
     * 回滚事务（模拟）
     */
    private void rollbackTransaction() {
        log.warn("回滚事务");
    }

    /**
     * 生成订单ID
     */
    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
