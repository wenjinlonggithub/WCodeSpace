package com.architecture.jvm.jmm;

/**
 * happens-before原则演示
 *
 * 演示happens-before的8条规则
 *
 * @author Architecture
 */
public class HappensBeforeDemo {

    private int value;
    private volatile boolean initialized = false;
    private static int staticValue;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== happens-before原则演示 ===\n");

        // 演示1: 程序次序规则
        demonstrateProgramOrderRule();

        // 演示2: 监视器锁规则
        demonstrateMonitorLockRule();

        // 演示3: volatile变量规则
        demonstrateVolatileRule();

        // 演示4: 线程启动规则
        demonstrateThreadStartRule();

        // 演示5: 线程终止规则
        demonstrateThreadJoinRule();

        // 演示6: 传递性
        demonstrateTransitivity();
    }

    /**
     * 规则1: 程序次序规则
     */
    private static void demonstrateProgramOrderRule() {
        System.out.println("【规则1: 程序次序规则 (Program Order Rule)】");
        System.out.println("在单个线程内，按照程序代码顺序，前面的操作happens-before后面的操作\n");

        System.out.println("示例:");
        System.out.println("  int a = 1;         // 操作A");
        System.out.println("  int b = 2;         // 操作B");
        System.out.println("  int c = a + b;     // 操作C");

        System.out.println("\nhappens-before关系:");
        System.out.println("  • A happens-before B");
        System.out.println("  • B happens-before C");
        System.out.println("  • A happens-before C (传递性)");

        System.out.println("\n注意:");
        System.out.println("  • happens-before ≠ 时间上的先后顺序");
        System.out.println("  • 如果重排序不改变结果，仍可能发生重排序");
        System.out.println("  • 例如: A和B之间可能重排序(无数据依赖)");
        System.out.println("  • 但C一定在A和B之后执行(有数据依赖)\n");
    }

    /**
     * 规则2: 监视器锁规则
     */
    private static void demonstrateMonitorLockRule() throws InterruptedException {
        System.out.println("【规则2: 监视器锁规则 (Monitor Lock Rule)】");
        System.out.println("对同一个锁的unlock操作，happens-before后续的lock操作\n");

        final Object lock = new Object();
        final int[] sharedData = {0};

        // 线程1: 写入数据
        Thread writer = new Thread(() -> {
            synchronized (lock) {
                sharedData[0] = 42;
                System.out.println("  [Writer] 写入: sharedData = 42 (持有锁)");
                // 退出同步块时释放锁 (unlock)
            }
            System.out.println("  [Writer] 释放锁 (unlock)");
        });

        writer.start();
        writer.join(); // 确保writer先执行

        // 线程2: 读取数据
        Thread reader = new Thread(() -> {
            System.out.println("  [Reader] 尝试获取锁...");
            synchronized (lock) { // 获取锁 (lock)
                System.out.println("  [Reader] 获取锁成功 (lock)");
                System.out.println("  [Reader] 读取: sharedData = " + sharedData[0]);
                System.out.println("  [Reader] ✓ 一定能看到42!");
            }
        });

        reader.start();
        reader.join();

        System.out.println("\n原理:");
        System.out.println("  • Writer的unlock happens-before Reader的lock");
        System.out.println("  • 根据happens-before语义:");
        System.out.println("    Writer在同步块内的所有修改，对Reader都可见");
        System.out.println("  • 这保证了synchronized的可见性\n");
    }

    /**
     * 规则3: volatile变量规则
     */
    private static void demonstrateVolatileRule() throws InterruptedException {
        System.out.println("【规则3: volatile变量规则 (Volatile Variable Rule)】");
        System.out.println("对volatile变量的写操作，happens-before后续的读操作\n");

        HappensBeforeDemo demo = new HappensBeforeDemo();

        // 线程1: 初始化数据
        Thread initializer = new Thread(() -> {
            demo.value = 42;
            System.out.println("  [Initializer] 写入: value = 42");
            demo.initialized = true; // volatile写
            System.out.println("  [Initializer] 设置: initialized = true (volatile写)");
        });

        initializer.start();
        initializer.join();

        // 线程2: 读取数据
        Thread reader = new Thread(() -> {
            System.out.println("  [Reader] 检查: initialized = " + demo.initialized);
            if (demo.initialized) { // volatile读
                System.out.println("  [Reader] initialized为true (volatile读)");
                System.out.println("  [Reader] 读取: value = " + demo.value);
                System.out.println("  [Reader] ✓ 一定能看到42!");
            }
        });

        reader.start();
        reader.join();

        System.out.println("\n原理:");
        System.out.println("  • initialized的写 happens-before initialized的读");
        System.out.println("  • value的写 happens-before initialized的写 (程序次序)");
        System.out.println("  • 根据传递性: value的写 happens-before value的读");
        System.out.println("  • 这就是volatile的\"可见性保证\"原理\n");
    }

    /**
     * 规则4: 线程启动规则
     */
    private static void demonstrateThreadStartRule() throws InterruptedException {
        System.out.println("【规则4: 线程启动规则 (Thread Start Rule)】");
        System.out.println("Thread.start() happens-before 该线程中的任何操作\n");

        staticValue = 100;
        System.out.println("  [Main] 设置: staticValue = 100");

        Thread thread = new Thread(() -> {
            System.out.println("  [Child] 读取: staticValue = " + staticValue);
            System.out.println("  [Child] ✓ 一定能看到100!");
        });

        System.out.println("  [Main] 调用 thread.start()");
        thread.start();
        thread.join();

        System.out.println("\n原理:");
        System.out.println("  • 主线程在start()之前的所有操作");
        System.out.println("  • happens-before子线程的任何操作");
        System.out.println("  • 保证子线程能看到start()之前的所有修改\n");
    }

    /**
     * 规则5: 线程终止规则
     */
    private static void demonstrateThreadJoinRule() throws InterruptedException {
        System.out.println("【规则5: 线程终止规则 (Thread Termination Rule)】");
        System.out.println("线程中的所有操作，happens-before 其他线程对该线程的join()返回\n");

        final int[] result = {0};

        Thread worker = new Thread(() -> {
            System.out.println("  [Worker] 开始计算...");
            result[0] = 42;
            System.out.println("  [Worker] 计算完成: result = 42");
            System.out.println("  [Worker] 线程即将结束...");
        });

        System.out.println("  [Main] 启动worker线程");
        worker.start();

        System.out.println("  [Main] 调用 worker.join()，等待worker完成...");
        worker.join();

        System.out.println("  [Main] worker.join() 返回");
        System.out.println("  [Main] 读取: result = " + result[0]);
        System.out.println("  [Main] ✓ 一定能看到42!");

        System.out.println("\n原理:");
        System.out.println("  • worker线程的所有操作 happens-before join()的返回");
        System.out.println("  • join()返回后，主线程能看到worker的所有修改");
        System.out.println("  • 这是线程间同步的常用手段\n");
    }

    /**
     * 规则8: 传递性
     */
    private static void demonstrateTransitivity() throws InterruptedException {
        System.out.println("【规则8: 传递性 (Transitivity)】");
        System.out.println("如果A happens-before B，B happens-before C");
        System.out.println("则 A happens-before C\n");

        HappensBeforeDemo demo = new HappensBeforeDemo();

        Thread producer = new Thread(() -> {
            int x = 100;                    // 操作A
            demo.value = x;                 // 操作B: 普通写
            demo.initialized = true;        // 操作C: volatile写
            System.out.println("  [Producer] value = " + x + ", initialized = true");
        });

        producer.start();
        producer.join();

        Thread consumer = new Thread(() -> {
            if (demo.initialized) {         // 操作D: volatile读
                int y = demo.value;         // 操作E: 普通读
                System.out.println("  [Consumer] value = " + y);
                System.out.println("  [Consumer] ✓ 一定能看到100!");
            }
        });

        consumer.start();
        consumer.join();

        System.out.println("\n传递链:");
        System.out.println("  1. A happens-before B (程序次序规则)");
        System.out.println("     int x = 100 → demo.value = x");
        System.out.println();
        System.out.println("  2. B happens-before C (程序次序规则)");
        System.out.println("     demo.value = x → demo.initialized = true");
        System.out.println();
        System.out.println("  3. C happens-before D (volatile规则)");
        System.out.println("     volatile写 → volatile读");
        System.out.println();
        System.out.println("  4. D happens-before E (程序次序规则)");
        System.out.println("     if (initialized) → int y = value");
        System.out.println();
        System.out.println("  根据传递性:");
        System.out.println("    A → B → C → D → E");
        System.out.println("    因此 A happens-before E");
        System.out.println("    int x = 100 一定对 int y = value 可见!");

        System.out.println("\n实际应用:");
        System.out.println("  这就是\"volatile保护普通变量\"的原理:");
        System.out.println("  • 在volatile写之前的所有变量写入");
        System.out.println("  • 都对volatile读之后的所有读取可见");
        System.out.println("  • 这是happens-before传递性的典型应用\n");
    }
}
