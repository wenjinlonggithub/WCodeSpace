package com.example.springdemo.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务服务类
 * 
 * 作为AOP演示的目标类，包含各种业务方法
 */
@Slf4j
@Service
public class BusinessService {
    
    /**
     * 处理订单 - 演示正常业务流程
     */
    public String processOrder(String orderId, double amount) {
        log.info("🛒 处理订单: {} 金额: ¥{}", orderId, amount);
        
        // 模拟业务处理
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "订单处理成功: " + orderId;
    }
    
    /**
     * 处理退款 - 演示异常情况
     */
    public String processRefund(String orderId, double amount) {
        log.info("💰 处理退款: {} 金额: ¥{}", orderId, amount);
        
        if (amount <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        
        if ("ORD-999".equals(orderId)) {
            throw new RuntimeException("订单不存在: " + orderId);
        }
        
        return "退款处理成功: " + orderId;
    }
    
    /**
     * 复杂计算 - 演示性能监控
     */
    public long complexCalculation(int iterations) {
        log.info("🧮 执行复杂计算，迭代次数: {}", iterations);
        
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += Math.sqrt(i) * Math.sin(i);
            
            // 模拟CPU密集型操作
            if (i % 100 == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.info("计算完成，结果: {}", sum);
        return sum;
    }
    
    /**
     * 查询用户信息 - 演示缓存切面
     */
    public String getUserInfo(String userId) {
        log.info("📋 查询用户信息: {}", userId);
        
        // 模拟数据库查询延迟
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "用户信息: " + userId;
    }
    
    /**
     * 更新用户信息 - 演示事务切面
     */
    public boolean updateUserInfo(String userId, String info) {
        log.info("✏️  更新用户信息: {} -> {}", userId, info);
        
        // 模拟数据库更新
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        return true;
    }
}