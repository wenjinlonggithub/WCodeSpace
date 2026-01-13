package com.architecture.distributedlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redisson分布式锁示例（推荐方案）
 *
 * Redisson是Redis官方推荐的Java客户端，提供了完善的分布式锁实现
 *
 * 核心优势：
 * 1. 看门狗机制（WatchDog）：自动续期，防止业务执行时间过长导致锁过期
 * 2. 可重入锁：支持同一线程多次获取
 * 3. 公平锁：支持FIFO队列
 * 4. 读写锁：支持共享锁和排他锁
 * 5. 红锁（RedLock）：支持多Redis实例的强一致性锁
 *
 * Maven依赖：
 * <dependency>
 *     <groupId>org.redisson</groupId>
 *     <artifactId>redisson</artifactId>
 *     <version>3.17.7</version>
 * </dependency>
 */
public class RedissonLockExample {

    private static final Logger logger = LoggerFactory.getLogger(RedissonLockExample.class);

    /**
     * 示例代码（需要引入Redisson依赖）
     */
    public static void example() {
        /*
        // 1. 创建Redisson客户端
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://127.0.0.1:6379")
              .setPassword("password")
              .setDatabase(0);

        RedissonClient redisson = Redisson.create(config);

        // 2. 获取锁对象
        RLock lock = redisson.getLock("myLock");

        try {
            // 3. 加锁（阻塞等待）
            // 默认30秒过期，看门狗每10秒自动续期
            lock.lock();

            // 或者指定过期时间（不会自动续期）
            // lock.lock(10, TimeUnit.SECONDS);

            // 或者尝试加锁（非阻塞）
            // boolean locked = lock.tryLock(100, 10, TimeUnit.SECONDS);
            // 参数1：等待时间100ms
            // 参数2：锁过期时间10s

            logger.info("获取锁成功，开始执行业务逻辑");

            // 执行业务逻辑
            Thread.sleep(60000); // 模拟长时间业务
            // 即使业务执行60秒，看门狗会自动续期，不会锁过期

            logger.info("业务逻辑执行完成");

        } catch (InterruptedException e) {
            logger.error("业务执行被中断", e);
        } finally {
            // 4. 释放锁
            // 只有锁的持有者才能释放，Redisson会自动校验
            lock.unlock();
            logger.info("释放锁成功");
        }

        // 5. 关闭客户端
        redisson.shutdown();
        */
    }

    /**
     * 看门狗机制（WatchDog）原理
     *
     * 问题：业务执行时间超过锁的过期时间，导致锁自动释放
     * 解决：Redisson的看门狗机制
     *
     * 实现原理：
     * 1. 加锁时，Redisson会启动一个定时任务（默认每10秒执行一次）
     * 2. 定时任务检查锁是否还被当前线程持有
     * 3. 如果持有，则延长锁的过期时间（默认延长30秒）
     * 4. 释放锁时，定时任务自动取消
     *
     * 关键代码（Redisson内部实现）：
     * scheduleExpirationRenewal(threadId) {
     *     Timeout task = timer.newTimeout(timeout -> {
     *         // 检查锁是否还存在
     *         if (lockExists()) {
     *             // 延长过期时间
     *             redis.expire(lockKey, 30, TimeUnit.SECONDS);
     *             // 继续调度下一次续期
     *             scheduleExpirationRenewal(threadId);
     *         }
     *     }, 10, TimeUnit.SECONDS);
     * }
     *
     * 注意：只有使用lock()方法（不指定过期时间）时，看门狗才会生效
     */

    /**
     * 可重入锁示例
     */
    public static void reentrantLockExample() {
        /*
        RedissonClient redisson = Redisson.create();
        RLock lock = redisson.getLock("reentrantLock");

        lock.lock();
        try {
            logger.info("第一次获取锁");

            // 同一线程可以重复获取锁
            lock.lock();
            try {
                logger.info("第二次获取锁（重入）");
            } finally {
                lock.unlock();
            }

        } finally {
            lock.unlock();
        }

        redisson.shutdown();
        */
    }

    /**
     * 公平锁示例（FIFO）
     */
    public static void fairLockExample() {
        /*
        RedissonClient redisson = Redisson.create();

        // 获取公平锁
        RLock fairLock = redisson.getFairLock("fairLock");

        // 多个线程竞争锁时，按照请求顺序获取（FIFO）
        fairLock.lock();
        try {
            logger.info("获取公平锁成功");
        } finally {
            fairLock.unlock();
        }

        redisson.shutdown();
        */
    }

    /**
     * 读写锁示例
     */
    public static void readWriteLockExample() {
        /*
        RedissonClient redisson = Redisson.create();
        RReadWriteLock rwLock = redisson.getReadWriteLock("rwLock");

        // 读锁（共享锁）：多个线程可以同时持有
        RLock readLock = rwLock.readLock();
        readLock.lock();
        try {
            logger.info("读取数据");
        } finally {
            readLock.unlock();
        }

        // 写锁（排他锁）：只有一个线程可以持有
        RLock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            logger.info("写入数据");
        } finally {
            writeLock.unlock();
        }

        redisson.shutdown();
        */
    }

    /**
     * RedLock（红锁）示例
     *
     * 解决问题：
     * 单Redis实例在主从切换时可能丢失锁（异步复制）
     *
     * 实现原理：
     * 1. 同时向多个独立的Redis实例（N个）请求加锁
     * 2. 超过半数（N/2+1）的实例加锁成功，才算成功
     * 3. 加锁耗时要小于锁的过期时间
     *
     * 使用场景：
     * 对锁的可靠性要求极高的场景（如金融系统）
     */
    public static void redLockExample() {
        /*
        // 创建多个Redis客户端
        RedissonClient redisson1 = Redisson.create(config1);
        RedissonClient redisson2 = Redisson.create(config2);
        RedissonClient redisson3 = Redisson.create(config3);

        // 获取RedLock
        RLock lock1 = redisson1.getLock("lock");
        RLock lock2 = redisson2.getLock("lock");
        RLock lock3 = redisson3.getLock("lock");

        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);

        try {
            // 尝试加锁
            // 100ms等待时间，10s锁过期时间
            boolean locked = redLock.tryLock(100, 10000, TimeUnit.MILLISECONDS);

            if (locked) {
                logger.info("RedLock获取成功");
                // 执行业务
            }
        } catch (InterruptedException e) {
            logger.error("RedLock异常", e);
        } finally {
            redLock.unlock();
        }

        redisson1.shutdown();
        redisson2.shutdown();
        redisson3.shutdown();
        */
    }
}
