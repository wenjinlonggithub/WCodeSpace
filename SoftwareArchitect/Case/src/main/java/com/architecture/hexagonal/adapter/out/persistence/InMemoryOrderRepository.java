package com.architecture.hexagonal.adapter.out.persistence;

import com.architecture.hexagonal.application.port.out.OrderRepository;
import com.architecture.hexagonal.domain.CustomerId;
import com.architecture.hexagonal.domain.Order;
import com.architecture.hexagonal.domain.OrderId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 次适配器 - 内存订单仓储实现
 * 被应用驱动(被动方),实现输出端口
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        storage.put(order.getId(), order);
        System.out.println("[Repository] 订单已保存: " + order.getId());
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return storage.values().stream()
            .filter(order -> order.getCustomerId().equals(customerId))
            .collect(Collectors.toList());
    }

    @Override
    public void delete(OrderId id) {
        storage.remove(id);
    }
}
