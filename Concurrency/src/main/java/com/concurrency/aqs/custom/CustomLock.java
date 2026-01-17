package com.concurrency.aqs.custom;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义互斥锁实现
 * 基于AQS实现一个可重入的互斥锁，类似ReentrantLock
 */
public class CustomLock implements Lock {

    private final Sync sync;

    public CustomLock() {
        this(false);
    }

    public CustomLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    /**
     * 同步器基类
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {

        /**
         * 执行加锁操作
         */
        abstract void lock();

        /**
         * 非公平方式尝试获取锁
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();

            if (c == 0) {
                // 锁未被占用，直接CAS获取
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 可重入：当前线程已持有锁
                int nextc = c + acquires;
                if (nextc < 0) // 溢出检查
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        /**
         * 释放锁
         */
        @Override
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();

            boolean free = false;
            if (c == 0) {
                // 完全释放锁
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        @Override
        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }
    }

    /**
     * 非公平同步器
     */
    static final class NonfairSync extends Sync {
        @Override
        final void lock() {
            // 非公平：直接尝试CAS获取锁
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        @Override
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 公平同步器
     */
    static final class FairSync extends Sync {
        @Override
        final void lock() {
            acquire(1);
        }

        @Override
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();

            if (c == 0) {
                // 公平：检查队列中是否有等待线程
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 可重入
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    // Lock接口实现
    @Override
    public void lock() {
        sync.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    // 辅助方法
    public boolean isLocked() {
        return sync.isLocked();
    }

    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    public int getHoldCount() {
        return sync.getHoldCount();
    }

    public Thread getOwner() {
        return sync.getOwner();
    }

    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 自定义锁测试 ===\n");

        CustomLock lock = new CustomLock();
        int[] counter = {0};

        // 测试1：基本功能
        System.out.println("1. 基本加锁解锁测试");
        lock.lock();
        System.out.println("   锁已获取: " + lock.isLocked());
        System.out.println("   持有次数: " + lock.getHoldCount());
        lock.unlock();
        System.out.println("   锁已释放: " + !lock.isLocked());

        // 测试2：可重入性
        System.out.println("\n2. 可重入性测试");
        lock.lock();
        lock.lock();
        lock.lock();
        System.out.println("   持有次数: " + lock.getHoldCount());
        lock.unlock();
        lock.unlock();
        lock.unlock();
        System.out.println("   完全释放: " + !lock.isLocked());

        // 测试3：并发安全性
        System.out.println("\n3. 并发安全性测试");
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    lock.lock();
                    try {
                        counter[0]++;
                    } finally {
                        lock.unlock();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("   最终计数: " + counter[0]);
        System.out.println("   预期计数: 1000");
        System.out.println("   结果: " + (counter[0] == 1000 ? "✓ 通过" : "✗ 失败"));
    }
}
