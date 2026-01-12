package com.architecture.hexagonal.domain;

/**
 * 订单项 - 聚合内部实体
 */
public class OrderItem {

    private final ProductId productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(ProductId productId, String productName, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }

    public ProductId getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}
