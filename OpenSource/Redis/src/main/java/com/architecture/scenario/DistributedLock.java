package com.architecture.scenario;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

/**
 * Redis分布式锁实现
 *
 * 应用场景：
 * - 防止库存超卖
 * - 避免重复提交
 * - 定时任务防止重复执行
 *
 * 实现要点：
 * 1. 互斥性：任意时刻只有一个客户端能持有锁
 * 2. 不会死锁：即使持有锁的客户端崩溃，锁最终也能被释放
 * 3. 加锁和解锁必须是同一个客户端
 * 4. 容错性：只要大部分Redis节点正常运行，就能正常加锁解锁
 */
public class DistributedLock {

    private Jedis jedis;
    private String lockKey;
    private String lockValue;
    private int expireTime; // 过期时间（秒）

    public DistributedLock(Jedis jedis, String lockKey, int expireTime) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.lockValue = UUID.randomUUID().toString();
    }

    /**
     * 加锁 - 使用SET NX EX命令
     * SET key value NX EX seconds
     * NX: 只在键不存在时设置
     * EX: 设置过期时间
     */
    public boolean tryLock() {
        SetParams params = new SetParams();
        params.nx(); // 不存在时才设置
        params.ex(expireTime); // 设置过期时间

        String result = jedis.set(lockKey, lockValue, params);
        return "OK".equals(result);
    }

    /**
     * 加锁（阻塞等待）
     * @param timeout 超时时间（毫秒）
     */
    public boolean lock(long timeout) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (tryLock()) {
                return true;
            }

            // 检查是否超时
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
     * 解锁 - 使用Lua脚本保证原子性
     * 必须验证lockValue，确保只能删除自己加的锁
     */
    public boolean unlock() {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
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
     * 续期 - 延长锁的过期时间
     * 用于长时间任务，防止锁自动过期
     */
    public boolean renewal() {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                "    return 0 " +
                "end";

        Object result = jedis.eval(
                script,
                Collections.singletonList(lockKey),
                java.util.Arrays.asList(lockValue, String.valueOf(expireTime))
        );

        return Long.valueOf(1).equals(result);
    }

    /**
     * 自动续期（看门狗机制）
     * 启动后台线程定期续期
     */
    public Thread startWatchDog() {
        Thread watchDog = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 每隔expireTime/3的时间续期一次
                    Thread.sleep(expireTime * 1000L / 3);
                    renewal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        watchDog.setDaemon(true);
        watchDog.setName("redis-lock-watchdog");
        watchDog.start();
        return watchDog;
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            DistributedLock lock = new DistributedLock(jedis, "order:123", 10);

            // 尝试加锁
            if (lock.tryLock()) {
                try {
                    System.out.println("获取锁成功，执行业务逻辑...");

                    // 模拟业务处理
                    Thread.sleep(2000);

                    System.out.println("业务逻辑执行完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 释放锁
                    if (lock.unlock()) {
                        System.out.println("释放锁成功");
                    } else {
                        System.out.println("释放锁失败");
                    }
                }
            } else {
                System.out.println("获取锁失败");
            }
        }
    }
}
