package com.architecture.layered.controller;

import com.architecture.layered.dto.CreateOrderRequest;
import com.architecture.layered.dto.OrderDTO;
import com.architecture.layered.entity.Order;
import com.architecture.layered.service.OrderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表现层 - 订单控制器
 * 职责: 处理HTTP请求、参数验证、调用业务层、返回响应
 */
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     */
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. 参数验证
        validateRequest(request);

        // 2. 调用业务层
        Order order = orderService.createOrder(
            request.getUserId(),
            request.getProductId(),
            request.getQuantity(),
            request.getShippingAddress()
        );

        // 3. 转换为DTO返回
        return convertToDTO(order);
    }

    /**
     * 获取订单详情
     */
    public OrderDTO getOrder(Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return convertToDTO(order);
    }

    /**
     * 获取用户订单列表
     */
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long orderId) {
        orderService.cancelOrder(orderId);
    }

    /**
     * 参数验证
     */
    private void validateRequest(CreateOrderRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().isEmpty()) {
            throw new IllegalArgumentException("收货地址不能为空");
        }
    }

    /**
     * Entity转DTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setProductId(order.getProductId());
        dto.setProductName(order.getProductName());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
