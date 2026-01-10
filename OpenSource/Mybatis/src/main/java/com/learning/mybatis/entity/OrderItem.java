package com.learning.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 * 
 * 演示多层关联映射
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    /**
     * 订单项ID
     */
    private Long id;
    
    /**
     * 订单ID（外键）
     */
    private Long orderId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
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
     * 小计
     */
    private BigDecimal totalPrice;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 关联的订单对象（多对一）
     */
    private Order order;
    
    /**
     * 计算小计
     */
    public BigDecimal calculateTotalPrice() {
        if (quantity == null || price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * 验证数据完整性
     */
    public boolean isValid() {
        return orderId != null && 
               productId != null && 
               quantity != null && quantity > 0 &&
               price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 获取格式化的价格字符串
     */
    public String getFormattedPrice() {
        return price != null ? "¥" + price.toString() : "¥0.00";
    }
    
    /**
     * 获取格式化的小计字符串
     */
    public String getFormattedTotalPrice() {
        BigDecimal total = totalPrice != null ? totalPrice : calculateTotalPrice();
        return "¥" + total.toString();
    }
}