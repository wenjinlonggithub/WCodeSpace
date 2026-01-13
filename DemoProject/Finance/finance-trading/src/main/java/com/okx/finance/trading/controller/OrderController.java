package com.okx.finance.trading.controller;

import com.okx.finance.common.dto.Result;
import com.okx.finance.trading.dto.PlaceOrderRequest;
import com.okx.finance.trading.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trading")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public Result<?> placeOrder(@RequestHeader("Authorization") String token,
                                @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(token, request);
    }

    @DeleteMapping("/order/{orderId}")
    public Result<?> cancelOrder(@RequestHeader("Authorization") String token,
                                  @PathVariable String orderId) {
        return orderService.cancelOrder(token, orderId);
    }

    @GetMapping("/order/{orderId}")
    public Result<?> getOrder(@RequestHeader("Authorization") String token,
                              @PathVariable String orderId) {
        return orderService.getOrder(token, orderId);
    }

    @GetMapping("/orders")
    public Result<?> getOrders(@RequestHeader("Authorization") String token,
                               @RequestParam(required = false) String symbol,
                               @RequestParam(required = false) String status) {
        return orderService.getOrders(token, symbol, status);
    }

    @GetMapping("/openOrders")
    public Result<?> getOpenOrders(@RequestHeader("Authorization") String token,
                                   @RequestParam(required = false) String symbol) {
        return orderService.getOpenOrders(token, symbol);
    }

    @GetMapping("/orderHistory")
    public Result<?> getOrderHistory(@RequestHeader("Authorization") String token,
                                     @RequestParam(required = false) String symbol,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return orderService.getOrderHistory(token, symbol, page, size);
    }
}
