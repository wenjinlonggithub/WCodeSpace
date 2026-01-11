package com.architecture.concurrency;

import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Java并发编程核心概念和实现
 * 
 * 涵盖内容：
 * 1. 线程创建和管理
 * 2. 同步机制 (synchronized, Lock, Atomic)
 * 3. 线程池使用
 * 4. 并发集合
 * 5. 生产者-消费者模式
 */

// ============= 线程创建方式 =============

// 方式1: 继承Thread类
class MyThread extends Thread {
    private String threadName;
    
    public MyThread(String name) {
        this.threadName = name;
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(threadName + " - Count: " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(threadName + " interrupted");
                return;
            }
        }
        System.out.println(threadName + " finished");
    }
}

// 方式2: 实现Runnable接口
class MyRunnable implements Runnable {
    private String taskName;
    
    public MyRunnable(String name) {
        this.taskName = name;
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(taskName + " - Count: " + i);
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                System.out.println(taskName + " interrupted");
                return;
            }
        }
        System.out.println(taskName + " finished");
    }
}

// 方式3: 实现Callable接口
class MyCallable implements Callable<String> {
    private String taskName;
    private int iterations;
    
    public MyCallable(String name, int iterations) {
        this.taskName = name;
        this.iterations = iterations;
    }
    
    @Override
    public String call() throws Exception {
        int sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += i;
            Thread.sleep(100);
        }
        return taskName + " completed with sum: " + sum;
    }
}

// ============= 同步机制示例 =============

// synchronized关键字示例
class Counter {
    private int count = 0;
    
    // 同步方法
    public synchronized void increment() {
        count++;
    }
    
    // 同步代码块
    public void decrement() {
        synchronized (this) {
            count--;
        }
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// ReentrantLock示例
class LockCounter {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
    
    public void decrement() {
        lock.lock();
        try {
            count--;
        } finally {
            lock.unlock();
        }
    }
    
    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

// AtomicInteger示例
class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();
    }
    
    public void decrement() {
        count.decrementAndGet();
    }
    
    public int getCount() {
        return count.get();
    }
    
    // CAS操作示例
    public boolean compareAndSet(int expected, int update) {
        return count.compareAndSet(expected, update);
    }
}

// ============= 读写锁示例 =============
class ReadWriteCounter {
    private int count = 0;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    public void increment() {
        rwLock.writeLock().lock();
        try {
            count++;
            System.out.println("Write: " + count);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    public int getCount() {
        rwLock.readLock().lock();
        try {
            System.out.println("Read: " + count);
            return count;
        } finally {
            rwLock.readLock().unlock();
        }
    }
}

// ============= 生产者-消费者模式 =============
class ProducerConsumerExample {
    private final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
    private final AtomicInteger counter = new AtomicInteger(0);
    
    class Producer implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 20; i++) {
                    int value = counter.incrementAndGet();
                    queue.put(value);
                    System.out.println("Produced: " + value);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    class Consumer implements Runnable {
        private String name;
        
        public Consumer(String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    Integer value = queue.take();
                    System.out.println(name + " consumed: " + value);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void start() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 启动1个生产者
        executor.submit(new Producer());
        
        // 启动2个消费者
        executor.submit(new Consumer("Consumer-1"));
        executor.submit(new Consumer("Consumer-2"));
        
        // 运行5秒后关闭
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdownNow();
    }
}

public class ConcurrencyBasics {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyBasics.class);
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== Thread Creation Examples ===");
        
        // 方式1: Thread类
        MyThread thread1 = new MyThread("Thread-1");
        thread1.start();
        
        // 方式2: Runnable接口
        Thread thread2 = new Thread(new MyRunnable("Runnable-Task"));
        thread2.start();
        
        // 方式3: Callable接口 + Future
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new MyCallable("Callable-Task", 10));
        
        try {
            String result = future.get(5, TimeUnit.SECONDS);
            System.out.println("Callable result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Task timed out");
            future.cancel(true);
        }
        
        executor.shutdown();
        
        System.out.println("\n=== Synchronization Examples ===");

        /*
        // synchronized示例
        Counter syncCounter = new Counter();
        testCounter("Synchronized Counter", 
            () -> syncCounter.increment(),
            () -> syncCounter.getCount());
        
        // ReentrantLock示例
        LockCounter lockCounter = new LockCounter();
        testCounter("Lock Counter",
            () -> lockCounter.increment(),
            () -> lockCounter.getCount());
        
        // AtomicInteger示例
        AtomicCounter atomicCounter = new AtomicCounter();
        testCounter("Atomic Counter",
            () -> atomicCounter.increment(),
            () -> {
                return atomicCounter.getCount();
            });*/
        
        System.out.println("\n=== Read-Write Lock Example ===");
        ReadWriteCounter rwCounter = new ReadWriteCounter();
        
        // 启动多个读写线程
        ExecutorService rwExecutor = Executors.newFixedThreadPool(5);
        
        // 写线程
        for (int i = 0; i < 3; i++) {
            rwExecutor.submit(() -> {
                for (int j = 0; j < 3; j++) {
                    rwCounter.increment();
                    try { 
                        Thread.sleep(100); 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during increment operation", e);
                        break;
                    }
                }
            });
        }
        
        // 读线程
        for (int i = 0; i < 5; i++) {
            rwExecutor.submit(() -> {
                for (int j = 0; j < 5; j++) {
                    rwCounter.getCount();
                    try { 
                        Thread.sleep(50); 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during read operation", e);
                        break;
                    }
                }
            });
        }
        
        rwExecutor.shutdown();
        rwExecutor.awaitTermination(3, TimeUnit.SECONDS);
        
        System.out.println("\n=== Producer-Consumer Example ===");
        ProducerConsumerExample pcExample = new ProducerConsumerExample();
        pcExample.start();
    }
    
    private static void testCounter(String name, Runnable incrementTask, Supplier<Integer> getCount)
            throws InterruptedException {
        System.out.println("\nTesting " + name);
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // 启动10个线程，每个线程执行1000次增量操作
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    incrementTask.run();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println(name + " final count: " + getCount.get());
        System.out.println("Expected: 10000");
    }
}

