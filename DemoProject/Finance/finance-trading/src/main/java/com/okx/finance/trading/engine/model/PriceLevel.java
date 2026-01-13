package com.okx.finance.trading.engine.model;

import com.okx.finance.common.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 价格层级 - 同一价格的订单队列
 */
@Data
public class PriceLevel {
    private BigDecimal price;
    private Queue<Order> orders;
    private BigDecimal totalQuantity;

    public PriceLevel(BigDecimal price) {
        this.price = price;
        this.orders = new LinkedList<>();
        this.totalQuantity = BigDecimal.ZERO;
    }

    public void addOrder(Order order) {
        orders.offer(order);
        BigDecimal remainingQuantity = order.getQuantity().subtract(order.getExecutedQuantity());
        totalQuantity = totalQuantity.add(remainingQuantity);
    }

    public Order peekOrder() {
        return orders.peek();
    }

    public Order pollOrder() {
        Order order = orders.poll();
        if (order != null) {
            BigDecimal remainingQuantity = order.getQuantity().subtract(order.getExecutedQuantity());
            totalQuantity = totalQuantity.subtract(remainingQuantity);
        }
        return order;
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public int size() {
        return orders.size();
    }

    public void removeOrder(Order order) {
        if (orders.remove(order)) {
            BigDecimal remainingQuantity = order.getQuantity().subtract(order.getExecutedQuantity());
            totalQuantity = totalQuantity.subtract(remainingQuantity);
        }
    }
}
