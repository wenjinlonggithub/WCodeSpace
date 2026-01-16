package com.architecture.jvm.jmm;

/**
 * Java内存模型(JMM)深度演示
 *
 * Java Memory Model定义了程序中各个变量的访问规则
 *
 * JMM抽象结构:
 * ┌─────────┐  ┌─────────┐  ┌─────────┐
 * │ Thread1 │  │ Thread2 │  │ Thread3 │
 * └────┬────┘  └────┬────┘  └────┬────┘
 *      │            │            │
 *   ┌──▼────┐   ┌──▼────┐   ┌──▼────┐
 *   │工作内存│   │工作内存│   │工作内存│
 *   │(本地) │   │(本地) │   │(本地) │
 *   └───┬───┘   └───┬───┘   └───┬───┘
 *       │  save/load  │            │
 *       └─────────┬───┴────────────┘
 *       ┌─────────▼─────────────────┐
 *       │      主内存 (Main Memory)  │
 *       │   (所有线程共享的变量)     │
 *       └───────────────────────────┘
 *
 * 核心概念：
 * 1. 原子性 (Atomicity)
 * 2. 可见性 (Visibility)
 * 3. 有序性 (Ordering)
 *
 * @author Architecture
 */
public class JavaMemoryModelDemo {

    private static volatile boolean flag = false;
    private static int sharedValue = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Java内存模型(JMM)深度演示 ===\n");

        // 1. JMM基础
        introduceJMM();

        // 2. 可见性问题
        demonstrateVisibility();

        // 3. volatile关键字
        demonstrateVolatile();

        // 4. happens-before原则
        explainHappensBefore();

        // 5. synchronized原理
        demonstrateSynchronized();

        // 6. 指令重排序
        demonstrateReordering();

        // 7. final语义
        demonstrateFinal();

        // 8. 内存屏障
        explainMemoryBarriers();
    }

    /**
     * 1. JMM基础介绍
     */
    private static void introduceJMM() {
        System.out.println("【1. Java内存模型(JMM)基础】\n");

        System.out.println("JMM是什么？");
        System.out.println("  • Java虚拟机规范定义的抽象概念");
        System.out.println("  • 描述线程如何通过内存进行交互");
        System.out.println("  • 屏蔽各种硬件和操作系统的内存访问差异");

        System.out.println("\nJMM主要目标:");
        System.out.println("  1. 定义程序中各个变量的访问规则");
        System.out.println("  2. 保证并发程序的正确性");
        System.out.println("  3. 在保证正确性的前提下,提供性能优化");

        System.out.println("\n主内存与工作内存:");
        System.out.println("  主内存 (Main Memory):");
        System.out.println("    • 所有线程共享");
        System.out.println("    • 存储实例字段、静态字段、数组元素");
        System.out.println("    • 对应物理上的堆内存和方法区");

        System.out.println("\n  工作内存 (Working Memory):");
        System.out.println("    • 线程私有");
        System.out.println("    • 存储主内存变量的副本");
        System.out.println("    • 对应物理上的CPU寄存器和高速缓存");

        System.out.println("\n内存交互操作 (8种原子操作):");
        System.out.println("  lock    (锁定)   : 锁定主内存变量");
        System.out.println("  unlock  (解锁)   : 解锁主内存变量");
        System.out.println("  read    (读取)   : 从主内存读取");
        System.out.println("  load    (载入)   : 载入到工作内存");
        System.out.println("  use     (使用)   : 传递给执行引擎");
        System.out.println("  assign  (赋值)   : 执行引擎赋值到工作内存");
        System.out.println("  store   (存储)   : 传送到主内存");
        System.out.println("  write   (写入)   : 写入主内存");
        System.out.println();
    }

    /**
     * 2. 可见性问题演示
     */
    private static void demonstrateVisibility() {
        System.out.println("【2. 可见性问题】\n");

        System.out.println("什么是可见性？");
        System.out.println("  • 一个线程修改了变量,其他线程能否立即看到");
        System.out.println("  • CPU缓存导致的可见性问题");

        System.out.println("\n可见性问题示例:");
        System.out.println("  Thread 1                Thread 2");
        System.out.println("  --------                --------");
        System.out.println("  int value = 0;          ");
        System.out.println("  value = 1;          →   while(value == 0) {");
        System.out.println("                              // 可能永远循环!");
        System.out.println("                          }");

        System.out.println("\n原因:");
        System.out.println("  1. Thread2将value读入CPU缓存");
        System.out.println("  2. Thread1修改了主内存中的value");
        System.out.println("  3. Thread2的缓存未失效,仍读取旧值");

        System.out.println("\n解决方案:");
        System.out.println("  • 使用volatile关键字");
        System.out.println("  • 使用synchronized同步");
        System.out.println("  • 使用Lock锁");
        System.out.println("  • 使用Atomic原子类");
        System.out.println();
    }

    /**
     * 3. volatile关键字演示
     */
    private static void demonstrateVolatile() throws InterruptedException {
        System.out.println("【3. volatile关键字】\n");

        System.out.println("volatile的两大特性:");
        System.out.println("  1. 保证可见性");
        System.out.println("     • 写入立即刷新到主内存");
        System.out.println("     • 读取从主内存读取最新值");

        System.out.println("\n  2. 禁止指令重排序");
        System.out.println("     • 插入内存屏障");
        System.out.println("     • 保证有序性");

        System.out.println("\nvolatile不保证原子性:");
        System.out.println("  volatile int count = 0;");
        System.out.println("  count++;  // 非原子操作!");
        System.out.println("  分解为:");
        System.out.println("    1. 读取count");
        System.out.println("    2. 加1");
        System.out.println("    3. 写回count");

        System.out.println("\n演示:");
        VolatileExample example = new VolatileExample();
        example.start();
        Thread.sleep(100);
        example.stop();
        System.out.println();
    }

    static class VolatileExample {
        private volatile boolean running = true;

        public void start() {
            new Thread(() -> {
                System.out.println("  线程启动,等待停止信号...");
                while (running) {
                    // 使用volatile,能正确读取到false
                }
                System.out.println("  线程停止!");
            }).start();
        }

        public void stop() {
            System.out.println("  发送停止信号...");
            running = false;
        }
    }

    /**
     * 4. happens-before原则
     */
    private static void explainHappensBefore() {
        System.out.println("【4. happens-before原则】\n");

        System.out.println("happens-before是什么？");
        System.out.println("  • JMM中定义的两个操作之间的偏序关系");
        System.out.println("  • 如果A happens-before B,则A的结果对B可见");

        System.out.println("\n8条happens-before规则:");

        System.out.println("\n  1. 程序次序规则 (Program Order Rule)");
        System.out.println("     • 单线程内,按代码顺序执行");
        System.out.println("     • int a = 1; int b = a + 1;");
        System.out.println("     • a的赋值 happens-before b的赋值");

        System.out.println("\n  2. 监视器锁规则 (Monitor Lock Rule)");
        System.out.println("     • unlock happens-before 后续的lock");
        System.out.println("     • 解锁后的修改对下次加锁可见");

        System.out.println("\n  3. volatile变量规则 (Volatile Variable Rule)");
        System.out.println("     • volatile写 happens-before volatile读");
        System.out.println("     • 写入的值对后续读取立即可见");

        System.out.println("\n  4. 线程启动规则 (Thread Start Rule)");
        System.out.println("     • Thread.start() happens-before 线程中的任何操作");

        System.out.println("\n  5. 线程终止规则 (Thread Termination Rule)");
        System.out.println("     • 线程中的操作 happens-before Thread.join()返回");

        System.out.println("\n  6. 线程中断规则 (Thread Interruption Rule)");
        System.out.println("     • interrupt() happens-before 检测到中断");

        System.out.println("\n  7. 对象终结规则 (Finalizer Rule)");
        System.out.println("     • 构造函数结束 happens-before finalize()方法");

        System.out.println("\n  8. 传递性 (Transitivity)");
        System.out.println("     • A happens-before B, B happens-before C");
        System.out.println("     • 则 A happens-before C");

        System.out.println("\n应用示例:");
        System.out.println("  private int value;");
        System.out.println("  private volatile boolean initialized = false;");
        System.out.println();
        System.out.println("  // Thread 1");
        System.out.println("  value = 42;              // (1)");
        System.out.println("  initialized = true;      // (2) volatile写");
        System.out.println();
        System.out.println("  // Thread 2");
        System.out.println("  if (initialized) {       // (3) volatile读");
        System.out.println("    int x = value;         // (4) 一定能看到42!");
        System.out.println("  }");
        System.out.println();
        System.out.println("  分析:");
        System.out.println("    • (1) happens-before (2) - 程序次序规则");
        System.out.println("    • (2) happens-before (3) - volatile规则");
        System.out.println("    • (3) happens-before (4) - 程序次序规则");
        System.out.println("    • 根据传递性: (1) happens-before (4)");
        System.out.println();
    }

    /**
     * 5. synchronized原理
     */
    private static void demonstrateSynchronized() {
        System.out.println("【5. synchronized原理】\n");

        System.out.println("synchronized的三大特性:");
        System.out.println("  1. 原子性 - 同一时刻只有一个线程执行");
        System.out.println("  2. 可见性 - 解锁前的修改对后续加锁可见");
        System.out.println("  3. 有序性 - 在锁内部禁止重排序到锁外");

        System.out.println("\nsynchronized的底层实现:");
        System.out.println("  方法级synchronized:");
        System.out.println("    • ACC_SYNCHRONIZED标志");
        System.out.println("    • JVM隐式调用monitorenter/monitorexit");

        System.out.println("\n  代码块synchronized:");
        System.out.println("    • monitorenter指令 - 进入同步块");
        System.out.println("    • monitorexit指令  - 退出同步块");

        System.out.println("\nMonitor对象结构:");
        System.out.println("  ┌───────────────────┐");
        System.out.println("  │ Owner (持有线程)  │");
        System.out.println("  ├───────────────────┤");
        System.out.println("  │ EntryList (等待队列)│");
        System.out.println("  ├───────────────────┤");
        System.out.println("  │ WaitSet (等待集合) │");
        System.out.println("  └───────────────────┘");

        System.out.println("\n锁优化技术:");
        System.out.println("  1. 偏向锁 (Biased Locking)");
        System.out.println("     • 适用于单线程场景");
        System.out.println("     • 减少轻量级锁的开销");

        System.out.println("\n  2. 轻量级锁 (Lightweight Locking)");
        System.out.println("     • 适用于竞争较少的场景");
        System.out.println("     • 使用CAS操作替代互斥量");

        System.out.println("\n  3. 重量级锁 (Heavyweight Locking)");
        System.out.println("     • 竞争激烈时使用");
        System.out.println("     • 调用操作系统互斥量");

        System.out.println("\n  4. 自适应自旋 (Adaptive Spinning)");
        System.out.println("     • 获取锁失败时自旋等待");
        System.out.println("     • 避免线程切换开销");

        System.out.println("\n  5. 锁消除 (Lock Elimination)");
        System.out.println("     • JIT编译器优化");
        System.out.println("     • 消除不可能竞争的锁");

        System.out.println("\n  6. 锁粗化 (Lock Coarsening)");
        System.out.println("     • 合并连续的加锁解锁");
        System.out.println("     • 减少加锁次数");
        System.out.println();
    }

    /**
     * 6. 指令重排序
     */
    private static void demonstrateReordering() {
        System.out.println("【6. 指令重排序】\n");

        System.out.println("什么是重排序？");
        System.out.println("  • 编译器和处理器为优化性能而改变指令执行顺序");

        System.out.println("\n三种重排序:");
        System.out.println("  1. 编译器优化重排序");
        System.out.println("     • 编译器在不改变单线程语义前提下重排");

        System.out.println("\n  2. 指令级并行重排序");
        System.out.println("     • CPU将指令并行执行");

        System.out.println("\n  3. 内存系统重排序");
        System.out.println("     • 处理器使用缓存和读写缓冲区");

        System.out.println("\n经典示例:");
        System.out.println("  // 初始: a = 0, b = 0, x = 0, y = 0");
        System.out.println();
        System.out.println("  Thread 1          Thread 2");
        System.out.println("  --------          --------");
        System.out.println("  x = 1;            y = 1;");
        System.out.println("  int r1 = y;       int r2 = x;");
        System.out.println();
        System.out.println("  可能的结果:");
        System.out.println("    • r1 = 0, r2 = 1");
        System.out.println("    • r1 = 1, r2 = 0");
        System.out.println("    • r1 = 1, r2 = 1");
        System.out.println("    • r1 = 0, r2 = 0  ← 重排序导致!");

        System.out.println("\n如何禁止重排序？");
        System.out.println("  • 使用volatile");
        System.out.println("  • 使用synchronized");
        System.out.println("  • 使用Lock");
        System.out.println("  • 使用final (部分禁止)");
        System.out.println();
    }

    /**
     * 7. final语义
     */
    private static void demonstrateFinal() {
        System.out.println("【7. final语义】\n");

        System.out.println("final的内存语义:");
        System.out.println("  1. final字段的写入,happens-before 构造函数return");
        System.out.println("  2. 初次读取包含final字段的对象引用,");
        System.out.println("     happens-before 读取final字段");

        System.out.println("\nfinal的重排序规则:");
        System.out.println("  1. 禁止final字段写入重排序到构造函数外");
        System.out.println("  2. 禁止初次读对象引用重排序到读final字段之后");

        System.out.println("\n示例:");
        System.out.println("  class FinalExample {");
        System.out.println("    private final int x;");
        System.out.println("    private int y;");
        System.out.println("    private static FinalExample obj;");
        System.out.println();
        System.out.println("    public FinalExample() {");
        System.out.println("      x = 3;        // final字段写入");
        System.out.println("      y = 4;        // 普通字段写入");
        System.out.println("    }             // 构造函数返回");
        System.out.println();
        System.out.println("    public static void writer() {");
        System.out.println("      obj = new FinalExample();");
        System.out.println("    }");
        System.out.println();
        System.out.println("    public static void reader() {");
        System.out.println("      FinalExample o = obj;");
        System.out.println("      int a = o.x;  // 一定能看到3");
        System.out.println("      int b = o.y;  // 可能看不到4");
        System.out.println("    }");
        System.out.println("  }");

        System.out.println("\nfinal的好处:");
        System.out.println("  • 保证对象发布的安全性");
        System.out.println("  • 不可变对象天然线程安全");
        System.out.println("  • JIT可以进行优化(常量折叠等)");
        System.out.println();
    }

    /**
     * 8. 内存屏障
     */
    private static void explainMemoryBarriers() {
        System.out.println("【8. 内存屏障 (Memory Barriers)】\n");

        System.out.println("什么是内存屏障？");
        System.out.println("  • 一种CPU指令");
        System.out.println("  • 禁止指令重排序");
        System.out.println("  • 强制刷新缓存/缓冲区");

        System.out.println("\n四种内存屏障:");

        System.out.println("\n  1. LoadLoad Barrier");
        System.out.println("     Load1; LoadLoad; Load2");
        System.out.println("     • 确保Load1先于Load2执行");

        System.out.println("\n  2. StoreStore Barrier");
        System.out.println("     Store1; StoreStore; Store2");
        System.out.println("     • 确保Store1先于Store2,且Store1刷新到内存");

        System.out.println("\n  3. LoadStore Barrier");
        System.out.println("     Load1; LoadStore; Store2");
        System.out.println("     • 确保Load1先于Store2");

        System.out.println("\n  4. StoreLoad Barrier");
        System.out.println("     Store1; StoreLoad; Load2");
        System.out.println("     • 确保Store1刷新到内存后再执行Load2");
        System.out.println("     • 开销最大的屏障");

        System.out.println("\nvolatile的内存屏障插入策略:");
        System.out.println("  volatile写操作:");
        System.out.println("    StoreStore");
        System.out.println("    volatile写");
        System.out.println("    StoreLoad");

        System.out.println("\n  volatile读操作:");
        System.out.println("    volatile读");
        System.out.println("    LoadLoad");
        System.out.println("    LoadStore");

        System.out.println("\n内存屏障的作用:");
        System.out.println("  • 禁止特定类型的处理器重排序");
        System.out.println("  • 强制刷新处理器缓存");
        System.out.println("  • 保证内存可见性");
        System.out.println();
    }
}
