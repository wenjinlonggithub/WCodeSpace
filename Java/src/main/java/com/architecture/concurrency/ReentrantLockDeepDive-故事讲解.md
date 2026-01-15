# ReentrantLock 可重入锁 - 故事讲解

> 通过生活化的故事，理解 ReentrantLock 的核心原理
>
> 代码文件：`ReentrantLockDeepDive.java`

---

## 目录

- [故事一：可重入性 - "钥匙记忆功能"](#故事一可重入性---钥匙记忆功能)
- [故事二：公平锁 vs 非公平锁 - "银行排队规则"](#故事二公平锁-vs-非公平锁---银行排队规则)
- [故事三：tryLock() - "试试门能不能开"](#故事三trylock---试试门能不能开)
- [故事四：Condition - "等座位的餐厅"](#故事四condition---等座位的餐厅)
- [故事五：转账避免死锁 - "两个人互相等待"](#故事五转账避免死锁---两个人互相等待)
- [核心知识点总结](#核心知识点总结)

---

## 故事一：可重入性 - "钥匙记忆功能"

### 生活场景

想象你家有一个**智能门锁**，这个门锁有"记忆功能"：

#### 普通门锁的问题
- 你进入客厅后，手上的钥匙就"失效"了
- 如果想从客厅进入卧室，需要把客厅的钥匙交出来，重新拿卧室的钥匙
- 非常麻烦，而且容易出错

#### 可重入锁的优势
- 你手上的钥匙有**记忆功能**
- 记住"你已经用过一次了"（计数器 = 1）
- 当你要进入卧室时，**同一把钥匙依然有效**（计数器 = 2）
- 出来时要"关门"两次（unlock 两次），计数器归零后锁才真正释放

### 代码对应

在 `deposit()` 方法（存款）中：

```java
public void deposit(double amount) {
    lock.lock();  // 🔑 第1次获取锁，holdCount = 1
    try {
        System.out.println("当前锁持有计数: " + lock.getHoldCount()); // 输出：1

        // 📞 调用 getBalance()，内部又需要获取锁
        double oldBalance = getBalance(); // 🔑 第2次获取锁（可重入！），holdCount = 2

        System.out.println("getBalance()后锁持有计数: " + lock.getHoldCount()); // 输出：2

        balance += amount;
    } finally {
        lock.unlock(); // 🔓 释放一次，holdCount = 1
        System.out.println("unlock后锁持有计数: " + lock.getHoldCount()); // 输出：0
    }
}

public double getBalance() {
    lock.lock(); // 🔑 如果是同一线程，可以再次获取
    try {
        return balance;
    } finally {
        lock.unlock(); // 🔓 对应的释放
    }
}
```

### 关键点

- **同一线程**可以多次获取同一把锁
- 每次 `lock()` 增加计数器（holdCount）
- 每次 `unlock()` 减少计数器
- 计数器归零时，锁才真正释放

### 为什么需要可重入性？

如果锁不可重入，`deposit()` 调用 `getBalance()` 时会**死锁**：
- `deposit()` 持有锁
- `getBalance()` 尝试获取同一把锁
- 但锁被自己持有，永远等待 → 死锁！

---

## 故事二：公平锁 vs 非公平锁 - "银行排队规则"

### 非公平锁（默认模式）

想象一个**允许插队**的银行柜台：

#### 运作方式
- 客户A正在办理业务
- 客户B、C、D在排队等待
- 当A办完业务时，**谁先冲到柜台谁就能办理**
- 可能客户D刚好站得近，直接"插队"成功

#### 优缺点
- ✅ **优点：效率高**
  - 没有空档期，柜台利用率高
  - 刚释放锁的线程可能再次获得锁（缓存热度高）
- ❌ **缺点：可能饥饿**
  - 某些线程可能永远排不到（一直被插队）

### 公平锁

想象一个有**严格叫号机**的银行柜台：

#### 运作方式
- 每个人取号：B-001、C-002、D-003
- 柜台严格按照号码顺序叫号
- 即使D站在柜台旁边，也得等叫到D-003才能办理

#### 优缺点
- ✅ **优点：绝对公平**
  - 每个人都能办到业务，不会饥饿
  - 先来先得，符合直觉
- ❌ **缺点：效率低**
  - 可能有空档期（叫号时间）
  - 线程切换开销大

### 代码对应

```java
// 非公平锁（默认）
ReentrantLock unfairLock = new ReentrantLock(false);

// 公平锁
ReentrantLock fairLock = new ReentrantLock(true);
```

### 性能对比

代码中的测试结果（测试6）：

```
非公平锁耗时: 1200ms
公平锁耗时:   1550ms
性能差异:     29.17%
```

**结论：非公平锁通常比公平锁快 20-30%**

### 如何选择？

| 场景 | 推荐 | 原因 |
|------|------|------|
| 高吞吐量系统 | 非公平锁 | 追求性能 |
| 任务耗时差异大 | 公平锁 | 避免长任务饥饿 |
| 对公平性有要求 | 公平锁 | 保证每个请求都能处理 |
| 默认情况 | 非公平锁 | JDK默认，性能更好 |

---

## 故事三：tryLock() - "试试门能不能开"

### 生活场景

#### 场景：拜访朋友

**使用 lock()（传统方式）：**
- 你按门铃
- 如果朋友不开门，你就**一直等在门口**
- 可能等1小时、2小时，甚至一整天
- 无论多久都不放弃（阻塞等待）

**使用 tryLock()（灵活方式）：**
- 你按门铃，**最多等5分钟**
- 如果5分钟后还没人开门：
  - "算了，朋友可能不在家"
  - 去干别的事（打电话、逛超市等）
- 不会浪费时间一直傻等

### 代码对应

#### 传统 lock() - 无限等待

```java
lock.lock(); // ⏳ 阻塞等待，直到获取锁为止
try {
    // 业务逻辑
} finally {
    lock.unlock();
}
```

#### tryLock() - 设置超时

```java
// 🕐 尝试在500毫秒内获取锁
if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
    try {
        System.out.println("✅ 成功获取锁，执行业务");
        // 业务逻辑
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("❌ 获取锁超时，放弃操作");
    // 做其他事情，或者稍后重试
}
```

### 实际案例（测试4）

```
【场景设置】
线程1：持有锁，睡眠2秒
线程2：尝试获取锁，只等待500ms

【执行结果】
线程1持有锁，睡眠2秒...
[尝试取款] 获取锁超时，放弃操作  ← 线程2快速失败
（2秒后）
线程1释放锁
```

### tryLock() 的三种用法

#### 1. 无参版本 - 立即返回

```java
if (lock.tryLock()) {  // 立即尝试，不等待
    try {
        // 成功获取锁
    } finally {
        lock.unlock();
    }
} else {
    // 立即返回false，不阻塞
}
```

#### 2. 带超时版本 - 限时等待

```java
if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {  // 最多等500ms
    // ...
}
```

#### 3. 组合使用 - 重试机制

```java
int retries = 3;
while (retries-- > 0) {
    if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
        try {
            // 执行业务
            return true;
        } finally {
            lock.unlock();
        }
    }
    System.out.println("获取锁失败，剩余重试次数: " + retries);
}
return false; // 重试3次后放弃
```

### 适用场景

| 场景 | 推荐方式 | 原因 |
|------|---------|------|
| Web请求处理 | tryLock | 快速失败，避免请求超时 |
| 定时任务 | tryLock | 避免任务堆积 |
| 关键业务 | lock | 必须执行完成 |
| 高并发API | tryLock | 提升响应速度 |

---

## 故事四：Condition - "等座位的餐厅"

### 生活场景

想象你去一家**热门餐厅**吃饭，但是没座位了。

#### 传统方式（轮询）- 低效

```
你: "有座位了吗？"
服务员: "没有"

（10秒后）
你: "有座位了吗？"
服务员: "没有"

（10秒后）
你: "有座位了吗？"
服务员: "没有"

（10秒后）
你: "有座位了吗？"
服务员: "有了！"
```

**问题：**
- 你一直在问，浪费精力
- 服务员一直在回答，浪费时间
- 效率极低（CPU空转）

#### 使用 Condition - 高效

```
你: "有座位吗？"
服务员: "没有，您先坐旁边沙发上等着（await），有座位我叫您（signal）"

你: 坐在沙发上刷手机（释放锁，进入等待）

（客人离开后）
服务员: "张先生，您的座位好了！（signal）"

你: 听到后站起来入座（被唤醒，重新获取锁）
```

**优势：**
- 你不用一直问，节省精力
- 服务员主动通知，高效
- 完美的协作机制

### 代码对应

#### 场景：余额不足时等待存款

```java
// 创建条件变量
private final Condition sufficientBalance = lock.newCondition();

// 取款方法 - 余额不足时等待
public boolean withdrawWithWait(double amount, long timeoutMs)
        throws InterruptedException {
    lock.lock(); // 🔒 先获取锁
    try {
        // ⏳ 循环等待，直到余额充足
        while (balance < amount) {
            System.out.println("余额不足，等待存款... 当前余额: " + balance);

            // 😴 释放锁，进入等待状态
            boolean signaled = sufficientBalance.await(timeoutMs, TimeUnit.MILLISECONDS);

            if (!signaled) {
                System.out.println("等待超时，取款失败");
                return false;
            }
            // 💡 被唤醒后，重新检查条件（while循环）
        }

        // ✅ 余额充足，执行取款
        balance -= amount;
        return true;
    } finally {
        lock.unlock(); // 🔓 释放锁
    }
}

// 存款方法 - 存款后通知等待的线程
public void deposit(double amount) {
    lock.lock();
    try {
        balance += amount;

        // 📢 通知所有等待的线程："余额充足了！"
        sufficientBalance.signalAll();
    } finally {
        lock.unlock();
    }
}
```

### 实际执行过程（测试3）

```
时间轴：0ms
├─ 取款线程启动
│  └─ 尝试取款500元，但余额只有100元
│     └─ 调用 await()，进入等待 😴
│     └─ **释放锁**（重要！让其他线程能执行）

时间轴：1000ms
├─ 存款线程启动
│  └─ 成功获取锁（因为取款线程已释放）
│     └─ 存入500元
│     └─ 调用 signalAll()，唤醒取款线程 📢
│     └─ 释放锁

时间轴：1001ms
├─ 取款线程被唤醒 ⏰
│  └─ 重新竞争锁
│     └─ 获取锁成功
│     └─ 检查条件：balance >= 500 ✅
│     └─ 执行取款
│     └─ 释放锁
```

### 关键要点

#### 1. 为什么用 while 而不是 if？

```java
// ❌ 错误写法
if (balance < amount) {
    sufficientBalance.await();
}
// 问题：被唤醒后不会再检查条件，可能余额还是不足

// ✅ 正确写法
while (balance < amount) {
    sufficientBalance.await();
}
// 被唤醒后会重新检查条件，确保条件满足
```

**原因：**
- 可能有多个线程在等待
- `signalAll()` 会唤醒所有线程
- 第一个线程取款后，余额可能又不足了
- 其他线程被唤醒后需要**重新检查**条件

#### 2. signal() vs signalAll()

```java
// signal() - 唤醒一个等待线程
sufficientBalance.signal();

// signalAll() - 唤醒所有等待线程
sufficientBalance.signalAll();
```

| 方法 | 唤醒数量 | 适用场景 |
|------|---------|---------|
| `signal()` | 1个线程 | 只需要唤醒一个（如资源只够一个用） |
| `signalAll()` | 所有线程 | 多个线程可能满足条件（如余额充足，多人可取） |

#### 3. await() 的三个动作

```java
sufficientBalance.await();
```

这一行代码做了三件事：
1. **释放锁**（让其他线程能执行）
2. **线程挂起**（进入等待队列）
3. **被唤醒后重新获取锁**（继续执行）

### Condition vs Object.wait()

| 特性 | Condition | Object.wait() |
|------|-----------|---------------|
| 锁类型 | ReentrantLock | synchronized |
| 条件变量数量 | 多个（精细控制） | 只有一个 |
| 超时等待 | `await(time, unit)` | `wait(timeout)` |
| 可中断 | `awaitUninterruptibly()` | 默认可中断 |
| 灵活性 | 高 | 低 |

### 使用场景

- **生产者-消费者模型**：队列满/空时等待
- **资源池**：资源不足时等待
- **限流器**：超过限制时等待
- **本案例**：余额不足时等待

---

## 故事五：转账避免死锁 - "两个人互相等待"

### 什么是死锁？

想象两个人在**单行道**上相遇：

```
←── 小明（开车）          （开车）小红 ──→
     |                          |
     └────── 都在等对方倒车 ──────┘
```

- 小明需要小红倒车才能过去
- 小红需要小明倒车才能过去
- 两人互相等待，**永远无法通行** → 死锁！

### 转账中的死锁场景

#### 危险的转账代码

```java
// ❌ 错误示范：可能导致死锁
public void transferUnsafe(Account from, Account to, double amount) {
    from.lock(); // 线程A：锁定账户1
    to.lock();   // 线程A：锁定账户2
    // 转账操作
    to.unlock();
    from.unlock();
}
```

#### 死锁如何发生

```
时间轴：

t1: 线程A: 从账户1转到账户2
    └─ lock(账户1) ✅

t2: 线程B: 从账户2转到账户1
    └─ lock(账户2) ✅

t3: 线程A: 尝试 lock(账户2)
    └─ ⏳ 等待（账户2被线程B持有）

t4: 线程B: 尝试 lock(账户1)
    └─ ⏳ 等待（账户1被线程A持有）

结果: 💀 死锁！
- 线程A等账户2，但账户2被线程B持有
- 线程B等账户1，但账户1被线程A持有
- 互相等待，永远无法完成
```

### 解决方案一：统一加锁顺序

#### 生活比喻：靠右行走规则

```
←── 小明（靠右）          （靠右）小红 ──→
     |                          |
     └──── 都靠右，顺利通过 ───────┘
```

- 就像交通规则"**都靠右行走**"
- 即使两个人相遇，也不会撞到一起
- 关键：**全局统一的规则**

#### 代码实现

```java
public static boolean transfer(BankAccount from, BankAccount to, double amount) {
    // 🔑 关键：按照账户ID排序，确保全局锁顺序一致
    BankAccount first, second;
    if (from.getAccountId().compareTo(to.getAccountId()) < 0) {
        first = from;   // 字典序小的先锁
        second = to;
    } else {
        first = to;
        second = from;
    }

    // 按顺序获取锁
    first.getLock().lock();   // 🔒 先锁第一个
    try {
        second.getLock().lock(); // 🔒 再锁第二个
        try {
            // ✅ 现在安全了，可以转账
            if (from.balance >= amount) {
                from.balance -= amount;
                to.balance += amount;
                return true;
            }
            return false;
        } finally {
            second.getLock().unlock(); // 🔓 先释放第二个
        }
    } finally {
        first.getLock().unlock(); // 🔓 再释放第一个
    }
}
```

#### 为什么能避免死锁？

```
场景：
- 线程A: 从账户1 → 账户2
- 线程B: 从账户2 → 账户1

使用统一顺序后：

线程A:
  from=账户1, to=账户2
  比较: "账户1" < "账户2" (字典序)
  顺序: 先锁账户1，再锁账户2

线程B:
  from=账户2, to=账户1
  比较: "账户1" < "账户2" (字典序)
  顺序: 先锁账户1，再锁账户2  ← 和线程A顺序一致！

结果: ✅ 不会死锁
- 两个线程都按相同顺序加锁
- 先到先得，另一个等待即可
```

### 解决方案二：tryLock() 超时机制

#### 生活比喻：设置等待上限

```
小明: "我最多等5分钟，等不到我就倒车"
小红: "我最多等5分钟，等不到我就倒车"

结果: 总有一个人会主动倒车，不会永远僵持
```

#### 代码实现

```java
public static boolean tryTransfer(BankAccount from, BankAccount to,
                                 double amount, long timeoutMs) {
    try {
        // 🕐 尝试获取第一把锁
        if (from.getLock().tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
            try {
                // 🕑 尝试获取第二把锁
                if (to.getLock().tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                    try {
                        // ✅ 两把锁都拿到了，执行转账
                        if (from.balance >= amount) {
                            from.balance -= amount;
                            to.balance += amount;
                            return true;
                        }
                    } finally {
                        to.getLock().unlock(); // 🔓 释放第二把锁
                    }
                }
                // ❌ 第二把锁获取失败，释放第一把锁，返回失败
            } finally {
                from.getLock().unlock(); // 🔓 释放第一把锁
            }
        }
        // ❌ 第一把锁获取失败，直接返回
        return false;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
    }
}
```

#### 优势

- 不会永久阻塞
- 获取不到锁会**快速失败**
- 可以重试或记录日志

#### 劣势

- 可能需要重试
- 在高并发下成功率可能较低

### 实际执行（测试5）

```
=== 转账开始 ===
从 A-Alice 转账到 B-Bob，金额: 300
  → 获取第一个锁: A-Alice  （字典序：A < B）
  → 获取第二个锁: B-Bob
  → 转账成功!
  → A-Alice 余额: 700
  → B-Bob 余额: 1300
  → 释放第二个锁: B-Bob
  → 释放第一个锁: A-Alice

=== 转账开始 ===
从 B-Bob 转账到 A-Alice，金额: 200
  → 获取第一个锁: A-Alice  （字典序：A < B，注意还是先锁A！）
  → 获取第二个锁: B-Bob
  → 转账成功!
  → A-Alice 余额: 900
  → B-Bob 余额: 1100
  → 释放第二个锁: B-Bob
  → 释放第一个锁: A-Alice
```

**关键观察：**
- 无论是 A→B 还是 B→A
- 都是**先锁A，再锁B**
- 全局顺序一致，不会死锁

### 死锁的四个必要条件

死锁发生需要**同时满足**四个条件：

| 条件 | 说明 | 转账场景 | 如何破解 |
|------|------|---------|---------|
| 互斥 | 资源不能共享 | 同一时刻只能一个线程持有账户锁 | 无法破解（必须互斥） |
| 持有并等待 | 持有资源的同时等待其他资源 | 持有账户1，等待账户2 | ✅ tryLock()（拿不到就释放） |
| 不可抢占 | 资源不能被强制剥夺 | 锁只能主动释放 | ✅ 设置超时自动释放 |
| 循环等待 | 形成资源等待环 | A等B，B等A | ✅ 统一加锁顺序 |

**破解任意一个条件，就能避免死锁！**

---

## 核心知识点总结

### 特性对比表

| 特性 | 生活比喻 | 代码位置 | 应用场景 |
|------|---------|---------|---------|
| **可重入性** | 钥匙有记忆功能 | `deposit()` 调用 `getBalance()` | 方法嵌套调用需要再次加锁 |
| **公平锁** | 银行叫号机 | `new ReentrantLock(true)` | 防止饥饿，保证公平性 |
| **非公平锁** | 允许插队 | `new ReentrantLock(false)` | 追求高性能，默认模式 |
| **tryLock** | 试试门能不能开 | `tryWithdraw()` | 避免无限等待，快速失败 |
| **Condition** | 餐厅等座位 | `withdrawWithWait()` | 线程间精细化通信 |
| **避免死锁** | 靠右行走规则 | `transfer()` | 多资源加锁时统一顺序 |

### 性能数据

根据测试6的结果：

```
非公平锁耗时: 1200ms
公平锁耗时:   1550ms
性能差异:     29.17%
```

**结论：**
- 非公平锁比公平锁快 **20-30%**
- 除非有特殊需求，否则使用默认的非公平锁

### 锁的选择指南

```
是否需要精细化的条件控制？
├─ 是 → ReentrantLock + Condition
│   └─ 例如：生产者消费者、资源池
└─ 否 → 是否需要tryLock或中断？
    ├─ 是 → ReentrantLock
    │   └─ 例如：Web请求处理、避免死锁
    └─ 否 → synchronized（更简单）
        └─ 例如：简单的同步块
```

### 最佳实践

#### 1. 总是使用 try-finally

```java
lock.lock();
try {
    // 业务逻辑
} finally {
    lock.unlock(); // 保证锁一定被释放
}
```

#### 2. 避免在锁内做耗时操作

```java
// ❌ 不好
lock.lock();
try {
    networkCall();    // 网络请求，可能很慢
    fileOperation();  // 文件操作，可能很慢
} finally {
    lock.unlock();
}

// ✅ 更好
Data data = prepareData(); // 准备数据（无锁）
lock.lock();
try {
    updateState(data); // 只在锁内做必要操作
} finally {
    lock.unlock();
}
```

#### 3. 使用 tryLock 设置合理超时

```java
if (lock.tryLock(3, TimeUnit.SECONDS)) {
    // 3秒是一个合理的超时时间
    // 太短：可能频繁失败
    // 太长：失去快速失败的意义
}
```

#### 4. 多资源加锁统一顺序

```java
// ✅ 始终按ID顺序加锁
if (id1.compareTo(id2) < 0) {
    lock(resource1);
    lock(resource2);
} else {
    lock(resource2);
    lock(resource1);
}
```

### 常见错误

#### 错误1：忘记释放锁

```java
lock.lock();
if (condition) {
    return; // ❌ 锁没有释放！
}
lock.unlock();
```

**正确做法：使用 try-finally**

#### 错误2：在 finally 外 unlock

```java
lock.lock();
try {
    // ...
}
lock.unlock(); // ❌ 如果try中抛异常，这行不执行
```

#### 错误3：Condition 使用 if 而不是 while

```java
if (balance < amount) {
    condition.await(); // ❌ 被唤醒后不会再检查
}
balance -= amount;
```

**正确做法：**

```java
while (balance < amount) { // ✅ 被唤醒后重新检查
    condition.await();
}
balance -= amount;
```

---

## 运行测试

### 编译运行

```bash
# 进入代码目录
cd Java/src/main/java

# 编译
javac com/architecture/concurrency/ReentrantLockDeepDive.java

# 运行
java com.architecture.concurrency.ReentrantLockDeepDive
```

### 测试覆盖

代码包含6个测试用例：

1. **测试1：可重入性演示** - 观察 holdCount 变化
2. **测试2：多线程并发存取款** - 验证线程安全
3. **测试3：Condition 条件变量** - 等待余额充足
4. **测试4：tryLock 避免阻塞** - 超时机制
5. **测试5：转账避免死锁** - 统一加锁顺序
6. **测试6：公平锁 vs 非公平锁** - 性能对比

### 预期输出

运行后会看到详细的日志输出，包括：
- 锁持有计数（holdCount）
- 线程操作顺序
- 锁获取和释放时机
- 性能对比数据

---

## 延伸阅读

### 相关类

- `java.util.concurrent.locks.Lock` - 锁接口
- `java.util.concurrent.locks.ReadWriteLock` - 读写锁
- `java.util.concurrent.locks.StampedLock` - 乐观读锁（Java 8+）

### 适用场景对比

| 锁类型 | 适用场景 | 优点 | 缺点 |
|--------|---------|------|------|
| synchronized | 简单同步 | 简单，JVM优化好 | 功能有限 |
| ReentrantLock | 需要tryLock/中断 | 功能丰富 | 代码复杂 |
| ReadWriteLock | 读多写少 | 读并发高 | 写性能低 |
| StampedLock | 读极多写极少 | 乐观读无锁 | API复杂 |

### 进阶主题

- AQS（AbstractQueuedSynchronizer）原理
- 锁升级/降级
- 锁消除与锁粗化
- Lock-Free 算法

---

## 总结

ReentrantLock 是 Java 并发编程的重要工具，通过本文的故事化讲解：

1. ✅ 理解了**可重入性**的原理和必要性
2. ✅ 掌握了**公平锁和非公平锁**的区别
3. ✅ 学会了使用 **tryLock** 避免阻塞
4. ✅ 理解了 **Condition** 的精细化控制
5. ✅ 掌握了**避免死锁**的实用方法

记住：**锁是工具，场景是关键**。选择合适的锁机制，写出高效且安全的并发代码！

---

> 文档版本：v1.0
> 最后更新：2026-01-15
> 作者：Claude Code
> 代码文件：`ReentrantLockDeepDive.java`
