package com.architecture.consistency.cqrs;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单缓存
 * 模拟Redis等缓存系统
 */
@Slf4j
public class OrderCache {

    /**
     * 缓存存储（模拟Redis）
     */
    private final Map<String, OrderReadModel> cache = new ConcurrentHashMap<>();

    /**
     * 缓存命中次数
     */
    private long hitCount = 0;

    /**
     * 缓存未命中次数
     */
    private long missCount = 0;

    /**
     * 放入缓存
     */
    public void put(String orderId, OrderReadModel readModel) {
        cache.put(orderId, readModel);
        log.info("写入缓存: orderId={}", orderId);
    }

    /**
     * 从缓存获取
     */
    public OrderReadModel get(String orderId) {
        OrderReadModel readModel = cache.get(orderId);

        if (readModel != null) {
            hitCount++;
            log.debug("缓存命中: orderId={}", orderId);
        } else {
            missCount++;
            log.debug("缓存未命中: orderId={}", orderId);
        }

        return readModel;
    }

    /**
     * 删除缓存
     */
    public void delete(String orderId) {
        cache.remove(orderId);
        log.info("删除缓存: orderId={}", orderId);
    }

    /**
     * 获取缓存命中率
     */
    public double getHitRate() {
        long total = hitCount + missCount;
        return total == 0 ? 0 : (double) hitCount / total;
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return cache.size();
    }

    /**
     * 打印缓存统计
     */
    public void printStatistics() {
        log.info("缓存统计: size={}, hitCount={}, missCount={}, hitRate={:.2f}%",
                cache.size(), hitCount, missCount, getHitRate() * 100);
    }
}
