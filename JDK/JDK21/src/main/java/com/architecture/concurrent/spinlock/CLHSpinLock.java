package com.architecture.concurrent.spinlock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * CLH 自旋锁 (Craig, Landin, and Hagersten Lock)
 *
 * 原理：
 * - 基于链表的自旋锁
 * - 每个线程在前驱节点的 locked 字段上自旋
 * - 避免了所有线程在同一个变量上自旋，减少缓存一致性开销
 *
 * 结构：
 * Head → Node(线程A, locked=false) → Node(线程B, locked=true) → Node(线程C, locked=true) → Tail
 *                ↑                            ↑                          ↑
 *              释放锁                      等待线程A                    等待线程B
 *
 * 优点：
 * ✓ 公平锁（FIFO）
 * ✓ 每个线程在不同的内存位置自旋，减少缓存争用
 * ✓ 空间复杂度 O(n)，n = 线程数
 * ✓ JDK AQS 的实现基础
 *
 * 缺点：
 * ✗ 实现复杂度较高
 * ✗ 需要为每个线程分配节点
 *
 * JDK 中的应用：
 * - AbstractQueuedSynchronizer (AQS) 使用变种的 CLH 队列
 *
 * @author Architecture Learning
 */
public class CLHSpinLock {

    /**
     * CLH 锁的节点
     */
    private static class Node {
        /**
         * locked = true: 当前节点正在等待锁或持有锁
         * locked = false: 当前节点已释放锁
         */
        volatile boolean locked = true;
    }

    /**
     * 队列的尾节点（原子更新）
     */
    private volatile Node tail;

    /**
     * 原子更新器：用于 CAS 更新 tail
     */
    private static final AtomicReferenceFieldUpdater<CLHSpinLock, Node> TAIL_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(CLHSpinLock.class, Node.class, "tail");

    /**
     * 每个线程持有的节点（当前节点）
     */
    private final ThreadLocal<Node> currentNode = ThreadLocal.withInitial(Node::new);

    /**
     * 每个线程持有的前驱节点引用
     */
    private final ThreadLocal<Node> predecessorNode = new ThreadLocal<>();

    /**
     * 构造器：初始化一个虚拟头节点
     */
    public CLHSpinLock() {
        // 初始状态：创建一个已释放的虚拟节点作为 tail
        tail = new Node();
        tail.locked = false;
    }

    /**
     * 获取锁
     *
     * 执行流程：
     * 1. 获取当前线程的节点，设置 locked = true（表示要获取锁）
     * 2. 将当前节点加入队列尾部（CAS 更新 tail）
     * 3. 获取前驱节点的引用
     * 4. 在前驱节点的 locked 字段上自旋
     * 5. 前驱节点释放锁后（locked = false），当前线程获取锁
     *
     * 详细流程图：
     *
     * 初始状态:
     * tail → [虚拟节点 locked=false]
     *
     * 线程A 调用 lock():
     *   1. nodeA.locked = true
     *   2. CAS: tail 指向 nodeA
     *   3. predecessor = 虚拟节点
     *   4. while (predecessor.locked) { } → false，立即获取锁
     *
     * 当前状态:
     * tail → [nodeA locked=true]
     *         ↑
     *       线程A持有锁
     *
     * 线程B 调用 lock():
     *   1. nodeB.locked = true
     *   2. CAS: tail 指向 nodeB
     *   3. predecessor = nodeA
     *   4. while (nodeA.locked) { }  ← 在 nodeA.locked 上自旋
     *
     * 当前状态:
     * [nodeA locked=true] → [nodeB locked=true] ← tail
     *    ↑                      ↑
     *  线程A持有锁           线程B在nodeA上自旋
     *
     * 线程A 调用 unlock():
     *   nodeA.locked = false
     *
     * 线程B 检测到:
     *   while (nodeA.locked) { } → false，获取锁
     *
     * 当前状态:
     * [nodeA locked=false] → [nodeB locked=true] ← tail
     *                           ↑
     *                        线程B持有锁
     */
    public void lock() {
        // 1. 获取当前线程的节点
        Node node = currentNode.get();
        node.locked = true;  // 设置为等待状态

        // 2. 将当前节点加入队列尾部，并获取前驱节点
        // CAS 操作：原子地将 tail 指向当前节点，并返回旧的 tail（即前驱节点）
        Node predecessor = TAIL_UPDATER.getAndSet(this, node);

        // 3. 保存前驱节点引用（unlock 时需要）
        predecessorNode.set(predecessor);

        // 4. 在前驱节点的 locked 字段上自旋
        // 关键：每个线程在不同的内存位置（predecessor.locked）上自旋
        // 这避免了缓存行争用（Cache Line Bouncing）
        while (predecessor.locked) {
            Thread.onSpinWait();  // 提示 CPU：当前在自旋
        }

        // 5. 前驱节点释放锁后，当前线程获取锁
    }

    /**
     * 释放锁
     *
     * 执行流程：
     * 1. 将当前节点的 locked 设为 false
     * 2. 后继节点会检测到并获取锁
     *
     * 注意：
     * - 不需要显式唤醒后继节点
     * - 后继节点一直在自旋检测 locked 字段
     */
    public void unlock() {
        // 获取当前节点
        Node node = currentNode.get();

        // 将 locked 设为 false，释放锁
        // 后继节点在自旋中会检测到这个变化
        node.locked = false;

        // 复用前驱节点，避免频繁创建对象
        // 将当前节点设为前驱节点（下次 lock 时会创建新节点）
        currentNode.set(predecessorNode.get());
    }

    /**
     * 尝试获取锁（非阻塞）
     *
     * @return 如果成功获取锁返回 true，否则返回 false
     */
    public boolean tryLock() {
        Node node = currentNode.get();
        node.locked = true;

        Node predecessor = TAIL_UPDATER.getAndSet(this, node);
        predecessorNode.set(predecessor);

        // 不自旋，直接检查前驱节点状态
        if (!predecessor.locked) {
            return true;  // 获取锁成功
        } else {
            // 获取失败，需要恢复状态
            node.locked = false;
            return false;
        }
    }
}
