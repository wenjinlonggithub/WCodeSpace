package com.architecture.seata.controller;

import com.architecture.seata.entity.Order;
import com.architecture.seata.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Order create(@RequestParam Long userId,
                       @RequestParam Long productId,
                       @RequestParam Integer count,
                       @RequestParam BigDecimal money) {
        return orderService.create(userId, productId, count, money);
    }

    /**
     * 创建订单（测试异常回滚）
     */
    @PostMapping("/create-with-exception")
    public Order createWithException(@RequestParam Long userId,
                                    @RequestParam Long productId,
                                    @RequestParam Integer count,
                                    @RequestParam BigDecimal money) {
        return orderService.createWithException(userId, productId, count, money);
    }
}
