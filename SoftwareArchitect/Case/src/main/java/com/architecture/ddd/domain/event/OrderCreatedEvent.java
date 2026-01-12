package com.architecture.ddd.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DDD - 订单创建领域事件
 */
public class OrderCreatedEvent extends DomainEvent {

    private final String orderId;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public OrderCreatedEvent(String orderId, String customerId, BigDecimal totalAmount, LocalDateTime createdAt) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", totalAmount=" + totalAmount +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}
