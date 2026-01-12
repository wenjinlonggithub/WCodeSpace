package com.architecture.eventdriven;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单创建事件
 */
public class OrderCreatedEvent {

    private final String orderId;
    private final String userId;
    private final BigDecimal totalAmount;
    private final LocalDateTime occurredOn;

    public OrderCreatedEvent(String orderId, String userId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.occurredOn = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", totalAmount=" + totalAmount +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
