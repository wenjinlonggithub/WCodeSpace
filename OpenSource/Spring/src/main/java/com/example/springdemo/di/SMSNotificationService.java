package com.example.springdemo.di;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信通知服务实现
 * 
 * 演示Spring依赖注入的另一个实现类
 */
@Slf4j
@Service("smsNotificationService")
public class SMSNotificationService implements NotificationService {
    
    @Override
    public void sendNotification(String title, String content) {
        log.info("📱 发送短信通知:");
        log.info("   标题: {}", title);
        log.info("   内容: {}", content);
        log.info("   状态: 短信发送成功");
        
        // 模拟短信发送延迟
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String getServiceType() {
        return "SMS";
    }
}