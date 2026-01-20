package com.architecture.concurrent.spinlock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自适应自旋锁
 *
 * 原理：
 * - 结合自旋和阻塞的优点
 * - 先自旋 N 次，如果还未获取到锁，则进入阻塞
 * - 根据上次获取锁的情况动态调整自旋次数
 *
 * 自适应策略：
 * - 如果上次自旋成功，增加自旋次数
 * - 如果上次自旋失败，减少自旋次数
 * - 如果锁被同一线程多次获得，增加自旋次数
 *
 * JDK 中的应用：
 * - JDK 6+ 的 synchronized 使用自适应自旋优化
 * - JVM 自动调整自旋次数
 *
 * @author Architecture Learning
 */
public class AdaptiveSpinLock {

    /**
     * 锁状态
     */
    private final AtomicBoolean locked = new AtomicBoolean(false);

    /**
     * 最大自旋次数
     */
    private static final int MAX_SPIN_COUNT = 1000;

    /**
     * 最小自旋次数
     */
    private static final int MIN_SPIN_COUNT = 10;

    /**
     * 当前自旋次数（动态调整）
     */
    private volatile int spinCount = 100;

    /**
     * 上次获取锁的线程
     */
    private volatile Thread lastOwner;

    /**
     * 获取锁
     *
     * 策略：
     * 1. 先自旋 spinCount 次尝试获取锁
     * 2. 如果自旋期间获取到锁，增加 spinCount（认为自旋有效）
     * 3. 如果自旋失败，使用 Thread.yield() 让出 CPU
     * 4. 继续尝试，直到获取锁
     */
    public void lock() {
        Thread currentThread = Thread.currentThread();
        int spins = calculateSpinCount(currentThread);

        // 第一阶段：自旋尝试
        for (int i = 0; i < spins; i++) {
            if (locked.compareAndSet(false, true)) {
                // 获取锁成功
                lastOwner = currentThread;
                onSpinSuccess();  // 自旋成功，增加自旋次数
                return;
            }
            Thread.onSpinWait();  // 提示 CPU 当前在自旋
        }

        // 第二阶段：自旋失败，降低自旋次数，然后使用 yield
        onSpinFailure();

        // 第三阶段：继续尝试，但每次尝试后 yield
        while (!locked.compareAndSet(false, true)) {
            Thread.yield();  // 让出 CPU，给其他线程机会
        }

        lastOwner = currentThread;
    }

    /**
     * 计算自旋次数
     *
     * 自适应策略：
     * - 如果上次获取锁的是同一个线程，认为锁会很快释放，增加自旋次数
     * - 否则使用默认自旋次数
     */
    private int calculateSpinCount(Thread currentThread) {
        if (lastOwner == currentThread) {
            // 上次持有锁的是当前线程，可能很快再次获取
            return Math.min(spinCount * 2, MAX_SPIN_COUNT);
        }
        return spinCount;
    }

    /**
     * 自旋成功后的处理：增加自旋次数
     */
    private void onSpinSuccess() {
        // 自旋成功，说明自旋是有效的，增加自旋次数
        spinCount = Math.min(spinCount + 10, MAX_SPIN_COUNT);
    }

    /**
     * 自旋失败后的处理：减少自旋次数
     */
    private void onSpinFailure() {
        // 自旋失败，说明锁竞争激烈或持有时间长，减少自旋次数
        spinCount = Math.max(spinCount - 10, MIN_SPIN_COUNT);
    }

    /**
     * 尝试获取锁（非阻塞）
     */
    public boolean tryLock() {
        if (locked.compareAndSet(false, true)) {
            lastOwner = Thread.currentThread();
            return true;
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        locked.set(false);
    }

    /**
     * 获取当前自旋次数（用于监控和调试）
     */
    public int getCurrentSpinCount() {
        return spinCount;
    }
}
