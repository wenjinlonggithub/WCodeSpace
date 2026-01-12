package com.architecture.cqrs.event;

import com.architecture.cqrs.command.CreateOrderCommand;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CQRS - 订单创建事件
 *
 * 用于同步读写模型
 */
public class OrderCreatedEvent {

    private final String orderId;
    private final String userId;
    private final BigDecimal totalAmount;
    private final List<CreateOrderCommand.OrderItemData> items;
    private final String shippingAddress;
    private final LocalDateTime occurredOn;

    public OrderCreatedEvent(
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<CreateOrderCommand.OrderItemData> items,
        String shippingAddress,
        LocalDateTime occurredOn
    ) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.occurredOn = occurredOn;
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

    public List<CreateOrderCommand.OrderItemData> getItems() {
        return items;
    }

    public String getShippingAddress() {
        return shippingAddress;
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
                ", itemCount=" + items.size() +
                '}';
    }
}
