package com.example.springdemo.di;

/**
 * 支付服务接口
 * 
 * 用于演示Spring依赖注入和@Primary注解的使用
 */
public interface PaymentService {
    
    /**
     * 处理支付
     * 
     * @param amount 支付金额
     * @param description 支付描述
     * @return 支付是否成功
     */
    boolean processPayment(double amount, String description);
    
    /**
     * 获取支付服务类型
     */
    String getServiceType();
    
    /**
     * 获取支付手续费率
     */
    double getFeeRate();
}