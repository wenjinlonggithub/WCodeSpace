package com.architecture.ddd.domain.model;

/**
 * DDD - 订单项实体
 *
 * 聚合内部实体，不能独立存在
 */
public class OrderItem {

    private ProductId productId;
    private String productName;
    private Money unitPrice;
    private Quantity quantity;

    private OrderItem() {
    }

    public static OrderItem create(Product product, Quantity quantity) {
        OrderItem item = new OrderItem();
        item.productId = product.getId();
        item.productName = product.getName();
        item.unitPrice = product.getPrice();
        item.quantity = quantity;
        return item;
    }

    /**
     * 增加数量
     */
    public void increaseQuantity(Quantity additionalQuantity) {
        this.quantity = this.quantity.add(additionalQuantity);
    }

    /**
     * 计算小计
     */
    public Money getSubtotal() {
        return unitPrice.multiply(quantity.getValue());
    }

    // Getters
    public ProductId getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Quantity getQuantity() {
        return quantity;
    }
}
