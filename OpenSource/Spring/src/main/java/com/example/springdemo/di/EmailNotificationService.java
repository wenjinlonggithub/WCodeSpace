package com.example.springdemo.di;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邮件通知服务实现
 * 
 * 演示Spring依赖注入的具体实现类
 */
@Slf4j
@Service("emailNotificationService")
public class EmailNotificationService implements NotificationService {
    
    @Override
    public void sendNotification(String title, String content) {
        log.info("📧 发送邮件通知:");
        log.info("   标题: {}", title);
        log.info("   内容: {}", content);
        log.info("   状态: 邮件发送成功");
        
        // 模拟邮件发送延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String getServiceType() {
        return "EMAIL";
    }
}