package com.architecture.concurrent.spinlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自旋锁使用示例和性能对比
 */
public class SpinLockDemo {

    /**
     * 示例1：简单自旋锁的基本使用
     */
    public static void demo1_SimpleSpinLock() throws InterruptedException {
        System.out.println("\n=== 示例1：简单自旋锁的基本使用 ===");

        SimpleSpinLock lock = new SimpleSpinLock();
        AtomicInteger counter = new AtomicInteger(0);
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            new Thread(() -> {
                lock.lock();
                try {
                    System.out.println("线程 " + threadId + " 获取锁，counter = " + counter.get());
                    Thread.sleep(100);  // 模拟临界区操作
                    counter.incrementAndGet();
                    System.out.println("线程 " + threadId + " 释放锁，counter = " + counter.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    latch.countDown();
                }
            }, "Thread-" + threadId).start();
        }

        latch.await();
        System.out.println("最终 counter = " + counter.get());
    }

    /**
     * 示例2：票据自旋锁演示公平性
     */
    public static void demo2_TicketSpinLock() throws InterruptedException {
        System.out.println("\n=== 示例2：票据自旋锁（公平锁） ===");

        TicketSpinLock lock = new TicketSpinLock();
        int threadCount = 5;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            new Thread(() -> {
                try {
                    startSignal.await();  // 等待所有线程就绪

                    System.out.println("线程 " + threadId + " 尝试获取锁...");
                    lock.lock();
                    System.out.println("  → 线程 " + threadId + " 获取到锁（队列长度: " + lock.getQueueLength() + "）");
                    Thread.sleep(200);  // 模拟工作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    System.out.println("  ← 线程 " + threadId + " 释放锁");
                    doneSignal.countDown();
                }
            }, "Thread-" + threadId).start();
        }

        Thread.sleep(100);  // 确保所有线程都启动
        System.out.println("所有线程就绪，开始竞争锁...\n");
        startSignal.countDown();  // 开始

        doneSignal.await();
        System.out.println("\n票据自旋锁保证了 FIFO 顺序！");
    }

    /**
     * 示例3：CLH 自旋锁演示
     */
    public static void demo3_CLHSpinLock() throws InterruptedException {
        System.out.println("\n=== 示例3：CLH 自旋锁（AQS 基础） ===");

        CLHSpinLock lock = new CLHSpinLock();
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            new Thread(() -> {
                lock.lock();
                try {
                    System.out.println("线程 " + threadId + " 进入临界区");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("线程 " + threadId + " 离开临界区");
                    lock.unlock();
                    latch.countDown();
                }
            }, "Thread-" + threadId).start();
        }

        latch.await();
        System.out.println("CLH 锁：每个线程在不同内存位置自旋，减少缓存争用！");
    }

    /**
     * 示例4：自适应自旋锁演示
     */
    public static void demo4_AdaptiveSpinLock() throws InterruptedException {
        System.out.println("\n=== 示例4：自适应自旋锁 ===");

        AdaptiveSpinLock lock = new AdaptiveSpinLock();
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        System.out.println("初始自旋次数: " + lock.getCurrentSpinCount());

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    lock.lock();
                    try {
                        if (j == 0) {
                            System.out.println("线程 " + threadId + " 第1次获取锁，当前自旋次数: " + lock.getCurrentSpinCount());
                        }
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
                latch.countDown();
            }, "Thread-" + threadId).start();
        }

        latch.await();
        System.out.println("最终自旋次数: " + lock.getCurrentSpinCount());
        System.out.println("自适应自旋锁会根据竞争情况动态调整自旋次数！");
    }

    /**
     * 示例5：性能对比 - 自旋锁 vs synchronized
     */
    public static void demo5_PerformanceComparison() throws InterruptedException {
        System.out.println("\n=== 示例5：性能对比（短临界区） ===");

        int threadCount = 10;
        int iterations = 10000;

        // 测试自旋锁
        SimpleSpinLock spinLock = new SimpleSpinLock();
        AtomicInteger spinCounter = new AtomicInteger(0);
        long spinStart = System.currentTimeMillis();

        CountDownLatch spinLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    spinLock.lock();
                    try {
                        spinCounter.incrementAndGet();
                    } finally {
                        spinLock.unlock();
                    }
                }
                spinLatch.countDown();
            }).start();
        }
        spinLatch.await();
        long spinDuration = System.currentTimeMillis() - spinStart;

        // 测试 synchronized
        Object syncLock = new Object();
        AtomicInteger syncCounter = new AtomicInteger(0);
        long syncStart = System.currentTimeMillis();

        CountDownLatch syncLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    synchronized (syncLock) {
                        syncCounter.incrementAndGet();
                    }
                }
                syncLatch.countDown();
            }).start();
        }
        syncLatch.await();
        long syncDuration = System.currentTimeMillis() - syncStart;

        System.out.println("自旋锁耗时: " + spinDuration + "ms, 结果: " + spinCounter.get());
        System.out.println("synchronized 耗时: " + syncDuration + "ms, 结果: " + syncCounter.get());
        System.out.println("性能提升: " + String.format("%.2f", (double) syncDuration / spinDuration) + "x");
        System.out.println("\n短临界区场景下，自旋锁通常性能更好（避免线程上下文切换）");
    }

    /**
     * 示例6：自旋锁的不当使用 - 长临界区
     */
    public static void demo6_LongCriticalSection() throws InterruptedException {
        System.out.println("\n=== 示例6：长临界区场景（自旋锁的劣势） ===");

        int threadCount = 5;
        int sleepTime = 50;  // 模拟长时间临界区

        // 自旋锁
        SimpleSpinLock spinLock = new SimpleSpinLock();
        long spinStart = System.currentTimeMillis();

        CountDownLatch spinLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            new Thread(() -> {
                spinLock.lock();
                try {
                    System.out.println("自旋锁 - 线程 " + threadId + " 进入临界区");
                    Thread.sleep(sleepTime);  // 长时间操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    spinLock.unlock();
                    spinLatch.countDown();
                }
            }).start();
        }
        spinLatch.await();
        long spinDuration = System.currentTimeMillis() - spinStart;

        // synchronized
        Object syncLock = new Object();
        long syncStart = System.currentTimeMillis();

        CountDownLatch syncLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            new Thread(() -> {
                synchronized (syncLock) {
                    try {
                        System.out.println("synchronized - 线程 " + threadId + " 进入临界区");
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                syncLatch.countDown();
            }).start();
        }
        syncLatch.await();
        long syncDuration = System.currentTimeMillis() - syncStart;

        System.out.println("\n自旋锁耗时: " + spinDuration + "ms");
        System.out.println("synchronized 耗时: " + syncDuration + "ms");
        System.out.println("\n长临界区场景下，synchronized 更好（自旋锁浪费 CPU）");
    }

    /**
     * 示例7：实际应用场景 - 计数器
     */
    public static void demo7_RealWorldScenario() throws InterruptedException {
        System.out.println("\n=== 示例7：实际应用 - 高性能计数器 ===");

        // 场景：高并发计数器，每次操作非常快
        class SpinCounter {
            private final SimpleSpinLock lock = new SimpleSpinLock();
            private int count = 0;

            public void increment() {
                lock.lock();
                try {
                    count++;  // 极短的临界区
                } finally {
                    lock.unlock();
                }
            }

            public int get() {
                lock.lock();
                try {
                    return count;
                } finally {
                    lock.unlock();
                }
            }
        }

        SpinCounter counter = new SpinCounter();
        int threadCount = 20;
        int increments = 5000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < increments; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }

        latch.await();
        long duration = System.currentTimeMillis() - start;

        System.out.println("线程数: " + threadCount);
        System.out.println("每线程操作数: " + increments);
        System.out.println("总操作数: " + (threadCount * increments));
        System.out.println("最终计数: " + counter.get());
        System.out.println("耗时: " + duration + "ms");
        System.out.println("TPS: " + (threadCount * increments * 1000L / duration) + " ops/s");

        executor.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        demo1_SimpleSpinLock();
        Thread.sleep(500);

        demo2_TicketSpinLock();
        Thread.sleep(500);

        demo3_CLHSpinLock();
        Thread.sleep(500);

        demo4_AdaptiveSpinLock();
        Thread.sleep(500);

        demo5_PerformanceComparison();
        Thread.sleep(500);

        demo6_LongCriticalSection();
        Thread.sleep(500);

        demo7_RealWorldScenario();
    }
}
