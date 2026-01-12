package com.architecture.hexagonal.application.port.out;

import com.architecture.hexagonal.domain.CustomerId;
import com.architecture.hexagonal.domain.Order;
import com.architecture.hexagonal.domain.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * 输出端口 - 订单仓储接口
 * 定义在领域层,由基础设施层实现
 * 体现依赖倒置原则
 */
public interface OrderRepository {

    /**
     * 保存订单
     */
    void save(Order order);

    /**
     * 根据ID查找订单
     */
    Optional<Order> findById(OrderId id);

    /**
     * 根据客户ID查找订单列表
     */
    List<Order> findByCustomerId(CustomerId customerId);

    /**
     * 删除订单
     */
    void delete(OrderId id);
}
