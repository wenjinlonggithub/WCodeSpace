package com.architecture.batchprocess.service;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多级限流器
 * 支持全局限流和API级别限流
 */
public class MultiLevelRateLimiter {

    // 全局限流器：每秒100个请求
    private final RateLimiter globalLimiter;

    // 单个API限流器：每秒30个请求
    private final Map<String, RateLimiter> apiLimiters;

    private final double globalQps;
    private final double apiQps;

    public MultiLevelRateLimiter() {
        this(100.0, 30.0);
    }

    public MultiLevelRateLimiter(double globalQps, double apiQps) {
        this.globalQps = globalQps;
        this.apiQps = apiQps;
        this.globalLimiter = RateLimiter.create(globalQps);
        this.apiLimiters = new ConcurrentHashMap<>();
    }

    /**
     * 获取令牌（阻塞等待）
     * @param apiName API名称
     */
    public void acquire(String apiName) {
        // 先通过全局限流

        // 调用Guava RateLimiter的acquire方法，该方法内部使用AQS（AbstractQueuedSynchronizer）实现
        // 1. 首先尝试CAS获取许可，如果当前可用许可大于0，则直接减少许可数
        // 2. 如果许可不足，线程会被封装成Node节点加入到同步队列的尾部
        // 3. 使用LockSupport.park()使线程进入阻塞状态
        // 4. 当其他线程释放许可后，会唤醒同步队列头部的线程
        // 5. 被唤醒的线程重新尝试获取许可，获取成功后继续执行
        // acquire()方法没有超时机制，会一直等待直到获取到许可



        /*
         * AQS（AbstractQueuedSynchronizer）是Java并发包中的一个抽象类，它是构建锁和其他同步组件的基础框架。
         * 
         * 原理：
         * 1. AQS使用一个int类型的state变量作为计数器，表示同步状态
         * 2. 使用双向链表构成同步队列，维护等待的线程
         * 3. 通过CAS操作保证state更新的原子性
         * 4. 当线程获取同步状态失败时，会被构造成Node节点加入同步队列并阻塞
         * 5. 当同步状态被释放时，会通知头节点的后继节点恢复运行
         * 
         * 作用：
         * 1. 提供了标准的同步状态管理机制
         * 2. 实现线程等待/通知机制（阻塞与唤醒）
         * 3. 为ReentrantLock、Semaphore、CountDownLatch等同步组件提供基础支持
         * 
         * 场景：
         * 1. 独占式同步资源访问（如ReentrantLock）
         * 2. 共享式同步资源访问（如Semaphore）
         * 3. 各种自定义的同步工具类实现
         * 
         * 在本代码中，RateLimiter内部使用AQS来控制请求的阻塞和唤醒，确保请求按设定的速率处理
         */


        //AQS 原理：使用CAS操作保证state更新的原子性，通过双向链表构成同步队列，维护等待的线程
        // AQS 线程等待/通知机制：通过LockSupport.park()使线程进入阻塞状态，当同步状态被释放时，会通知头节点的后继节点恢复运行
        // 核心代码：AQS的acquire()方法内部使用CAS操作保证state更新的原子性，通过LockSupport.park()使线程进入阻塞状态，当同步状态被释放时，会通知头节点的后继节点恢复运行


        // 使用全局限流器获取令牌，如果当前请求速率超过限制，会阻塞等待直到获得许可
        globalLimiter.acquire();

        // 再通过API级别限流
        RateLimiter apiLimiter = apiLimiters.computeIfAbsent(
            apiName, k -> RateLimiter.create(apiQps)
        );
        apiLimiter.acquire();
    }

    /**
     * 尝试获取令牌（非阻塞）
     * @param apiName API名称
     * @return 是否成功获取令牌
     */
    public boolean tryAcquire(String apiName) {
        // 先尝试全局限流
        if (!globalLimiter.tryAcquire()) {
            return false;
        }

        // 再尝试API级别限流
        RateLimiter apiLimiter = apiLimiters.computeIfAbsent(
            apiName, k -> RateLimiter.create(apiQps)
        );
        return apiLimiter.tryAcquire();
    }

    /**
     * 动态调整全局QPS
     */
    public void setGlobalQps(double qps) {
        globalLimiter.setRate(qps);
    }

    /**
     * 动态调整指定API的QPS
     */
    public void setApiQps(String apiName, double qps) {
        RateLimiter apiLimiter = apiLimiters.get(apiName);
        if (apiLimiter != null) {
            apiLimiter.setRate(qps);
        }
    }

    public double getGlobalQps() {
        return globalQps;
    }

    public double getApiQps() {
        return apiQps;
    }
}
