package com.architecture.consistency.eventsourcing;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单支付事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPaidEvent extends DomainEvent {

    public OrderPaidEvent() {
        super();
        setEventType("OrderPaid");
    }

    public OrderPaidEvent(String orderId, LocalDateTime occurredAt) {
        super(null, orderId, "OrderPaid", null, occurredAt);
    }

    @Override
    public void applyTo(Object aggregate) {
        OrderAggregate order = (OrderAggregate) aggregate;
        order.setStatus(OrderAggregate.OrderStatus.PAID);
        order.setVersion(getVersion());
    }
}
