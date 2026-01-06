package com.example.springdemo.di;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 依赖注入演示服务
 * 
 * 演示Spring依赖注入的各种方式：
 * 1. 构造器注入 (推荐)
 * 2. Setter方法注入
 * 3. 字段注入
 * 4. 多实现选择 (@Qualifier, @Primary)
 * 5. 循环依赖处理
 */
@Slf4j
@Service
public class DIDemoService {
    
    // 字段注入演示
    @Autowired
    @Qualifier("emailNotificationService")
    private NotificationService emailNotification;
    
    // 构造器注入 - Spring 4.3+可以省略@Autowired
    private final PaymentService primaryPaymentService;
    private final OrderService orderService;
    
    // Setter注入演示
    private NotificationService smsNotification;
    
    public DIDemoService(
            @Qualifier("creditCardPaymentService") PaymentService primaryPaymentService,
            OrderService orderService) {
        this.primaryPaymentService = primaryPaymentService;
        this.orderService = orderService;
        log.info("🔧 构造器注入完成: PaymentService={}, OrderService={}", 
                primaryPaymentService.getClass().getSimpleName(),
                orderService.getClass().getSimpleName());
    }
    
    @Autowired
    @Qualifier("smsNotificationService")
    public void setSmsNotification(NotificationService smsNotification) {
        this.smsNotification = smsNotification;
        log.info("🔧 Setter注入完成: SMSNotificationService");
    }
    
    /**
     * 演示依赖注入功能
     */
    public void demonstrateDI() {
        log.info("🔍 依赖注入演示开始...");
        
        // 1. 演示构造器注入的服务
        demonstrateConstructorInjection();
        
        // 2. 演示字段注入的服务
        demonstrateFieldInjection();
        
        // 3. 演示Setter注入的服务
        demonstrateSetterInjection();
        
        // 4. 演示多实现选择
        demonstrateQualifierUsage();
        
        // 5. 演示完整的业务流程
        demonstrateBusinessFlow();
        
        log.info("✅ 依赖注入演示完成");
    }
    
    /**
     * 演示构造器注入
     */
    private void demonstrateConstructorInjection() {
        log.info("\n🏗️ 构造器注入演示：");
        log.info("主要支付服务类型: {}", primaryPaymentService.getClass().getSimpleName());
        
        // 使用注入的支付服务
        boolean result = primaryPaymentService.processPayment(100.0, "构造器注入测试");
        log.info("支付处理结果: {}", result ? "成功" : "失败");
    }
    
    /**
     * 演示字段注入
     */
    private void demonstrateFieldInjection() {
        log.info("\n📧 字段注入演示：");
        log.info("邮件通知服务类型: {}", emailNotification.getClass().getSimpleName());
        
        // 使用注入的邮件服务
        emailNotification.sendNotification("字段注入测试", "这是通过字段注入获取的邮件服务发送的消息");
    }
    
    /**
     * 演示Setter注入
     */
    private void demonstrateSetterInjection() {
        log.info("\n📱 Setter注入演示：");
        log.info("短信通知服务类型: {}", smsNotification.getClass().getSimpleName());
        
        // 使用注入的短信服务
        smsNotification.sendNotification("Setter注入测试", "这是通过Setter注入获取的短信服务发送的消息");
    }
    
    /**
     * 演示@Qualifier的使用
     */
    private void demonstrateQualifierUsage() {
        log.info("\n🎯 @Qualifier使用演示：");
        log.info("通过@Qualifier指定的邮件服务: {}", emailNotification.getClass().getSimpleName());
        log.info("通过@Qualifier指定的短信服务: {}", smsNotification.getClass().getSimpleName());
        log.info("通过@Qualifier指定的支付服务: {}", primaryPaymentService.getClass().getSimpleName());
        
        // 演示不同实现的行为差异
        emailNotification.sendNotification("Qualifier测试", "邮件通知内容");
        smsNotification.sendNotification("Qualifier测试", "短信通知内容");
    }
    
    /**
     * 演示完整的业务流程
     */
    private void demonstrateBusinessFlow() {
        log.info("\n🔄 完整业务流程演示：");
        
        // 创建订单，演示依赖注入链
        Order order = orderService.createOrder("用户123", 299.99);
        log.info("创建的订单: {}", order);
        
        // 处理支付
        boolean paymentResult = orderService.processOrderPayment(order.getId());
        log.info("订单支付结果: {}", paymentResult ? "成功" : "失败");
        
        // 发送通知
        if (paymentResult) {
            orderService.sendOrderConfirmation(order.getId());
        }
    }
}