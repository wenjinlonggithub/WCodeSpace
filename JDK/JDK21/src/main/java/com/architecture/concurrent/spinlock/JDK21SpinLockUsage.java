package com.architecture.concurrent.spinlock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JDK 21 中自旋锁的实际应用
 *
 * 本类展示 JDK 中哪些地方使用了自旋锁机制
 */
public class JDK21SpinLockUsage {

    /**
     * 1. synchronized 的自适应自旋
     *
     * JDK 6+ 引入了自适应自旋优化：
     * - 线程尝试获取 synchronized 锁失败时，不会立即阻塞
     * - 先进行自旋（空循环等待）
     * - 如果自旋一定次数后仍未获取锁，才进入阻塞状态
     * - 自旋次数由 JVM 自适应调整
     *
     * JVM 参数：
     * -XX:+UseSpinning          开启自旋（JDK 6+默认开启）
     * -XX:PreBlockSpin=10       设置自旋次数（JDK 7+已废弃，改为自适应）
     *
     * 示例：
     */
    public static void demo_SynchronizedSpinning() {
        Object lock = new Object();

        // 第一个线程持有锁
        new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("线程1持有锁");
                    Thread.sleep(100);  // 短时间持有
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 第二个线程尝试获取锁
        // 内部流程：
        // 1. CAS 尝试获取锁（轻量级锁）
        // 2. 失败后进入自旋（在用户态循环等待）
        // 3. 自旋一定次数后，膨胀为重量级锁，进入阻塞
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("线程2获取锁");
            }
        }).start();
    }

    /**
     * 2. AtomicInteger 的 CAS + 自旋
     *
     * Atomic 类的所有操作都基于 CAS + 自旋：
     * - getAndIncrement() 内部使用自旋
     * - 如果 CAS 失败，会在循环中重试
     *
     * 源码分析：
     */
    public static void demo_AtomicSpinning() {
        AtomicInteger counter = new AtomicInteger(0);

        // getAndIncrement() 的内部实现（简化版）：
        /*
        public final int getAndIncrement() {
            for (;;) {  // ← 自旋循环
                int current = get();
                int next = current + 1;
                if (compareAndSet(current, next))  // CAS
                    return current;
                // CAS 失败，继续自旋
            }
        }
        */

        // 多线程并发调用
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.getAndIncrement();  // 内部自旋
                }
            }).start();
        }

        try {
            Thread.sleep(1000);
            System.out.println("AtomicInteger 使用 CAS + 自旋，最终结果: " + counter.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 3. ReentrantLock 的自旋（NonfairSync）
     *
     * ReentrantLock 的非公平模式在尝试获取锁时会先自旋：
     * - 调用 lock() 时，先 CAS 尝试获取锁
     * - 失败后进入 AQS 队列前，会再自旋几次
     * - 最后才进入队列阻塞
     */
    public static void demo_ReentrantLockSpinning() {
        Lock lock = new ReentrantLock(false);  // 非公平锁

        new Thread(() -> {
            lock.lock();
            try {
                System.out.println("线程1持有 ReentrantLock");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();

        new Thread(() -> {
            // 内部流程（NonfairSync）：
            // 1. CAS 尝试获取锁
            // 2. 失败后，调用 acquire(1)
            // 3. acquire 内部会先调用 tryAcquire()（可能自旋）
            // 4. 再失败才进入 AQS 队列
            lock.lock();
            try {
                System.out.println("线程2获取 ReentrantLock");
            } finally {
                lock.unlock();
            }
        }).start();
    }

    /**
     * 4. ConcurrentHashMap 的自旋
     *
     * ConcurrentHashMap 在多个地方使用自旋：
     * - 初始化数组时的自旋等待
     * - 扩容时的协助转移（自旋等待）
     * - 树化/链表化时的自旋
     */
    public static void demo_ConcurrentHashMapSpinning() {
        // ConcurrentHashMap 初始化的简化逻辑：
        /*
        private final Node<K,V>[] initTable() {
            for (;;) {  // ← 自旋
                if (sizeCtl < 0)
                    Thread.yield();  // 其他线程在初始化，让出CPU
                else if (compareAndSetSizeCtl(...))
                    // 初始化
                    break;
            }
        }
        */

        System.out.println("ConcurrentHashMap 在初始化和扩容时使用自旋优化");
    }

    /**
     * 5. Thread.onSpinWait() - JDK 9+
     *
     * JDK 9 引入的新方法，用于优化自旋循环：
     * - 提示 CPU 当前线程在自旋等待
     * - CPU 可以优化流水线、降低功耗
     * - 在 x86 上映射到 PAUSE 指令
     */
    public static void demo_OnSpinWait() {
        // 使用 Thread.onSpinWait() 的自旋锁
        class OptimizedSpinLock {
            private volatile boolean locked = false;

            public void lock() {
                while (!tryLock()) {
                    // 关键：提示 CPU 我在自旋
                    Thread.onSpinWait();
                    // 等价于 x86 的 PAUSE 指令
                    // 作用：
                    // 1. 减少内存序冲突
                    // 2. 降低功耗
                    // 3. 提高自旋效率
                }
            }

            public boolean tryLock() {
                // 模拟 CAS
                if (!locked) {
                    locked = true;
                    return true;
                }
                return false;
            }

            public void unlock() {
                locked = false;
            }
        }

        OptimizedSpinLock lock = new OptimizedSpinLock();
        System.out.println("Thread.onSpinWait() 优化自旋循环性能");
    }

    /**
     * 6. LongAdder 的分段自旋
     *
     * LongAdder 使用分段 + 自旋策略：
     * - 多个 Cell，每个线程在不同 Cell 上自旋
     * - 减少竞争，提高性能
     */
    public static void demo_LongAdderSpinning() {
        /*
        // LongAdder 的 Cell 更新逻辑（简化）
        final void longAccumulate(...) {
            for (;;) {  // ← 自旋
                if (casBase(...))  // 尝试更新 base
                    break;
                // 失败后，尝试更新 Cell
                if (casCellsBusy())  // 自旋获取锁
                    // 创建或扩容 Cell
                    break;
            }
        }
        */

        System.out.println("LongAdder 使用分段自旋，降低竞争");
    }

    /**
     * 总结：JDK 中自旋锁的应用场景
     */
    public static void summary() {
        System.out.println("\n=== JDK 中自旋锁的应用总结 ===\n");

        System.out.println("1. synchronized 自适应自旋");
        System.out.println("   - 获取锁失败时先自旋，再阻塞");
        System.out.println("   - JVM 自动调整自旋次数\n");

        System.out.println("2. Atomic 类（AtomicInteger, AtomicLong 等）");
        System.out.println("   - 所有操作都是 CAS + 自旋");
        System.out.println("   - 适合低竞争场景\n");

        System.out.println("3. AQS（AbstractQueuedSynchronizer）");
        System.out.println("   - tryAcquire 失败后会短暂自旋");
        System.out.println("   - 再失败才进入等待队列\n");

        System.out.println("4. ReentrantLock（非公平模式）");
        System.out.println("   - 基于 AQS，包含自旋优化");
        System.out.println("   - 先自旋尝试，后阻塞等待\n");

        System.out.println("5. ConcurrentHashMap");
        System.out.println("   - 初始化、扩容时使用自旋");
        System.out.println("   - 减少阻塞开销\n");

        System.out.println("6. LongAdder / LongAccumulator");
        System.out.println("   - 分段 + 自旋");
        System.out.println("   - 高并发场景性能优于 AtomicLong\n");

        System.out.println("7. Thread.onSpinWait()（JDK 9+）");
        System.out.println("   - 优化自旋循环");
        System.out.println("   - 降低功耗，提高效率\n");

        System.out.println("核心原则：");
        System.out.println("✓ 临界区短 → 自旋");
        System.out.println("✓ 临界区长 → 阻塞");
        System.out.println("✓ 自适应调整 → 最佳性能");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== JDK 21 中的自旋锁应用 ===\n");

        demo_AtomicSpinning();
        Thread.sleep(1500);

        summary();
    }
}
