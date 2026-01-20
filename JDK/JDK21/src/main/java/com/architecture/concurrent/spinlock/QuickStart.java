package com.architecture.concurrent.spinlock;

/**
 * 自旋锁学习包 - 快速开始指南
 *
 * ==========================================
 * 📚 学习包内容
 * ==========================================
 *
 * 1. SpinLockPrinciple.java - 原理详解（必读！）
 *    - 什么是自旋锁
 *    - 自旋锁 vs 互斥锁
 *    - 适用场景和演进历史
 *    - 缓存一致性问题
 *
 * 2. SimpleSpinLock.java - 最简单的自旋锁
 *    - AtomicBoolean + CAS 实现
 *    - 理解基本原理
 *
 * 3. TicketSpinLock.java - 公平的自旋锁
 *    - 类似银行取号排队
 *    - 保证 FIFO 顺序
 *    - 避免线程饥饿
 *
 * 4. CLHSpinLock.java - JDK AQS 的基础
 *    - 每个线程在不同内存位置自旋
 *    - 减少缓存行争用
 *    - 最重要的自旋锁实现
 *
 * 5. AdaptiveSpinLock.java - 自适应自旋
 *    - 动态调整自旋次数
 *    - 结合自旋和阻塞的优点
 *    - JDK synchronized 的优化策略
 *
 * 6. SpinLockDemo.java - 7 个实际场景示例
 *    - 基本使用
 *    - 公平性演示
 *    - 性能对比
 *    - 实际应用
 *
 * 7. JDK21SpinLockUsage.java - JDK 中的应用
 *    - synchronized 自适应自旋
 *    - AtomicInteger 的自旋
 *    - Thread.onSpinWait() 的使用
 *
 * 8. SpinLockBenchmark.java - 性能基准测试
 *    - 短/中/长临界区性能对比
 *    - 不同锁的性能测试
 *    - 竞争强度测试
 *
 * 9. README.md - 完整学习指南
 *    - 学习路径
 *    - 核心知识点
 *    - 实验建议
 *    - 面试题
 *
 * ==========================================
 * 🎯 推荐学习顺序
 * ==========================================
 *
 * 第一步：理解概念
 *   ├─ SpinLockPrinciple.java（读注释）
 *   └─ README.md（快速概览）
 *
 * 第二步：学习实现（按顺序）
 *   ├─ SimpleSpinLock.java（最简单）
 *   ├─ TicketSpinLock.java（公平性）
 *   ├─ CLHSpinLock.java（重点！AQS 基础）
 *   └─ AdaptiveSpinLock.java（自适应）
 *
 * 第三步：运行示例
 *   ├─ SpinLockDemo.java（7 个场景）
 *   └─ SpinLockBenchmark.java（性能测试）
 *
 * 第四步：了解 JDK 应用
 *   └─ JDK21SpinLockUsage.java
 *
 * ==========================================
 * 🚀 快速开始
 * ==========================================
 *
 * # 1. 编译所有文件
 * javac com/architecture/concurrent/spinlock/*.java
 *
 * # 2. 运行示例（推荐先运行这个）
 * java com.architecture.concurrent.spinlock.SpinLockDemo
 *
 * # 3. 运行性能测试
 * java com.architecture.concurrent.spinlock.SpinLockBenchmark
 *
 * # 4. 运行 JDK 应用示例
 * java com.architecture.concurrent.spinlock.JDK21SpinLockUsage
 *
 * ==========================================
 * 🔑 核心要点
 * ==========================================
 *
 * 1. 自旋锁原理
 *    - 获取锁失败时不阻塞，而是在循环中不断尝试
 *    - 避免了线程上下文切换的开销
 *    - 使用 CAS 保证原子性
 *
 * 2. 适用场景
 *    ✓ 临界区执行时间极短（< 1 微秒）
 *    ✓ 多核 CPU 环境
 *    ✓ 锁竞争不激烈
 *    ✗ 单核 CPU
 *    ✗ 临界区时间长
 *    ✗ 高竞争场景
 *
 * 3. 性能对比
 *    短临界区：自旋锁 > synchronized（2-3x）
 *    长临界区：synchronized > 自旋锁
 *
 * 4. JDK 应用
 *    - synchronized 的自适应自旋
 *    - AtomicInteger 等原子类
 *    - AQS 的 CLH 队列
 *    - ConcurrentHashMap 的初始化
 *
 * ==========================================
 * 🎓 面试重点
 * ==========================================
 *
 * Q1: 什么是自旋锁？
 * A: 获取锁失败时在循环中不断尝试，而不是阻塞睡眠
 *
 * Q2: 自旋锁的优缺点？
 * A: 优点：无上下文切换，响应快
 *    缺点：占用 CPU，不适合长临界区
 *
 * Q3: 何时使用自旋锁？
 * A: 临界区执行时间 < 2 次上下文切换时间（约 1 微秒）
 *
 * Q4: 什么是 CLH 锁？
 * A: 基于链表的自旋锁，每个线程在前驱节点上自旋，
 *    是 JDK AQS 的实现基础
 *
 * Q5: JDK 哪里用了自旋锁？
 * A: synchronized、AtomicInteger、AQS、ConcurrentHashMap
 *
 * ==========================================
 * 💡 实验建议
 * ==========================================
 *
 * 实验 1：对比性能
 *   - 运行 SpinLockBenchmark
 *   - 观察短/长临界区的性能差异
 *
 * 实验 2：公平性
 *   - 运行 demo2_TicketSpinLock()
 *   - 观察 FIFO 顺序
 *
 * 实验 3：自适应调整
 *   - 运行 demo4_AdaptiveSpinLock()
 *   - 观察自旋次数的动态变化
 *
 * 实验 4：修改临界区大小
 *   - 修改 demo6 的 sleepTime
 *   - 观察性能变化的临界点
 *
 * ==========================================
 * 📖 延伸阅读
 * ==========================================
 *
 * 1. JDK 源码
 *    - java.util.concurrent.locks.AbstractQueuedSynchronizer
 *    - java.util.concurrent.atomic.AtomicInteger
 *    - java.lang.Thread.onSpinWait()
 *
 * 2. 相关论文
 *    - "The Performance of Spin Lock Alternatives for
 *       Shared-Memory Multiprocessors"
 *    - CLH 锁原始论文（1993）
 *
 * 3. JVM 优化
 *    - 偏向锁、轻量级锁、重量级锁
 *    - 锁消除、锁粗化
 *
 * ==========================================
 * 📝 总结
 * ==========================================
 *
 * 自旋锁是并发编程的基础工具：
 * - 理解原理：CAS + 循环
 * - 掌握适用场景：短临界区
 * - 熟悉 JDK 应用：synchronized、Atomic、AQS
 * - 重点学习：CLH 锁（AQS 基础）
 *
 * 祝学习顺利！🎉
 */
public class QuickStart {
    public static void main(String[] args) {
        System.out.println("=== 自旋锁学习包 - 快速开始 ===\n");

        System.out.println("📚 推荐学习顺序：\n");
        System.out.println("1. 阅读 SpinLockPrinciple.java 的注释（理解原理）");
        System.out.println("2. 依次查看 Simple → Ticket → CLH → Adaptive 的实现");
        System.out.println("3. 运行 SpinLockDemo.java（查看 7 个实际场景）");
        System.out.println("4. 运行 SpinLockBenchmark.java（性能测试）");
        System.out.println("5. 了解 JDK21SpinLockUsage.java（JDK 中的应用）\n");

        System.out.println("🚀 快速运行示例：\n");
        System.out.println("javac com/architecture/concurrent/spinlock/*.java");
        System.out.println("java com.architecture.concurrent.spinlock.SpinLockDemo\n");

        System.out.println("💡 核心要点：\n");
        System.out.println("- 自旋锁 = CAS + 循环（不阻塞）");
        System.out.println("- 适合短临界区（< 1 微秒）");
        System.out.println("- CLH 锁是 JDK AQS 的基础（重点！）");
        System.out.println("- JDK 广泛应用：synchronized、Atomic、AQS\n");

        System.out.println("详细信息请查看 README.md 或本类的注释！");
    }
}
