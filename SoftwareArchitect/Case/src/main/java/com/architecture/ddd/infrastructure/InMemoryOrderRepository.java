package com.architecture.ddd.infrastructure;

import com.architecture.ddd.domain.model.CustomerId;
import com.architecture.ddd.domain.model.Order;
import com.architecture.ddd.domain.model.OrderId;
import com.architecture.ddd.domain.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DDD - 订单仓储实现（基础设施层）
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
