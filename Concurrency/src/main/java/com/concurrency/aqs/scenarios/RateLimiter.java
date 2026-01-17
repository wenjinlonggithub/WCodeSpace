package com.concurrency.aqs.scenarios;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于AQS实现的限流器
 * 使用令牌桶算法控制访问速率
 */
public class RateLimiter {

    private final Sync sync;
    private final long intervalNanos;
    private volatile long nextPermitTime;

    public RateLimiter(double permitsPerSecond) {
        if (permitsPerSecond <= 0.0) {
            throw new IllegalArgumentException("rate must be positive");
        }
        this.sync = new Sync();
        this.intervalNanos = (long) (TimeUnit.SECONDS.toNanos(1) / permitsPerSecond);
        this.nextPermitTime = System.nanoTime();
    }

    /**
     * 同步器实现
     */
    private class Sync extends AbstractQueuedSynchronizer {

        @Override
        protected int tryAcquireShared(int acquires) {
            long now = System.nanoTime();
            long next = nextPermitTime;

            if (now >= next) {
                // 可以获取许可
                long newNext = now + intervalNanos * acquires;
                if (compareAndSetNextPermitTime(next, newNext)) {
                    return 1;
                }
            }
            return -1;
        }

        private boolean compareAndSetNextPermitTime(long expect, long update) {
            // 使用state字段存储时间戳的低32位（简化实现）
            // 实际应用中应该使用AtomicLong
            synchronized (RateLimiter.this) {
                if (nextPermitTime == expect) {
                    nextPermitTime = update;
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 获取许可（阻塞）
     */
    public void acquire() throws InterruptedException {
        acquire(1);
    }

    /**
     * 获取指定数量的许可（阻塞）
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }

        while (true) {
            long now = System.nanoTime();
            long next = nextPermitTime;

            if (now >= next) {
                synchronized (this) {
                    if (nextPermitTime == next) {
                        nextPermitTime = now + intervalNanos * permits;
                        return;
                    }
                }
            } else {
                long waitNanos = next - now;
                TimeUnit.NANOSECONDS.sleep(waitNanos);
            }
        }
    }

    /**
     * 尝试获取许可（非阻塞）
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取指定数量的许可（非阻塞）
     */
    public boolean tryAcquire(int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }

        long now = System.nanoTime();
        long next = nextPermitTime;

        if (now >= next) {
            synchronized (this) {
                if (nextPermitTime == next) {
                    nextPermitTime = now + intervalNanos * permits;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 带超时的获取许可
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return tryAcquire(1, timeout, unit);
    }

    /**
     * 带超时的获取指定数量许可
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }

        long timeoutNanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + timeoutNanos;

        while (true) {
            long now = System.nanoTime();
            if (now >= deadline) {
                return false;
            }

            long next = nextPermitTime;
            if (now >= next) {
                synchronized (this) {
                    if (nextPermitTime == next) {
                        nextPermitTime = now + intervalNanos * permits;
                        return true;
                    }
                }
            } else {
                long waitNanos = Math.min(next - now, deadline - now);
                if (waitNanos > 0) {
                    TimeUnit.NANOSECONDS.sleep(waitNanos);
                }
            }
        }
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 限流器测试 ===\n");

        // 每秒允许2个请求
        RateLimiter limiter = new RateLimiter(2.0);

        System.out.println("1. 基本限流测试（每秒2个请求）");
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            limiter.acquire();
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("   请求" + i + " 通过，等待时间: " + elapsed + "ms");
        }

        System.out.println("\n2. 非阻塞尝试获取");
        for (int i = 0; i < 3; i++) {
            boolean acquired = limiter.tryAcquire();
            System.out.println("   尝试" + i + ": " + (acquired ? "成功" : "失败"));
            if (!acquired) {
                Thread.sleep(600); // 等待一段时间再试
            }
        }

        System.out.println("\n3. 并发请求限流");
        RateLimiter apiLimiter = new RateLimiter(3.0);

        Thread[] threads = new Thread[6];
        for (int i = 0; i < 6; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    apiLimiter.acquire();
                    long elapsed = System.currentTimeMillis() - start;
                    System.out.println("   线程" + id + " 获取许可，等待: " + elapsed + "ms");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Thread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n测试完成！");
    }
}
