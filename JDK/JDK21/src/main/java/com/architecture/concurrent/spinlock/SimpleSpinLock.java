package com.architecture.concurrent.spinlock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 简单自旋锁实现
 *
 * 原理：
 * - 使用 AtomicBoolean 作为锁状态（false=未锁定，true=已锁定）
 * - 通过 CAS 操作保证原子性
 * - 获取锁失败时在 while 循环中自旋
 *
 * 优点：
 * - 实现简单
 * - 无阻塞开销
 *
 * 缺点：
 * - 不公平（无法保证 FIFO）
 * - 可能导致线程饥饿
 * - 多个线程在同一个变量上自旋，导致缓存行争用
 *
 * @author Architecture Learning
 */
public class SimpleSpinLock {

    /**
     * 锁状态：false=未锁定，true=已锁定
     */
    private final AtomicBoolean locked = new AtomicBoolean(false);

    /**
     * 获取锁
     *
     * 执行流程：
     * 1. 尝试 CAS 将 locked 从 false 改为 true
     * 2. 如果成功，获取锁，方法返回
     * 3. 如果失败，说明锁被其他线程持有，继续自旋（循环重试）
     *
     * 自旋过程：
     * while (!locked.compareAndSet(false, true)) {
     *     // 空循环，一直尝试
     *     // 这里持续占用 CPU，直到获取锁成功
     * }
     */
    public void lock() {
        // 自旋：不断尝试 CAS，直到成功
        while (!locked.compareAndSet(false, true)) {
            // 空循环体，持续自旋
            // 这里可以添加 Thread.onSpinWait() 优化 CPU 占用
        }
    }

    /**
     * 尝试获取锁（非阻塞）
     *
     * @return 获取成功返回 true，失败返回 false（不自旋）
     */
    public boolean tryLock() {
        return locked.compareAndSet(false, true);
    }

    /**
     * 释放锁
     *
     * 直接将 locked 设为 false
     * 其他自旋的线程会立即感知到并尝试获取锁
     */
    public void unlock() {
        locked.set(false);
    }

    /**
     * 检查是否已锁定
     */
    public boolean isLocked() {
        return locked.get();
    }
}
