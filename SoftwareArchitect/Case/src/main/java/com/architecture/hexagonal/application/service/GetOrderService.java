package com.architecture.hexagonal.application.service;

import com.architecture.hexagonal.application.port.in.GetOrderUseCase;
import com.architecture.hexagonal.application.port.out.OrderRepository;
import com.architecture.hexagonal.domain.Order;
import com.architecture.hexagonal.domain.OrderId;

/**
 * 应用服务 - 获取订单用例实现
 */
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
    }
}
