package com.architecture.scenario;

import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存模式实现
 *
 * 常见缓存模式：
 * 1. Cache-Aside（旁路缓存）：最常用
 * 2. Read-Through（读穿透）
 * 3. Write-Through（写穿透）
 * 4. Write-Behind（写回）
 */
public class CachePattern {

    private Jedis jedis;

    public CachePattern(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * Cache-Aside模式（最常用）
     *
     * 读流程：
     * 1. 先查缓存
     * 2. 缓存命中，直接返回
     * 3. 缓存未命中，查数据库
     * 4. 将数据写入缓存
     * 5. 返回数据
     *
     * 写流程：
     * 1. 先更新数据库
     * 2. 再删除缓存（推荐）
     */
    public <T> T cacheAside(String key, Class<T> clazz, int expireSeconds,
                            DataLoader<T> loader) {
        // 1. 查询缓存
        String cached = jedis.get(key);
        if (cached != null) {
            System.out.println("[Cache-Aside] 缓存命中: " + key);
            return JSON.parseObject(cached, clazz);
        }

        System.out.println("[Cache-Aside] 缓存未命中，查询数据库: " + key);

        // 2. 查询数据库
        T data = loader.load();
        if (data == null) {
            // 防止缓存穿透：缓存空值
            jedis.setex(key, 60, "");
            return null;
        }

        // 3. 写入缓存
        jedis.setex(key, expireSeconds, JSON.toJSONString(data));

        return data;
    }

    /**
     * 更新数据（Cache-Aside模式）
     */
    public <T> void updateCacheAside(String key, T data, DataUpdater<T> updater) {
        // 1. 更新数据库
        updater.update(data);
        System.out.println("[Cache-Aside] 数据库更新完成");

        // 2. 删除缓存
        jedis.del(key);
        System.out.println("[Cache-Aside] 缓存已删除: " + key);
    }

    /**
     * 缓存预热
     * 在系统启动时，提前加载热点数据到缓存
     */
    public <T> void warmUp(String key, int expireSeconds, DataLoader<T> loader) {
        System.out.println("[缓存预热] 加载数据: " + key);
        T data = loader.load();
        if (data != null) {
            jedis.setex(key, expireSeconds, JSON.toJSONString(data));
            System.out.println("[缓存预热] 完成: " + key);
        }
    }

    /**
     * 防止缓存击穿 - 互斥锁方案
     * 热点key过期时，只让一个线程查询数据库
     */
    public <T> T getWithMutex(String key, String lockKey, Class<T> clazz,
                               int expireSeconds, DataLoader<T> loader) {
        // 1. 查询缓存
        String cached = jedis.get(key);
        if (cached != null && !cached.isEmpty()) {
            return JSON.parseObject(cached, clazz);
        }

        // 2. 尝试获取互斥锁
        String lockValue = String.valueOf(System.currentTimeMillis());
        boolean locked = jedis.setnx(lockKey, lockValue) == 1;

        if (locked) {
            jedis.expire(lockKey, 10); // 设置锁过期时间

            try {
                System.out.println("[互斥锁] 获取锁成功，查询数据库");

                // 3. 再次检查缓存（双重检查）
                cached = jedis.get(key);
                if (cached != null && !cached.isEmpty()) {
                    return JSON.parseObject(cached, clazz);
                }

                // 4. 查询数据库
                T data = loader.load();

                // 5. 写入缓存
                if (data != null) {
                    jedis.setex(key, expireSeconds, JSON.toJSONString(data));
                } else {
                    // 缓存空值，防止缓存穿透
                    jedis.setex(key, 60, "");
                }

                return data;
            } finally {
                // 6. 释放锁
                jedis.del(lockKey);
                System.out.println("[互斥锁] 释放锁");
            }
        } else {
            System.out.println("[互斥锁] 获取锁失败，等待重试");

            // 等待一段时间后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return getWithMutex(key, lockKey, clazz, expireSeconds, loader);
        }
    }

    /**
     * 防止缓存击穿 - 逻辑过期方案
     * 不设置过期时间，而是在value中记录过期时间
     */
    public <T> T getWithLogicalExpire(String key, Class<T> clazz,
                                       int expireSeconds, DataLoader<T> loader) {
        String cached = jedis.get(key);

        if (cached == null || cached.isEmpty()) {
            // 首次访问，加载数据
            return refreshLogicalExpire(key, clazz, expireSeconds, loader);
        }

        // 解析缓存数据
        CacheData<T> cacheData = JSON.parseObject(cached,
                JSON.parseObject(cached, CacheData.class).getClass());

        // 检查是否逻辑过期
        if (cacheData.expireTime > System.currentTimeMillis()) {
            // 未过期，直接返回
            return cacheData.data;
        }

        // 已过期，异步刷新
        System.out.println("[逻辑过期] 数据已过期，异步刷新");
        Thread refreshThread = new Thread(() ->
                refreshLogicalExpire(key, clazz, expireSeconds, loader)
        );
        refreshThread.start();

        // 返回旧数据
        return cacheData.data;
    }

    private <T> T refreshLogicalExpire(String key, Class<T> clazz,
                                        int expireSeconds, DataLoader<T> loader) {
        T data = loader.load();
        if (data != null) {
            CacheData<T> cacheData = new CacheData<>();
            cacheData.data = data;
            cacheData.expireTime = System.currentTimeMillis() +
                    TimeUnit.SECONDS.toMillis(expireSeconds);

            jedis.set(key, JSON.toJSONString(cacheData));
        }
        return data;
    }

    /**
     * 缓存数据封装（用于逻辑过期）
     */
    static class CacheData<T> {
        T data;
        long expireTime;
    }

    /**
     * 数据加载器接口
     */
    @FunctionalInterface
    public interface DataLoader<T> {
        T load();
    }

    /**
     * 数据更新器接口
     */
    @FunctionalInterface
    public interface DataUpdater<T> {
        void update(T data);
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            CachePattern pattern = new CachePattern(jedis);

            // 示例：用户信息缓存
            String userId = "1001";
            String cacheKey = "user:info:" + userId;

            // 1. Cache-Aside模式读取
            System.out.println("=== Cache-Aside 读取 ===");
            User user = pattern.cacheAside(
                    cacheKey,
                    User.class,
                    3600,
                    () -> {
                        // 模拟从数据库加载
                        System.out.println("从数据库加载用户信息...");
                        return new User(userId, "张三", 25);
                    }
            );
            System.out.println("用户信息: " + JSON.toJSONString(user));

            // 再次读取，应该命中缓存
            user = pattern.cacheAside(cacheKey, User.class, 3600, () -> null);
            System.out.println("用户信息: " + JSON.toJSONString(user));

            // 2. Cache-Aside模式更新
            System.out.println("\n=== Cache-Aside 更新 ===");
            user.age = 26;
            pattern.updateCacheAside(cacheKey, user, (data) -> {
                // 模拟更新数据库
                System.out.println("更新数据库: " + JSON.toJSONString(data));
            });

            // 3. 互斥锁防止缓存击穿
            System.out.println("\n=== 互斥锁方案 ===");
            jedis.del(cacheKey); // 清空缓存
            user = pattern.getWithMutex(
                    cacheKey,
                    cacheKey + ":lock",
                    User.class,
                    3600,
                    () -> {
                        System.out.println("查询数据库...");
                        return new User(userId, "张三", 26);
                    }
            );
            System.out.println("用户信息: " + JSON.toJSONString(user));
        }
    }

    /**
     * 用户实体类
     */
    static class User {
        String id;
        String name;
        int age;

        public User() {}

        public User(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }
}
