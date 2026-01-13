package com.okx.finance.trading.engine;

import com.okx.finance.common.constant.OrderSide;
import com.okx.finance.common.constant.OrderStatus;
import com.okx.finance.common.constant.OrderType;
import com.okx.finance.common.entity.Order;
import com.okx.finance.trading.engine.listener.MatchListener;
import com.okx.finance.trading.engine.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 撮合引擎 V2 - 完整版本
 * 特性：
 * 1. 基于价格-时间优先的撮合算法
 * 2. 支持限价单和市价单
 * 3. 支持多交易对
 * 4. 异步撮合处理
 * 5. 事件通知机制
 */
@Slf4j
@Component
public class MatchingEngineV2 {

    // 订单簿管理：交易对 -> 订单簿
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    // 撮合队列：每个交易对一个队列
    private final Map<String, BlockingQueue<Order>> matchQueues = new ConcurrentHashMap<>();

    // 撮合线程池
    private final ExecutorService matchExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    // 撮合监听器
    @Autowired
    private MatchListener matchListener;

    // 手续费率
    private static final BigDecimal TAKER_FEE_RATE = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal MAKER_FEE_RATE = new BigDecimal("0.0008"); // 0.08%

    @PostConstruct
    public void init() {
        log.info("撮合引擎初始化完成");
    }

    /**
     * 提交订单到撮合引擎
     */
    public void submitOrder(Order order) {
        String symbol = order.getSymbol();

        // 获取或创建订单簿
        OrderBook orderBook = orderBooks.computeIfAbsent(symbol, OrderBook::new);

        // 获取或创建撮合队列
        BlockingQueue<Order> queue = matchQueues.computeIfAbsent(
                symbol, k -> new LinkedBlockingQueue<>()
        );

        try {
            queue.put(order);

            // 启动撮合任务
            matchExecutor.submit(() -> processOrder(symbol));
        } catch (InterruptedException e) {
            log.error("提交订单失败: {}", order.getOrderId(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理订单撮合
     */
    private void processOrder(String symbol) {
        BlockingQueue<Order> queue = matchQueues.get(symbol);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        Order order = queue.poll();
        if (order == null) {
            return;
        }

        OrderBook orderBook = orderBooks.get(symbol);
        if (orderBook == null) {
            log.error("订单簿不存在: {}", symbol);
            return;
        }

        log.info("开始撮合订单 - Symbol: {}, OrderId: {}, Type: {}, Side: {}, Price: {}, Quantity: {}",
                symbol, order.getOrderId(), order.getOrderType(), order.getSide(),
                order.getPrice(), order.getQuantity());

        try {
            // 根据订单类型执行不同的撮合逻辑
            if (OrderType.MARKET.equals(order.getOrderType())) {
                executeMarketOrder(order, orderBook);
            } else if (OrderType.LIMIT.equals(order.getOrderType())) {
                executeLimitOrder(order, orderBook);
            }
        } catch (Exception e) {
            log.error("订单撮合异常 - OrderId: {}", order.getOrderId(), e);
            order.setStatus(OrderStatus.REJECTED);
        }
    }

    /**
     * 执行市价单撮合
     */
    private void executeMarketOrder(Order order, OrderBook orderBook) {
        BigDecimal remainingQuantity = order.getQuantity();
        List<MatchResult> matchResults = new ArrayList<>();

        while (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            PriceLevel bestLevel = OrderSide.BUY.equals(order.getSide())
                    ? orderBook.getBestAskLevel()
                    : orderBook.getBestBidLevel();

            if (bestLevel == null || bestLevel.isEmpty()) {
                break; // 没有对手盘
            }

            Order makerOrder = bestLevel.peekOrder();
            if (makerOrder == null) {
                break;
            }

            // 计算可成交数量
            BigDecimal makerRemaining = makerOrder.getQuantity().subtract(makerOrder.getExecutedQuantity());
            BigDecimal matchQuantity = makerRemaining.min(remainingQuantity);

            // 成交价格为maker订单价格
            BigDecimal matchPrice = makerOrder.getPrice();

            // 更新订单状态
            updateOrderAfterMatch(order, makerOrder, matchPrice, matchQuantity, true);
            remainingQuantity = remainingQuantity.subtract(matchQuantity);

            // 记录撮合结果
            MatchResult matchResult = new MatchResult(order, makerOrder, matchPrice, matchQuantity);
            matchResults.add(matchResult);

            // 通知监听器
            matchListener.onMatch(matchResult);

            // 如果maker订单完全成交，从订单簿移除
            if (makerOrder.getStatus().equals(OrderStatus.FILLED)) {
                bestLevel.pollOrder();
                if (bestLevel.isEmpty()) {
                    if (OrderSide.BUY.equals(order.getSide())) {
                        orderBook.getAskLevels().remove(bestLevel.getPrice());
                    } else {
                        orderBook.getBidLevels().remove(bestLevel.getPrice());
                    }
                }
                matchListener.onOrderFilled(makerOrder.getOrderId());
            }

            // 更新订单簿统计
            orderBook.updateTradePrice(matchPrice, matchQuantity);
        }

        // 设置订单最终状态
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
            matchListener.onOrderFilled(order.getOrderId());
        } else if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        } else {
            order.setStatus(OrderStatus.REJECTED);
        }

        log.info("市价单撮合完成 - OrderId: {}, Status: {}, ExecutedQty: {}/{}",
                order.getOrderId(), order.getStatus(),
                order.getExecutedQuantity(), order.getQuantity());
    }

    /**
     * 执行限价单撮合
     */
    private void executeLimitOrder(Order order, OrderBook orderBook) {
        BigDecimal remainingQuantity = order.getQuantity();
        List<MatchResult> matchResults = new ArrayList<>();

        // 尝试撮合
        while (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            PriceLevel bestLevel = OrderSide.BUY.equals(order.getSide())
                    ? orderBook.getBestAskLevel()
                    : orderBook.getBestBidLevel();

            if (bestLevel == null || bestLevel.isEmpty()) {
                break; // 没有对手盘
            }

            Order makerOrder = bestLevel.peekOrder();
            if (makerOrder == null) {
                break;
            }

            // 检查价格是否匹配
            boolean priceMatch = OrderSide.BUY.equals(order.getSide())
                    ? order.getPrice().compareTo(makerOrder.getPrice()) >= 0
                    : order.getPrice().compareTo(makerOrder.getPrice()) <= 0;

            if (!priceMatch) {
                break; // 价格不匹配，停止撮合
            }

            // 计算可成交数量
            BigDecimal makerRemaining = makerOrder.getQuantity().subtract(makerOrder.getExecutedQuantity());
            BigDecimal matchQuantity = makerRemaining.min(remainingQuantity);

            // 成交价格为maker订单价格（价格优先）
            BigDecimal matchPrice = makerOrder.getPrice();

            // 更新订单状态
            updateOrderAfterMatch(order, makerOrder, matchPrice, matchQuantity, false);
            remainingQuantity = remainingQuantity.subtract(matchQuantity);

            // 记录撮合结果
            MatchResult matchResult = new MatchResult(order, makerOrder, matchPrice, matchQuantity);
            matchResults.add(matchResult);

            // 通知监听器
            matchListener.onMatch(matchResult);

            // 如果maker订单完全成交，从订单簿移除
            if (makerOrder.getStatus().equals(OrderStatus.FILLED)) {
                bestLevel.pollOrder();
                if (bestLevel.isEmpty()) {
                    if (OrderSide.BUY.equals(order.getSide())) {
                        orderBook.getAskLevels().remove(bestLevel.getPrice());
                    } else {
                        orderBook.getBidLevels().remove(bestLevel.getPrice());
                    }
                }
                matchListener.onOrderFilled(makerOrder.getOrderId());
            }

            // 更新订单簿统计
            orderBook.updateTradePrice(matchPrice, matchQuantity);
        }

        // 设置订单最终状态
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
            matchListener.onOrderFilled(order.getOrderId());
        } else {
            // 未完全成交，加入订单簿
            if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                order.setStatus(OrderStatus.PARTIALLY_FILLED);
            } else {
                order.setStatus(OrderStatus.NEW);
            }
            orderBook.addOrder(order);
        }

        log.info("限价单撮合完成 - OrderId: {}, Status: {}, ExecutedQty: {}/{}",
                order.getOrderId(), order.getStatus(),
                order.getExecutedQuantity(), order.getQuantity());
    }

    /**
     * 更新订单成交信息
     */
    private void updateOrderAfterMatch(Order takerOrder, Order makerOrder,
                                       BigDecimal matchPrice, BigDecimal matchQuantity,
                                       boolean isMarketOrder) {
        // 更新taker订单
        BigDecimal takerExecutedQty = takerOrder.getExecutedQuantity().add(matchQuantity);
        BigDecimal takerExecutedAmt = takerOrder.getExecutedAmount().add(matchPrice.multiply(matchQuantity));
        takerOrder.setExecutedQuantity(takerExecutedQty);
        takerOrder.setExecutedAmount(takerExecutedAmt);

        if (takerExecutedQty.compareTo(takerOrder.getQuantity()) >= 0) {
            takerOrder.setStatus(OrderStatus.FILLED);
        } else {
            takerOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        }

        // 更新maker订单
        BigDecimal makerExecutedQty = makerOrder.getExecutedQuantity().add(matchQuantity);
        BigDecimal makerExecutedAmt = makerOrder.getExecutedAmount().add(matchPrice.multiply(matchQuantity));
        makerOrder.setExecutedQuantity(makerExecutedQty);
        makerOrder.setExecutedAmount(makerExecutedAmt);

        if (makerExecutedQty.compareTo(makerOrder.getQuantity()) >= 0) {
            makerOrder.setStatus(OrderStatus.FILLED);
        } else {
            makerOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        }

        log.debug("订单更新 - TakerOrderId: {}, ExecutedQty: {}, MakerOrderId: {}, ExecutedQty: {}",
                takerOrder.getOrderId(), takerExecutedQty,
                makerOrder.getOrderId(), makerExecutedQty);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Order order) {
        String symbol = order.getSymbol();
        OrderBook orderBook = orderBooks.get(symbol);

        if (orderBook != null) {
            orderBook.removeOrder(order);
            order.setStatus(OrderStatus.CANCELED);
            matchListener.onOrderCanceled(order.getOrderId());
            log.info("订单已取消 - OrderId: {}", order.getOrderId());
        }
    }

    /**
     * 获取订单簿深度
     */
    public Map<String, Object> getOrderBookDepth(String symbol, int limit) {
        OrderBook orderBook = orderBooks.get(symbol);
        if (orderBook != null) {
            return orderBook.getDepth(limit);
        }
        return null;
    }

    /**
     * 获取订单簿统计信息
     */
    public Map<String, Object> getOrderBookStatistics(String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        if (orderBook != null) {
            return orderBook.getStatistics();
        }
        return null;
    }

    /**
     * 关闭撮合引擎
     */
    public void shutdown() {
        matchExecutor.shutdown();
        try {
            if (!matchExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                matchExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            matchExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("撮合引擎已关闭");
    }
}
