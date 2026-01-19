package com.concurrency.aqs.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 基于AQS实现的批处理器
 * 支持按大小或时间触发批量处理
 *
 * 应用场景：
 * - 批量数据库插入
 * - 批量日志写入
 * - 批量消息发送
 * - 批量API调用
 */
public class BatchProcessor<T> {

    private final List<T> buffer;
    private final int batchSize;
    private final long batchTimeoutMillis;
    private final Consumer<List<T>> processor;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final Latch processingLatch;
    private volatile boolean running;
    private volatile long lastProcessTime;
    private Thread processorThread;

    public BatchProcessor(int batchSize, long batchTimeoutMillis, Consumer<List<T>> processor) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        if (batchTimeoutMillis <= 0) {
            throw new IllegalArgumentException("batchTimeoutMillis must be positive");
        }
        if (processor == null) {
            throw new IllegalArgumentException("processor cannot be null");
        }

        this.buffer = new ArrayList<>(batchSize);
        this.batchSize = batchSize;
        this.batchTimeoutMillis = batchTimeoutMillis;
        this.processor = processor;
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
        this.processingLatch = new Latch();
        this.running = false;
        this.lastProcessTime = System.currentTimeMillis();
    }

    /**
     * 自定义闭锁，用于等待批处理完成
     */
    private static class Latch {
        private final Sync sync = new Sync();

        static final class Sync extends AbstractQueuedSynchronizer {
            @Override
            protected int tryAcquireShared(int acquires) {
                return getState() == 0 ? 1 : -1;
            }

            @Override
            protected boolean tryReleaseShared(int releases) {
                setState(0);
                return true;
            }

            void countUp() {
                setState(1);
            }

            void countDown() {
                releaseShared(1);
            }

            void await() throws InterruptedException {
                acquireSharedInterruptibly(1);
            }
        }

        void countUp() {
            sync.countUp();
        }

        void countDown() {
            sync.countDown();
        }

        void await() throws InterruptedException {
            sync.await();
        }
    }

    /**
     * 启动批处理器
     */
    public void start() {
        lock.lock();
        try {
            if (running) {
                return;
            }
            running = true;
            lastProcessTime = System.currentTimeMillis();

            processorThread = new Thread(this::processLoop, "BatchProcessor");
            processorThread.start();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 停止批处理器
     */
    public void shutdown() throws InterruptedException {
        lock.lock();
        try {
            running = false;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }

        if (processorThread != null) {
            processorThread.join();
        }

        // 处理剩余数据
        flush();
    }

    /**
     * 添加元素
     */
    public void add(T item) throws InterruptedException {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        lock.lock();
        try {
            while (buffer.size() >= batchSize) {
                notFull.await();
            }

            buffer.add(item);

            if (buffer.size() >= batchSize) {
                notEmpty.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 添加元素（带超时）
     */
    public boolean add(T item, long timeout, TimeUnit unit) throws InterruptedException {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        long nanos = unit.toNanos(timeout);
        lock.lock();
        try {
            while (buffer.size() >= batchSize) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }

            buffer.add(item);

            if (buffer.size() >= batchSize) {
                notEmpty.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 强制刷新缓冲区
     */
    public void flush() {
        lock.lock();
        try {
            if (!buffer.isEmpty()) {
                processBatch();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 处理循环
     */
    private void processLoop() {
        while (running) {
            lock.lock();
            try {
                // 等待数据或超时
                while (running && buffer.size() < batchSize) {
                    long now = System.currentTimeMillis();
                    long elapsed = now - lastProcessTime;
                    long remaining = batchTimeoutMillis - elapsed;

                    if (remaining <= 0 && !buffer.isEmpty()) {
                        break; // 超时且有数据，触发处理
                    }

                    if (remaining > 0) {
                        notEmpty.await(remaining, TimeUnit.MILLISECONDS);
                    } else {
                        notEmpty.await(batchTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                }

                // 检查是否需要处理
                if (!buffer.isEmpty() &&
                    (buffer.size() >= batchSize ||
                     System.currentTimeMillis() - lastProcessTime >= batchTimeoutMillis)) {
                    processBatch();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 处理批次
     */
    private void processBatch() {
        if (buffer.isEmpty()) {
            return;
        }

        List<T> batch = new ArrayList<>(buffer);
        buffer.clear();
        lastProcessTime = System.currentTimeMillis();
        notFull.signalAll();

        // 标记开始处理
        processingLatch.countUp();

        try {
            processor.accept(batch);
        } finally {
            // 标记处理完成
            processingLatch.countDown();
        }
    }

    /**
     * 获取当前缓冲区大小
     */
    public int getBufferSize() {
        lock.lock();
        try {
            return buffer.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待当前批次处理完成
     */
    public void awaitProcessing() throws InterruptedException {
        processingLatch.await();
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 批处理器测试 ===\n");

        // 测试1：按大小触发
        System.out.println("1. 按大小触发（批次大小: 5）");
        BatchProcessor<String> processor1 = new BatchProcessor<>(
            5,
            10000,
            batch -> System.out.println("   处理批次: " + batch + " (大小: " + batch.size() + ")")
        );
        processor1.start();

        for (int i = 1; i <= 12; i++) {
            processor1.add("Item-" + i);
            System.out.println("   添加: Item-" + i + " (缓冲: " + processor1.getBufferSize() + ")");
            Thread.sleep(100);
        }

        processor1.shutdown();
        System.out.println("   批处理器已关闭\n");

        // 测试2：按时间触发
        System.out.println("2. 按时间触发（超时: 2秒）");
        BatchProcessor<Integer> processor2 = new BatchProcessor<>(
            10,
            2000,
            batch -> System.out.println("   [时间触发] 处理批次: " + batch + " (大小: " + batch.size() + ")")
        );
        processor2.start();

        for (int i = 1; i <= 7; i++) {
            processor2.add(i);
            System.out.println("   添加: " + i);
            Thread.sleep(500);
        }

        System.out.println("   等待超时触发...");
        Thread.sleep(3000);

        processor2.shutdown();
        System.out.println("   批处理器已关闭\n");

        // 测试3：混合触发
        System.out.println("3. 混合触发测试（大小: 3, 超时: 1秒）");
        BatchProcessor<String> processor3 = new BatchProcessor<>(
            3,
            1000,
            batch -> {
                System.out.println("   [批次处理] 大小: " + batch.size() + ", 内容: " + batch);
                try {
                    Thread.sleep(500); // 模拟处理耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        );
        processor3.start();

        // 快速添加3个（触发大小）
        processor3.add("A1");
        processor3.add("A2");
        processor3.add("A3");
        System.out.println("   快速添加3个元素");

        Thread.sleep(500);

        // 慢速添加2个（触发超时）
        processor3.add("B1");
        System.out.println("   添加 B1");
        Thread.sleep(600);
        processor3.add("B2");
        System.out.println("   添加 B2");

        Thread.sleep(1500);

        processor3.shutdown();
        System.out.println("   批处理器已关闭\n");

        // 测试4：并发添加
        System.out.println("4. 并发添加测试");
        BatchProcessor<Integer> processor4 = new BatchProcessor<>(
            5,
            2000,
            batch -> System.out.println("   [并发批次] 处理 " + batch.size() + " 个元素")
        );
        processor4.start();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 4; j++) {
                        int value = threadId * 10 + j;
                        processor4.add(value);
                        System.out.println("   线程" + threadId + " 添加: " + value);
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Thread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Thread.sleep(3000);
        processor4.shutdown();

        System.out.println("\n测试完成！");
    }
}
