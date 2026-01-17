package com.concurrency.aqs.basic;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * AQS基本概念演示
 * 展示AQS的核心机制：state状态管理和队列操作
 */
public class AQSBasicDemo {

    /**
     * 简单的二元锁（0表示未锁定，1表示已锁定）
     * 演示AQS的独占模式
     */
    static class SimpleLock {

        private final Sync sync = new Sync();

        /**
         * 内部同步器，继承AQS
         */
        private static class Sync extends AbstractQueuedSynchronizer {

            /**
             * 尝试获取锁（独占模式）
             * @param arg 参数（这里未使用）
             * @return true表示获取成功，false表示失败
             */
            @Override
            protected boolean tryAcquire(int arg) {

                // 使用CAS（Compare-And-Swap）将state从0改为1
                // CAS是一种无锁的原子操作，在修改值之前会先比较当前值是否等于预期值，
                // CAS 操作的原理是: 比较当前值是否等于预期值，如果相等则更新为新值，否则不进行任何操作
                // 如果相等则更新为新值，否则不进行任何操作
                // compareAndSetState(0, 1) 是一个原子操作，尝试将 state 的值从 0 更新为 1

                // 这个方法利用了硬件级别的原子指令（如CAS - Compare And Swap）
                // 如果当前 state 值为 0（即锁未被占用），则将其设置为 1（表示锁已被占用），并返回 true
                // 如果当前 state 值不是 0（即锁已被其他线程占用），则不进行任何操作，返回 false
                // 这保证了在同一时刻只有一个线程能够成功获取锁，实现了线程安全的锁获取机制
                if (compareAndSetState(0, 1)) {
                    // 设置当前线程为独占线程
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }

            /**
             * 尝试释放锁（独占模式）
             * @param arg 参数（这里未使用）
             * @return true表示释放成功
             */
            @Override
            protected boolean tryRelease(int arg) {
                // 检查是否是当前线程持有锁
                if (getState() == 0) {
                    throw new IllegalMonitorStateException();
                }
                // 清除独占线程
                setExclusiveOwnerThread(null);
                // 释放锁，将state设为0
                setState(0);
                return true;
            }

            /**
             * 判断是否被当前线程独占
             */
            @Override
            protected boolean isHeldExclusively() {
                return getState() == 1 &&
                       getExclusiveOwnerThread() == Thread.currentThread();
            }

            /**
             * 判断锁是否被占用
             */
            boolean isLocked() {
                return getState() == 1;
            }
        }

        public void lock() {
            sync.acquire(1);
        }

        public void unlock() {
            sync.release(1);
        }

        public boolean isLocked() {
            return sync.isLocked();
        }
    }

    /**
     * 演示SimpleLock的使用
     */
    public static void main(String[] args) throws InterruptedException {
        SimpleLock lock = new SimpleLock();
        int[] counter = {0};

        System.out.println("=== AQS基本概念演示 ===\n");

        // 创建10个线程并发执行
        Thread[] threads = new Thread[100];
        for (int i = 0; i < 100; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    lock.lock();
                    try {
                        counter[0]++;
                        if (j == 0) {
                            System.out.println("线程 " + threadId + " 获取到锁，当前计数: " + counter[0]);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }, "Thread-" + i);
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n最终计数: " + counter[0]);
        System.out.println("预期计数: 10000");
        System.out.println("结果: " + (counter[0] == 10000 ? "✓ 正确" : "✗ 错误"));

        // 演示锁的状态
        System.out.println("\n=== 锁状态演示 ===");
        System.out.println("当前锁状态: " + (lock.isLocked() ? "已锁定" : "未锁定"));

        lock.lock();
        System.out.println("执行lock()后: " + (lock.isLocked() ? "已锁定" : "未锁定"));

        lock.unlock();
        System.out.println("执行unlock()后: " + (lock.isLocked() ? "已锁定" : "未锁定"));
    }
}
