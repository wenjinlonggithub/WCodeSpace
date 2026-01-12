package com.architecture.ddd.domain.repository;

import com.architecture.ddd.domain.model.CustomerId;
import com.architecture.ddd.domain.model.Order;
import com.architecture.ddd.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * DDD - 订单仓储接口
 *
 * 仓储定义在领域层，实现在基础设施层
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findByCustomerId(CustomerId customerId);

    void delete(OrderId id);
}
