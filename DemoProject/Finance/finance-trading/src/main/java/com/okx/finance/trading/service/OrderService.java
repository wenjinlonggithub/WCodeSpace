package com.okx.finance.trading.service;

import com.okx.finance.common.constant.OrderSide;
import com.okx.finance.common.constant.OrderStatus;
import com.okx.finance.common.dto.Result;
import com.okx.finance.common.entity.Order;
import com.okx.finance.common.util.JwtUtil;
import com.okx.finance.common.util.SnowflakeIdGenerator;
import com.okx.finance.trading.dto.PlaceOrderRequest;
import com.okx.finance.trading.engine.MatchingEngine;
import com.okx.finance.trading.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MatchingEngine matchingEngine;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(3);

    @Transactional
    public Result<?> placeOrder(String token, PlaceOrderRequest request) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);

        Order order = new Order();
        order.setId(idGenerator.nextId());
        order.setUserId(userId);
        order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        order.setSymbol(request.getSymbol());
        order.setOrderType(request.getOrderType());
        order.setSide(request.getSide());
        order.setPrice(new BigDecimal(request.getPrice()));
        order.setQuantity(new BigDecimal(request.getQuantity()));
        order.setExecutedQuantity(BigDecimal.ZERO);
        order.setExecutedAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.NEW);
        order.setTimeInForce(request.getTimeInForce());

        orderMapper.insert(order);

        matchingEngine.submitOrder(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("status", order.getStatus());

        return Result.success(result);
    }

    @Transactional
    public Result<?> cancelOrder(String token, String orderId) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Order order = orderMapper.findByOrderId(orderId);

        if (order == null) {
            return Result.error("Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            return Result.error("Unauthorized");
        }

        if (!OrderStatus.NEW.equals(order.getStatus()) &&
            !OrderStatus.PARTIALLY_FILLED.equals(order.getStatus())) {
            return Result.error("Order cannot be canceled");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderMapper.update(order);

        matchingEngine.cancelOrder(order);

        return Result.success("Order canceled successfully");
    }

    public Result<?> getOrder(String token, String orderId) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Order order = orderMapper.findByOrderId(orderId);

        if (order == null) {
            return Result.error("Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            return Result.error("Unauthorized");
        }

        return Result.success(order);
    }

    public Result<?> getOrders(String token, String symbol, String status) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        List<Order> orders;

        if (symbol != null && status != null) {
            orders = orderMapper.findByUserIdAndSymbolAndStatus(userId, symbol, status);
        } else if (symbol != null) {
            orders = orderMapper.findByUserIdAndSymbol(userId, symbol);
        } else if (status != null) {
            orders = orderMapper.findByUserIdAndStatus(userId, status);
        } else {
            orders = orderMapper.findByUserId(userId);
        }

        return Result.success(orders);
    }

    public Result<?> getOpenOrders(String token, String symbol) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        List<Order> orders;

        if (symbol != null) {
            orders = orderMapper.findOpenOrdersByUserIdAndSymbol(userId, symbol);
        } else {
            orders = orderMapper.findOpenOrdersByUserId(userId);
        }

        return Result.success(orders);
    }

    public Result<?> getOrderHistory(String token, String symbol, int page, int size) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        int offset = (page - 1) * size;

        List<Order> orders;
        if (symbol != null) {
            orders = orderMapper.findHistoryByUserIdAndSymbol(userId, symbol, offset, size);
        } else {
            orders = orderMapper.findHistoryByUserId(userId, offset, size);
        }

        return Result.success(orders);
    }
}
