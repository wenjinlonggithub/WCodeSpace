package com.architecture.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * 业务说明:
 * - 订单是整个电商系统的核心业务对象
 * - 一个订单包含用户、商品、数量、金额等信息
 * - 订单有多个状态: 待支付、已支付、已发货、已完成、已取消
 */
public class Order {

    /** 订单ID */
    private Long id;

    /** 订单号（唯一） */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 购买数量 */
    private Integer quantity;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 支付ID */
    private Long paymentId;

    /** 订单状态 */
    private OrderStatus status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 订单备注 */
    private String remark;

    // ==================== 构造方法 ====================

    public Order() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public Order(Long userId, Long productId, Integer quantity, BigDecimal totalAmount) {
        this();
        this.orderNo = generateOrderNo();
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    // ==================== 业务方法 ====================

    /**
     * 生成订单号
     * 格式: ORD + 时间戳 + 随机数
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() +
               String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 订单支付
     */
    public void pay(Long paymentId) {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("订单状态不正确，无法支付");
        }
        this.paymentId = paymentId;
        this.status = OrderStatus.PAID;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 订单发货
     */
    public void ship() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("订单未支付，无法发货");
        }
        this.status = OrderStatus.SHIPPED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 订单完成
     */
    public void complete() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("订单未发货，无法完成");
        }
        this.status = OrderStatus.COMPLETED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 订单取消
     */
    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("订单已完成，无法取消");
        }
        this.status = OrderStatus.CANCELLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 计算订单金额
     */
    public BigDecimal calculateAmount(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(this.quantity));
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
