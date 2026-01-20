package com.architecture.concurrent.countdownlatch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * CountDownLatch 源码实现原理学习
 *
 * 核心原理：
 * 1. 基于 AQS (AbstractQueuedSynchronizer) 实现
 * 2. 使用 AQS 的 state 存储计数器值
 * 3. 使用共享模式：一次唤醒所有等待线程
 * 4. 使用 CAS 保证计数器减少的线程安全
 *
 * @author Architecture Learning
 */
public class MyCountDownLatch {

    /**
     * 内部同步器：基于 AQS 的共享模式实现
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        /**
         * 构造器：初始化计数器
         * @param count 初始计数值，存储在 AQS 的 state 中
         */
        Sync(int count) {
            setState(count);  // 设置 AQS 的 state 为计数值
        }

        /**
         * 获取当前计数值
         */
        int getCount() {
            return getState();
        }

        /**
         * 尝试获取共享锁（await 方法调用）
         *
         * 返回值含义：
         * - 返回值 >= 0：获取成功，线程可以继续执行
         * - 返回值 < 0：获取失败，线程需要进入等待队列阻塞
         *
         * @param acquires 参数（这里未使用）
         * @return state == 0 时返回 1（成功），否则返回 -1（失败）
         */
        protected int tryAcquireShared(int acquires) {
            // 计数器为 0 时，所有等待线程可以通过
            return (getState() == 0) ? 1 : -1;
        }

        /**
         * 尝试释放共享锁（countDown 方法调用）
         *
         * 核心逻辑：使用 CAS 将计数器减 1
         *
         * @param releases 释放的数量（这里固定为 1）
         * @return 如果减到 0 返回 true（触发唤醒），否则返回 false
         */
        protected boolean tryReleaseShared(int releases) {
            // 自旋 + CAS 实现无锁的线程安全操作
            for (;;) {
                int c = getState();           // 1. 读取当前计数值

                if (c == 0)                   // 2. 已经是 0，无需再减（这就是为什么不可重置）
                    return false;

                int nextc = c - 1;            // 3. 计算新的计数值

                // 4. CAS 原子更新 state
                if (compareAndSetState(c, nextc)) {
                    // CAS 成功：
                    // - 如果减到 0，返回 true，触发 doReleaseShared() 唤醒等待线程
                    // - 如果还没到 0，返回 false，不唤醒
                    return nextc == 0;
                }
                // CAS 失败：说明有其他线程修改了 state，继续循环重试
            }
        }
    }

    private final Sync sync;

    /**
     * 构造一个指定计数的 CountDownLatch
     *
     * @param count 初始计数（必须 >= 0）
     * @throws IllegalArgumentException 如果 count < 0
     */
    public MyCountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 使当前线程等待，直到计数器减到 0
     *
     * 执行流程：
     * 1. 调用 AQS 的 acquireSharedInterruptibly(1)
     * 2. 内部调用 tryAcquireShared(1)
     * 3. 如果 state != 0，返回 -1，线程进入等待队列并阻塞
     * 4. 如果 state == 0，返回 1，线程直接通过
     *
     * @throws InterruptedException 如果等待过程中被中断
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 使当前线程等待，直到计数器减到 0 或超时
     *
     * @param timeout 最大等待时间
     * @param unit 时间单位
     * @return 如果计数器减到 0 返回 true，超时返回 false
     * @throws InterruptedException 如果等待过程中被中断
     */
    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 计数器减 1，如果减到 0 则唤醒所有等待线程
     *
     * 执行流程：
     * 1. 调用 AQS 的 releaseShared(1)
     * 2. 内部调用 tryReleaseShared(1)：CAS 将 state 减 1
     * 3. 如果减到 0，tryReleaseShared 返回 true
     * 4. AQS 调用 doReleaseShared() 唤醒等待队列中的所有线程
     *
     * 关键点：
     * - 使用 CAS 保证线程安全
     * - 自旋重试保证并发场景下的正确性
     * - 只有减到 0 时才唤醒线程（共享模式一次性唤醒所有）
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 获取当前计数值
     * 主要用于调试和测试
     */
    public long getCount() {
        return sync.getCount();
    }

    @Override
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
