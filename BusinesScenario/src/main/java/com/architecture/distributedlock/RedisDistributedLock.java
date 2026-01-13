package com.architecture.distributedlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

/**
 * 基于Redis的分布式锁（原生实现）
 *
 * 核心技术点：
 * 1. SET NX EX：原子性设置key，并设置过期时间
 * 2. value使用UUID：释放时校验，防止误删
 * 3. Lua脚本释放锁：保证原子性
 * 4. 自旋重试：加锁失败时重试
 */
public class RedisDistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    private static final String LOCK_PREFIX = "distributed:lock:";
    private static final int DEFAULT_EXPIRE_TIME = 30; // 默认过期时间30秒

    // Lua脚本：释放锁时校验value
    // 只有value匹配时才删除，保证不会误删其他线程的锁
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final JedisPool jedisPool;

    public RedisDistributedLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 尝试获取锁（非阻塞）
     *
     * @param lockKey 锁的key
     * @param expireTime 过期时间（秒）
     * @return 锁的value（UUID），获取失败返回null
     */
    public String tryLock(String lockKey, int expireTime) {
        String key = LOCK_PREFIX + lockKey;
        String value = UUID.randomUUID().toString();

        try (Jedis jedis = jedisPool.getResource()) {
            // SET key value NX EX expireTime
            // NX：只在key不存在时设置
            // EX：设置过期时间（秒）
            SetParams params = new SetParams().nx().ex(expireTime);
            String result = jedis.set(key, value, params);

            if ("OK".equals(result)) {
                logger.debug("获取锁成功, lockKey={}, value={}", lockKey, value);
                return value;
            } else {
                logger.debug("获取锁失败, lockKey={}", lockKey);
                return null;
            }
        } catch (Exception e) {
            logger.error("获取锁异常, lockKey={}", lockKey, e);
            return null;
        }
    }

    /**
     * 尝试获取锁（使用默认过期时间）
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 阻塞获取锁（自旋重试）
     *
     * @param lockKey 锁的key
     * @param expireTime 过期时间（秒）
     * @param retryTimes 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return 锁的value，获取失败返回null
     */
    public String lock(String lockKey, int expireTime, int retryTimes, long retryInterval) {
        for (int i = 0; i < retryTimes; i++) {
            String value = tryLock(lockKey, expireTime);
            if (value != null) {
                return value;
            }

            // 获取失败，等待后重试
            if (i < retryTimes - 1) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("等待锁被中断, lockKey={}", lockKey);
                    return null;
                }
            }
        }

        logger.warn("获取锁失败（重试{}次）, lockKey={}", retryTimes, lockKey);
        return null;
    }

    /**
     * 阻塞获取锁（默认参数：重试3次，间隔100ms）
     */
    public String lock(String lockKey) {
        return lock(lockKey, DEFAULT_EXPIRE_TIME, 3, 100);
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     * @param value 锁的value（获取锁时返回的UUID）
     * @return true-成功，false-失败（锁不存在或value不匹配）
     */
    public boolean unlock(String lockKey, String value) {
        if (value == null) {
            return false;
        }

        String key = LOCK_PREFIX + lockKey;

        try (Jedis jedis = jedisPool.getResource()) {
            // 使用Lua脚本保证原子性：先校验value，再删除
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(key),
                    Collections.singletonList(value));

            boolean success = Long.valueOf(1).equals(result);
            if (success) {
                logger.debug("释放锁成功, lockKey={}", lockKey);
            } else {
                logger.warn("释放锁失败（value不匹配或锁不存在）, lockKey={}", lockKey);
            }
            return success;
        } catch (Exception e) {
            logger.error("释放锁异常, lockKey={}", lockKey, e);
            return false;
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        // 初始化Redis连接池
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        RedisDistributedLock lock = new RedisDistributedLock(jedisPool);

        String lockKey = "order:create:12345";
        String lockValue = null;

        try {
            // 获取锁
            lockValue = lock.lock(lockKey);

            if (lockValue != null) {
                logger.info("获取锁成功，开始执行业务逻辑");

                // 执行业务逻辑
                // ...

                logger.info("业务逻辑执行完成");
            } else {
                logger.warn("获取锁失败，放弃执行");
            }
        } finally {
            // 释放锁（一定要在finally中释放）
            if (lockValue != null) {
                lock.unlock(lockKey, lockValue);
            }
        }

        jedisPool.close();
    }
}
