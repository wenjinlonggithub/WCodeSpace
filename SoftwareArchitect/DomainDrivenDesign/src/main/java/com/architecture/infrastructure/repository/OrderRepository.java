package com.architecture.infrastructure.repository;

import com.architecture.domain.aggregate.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Long nextId();
    void save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByCustomerId(Long customerId);
    void delete(Long id);
}
