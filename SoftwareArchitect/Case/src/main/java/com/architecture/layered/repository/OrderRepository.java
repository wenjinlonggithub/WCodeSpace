package com.architecture.layered.repository;

import com.architecture.layered.entity.Order;
import com.architecture.layered.entity.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 持久层 - 订单仓储
 * 职责: 数据访问、CRUD操作、不包含业务逻辑
 */
public class OrderRepository {

    // 模拟数据库存储
    private final Map<Long, Order> database = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 保存订单
     */
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        database.put(order.getId(), order);
        return order;
    }

    /**
     * 根据ID查询订单
     */
    public Order findById(Long id) {
        return database.get(id);
    }

    /**
     * 根据用户ID查询订单列表
     */
    public List<Order> findByUserId(Long userId) {
        return database.values().stream()
            .filter(order -> order.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    /**
     * 根据状态查询订单列表
     */
    public List<Order> findByStatus(OrderStatus status) {
        return database.values().stream()
            .filter(order -> order.getStatus() == status)
            .collect(Collectors.toList());
    }

    /**
     * 更新订单
     */
    public void update(Order order) {
        if (order.getId() == null || !database.containsKey(order.getId())) {
            throw new IllegalArgumentException("订单不存在");
        }
        database.put(order.getId(), order);
    }

    /**
     * 删除订单
     */
    public void delete(Long id) {
        database.remove(id);
    }

    /**
     * 查询所有订单
     */
    public List<Order> findAll() {
        return new ArrayList<>(database.values());
    }

    /**
     * 统计用户订单数量
     */
    public long countByUserId(Long userId) {
        return database.values().stream()
            .filter(order -> order.getUserId().equals(userId))
            .count();
    }
}
