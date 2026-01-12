package com.architecture.ddd.domain.model;

/**
 * DDD - 订单状态枚举
 */
public enum OrderStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交"),
    PAID("已支付"),
    SHIPPED("已发货"),
    DELIVERED("已送达"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isSubmitted() {
        return this == SUBMITTED;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isShipped() {
        return this == SHIPPED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}
