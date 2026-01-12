package com.architecture.ddd.domain.model;

/**
 * DDD - 商品实体
 */
public class Product {

    private ProductId id;
    private String name;
    private Money price;
    private String description;

    private Product() {
    }

    public static Product create(ProductId id, String name, Money price) {
        Product product = new Product();
        product.id = id;
        product.name = name;
        product.price = price;
        return product;
    }

    public ProductId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
