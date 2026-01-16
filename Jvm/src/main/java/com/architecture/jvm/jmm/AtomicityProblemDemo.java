package com.architecture.jvm.jmm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 原子性问题演示
 *
 * 演示volatile不保证原子性的问题
 *
 * @author Architecture
 */
public class AtomicityProblemDemo {

    // 场景1: 普通变量 - 既不保证可见性，也不保证原子性
    private static int normalCount = 0;

    // 场景2: volatile变量 - 保证可见性，但不保证原子性
    private static volatile int volatileCount = 0;

    // 场景3: AtomicInteger - 保证原子性
    private static AtomicInteger atomicCount = new AtomicInteger(0);

    // 场景4: synchronized保护的变量 - 保证原子性和可见性
    private static int synchronizedCount = 0;
    private static final Object lock = new Object();

    private static final int THREAD_COUNT = 10;
    private static final int INCREMENT_COUNT = 1000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 原子性问题演示 ===");
        System.out.println("每个线程执行 " + INCREMENT_COUNT + " 次自增操作");
        System.out.println("预期结果: " + (THREAD_COUNT * INCREMENT_COUNT) + "\n");

        // 演示1: 普通变量
        testNormalVariable();

        // 演示2: volatile变量（不保证原子性）
        testVolatileVariable();

        // 演示3: AtomicInteger（保证原子性）
        testAtomicInteger();

        // 演示4: synchronized（保证原子性）
        testSynchronized();
    }

    /**
     * 测试普通变量
     */
    private static void testNormalVariable() throws InterruptedException {
        System.out.println("【演示1: 普通变量】");
        normalCount = 0;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < INCREMENT_COUNT; j++) {
                    normalCount++; // 非原子操作
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("  普通变量最终值: " + normalCount);
        System.out.println("  ❌ 结果不正确 - 既有可见性问题，也有原子性问题");
        System.out.println("  原因: count++ 分解为三步 (读-改-写)，多线程交错执行\n");
    }

    /**
     * 测试volatile变量
     */
    private static void testVolatileVariable() throws InterruptedException {
        System.out.println("【演示2: volatile变量】");
        volatileCount = 0;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < INCREMENT_COUNT; j++) {
                    volatileCount++; // volatile不保证原子性
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("  volatile变量最终值: " + volatileCount);
        System.out.println("  ❌ 结果仍然不正确 - volatile只保证可见性，不保证原子性");

        System.out.println("\n  volatileCount++ 的执行过程:");
        System.out.println("    1. 读取volatileCount的值 (从主内存读取)");
        System.out.println("    2. 将值加1 (在工作内存中)");
        System.out.println("    3. 写回volatileCount (刷新到主内存)");
        System.out.println("  问题: 步骤1和步骤3之间可能被其他线程打断!\n");

        System.out.println("  线程交错示例:");
        System.out.println("    时刻  Thread1          Thread2          volatileCount");
        System.out.println("    T1    读取(0)                           0");
        System.out.println("    T2                     读取(0)          0");
        System.out.println("    T3    加1(得1)                          0");
        System.out.println("    T4                     加1(得1)         0");
        System.out.println("    T5    写回(1)                           1");
        System.out.println("    T6                     写回(1)          1");
        System.out.println("    结果: 两次自增，最终值却是1，丢失了一次更新!\n");
    }

    /**
     * 测试AtomicInteger
     */
    private static void testAtomicInteger() throws InterruptedException {
        System.out.println("【演示3: AtomicInteger】");
        atomicCount.set(0);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < INCREMENT_COUNT; j++) {
                    atomicCount.incrementAndGet(); // 原子操作
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("  AtomicInteger最终值: " + atomicCount.get());
        System.out.println("  ✓ 结果正确! AtomicInteger使用CAS保证原子性");

        System.out.println("\n  CAS (Compare-And-Swap) 原理:");
        System.out.println("    while (true) {");
        System.out.println("      int current = get();              // 读取当前值");
        System.out.println("      int next = current + 1;           // 计算新值");
        System.out.println("      if (compareAndSet(current, next)) // CAS原子更新");
        System.out.println("        break;                          // 成功则退出");
        System.out.println("      // 失败则重试 (说明其他线程修改了值)");
        System.out.println("    }");
        System.out.println("  优点: 无锁化，性能高\n");
    }

    /**
     * 测试synchronized
     */
    private static void testSynchronized() throws InterruptedException {
        System.out.println("【演示4: synchronized】");
        synchronizedCount = 0;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < INCREMENT_COUNT; j++) {
                    synchronized (lock) {
                        synchronizedCount++; // 同步块保护
                    }
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("  synchronized变量最终值: " + synchronizedCount);
        System.out.println("  ✓ 结果正确! synchronized保证原子性、可见性和有序性");

        System.out.println("\n  synchronized的三大特性:");
        System.out.println("    • 原子性: 同一时刻只有一个线程执行同步块");
        System.out.println("    • 可见性: 解锁前的修改对后续加锁线程可见");
        System.out.println("    • 有序性: 禁止将同步块内的操作重排序到块外");
    }
}
