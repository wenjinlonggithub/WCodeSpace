/*
package com.concurrency.aqs.scenarios;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

*/
/**
 * 基于AQS实现的熔断器
 * 用于保护系统免受故障服务的影响
 *
 * 三种状态：
 * - CLOSED（关闭）：正常状态，请求正常通过
 * - OPEN（打开）：熔断状态，请求直接失败
 * - HALF_OPEN（半开）：尝试恢复，允许部分请求通过
 *//*

public class CircuitBreaker {

    private final Sync sync;
    private final int failureThreshold;      // 失败阈值
    private final long timeoutMillis;        // 熔断超时时间
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;
    private volatile long openTime;          // 熔断开启时间

    public CircuitBreaker(int failureThreshold, long timeoutMillis) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be positive");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }
        this.sync = new Sync();
        this.failureThreshold = failureThreshold;
        this.timeoutMillis = timeoutMillis;
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.openTime = 0;
    }

    */
/**
     * 熔断器状态
     *//*

    public enum State {
        CLOSED(0),      // 关闭状态
        OPEN(1),        // 打开状态（熔断）
        HALF_OPEN(2);   // 半开状态

        private final int value;

        State(int value) {
            this.value = value;
        }

        static State fromValue(int value) {
            for (State state : values()) {
                if (state.value == value) {
                    return state;
                }
            }
            return CLOSED;
        }
    }

    */
/**
     * 同步器实现
     *//*

    private class Sync extends AbstractQueuedSynchronizer {

        @Override
        protected int tryAcquireShared(int acquires) {
            State state = getState();

            switch (state) {
                case CLOSED:
                    // 关闭状态，允许通过
                    return 1;

                case OPEN:
                    // 检查是否超时，可以进入半开状态
                    if (System.currentTimeMillis() - openTime >= timeoutMillis) {
                        if (compareAndSetState(State.OPEN.value, State.HALF_OPEN.value)) {
                            successCount.set(0);
                            return 1; // 允许一个请求通过
                        }
                    }
                    return -1; // 熔断中，拒绝请求

                case HALF_OPEN:
                    // 半开状态，只允许一个请求通过
                    return 1;

                default:
                    return -1;
            }
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            // 不需要实现
            return true;
        }

        State getState() {
            return State.fromValue(super.getState());
        }

        boolean compareAndSetState(int expect, int update) {
            return compareAndSetState(expect, update);
        }
    }

    */
/**
     * 尝试执行请求
     *//*

    public boolean tryAcquire() {
        State state = sync.getState();

        switch (state) {
            case CLOSED:
                return true;

            case OPEN:
                // 检查是否可以进入半开状态
                if (System.currentTimeMillis() - openTime >= timeoutMillis) {
                    if (sync.compareAndSetState(State.OPEN.value, State.HALF_OPEN.value)) {
                        successCount.set(0);
                        return true;
                    }
                }
                return false;

            case HALF_OPEN:
                return true;

            default:
                return false;
        }
    }

    */
/**
     * 记录成功
     *//*

    public void recordSuccess() {
        State state = sync.getState();

        if (state == State.HALF_OPEN) {
            int success = successCount.incrementAndGet();
            // 半开状态下，连续成功则关闭熔断器
            if (success >= 3) {
                if (sync.compareAndSetState(State.HALF_OPEN.value, State.CLOSED.value)) {
                    failureCount.set(0);
                    successCount.set(0);
                }
            }
        } else if (state == State.CLOSED) {
            // 关闭状态下，成功则重置失败计数
            failureCount.set(0);
        }
    }

    */
/**
     * 记录失败
     *//*

    public void recordFailure() {
        State state = sync.getState();

        if (state == State.HALF_OPEN) {
            // 半开状态下失败，立即重新打开熔断器
            if (sync.compareAndSetState(State.HALF_OPEN.value, State.OPEN.value)) {
                openTime = System.currentTimeMillis();
                successCount.set(0);
            }
        } else if (state == State.CLOSED) {
            // 关闭状态下，失败次数达到阈值则打开熔断器
            int failures = failureCount.incrementAndGet();
            if (failures >= failureThreshold) {
                if (sync.compareAndSetState(State.CLOSED.value, State.OPEN.value)) {
                    openTime = System.currentTimeMillis();
                    failureCount.set(0);
                }
            }
        }
    }

    */
/**
     * 获取当前状态
     *//*

    public State getState() {
        return sync.getState();
    }

    */
/**
     * 获取失败次数
     *//*

    public int getFailureCount() {
        return failureCount.get();
    }

    */
/**
     * 重置熔断器
     *//*

    public void reset() {
        sync.setState(State.CLOSED.value);
        failureCount.set(0);
        successCount.set(0);
        openTime = 0;
    }

    */
/**
     * 测试示例
     *//*

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 熔断器测试 ===\n");

        CircuitBreaker breaker = new CircuitBreaker(3, 2000);

        // 测试1：正常请求
        System.out.println("1. 正常请求测试");
        for (int i = 0; i < 3; i++) {
            if (breaker.tryAcquire()) {
                System.out.println("   请求" + i + " 通过");
                breaker.recordSuccess();
            } else {
                System.out.println("   请求" + i + " 被熔断");
            }
        }
        System.out.println("   当前状态: " + breaker.getState());

        // 测试2：触发熔断
        System.out.println("\n2. 触发熔断测试");
        for (int i = 0; i < 5; i++) {
            if (breaker.tryAcquire()) {
                System.out.println("   请求" + i + " 通过");
                breaker.recordFailure();
                System.out.println("   失败次数: " + breaker.getFailureCount() +
                                 ", 状态: " + breaker.getState());
            } else {
                System.out.println("   请求" + i + " 被熔断，状态: " + breaker.getState());
            }
        }

        // 测试3：等待恢复
        System.out.println("\n3. 等待熔断器恢复");
        System.out.println("   等待2秒...");
        Thread.sleep(2000);

        System.out.println("   尝试请求（应进入半开状态）");
        if (breaker.tryAcquire()) {
            System.out.println("   请求通过，状态: " + breaker.getState());
            breaker.recordSuccess();
        }

        // 测试4：半开状态恢复
        System.out.println("\n4. 半开状态恢复测试");
        for (int i = 0; i < 3; i++) {
            if (breaker.tryAcquire()) {
                System.out.println("   请求" + i + " 通过");
                breaker.recordSuccess();
                System.out.println("   状态: " + breaker.getState());
            }
        }

        // 测试5：模拟真实场景
        System.out.println("\n5. 模拟真实API调用场景");
        CircuitBreaker apiBreaker = new CircuitBreaker(5, 3000);

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    if (apiBreaker.tryAcquire()) {
                        try {
                            // 模拟API调用
                            Thread.sleep(100);
                            // 模拟50%失败率
                            if (Math.random() < 0.5) {
                                apiBreaker.recordSuccess();
                                System.out.println("   线程" + id + "-请求" + j + " 成功");
                            } else {
                                apiBreaker.recordFailure();
                                System.out.println("   线程" + id + "-请求" + j + " 失败 [" +
                                                 apiBreaker.getState() + "]");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        System.out.println("   线程" + id + "-请求" + j + " 被熔断");
                    }

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Thread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n最终状态: " + apiBreaker.getState());
        System.out.println("测试完成！");
    }
}
*/
