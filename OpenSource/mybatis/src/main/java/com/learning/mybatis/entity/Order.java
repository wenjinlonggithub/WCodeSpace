package com.learning.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 * 
 * 演示一对多关联映射
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID（外键）
     */
    private Long userId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 关联的用户对象（多对一）
     */
    private User user;
    
    /**
     * 订单项列表（一对多）
     */
    private List<OrderItem> orderItems;
    
    /**
     * 订单状态枚举
     */
    public enum Status {
        PENDING("PENDING", "待支付"),
        PAID("PAID", "已支付"),
        SHIPPED("SHIPPED", "已发货"),
        COMPLETED("COMPLETED", "已完成"),
        CANCELLED("CANCELLED", "已取消");
        
        private final String code;
        private final String desc;
        
        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() { return code; }
        public String getDesc() { return desc; }
        
        public static Status fromCode(String code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status orderStatus = Status.fromCode(this.status);
        return orderStatus != null ? orderStatus.getDesc() : "未知状态";
    }
    
    /**
     * 判断订单是否可以取消
     */
    public boolean canCancel() {
        return Status.PENDING.getCode().equals(this.status) || 
               Status.PAID.getCode().equals(this.status);
    }
    
    /**
     * 判断订单是否已完成
     */
    public boolean isCompleted() {
        return Status.COMPLETED.getCode().equals(this.status);
    }
    
    /**
     * 判断订单是否已取消
     */
    public boolean isCancelled() {
        return Status.CANCELLED.getCode().equals(this.status);
    }
    
    /**
     * 计算订单总价（如果有订单项的话）
     */
    public BigDecimal calculateTotalFromItems() {
        if (orderItems == null || orderItems.isEmpty()) {
            return totalAmount != null ? totalAmount : BigDecimal.ZERO;
        }
        
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 获取订单项数量
     */
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }
}