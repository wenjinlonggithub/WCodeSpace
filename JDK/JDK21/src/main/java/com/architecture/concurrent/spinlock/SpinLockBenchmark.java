package com.architecture.concurrent.spinlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自旋锁性能基准测试
 *
 * 对比不同锁在不同场景下的性能表现
 */
public class SpinLockBenchmark {

    private static final int THREAD_COUNT = 10;
    private static final int ITERATIONS = 100000;

    /**
     * 基准测试：短临界区（几纳秒）
     */
    public static void benchmark_ShortCriticalSection() throws InterruptedException {
        System.out.println("\n========== 短临界区性能测试（临界区 ~10ns） ==========\n");

        // 1. SimpleSpinLock
        SimpleSpinLock spinLock = new SimpleSpinLock();
        long spinTime = runTest("SimpleSpinLock", () -> {
            spinLock.lock();
            try {
                int x = 1 + 1;  // 极短操作
            } finally {
                spinLock.unlock();
            }
        });

        // 2. TicketSpinLock
        TicketSpinLock ticketLock = new TicketSpinLock();
        long ticketTime = runTest("TicketSpinLock", () -> {
            ticketLock.lock();
            try {
                int x = 1 + 1;
            } finally {
                ticketLock.unlock();
            }
        });

        // 3. CLHSpinLock
        CLHSpinLock clhLock = new CLHSpinLock();
        long clhTime = runTest("CLHSpinLock", () -> {
            clhLock.lock();
            try {
                int x = 1 + 1;
            } finally {
                clhLock.unlock();
            }
        });

        // 4. synchronized
        Object syncLock = new Object();
        long syncTime = runTest("synchronized", () -> {
            synchronized (syncLock) {
                int x = 1 + 1;
            }
        });

        // 5. ReentrantLock
        Lock reentrantLock = new ReentrantLock();
        long reentrantTime = runTest("ReentrantLock", () -> {
            reentrantLock.lock();
            try {
                int x = 1 + 1;
            } finally {
                reentrantLock.unlock();
            }
        });

        // 输出结果
        printResults("短临界区", spinTime, ticketTime, clhTime, syncTime, reentrantTime);
    }

    /**
     * 基准测试：中等临界区（几微秒）
     */
    public static void benchmark_MediumCriticalSection() throws InterruptedException {
        System.out.println("\n========== 中等临界区性能测试（临界区 ~1μs） ==========\n");

        // 1. SimpleSpinLock
        SimpleSpinLock spinLock = new SimpleSpinLock();
        long spinTime = runTest("SimpleSpinLock", () -> {
            spinLock.lock();
            try {
                busySpin(1000);  // 约 1 微秒
            } finally {
                spinLock.unlock();
            }
        });

        // 2. synchronized
        Object syncLock = new Object();
        long syncTime = runTest("synchronized", () -> {
            synchronized (syncLock) {
                busySpin(1000);
            }
        });

        // 3. ReentrantLock
        Lock reentrantLock = new ReentrantLock();
        long reentrantTime = runTest("ReentrantLock", () -> {
            reentrantLock.lock();
            try {
                busySpin(1000);
            } finally {
                reentrantLock.unlock();
            }
        });

        // 输出结果
        System.out.println("SimpleSpinLock:  " + spinTime + " ms");
        System.out.println("synchronized:    " + syncTime + " ms");
        System.out.println("ReentrantLock:   " + reentrantTime + " ms");
    }

    /**
     * 基准测试：长临界区（几十微秒）
     */
    public static void benchmark_LongCriticalSection() throws InterruptedException {
        System.out.println("\n========== 长临界区性能测试（临界区 ~10μs） ==========\n");

        // 1. SimpleSpinLock
        SimpleSpinLock spinLock = new SimpleSpinLock();
        long spinTime = runTest("SimpleSpinLock", () -> {
            spinLock.lock();
            try {
                busySpin(10000);  // 约 10 微秒
            } finally {
                spinLock.unlock();
            }
        });

        // 2. synchronized
        Object syncLock = new Object();
        long syncTime = runTest("synchronized", () -> {
            synchronized (syncLock) {
                busySpin(10000);
            }
        });

        // 3. ReentrantLock
        Lock reentrantLock = new ReentrantLock();
        long reentrantTime = runTest("ReentrantLock", () -> {
            reentrantLock.lock();
            try {
                busySpin(10000);
            } finally {
                reentrantLock.unlock();
            }
        });

        // 输出结果
        System.out.println("SimpleSpinLock:  " + spinTime + " ms  ← 性能差（CPU 空转）");
        System.out.println("synchronized:    " + syncTime + " ms  ← 性能好（阻塞等待）");
        System.out.println("ReentrantLock:   " + reentrantTime + " ms  ← 性能好（阻塞等待）");
    }

    /**
     * 运行测试
     */
    private static long runTest(String name, Runnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < ITERATIONS; j++) {
                    task.run();
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println(String.format("%-20s 耗时: %5d ms  (TPS: %,d ops/s)",
            name, duration, (THREAD_COUNT * ITERATIONS * 1000L / duration)));

        return duration;
    }

    /**
     * 忙等待（模拟 CPU 密集型操作）
     */
    private static void busySpin(int iterations) {
        long result = 0;
        for (int i = 0; i < iterations; i++) {
            result += i;
        }
    }

    /**
     * 打印对比结果
     */
    private static void printResults(String scenario, long spin, long ticket, long clh, long sync, long reentrant) {
        System.out.println("\n性能对比：");
        System.out.println("  SimpleSpinLock   是 synchronized 的 " + String.format("%.2f", (double) sync / spin) + " 倍");
        System.out.println("  CLHSpinLock      是 synchronized 的 " + String.format("%.2f", (double) sync / clh) + " 倍");
        System.out.println("  TicketSpinLock   是 synchronized 的 " + String.format("%.2f", (double) sync / ticket) + " 倍");

        System.out.println("\n结论：");
        if (spin < sync) {
            System.out.println("  ✓ " + scenario + "下，自旋锁性能优于 synchronized（避免上下文切换）");
        } else {
            System.out.println("  ✗ " + scenario + "下，synchronized 性能优于自旋锁（避免 CPU 空转）");
        }
    }

    /**
     * 竞争强度测试
     */
    public static void benchmark_Contention() throws InterruptedException {
        System.out.println("\n========== 竞争强度测试 ==========\n");

        for (int threadCount : new int[]{2, 5, 10, 20}) {
            System.out.println("--- " + threadCount + " 个线程竞争 ---");

            SimpleSpinLock spinLock = new SimpleSpinLock();
            Object syncLock = new Object();

            long spinTime = runTestWithThreads(spinLock, threadCount);
            long syncTime = runTestWithThreads(syncLock, threadCount);

            System.out.println("  自旋锁: " + spinTime + " ms");
            System.out.println("  synchronized: " + syncTime + " ms");
            System.out.println("  性能比: " + String.format("%.2f", (double) syncTime / spinTime));
            System.out.println();
        }

        System.out.println("结论：竞争越激烈，自旋锁优势越小（甚至劣于 synchronized）");
    }

    private static long runTestWithThreads(Object lock, int threadCount) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threadCount);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    if (lock instanceof SimpleSpinLock) {
                        SimpleSpinLock spinLock = (SimpleSpinLock) lock;
                        spinLock.lock();
                        try {
                            int x = 1 + 1;
                        } finally {
                            spinLock.unlock();
                        }
                    } else {
                        synchronized (lock) {
                            int x = 1 + 1;
                        }
                    }
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        return System.currentTimeMillis() - startTime;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 自旋锁性能基准测试 ===");
        System.out.println("线程数: " + THREAD_COUNT);
        System.out.println("每线程迭代数: " + ITERATIONS);
        System.out.println("总操作数: " + (THREAD_COUNT * ITERATIONS));

        // 预热 JVM
        System.out.println("\nJVM 预热中...");
        SimpleSpinLock warmupLock = new SimpleSpinLock();
        for (int i = 0; i < 10000; i++) {
            warmupLock.lock();
            warmupLock.unlock();
        }

        // 运行测试
        benchmark_ShortCriticalSection();
        Thread.sleep(500);

        benchmark_MediumCriticalSection();
        Thread.sleep(500);

        benchmark_LongCriticalSection();
        Thread.sleep(500);

        benchmark_Contention();
    }
}
