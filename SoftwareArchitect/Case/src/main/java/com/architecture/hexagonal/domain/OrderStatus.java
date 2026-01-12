package com.architecture.hexagonal.domain;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    DRAFT,          // 草稿
    SUBMITTED,      // 已提交
    PAID,           // 已支付
    SHIPPED,        // 已发货
    DELIVERED,      // 已送达
    CANCELLED       // 已取消
}
