package com.architecture.medicalreport.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 报表缓存服务 (多级缓存)
 *
 * 缓存策略:
 * L1: Caffeine (本地缓存) - 热点数据，容量10000，过期5分钟
 * L2: Redis (分布式缓存) - 共享数据，容量100万，过期1小时
 * L3: 数据库 - 持久化数据
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Slf4j
@Service
public class ReportCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 本地缓存 (L1) */
    private Cache<String, Object> localCache;

    @PostConstruct
    public void init() {
        this.localCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("初始化本地缓存完成: maxSize=10000, expireAfterWrite=5min");
    }

    /**
     * 获取缓存数据 (多级缓存查找)
     *
     * @param key 缓存key
     * @param <T> 数据类型
     * @return 缓存数据，不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        // L1: 本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            log.debug("L1缓存命中: key={}", key);
            return (T) value;
        }

        // L2: Redis缓存
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("L2缓存命中: key={}", key);
            // 回写L1缓存
            localCache.put(key, value);
            return (T) value;
        }

        log.debug("缓存未命中: key={}", key);
        return null;
    }

    /**
     * 写入缓存
     *
     * @param key 缓存key
     * @param value 缓存值
     * @param expireSeconds 过期时间(秒)
     */
    public void put(String key, Object value, long expireSeconds) {
        // 写入L1
        localCache.put(key, value);

        // 写入L2
        redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);

        log.debug("写入缓存: key={}, expire={}s", key, expireSeconds);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     */
    public void evict(String key) {
        // 删除L1
        localCache.invalidate(key);

        // 删除L2
        redisTemplate.delete(key);

        log.debug("删除缓存: key={}", key);
    }

    /**
     * 批量删除缓存 (通配符)
     *
     * @param pattern 匹配模式，如: "annual_report:*"
     */
    public void evictByPattern(String pattern) {
        // L1: 清空所有本地缓存 (Caffeine不支持通配符删除)
        localCache.invalidateAll();

        // L2: Redis通配符删除
        redisTemplate.keys(pattern).forEach(key -> redisTemplate.delete(key));

        log.info("批量删除缓存: pattern={}", pattern);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息
     */
    public CacheStats getStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();

        return CacheStats.builder()
                .hitCount(stats.hitCount())
                .missCount(stats.missCount())
                .hitRate(stats.hitRate())
                .evictionCount(stats.evictionCount())
                .build();
    }

    /**
     * 缓存统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        /** 命中次数 */
        private long hitCount;

        /** 未命中次数 */
        private long missCount;

        /** 命中率 */
        private double hitRate;

        /** 驱逐次数 */
        private long evictionCount;
    }
}
