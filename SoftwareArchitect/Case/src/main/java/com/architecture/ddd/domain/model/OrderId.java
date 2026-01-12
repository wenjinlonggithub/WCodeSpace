package com.architecture.ddd.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * DDD - 订单ID值对象
 *
 * 值对象特点:
 * 1. 不可变(Immutable)
 * 2. 通过属性值判断相等性
 * 3. 封装相关的业务规则
 */
public class OrderId {

    private final String value;

    private OrderId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        this.value = value;
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId)) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
