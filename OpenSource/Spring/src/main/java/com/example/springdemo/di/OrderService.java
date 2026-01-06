package com.example.springdemo.di;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单服务
 * 
 * 演示Spring依赖注入在业务服务中的应用
 * 展示多个依赖的协同工作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    // 通过构造器注入多个依赖
    private final PaymentService paymentService; // 由于CreditCardPaymentService有@Primary，会自动注入
    
    @Qualifier("emailNotificationService")
    private final NotificationService emailNotificationService;
    
    @Qualifier("smsNotificationService") 
    private final NotificationService smsNotificationService;
    
    // 内存存储订单（演示用）
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private static int orderCounter = 1;
    
    /**
     * 创建订单
     */
    public Order createOrder(String userId, double amount) {
        String orderId = "ORD-" + String.format("%04d", orderCounter++);
        Order order = new Order(orderId, userId, amount);
        
        orders.put(orderId, order);
        log.info("📝 创建订单: {}", order);
        
        return order;
    }
    
    /**
     * 处理订单支付
     */
    public boolean processOrderPayment(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            log.error("❌ 订单不存在: {}", orderId);
            return false;
        }
        
        log.info("💰 开始处理订单支付: {}", orderId);
        
        // 使用注入的支付服务处理支付
        boolean paymentSuccess = paymentService.processPayment(
                order.getAmount(), 
                "订单支付: " + orderId
        );
        
        if (paymentSuccess) {
            order.setStatus(Order.OrderStatus.PAID);
            orders.put(orderId, order);
            log.info("✅ 订单支付成功: {}", orderId);
        } else {
            log.error("❌ 订单支付失败: {}", orderId);
        }
        
        return paymentSuccess;
    }
    
    /**
     * 发送订单确认通知
     */
    public void sendOrderConfirmation(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            log.error("❌ 订单不存在: {}", orderId);
            return;
        }
        
        String title = "订单确认";
        String content = String.format(
                "您的订单 %s 已确认，金额：¥%.2f，感谢您的购买！", 
                orderId, order.getAmount()
        );
        
        log.info("📢 发送订单确认通知: {}", orderId);
        
        // 同时发送邮件和短信通知
        emailNotificationService.sendNotification(title, content);
        smsNotificationService.sendNotification(title, content);
        
        // 更新订单状态
        order.setStatus(Order.OrderStatus.COMPLETED);
        orders.put(orderId, order);
        log.info("🎉 订单处理完成: {}", orderId);
    }
    
    /**
     * 获取订单信息
     */
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }
    
    /**
     * 取消订单
     */
    public boolean cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            log.error("❌ 订单不存在: {}", orderId);
            return false;
        }
        
        if (order.getStatus() == Order.OrderStatus.PAID) {
            log.error("❌ 已支付订单不能取消: {}", orderId);
            return false;
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        orders.put(orderId, order);
        log.info("🚫 订单已取消: {}", orderId);
        
        return true;
    }
}