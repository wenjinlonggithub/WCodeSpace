package com.architecture.hexagonal.application.port.in;

import com.architecture.hexagonal.domain.OrderId;

/**
 * 输入端口 - 创建订单用例
 * 定义应用的功能,由应用层实现,表现层调用
 */
public interface CreateOrderUseCase {

    /**
     * 执行创建订单用例
     */
    OrderId execute(CreateOrderCommand command);
}
