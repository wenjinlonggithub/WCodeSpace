# CountDownLatch 源码学习指南

## 📚 学习路径

### 1. 先看原理图解
📄 `CountDownLatchPrinciple.java`
- 通过详细的注释和 ASCII 图了解内部实现原理
- 理解 AQS、CAS、自旋、共享模式等核心概念
- 掌握 await() 和 countDown() 的执行流程

### 2. 再看源码实现
📄 `MyCountDownLatch.java`
- 完全参照 JDK 源码编写，带详细中文注释
- 理解每一行代码的作用
- 重点关注 `tryAcquireShared()` 和 `tryReleaseShared()` 方法

### 3. 最后跑示例代码
📄 `CountDownLatchDemo.java`
- 运行 5 个实际业务场景的示例
- 观察输出，理解实际应用
- 可以修改代码进行实验

## 🔑 核心知识点

### 1. 基于 AQS 实现
```java
// CountDownLatch 内部持有一个 Sync，继承自 AQS
private static final class Sync extends AbstractQueuedSynchronizer {
    Sync(int count) {
        setState(count);  // 使用 AQS 的 state 存储计数值
    }
}
```

### 2. countDown() 的 CAS 实现
```java
protected boolean tryReleaseShared(int releases) {
    for (;;) {  // 自旋
        int c = getState();
        if (c == 0) return false;  // 已经是0，不再减少（一次性）

        int nextc = c - 1;
        if (compareAndSetState(c, nextc)) {  // CAS 原子更新
            return nextc == 0;  // 减到0返回true，触发唤醒
        }
        // CAS 失败，continue 重试
    }
}
```

**关键点：**
- **CAS (Compare-And-Set)** 保证线程安全
- **自旋重试** 处理并发冲突
- **只有减到 0 才返回 true**，触发唤醒所有等待线程

### 3. await() 的阻塞机制
```java
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}

protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;  // state=0 通过，否则阻塞
}
```

**流程：**
1. state != 0 → 返回 -1 → 进入 AQS 等待队列 → LockSupport.park() 阻塞
2. state == 0 → 返回 1 → 直接通过

### 4. 共享模式 vs 独占模式

| 模式 | 唤醒方式 | 典型应用 |
|------|---------|---------|
| 共享模式 | 一次唤醒所有等待线程 | CountDownLatch, Semaphore |
| 独占模式 | 只唤醒一个线程 | ReentrantLock |

## 🎯 实际业务场景

1. **批量数据导入** - 多线程并行导入，主线程等待全部完成
2. **微服务聚合查询** - 并行调用多个服务，聚合返回结果
3. **报表生成** - 并行生成多个 sheet，最后导出 Excel
4. **应用启动预热** - 多个组件并行初始化，全部就绪后接受请求
5. **压测/秒杀模拟** - 多线程同时开始，模拟高并发场景

## 🔬 实验建议

### 实验1：观察 CAS 并发安全性
运行 `demo5_ConcurrentCountDown()`，观察 100 个线程同时 countDown 的结果

### 实验2：测试超时机制
```java
boolean success = latch.await(2, TimeUnit.SECONDS);
if (!success) {
    System.out.println("超时了！");
}
```

### 实验3：验证一次性特性
```java
CountDownLatch latch = new CountDownLatch(1);
latch.countDown();  // 减到0
latch.countDown();  // 再次调用
System.out.println(latch.getCount());  // 仍然是0，不会变成负数
```

### 实验4：对比性能
```java
// 串行 vs 并行
long start = System.currentTimeMillis();
// 执行任务...
long duration = System.currentTimeMillis() - start;
```

## 📖 深入学习

### 阅读 JDK 源码
```bash
# 找到你的 JDK 安装目录，查看源码
java.util.concurrent.CountDownLatch
java.util.concurrent.locks.AbstractQueuedSynchronizer
```

### 关键类关系
```
CountDownLatch
    └── Sync (extends AbstractQueuedSynchronizer)
            └── state (volatile int)  ← 计数器
            └── CLH Queue             ← 等待队列
            └── CAS 操作               ← 线程安全
```

### AQS 核心方法
- `tryAcquireShared()` - 尝试获取共享锁
- `tryReleaseShared()` - 尝试释放共享锁
- `doAcquireSharedInterruptibly()` - 加入等待队列并阻塞
- `doReleaseShared()` - 唤醒等待线程

## 🤔 常见面试题

### Q1: CountDownLatch 的原理是什么？
**A:** 基于 AQS 实现，使用 AQS 的 state 存储计数值，countDown() 通过 CAS 原子减 1，await() 在 state 不为 0 时阻塞，减到 0 时唤醒所有等待线程。

### Q2: CountDownLatch 为什么是一次性的？
**A:** tryReleaseShared() 中，当 state=0 时直接返回 false，不再修改 state，因此无法重置。

### Q3: CountDownLatch vs CyclicBarrier？
**A:**
- CountDownLatch：一次性，递减到 0，N 个线程完成后通知等待线程
- CyclicBarrier：可重用，累加到 N，N 个线程互相等待，全部到达后继续

### Q4: CountDownLatch 是如何保证线程安全的？
**A:** 使用 CAS (compareAndSetState) 原子更新计数器，配合自旋重试处理并发冲突。

### Q5: await() 会一直阻塞吗？
**A:** 可以使用 `await(timeout, unit)` 设置超时时间，超时后返回 false。

## 🚀 运行示例

```bash
# 编译
javac com/architecture/concurrent/countdownlatch/*.java

# 运行示例
java com.architecture.concurrent.countdownlatch.CountDownLatchDemo
```

## 📝 总结

CountDownLatch 的核心：
1. **AQS state** 存储计数器
2. **CAS + 自旋** 保证线程安全
3. **共享模式** 一次唤醒所有线程
4. **一次性使用** 不可重置

掌握这些，你就理解了 CountDownLatch 的精髓！
