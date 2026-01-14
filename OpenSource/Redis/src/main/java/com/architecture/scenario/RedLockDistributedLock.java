package com.architecture.scenario;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Redlock算法实现 - Redis分布式锁的高可用方案
 *
 * 核心思想：
 * 1. 部署N个独立的Redis实例（非主从关系）
 * 2. 向所有实例请求加锁
 * 3. 超过半数(N/2+1)成功才算加锁成功
 * 4. 考虑时钟漂移，有效时间 = TTL - 耗时 - 时钟漂移
 *
 * 解决的问题：
 * - 主从架构下，主节点宕机导致锁丢失
 * - 单点故障
 *
 * 参考：https://redis.io/docs/manual/patterns/distributed-locks/
 *
 * @author Redis Architect
 * @date 2026-01-14
 */
public class RedLockDistributedLock {

    /**
     * Redis实例列表（必须是独立实例，不能是主从）
     */
    private List<Jedis> redisInstances;

    /**
     * 锁的key
     */
    private String lockKey;

    /**
     * 锁的唯一标识值
     */
    private String lockValue;

    /**
     * 锁的过期时间（毫秒）
     */
    private int lockTTL;

    /**
     * 时钟漂移因子（默认0.01，即1%）
     */
    private static final double CLOCK_DRIFT_FACTOR = 0.01;

    /**
     * 最小有效时间（毫秒）
     */
    private static final int MIN_VALIDITY_TIME = 10;

    /**
     * 构造函数
     *
     * @param redisHosts Redis实例地址列表（建议5个：容忍2个故障）
     * @param lockKey 锁的key
     * @param lockTTL 锁的TTL（毫秒）
     */
    public RedLockDistributedLock(List<String> redisHosts, String lockKey, int lockTTL) {
        this.redisInstances = new ArrayList<>();

        // 初始化Redis连接
        for (String host : redisHosts) {
            String[] parts = host.split(":");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            this.redisInstances.add(new Jedis(ip, port));
        }

        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString() + ":" + Thread.currentThread().getId();
        this.lockTTL = lockTTL;
    }

    /**
     * 尝试获取锁（Redlock算法核心）
     *
     * 流程：
     * 1. 获取当前时间戳
     * 2. 依次向N个Redis实例请求加锁
     * 3. 只有当超过半数实例加锁成功，且总耗时 < TTL时，才算成功
     * 4. 如果失败，释放所有已加的锁
     *
     * @return 是否加锁成功
     */
    public boolean tryLock() {
        // 1. 记录开始时间
        long startTime = System.currentTimeMillis();

        // 2. 向所有Redis实例请求加锁
        int successCount = 0;
        Set<Jedis> lockedInstances = new HashSet<>();

        for (Jedis jedis : redisInstances) {
            try {
                // 向单个实例加锁（带超时控制）
                if (lockInstance(jedis, lockKey, lockValue, lockTTL)) {
                    successCount++;
                    lockedInstances.add(jedis);
                }
            } catch (Exception e) {
                // 网络异常或Redis宕机，跳过该实例
                System.err.println("加锁失败: " + jedis + ", 原因: " + e.getMessage());
            }
        }

        // 3. 计算耗时
        long elapsed = System.currentTimeMillis() - startTime;

        // 4. 计算有效时间（考虑时钟漂移）
        long validityTime = lockTTL - elapsed - (long) (lockTTL * CLOCK_DRIFT_FACTOR);

        // 5. 判断是否加锁成功
        int quorum = redisInstances.size() / 2 + 1;  // 超过半数

        if (successCount >= quorum && validityTime > MIN_VALIDITY_TIME) {
            // 成功：超过半数实例加锁成功，且有效时间足够
            System.out.println("✅ Redlock加锁成功！");
            System.out.println("   - 成功实例: " + successCount + "/" + redisInstances.size());
            System.out.println("   - 耗时: " + elapsed + "ms");
            System.out.println("   - 有效时间: " + validityTime + "ms");
            return true;
        } else {
            // 失败：释放所有已加的锁
            System.out.println("❌ Redlock加锁失败！");
            System.out.println("   - 成功实例: " + successCount + "/" + redisInstances.size());
            System.out.println("   - 需要实例: " + quorum);
            System.out.println("   - 耗时: " + elapsed + "ms");
            System.out.println("   - 有效时间: " + validityTime + "ms");

            unlockInstances(lockedInstances);
            return false;
        }
    }

    /**
     * 向单个Redis实例加锁
     *
     * @param jedis Redis连接
     * @param key 锁key
     * @param value 锁value
     * @param ttl 过期时间（毫秒）
     * @return 是否成功
     */
    private boolean lockInstance(Jedis jedis, String key, String value, int ttl) {
        try {
            SetParams params = SetParams.setParams()
                .nx()           // 不存在时才设置
                .px(ttl);       // 过期时间（毫秒）

            String result = jedis.set(key, value, params);
            return "OK".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 释放锁（向所有实例释放）
     *
     * @return 成功释放的实例数量
     */
    public int unlock() {
        return unlockInstances(new HashSet<>(redisInstances));
    }

    /**
     * 释放指定实例上的锁
     *
     * @param instances 实例集合
     * @return 成功释放的实例数量
     */
    private int unlockInstances(Set<Jedis> instances) {
        // Lua脚本：验证value后删除
        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

        int unlockCount = 0;
        for (Jedis jedis : instances) {
            try {
                Object result = jedis.eval(
                    script,
                    Collections.singletonList(lockKey),
                    Collections.singletonList(lockValue)
                );

                if (Long.valueOf(1).equals(result)) {
                    unlockCount++;
                }
            } catch (Exception e) {
                System.err.println("解锁失败: " + jedis + ", 原因: " + e.getMessage());
            }
        }

        System.out.println("🔓 已释放 " + unlockCount + " 个实例的锁");
        return unlockCount;
    }

    /**
     * 自动续期（看门狗机制）
     *
     * 注意：Redlock场景下，需要向所有实例续期
     *
     * @return 成功续期的实例数量
     */
    public int renewal() {
        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('pexpire', KEYS[1], ARGV[2]) " +
            "else " +
            "    return 0 " +
            "end";

        int renewalCount = 0;
        for (Jedis jedis : redisInstances) {
            try {
                Object result = jedis.eval(
                    script,
                    Collections.singletonList(lockKey),
                    Arrays.asList(lockValue, String.valueOf(lockTTL))
                );

                if (Long.valueOf(1).equals(result)) {
                    renewalCount++;
                }
            } catch (Exception e) {
                System.err.println("续期失败: " + jedis + ", 原因: " + e.getMessage());
            }
        }

        return renewalCount;
    }

    /**
     * 启动看门狗线程
     */
    public Thread startWatchDog() {
        Thread watchDog = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 每隔TTL/3续期一次
                    Thread.sleep(lockTTL / 3);

                    int renewalCount = renewal();
                    int quorum = redisInstances.size() / 2 + 1;

                    if (renewalCount < quorum) {
                        System.err.println("⚠️ 续期失败！成功数: " + renewalCount + ", 需要: " + quorum);
                        // 可以选择中断业务或告警
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        watchDog.setDaemon(true);
        watchDog.setName("redlock-watchdog");
        watchDog.start();

        return watchDog;
    }

    /**
     * 关闭所有Redis连接
     */
    public void close() {
        for (Jedis jedis : redisInstances) {
            try {
                jedis.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        // 1. 准备5个独立的Redis实例
        List<String> redisHosts = Arrays.asList(
            "192.168.1.101:6379",
            "192.168.1.102:6379",
            "192.168.1.103:6379",
            "192.168.1.104:6379",
            "192.168.1.105:6379"
        );

        // 注意：演示时可以用localhost不同端口模拟
        List<String> demoHosts = Arrays.asList(
            "localhost:6379",
            "localhost:6380",
            "localhost:6381",
            "localhost:6382",
            "localhost:6383"
        );

        System.out.println("=== Redlock 分布式锁演示 ===\n");

        // 2. 创建Redlock实例
        RedLockDistributedLock redlock = new RedLockDistributedLock(
            Arrays.asList("localhost:6379"),  // 演示用，实际应该用多实例
            "lock:redlock:order:123",
            10000  // 10秒TTL
        );

        // 3. 尝试加锁
        if (redlock.tryLock()) {
            Thread watchDog = null;
            try {
                // 启动看门狗
                watchDog = redlock.startWatchDog();

                System.out.println("\n🔒 执行业务逻辑...");

                // 模拟业务处理
                Thread.sleep(3000);

                System.out.println("✅ 业务逻辑执行完成\n");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // 停止看门狗
                if (watchDog != null) {
                    watchDog.interrupt();
                }

                // 释放锁
                redlock.unlock();
            }
        } else {
            System.out.println("❌ 获取锁失败");
        }

        // 4. 关闭连接
        redlock.close();

        System.out.println("\n=== Redlock 演示完成 ===");
    }

    /**
     * 并发测试示例
     */
    public static void concurrentTest() throws InterruptedException {
        System.out.println("=== Redlock 并发测试 ===\n");

        List<String> hosts = Arrays.asList("localhost:6379");
        String lockKey = "lock:redlock:concurrent";
        int threadCount = 10;

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                RedLockDistributedLock lock = new RedLockDistributedLock(hosts, lockKey, 5000);

                if (lock.tryLock()) {
                    try {
                        System.out.println("✅ 线程" + threadId + " 获取锁成功");
                        Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                        System.out.println("🔓 线程" + threadId + " 释放锁");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                        lock.close();
                    }
                } else {
                    System.out.println("❌ 线程" + threadId + " 获取锁失败");
                    lock.close();
                }
            });

            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n=== 并发测试完成 ===");
    }
}
