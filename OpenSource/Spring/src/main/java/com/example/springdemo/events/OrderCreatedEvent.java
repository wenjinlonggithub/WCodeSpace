package com.example.springdemo.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订单创建事件
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    
    private final String orderId;
    private final String userId;
    private final double amount;
    
    public OrderCreatedEvent(Object source, String orderId, String userId, double amount) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }
}