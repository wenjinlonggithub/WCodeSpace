package com.example.springdemo.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 事件监听器
 * 
 * 演示Spring事件监听的各种方式
 */
@Slf4j
@Component
public class EventListeners {
    
    // 同步监听用户注册事件
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("🎯 [同步] 用户注册事件处理:");
        log.info("   用户ID: {}", event.getUserId());
        log.info("   用户名: {}", event.getUsername());
        log.info("   邮箱: {}", event.getEmail());
        
        // 模拟业务处理
        try {
            Thread.sleep(100);
            log.info("   ✅ 用户权限初始化完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 异步监听用户注册事件
    @Async
    @EventListener
    public void handleUserRegisteredAsync(UserRegisteredEvent event) {
        log.info("🚀 [异步] 用户注册事件处理:");
        log.info("   发送欢迎邮件到: {}", event.getEmail());
        
        // 模拟邮件发送延迟
        try {
            Thread.sleep(300);
            log.info("   ✅ 欢迎邮件发送成功");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 监听订单创建事件
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("🎯 订单创建事件处理:");
        log.info("   订单ID: {}", event.getOrderId());
        log.info("   用户ID: {}", event.getUserId());
        log.info("   金额: ¥{}", event.getAmount());
        
        // 业务处理
        log.info("   📋 订单详情记录完成");
        log.info("   📊 更新用户积分");
    }
    
    // 监听支付完成事件
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("🎯 支付完成事件处理:");
        log.info("   订单ID: {}", event.getOrderId());
        log.info("   金额: ¥{}", event.getAmount());
        log.info("   支付方式: {}", event.getPaymentMethod());
        
        // 业务处理
        log.info("   💰 财务记账处理");
        log.info("   📦 触发发货流程");
        log.info("   🎁 发放购买积分");
    }
    
    // 异步监听支付完成事件 - 发送通知
    @Async
    @EventListener
    public void handlePaymentCompletedNotification(PaymentCompletedEvent event) {
        log.info("🚀 [异步] 支付完成通知处理:");
        
        try {
            Thread.sleep(200);
            log.info("   📱 短信通知发送成功");
            
            Thread.sleep(150);
            log.info("   📧 邮件通知发送成功");
            
            log.info("   ✅ 所有通知发送完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}