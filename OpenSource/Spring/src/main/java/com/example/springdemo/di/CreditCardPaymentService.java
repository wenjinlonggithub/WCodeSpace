package com.example.springdemo.di;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 信用卡支付服务实现
 * 
 * 使用@Primary注解标记为主要实现
 * 演示Spring依赖注入的优先选择机制
 */
@Slf4j
@Service("creditCardPaymentService")
@Primary
public class CreditCardPaymentService implements PaymentService {
    
    @Override
    public boolean processPayment(double amount, String description) {
        log.info("💳 信用卡支付处理:");
        log.info("   金额: ¥{}", amount);
        log.info("   描述: {}", description);
        log.info("   手续费: ¥{}", amount * getFeeRate());
        
        // 模拟支付处理
        try {
            Thread.sleep(200); // 模拟网络请求延迟
            log.info("   状态: 支付成功");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("   状态: 支付失败 - {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getServiceType() {
        return "CREDIT_CARD";
    }
    
    @Override
    public double getFeeRate() {
        return 0.025; // 2.5% 手续费
    }
}