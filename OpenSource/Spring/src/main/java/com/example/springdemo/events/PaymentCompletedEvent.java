package com.example.springdemo.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 支付完成事件
 */
@Getter
public class PaymentCompletedEvent extends ApplicationEvent {
    
    private final String orderId;
    private final double amount;
    private final String paymentMethod;
    
    public PaymentCompletedEvent(Object source, String orderId, double amount, String paymentMethod) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}