package com.architecture.order.domain;

/**
 * 订单状态枚举
 *
 * 状态流转:
 * PENDING_PAYMENT(待支付) → PAID(已支付) → SHIPPED(已发货) → COMPLETED(已完成)
 *                     ↓
 *                CANCELLED(已取消)
 */
public enum OrderStatus {

    /** 待支付 - 订单创建后的初始状态 */
    PENDING_PAYMENT("待支付", "订单已创建，等待用户支付"),

    /** 已支付 - 用户完成支付 */
    PAID("已支付", "用户已完成支付，等待商家发货"),

    /** 已发货 - 商家已发货 */
    SHIPPED("已发货", "商家已发货，等待用户确认收货"),

    /** 已完成 - 交易完成 */
    COMPLETED("已完成", "用户已确认收货，交易完成"),

    /** 已取消 - 订单取消 */
    CANCELLED("已取消", "订单已取消");

    private final String desc;
    private final String detail;

    OrderStatus(String desc, String detail) {
        this.desc = desc;
        this.detail = detail;
    }

    public String getDesc() {
        return desc;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING_PAYMENT || this == PAID;
    }

    /**
     * 判断是否已完成
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }
}
