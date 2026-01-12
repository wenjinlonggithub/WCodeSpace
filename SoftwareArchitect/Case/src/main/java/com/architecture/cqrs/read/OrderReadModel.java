package com.architecture.cqrs.read;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CQRS - 读模型 (非规范化设计)
 *
 * 读模型特点:
 * 1. 非规范化设计
 * 2. 冗余数据以优化查询
 * 3. 可能有多个读模型
 */
public class OrderReadModel {

    private String id;
    private String userId;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private int itemCount; // 冗余:商品数量
    private String productNames; // 冗余:商品名称列表
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
