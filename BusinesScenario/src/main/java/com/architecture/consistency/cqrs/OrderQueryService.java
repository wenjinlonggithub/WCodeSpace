package com.architecture.consistency.cqrs;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单查询服务（读服务）
 * 处理所有查询操作，优先从缓存读取
 */
@Slf4j
public class OrderQueryService {

    /**
     * 缓存
     */
    private final OrderCache cache;

    public OrderQueryService(OrderCache cache) {
        this.cache = cache;
    }

    /**
     * 查询订单
     * 优先从缓存读取，命中率高，延迟低
     */
    public OrderReadModel getOrder(String orderId) {
        log.info("查询订单: orderId={}", orderId);

        // 从缓存查询
        OrderReadModel readModel = cache.get(orderId);

        if (readModel != null) {
            log.info("缓存命中: orderId={}", orderId);
            // 增加查询计数
            readModel.setQueryCount(readModel.getQueryCount() + 1);
            return readModel;
        }

        log.warn("缓存未命中: orderId={}", orderId);
        // 实际场景中，这里应该从读库查询
        // 如果读库也没有，说明订单可能还在创建中或不存在
        return null;
    }

    /**
     * 查询订单（带重试）
     * 适用于订单刚创建，缓存可能还未预热的场景
     */
    public OrderReadModel getOrderWithRetry(String orderId, int maxRetries, long retryDelayMs) {
        for (int i = 0; i < maxRetries; i++) {
            OrderReadModel readModel = getOrder(orderId);
            if (readModel != null) {
                return readModel;
            }

            if (i < maxRetries - 1) {
                log.info("订单未查询到，{}ms后重试: orderId={}, retry={}/{}",
                        retryDelayMs, orderId, i + 1, maxRetries);
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("订单查询失败（重试{}次后）: orderId={}", maxRetries, orderId);
        return null;
    }
}
