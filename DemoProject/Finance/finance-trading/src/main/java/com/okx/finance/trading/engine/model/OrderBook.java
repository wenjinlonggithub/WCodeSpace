package com.okx.finance.trading.engine.model;

import com.okx.finance.common.constant.OrderSide;
import com.okx.finance.common.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 订单簿 - 管理交易对的所有订单
 */
@Data
public class OrderBook {
    private String symbol;

    // 买单订单簿（价格从高到低）
    private NavigableMap<BigDecimal, PriceLevel> bidLevels;

    // 卖单订单簿（价格从低到高）
    private NavigableMap<BigDecimal, PriceLevel> askLevels;

    // 订单ID到订单的映射，用于快速查找
    private Map<String, Order> orderIndex;

    // 最新成交价
    private BigDecimal lastPrice;

    // 24小时最高价
    private BigDecimal high24h;

    // 24小时最低价
    private BigDecimal low24h;

    // 24小时成交量
    private BigDecimal volume24h;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        // 买单：降序排列（价格从高到低）
        this.bidLevels = new ConcurrentSkipListMap<>(Collections.reverseOrder());
        // 卖单：升序排列（价格从低到高）
        this.askLevels = new ConcurrentSkipListMap<>();
        this.orderIndex = new ConcurrentHashMap<>();
        this.lastPrice = BigDecimal.ZERO;
        this.volume24h = BigDecimal.ZERO;
    }

    /**
     * 添加订单到订单簿
     */
    public void addOrder(Order order) {
        NavigableMap<BigDecimal, PriceLevel> levels = OrderSide.BUY.equals(order.getSide()) ? bidLevels : askLevels;

        PriceLevel priceLevel = levels.get(order.getPrice());
        if (priceLevel == null) {
            priceLevel = new PriceLevel(order.getPrice());
            levels.put(order.getPrice(), priceLevel);
        }

        priceLevel.addOrder(order);
        orderIndex.put(order.getOrderId(), order);
    }

    /**
     * 移除订单
     */
    public void removeOrder(Order order) {
        NavigableMap<BigDecimal, PriceLevel> levels = OrderSide.BUY.equals(order.getSide()) ? bidLevels : askLevels;

        PriceLevel priceLevel = levels.get(order.getPrice());
        if (priceLevel != null) {
            priceLevel.removeOrder(order);
            if (priceLevel.isEmpty()) {
                levels.remove(order.getPrice());
            }
        }

        orderIndex.remove(order.getOrderId());
    }

    /**
     * 获取最优买价
     */
    public BigDecimal getBestBidPrice() {
        Map.Entry<BigDecimal, PriceLevel> entry = bidLevels.firstEntry();
        return entry != null ? entry.getKey() : null;
    }

    /**
     * 获取最优卖价
     */
    public BigDecimal getBestAskPrice() {
        Map.Entry<BigDecimal, PriceLevel> entry = askLevels.firstEntry();
        return entry != null ? entry.getKey() : null;
    }

    /**
     * 获取最优买单价格层级
     */
    public PriceLevel getBestBidLevel() {
        Map.Entry<BigDecimal, PriceLevel> entry = bidLevels.firstEntry();
        return entry != null ? entry.getValue() : null;
    }

    /**
     * 获取最优卖单价格层级
     */
    public PriceLevel getBestAskLevel() {
        Map.Entry<BigDecimal, PriceLevel> entry = askLevels.firstEntry();
        return entry != null ? entry.getValue() : null;
    }

    /**
     * 获取深度数据
     */
    public Map<String, Object> getDepth(int limit) {
        Map<String, Object> depth = new HashMap<>();

        List<List<String>> bids = new ArrayList<>();
        int count = 0;
        for (Map.Entry<BigDecimal, PriceLevel> entry : bidLevels.entrySet()) {
            if (count >= limit) break;
            bids.add(Arrays.asList(
                entry.getKey().toPlainString(),
                entry.getValue().getTotalQuantity().toPlainString()
            ));
            count++;
        }

        List<List<String>> asks = new ArrayList<>();
        count = 0;
        for (Map.Entry<BigDecimal, PriceLevel> entry : askLevels.entrySet()) {
            if (count >= limit) break;
            asks.add(Arrays.asList(
                entry.getKey().toPlainString(),
                entry.getValue().getTotalQuantity().toPlainString()
            ));
            count++;
        }

        depth.put("symbol", symbol);
        depth.put("bids", bids);
        depth.put("asks", asks);
        depth.put("timestamp", System.currentTimeMillis());

        return depth;
    }

    /**
     * 更新成交价和统计信息
     */
    public void updateTradePrice(BigDecimal price, BigDecimal quantity) {
        this.lastPrice = price;

        if (high24h == null || price.compareTo(high24h) > 0) {
            high24h = price;
        }

        if (low24h == null || price.compareTo(low24h) < 0) {
            low24h = price;
        }

        volume24h = volume24h.add(quantity);
    }

    /**
     * 获取订单簿统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("symbol", symbol);
        stats.put("bidLevels", bidLevels.size());
        stats.put("askLevels", askLevels.size());
        stats.put("totalOrders", orderIndex.size());
        stats.put("bestBid", getBestBidPrice());
        stats.put("bestAsk", getBestAskPrice());
        stats.put("lastPrice", lastPrice);
        stats.put("high24h", high24h);
        stats.put("low24h", low24h);
        stats.put("volume24h", volume24h);
        return stats;
    }
}
