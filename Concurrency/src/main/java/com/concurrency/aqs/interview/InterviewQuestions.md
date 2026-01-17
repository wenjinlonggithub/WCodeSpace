# AQS 面试题汇总

## 一、基础概念题

### 1. 什么是AQS？它的作用是什么？

**答案：**
AQS（AbstractQueuedSynchronizer）是Java并发包的核心基础组件，是一个用于构建锁和同步器的框架。

**核心作用：**
- 提供了一套通用的同步器实现框架
- 管理同步状态（state）和等待队列
- 支持独占模式和共享模式
- 简化了自定义同步器的实现

**典型应用：**
- ReentrantLock、ReentrantReadWriteLock
- Semaphore、CountDownLatch
- ThreadPoolExecutor的Worker

---

### 2. AQS的核心数据结构是什么？

**答案：**

**1. 同步状态（state）**
```java
private volatile int state;
```
- 使用volatile保证可见性
- 通过CAS操作保证原子性
- 不同同步器赋予不同含义

**2. CLH队列**
```java
private transient volatile Node head;
private transient volatile Node tail;
```
- 双向链表结构
- FIFO队列
- 存储等待线程

**3. Node节点**
```java
static final class Node {
    volatile int waitStatus;  // 等待状态
    volatile Node prev;       // 前驱
    volatile Node next;       // 后继
    volatile Thread thread;   // 线程
    Node nextWaiter;         // 条件队列
}
```

---

### 3. AQS支持哪两种模式？有什么区别？

**答案：**

**独占模式（Exclusive）**
- 同一时刻只有一个线程能获取资源
- 典型应用：ReentrantLock
- 核心方法：tryAcquire()、tryRelease()

**共享模式（Shared）**
- 同一时刻可以有多个线程获取资源
- 典型应用：Semaphore、CountDownLatch
- 核心方法：tryAcquireShared()、tryReleaseShared()

**主要区别：**
| 特性 | 独占模式 | 共享模式 |
|------|---------|---------|
| 并发数 | 1 | 多个 |
| 释放传播 | 不传播 | 传播唤醒 |
| 返回值 | boolean | int |

---

## 二、原理深入题

### 4. 详细说明AQS的acquire()流程

**答案：**

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

**执行流程：**

1. **tryAcquire(arg)** - 尝试获取资源
   - 由子类实现具体逻辑
   - 成功返回true，失败返回false

2. **addWaiter(Node.EXCLUSIVE)** - 加入等待队列
   - 创建独占模式节点
   - CAS操作添加到队列尾部
   - 返回新创建的节点

3. **acquireQueued(node, arg)** - 自旋获取
   - 如果前驱是head，尝试获取资源
   - 获取成功，设置自己为head
   - 获取失败，判断是否需要阻塞
   - 使用LockSupport.park()阻塞线程

4. **selfInterrupt()** - 中断处理
   - 如果等待过程中被中断，补上中断标记

---

### 5. AQS如何实现公平锁和非公平锁？

**答案：**

**非公平锁实现：**
```java
final boolean nonfairTryAcquire(int acquires) {
    if (compareAndSetState(0, acquires)) {
        // 直接CAS获取，不检查队列
        setExclusiveOwnerThread(Thread.currentThread());
        return true;
    }
    return false;
}
```

**公平锁实现：**
```java
protected final boolean tryAcquire(int acquires) {
    if (!hasQueuedPredecessors() &&  // 检查队列
        compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread());
        return true;
    }
    return false;
}
```

**关键区别：**
- 公平锁：调用`hasQueuedPredecessors()`检查队列中是否有等待线程
- 非公平锁：直接尝试CAS获取，不管队列状态

**性能对比：**
- 非公平锁：吞吐量高，可能导致线程饥饿
- 公平锁：保证FIFO，但性能较低

---

### 6. 什么是CLH队列？它的特点是什么？

**答案：**

CLH（Craig, Landin, and Hagersten）队列是一个虚拟的双向队列。

**特点：**
1. **虚拟队列**：不存在队列实例，仅存在节点关联
2. **双向链表**：通过prev和next指针连接
3. **FIFO顺序**：先进先出
4. **虚拟头节点**：head不存储线程信息

**节点状态（waitStatus）：**
```java
static final int CANCELLED =  1;  // 已取消
static final int SIGNAL    = -1;  // 需要唤醒后继
static final int CONDITION = -2;  // 在条件队列中
static final int PROPAGATE = -3;  // 共享模式传播
// 0: 初始状态
```

---

## 三、实战应用题

### 7. 如何使用AQS实现一个自定义锁？

**答案：**

参考代码：`custom/CustomLock.java`

**关键步骤：**
1. 继承AbstractQueuedSynchronizer
2. 实现tryAcquire()和tryRelease()
3. 实现可重入逻辑
4. 封装Lock接口

**核心代码：**
```java
class Sync extends AbstractQueuedSynchronizer {
    protected boolean tryAcquire(int arg) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        // 可重入检查
        if (getExclusiveOwnerThread() == Thread.currentThread()) {
            setState(getState() + 1);
            return true;
        }
        return false;
    }
}
```

---

### 8. ReentrantLock和synchronized的区别？

**答案：**

| 特性 | ReentrantLock | synchronized |
|------|--------------|--------------|
| 实现层面 | JDK层面（AQS） | JVM层面 |
| 锁类型 | 可选公平/非公平 | 非公平 |
| 可中断 | 支持 | 不支持 |
| 超时获取 | 支持 | 不支持 |
| 条件变量 | 多个Condition | 单个wait/notify |
| 自动释放 | 需手动unlock | 自动释放 |
| 性能 | 相近 | 相近（JDK6+优化） |

**使用建议：**
- 简单场景：使用synchronized
- 需要高级特性：使用ReentrantLock

---

### 9. CountDownLatch和CyclicBarrier的区别？

**答案：**

| 特性 | CountDownLatch | CyclicBarrier |
|------|----------------|---------------|
| 基础实现 | AQS共享模式 | ReentrantLock + Condition |
| 可重用性 | 不可重用 | 可重用 |
| 计数方向 | 递减到0 | 递增到N |
| 等待方式 | await()等待 | await()等待 |
| 回调支持 | 不支持 | 支持barrierAction |

**使用场景：**
- CountDownLatch：一次性事件，如启动信号
- CyclicBarrier：循环使用，如分阶段任务

---

## 四、性能优化题

### 10. AQS如何保证高性能？

**答案：**

**1. CAS操作**
- 避免重量级锁
- 减少上下文切换
- 乐观锁策略

**2. 自旋优化**
- 短时间自旋避免阻塞
- 减少线程切换开销

**3. LockSupport**
- 精确的线程阻塞/唤醒
- 比wait/notify更高效

**4. volatile + CAS**
- state使用volatile保证可见性
- CAS保证原子性
- 避免synchronized开销

---

### 11. 什么情况下会发生线程饥饿？如何避免？

**答案：**

**发生场景：**
1. 非公平锁下，新线程不断插队
2. 优先级调度不当
3. 资源分配不均

**避免方法：**
1. 使用公平锁
```java
ReentrantLock lock = new ReentrantLock(true);
```

2. 合理设置超时
```java
if (!lock.tryLock(timeout, TimeUnit.SECONDS)) {
    // 处理获取失败
}
```

3. 监控等待时间
4. 使用信号量限制并发

---

## 五、源码分析题

### 12. 为什么AQS使用模板方法模式？

**答案：**

**设计优势：**
1. **框架复用**：AQS提供通用流程
2. **灵活扩展**：子类实现特定逻辑
3. **代码简化**：避免重复实现队列管理

**模板方法：**
- acquire()、release()：AQS实现
- tryAcquire()、tryRelease()：子类实现

**示例：**
```java
// AQS提供模板
public final void acquire(int arg) {
    if (!tryAcquire(arg))  // 调用子类实现
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg);
}

// 子类实现具体逻辑
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
```

---

### 13. AQS的state字段在不同同步器中的含义？

**答案：**

| 同步器 | state含义 | 取值范围 |
|--------|----------|---------|
| ReentrantLock | 重入次数 | 0表示未锁定，>0表示锁定次数 |
| Semaphore | 可用许可数 | 初始值为许可总数 |
| CountDownLatch | 倒计时数 | 初始值为计数总数，减到0 |
| ReentrantReadWriteLock | 高16位：读锁计数<br>低16位：写锁计数 | 32位整数拆分使用 |

---

## 六、综合题

### 14. 设计一个支持超时的连接池

**答案：**

参考实现：`scenarios/ConnectionPool.java`

**核心要点：**
1. 使用Semaphore控制并发数
2. 使用ReentrantLock + Condition管理连接
3. 支持超时获取
4. 线程安全的连接管理

---

### 15. 如何实现一个限流器？

**答案：**

参考实现：`scenarios/RateLimiter.java`

**实现方案：**
1. **令牌桶算法**
2. **基于AQS的时间控制**
3. **支持阻塞和非阻塞模式**

**关键代码：**
```java
public void acquire() {
    long now = System.nanoTime();
    long next = nextPermitTime;
    if (now >= next) {
        nextPermitTime = now + intervalNanos;
    } else {
        Thread.sleep(next - now);
    }
}
```

---

## 七、总结

### 面试准备建议

1. **理解核心原理**：state、CLH队列、独占/共享模式
2. **熟悉源码实现**：acquire/release流程
3. **掌握实际应用**：各种JUC组件的使用
4. **动手实践**：自己实现简单的同步器
5. **性能优化**：理解CAS、自旋、公平性等

### 推荐学习路径

1. 阅读本目录下的README.md
2. 运行basic/下的示例代码
3. 研究custom/下的自定义实现
4. 分析scenarios/下的实战案例
5. 阅读SourceCodeAnalysis.md深入源码
