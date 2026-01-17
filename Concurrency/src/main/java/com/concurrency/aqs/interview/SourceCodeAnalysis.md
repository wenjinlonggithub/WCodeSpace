# AQS 源码深度分析

## 一、核心字段分析

### 1.1 同步状态

```java
/**
 * 同步状态
 * - 使用volatile保证可见性
 * - 通过CAS操作保证原子性
 * - 不同同步器赋予不同语义
 */
private volatile int state;

protected final int getState() {
    return state;
}

protected final void setState(int newState) {
    state = newState;
}

protected final boolean compareAndSetState(int expect, int update) {
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

**设计要点：**
- volatile保证多线程可见性
- CAS操作保证原子性修改
- 提供protected方法供子类使用

---

### 1.2 等待队列

```java
/**
 * 等待队列的头节点（虚拟节点）
 */
private transient volatile Node head;

/**
 * 等待队列的尾节点
 */
private transient volatile Node tail;
```

**队列特点：**
- 懒初始化：首次入队时才创建
- head是虚拟节点，不存储线程
- 使用CAS操作维护队列

---

## 二、Node节点源码分析

### 2.1 Node结构

```java
static final class Node {
    // 共享模式标记
    static final Node SHARED = new Node();
    // 独占模式标记
    static final Node EXCLUSIVE = null;

    // 节点状态常量
    static final int CANCELLED =  1;  // 线程已取消
    static final int SIGNAL    = -1;  // 后继需要唤醒
    static final int CONDITION = -2;  // 在条件队列中
    static final int PROPAGATE = -3;  // 共享模式传播
    // 0: 初始状态

    volatile int waitStatus;
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
    Node nextWaiter;  // 条件队列或模式标记

    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }
}
```

**waitStatus状态转换：**
```
初始(0) → SIGNAL(-1) → 唤醒后继
       → CANCELLED(1) → 取消等待
       → CONDITION(-2) → 条件等待
       → PROPAGATE(-3) → 共享传播
```

---

## 三、独占模式源码分析

### 3.1 acquire() - 获取资源

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

**执行流程：**
1. tryAcquire()：子类实现，尝试获取
2. addWaiter()：失败则加入队列
3. acquireQueued()：自旋获取或阻塞
4. selfInterrupt()：处理中断

---

### 3.2 addWaiter() - 加入队列

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // 快速尝试
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 完整入队
    enq(node);
    return node;
}

private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        if (t == null) {
            // 初始化队列
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

**关键点：**
- 先快速尝试CAS入队
- 失败则进入自旋完整入队
- 懒初始化：首次创建虚拟head

---

### 3.3 acquireQueued() - 自旋获取

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            // 前驱是head，尝试获取
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            // 判断是否需要阻塞
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

**核心逻辑：**
1. 前驱是head才尝试获取（减少竞争）
2. 获取成功则设置自己为head
3. 失败则判断是否需要阻塞
4. 使用LockSupport.park()阻塞

---

### 3.4 shouldParkAfterFailedAcquire() - 判断是否阻塞

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        // 前驱已设置SIGNAL，可以安全阻塞
        return true;
    if (ws > 0) {
        // 前驱已取消，跳过
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        // 设置前驱为SIGNAL
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

**状态管理：**
- SIGNAL：表示后继需要唤醒
- 跳过CANCELLED节点
- 首次调用设置SIGNAL，第二次才阻塞

---

### 3.5 release() - 释放资源

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}

private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        // 从尾部向前找第一个有效节点
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

**唤醒策略：**
- 从后向前找第一个有效节点
- 使用LockSupport.unpark()唤醒
- 为什么从后向前？因为入队时先设置prev

---

## 四、共享模式源码分析

### 4.1 acquireShared() - 共享获取

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}

private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null;
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

**与独占模式区别：**
- 返回值是int（剩余资源数）
- 成功后调用setHeadAndPropagate()传播

---

### 4.2 setHeadAndPropagate() - 传播唤醒

```java
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head;
    setHead(node);

    // 传播唤醒后继节点
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```

**传播机制：**
- 共享模式下，唤醒会传播
- 确保所有可获取资源的线程被唤醒

---

### 4.3 releaseShared() - 共享释放

```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}

private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;
                unparkSuccessor(h);
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;
        }
        if (h == head)
            break;
    }
}
```

**关键点：**
- 使用PROPAGATE状态传播唤醒
- 循环确保所有节点被处理

---

## 五、Condition实现分析

### 5.1 ConditionObject结构

```java
public class ConditionObject implements Condition {
    private transient Node firstWaiter;
    private transient Node lastWaiter;

    public final void await() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        Node node = addConditionWaiter();
        int savedState = fullyRelease(node);
        int interruptMode = 0;
        while (!isOnSyncQueue(node)) {
            LockSupport.park(this);
            if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                break;
        }
        if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
            interruptMode = REINTERRUPT;
        if (node.nextWaiter != null)
            unlinkCancelledWaiters();
        if (interruptMode != 0)
            reportInterruptAfterWait(interruptMode);
    }

    public final void signal() {
        if (!isHeldExclusively())
            throw new IllegalMonitorStateException();
        Node first = firstWaiter;
        if (first != null)
            doSignal(first);
    }
}
```

**条件队列特点：**
- 单向链表（通过nextWaiter连接）
- await()释放锁并加入条件队列
- signal()将节点移到同步队列

---

## 六、关键技术点

### 6.1 CAS操作

```java
// Unsafe实例
private static final Unsafe unsafe = Unsafe.getUnsafe();
private static final long stateOffset;
private static final long headOffset;
private static final long tailOffset;

static {
    try {
        stateOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
        headOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
        tailOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
    } catch (Exception ex) { throw new Error(ex); }
}

private final boolean compareAndSetHead(Node update) {
    return unsafe.compareAndSwapObject(this, headOffset, null, update);
}
```

**CAS优势：**
- 无锁算法，避免阻塞
- 减少上下文切换
- 提高并发性能

---

### 6.2 LockSupport

```java
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```

**LockSupport特点：**
- 精确的线程阻塞/唤醒
- 不需要获取监视器锁
- 支持许可机制（permit）

---

## 七、性能优化技巧

### 7.1 自旋优化

```java
// 前驱是head才尝试获取
if (p == head && tryAcquire(arg)) {
    setHead(node);
    return;
}
```

**优化点：**
- 减少不必要的竞争
- 只有队首节点才尝试获取

---

### 7.2 快速路径

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    Node pred = tail;
    if (pred != null) {
        // 快速路径：直接CAS
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 慢速路径：完整入队
    enq(node);
    return node;
}
```

**设计思想：**
- 常见情况快速处理
- 特殊情况完整处理

---

## 八、常见问题

### 8.1 为什么从后向前遍历？

```java
for (Node t = tail; t != null && t != node; t = t.prev)
```

**原因：**
- 入队时先设置prev，再CAS设置tail，最后设置next
- 从后向前能保证遍历到所有节点
- 从前向后可能遗漏刚入队的节点

---

### 8.2 为什么需要虚拟head节点？

**原因：**
1. 简化边界条件处理
2. 统一节点操作逻辑
3. head.waitStatus存储后继状态

---

### 8.3 state为什么用volatile？

**原因：**
1. 保证多线程可见性
2. 禁止指令重排序
3. 配合CAS实现无锁同步

---

## 九、学习建议

1. **阅读顺序**
   - 先理解Node和state
   - 再看独占模式（acquire/release）
   - 最后看共享模式和Condition

2. **调试技巧**
   - 使用断点跟踪执行流程
   - 观察队列状态变化
   - 分析waitStatus转换

3. **实践建议**
   - 自己实现简单同步器
   - 对比JUC组件源码
   - 分析性能差异

---

## 十、参考资料

- Doug Lea的论文：《The java.util.concurrent Synchronizer Framework》
- JDK源码：java.util.concurrent.locks.AbstractQueuedSynchronizer
- 本项目示例代码：basic/、custom/、scenarios/
