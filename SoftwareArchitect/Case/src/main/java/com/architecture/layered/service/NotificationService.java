package com.architecture.layered.service;

/**
 * 通知服务
 */
public class NotificationService {

    /**
     * 发送订单确认通知
     */
    public void sendOrderConfirmation(Long userId, Long orderId) {
        System.out.println("发送订单确认通知 - 用户: " + userId + ", 订单: " + orderId);
        // 实际实现: 发送邮件、短信、推送等
    }

    /**
     * 发送订单取消通知
     */
    public void sendOrderCancellation(Long userId, Long orderId) {
        System.out.println("发送订单取消通知 - 用户: " + userId + ", 订单: " + orderId);
    }

    /**
     * 发送发货通知
     */
    public void sendShippingNotification(Long orderId) {
        System.out.println("发送发货通知 - 订单: " + orderId);
    }
}
