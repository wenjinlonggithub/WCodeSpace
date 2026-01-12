package com.architecture.cqrs.query;

/**
 * CQRS - 订单查询
 *
 * 查询特点:
 * 1. 表示要查询的数据
 * 2. 不修改数据
 * 3. 返回DTO
 */
public class OrderQuery {

    private final String orderId;

    public OrderQuery(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
