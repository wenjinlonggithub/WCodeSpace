package com.architecture.concurrent.spinlock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 票据自旋锁（Ticket Spin Lock）
 *
 * 原理：
 * - 类似银行取号排队系统
 * - serviceNum: 当前正在服务的号码
 * - ticketNum: 下一个要分配的号码
 * - 线程获取锁时先取号，然后自旋等待轮到自己的号码
 *
 * 优点：
 * ✓ 保证公平性（FIFO）
 * ✓ 避免线程饥饿
 * ✓ 实现简单
 *
 * 缺点：
 * ✗ 仍然存在缓存行争用问题
 * ✗ 所有线程在同一个 serviceNum 上自旋
 *
 * @author Architecture Learning
 */
public class TicketSpinLock {

    /**
     * 当前正在服务的号码（当前可以获取锁的票据号）
     */
    private volatile int serviceNum = 0;

    /**
     * 下一个要分配的票据号
     */
    private volatile int ticketNum = 0;

    /**
     * 使用 ThreadLocal 存储每个线程的票据号
     */
    private final ThreadLocal<Integer> myTicket = new ThreadLocal<>();

    /**
     * 获取锁
     *
     * 流程：
     * 1. 原子地获取一个票据号（ticketNum++）
     * 2. 自旋等待轮到自己的号码（myTicket == serviceNum）
     * 3. 轮到后，获取锁
     *
     * 示例：
     * 初始: serviceNum=0, ticketNum=0
     *
     * 线程A调用 lock():
     *   myTicket = ticketNum++ = 0
     *   while (myTicket != serviceNum) { } → 0 == 0，立即通过
     *   获取锁
     *
     * 线程B调用 lock():
     *   myTicket = ticketNum++ = 1
     *   while (myTicket != serviceNum) { } → 1 != 0，自旋等待
     *
     * 线程C调用 lock():
     *   myTicket = ticketNum++ = 2
     *   while (myTicket != serviceNum) { } → 2 != 0，自旋等待
     *
     * 线程A释放锁:
     *   serviceNum++ → serviceNum = 1
     *
     * 线程B检测到:
     *   myTicket == serviceNum → 1 == 1，获取锁
     *
     * 线程B释放锁:
     *   serviceNum++ → serviceNum = 2
     *
     * 线程C检测到:
     *   myTicket == serviceNum → 2 == 2，获取锁
     */
    public void lock() {
        // 原子地获取票据号（相当于银行取号）
        int ticket = getNextTicket();
        myTicket.set(ticket);

        // 自旋等待轮到自己的号码
        while (ticket != serviceNum) {
            // 空循环，等待轮到自己
            Thread.onSpinWait(); // JDK 9+ 提示 CPU 当前在自旋
        }
        // 轮到自己，获取锁
    }

    /**
     * 原子地获取下一个票据号
     */
    private synchronized int getNextTicket() {
        return ticketNum++;
    }

    /**
     * 释放锁
     *
     * 将 serviceNum 加 1，通知下一个线程可以获取锁
     */
    public void unlock() {
        // 服务下一个号码
        serviceNum++;
    }

    /**
     * 获取当前队列长度（等待的线程数）
     */
    public int getQueueLength() {
        return ticketNum - serviceNum;
    }
}
