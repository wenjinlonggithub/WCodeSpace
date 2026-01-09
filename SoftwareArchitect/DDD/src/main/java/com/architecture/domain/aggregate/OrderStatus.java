package com.architecture.domain.aggregate;

public enum OrderStatus {
    PENDING("待处理"),
    CONFIRMED("已确认"),
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
}