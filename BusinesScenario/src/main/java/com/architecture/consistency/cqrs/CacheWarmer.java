package com.architecture.consistency.cqrs;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 缓存预热器
 * 在订单创建时立即预热缓存，避免查询时缓存穿透
 */
@Slf4j
public class CacheWarmer {

    private final OrderCache cache;

    public CacheWarmer(OrderCache cache) {
        this.cache = cache;
    }

    /**
     * 预热缓存
     * 在订单创建后立即调用，将订单数据写入缓存
     */
    public void warmup(String orderId, OrderReadModel readModel) {
        log.info("预热缓存: orderId={}", orderId);

        try {
            // 写入缓存
            cache.put(orderId, readModel);

            log.info("缓存预热成功: orderId={}", orderId);

        } catch (Exception e) {
            log.error("缓存预热失败: orderId={}", orderId, e);
            // 预热失败不影响主流程，只记录日志
        }
    }

    /**
     * 批量预热缓存
     * 适用于系统启动时预热热点数据
     */
    public void warmupBatch(Map<String, OrderReadModel> orders) {
        log.info("批量预热缓存: count={}", orders.size());

        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<String, OrderReadModel> entry : orders.entrySet()) {
            try {
                cache.put(entry.getKey(), entry.getValue());
                successCount++;
            } catch (Exception e) {
                log.error("预热失败: orderId={}", entry.getKey(), e);
                failureCount++;
            }
        }

        log.info("批量预热完成: success={}, failure={}", successCount, failureCount);
    }
}
