package com.architecture.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;

/**
 * 基于Redis的分布式限流
 *
 * 实现方案：使用Redis + Lua脚本实现滑动窗口限流
 *
 * 原理：
 * 1. 使用Redis的Sorted Set存储请求时间戳
 * 2. Score为请求时间戳
 * 3. 每次请求时，删除窗口外的记录
 * 4. 统计窗口内的请求数
 * 5. 判断是否超限
 *
 * 优势：
 * - 支持分布式（多实例共享限流）
 * - 精确度高（滑动窗口）
 * - 原子性（Lua脚本）
 */
public class RedisRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimiter.class);

    private static final String LIMITER_KEY_PREFIX = "rate:limiter:";

    private final JedisPool jedisPool;

    // Lua脚本：滑动窗口限流
    private static final String RATE_LIMIT_SCRIPT =
            // 参数：KEYS[1]=限流key, ARGV[1]=窗口大小(ms), ARGV[2]=限流阈值, ARGV[3]=当前时间戳
            "local key = KEYS[1] " +
            "local window = tonumber(ARGV[1]) " +
            "local limit = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "local min_time = now - window " +
            // 删除窗口外的记录
            "redis.call('zremrangebyscore', key, 0, min_time) " +
            // 统计窗口内的请求数
            "local count = redis.call('zcard', key) " +
            // 判断是否超限
            "if count < limit then " +
            "    redis.call('zadd', key, now, now) " +
            "    redis.call('expire', key, math.ceil(window / 1000)) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    public RedisRateLimiter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 尝试获取令牌
     *
     * @param resource 资源标识（如：user:123, api:/order/create）
     * @param limit 限流阈值
     * @param windowMs 时间窗口（毫秒）
     * @return true-通过，false-限流
     */
    public boolean tryAcquire(String resource, long limit, long windowMs) {
        String key = LIMITER_KEY_PREFIX + resource;
        long now = System.currentTimeMillis();

        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(
                    RATE_LIMIT_SCRIPT
            );

            boolean allowed = Long.valueOf(1).equals(result);

            if (allowed) {
                logger.debug("限流检查通过, resource={}, limit={}, window={}ms", resource, limit, windowMs);
            } else {
                logger.warn("触发限流, resource={}, limit={}, window={}ms", resource, limit, windowMs);
            }

            return allowed;
        } catch (Exception e) {
            logger.error("限流检查异常, resource={}", resource, e);
            // 异常时放行（fail-open策略）
            return true;
        }
    }

    /**
     * 固定窗口限流（更简单但精度较低）
     *
     * 使用Redis的INCR + EXPIRE实现
     */
    public boolean tryAcquireSimple(String resource, long limit, long windowSeconds) {
        String key = LIMITER_KEY_PREFIX + resource;

        try (Jedis jedis = jedisPool.getResource()) {
            // INCR计数
            Long count = jedis.incr(key);

            if (count == 1) {
                // 第一次请求，设置过期时间
                jedis.expire(key, (int) windowSeconds);
            }

            boolean allowed = count <= limit;

            if (!allowed) {
                logger.warn("触发限流（固定窗口）, resource={}, count={}, limit={}", resource, count, limit);
            }

            return allowed;
        } catch (Exception e) {
            logger.error("限流检查异常, resource={}", resource, e);
            return true;
        }
    }

    /**
     * 使用示例：用户维度限流
     */
    public static void exampleUserRateLimit() {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        RedisRateLimiter limiter = new RedisRateLimiter(jedisPool);

        // 限制：单用户每分钟最多10个请求
        String userId = "user:12345";
        boolean allowed = limiter.tryAcquire(userId, 10, 60 * 1000);

        if (allowed) {
            // 处理业务
            logger.info("用户请求通过");
        } else {
            // 返回限流错误
            logger.warn("用户请求被限流");
        }

        jedisPool.close();
    }

    /**
     * 使用示例：API限流
     */
    public static void exampleApiRateLimit() {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        RedisRateLimiter limiter = new RedisRateLimiter(jedisPool);

        // 限制：接口每秒最多1000个请求
        String api = "api:/order/create";
        boolean allowed = limiter.tryAcquire(api, 1000, 1000);

        if (allowed) {
            logger.info("API请求通过");
        } else {
            logger.warn("API请求被限流");
        }

        jedisPool.close();
    }
}
