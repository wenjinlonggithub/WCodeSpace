package com.architecture.domain.entity;

public enum ProductCategory {
    ELECTRONICS("电子产品"),
    CLOTHING("服装"),
    FOOD("食品"),
    BOOKS("图书"),
    HOME("家居"),
    SPORTS("运动");

    private final String description;

    ProductCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
