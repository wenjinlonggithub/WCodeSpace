package com.concurrency.aqs.basic;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * ReentrantLock使用示例
 * 演示可重入锁的各种特性
 */
public class ReentrantLockDemo {

    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    /**
     * 演示基本的加锁和解锁
     */
    public void basicLockUsage() {
        lock.lock();
        try {
            count++;
            System.out.println(Thread.currentThread().getName() + " - 计数: " + count);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 演示可重入特性
     */
    public void reentrantFeature() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " - 第一次获取锁");
            lock.lock(); // 可重入，同一线程可以多次获取
            try {
                System.out.println(Thread.currentThread().getName() + " - 第二次获取锁（可重入）");
                System.out.println("持有锁的次数: " + lock.getHoldCount());
            } finally {
                lock.unlock();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 演示tryLock()非阻塞获取锁
     */
    public void tryLockDemo() {
        if (lock.tryLock()) {
            try {
                System.out.println(Thread.currentThread().getName() + " - 成功获取锁");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println(Thread.currentThread().getName() + " - 获取锁失败，执行其他逻辑");
        }
    }

    /**
     * 演示带超时的tryLock()
     */
    public void tryLockWithTimeout() {
        try {
            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    System.out.println(Thread.currentThread().getName() + " - 在超时时间内获取到锁");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println(Thread.currentThread().getName() + " - 超时未获取到锁");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 演示公平锁和非公平锁
     */
    static class FairVsUnfairDemo {
        public static void demonstrate() throws InterruptedException {
            System.out.println("\n=== 非公平锁演示 ===");
            testLock(new ReentrantLock(false));

            System.out.println("\n=== 公平锁演示 ===");
            testLock(new ReentrantLock(true));
        }

        private static void testLock(ReentrantLock lock) throws InterruptedException {
            Runnable task = () -> {
                for (int i = 0; i < 2; i++) {
                    lock.lock();
                    try {
                        System.out.println(Thread.currentThread().getName() + " 获取到锁");
                    } finally {
                        lock.unlock();
                    }
                }
            };

            Thread[] threads = new Thread[3];
            for (int i = 0; i < 3; i++) {
                threads[i] = new Thread(task, "Thread-" + i);
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockDemo demo = new ReentrantLockDemo();

        System.out.println("=== 1. 基本使用演示 ===");
        Thread t1 = new Thread(demo::basicLockUsage, "Thread-1");
        Thread t2 = new Thread(demo::basicLockUsage, "Thread-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\n=== 2. 可重入特性演示 ===");
        demo.reentrantFeature();

        System.out.println("\n=== 3. tryLock()非阻塞演示 ===");
        Thread t3 = new Thread(demo::tryLockDemo, "Thread-3");
        Thread t4 = new Thread(demo::tryLockDemo, "Thread-4");
        t3.start();
        Thread.sleep(10); // 确保t3先获取锁
        t4.start();
        t3.join();
        t4.join();

        System.out.println("\n=== 4. tryLock()超时演示 ===");
        demo.tryLockWithTimeout();

        // 公平锁vs非公平锁
        FairVsUnfairDemo.demonstrate();
    }
}
