package com.architecture.infrastructure.repository.impl;

import com.architecture.domain.aggregate.Order;
import com.architecture.infrastructure.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    
    private final Map<Long, Order> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Long nextId() {
        return idGenerator.getAndIncrement();
    }

    @Override
    public void save(Order order) {
        storage.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return storage.values().stream()
            .filter(order -> order.getCustomer().getId().equals(customerId))
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
