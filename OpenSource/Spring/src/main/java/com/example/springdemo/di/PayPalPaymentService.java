package com.example.springdemo.di;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * PayPal支付服务实现
 * 
 * 演示Spring依赖注入的备用实现
 */
@Slf4j
@Service("paypalPaymentService")
public class PayPalPaymentService implements PaymentService {
    
    @Override
    public boolean processPayment(double amount, String description) {
        log.info("🅿️ PayPal支付处理:");
        log.info("   金额: ¥{}", amount);
        log.info("   描述: {}", description);
        log.info("   手续费: ¥{}", amount * getFeeRate());
        
        // 模拟支付处理
        try {
            Thread.sleep(150); // 模拟网络请求延迟
            log.info("   状态: PayPal支付成功");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("   状态: PayPal支付失败 - {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getServiceType() {
        return "PAYPAL";
    }
    
    @Override
    public double getFeeRate() {
        return 0.035; // 3.5% 手续费
    }
}