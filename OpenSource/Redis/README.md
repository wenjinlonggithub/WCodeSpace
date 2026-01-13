# Redis深度学习项目

这是一个全面的Redis学习项目，涵盖Redis的核心原理、面试题、业务场景实现、核心类解析和源码分析。

## 项目结构

```
OpenSource/Redis
├── src/main/java/com/architecture
│   ├── principle/          # 原理实现
│   │   ├── SkipList.java                   # 跳表实现
│   │   ├── SimpleDynamicString.java        # SDS简单动态字符串
│   │   ├── PersistenceRDB.java             # RDB持久化
│   │   └── PersistenceAOF.java             # AOF持久化
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

### 1. 跳表 (SkipList)
- Redis ZSet底层数据结构之一
- 时间复杂度：O(logN)
- 支持快速查找、插入、删除

**运行示例：**
```java
SkipList<String> skipList = new SkipList<>();
skipList.insert("user1", 100.0);
skipList.insert("user2", 200.0);
skipList.print();
```

### 2. 简单动态字符串 (SDS)
- Redis String的底层实现
- 特点：
  - O(1)时间获取长度
  - 空间预分配
  - 惰性空间释放
  - 二进制安全

**运行示例：**
```java
SimpleDynamicString sds = new SimpleDynamicString("Hello");
sds.append(" World");
System.out.println(sds.getMemoryInfo());
```

### 3. RDB持久化
- 全量快照
- SAVE（同步）和BGSAVE（异步）
- 恢复速度快

**运行示例：**
```java
PersistenceRDB rdb = new PersistenceRDB("dump.rdb");
rdb.set("key1", "value1");
rdb.bgsave();
```

### 4. AOF持久化
- 日志追加
- 三种同步策略：always/everysec/no
- 支持AOF重写

**运行示例：**
```java
PersistenceAOF aof = new PersistenceAOF("appendonly.aof", SyncPolicy.EVERYSEC);
aof.set("key1", "value1");
```

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

### 1. 分布式锁 (DistributedLock)
- SET NX EX实现
- Lua脚本保证原子性
- 自动续期（看门狗）

**使用场景：**
- 防止库存超卖
- 避免重复提交
- 定时任务防重

**运行示例：**
```java
DistributedLock lock = new DistributedLock(jedis, "order:123", 10);
if (lock.tryLock()) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}
```

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
