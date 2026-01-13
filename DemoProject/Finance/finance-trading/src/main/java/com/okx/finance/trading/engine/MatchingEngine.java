package com.okx.finance.trading.engine;

import com.okx.finance.common.constant.OrderSide;
import com.okx.finance.common.constant.OrderStatus;
import com.okx.finance.common.constant.OrderType;
import com.okx.finance.common.entity.Order;
import com.okx.finance.trading.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchingEngine {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, PriorityQueue<Order>> buyOrderBook = new ConcurrentHashMap<>();
    private final Map<String, PriorityQueue<Order>> sellOrderBook = new ConcurrentHashMap<>();

    @Async
    public void submitOrder(Order order) {
        String symbol = order.getSymbol();

        if (OrderType.MARKET.equals(order.getOrderType())) {
            executeMarketOrder(order);
        } else if (OrderType.LIMIT.equals(order.getOrderType())) {
            executeLimitOrder(order);
        }
    }

    private void executeMarketOrder(Order order) {
        String symbol = order.getSymbol();
        PriorityQueue<Order> oppositeBook;

        if (OrderSide.BUY.equals(order.getSide())) {
            oppositeBook = sellOrderBook.computeIfAbsent(symbol,
                k -> new PriorityQueue<>(Comparator.comparing(Order::getPrice)));
        } else {
            oppositeBook = buyOrderBook.computeIfAbsent(symbol,
                k -> new PriorityQueue<>((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())));
        }

        BigDecimal remainingQuantity = order.getQuantity();

        while (!oppositeBook.isEmpty() && remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            Order matchOrder = oppositeBook.peek();
            BigDecimal matchQuantity = matchOrder.getQuantity().subtract(matchOrder.getExecutedQuantity());

            if (matchQuantity.compareTo(remainingQuantity) <= 0) {
                remainingQuantity = remainingQuantity.subtract(matchQuantity);
                matchOrder.setExecutedQuantity(matchOrder.getQuantity());
                matchOrder.setExecutedAmount(matchOrder.getExecutedAmount().add(
                    matchQuantity.multiply(matchOrder.getPrice())));
                matchOrder.setStatus(OrderStatus.FILLED);
                orderMapper.update(matchOrder);
                oppositeBook.poll();
            } else {
                matchOrder.setExecutedQuantity(matchOrder.getExecutedQuantity().add(remainingQuantity));
                matchOrder.setExecutedAmount(matchOrder.getExecutedAmount().add(
                    remainingQuantity.multiply(matchOrder.getPrice())));
                matchOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
                orderMapper.update(matchOrder);
                remainingQuantity = BigDecimal.ZERO;
            }

            order.setExecutedQuantity(order.getQuantity().subtract(remainingQuantity));
            order.setExecutedAmount(order.getExecutedAmount().add(
                matchQuantity.multiply(matchOrder.getPrice())));
        }

        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
        } else if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        } else {
            order.setStatus(OrderStatus.REJECTED);
        }

        orderMapper.update(order);
    }

    private void executeLimitOrder(Order order) {
        String symbol = order.getSymbol();
        PriorityQueue<Order> oppositeBook;
        PriorityQueue<Order> sameBook;

        if (OrderSide.BUY.equals(order.getSide())) {
            oppositeBook = sellOrderBook.computeIfAbsent(symbol,
                k -> new PriorityQueue<>(Comparator.comparing(Order::getPrice)));
            sameBook = buyOrderBook.computeIfAbsent(symbol,
                k -> new PriorityQueue<>((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())));
        } else {
            oppositeBook = buyOrderBook.computeIfAbsent(symbol,
                k -> new PriorityQueue<>((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())));
            sameBook = sellOrderBook.computeIfAbsent(symbol,
                k -> new PriorityQueue<>(Comparator.comparing(Order::getPrice)));
        }

        BigDecimal remainingQuantity = order.getQuantity();

        while (!oppositeBook.isEmpty() && remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            Order matchOrder = oppositeBook.peek();

            boolean canMatch = OrderSide.BUY.equals(order.getSide()) ?
                order.getPrice().compareTo(matchOrder.getPrice()) >= 0 :
                order.getPrice().compareTo(matchOrder.getPrice()) <= 0;

            if (!canMatch) {
                break;
            }

            BigDecimal matchQuantity = matchOrder.getQuantity().subtract(matchOrder.getExecutedQuantity());
            BigDecimal executeQuantity = matchQuantity.min(remainingQuantity);

            matchOrder.setExecutedQuantity(matchOrder.getExecutedQuantity().add(executeQuantity));
            matchOrder.setExecutedAmount(matchOrder.getExecutedAmount().add(
                executeQuantity.multiply(matchOrder.getPrice())));

            if (matchOrder.getExecutedQuantity().compareTo(matchOrder.getQuantity()) >= 0) {
                matchOrder.setStatus(OrderStatus.FILLED);
                oppositeBook.poll();
            } else {
                matchOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
            orderMapper.update(matchOrder);

            remainingQuantity = remainingQuantity.subtract(executeQuantity);
            order.setExecutedQuantity(order.getQuantity().subtract(remainingQuantity));
            order.setExecutedAmount(order.getExecutedAmount().add(
                executeQuantity.multiply(matchOrder.getPrice())));
        }

        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
        } else {
            if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                order.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
            sameBook.offer(order);
        }

        orderMapper.update(order);
    }

    public void cancelOrder(Order order) {
        String symbol = order.getSymbol();

        if (OrderSide.BUY.equals(order.getSide())) {
            PriorityQueue<Order> book = buyOrderBook.get(symbol);
            if (book != null) {
                book.remove(order);
            }
        } else {
            PriorityQueue<Order> book = sellOrderBook.get(symbol);
            if (book != null) {
                book.remove(order);
            }
        }
    }
}
