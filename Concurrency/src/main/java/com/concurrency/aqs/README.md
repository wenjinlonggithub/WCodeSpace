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

## 六、大厂应用案例

### 6.1 阿里巴巴

#### 1. Dubbo框架 - 限流器实现
- **应用场景**：服务端限流保护
- **技术方案**：使用Semaphore控制并发请求数
- **核心实现**：基于AQS的共享模式，动态调整许可数量
- **业务价值**：防止服务过载，保障系统稳定性

#### 2. Sentinel - 流量控制组件
- **应用场景**：分布式系统流量防护
- **技术方案**：自定义AQS实现滑动窗口限流
- **核心实现**：结合AQS的state状态管理和条件队列
- **业务价值**：实现QPS限流、并发线程数限流、系统自适应保护

#### 3. Nacos - 配置中心
- **应用场景**：配置变更通知机制
- **技术方案**：使用CountDownLatch协调配置加载
- **核心实现**：多个配置源并行加载，等待全部完成
- **业务价值**：确保配置一致性，提升启动速度

### 6.2 腾讯

#### 4. 微信红包系统
- **应用场景**：高并发抢红包
- **技术方案**：使用ReentrantLock保证红包分配的原子性
- **核心实现**：公平锁模式，防止饥饿问题
- **业务价值**：处理每秒数十万次抢红包请求，保证公平性

#### 5. 腾讯云API网关
- **应用场景**：API请求并发控制
- **技术方案**：基于Semaphore实现租户级别的限流
- **核心实现**：动态调整不同租户的并发配额
- **业务价值**：多租户资源隔离，防止单个租户占用过多资源

### 6.3 美团

#### 6. 分布式锁服务
- **应用场景**：订单系统防重复提交
- **技术方案**：本地使用ReentrantLock，配合Redis实现分布式锁
- **核心实现**：可重入特性，支持锁续期
- **业务价值**：防止订单重复创建，保证数据一致性

#### 7. 外卖配送系统
- **应用场景**：骑手接单并发控制
- **技术方案**：使用ReadWriteLock实现订单池的读写分离
- **核心实现**：多个骑手并发读取订单，抢单时独占写入
- **业务价值**：提升订单分配效率，降低锁竞争

### 6.4 京东

#### 8. 秒杀系统
- **应用场景**：商品库存扣减
- **技术方案**：使用Semaphore控制库存数量
- **核心实现**：许可数量代表库存，acquire代表扣减库存
- **业务价值**：处理百万级并发秒杀请求，防止超卖

#### 9. 物流系统
- **应用场景**：包裹分拣任务协调
- **技术方案**：使用CyclicBarrier协调多个分拣线程
- **核心实现**：等待所有分拣任务完成后统一发车
- **业务价值**：提高物流效率，优化资源利用

### 6.5 字节跳动

#### 10. 抖音推荐系统
- **应用场景**：多路召回结果聚合
- **技术方案**：使用CountDownLatch等待多个召回服务返回
- **核心实现**：并行调用多个推荐算法，设置超时时间
- **业务价值**：降低推荐延迟，提升用户体验

#### 11. 飞书文档协同编辑
- **应用场景**：文档并发编辑冲突控制
- **技术方案**：使用ReadWriteLock实现读写分离
- **核心实现**：多人同时查看（读锁），单人编辑（写锁）
- **业务价值**：支持多人协同，保证数据一致性

### 6.6 滴滴

#### 12. 订单派单系统
- **应用场景**：司机抢单并发控制
- **技术方案**：使用ReentrantLock保证订单分配的原子性
- **核心实现**：非公平锁模式，优先响应速度快的司机
- **业务价值**：提升派单效率，减少乘客等待时间

### 6.7 百度

#### 13. 搜索引擎查询系统
- **应用场景**：查询请求并发控制
- **技术方案**：使用Semaphore限制并发查询数
- **核心实现**：根据系统负载动态调整许可数量
- **业务价值**：保护后端存储，防止查询风暴

### 6.8 网易

#### 14. 游戏服务器连接池
- **应用场景**：数据库连接管理
- **技术方案**：使用Semaphore控制连接数量
- **核心实现**：acquire获取连接，release归还连接
- **业务价值**：复用连接，降低资源消耗

### 6.9 蚂蚁金服

#### 15. 支付系统
- **应用场景**：支付请求串行化处理
- **技术方案**：使用ReentrantLock保证同一账户的支付操作串行
- **核心实现**：按账户ID加锁，支持可重入
- **业务价值**：防止余额并发扣减导致的数据不一致

### 6.10 拼多多

#### 16. 拼团系统
- **应用场景**：拼团人数控制
- **技术方案**：使用CountDownLatch等待拼团人数达标
- **核心实现**：每个用户参团时countDown，人数满后触发成团
- **业务价值**：精确控制拼团逻辑，提升用户体验

### 6.11 携程

#### 17. 酒店库存管理
- **应用场景**：房间库存并发扣减
- **技术方案**：使用Semaphore表示可用房间数
- **核心实现**：预订时acquire，取消时release
- **业务价值**：防止超订，保证库存准确性

### 6.12 小米

#### 18. 小米商城抢购系统
- **应用场景**：限量商品抢购
- **技术方案**：使用Semaphore + ReentrantLock组合
- **核心实现**：Semaphore控制库存，ReentrantLock保证订单创建原子性
- **业务价值**：支持千万级用户同时抢购，系统稳定可靠

## 七、目录结构

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
│   ├── ConnectionPool.java          # 连接池实现
│   ├── RateLimiter.java             # 限流器实现
│   ├── TaskCoordinator.java         # 任务协调器
│   ├── CacheWithReadWriteLock.java  # 读写分离缓存
│   ├── CircuitBreaker.java          # 熔断器实现
│   ├── DownloadManager.java         # 下载管理器
│   ├── ParkingLotSystem.java        # 停车场管理系统
│   └── BatchProcessor.java          # 批处理器
├── enterprise/                  # 大厂业务案例（带完整代码实现）
│   ├── DubboRateLimiter.java        # 阿里Dubbo限流器
│   ├── WeChatRedPacket.java         # 腾讯微信红包系统
│   ├── JDSeckillSystem.java         # 京东秒杀系统
│   ├── MeituanOrderPool.java        # 美团外卖订单池
│   ├── DouyinRecommendSystem.java   # 字节抖音推荐系统
│   ├── AlipayPaymentSystem.java     # 蚂蚁支付系统
│   ├── PinduoduoGroupBuying.java    # 拼多多拼团系统
│   └── CtripHotelInventory.java     # 携程酒店库存管理
└── interview/                   # 面试相关
    ├── InterviewQuestions.md   # 面试题汇总
    └── SourceCodeAnalysis.md   # 源码分析
```

## 八、大厂业务案例代码说明

`enterprise/` 目录包含8个真实的大厂业务场景代码实现，每个案例都包含：

### 8.1 案例特点
- **完整可运行**：每个类都有main方法，可以直接运行查看效果
- **详细注释**：包含业务场景、技术方案、业务价值说明
- **真实场景**：基于大厂实际业务场景设计
- **最佳实践**：展示AQS在生产环境的正确使用方式

### 8.2 案例列表

| 文件 | 公司 | 场景 | 核心技术 | 难度 |
|------|------|------|----------|------|
| DubboRateLimiter.java | 阿里 | 服务限流 | Semaphore | ⭐⭐ |
| WeChatRedPacket.java | 腾讯 | 红包系统 | ReentrantLock(公平锁) | ⭐⭐⭐ |
| JDSeckillSystem.java | 京东 | 秒杀系统 | Semaphore + 超时机制 | ⭐⭐⭐ |
| MeituanOrderPool.java | 美团 | 订单池 | ReadWriteLock | ⭐⭐⭐ |
| DouyinRecommendSystem.java | 字节 | 推荐系统 | CountDownLatch + 线程池 | ⭐⭐⭐⭐ |
| AlipayPaymentSystem.java | 蚂蚁 | 支付系统 | ReentrantLock + 分段锁 | ⭐⭐⭐⭐ |
| PinduoduoGroupBuying.java | 拼多多 | 拼团系统 | CountDownLatch + 超时 | ⭐⭐⭐ |
| CtripHotelInventory.java | 携程 | 库存管理 | Semaphore + 批量操作 | ⭐⭐⭐ |

### 8.3 运行示例

```bash
# 运行微信红包案例
cd Concurrency/src/main/java
javac com/concurrency/aqs/enterprise/WeChatRedPacket.java
java com.concurrency.aqs.enterprise.WeChatRedPacket

# 运行京东秒杀案例
javac com/concurrency/aqs/enterprise/JDSeckillSystem.java
java com.concurrency.aqs.enterprise.JDSeckillSystem
```

### 8.4 学习建议

1. **按难度学习**：从⭐⭐开始，逐步提升到⭐⭐⭐⭐
2. **对比学习**：对比不同场景下AQS组件的选择
3. **修改实验**：尝试修改参数，观察不同效果
4. **扩展实现**：基于案例扩展更多功能

## 九、学习路径

1. **理解核心概念**：state、CLH队列、独占/共享模式
2. **学习基础使用**：ReentrantLock、Semaphore等JUC组件
3. **自定义实现**：实现简单的同步器，理解模板方法模式
4. **大厂案例学习**：运行和分析 `enterprise/` 目录下的8个案例
5. **源码分析**：深入研究AQS源码实现细节
6. **实战应用**：在实际项目中应用AQS解决并发问题

---

**下一步**：
- 初学者：查看 `basic/` 目录下的示例代码
- 进阶者：运行 `enterprise/` 目录下的大厂案例
- 面试准备：查看第六章的18个大厂应用案例
