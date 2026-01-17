package com.concurrency.aqs.scenarios;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于AQS实现的任务协调器
 * 用于协调多个任务的执行顺序和依赖关系
 */
public class TaskCoordinator {

    private final Sync sync;

    public TaskCoordinator(int parties) {
        if (parties <= 0) {
            throw new IllegalArgumentException("parties must be positive");
        }
        this.sync = new Sync(parties);
    }

    /**
     * 同步器实现 - 使用共享模式
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private final int parties;

        Sync(int parties) {
            this.parties = parties;
            setState(parties);
        }

        int getParties() {
            return parties;
        }

        int getCount() {
            return getState();
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                if (c == 0) {
                    return false;
                }
                int nextc = c - 1;
                if (compareAndSetState(c, nextc)) {
                    return nextc == 0;
                }
            }
        }

        void reset() {
            setState(parties);
        }
    }

    /**
     * 等待所有任务就绪
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 带超时的等待
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 标记一个任务完成
     */
    public void arrive() {
        sync.releaseShared(1);
    }

    /**
     * 获取等待的任务数
     */
    public int getWaitingCount() {
        return sync.getCount();
    }

    /**
     * 重置协调器
     */
    public void reset() {
        sync.reset();
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 任务协调器测试 ===\n");

        // 测试1：基本协调功能
        System.out.println("1. 基本任务协调");
        TaskCoordinator coordinator = new TaskCoordinator(3);

        Thread[] workers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i;
            workers[i] = new Thread(() -> {
                try {
                    System.out.println("   任务" + id + " 开始准备");
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("   任务" + id + " 准备完成（剩余: " +
                        coordinator.getWaitingCount() + "）");
                    coordinator.arrive();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Worker-" + i);
            workers[i].start();
        }

        System.out.println("   主线程等待所有任务准备完成...");
        coordinator.await();
        System.out.println("   所有任务已就绪，开始执行！\n");

        for (Thread worker : workers) {
            worker.join();
        }

        // 测试2：流水线任务协调
        System.out.println("2. 流水线任务协调");
        TaskCoordinator stage1 = new TaskCoordinator(2);
        TaskCoordinator stage2 = new TaskCoordinator(2);

        new Thread(() -> {
            try {
                System.out.println("   阶段1-任务A 执行");
                Thread.sleep(500);
                stage1.arrive();
                System.out.println("   阶段1-任务A 完成");

                stage1.await();
                System.out.println("   阶段2-任务A 执行");
                Thread.sleep(500);
                stage2.arrive();
                System.out.println("   阶段2-任务A 完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Pipeline-A").start();

        new Thread(() -> {
            try {
                System.out.println("   阶段1-任务B 执行");
                Thread.sleep(800);
                stage1.arrive();
                System.out.println("   阶段1-任务B 完成");

                stage1.await();
                System.out.println("   阶段2-任务B 执行");
                Thread.sleep(500);
                stage2.arrive();
                System.out.println("   阶段2-任务B 完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Pipeline-B").start();

        stage2.await();
        System.out.println("   流水线所有阶段完成！");
    }
}
