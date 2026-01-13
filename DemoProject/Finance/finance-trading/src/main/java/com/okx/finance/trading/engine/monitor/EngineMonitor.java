package com.okx.finance.trading.engine.monitor;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 撮合引擎监控
 */
@Data
@Component
public class EngineMonitor {

    // 总订单数
    private final LongAdder totalOrders = new LongAdder();

    // 成交订单数
    private final LongAdder matchedOrders = new LongAdder();

    // 取消订单数
    private final LongAdder canceledOrders = new LongAdder();

    // 拒绝订单数
    private final LongAdder rejectedOrders = new LongAdder();

    // 总成交笔数
    private final LongAdder totalTrades = new LongAdder();

    // 平均撮合延迟（纳秒）
    private final AtomicLong avgMatchLatency = new AtomicLong(0);

    // 最大撮合延迟（纳秒）
    private final AtomicLong maxMatchLatency = new AtomicLong(0);

    // 最小撮合延迟（纳秒）
    private final AtomicLong minMatchLatency = new AtomicLong(Long.MAX_VALUE);

    /**
     * 记录新订单
     */
    public void recordNewOrder() {
        totalOrders.increment();
    }

    /**
     * 记录成交订单
     */
    public void recordMatchedOrder() {
        matchedOrders.increment();
    }

    /**
     * 记录取消订单
     */
    public void recordCanceledOrder() {
        canceledOrders.increment();
    }

    /**
     * 记录拒绝订单
     */
    public void recordRejectedOrder() {
        rejectedOrders.increment();
    }

    /**
     * 记录成交
     */
    public void recordTrade() {
        totalTrades.increment();
    }

    /**
     * 记录撮合延迟
     */
    public void recordMatchLatency(long latencyNanos) {
        // 更新平均延迟
        long currentAvg = avgMatchLatency.get();
        avgMatchLatency.set((currentAvg + latencyNanos) / 2);

        // 更新最大延迟
        long currentMax = maxMatchLatency.get();
        if (latencyNanos > currentMax) {
            maxMatchLatency.set(latencyNanos);
        }

        // 更新最小延迟
        long currentMin = minMatchLatency.get();
        if (latencyNanos < currentMin) {
            minMatchLatency.set(latencyNanos);
        }
    }

    /**
     * 获取统计信息
     */
    public String getStatistics() {
        return String.format(
            "EngineMonitor [totalOrders=%d, matchedOrders=%d, canceledOrders=%d, " +
            "rejectedOrders=%d, totalTrades=%d, avgLatency=%.2fms, maxLatency=%.2fms, minLatency=%.2fms]",
            totalOrders.sum(),
            matchedOrders.sum(),
            canceledOrders.sum(),
            rejectedOrders.sum(),
            totalTrades.sum(),
            avgMatchLatency.get() / 1_000_000.0,
            maxMatchLatency.get() / 1_000_000.0,
            minMatchLatency.get() / 1_000_000.0
        );
    }

    /**
     * 重置统计
     */
    public void reset() {
        totalOrders.reset();
        matchedOrders.reset();
        canceledOrders.reset();
        rejectedOrders.reset();
        totalTrades.reset();
        avgMatchLatency.set(0);
        maxMatchLatency.set(0);
        minMatchLatency.set(Long.MAX_VALUE);
    }
}
