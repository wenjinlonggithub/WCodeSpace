package com.example.springdemo.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 事件机制演示服务
 * 
 * 演示Spring事件驱动编程：
 * 1. 自定义事件发布
 * 2. 事件监听
 * 3. 异步事件处理
 * 4. 事件传播
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventsDemoService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void demonstrateEvents() {
        log.info("🔍 事件机制演示开始...");
        
        // 1. 发布用户注册事件
        publishUserRegistrationEvent();
        
        // 2. 发布订单创建事件  
        publishOrderCreatedEvent();
        
        // 3. 发布支付完成事件
        publishPaymentCompletedEvent();
        
        // 等待异步事件处理完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ 事件机制演示完成");
    }
    
    private void publishUserRegistrationEvent() {
        log.info("\n👤 发布用户注册事件...");
        
        UserRegisteredEvent event = new UserRegisteredEvent(
                this, "user123", "张三", "zhangsan@example.com"
        );
        
        eventPublisher.publishEvent(event);
        log.info("📢 用户注册事件已发布");
    }
    
    private void publishOrderCreatedEvent() {
        log.info("\n🛒 发布订单创建事件...");
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                this, "ORD-001", "user123", 299.99
        );
        
        eventPublisher.publishEvent(event);
        log.info("📢 订单创建事件已发布");
    }
    
    private void publishPaymentCompletedEvent() {
        log.info("\n💰 发布支付完成事件...");
        
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                this, "ORD-001", 299.99, "CREDIT_CARD"
        );
        
        eventPublisher.publishEvent(event);
        log.info("📢 支付完成事件已发布");
    }
}