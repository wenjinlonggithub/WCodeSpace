package com.architecture.consistency.eventsourcing;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单取消事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends DomainEvent {

    private String reason;

    public OrderCancelledEvent() {
        super();
        setEventType("OrderCancelled");
    }

    public OrderCancelledEvent(String orderId, String reason, LocalDateTime occurredAt) {
        super(null, orderId, "OrderCancelled", null, occurredAt);
        this.reason = reason;
    }

    @Override
    public void applyTo(Object aggregate) {
        OrderAggregate order = (OrderAggregate) aggregate;
        order.setStatus(OrderAggregate.OrderStatus.CANCELLED);
        order.setVersion(getVersion());
    }
}
