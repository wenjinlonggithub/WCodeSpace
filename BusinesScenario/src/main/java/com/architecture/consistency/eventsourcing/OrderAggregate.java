package com.architecture.consistency.eventsourcing;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根
 * 通过重放事件来恢复状态
 */
@Data
public class OrderAggregate {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 版本号（用于乐观锁）
     */
    private Long version = 0L;

    /**
     * 未提交的事件
     */
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();

    /**
     * 创建订单
     */
    public void createOrder(String orderId, String userId, String productName, BigDecimal amount) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, userId, productName, amount, LocalDateTime.now()
        );

        applyEvent(event);
    }

    /**
     * 支付订单
     */
    public void payOrder() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有已创建的订单才能支付");
        }

        OrderPaidEvent event = new OrderPaidEvent(orderId, LocalDateTime.now());
        applyEvent(event);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(String reason) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("订单已取消");
        }

        OrderCancelledEvent event = new OrderCancelledEvent(orderId, reason, LocalDateTime.now());
        applyEvent(event);
    }

    /**
     * 应用事件
     */
    private void applyEvent(DomainEvent event) {
        event.setEventId("EVT" + System.currentTimeMillis() + version);
        event.setAggregateId(orderId);
        event.setVersion(version + 1);

        event.applyTo(this);
        uncommittedEvents.add(event);
    }

    /**
     * 重放事件（从事件流恢复状态）
     */
    public void replayEvents(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            event.applyTo(this);
        }
    }

    /**
     * 获取未提交的事件
     */
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    /**
     * 清空未提交的事件
     */
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        CREATED, PAID, CANCELLED
    }
}
