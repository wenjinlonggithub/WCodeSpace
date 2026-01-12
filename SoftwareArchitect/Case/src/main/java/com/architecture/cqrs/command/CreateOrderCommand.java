package com.architecture.cqrs.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * CQRS - 创建订单命令
 *
 * 命令特点:
 * 1. 表示要执行的操作
 * 2. 包含执行操作所需的所有数据
 * 3. 可以被验证和拒绝
 */
public class CreateOrderCommand {

    private final String userId;
    private final List<OrderItemData> items;
    private final String shippingAddress;

    public CreateOrderCommand(String userId, List<OrderItemData> items, String shippingAddress) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderItemData> getItems() {
        return items;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public static class OrderItemData {
        private final String productId;
        private final int quantity;
        private final BigDecimal unitPrice;

        public OrderItemData(String productId, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
    }
}
