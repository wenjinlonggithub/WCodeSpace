package com.example.springdemo.di;

/**
 * 通知服务接口
 * 
 * 用于演示Spring多实现选择和@Qualifier的使用
 */
public interface NotificationService {
    
    /**
     * 发送通知
     * 
     * @param title 通知标题
     * @param content 通知内容
     */
    void sendNotification(String title, String content);
    
    /**
     * 获取通知服务类型
     */
    String getServiceType();
}