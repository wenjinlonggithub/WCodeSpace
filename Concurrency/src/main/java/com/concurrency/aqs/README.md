# AQS (AbstractQueuedSynchronizer) 核心知识体系

## 一、AQS 核心原理

### 1.1 什么是AQS？

AbstractQueuedSynchronizer（抽象队列同步器，简称AQS）是Java并发包（java.util.concurrent）的核心基础组件，由Doug Lea设计实现。它为实现依赖于先进先出（FIFO）等待队列的阻塞锁和相关同步器（信号量、事件等）提供了一个框架。

### 1.2 核心设计思想

AQS使用一个int类型的成员变量`state`来表示同步状态，通过内置的FIFO队列来完成资源获取线程的排队工作。

**核心要素：**
- **同步状态（state）**：使用volatile int变量表示，通过CAS操作保证原子性
- **等待队列（CLH队列）**：双向链表实现的FIFO队列，用于存储等待线程
- **独占模式（Exclusive）**：同一时刻只有一个线程能获取资源（如ReentrantLock）
- **共享模式（Shared）**：同一时刻可以有多个线程获取资源（如Semaphore、CountDownLatch）

### 1.3 核心数据结构

```java
// AQS核心字段
private volatile int state;  // 同步状态
private transient volatile Node head;  // 队列头节点
private transient volatile Node tail;  // 队列尾节点

// Node节点结构
static final class Node {
    volatile int waitStatus;  // 等待状态
    volatile Node prev;       // 前驱节点
    volatile Node next;       // 后继节点
    volatile Thread thread;   // 等待的线程
    Node nextWaiter;         // 等待队列中的下一个节点
}
```

### 1.4 核心方法

**需要子类实现的方法（模板方法模式）：**
- `tryAcquire(int)`：独占方式获取资源
- `tryRelease(int)`：独占方式释放资源
- `tryAcquireShared(int)`：共享方式获取资源
- `tryReleaseShared(int)`：共享方式释放资源
- `isHeldExclusively()`：判断是否被当前线程独占

**AQS提供的模板方法：**
- `acquire(int)`：独占模式获取资源
- `acquireInterruptibly(int)`：可中断的独占模式获取
- `release(int)`：独占模式释放资源
- `acquireShared(int)`：共享模式获取资源
- `releaseShared(int)`：共享模式释放资源

## 二、AQS工作流程

### 2.1 独占模式获取流程

```
1. 调用tryAcquire()尝试获取资源
   ├─ 成功：直接返回
   └─ 失败：进入步骤2

2. 将当前线程封装成Node节点加入等待队列
   └─ 使用CAS操作将节点添加到队列尾部

3. 自旋尝试获取资源
   ├─ 如果前驱是head节点，再次尝试获取
   │  └─ 成功：将当前节点设为head，返回
   └─ 失败：进入步骤4

4. 阻塞当前线程
   └─ 使用LockSupport.park()挂起线程
```

### 2.2 独占模式释放流程

```
1. 调用tryRelease()释放资源
   └─ 失败：返回false

2. 唤醒后继节点
   └─ 使用LockSupport.unpark()唤醒线程
```

### 2.3 共享模式特点

- 多个线程可以同时获取资源
- 释放资源时会传播唤醒后续节点
- 典型应用：Semaphore、CountDownLatch、ReadWriteLock的读锁

## 三、CLH队列详解

### 3.1 CLH队列特点

CLH（Craig, Landin, and Hagersten）队列是一个虚拟的双向队列：
- 不存在队列实例，仅存在节点之间的关联关系
- 每个节点通过prev和next指针连接
- head节点不存储线程信息，是一个虚拟节点

### 3.2 节点状态（waitStatus）

```java
static final int CANCELLED =  1;  // 节点已取消
static final int SIGNAL    = -1;  // 后继节点需要被唤醒
static final int CONDITION = -2;  // 节点在条件队列中等待
static final int PROPAGATE = -3;  // 共享模式下，释放操作需要传播
// 0: 初始状态
```

## 四、AQS的优势

1. **高性能**：使用CAS操作避免重量级锁，减少上下文切换
2. **灵活性**：提供模板方法，子类只需实现特定方法
3. **可扩展**：支持独占和共享两种模式
4. **公平性可选**：可以实现公平锁和非公平锁
5. **条件队列**：支持Condition机制，实现精确唤醒

## 五、基于AQS的JUC组件

| 组件 | 模式 | 说明 |
|------|------|------|
| ReentrantLock | 独占 | 可重入的互斥锁 |
| ReentrantReadWriteLock | 共享+独占 | 读写锁，读共享写独占 |
| Semaphore | 共享 | 信号量，控制并发数 |
| CountDownLatch | 共享 | 倒计时门闩 |
| CyclicBarrier | - | 循环栅栏（基于ReentrantLock） |
| ThreadPoolExecutor | - | 线程池（使用AQS实现Worker） |

## 六、目录结构

```
aqs/
├── README.md                    # 本文档
├── basic/                       # 基础使用示例
│   ├── AQSBasicDemo.java       # AQS基本概念演示
│   ├── ReentrantLockDemo.java  # ReentrantLock使用示例
│   └── SemaphoreDemo.java      # Semaphore使用示例
├── custom/                      # 自定义实现
│   ├── CustomLock.java         # 自定义互斥锁
│   ├── CustomSharedLock.java   # 自定义共享锁
│   └── CustomCountDownLatch.java # 自定义倒计时器
├── scenarios/                   # 应用场景
│   ├── ConnectionPool.java     # 连接池实现
│   ├── RateLimiter.java        # 限流器实现
│   └── TaskCoordinator.java    # 任务协调器
└── interview/                   # 面试相关
    ├── InterviewQuestions.md   # 面试题汇总
    └── SourceCodeAnalysis.md   # 源码分析
```

## 七、学习路径

1. **理解核心概念**：state、CLH队列、独占/共享模式
2. **学习基础使用**：ReentrantLock、Semaphore等JUC组件
3. **自定义实现**：实现简单的同步器，理解模板方法模式
4. **源码分析**：深入研究AQS源码实现细节
5. **实战应用**：在实际项目中应用AQS解决并发问题

---

**下一步**：查看 `basic/` 目录下的示例代码，开始实践学习。
