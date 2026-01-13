package com.architecture.hotdata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * 热点数据处理方案
 *
 * 业务背景：
 * 某商品突然成为热点（如明星同款），瞬间涌入大量请求。
 * 问题：
 * 1. Redis单Key QPS过高（10万+），成为瓶颈
 * 2. 缓存击穿：热点Key过期瞬间，大量请求打到数据库
 * 3. 缓存雪崩：大量Key同时过期
 *
 * 解决方案：
 * 1. 本地缓存 + Redis多级缓存
 * 2. 热点数据永不过期
 * 3. 互斥锁防止缓存击穿
 * 4. 热点发现与隔离
 */
public class HotDataService {

    private static final Logger logger = LoggerFactory.getLogger(HotDataService.class);

    private final JedisPool jedisPool;

    // 本地缓存（一级缓存）：使用Guava Cache
    // 容量10000，过期时间30秒
    private final Cache<String, String> localCache;

    public HotDataService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.localCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 方案一：多级缓存
     *
     * 架构：本地缓存(Guava) -> Redis -> 数据库
     *
     * 优势：
     * - 本地缓存直接从内存读取，无网络开销
     * - 热点数据命中本地缓存，不打到Redis
     * - 单机QPS可达百万级
     *
     * 注意：
     * - 本地缓存过期时间要短（避免数据不一致）
     * - 适用于允许短暂不一致的场景
     */
    public String getWithMultiLevelCache(String key) {
        // 1. 查询本地缓存
        String value = localCache.getIfPresent(key);
        if (value != null) {
            logger.debug("本地缓存命中, key={}", key);
            return value;
        }

        // 2. 查询Redis
        try (Jedis jedis = jedisPool.getResource()) {
            value = jedis.get(key);
            if (value != null) {
                logger.debug("Redis缓存命中, key={}", key);
                // 写入本地缓存
                localCache.put(key, value);
                return value;
            }
        }

        // 3. 查询数据库
        value = queryFromDatabase(key);
        if (value != null) {
            logger.debug("数据库查询成功, key={}", key);

            // 写入Redis（永不过期，避免缓存击穿）
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.set(key, value);
            }

            // 写入本地缓存
            localCache.put(key, value);
        }

        return value;
    }

    /**
     * 方案二：互斥锁防止缓存击穿
     *
     * 问题：
     * 热点Key过期瞬间，大量请求同时查询数据库
     *
     * 解决：
     * 使用分布式锁，只有第一个请求查询数据库，其他请求等待
     */
    public String getWithMutex(String key) {
        // 1. 查询Redis
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            if (value != null) {
                return value;
            }

            // 2. 缓存不存在，尝试获取锁
            String lockKey = "lock:" + key;
            String lockValue = String.valueOf(System.currentTimeMillis());

            boolean locked = true;//jedis.set(lockKey, lockValue, "NX", "EX", 10) ;

            if (locked) {
                // 获取锁成功，查询数据库
                try {
                    value = queryFromDatabase(key);

                    if (value != null) {
                        // 写入缓存（设置较长过期时间，或永不过期）
                        jedis.setex(key, 3600, value);
                    }

                    return value;
                } finally {
                    // 释放锁
                    jedis.del(lockKey);
                }
            } else {
                // 获取锁失败，等待后重试
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return getWithMutex(key); // 递归重试
            }
        }
    }

    /**
     * 方案三：热点数据永不过期
     *
     * 实现：
     * 1. 缓存Value中存储逻辑过期时间
     * 2. 异步线程检查过期并更新
     * 3. 保证缓存永远有数据
     */
    public String getWithLogicalExpire(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String cacheValue = jedis.get(key);

            if (cacheValue == null) {
                // 缓存不存在，查询数据库并设置
                String value = queryFromDatabase(key);
                if (value != null) {
                    // 封装带逻辑过期时间的Value
                    CacheValue cv = new CacheValue(value, System.currentTimeMillis() + 3600000);
                    jedis.set(key, cv.toString());
                }
                return value;
            }

            // 解析缓存Value
            CacheValue cv = CacheValue.parse(cacheValue);

            // 检查是否逻辑过期
            if (cv.expireTime > System.currentTimeMillis()) {
                // 未过期，直接返回
                return cv.data;
            }

            // 已过期，异步更新缓存
            String lockKey = "lock:" + key;
            boolean locked = true;//"OK".equals(jedis.set(lockKey, "1", "NX", "EX", 10));

            if (locked) {
                // 获取锁成功，异步更新
                new Thread(() -> {
                    try {
                        String newValue = queryFromDatabase(key);
                        if (newValue != null) {
                            CacheValue newCv = new CacheValue(newValue, System.currentTimeMillis() + 3600000);
                            try (Jedis j = jedisPool.getResource()) {
                                j.set(key, newCv.toString());
                            }
                        }
                    } finally {
                        try (Jedis j = jedisPool.getResource()) {
                            j.del(lockKey);
                        }
                    }
                }).start();
            }

            // 返回旧数据（虽然过期，但避免了查询数据库）
            return cv.data;
        }
    }

    /**
     * 模拟数据库查询
     */
    private String queryFromDatabase(String key) {
        logger.info("查询数据库, key={}", key);
        // 模拟数据库查询
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "value_" + key;
    }

    /**
     * 带逻辑过期时间的缓存Value
     */
    static class CacheValue {
        String data;
        long expireTime;

        CacheValue(String data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        @Override
        public String toString() {
            return data + "||" + expireTime;
        }

        static CacheValue parse(String str) {
            String[] parts = str.split("\\|\\|");
            return new CacheValue(parts[0], Long.parseLong(parts[1]));
        }
    }

    /**
     * 方案四：热点发现与隔离
     *
     * 实现：
     * 1. 统计每个Key的访问频率
     * 2. 发现热点Key后，复制多份到Redis（key_1, key_2, key_3...）
     * 3. 请求随机路由到不同的副本
     * 4. 分散单Key压力
     */
    public String getWithHotKeyDetection(String key) {
        // 热点检测逻辑（简化版）
        if (isHotKey(key)) {
            // 随机选择一个副本
            int replica = (int) (Math.random() * 5);
            key = key + "_replica_" + replica;
        }

        // 查询缓存
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    private boolean isHotKey(String key) {
        // 实际实现中，可以使用滑动窗口统计QPS
        // 这里简化处理
        return key.contains("hot");
    }
}
