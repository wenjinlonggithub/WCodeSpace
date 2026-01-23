package com.architecture.function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 消息处理场景示例
 *
 * 展示 Spring Cloud Function 在消息驱动架构中的应用
 * 可与 Spring Cloud Stream 集成，处理 Kafka/RabbitMQ 等消息
 */
@Configuration
public class MessageProcessingFunctions {

    /**
     * 场景1: 订单消息处理
     * 接收订单消息，进行业务处理
     */
    @Bean
    public Consumer<Message<OrderEvent>> orderEventProcessor() {
        return message -> {
            OrderEvent event = message.getPayload();
            String eventType = (String) message.getHeaders().get("eventType");

            System.out.println("=== 处理订单事件 ===");
            System.out.println("事件类型: " + eventType);
            System.out.println("订单ID: " + event.getOrderId());
            System.out.println("用户ID: " + event.getUserId());
            System.out.println("金额: " + event.getAmount());

            // 根据事件类型执行不同的业务逻辑
            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(event);
                    break;
                case "ORDER_PAID":
                    handleOrderPaid(event);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(event);
                    break;
                default:
                    System.out.println("未知事件类型: " + eventType);
            }
        };
    }

    /**
     * 场景2: 消息转换和路由
     * 接收原始消息，转换后发送到下游
     */
    @Bean
    public Function<Message<OrderEvent>, Message<NotificationEvent>> orderToNotification() {
        return message -> {
            OrderEvent order = message.getPayload();

            // 转换为通知事件
            NotificationEvent notification = new NotificationEvent(
                order.getUserId(),
                "订单通知",
                "您的订单 " + order.getOrderId() + " 已创建，金额: " + order.getAmount(),
                LocalDateTime.now()
            );

            // 构建新消息，保留部分原始 Headers
            return MessageBuilder
                .withPayload(notification)
                .setHeader("source", "order-service")
                .setHeader("timestamp", System.currentTimeMillis())
                .build();
        };
    }

    /**
     * 场景3: 消息过滤
     * 只处理满足条件的消息
     */
    @Bean
    public Function<OrderEvent, OrderEvent> filterHighValueOrders() {
        return order -> {
            // 只处理金额大于1000的订单
            if (order.getAmount() > 1000) {
                System.out.println("高价值订单: " + order.getOrderId());
                return order;
            }
            return null; // 返回 null 表示过滤掉该消息
        };
    }

    /**
     * 场景4: 消息聚合
     * 将订单事件转换为统计数据
     */
    @Bean
    public Function<OrderEvent, OrderStatistics> aggregateOrderStats() {
        return order -> {
            // 实际应用中可能需要使用状态存储（如 Redis）来聚合
            return new OrderStatistics(
                order.getUserId(),
                1, // 订单数量
                order.getAmount(), // 总金额
                LocalDateTime.now()
            );
        };
    }

    // ========== 业务逻辑方法 ==========

    private void handleOrderCreated(OrderEvent event) {
        System.out.println("处理订单创建: 检查库存、锁定商品");
        // 实际业务逻辑：
        // 1. 检查库存
        // 2. 锁定商品
        // 3. 创建支付订单
    }

    private void handleOrderPaid(OrderEvent event) {
        System.out.println("处理订单支付: 扣减库存、发送通知");
        // 实际业务逻辑：
        // 1. 扣减库存
        // 2. 发送支付成功通知
        // 3. 触发发货流程
    }

    private void handleOrderCancelled(OrderEvent event) {
        System.out.println("处理订单取消: 释放库存、退款");
        // 实际业务逻辑：
        // 1. 释放库存
        // 2. 处理退款
        // 3. 发送取消通知
    }

    // ========== 数据类定义 ==========

    /**
     * 订单事件
     */
    public static class OrderEvent {
        private String orderId;
        private String userId;
        private double amount;
        private LocalDateTime timestamp;

        public OrderEvent() {}

        public OrderEvent(String orderId, String userId, double amount, LocalDateTime timestamp) {
            this.orderId = orderId;
            this.userId = userId;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 通知事件
     */
    public static class NotificationEvent {
        private String userId;
        private String title;
        private String content;
        private LocalDateTime timestamp;

        public NotificationEvent() {}

        public NotificationEvent(String userId, String title, String content, LocalDateTime timestamp) {
            this.userId = userId;
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 订单统计
     */
    public static class OrderStatistics {
        private String userId;
        private int orderCount;
        private double totalAmount;
        private LocalDateTime lastUpdated;

        public OrderStatistics() {}

        public OrderStatistics(String userId, int orderCount, double totalAmount, LocalDateTime lastUpdated) {
            this.userId = userId;
            this.orderCount = orderCount;
            this.totalAmount = totalAmount;
            this.lastUpdated = lastUpdated;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public int getOrderCount() { return orderCount; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
