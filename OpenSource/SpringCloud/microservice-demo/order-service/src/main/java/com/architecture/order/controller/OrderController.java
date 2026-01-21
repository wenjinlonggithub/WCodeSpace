package com.architecture.order.controller;

import com.architecture.order.dto.CreateOrderRequest;
import com.architecture.order.dto.OrderDTO;
import com.architecture.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 *
 * API设计:
 * - POST /orders/create: 创建订单
 * - GET /orders/{id}: 查询订单
 * - PUT /orders/{id}/cancel: 取消订单
 *
 * @author architecture
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     *
     * 测试命令:
     * curl -X POST http://localhost:8002/orders/create \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "userId": 1,
     *     "productId": 100,
     *     "quantity": 2
     *   }'
     *
     * @param request 创建订单请求
     * @return 订单信息
     */
    @PostMapping("/create")
    public OrderDTO createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    /**
     * 查询订单
     *
     * 测试命令:
     * curl http://localhost:8002/orders/1
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{id}")
    public OrderDTO getOrder(@PathVariable("id") Long orderId) {
        return orderService.getOrder(orderId);
    }

    /**
     * 取消订单
     *
     * 测试命令:
     * curl -X PUT http://localhost:8002/orders/1/cancel
     *
     * @param orderId 订单ID
     */
    @PutMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable("id") Long orderId) {
        orderService.cancelOrder(orderId);
    }
}
