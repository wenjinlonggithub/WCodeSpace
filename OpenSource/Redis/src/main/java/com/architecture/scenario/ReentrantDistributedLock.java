package com.architecture.scenario;

import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * 可重入分布式锁实现
 *
 * 核心思想：
 * 1. 使用Redis Hash结构存储锁
 * 2. Field = uuid:threadId（标识加锁的线程）
 * 3. Value = 重入次数（count）
 * 4. 同一线程可以多次加锁，每次count+1
 * 5. 解锁时count-1，count=0时删除锁
 *
 * 数据结构：
 * HSET lock_key {uuid:threadId} {count}
 *
 * 示例：
 * HGETALL myLock
 * 1) "uuid-abc-123:thread-456"
 * 2) "3"  ← 该线程重入了3次
 *
 * 对比普通锁：
 * - 普通锁：String类型，value=uuid
 * - 可重入锁：Hash类型，field=uuid:threadId, value=count
 *
 * @author Redis Architect
 * @date 2026-01-14
 */
public class ReentrantDistributedLock {

    private Jedis jedis;
    private String lockKey;
    private String lockValue;  // uuid:threadId
    private int expireTime;    // 过期时间（秒）

    /**
     * 构造函数
     */
    public ReentrantDistributedLock(Jedis jedis, String lockKey, int expireTime) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.expireTime = expireTime;

        // lockValue = UUID + 线程ID
        this.lockValue = UUID.randomUUID().toString() + ":" + Thread.currentThread().getId();
    }

    /**
     * 加锁（支持重入）
     *
     * Lua脚本逻辑：
     * 1. 如果锁不存在：HINCRBY field +1，设置过期时间，返回nil（成功）
     * 2. 如果锁存在且是当前线程：HINCRBY field +1，刷新过期时间，返回nil（重入成功）
     * 3. 如果锁被其他线程持有：返回剩余TTL（失败）
     *
     * @return 是否加锁成功
     */
    public boolean tryLock() {
        // Lua脚本保证原子性
        String script =
            // 1. 锁不存在，创建锁
            "if (redis.call('exists', KEYS[1]) == 0) then " +
            "    redis.call('hincrby', KEYS[1], ARGV[2], 1); " +  // count = 1
            "    redis.call('expire', KEYS[1], ARGV[1]); " +      // 设置过期时间
            "    return nil; " +                                   // 返回nil表示成功
            "end; " +

            // 2. 锁存在且是当前线程，重入
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
            "    redis.call('hincrby', KEYS[1], ARGV[2], 1); " +  // count +1
            "    redis.call('expire', KEYS[1], ARGV[1]); " +      // 刷新过期时间
            "    return nil; " +                                   // 返回nil表示重入成功
            "end; " +

            // 3. 锁被其他线程持有
            "return redis.call('pttl', KEYS[1]);";               // 返回剩余TTL

        Object result = jedis.eval(
            script,
            Collections.singletonList(lockKey),
            Arrays.asList(String.valueOf(expireTime), lockValue)
        );

        // result == null 表示加锁成功（包括首次和重入）
        if (result == null) {
            return true;
        }

        // result 是数字，表示锁被占用，返回剩余TTL（毫秒）
        System.out.println("锁被占用，剩余时间: " + result + "ms");
        return false;
    }

    /**
     * 阻塞加锁（支持重入）
     *
     * @param timeout 超时时间（毫秒）
     * @return 是否加锁成功
     */
    public boolean lock(long timeout) {
        long startTime = System.currentTimeMillis();

        while (true) {
            if (tryLock()) {
                return true;
            }

            // 检查超时
            if (System.currentTimeMillis() - startTime > timeout) {
                return false;
            }

            // 短暂休眠后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 解锁（支持重入）
     *
     * Lua脚本逻辑：
     * 1. 如果锁不存在或不是当前线程：返回nil（失败）
     * 2. 如果是当前线程：count -1
     *    - 如果 count > 0：保留锁，刷新过期时间，返回0
     *    - 如果 count == 0：删除锁，返回1
     *
     * @return 是否解锁成功
     */
    public boolean unlock() {
        String script =
            // 1. 验证锁是否存在且属于当前线程
            "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
            "    return nil;" +  // 锁不存在或不属于当前线程
            "end; " +

            // 2. count -1
            "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +

            // 3. 判断count
            "if (counter > 0) then " +
            "    redis.call('expire', KEYS[1], ARGV[2]); " +  // 还有重入，刷新过期时间
            "    return 0; " +
            "else " +
            "    redis.call('del', KEYS[1]); " +              // count=0，删除锁
            "    return 1; " +
            "end; " +

            "return nil;";

        Object result = jedis.eval(
            script,
            Collections.singletonList(lockKey),
            Arrays.asList("1", String.valueOf(expireTime), lockValue)
        );

        // result == 1 表示完全释放锁
        // result == 0 表示还有重入，未完全释放
        // result == null 表示锁不存在或不属于当前线程
        return result != null;
    }

    /**
     * 获取当前锁的重入次数
     *
     * @return 重入次数，如果未持有锁返回0
     */
    public int getHoldCount() {
        String value = jedis.hget(lockKey, lockValue);
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    /**
     * 检查当前线程是否持有锁
     *
     * @return 是否持有锁
     */
    public boolean isHeldByCurrentThread() {
        return jedis.hexists(lockKey, lockValue);
    }

    /**
     * 强制释放锁（不管重入次数）
     *
     * 注意：仅用于异常情况，正常情况应该调用unlock()
     *
     * @return 是否成功
     */
    public boolean forceUnlock() {
        String script =
            "if redis.call('hexists', KEYS[1], ARGV[1]) == 1 then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

        Object result = jedis.eval(
            script,
            Collections.singletonList(lockKey),
            Collections.singletonList(lockValue)
        );

        return Long.valueOf(1).equals(result);
    }

    /**
     * 使用示例1：基本用法
     */
    public static void basicExample() {
        System.out.println("=== 可重入锁基本用法 ===\n");

        try (Jedis jedis = new Jedis("localhost", 6379)) {
            ReentrantDistributedLock lock = new ReentrantDistributedLock(
                jedis, "lock:reentrant:order:123", 10
            );

            // 第一次加锁
            if (lock.tryLock()) {
                System.out.println("✅ 第一次加锁成功，重入次数: " + lock.getHoldCount());

                try {
                    // 第二次加锁（重入）
                    if (lock.tryLock()) {
                        System.out.println("✅ 第二次加锁成功（重入），重入次数: " + lock.getHoldCount());

                        try {
                            // 第三次加锁（重入）
                            if (lock.tryLock()) {
                                System.out.println("✅ 第三次加锁成功（重入），重入次数: " + lock.getHoldCount());

                                try {
                                    System.out.println("🔒 执行业务逻辑...");
                                    Thread.sleep(1000);
                                } finally {
                                    lock.unlock();
                                    System.out.println("🔓 第三次解锁，重入次数: " + lock.getHoldCount());
                                }
                            }
                        } finally {
                            lock.unlock();
                            System.out.println("🔓 第二次解锁，重入次数: " + lock.getHoldCount());
                        }
                    }
                } finally {
                    lock.unlock();
                    System.out.println("🔓 第一次解锁，重入次数: " + lock.getHoldCount());
                }
            }

            System.out.println("\n✅ 锁已完全释放");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用示例2：递归调用场景
     */
    public static void recursiveExample() {
        System.out.println("=== 可重入锁递归调用示例 ===\n");

        try (Jedis jedis = new Jedis("localhost", 6379)) {
            ReentrantDistributedLock lock = new ReentrantDistributedLock(
                jedis, "lock:reentrant:recursive", 10
            );

            // 递归调用
            recursiveMethod(lock, 5);

            System.out.println("\n✅ 递归调用完成");
        }
    }

    /**
     * 递归方法（每次调用都会加锁）
     */
    private static void recursiveMethod(ReentrantDistributedLock lock, int depth) {
        if (depth == 0) return;

        if (lock.tryLock()) {
            try {
                System.out.println("递归深度: " + depth + ", 重入次数: " + lock.getHoldCount());
                recursiveMethod(lock, depth - 1);  // 递归调用
            } finally {
                lock.unlock();
                System.out.println("递归返回: " + depth + ", 重入次数: " + lock.getHoldCount());
            }
        }
    }

    /**
     * 使用示例3：多线程场景
     */
    public static void multiThreadExample() throws InterruptedException {
        System.out.println("=== 可重入锁多线程示例 ===\n");

        try (Jedis jedis = new Jedis("localhost", 6379)) {
            String lockKey = "lock:reentrant:multithread";

            // 线程1：持有锁并重入3次
            Thread thread1 = new Thread(() -> {
                ReentrantDistributedLock lock = new ReentrantDistributedLock(jedis, lockKey, 10);

                if (lock.tryLock()) {
                    try {
                        System.out.println("线程1: 第1次加锁，重入次数: " + lock.getHoldCount());

                        if (lock.tryLock()) {
                            try {
                                System.out.println("线程1: 第2次加锁（重入），重入次数: " + lock.getHoldCount());

                                if (lock.tryLock()) {
                                    try {
                                        System.out.println("线程1: 第3次加锁（重入），重入次数: " + lock.getHoldCount());
                                        Thread.sleep(3000);  // 持有锁3秒
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } finally {
                                        lock.unlock();
                                        System.out.println("线程1: 第3次解锁，重入次数: " + lock.getHoldCount());
                                    }
                                }
                            } finally {
                                lock.unlock();
                                System.out.println("线程1: 第2次解锁，重入次数: " + lock.getHoldCount());
                            }
                        }
                    } finally {
                        lock.unlock();
                        System.out.println("线程1: 第1次解锁，重入次数: " + lock.getHoldCount());
                    }
                }
            });

            // 线程2：尝试获取锁（应该失败）
            Thread thread2 = new Thread(() -> {
                try {
                    Thread.sleep(500);  // 等待线程1先加锁

                    ReentrantDistributedLock lock = new ReentrantDistributedLock(jedis, lockKey, 10);
                    if (lock.tryLock()) {
                        System.out.println("线程2: 加锁成功");
                        lock.unlock();
                    } else {
                        System.out.println("线程2: 加锁失败（锁被线程1持有）");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            System.out.println("\n✅ 多线程测试完成");
        }
    }

    /**
     * 主函数
     */
    public static void main(String[] args) {
        try {
            // 示例1：基本用法
            basicExample();
            System.out.println("\n" + "=".repeat(50) + "\n");

            // 示例2：递归调用
            recursiveExample();
            System.out.println("\n" + "=".repeat(50) + "\n");

            // 示例3：多线程场景
            multiThreadExample();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
