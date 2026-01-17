# 大厂面试 - 业务场景疑难杂症解决方案

本项目整理了大厂面试中常见的业务场景疑难杂症及其技术实现方案，涵盖高并发、分布式系统、缓存、队列、限流等核心技术领域。

每个场景都包含：
- 业务背景与问题描述
- 核心瓶颈分析
- 多种技术方案对比
- 完整的代码实现
- 实际应用案例

## 项目结构

```
BusinesScenario/
├── src/main/java/com/architecture/
│   ├── seckill/          # 场景一：高并发秒杀系统
│   ├── distributedlock/  # 场景二：分布式锁
│   ├── delayqueue/       # 场景三：延迟队列
│   ├── ratelimit/        # 场景四：限流算法
│   ├── idempotent/       # 场景五：接口幂等性
│   ├── hotdata/          # 场景六：热点数据处理
│   ├── distributedid/    # 场景七：分布式ID生成
│   ├── apppush/          # 场景八：APP在线状态检测与智能推送
│   └── esignature/       # 场景九：电子签名回调重复日志问题
└── pom.xml
```

## 场景列表

### 场景一：高并发秒杀系统

**业务背景**：电商大促期间，限量商品秒杀，百万级QPS

**核心问题**：
- 数据库压力巨大
- 超卖问题
- 恶意刷单

**技术方案**：
- Redis预减库存（原子操作）
- 消息队列异步削峰
- 数据库乐观锁防超卖
- 多级缓存 + 限流

**核心文件**：
- `seckill/SeckillService.java` - 秒杀核心逻辑
- `seckill/RedisStockService.java` - Redis库存管理
- `seckill/OrderQueueService.java` - 订单队列处理

**性能指标**：QPS 10万+，零超卖

---

### 场景二：分布式锁

**业务背景**：分布式系统中多个实例需要互斥访问共享资源

**核心问题**：
- 单机锁失效
- 死锁风险
- 锁过期问题
- 锁误删问题

**技术方案**：
- **Redis方案**：SET NX EX + Lua脚本 + Redisson看门狗
- **Zookeeper方案**：临时顺序节点 + 监听机制

**核心文件**：
- `distributedlock/RedisDistributedLock.java` - Redis分布式锁
- `distributedlock/RedissonLockExample.java` - Redisson实现（推荐）
- `distributedlock/ZookeeperDistributedLock.java` - Zookeeper实现

**推荐方案**：
- 通用场景：Redisson（简单高效）
- 强一致性：Zookeeper

---

### 场景三：延迟队列

**业务背景**：订单30分钟未支付自动取消，延迟重试等

**核心问题**：
- 定时扫描数据库压力大
- 精确延迟时间
- 高可用

**技术方案**：
- **Redis Sorted Set**：Score存储执行时间，定时扫描
- **RabbitMQ死信队列**：TTL + DLX机制
- **时间轮算法**：高性能内存实现

**核心文件**：
- `delayqueue/RedisDelayQueue.java` - Redis延迟队列（推荐）
- `delayqueue/TimeWheelDelayQueue.java` - 时间轮算法
- `delayqueue/RabbitMQDelayQueueExample.java` - RabbitMQ方案

**方案对比**：
| 方案 | 延迟精度 | 性能 | 可靠性 |
|------|---------|------|--------|
| Redis Sorted Set | 秒级 | 高 | 中 |
| RabbitMQ | 秒级 | 中 | 高 |
| 时间轮 | 毫秒级 | 极高 | 低 |

---

### 场景四：限流算法

**业务背景**：保护系统不被流量洪峰打垮

**核心问题**：
- 流量突刺
- 分布式限流
- 精确度vs性能

**技术方案**：
- **令牌桶算法**：允许突发流量，平滑限流（推荐）
- **漏桶算法**：强制平滑输出
- **滑动窗口**：精确限流
- **固定窗口**：简单但有临界问题

**核心文件**：
- `ratelimit/TokenBucketRateLimiter.java` - 令牌桶实现
- `ratelimit/RedisRateLimiter.java` - 基于Redis的分布式限流

**应用场景**：
- API接口限流：每秒1000个请求
- 用户维度限流：每分钟10个请求
- IP限流：每秒100个请求

---

### 场景五：接口幂等性设计

**业务背景**：网络重试、消息重复消费导致重复执行

**核心问题**：
- 重复扣款
- 重复下单
- 数据一致性

**技术方案**：
- **唯一索引**：数据库层保证
- **Token机制**：前端重复提交（推荐）
- **分布式锁**：并发安全
- **状态机**：业务状态流转
- **去重表**：记录已处理请求

**核心文件**：
- `idempotent/TokenIdempotentService.java` - Token机制实现

**实现流程**：
1. 客户端请求Token
2. 服务端生成Token存Redis
3. 客户端携带Token请求业务
4. 服务端验证并删除Token（原子性）
5. 执行业务逻辑

---

### 场景六：热点数据处理

**业务背景**：明星同款商品成为热点，单Key QPS 10万+

**核心问题**：
- Redis单Key成为瓶颈
- 缓存击穿：热点Key过期，大量请求打到数据库
- 缓存雪崩：大量Key同时过期

**技术方案**：
- **多级缓存**：本地缓存(Guava) + Redis + 数据库
- **互斥锁**：防止缓存击穿
- **热点数据永不过期**：逻辑过期 + 异步更新
- **热点发现与隔离**：复制多份分散压力

**核心文件**：
- `hotdata/HotDataService.java` - 热点数据处理方案

**性能提升**：
- 本地缓存QPS可达百万级
- 避免缓存击穿导致的数据库雪崩

---

### 场景七：分布式ID生成

**业务背景**：分布式系统需要生成全局唯一ID

**核心问题**：
- 数据库自增：性能差，单点故障
- UUID：无序，不适合索引
- Redis INCR：依赖Redis

**技术方案**：
- **雪花算法（Snowflake）**：趋势递增 + 高性能 + 高可用（推荐）
- **美团Leaf**：号段模式 + Snowflake
- **百度UidGenerator**：雪花算法优化

**核心文件**：
- `distributedid/SnowflakeIdGenerator.java` - 雪花算法实现

**ID结构**（64位）：
```
1位符号 + 41位时间戳 + 5位机房ID + 5位机器ID + 12位序列号
```

**性能指标**：
- 单机QPS：1000万
- 时间精度：毫秒级
- 可用时间：69年

---

### 场景八：APP在线状态检测与智能推送

**业务背景**：APP消息推送系统，根据用户在线状态决定推送方式

**业务需求**：
- 用户在线：通过WebSocket实时推送
- 用户离线：通过极光/FCM推送到系统通知栏
- 避免重复推送，节省推送成本

**核心问题**：
- 如何判断用户是否在线？
- 网络抖动、APP切后台如何处理？
- 分布式环境下的状态同步
- 心跳检测策略

**技术方案**：
- **WebSocket长连接**：建立持久连接，实时通信
- **Redis状态存储**：全局共享在线状态
- **心跳机制**：每30秒心跳，60秒超时
- **智能推送**：在线WebSocket，离线极光推送

**核心文件**：
- `apppush/OnlineStatusManager.java` - 在线状态管理
- `apppush/WebSocketServer.java` - WebSocket服务器
- `apppush/SmartPushService.java` - 智能推送服务
- `apppush/ClientIntegrationGuide.md` - 客户端集成指南（Android/iOS/Web）

**系统架构**：
```
客户端 → WebSocket建立连接 → Redis记录在线状态 → 心跳保活
                                    ↓
                          推送服务查询在线状态
                          ↙              ↘
                   在线：WebSocket推送    离线：极光推送
```

**关键技术点**：
1. **在线状态维护**：Redis存储 userId:deviceId -> {serverId, connectTime, platform}
2. **心跳机制**：客户端30秒心跳，服务端60秒TTL自动过期
3. **多端登录**：支持手机+平板+PC同时在线
4. **网络抖动**：断线5秒缓冲期，给重连机会
5. **状态同步**：通过Redis Pub/Sub跨服务器转发消息

**性能优化**：
- 批量查询在线状态（Redis Pipeline）
- 本地缓存（Caffeine缓存1秒）
- 分离推送（在线用户WebSocket，离线用户批量极光推送）

**适用场景**：
- IM聊天系统
- 消息推送系统
- 实时通知系统
- 社交APP

---

### 场景九：电子签名回调重复日志问题

**业务背景**：医疗系统中,医生完成电子签名后,第三方平台(易签宝)回调业务系统。系统使用Redis分布式锁和异步处理来更新签署状态和记录日志。

**核心问题**：
- 同一次签署产生两条日志记录
- 数据冗余,审计混乱
- Redis锁的作用范围不足

**技术方案**：
- **数据库唯一约束**：唯一索引 + 异常捕获(推荐)
- **锁覆盖异步任务**：分布式锁保护整个流程
- **Token幂等性**：防止回调重试
- **状态机**：CAS操作保证状态只流转一次
- **先查后插**：查询存在性 + 分布式锁

**核心文件**：
- `esignature/场景说明_电子签名回调重复日志问题.md` - 详细问题分析
- `esignature/SignatureCallbackController.java` - 回调接口(5种方案)
- `esignature/SignatureAsyncService.java` - 异步处理服务
- `esignature/SignatureLogService.java` - 日志记录服务
- `esignature/SignatureDuplicateLogTest.java` - 测试演示
- `esignature/database.sql` - 数据库表结构

**问题根源**：
```java
// 问题代码
try {
    redisLock.tryLock(lockKey);
    asyncService.processSignature(dto);  // 异步执行
    return Result.success();
} finally {
    redisLock.unlock(lockKey);  // ❌ 立即释放,异步任务未完成!
}
```

**推荐解决方案**：
```sql
-- 数据库添加唯一索引
ALTER TABLE signature_log ADD UNIQUE INDEX uk_signature_id (signature_id);
```

```java
// 业务代码捕获重复异常
try {
    signatureLogMapper.insert(log);
} catch (DuplicateKeyException e) {
    log.warn("日志已存在,忽略: {}", signatureId);
}
```

**方案对比**：
| 方案 | 实现难度 | 可靠性 | 性能 | 推荐指数 |
|------|---------|--------|------|---------|
| 数据库唯一约束 | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 锁覆盖异步任务 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Token幂等性 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 状态机 | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

**最佳实践**：
```
多层防护策略:
第1层: Token幂等性验证 (防止回调重试)
第2层: Redis分布式锁 (防止并发)
第3层: 业务状态检查 (先查后插)
第4层: 数据库唯一约束 (最后防线)
```

**关键要点**：
- ✅ 分布式锁要覆盖整个业务逻辑(包括异步任务)
- ✅ 数据库唯一约束是最简单、最可靠的方案
- ✅ 第三方回调要考虑重试机制
- ✅ 多层防护,纵深防御
- ✅ 完善的监控和日志追踪

---

### 场景十：大数据批处理与外部API调用

**业务背景**：系统需要循环处理1亿条数据，每条数据调用N个外部API获取补充信息，在内存中运算后更新到数据库

**核心问题**：
- 性能瓶颈：1亿条数据 × N个API × 200ms延迟
- 可靠性问题：API调用失败、任务中断、数据不一致
- 资源管理：线程池耗尽、数据库连接池耗尽、API限流

**技术方案**：
- **方案1：分页查询 + 多线程并发 + 限流控制**（推荐）
- **方案2：消息队列 + 消费者集群**
- **方案3：分布式任务调度 + 分片处理**（推荐）
- **方案4：流式处理 + 背压控制**

**核心文档**：
- `大数据批处理与外部API调用方案.md` - 完整技术方案

**关键技术点**：
- 分页查询：每次1000条，避免OOM
- 多线程并发：50-200线程，充分利用CPU
- 限流控制：Guava RateLimiter，保护外部API
- 断点续传：Redis记录进度，支持任务重启
- 批量更新：每100条批量UPDATE数据库
- 重试机制：指数退避重试，最多3次
- 监控告警：TPS、成功率、API延迟

**性能数据**：
- 单机多线程：100-500 TPS
- 分布式10机：1000-5000 TPS
- 1亿条数据：5-24小时完成

**大厂案例**：
- 阿里巴巴：商品价格批量更新（5000万条，4小时）
- 美团：用户信用分批量计算（1亿条，23小时）

**推荐方案**：
- 数据量 < 5000万：单机多线程 + 限流控制
- 数据量 > 5000万：分布式任务调度 + 分片处理

---

## 依赖说明

主要依赖：
- **Redis客户端**：Jedis 4.3.1
- **工具包**：Guava 31.1
- **日志**：SLF4J + Logback
- **JSON**：Gson 2.10.1

```xml
<dependencies>
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>4.3.1</version>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>31.1-jre</version>
    </dependency>
</dependencies>
```

## 使用指南

### 1. 环境准备

- JDK 8+
- Maven 3.6+
- Redis 5.0+（可选）

### 2. 编译项目

```bash
cd BusinesScenario
mvn clean compile
```

### 3. 运行示例

每个场景的主类都包含main方法，可以直接运行查看效果：

```bash
# 秒杀系统示例
java -cp target/classes com.architecture.seckill.SeckillService

# 分布式锁示例
java -cp target/classes com.architecture.distributedlock.RedisDistributedLock

# 延迟队列示例
java -cp target/classes com.architecture.delayqueue.RedisDelayQueue

# 限流算法示例
java -cp target/classes com.architecture.ratelimit.TokenBucketRateLimiter

# 雪花算法示例
java -cp target/classes com.architecture.distributedid.SnowflakeIdGenerator

# 电子签名回调重复日志测试
java -cp target/classes com.architecture.esignature.SignatureDuplicateLogTest
```

## 技术栈总结

| 技术领域 | 核心技术 | 应用场景 |
|---------|---------|---------|
| 高并发 | Redis + 消息队列 + 本地缓存 | 秒杀、抢购 |
| 分布式协调 | Redis NX + Redisson + Zookeeper | 定时任务去重、资源互斥 |
| 异步处理 | 延迟队列 + 时间轮 | 订单超时、延迟重试 |
| 流量控制 | 令牌桶 + 滑动窗口 | API限流、防刷 |
| 数据一致性 | Token + 分布式锁 + 状态机 + 唯一约束 | 防重复提交、幂等性、防重复日志 |
| 性能优化 | 多级缓存 + 热点隔离 | 热点数据、缓存击穿 |
| ID生成 | 雪花算法 | 订单号、用户ID |

## 面试要点

### 高频考点

1. **秒杀系统设计**
   - 如何防止超卖？
   - 如何应对百万QPS？
   - Redis和数据库如何配合？

2. **分布式锁**
   - Redis和Zookeeper方案对比？
   - Redisson看门狗机制原理？
   - 如何处理时钟回拨？

3. **延迟队列**
   - Redis、RabbitMQ、时间轮对比？
   - 如何保证消息不丢失？

4. **限流算法**
   - 令牌桶和漏桶的区别？
   - 如何实现分布式限流？

5. **缓存一致性**
   - 如何防止缓存击穿、雪崩、穿透？
   - 多级缓存的数据一致性？

6. **异步任务与分布式锁**
   - 分布式锁的作用范围如何确定？
   - 异步任务如何避免重复执行？
   - 如何防止第三方回调重复？

### 回答模板

1. **描述业务场景**：电商秒杀、支付重试等
2. **分析核心问题**：并发、一致性、性能瓶颈
3. **对比多种方案**：Redis vs Zookeeper，优劣势
4. **给出推荐方案**：结合场景选择
5. **说明注意事项**：边界条件、异常处理

## 扩展阅读

- [Redis官方文档](https://redis.io/documentation)
- [Redisson官方文档](https://redisson.org/)
- [RabbitMQ延迟队列](https://www.rabbitmq.com/dlx.html)
- [Guava RateLimiter](https://github.com/google/guava/wiki/CachesExplained)
- [Twitter Snowflake](https://github.com/twitter-archive/snowflake)

## 作者

本项目用于记录各种业务场景的处理方案和疑难杂症的技术实现。

## 许可证

MIT License
