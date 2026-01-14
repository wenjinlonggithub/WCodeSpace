# Redis深度学习项目

这是一个全面的Redis学习项目，涵盖Redis的核心原理、面试题、业务场景实现、核心类解析和源码分析。

## 项目结构

```
OpenSource/Redis
├── src/main/java/com/architecture
│   ├── principle/          # 原理实现
│   │   ├── skiplist/       # 跳表
│   │   │   ├── SkipList.java
│   │   │   ├── SkipList_Interactive.html（可视化动画）
│   │   │   └── README.md
│   │   ├── replication/    # 主从复制
│   │   │   ├── SimpleReplicationDemo.java（简化版）
│   │   │   ├── RedisReplication.java（完整版）
│   │   │   └── README.md
│   │   ├── persistence/    # 持久化
│   │   │   ├── PersistenceRDB.java
│   │   │   ├── PersistenceAOF.java
│   │   │   └── README.md
│   │   ├── sds/           # 简单动态字符串
│   │   │   ├── SimpleDynamicString.java
│   │   │   └── README.md
│   │   └── README.md      # 原理模块总导航
│   │
│   ├── interview/          # 面试题
│   │   └── RedisInterviewQuestions.java    # Redis面试题集锦
│   │
│   ├── scenario/           # 业务场景
│   │   ├── DistributedLock.java            # 分布式锁
│   │   ├── RateLimiter.java                # 限流器
│   │   ├── Leaderboard.java                # 排行榜
│   │   └── CachePattern.java               # 缓存模式
│   │
│   ├── core/               # 核心类解析
│   │   ├── RedisObjectAnalysis.java        # RedisObject分析
│   │   └── DictAnalysis.java               # 字典实现分析
│   │
│   └── source/             # 源码分析
│       ├── EventLoopAnalysis.java          # 事件循环分析
│       └── CommandExecutionAnalysis.java   # 命令执行流程
│
└── pom.xml
```

## 一、原理实现 (principle)

**📂 已按功能分类整理，详见：** [principle/README.md](src/main/java/com/architecture/principle/README.md)

```
principle/
├── skiplist/      # 跳表（含可视化动画）
├── replication/   # 主从复制（双版本实现）
├── persistence/   # 持久化（RDB + AOF）
└── sds/          # 简单动态字符串
```

### 1. 跳表 (SkipList) 🎯

**位置：** `principle/skiplist/`

- Redis ZSet底层数据结构之一
- 时间复杂度：O(logN)
- 支持快速查找、插入、删除

**特色：**
- ⭐ 交互式动画演示（`SkipList_Interactive.html`）
- ⭐ 大白话讲解（`SkipList_SimpleExplanation.md`）
- ⭐ 3个业务场景（游戏排行榜、延迟队列、竞价系统）

**快速开始：**
```bash
# 观看动画（推荐！）
打开: src/main/java/com/architecture/principle/skiplist/SkipList_Interactive.html

# 或运行代码
javac src/main/java/com/architecture/principle/skiplist/SkipList.java
java -cp target/classes com.architecture.principle.skiplist.SkipList
```

**详细文档：** [skiplist/README.md](src/main/java/com/architecture/principle/skiplist/README.md)

---

### 2. 简单动态字符串 (SDS) 📝

**位置：** `principle/sds/`

- Redis String的底层实现
- 核心优势：
  - ✅ O(1)时间获取长度
  - ✅ 空间预分配（减少内存分配）
  - ✅ 惰性空间释放
  - ✅ 二进制安全

**运行示例：**
```bash
javac src/main/java/com/architecture/principle/sds/SimpleDynamicString.java
java -cp target/classes com.architecture.principle.sds.SimpleDynamicString
```

**详细文档：** [sds/README.md](src/main/java/com/architecture/principle/sds/README.md)

---

### 3. 持久化 (Persistence) 💾

**位置：** `principle/persistence/`

#### RDB（快照）
- 全量快照，文件小，恢复快
- SAVE（同步）和BGSAVE（异步）

#### AOF（日志）
- 增量日志，数据更安全
- 三种同步策略：ALWAYS / EVERYSEC / NO

**运行示例：**
```bash
# RDB
javac src/main/java/com/architecture/principle/persistence/PersistenceRDB.java
java -cp target/classes com.architecture.principle.persistence.PersistenceRDB

# AOF
javac src/main/java/com/architecture/principle/persistence/PersistenceAOF.java
java -cp target/classes com.architecture.principle.persistence.PersistenceAOF
```

**详细文档：** [persistence/README.md](src/main/java/com/architecture/principle/persistence/README.md)

---

### 4. 主从复制 (Replication) 🔄

**位置：** `principle/replication/`

提供两个版本的实现，适合不同学习阶段：

#### 🌟 简化版 (SimpleReplicationDemo) - 推荐新手
- 300行精简代码，核心概念清晰
- 无网络通信复杂度，快速理解原理

**核心特性：**
- ✅ Replication ID、Offset、Backlog
- ✅ PSYNC协议（简化版）
- ✅ 全量复制 + 增量复制
- ✅ 命令传播

#### 🚀 完整版 (RedisReplication) - 进阶学习
- 900行完整实现，真实TCP Socket通信
- RDB文件生成和传输，环形缓冲区

**运行示例：**
```bash
# 简化版（推荐新手）
./run_simple_replication.bat

# 完整版（进阶学习）
./run_full_replication.bat
```

**核心概念：**
- **Replication ID** - 标识数据集版本
- **Replication Offset** - 复制进度（字节数）
- **Replication Backlog** - 环形缓冲区，支持增量复制
- **全量复制** - 传输所有数据（RDB）
- **增量复制** - 只传输缺失命令

**详细文档：** [replication/README.md](src/main/java/com/architecture/principle/replication/README.md)

**快速参考：** [replication/QUICK_REFERENCE.md](src/main/java/com/architecture/principle/replication/QUICK_REFERENCE.md)

## 二、面试题 (interview)

### RedisInterviewQuestions
包含10大核心面试问题及详细解答：

1. **Redis为什么这么快？**
   - 基于内存
   - 单线程模型
   - 高效的数据结构
   - I/O多路复用

2. **数据类型及底层结构**
   - String -> SDS
   - List -> quicklist
   - Hash -> dict/ziplist
   - Set -> dict/intset
   - ZSet -> skiplist+dict

3. **缓存三大问题**
   - 缓存穿透
   - 缓存击穿
   - 缓存雪崩

4. **过期删除和内存淘汰**
   - 惰性删除 + 定期删除
   - LRU/LFU算法

5. **RDB vs AOF**
   - 持久化对比
   - 混合持久化

6. **主从复制**
   - 全量复制
   - 增量复制

7. **哨兵机制**
   - 监控、通知、故障转移

8. **集群方案**
   - Redis Cluster
   - Codis
   - Twemproxy

9. **事务**
   - MULTI/EXEC
   - 不支持回滚

10. **缓存一致性**
    - 先更新数据库，再删除缓存
    - 最终一致性方案

**运行示例：**
```bash
cd OpenSource/Redis
mvn compile
java -cp target/classes com.architecture.interview.RedisInterviewQuestions
```

## 三、业务场景 (scenario)

### 1. 分布式锁 (DistributedLock) 🔒

**位置：** `scenario/`

Redis分布式锁完整实现，包含多个版本和实战案例

#### 核心实现文件

| 文件 | 说明 | 特点 |
|------|------|------|
| **DistributedLock.java** | 基础版 | SET NX EX + Lua + 看门狗 |
| **RedLockDistributedLock.java** | Redlock版 | 高可用方案，容忍N/2-1故障 |
| **ReentrantDistributedLock.java** | 可重入版 | Hash结构，支持重入 |
| **DistributedLockCases.java** | 实战案例 | 秒杀、定时任务、幂等、缓存 |

#### 完整文档

- **📖 [DISTRIBUTED_LOCK_GUIDE.md](src/main/java/com/architecture/scenario/DISTRIBUTED_LOCK_GUIDE.md)** - 分布式锁完全指南
  - 底层原理（SET NX EX、Lua脚本、Redis内部实现）
  - 实现方案演进（V1.0 ~ V5.0）
  - 核心问题与解决方案
  - 实战案例（秒杀、定时任务、幂等等）
  - 性能优化
  - 最佳实践

- **📝 [INTERVIEW_QUESTIONS.md](src/main/java/com/architecture/scenario/INTERVIEW_QUESTIONS.md)** - 面试题完整题库
  - 基础题（⭐）
  - 中级题（⭐⭐）
  - 高级题（⭐⭐⭐）
  - 专家题（⭐⭐⭐⭐）
  - 场景设计题
  - 代码题

#### 核心特性

**1. 基础实现（DistributedLock.java）**
```java
// 加锁：SET NX EX
SetParams params = SetParams.setParams().nx().ex(10);
String result = jedis.set(lockKey, lockValue, params);

// 解锁：Lua脚本保证原子性
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
end

// 自动续期：看门狗机制
Thread watchDog = lock.startWatchDog();
```

**2. Redlock算法（RedLockDistributedLock.java）**
```
原理：
- 向N个独立Redis实例（N=5）请求加锁
- 超过半数（N/2+1=3）成功才算成功
- 考虑时钟漂移：validityTime = TTL - elapsed - clockDrift

优势：
- 容忍N/2-1个节点故障
- 解决主从切换丢锁问题
```

**3. 可重入锁（ReentrantDistributedLock.java）**
```
原理：
- 使用Hash结构：HSET lock_key {uuid:threadId} {count}
- 同一线程可多次加锁，count+1
- 解锁时count-1，count=0时删除

示例：
lock()  → count=1
lock()  → count=2（重入）
lock()  → count=3（重入）
unlock() → count=2
unlock() → count=1
unlock() → count=0（完全释放）
```

#### 实战案例

**案例1：秒杀防超卖**
```java
// 方案A：单一锁（简单但性能差）
lock("stock:" + productId) → QPS < 1000

// 方案B：分段锁（推荐，性能提升10倍）
lock("stock:" + productId + ":" + segment) → QPS > 10000

// 方案C：无锁方案（性能最优）
jedis.decr("stock:" + productId) → QPS > 50000
```

**案例2：定时任务防重**
```java
// 3台服务器，只有一个执行
@Scheduled(cron = "0 0 2 * * ?")
public void syncTask() {
    if (lock.tryLock()) {
        try {
            syncData();
        } finally {
            lock.unlock();
        }
    }
}
```

**案例3：接口幂等性**
```java
// 防止重复提交
String lockKey = "order:idempotent:" + requestId;
if (lock.tryLock()) {
    try {
        // 检查是否已处理
        if (existingOrder != null) return existingOrder;
        // 处理订单
        return processOrder(request);
    } finally {
        lock.unlock();
    }
}
```

**案例4：缓存击穿**
```java
// 热点数据过期，只有一个请求查DB
String cacheKey = "cache:user:" + userId;
if (cache.get(cacheKey) == null) {
    if (lock.tryLock()) {
        try {
            // 双重检查
            if (cache.get(cacheKey) == null) {
                User user = queryDatabase(userId);
                cache.set(cacheKey, user, 60);
            }
        } finally {
            lock.unlock();
        }
    }
}
```

#### 核心问题与解决方案

**问题1：锁过期但业务未完成**
```
解决方案：看门狗自动续期
- 后台线程每隔TTL/3续期一次
- 业务完成时停止看门狗
- Redisson内置该机制
```

**问题2：主从同步延迟导致锁丢失**
```
解决方案：
- Redlock算法（推荐）
- WAIT命令（简单场景）
- Zookeeper（强一致性）
- 业务补偿（实用）
```

**问题3：可重入性**
```
解决方案：使用Hash结构
- field = uuid:threadId
- value = 重入次数
```

#### 快速开始

```bash
# 1. 基础版演示
cd OpenSource/Redis
mvn compile
java -cp target/classes com.architecture.scenario.DistributedLock

# 2. Redlock演示
java -cp target/classes com.architecture.scenario.RedLockDistributedLock

# 3. 可重入锁演示
java -cp target/classes com.architecture.scenario.ReentrantDistributedLock

# 4. 实战案例演示
java -cp target/classes com.architecture.scenario.DistributedLockCases
```

#### 性能对比

| 方案 | QPS | 适用场景 |
|------|-----|----------|
| **单一锁** | 1,000 | 低并发 |
| **分段锁** | 10,000 | 高并发 |
| **无锁方案** | 50,000+ | 极高并发 |
| **Redlock** | 3,000 | 高可用要求 |

#### 面试高频问题

1. **Redis分布式锁如何实现？**
   - SET NX EX + Lua脚本 + 看门狗

2. **为什么要用Lua脚本解锁？**
   - 保证GET+DEL的原子性，防止误删

3. **锁过期但业务未完成怎么办？**
   - 看门狗自动续期 + UUID防误删 + 业务幂等

4. **主节点宕机导致锁丢失怎么办？**
   - Redlock算法 / WAIT命令 / 业务补偿

5. **如何实现可重入锁？**
   - Hash结构 + 重入计数

6. **Redis锁 vs Zookeeper锁？**
   - Redis: AP系统，性能高，最终一致
   - Zookeeper: CP系统，强一致，性能中等

#### 最佳实践

```java
// 1. 使用try-finally确保释放锁
if (lock.tryLock()) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}

// 2. 设置合理的TTL
int ttl = businessTime * 2;  // 业务时间的2倍

// 3. 使用看门狗（长时间任务）
Thread watchDog = lock.startWatchDog();
try {
    longRunningTask();
} finally {
    watchDog.interrupt();
    lock.unlock();
}

// 4. 业务兜底（推荐）
// 数据库唯一索引 + 乐观锁 + 定期对账
```

#### 进阶学习

- 阅读 Redisson 源码
- 理解 Redlock 争议（Martin Kleppmann vs Antirez）
- 学习 Zookeeper 分布式锁实现
- 研究 etcd 分布式锁

**使用场景：**
- 秒杀/抢购
- 库存扣减
- 定时任务防重
- 接口幂等性
- 缓存更新

**详细文档：** [DISTRIBUTED_LOCK_GUIDE.md](src/main/java/com/architecture/scenario/DISTRIBUTED_LOCK_GUIDE.md)

### 2. 限流器 (RateLimiter)
支持4种限流算法：

- **固定窗口**：简单计数
- **滑动窗口**：ZSet实现
- **令牌桶**：支持突发流量
- **漏桶**：流量平滑

**使用场景：**
- API接口限流
- 防刷单
- 并发控制

**运行示例：**
```java
RateLimiter limiter = new RateLimiter(jedis);
boolean allowed = limiter.slidingWindowLimiterLua("api:key", 100, 60);
```

### 3. 排行榜 (Leaderboard)
- 基于ZSet实现
- 支持TOP N查询
- 支持用户周围排名

**使用场景：**
- 游戏积分榜
- 热门文章排行
- 销售排行

**运行示例：**
```java
Leaderboard leaderboard = new Leaderboard(jedis, "game:score");
leaderboard.addScore("user1", 1000);
leaderboard.printTopN(10);
```

### 4. 缓存模式 (CachePattern)
实现多种缓存策略：

- **Cache-Aside**：最常用
- **缓存预热**：系统启动时加载
- **互斥锁**：防缓存击穿
- **逻辑过期**：异步更新

**使用场景：**
- 热点数据缓存
- 数据库查询优化

**运行示例：**
```java
CachePattern pattern = new CachePattern(jedis);
User user = pattern.cacheAside("user:1001", User.class, 3600,
    () -> loadFromDatabase());
```

## 四、核心类解析 (core)

### 1. RedisObject分析
- 对象类型和编码
- 引用计数
- LRU/LFU机制
- 编码转换规则

**主要内容：**
- 5种对象类型
- 11种编码方式
- 对象共享
- 内存优化

### 2. Dict分析
- 哈希表实现
- 渐进式rehash
- 哈希冲突解决
- 字典迭代器

**主要内容：**
- MurmurHash2算法
- 链地址法
- 负载因子
- 两个哈希表

## 五、源码分析 (source)

### 1. 事件循环分析 (EventLoopAnalysis)
- 文件事件：I/O多路复用
- 时间事件：serverCron
- Reactor模式
- Redis 6.0 I/O多线程

**主要内容：**
- aeEventLoop结构
- epoll/select/kqueue
- beforesleep机制
- 事件处理流程

### 2. 命令执行流程 (CommandExecutionAnalysis)
- 完整执行流程
- 命令表查找
- processCommand检查
- call函数执行
- RESP协议
- Pipeline机制

**主要内容：**
- readQueryFromClient
- processInputBuffer
- processCommand
- 输出缓冲区管理

## 技术栈

- **Java**: 8+
- **Redis客户端**: Jedis 4.3.1
- **分布式客户端**: Redisson 3.20.0
- **Spring Boot**: 2.7.10
- **JSON**: Fastjson 1.2.83
- **工具**: Lombok 1.18.26

## 快速开始

### 1. 环境要求
- JDK 8+
- Maven 3.6+
- Redis 5.0+

### 2. 安装Redis
```bash
# Linux
sudo apt-get install redis-server
sudo systemctl start redis

# macOS
brew install redis
brew services start redis

# Windows
# 下载Redis for Windows
# 启动redis-server.exe
```

### 3. 构建项目
```bash
cd OpenSource/Redis
mvn clean install
```

### 4. 运行示例

#### 运行面试题
```bash
mvn exec:java -Dexec.mainClass="com.architecture.interview.RedisInterviewQuestions"
```

#### 运行分布式锁示例
```bash
mvn exec:java -Dexec.mainClass="com.architecture.scenario.DistributedLock"
```

#### 运行排行榜示例
```bash
mvn exec:java -Dexec.mainClass="com.architecture.scenario.Leaderboard"
```

#### 运行限流器示例
```bash
mvn exec:java -Dexec.mainClass="com.architecture.scenario.RateLimiter"
```

## 学习路径建议

### 初级（1-2周）
1. 了解Redis基本数据类型
2. 学习常用命令
3. 运行面试题代码
4. 理解缓存基本概念

### 中级（2-4周）
1. 深入学习数据结构原理
2. 掌握持久化机制
3. 实践业务场景代码
4. 理解主从复制和哨兵

### 高级（4-8周）
1. 研究核心类源码
2. 理解事件循环机制
3. 掌握集群方案
4. 性能优化和调优

## 常见问题

### Q1: 如何连接到Redis？
```java
Jedis jedis = new Jedis("localhost", 6379);
// 如果有密码
jedis.auth("password");
```

### Q2: 如何处理连接池？
```java
JedisPoolConfig config = new JedisPoolConfig();
config.setMaxTotal(100);
config.setMaxIdle(50);
JedisPool pool = new JedisPool(config, "localhost", 6379);

try (Jedis jedis = pool.getResource()) {
    // 使用jedis
}
```

### Q3: 如何在Spring Boot中使用？
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
```

## 进阶资源

### 官方文档
- [Redis官方文档](https://redis.io/documentation)
- [Redis命令参考](https://redis.io/commands)

### 推荐书籍
- 《Redis设计与实现》 - 黄健宏
- 《Redis深度历险》 - 钱文品
- 《Redis实战》 - Josiah L. Carlson

### 源码阅读
- [Redis源码](https://github.com/redis/redis)
- [Jedis源码](https://github.com/redis/jedis)
- [Redisson源码](https://github.com/redisson/redisson)

## 贡献指南

欢迎贡献代码、报告问题或提出建议！

1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## License

本项目仅用于学习目的。

## 联系方式

如有问题或建议，欢迎提Issue。

---

**祝学习愉快！Happy Coding!** 🚀
