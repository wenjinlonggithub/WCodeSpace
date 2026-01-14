package com.architecture.esignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁
 * 简化实现,实际生产环境建议使用Redisson
 */
@Component
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    // 模拟Redis存储 (key -> value)
    private final ConcurrentMap<String, LockValue> redisStorage = new ConcurrentHashMap<>();

    // 当前线程持有的锁 (用于unlock时验证)
    private final ThreadLocal<String> currentLockValue = new ThreadLocal<>();

    /**
     * 尝试获取锁
     *
     * Redis命令:
     * SET key value NX EX seconds
     *
     * @param key 锁的key
     * @param timeout 锁的超时时间
     * @param unit 时间单位
     * @return true=获取成功, false=获取失败
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        String value = UUID.randomUUID().toString();
        long expireTime = System.currentTimeMillis() + unit.toMillis(timeout);

        log.debug("尝试获取锁: key={}, value={}, timeout={}{}",
                key, value, timeout, unit);

        // SET NX (putIfAbsent)显示器

        LockValue lockValue = new LockValue(value, expireTime);
        LockValue existing = redisStorage.putIfAbsent(key, lockValue);

        if (existing == null) {
            // 获取锁成功
            currentLockValue.set(value);
            log.debug("获取锁成功: key={}, value={}", key, value);
            return true;

        } else {
            // 锁已存在,检查是否过期
            if (existing.isExpired()) {
                // 锁已过期,尝试删除旧锁并获取新锁
                if (redisStorage.remove(key, existing)) {
                    redisStorage.put(key, lockValue);
                    currentLockValue.set(value);
                    log.debug("锁已过期,获取成功: key={}, value={}", key, value);
                    return true;
                }
            }

            log.debug("获取锁失败: key={}, 已被占用", key);
            return false;
        }
    }

    /**
     * 释放锁
     *
     * Redis Lua脚本:
     * if redis.call("get",KEYS[1]) == ARGV[1] then
     *     return redis.call("del",KEYS[1])
     * else
     *     return 0
     * end
     */
    public void unlock(String key) {
        String value = currentLockValue.get();
        if (value == null) {
            log.warn("当前线程没有持有锁: key={}", key);
            return;
        }

        LockValue existing = redisStorage.get(key);
        if (existing != null && existing.getValue().equals(value)) {
            // 验证value相同,才删除 (防止误删其他线程的锁)
            redisStorage.remove(key, existing);
            log.debug("释放锁成功: key={}, value={}", key, value);
        } else {
            log.warn("锁的value不匹配或已被删除: key={}, expectedValue={}", key, value);
        }

        currentLockValue.remove();
    }

    /**
     * 删除Token (用于幂等性验证)
     *
     * @return true=删除成功(第一次), false=token不存在(重复)
     */
    public Boolean deleteToken(String key) {
        LockValue removed = redisStorage.remove(key);
        boolean deleted = (removed != null);

        log.debug("删除Token: key={}, deleted={}", key, deleted);

        return deleted;
    }

    /**
     * 设置Token (用于幂等性验证)
     */
    public void setToken(String key, long timeout, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(timeout);
        LockValue lockValue = new LockValue("1", expireTime);

        redisStorage.put(key, lockValue);

        log.debug("设置Token: key={}, timeout={}{}", key, timeout, unit);
    }

    /**
     * 清空所有锁 (用于测试)
     */
    public void clear() {
        redisStorage.clear();
        log.debug("清空所有Redis锁");
    }

    /**
     * 锁的值对象
     */
    private static class LockValue {
        private final String value;
        private final long expireTime;

        public LockValue(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
