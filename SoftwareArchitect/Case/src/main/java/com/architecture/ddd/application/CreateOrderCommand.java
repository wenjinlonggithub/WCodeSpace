package com.architecture.ddd.application;

import com.architecture.ddd.domain.model.Address;
import com.architecture.ddd.domain.model.CustomerId;
import com.architecture.ddd.domain.model.ProductId;
import com.architecture.ddd.domain.model.Quantity;

import java.util.List;

/**
 * DDD - 创建订单命令
 */
public class CreateOrderCommand {

    private final CustomerId customerId;
    private final Address shippingAddress;
    private final List<OrderItemData> items;

    public CreateOrderCommand(CustomerId customerId, Address shippingAddress, List<OrderItemData> items) {
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
        this.items = items;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItemData> getItems() {
        return items;
    }

    public static class OrderItemData {
        private final ProductId productId;
        private final Quantity quantity;

        public OrderItemData(ProductId productId, Quantity quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public ProductId getProductId() {
            return productId;
        }

        public Quantity getQuantity() {
            return quantity;
        }
    }
}
