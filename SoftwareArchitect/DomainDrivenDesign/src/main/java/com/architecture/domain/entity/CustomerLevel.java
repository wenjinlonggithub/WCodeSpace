package com.architecture.domain.entity;

public enum CustomerLevel {
    NORMAL("普通会员", 1.0),
    VIP("VIP会员", 0.95),
    SVIP("超级VIP", 0.9);

    private final String description;
    private final double discount;

    CustomerLevel(String description, double discount) {
        this.description = description;
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public double getDiscount() {
        return discount;
    }
}
