package com.architecture.consistency.eventsourcing;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单创建事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends DomainEvent {

    private String userId;
    private String productName;
    private BigDecimal amount;

    public OrderCreatedEvent() {
        super();
        setEventType("OrderCreated");
    }

    public OrderCreatedEvent(String orderId, String userId, String productName,
                           BigDecimal amount, LocalDateTime occurredAt) {
        super(null, orderId, "OrderCreated", null, occurredAt);
        this.userId = userId;
        this.productName = productName;
        this.amount = amount;
    }

    @Override
    public void applyTo(Object aggregate) {
        OrderAggregate order = (OrderAggregate) aggregate;
        order.setOrderId(getAggregateId());
        order.setUserId(userId);
        order.setProductName(productName);
        order.setAmount(amount);
        order.setStatus(OrderAggregate.OrderStatus.CREATED);
        order.setVersion(getVersion());
    }
}
