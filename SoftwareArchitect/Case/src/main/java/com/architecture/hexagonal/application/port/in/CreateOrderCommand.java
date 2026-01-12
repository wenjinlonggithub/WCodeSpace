package com.architecture.hexagonal.application.port.in;

import com.architecture.hexagonal.domain.CustomerId;
import com.architecture.hexagonal.domain.ProductId;

/**
 * 创建订单命令
 */
public class CreateOrderCommand {

    private final CustomerId customerId;
    private final ProductId productId;
    private final int quantity;

    public CreateOrderCommand(CustomerId customerId, ProductId productId, int quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
