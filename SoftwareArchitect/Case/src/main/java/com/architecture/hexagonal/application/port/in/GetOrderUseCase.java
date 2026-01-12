package com.architecture.hexagonal.application.port.in;

import com.architecture.hexagonal.domain.Order;
import com.architecture.hexagonal.domain.OrderId;

/**
 * 输入端口 - 获取订单用例
 */
public interface GetOrderUseCase {

    Order execute(OrderId orderId);
}
