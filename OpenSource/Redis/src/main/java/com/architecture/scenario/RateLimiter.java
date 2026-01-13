package com.architecture.scenario;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis限流器实现
 *
 * 应用场景：
 * - API接口限流
 * - 防止恶意刷单
 * - 控制并发访问
 *
 * 常见算法：
 * 1. 固定窗口：简单但有临界问题
 * 2. 滑动窗口：精确控制，性能较好
 * 3. 漏桶算法：流量平滑
 * 4. 令牌桶算法：支持突发流量
 */
public class RateLimiter {

    private Jedis jedis;

    public RateLimiter(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * 固定窗口限流
     * 使用简单的计数器，每个窗口期重置
     *
     * @param key 限流key
     * @param limit 窗口内最大请求数
     * @param window 窗口大小（秒）
     */
    public boolean fixedWindowLimiter(String key, int limit, int window) {
        long current = jedis.incr(key);

        // 第一次访问，设置过期时间
        if (current == 1) {
            jedis.expire(key, window);
        }

        return current <= limit;
    }

    /**
     * 滑动窗口限流
     * 使用ZSet记录每次请求的时间戳
     *
     * @param key 限流key
     * @param limit 窗口内最大请求数
     * @param window 窗口大小（秒）
     */
    public boolean slidingWindowLimiter(String key, int limit, int window) {
        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000L;

        // 删除窗口外的数据
        jedis.zremrangeByScore(key, 0, windowStart);

        // 统计当前窗口内的请求数
        long count = jedis.zcard(key);

        if (count < limit) {
            // 添加当前请求
            jedis.zadd(key, now, String.valueOf(now));
            jedis.expire(key, window + 1);
            return true;
        }

        return false;
    }

    /**
     * 滑动窗口限流（Lua脚本实现）
     * 保证原子性
     */
    public boolean slidingWindowLimiterLua(String key, int limit, int window) {
        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000L;

        String script =
                "redis.call('zremrangeByScore', KEYS[1], 0, ARGV[1]) " +
                "local count = redis.call('zcard', KEYS[1]) " +
                "if count < tonumber(ARGV[2]) then " +
                "    redis.call('zadd', KEYS[1], ARGV[3], ARGV[3]) " +
                "    redis.call('expire', KEYS[1], ARGV[4]) " +
                "    return 1 " +
                "else " +
                "    return 0 " +
                "end";

        List<String> keys = new ArrayList<>();
        keys.add(key);

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(windowStart));
        args.add(String.valueOf(limit));
        args.add(String.valueOf(now));
        args.add(String.valueOf(window + 1));

        Object result = jedis.eval(script, keys, args);
        return Long.valueOf(1).equals(result);
    }

    /**
     * 令牌桶限流
     * 以固定速率生成令牌，支持突发流量
     *
     * @param key 限流key
     * @param capacity 桶容量
     * @param rate 生成速率（令牌/秒）
     */
    public boolean tokenBucketLimiter(String key, int capacity, double rate) {
        long now = System.currentTimeMillis();

        String script =
                "local tokens_key = KEYS[1] " +
                "local timestamp_key = KEYS[1]..':timestamp' " +
                "local capacity = tonumber(ARGV[1]) " +
                "local rate = tonumber(ARGV[2]) " +
                "local now = tonumber(ARGV[3]) " +
                "local requested = 1 " +
                "" +
                "local last_tokens = tonumber(redis.call('get', tokens_key)) " +
                "if last_tokens == nil then " +
                "    last_tokens = capacity " +
                "end " +
                "" +
                "local last_time = tonumber(redis.call('get', timestamp_key)) " +
                "if last_time == nil then " +
                "    last_time = now " +
                "end " +
                "" +
                "local delta = math.max(0, now - last_time) " +
                "local new_tokens = math.min(capacity, last_tokens + delta * rate / 1000) " +
                "" +
                "if new_tokens >= requested then " +
                "    redis.call('set', tokens_key, new_tokens - requested) " +
                "    redis.call('set', timestamp_key, now) " +
                "    redis.call('expire', tokens_key, 60) " +
                "    redis.call('expire', timestamp_key, 60) " +
                "    return 1 " +
                "else " +
                "    return 0 " +
                "end";

        List<String> keys = new ArrayList<>();
        keys.add(key);

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(capacity));
        args.add(String.valueOf(rate));
        args.add(String.valueOf(now));

        Object result = jedis.eval(script, keys, args);
        return Long.valueOf(1).equals(result);
    }

    /**
     * 漏桶限流
     * 请求以任意速率进入，以固定速率流出
     *
     * @param key 限流key
     * @param capacity 桶容量
     * @param rate 流出速率（请求/秒）
     */
    public boolean leakyBucketLimiter(String key, int capacity, double rate) {
        long now = System.currentTimeMillis();

        String script =
                "local key = KEYS[1] " +
                "local capacity = tonumber(ARGV[1]) " +
                "local rate = tonumber(ARGV[2]) " +
                "local now = tonumber(ARGV[3]) " +
                "" +
                "local bucket = redis.call('hmget', key, 'water', 'time') " +
                "local water = tonumber(bucket[1]) or 0 " +
                "local last_time = tonumber(bucket[2]) or now " +
                "" +
                "local delta = math.max(0, now - last_time) " +
                "local leaked = delta * rate / 1000 " +
                "local current_water = math.max(0, water - leaked) " +
                "" +
                "if current_water + 1 <= capacity then " +
                "    redis.call('hmset', key, 'water', current_water + 1, 'time', now) " +
                "    redis.call('expire', key, 60) " +
                "    return 1 " +
                "else " +
                "    return 0 " +
                "end";

        List<String> keys = new ArrayList<>();
        keys.add(key);

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(capacity));
        args.add(String.valueOf(rate));
        args.add(String.valueOf(now));

        Object result = jedis.eval(script, keys, args);
        return Long.valueOf(1).equals(result);
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            RateLimiter limiter = new RateLimiter(jedis);

            String userId = "user:1001";
            String apiKey = "api:getUserInfo";

            // 1. 固定窗口：每秒最多10次请求
            System.out.println("=== 固定窗口限流 ===");
            for (int i = 0; i < 15; i++) {
                boolean allowed = limiter.fixedWindowLimiter(
                        "limiter:fixed:" + userId,
                        10,
                        1
                );
                System.out.println("请求" + (i + 1) + ": " + (allowed ? "通过" : "限流"));
            }

            // 2. 滑动窗口：10秒内最多50次请求
            System.out.println("\n=== 滑动窗口限流 ===");
            for (int i = 0; i < 55; i++) {
                boolean allowed = limiter.slidingWindowLimiterLua(
                        "limiter:sliding:" + userId + ":" + apiKey,
                        50,
                        10
                );
                if (i % 10 == 0) {
                    System.out.println("请求" + (i + 1) + ": " + (allowed ? "通过" : "限流"));
                }
            }

            // 3. 令牌桶：容量100，每秒生成10个令牌
            System.out.println("\n=== 令牌桶限流 ===");
            for (int i = 0; i < 5; i++) {
                boolean allowed = limiter.tokenBucketLimiter(
                        "limiter:token:" + userId,
                        100,
                        10
                );
                System.out.println("请求" + (i + 1) + ": " + (allowed ? "通过" : "限流"));
            }
        }
    }
}
