package com.example.springdemo.di;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * 用于演示依赖注入的业务对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    private String id;
    private String userId;
    private double amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Order(String id, String userId, double amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum OrderStatus {
        CREATED,    // 已创建
        PAID,       // 已支付
        CANCELLED,  // 已取消
        COMPLETED   // 已完成
    }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', userId='%s', amount=%.2f, status=%s}", 
                id, userId, amount, status);
    }
}