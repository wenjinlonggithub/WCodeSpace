package com.architecture.domain.entity;

import com.architecture.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Product {
    private Long id;
    private String name;
    private String description;
    private Money price;
    private int stock;
    private ProductCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product(Long id, String name, String description, Money price, int stock, ProductCategory category) {
        if (stock < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return stock > 0;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }
        if (stock < quantity) {
            throw new IllegalStateException("库存不足");
        }
        this.stock -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("增加数量必须大于0");
        }
        this.stock += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePrice(Money newPrice) {
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
