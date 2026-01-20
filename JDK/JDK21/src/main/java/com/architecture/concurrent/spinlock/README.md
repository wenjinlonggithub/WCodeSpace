# 自旋锁（Spin Lock）学习指南

## 📚 什么是自旋锁？

自旋锁是一种基于**忙等待（busy-waiting）**的锁机制：
- 线程获取锁失败时，不会立即阻塞睡眠
- 而是在循环中不断尝试获取锁（自旋）
- 避免了线程上下文切换的开销

## 🎯 学习路径

### 1. 先看原理
📄 `SpinLockPrinciple.java`
- 理解自旋锁的核心思想
- 自旋锁 vs 互斥锁的区别
- 适用场景和优缺点

### 2. 学习实现
按以下顺序阅读源码：

1. **SimpleSpinLock.java** - 最简单的自旋锁
   - 使用 AtomicBoolean + CAS 实现
   - 理解基本原理

2. **TicketSpinLock.java** - 公平的自旋锁
   - 保证 FIFO 顺序
   - 避免线程饥饿

3. **CLHSpinLock.java** - JDK AQS 的基础
   - 每个线程在不同内存位置自旋
   - 减少缓存行争用

4. **AdaptiveSpinLock.java** - 自适应自旋
   - 动态调整自旋次数
   - 综合自旋和阻塞的优点

### 3. 运行示例
📄 `SpinLockDemo.java`
- 7 个实际场景示例
- 性能对比测试
- 理解最佳实践

### 4. 了解 JDK 应用
📄 `JDK21SpinLockUsage.java`
- JDK 中哪些地方用了自旋锁
- Thread.onSpinWait() 的使用

## 🔑 核心知识点

### 1. 自旋锁的基本实现

```java
public class SimpleSpinLock {
    private AtomicBoolean locked = new AtomicBoolean(false);

    public void lock() {
        // 自旋：不断尝试 CAS，直到成功
        while (!locked.compareAndSet(false, true)) {
            // 空循环，持续自旋
        }
    }

    public void unlock() {
        locked.set(false);
    }
}
```

**关键点：**
- CAS 保证原子性
- while 循环实现自旋
- 无阻塞，无上下文切换

### 2. 自旋锁 vs 互斥锁

| 对比项 | 自旋锁 | 互斥锁（synchronized/ReentrantLock） |
|--------|--------|-------------------------------------|
| 获取失败 | 循环等待（自旋） | 阻塞（睡眠） |
| CPU 占用 | 高（一直占用 CPU） | 低（让出 CPU） |
| 上下文切换 | 无 | 有（开销大） |
| 适用场景 | 锁持有时间极短 | 锁持有时间长 |
| 临界区大小 | 几十纳秒~几微秒 | 几微秒以上 |

### 3. 何时使用自旋锁？

**适合场景：**
- ✅ 临界区代码执行时间很短（< 2 次上下文切换时间）
- ✅ 多核 CPU 环境
- ✅ 锁竞争不激烈
- ✅ 对响应时间要求高

**不适合场景：**
- ❌ 临界区代码执行时间长
- ❌ 单核 CPU（浪费时间片）
- ❌ 锁竞争激烈
- ❌ 临界区内有 IO 操作或可能阻塞

**经验法则：**
```
临界区执行时间 < 2 × 上下文切换时间 → 使用自旋锁
临界区执行时间 > 2 × 上下文切换时间 → 使用互斥锁

上下文切换时间通常：1-10 微秒
```

### 4. 自旋锁的演进

```
简单自旋锁 (SimpleSpinLock)
    ↓
票据自旋锁 (TicketSpinLock) - 保证公平性
    ↓
CLH 自旋锁 - 减少缓存争用（JDK AQS 基础）
    ↓
自适应自旋锁 - 动态调整策略
```

### 5. CLH 锁的优势

```
简单自旋锁：所有线程在同一个变量上自旋
CPU0        CPU1        CPU2
  ↓           ↓           ↓
读 locked   读 locked   读 locked  ← 缓存行争用！

CLH 锁：每个线程在不同的内存位置自旋
CPU0              CPU1              CPU2
  ↓                 ↓                 ↓
读 nodeA.locked   读 nodeB.locked   读 nodeC.locked  ← 无争用
```

## 🔬 实验建议

### 实验 1：对比性能
运行 `demo5_PerformanceComparison()`
- 观察短临界区下自旋锁的性能优势
- 对比 synchronized 的开销

### 实验 2：长临界区场景
运行 `demo6_LongCriticalSection()`
- 观察长临界区下自旋锁的劣势
- 理解为什么会浪费 CPU

### 实验 3：公平性验证
运行 `demo2_TicketSpinLock()`
- 观察票据锁的 FIFO 顺序
- 对比简单自旋锁的无序性

### 实验 4：自适应调整
运行 `demo4_AdaptiveSpinLock()`
- 观察自旋次数的动态变化
- 理解自适应策略

## 📖 JDK 中的自旋锁

### 1. synchronized 的自适应自旋
```java
// JDK 6+ 自动优化
synchronized (lock) {
    // 线程尝试获取锁时：
    // 1. 先 CAS 尝试（轻量级锁）
    // 2. 失败后自旋等待
    // 3. 自旋失败后膨胀为重量级锁
}
```

### 2. AtomicInteger 的自旋
```java
public final int getAndIncrement() {
    for (;;) {  // 自旋循环
        int current = get();
        int next = current + 1;
        if (compareAndSet(current, next))
            return current;
        // CAS 失败，继续自旋
    }
}
```

### 3. Thread.onSpinWait() (JDK 9+)
```java
while (!tryLock()) {
    Thread.onSpinWait();  // 提示 CPU：我在自旋
    // 映射到 x86 PAUSE 指令
    // 优化流水线，降低功耗
}
```

## 🎓 常见面试题

### Q1: 什么是自旋锁？
**A:** 自旋锁是获取锁失败时不阻塞，而是在循环中不断尝试获取锁的机制，避免了线程上下文切换的开销。

### Q2: 自旋锁的优缺点？
**A:**
- 优点：无上下文切换，响应快，适合短临界区
- 缺点：持续占用 CPU，不适合长临界区和单核 CPU

### Q3: 自旋锁和互斥锁的区别？
**A:** 自旋锁失败时循环等待（占用 CPU），互斥锁失败时阻塞睡眠（让出 CPU）。

### Q4: JDK 哪里用了自旋锁？
**A:**
- synchronized 的自适应自旋
- AtomicInteger 等原子类
- AQS 的 tryAcquire 尝试
- ConcurrentHashMap 的初始化和扩容

### Q5: 什么是自适应自旋？
**A:** 根据历史获取锁的成功率动态调整自旋次数，成功率高则多自旋，低则少自旋。

### Q6: 什么是 CLH 锁？
**A:** 基于链表的自旋锁，每个线程在前驱节点上自旋，避免了缓存行争用，是 JDK AQS 的实现基础。

### Q7: Thread.onSpinWait() 有什么用？
**A:** 提示 CPU 当前线程在自旋，CPU 可以优化流水线、降低功耗，在 x86 上映射到 PAUSE 指令。

## 🚀 运行示例

```bash
# 编译
javac com/architecture/concurrent/spinlock/*.java

# 运行完整示例
java com.architecture.concurrent.spinlock.SpinLockDemo

# 运行 JDK 应用示例
java com.architecture.concurrent.spinlock.JDK21SpinLockUsage
```

## 📊 性能测试结果参考

**短临界区（< 1 微秒）：**
- 自旋锁：2-3 倍性能优势
- 避免了上下文切换

**长临界区（> 10 微秒）：**
- synchronized 更好
- 自旋锁浪费 CPU

## 🔧 调优参数

### JVM 自旋相关参数（参考）
```bash
# JDK 6-8
-XX:+UseSpinning              # 开启自旋（默认开启）
-XX:PreBlockSpin=10           # 自旋次数（JDK 7+ 已废弃）

# JDK 9+
# 使用自适应自旋，无需手动配置
# 可以使用 Thread.onSpinWait() 优化自旋
```

## 📝 总结

### 自旋锁的核心
1. **CAS + 循环** 实现无阻塞获取锁
2. **适合短临界区**，避免上下文切换
3. **多核 CPU** 下效果最佳
4. **JDK 广泛应用**：synchronized、Atomic、AQS

### 选择建议
```
临界区 < 1 微秒  → 自旋锁（SimpleSpinLock）
临界区 1-10 微秒 → 自适应自旋（AdaptiveSpinLock）
临界区 > 10 微秒 → 互斥锁（synchronized/ReentrantLock）

需要公平性      → TicketSpinLock 或 CLHSpinLock
高竞争场景      → 互斥锁
极致性能        → CLHSpinLock（AQS 风格）
```

掌握这些，你就完全理解了自旋锁！🎉
