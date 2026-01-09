package com.architecture.domain.aggregate;

import com.architecture.domain.entity.Product;
import com.architecture.domain.valueobject.Money;
import java.util.Objects;

public class OrderItem {
    private Product product;
    private int quantity;
    private Money unitPrice;
    private Money subTotal;

    public OrderItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.subTotal = unitPrice.multiply(quantity);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(product, orderItem.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product);
    }
}