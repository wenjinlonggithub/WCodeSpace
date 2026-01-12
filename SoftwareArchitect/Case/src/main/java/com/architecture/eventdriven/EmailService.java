package com.architecture.eventdriven;

/**
 * 邮件服务 - 事件消费者
 * 监听订单事件并发送邮件
 */
public class EmailService {

    /**
     * 发送订单确认邮件
     */
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        System.out.println("  [EmailService] 发送订单确认邮件");
        System.out.println("    收件人: user-" + event.getUserId() + "@example.com");
        System.out.println("    订单号: " + event.getOrderId());
        System.out.println("    金额: " + event.getTotalAmount());
    }

    /**
     * 发送支付成功邮件
     */
    public void sendPaymentConfirmation(OrderPaidEvent event) {
        System.out.println("  [EmailService] 发送支付成功邮件");
        System.out.println("    订单号: " + event.getOrderId());
        System.out.println("    支付金额: " + event.getAmount());
    }
}
