package com.concurrency.aqs.custom;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 自定义倒计时器实现
 * 基于AQS实现CountDownLatch功能
 */
public class CustomCountDownLatch {

    private final Sync sync;

    public CustomCountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 同步器实现
     */
    private static final class Sync extends AbstractQueuedSynchronizer {

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        /**
         * 共享模式获取：当count为0时返回1，否则返回-1
         */
        @Override
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        /**
         * 共享模式释放：递减count
         */
        @Override
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    /**
     * 等待计数器归零
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
     * 计数器减1
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 获取当前计数
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 自定义倒计时器测试 ===\n");

        // 测试1：基本功能
        System.out.println("1. 基本倒计时功能");
        CustomCountDownLatch latch1 = new CustomCountDownLatch(3);
        System.out.println("   初始计数: " + latch1.getCount());

        new Thread(() -> {
            System.out.println("   工作线程1 开始");
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            latch1.countDown();
            System.out.println("   工作线程1 完成，计数: " + latch1.getCount());
        }).start();

        new Thread(() -> {
            System.out.println("   工作线程2 开始");
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            latch1.countDown();
            System.out.println("   工作线程2 完成，计数: " + latch1.getCount());
        }).start();

        new Thread(() -> {
            System.out.println("   工作线程3 开始");
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            latch1.countDown();
            System.out.println("   工作线程3 完成，计数: " + latch1.getCount());
        }).start();

        System.out.println("   主线程等待所有工作完成...");
        latch1.await();
        System.out.println("   所有工作完成！\n");

        // 测试2：多线程协调
        System.out.println("2. 多线程协调测试");
        int workerCount = 5;
        CustomCountDownLatch startSignal = new CustomCountDownLatch(1);
        CustomCountDownLatch doneSignal = new CustomCountDownLatch(workerCount);

        for (int i = 0; i < workerCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("   工作者" + id + " 准备就绪");
                    startSignal.await(); // 等待开始信号
                    System.out.println("   工作者" + id + " 开始工作");
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("   工作者" + id + " 完成工作");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneSignal.countDown();
                }
            }).start();
        }

        Thread.sleep(500);
        System.out.println("   发送开始信号！");
        startSignal.countDown();

        doneSignal.await();
        System.out.println("   所有工作者完成！");
    }
}
