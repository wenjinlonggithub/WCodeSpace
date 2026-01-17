package com.concurrency.aqs.basic;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore（信号量）使用示例
 * 演示如何使用Semaphore控制并发访问数量
 */
public class SemaphoreDemo {

    /**
     * 模拟数据库连接池
     */
    static class DatabaseConnectionPool {
        private final Semaphore semaphore;
        private final int poolSize;

        public DatabaseConnectionPool(int poolSize) {
            this.poolSize = poolSize;
            this.semaphore = new Semaphore(poolSize);
        }

        public void executeQuery(String query) {
            try {
                semaphore.acquire(); // 获取许可
                System.out.println(Thread.currentThread().getName() +
                    " 获取连接，执行查询: " + query +
                    " (可用连接: " + semaphore.availablePermits() + "/" + poolSize + ")");

                // 模拟查询执行
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println(Thread.currentThread().getName() + " 释放连接");
                semaphore.release(); // 释放许可
            }
        }

        public boolean tryExecuteQuery(String query, long timeout) {
            try {
                if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println(Thread.currentThread().getName() +
                            " 获取连接（超时模式），执行: " + query);
                        Thread.sleep(500);
                        return true;
                    } finally {
                        semaphore.release();
                    }
                } else {
                    System.out.println(Thread.currentThread().getName() +
                        " 超时未获取到连接");
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 演示限流场景
     */
    static class RateLimiterDemo {
        private final Semaphore semaphore;

        public RateLimiterDemo(int maxConcurrent) {
            this.semaphore = new Semaphore(maxConcurrent);
        }

        public void processRequest(int requestId) {
            try {
                semaphore.acquire();
                System.out.println("处理请求 " + requestId + " - " +
                    Thread.currentThread().getName());
                Thread.sleep(500); // 模拟处理时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. 数据库连接池演示 ===");
        DatabaseConnectionPool pool = new DatabaseConnectionPool(3);

        // 创建5个线程竞争3个连接
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i;
            threads[i] = new Thread(() ->
                pool.executeQuery("SELECT * FROM users WHERE id=" + id),
                "Thread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n=== 2. 带超时的连接获取演示 ===");
        Thread t1 = new Thread(() ->
            pool.tryExecuteQuery("SELECT * FROM orders", 500), "TimeoutThread-1");
        Thread t2 = new Thread(() ->
            pool.tryExecuteQuery("SELECT * FROM products", 2000), "TimeoutThread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\n=== 3. 限流器演示 ===");
        RateLimiterDemo limiter = new RateLimiterDemo(2);

        Thread[] requestThreads = new Thread[6];
        for (int i = 0; i < 6; i++) {
            final int requestId = i;
            requestThreads[i] = new Thread(() ->
                limiter.processRequest(requestId), "Request-" + i);
            requestThreads[i].start();
        }

        for (Thread thread : requestThreads) {
            thread.join();
        }

        System.out.println("\n所有演示完成！");
    }
}
