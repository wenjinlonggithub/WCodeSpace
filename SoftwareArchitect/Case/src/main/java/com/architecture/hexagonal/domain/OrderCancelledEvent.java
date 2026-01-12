package com.architecture.hexagonal.domain;

/**
 * 订单取消事件
 */
public class OrderCancelledEvent extends DomainEvent {

    private final OrderId orderId;
    private final String reason;

    public OrderCancelledEvent(OrderId orderId, String reason) {
        super();
        this.orderId = orderId;
        this.reason = reason;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "OrderCancelledEvent{" +
                "orderId=" + orderId +
                ", reason='" + reason + '\'' +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}
