package com.concurrency.aqs.custom;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 自定义共享锁实现
 * 基于AQS实现一个共享锁，允许指定数量的线程同时访问
 * 类似Semaphore的功能
 */
public class CustomSharedLock {

    private final Sync sync;

    public CustomSharedLock(int permits) {
        sync = new Sync(permits);
    }

    /**
     * 共享模式同步器
     */
    private static final class Sync extends AbstractQueuedSynchronizer {

        Sync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }

        /**
         * 共享模式获取资源
         * @param acquires 要获取的许可数
         * @return 剩余许可数，负数表示获取失败
         */
        @Override
        protected int tryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;

                // 如果剩余许可不足，或CAS成功，则返回
                if (remaining < 0 || compareAndSetState(available, remaining)) {
                    return remaining;
                }
            }
        }

        /**
         * 共享模式释放资源
         * @param releases 要释放的许可数
         * @return 总是返回true
         */
        @Override
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;

                if (next < current) // 溢出检查
                    throw new Error("Maximum permit count exceeded");

                if (compareAndSetState(current, next))
                    return true;
            }
        }

        /**
         * 尝试减少许可（非阻塞）
         */
        final boolean tryReducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;

                if (next < 0)
                    return false;

                if (compareAndSetState(current, next))
                    return true;
            }
        }
    }

    /**
     * 获取一个许可（阻塞）
     */
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 获取指定数量的许可（阻塞）
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }

    /**
     * 尝试获取一个许可（非阻塞）
     */
    public boolean tryAcquire() {
        return sync.tryAcquireShared(1) >= 0;
    }

    /**
     * 尝试获取指定数量的许可（非阻塞）
     */
    public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireShared(permits) >= 0;
    }

    /**
     * 释放一个许可
     */
    public void release() {
        sync.releaseShared(1);
    }

    /**
     * 释放指定数量的许可
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

    /**
     * 获取可用许可数
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 自定义共享锁测试 ===\n");

        CustomSharedLock sharedLock = new CustomSharedLock(3);

        System.out.println("1. 初始许可数: " + sharedLock.availablePermits());

        // 测试1：基本获取和释放
        System.out.println("\n2. 基本获取释放测试");
        sharedLock.acquire();
        System.out.println("   获取1个许可后: " + sharedLock.availablePermits());
        sharedLock.acquire(2);
        System.out.println("   再获取2个许可后: " + sharedLock.availablePermits());
        sharedLock.release();
        System.out.println("   释放1个许可后: " + sharedLock.availablePermits());
        sharedLock.release(2);
        System.out.println("   再释放2个许可后: " + sharedLock.availablePermits());

        // 测试2：并发访问控制
        System.out.println("\n3. 并发访问控制测试（最多3个线程同时执行）");
        CustomSharedLock pool = new CustomSharedLock(3);

        Thread[] threads = new Thread[6];
        for (int i = 0; i < 6; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("   线程" + id + " 等待获取许可...");
                    pool.acquire();
                    System.out.println("   线程" + id + " 获取许可，开始执行（剩余: " +
                        pool.availablePermits() + "）");
                    Thread.sleep(1000); // 模拟工作
                    System.out.println("   线程" + id + " 完成，释放许可");
                    pool.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Thread-" + i);
            threads[i].start();
            Thread.sleep(100); // 错开启动时间
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n4. 最终许可数: " + pool.availablePermits());
        System.out.println("   结果: " + (pool.availablePermits() == 3 ? "✓ 通过" : "✗ 失败"));
    }
}
