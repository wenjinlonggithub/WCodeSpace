package com.architecture.eventdriven;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付事件
 */
public class OrderPaidEvent {

    private final String orderId;
    private final String paymentId;
    private final BigDecimal amount;
    private final LocalDateTime paidAt;

    public OrderPaidEvent(String orderId, String paymentId, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.paidAt = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    @Override
    public String toString() {
        return "OrderPaidEvent{" +
                "orderId='" + orderId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", amount=" + amount +
                ", paidAt=" + paidAt +
                '}';
    }
}
