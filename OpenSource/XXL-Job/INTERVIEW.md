# XXL-Job面试题集

## 基础篇

### 1. 什么是XXL-Job?它解决了什么问题?

**答案**:

XXL-Job是一个轻量级分布式任务调度平台,核心设计目标是开发迅速、学习简单、轻量级、易扩展。

**解决的问题**:
1. **定时任务管理**: 提供Web界面统一管理定时任务,无需重启应用
2. **分布式调度**: 支持任务在分布式环境下调度和执行
3. **高可用**: 调度中心和执行器都支持集群部署
4. **任务监控**: 提供完整的任务执行日志和监控报警
5. **动态扩展**: 支持执行器动态上线下线

**对比Spring Task**:
- Spring Task只支持单机定时任务
- XXL-Job支持分布式调度、任务分片、故障转移等高级特性

### 2. XXL-Job的核心架构是怎样的?

**答案**:

XXL-Job采用**调度中心 + 执行器**的架构:

```
┌─────────────────────────────────────────┐
│         调度中心集群(Admin)              │
│  ┌──────────┐  ┌──────────┐            │
│  │ Admin-1  │  │ Admin-2  │  负载均衡  │
│  └──────────┘  └──────────┘            │
│       │              │                  │
│       └──────┬───────┘                  │
│              │                          │
│         ┌────┴────┐                     │
│         │  MySQL  │  任务配置/日志      │
│         └─────────┘                     │
└─────────────┬───────────────────────────┘
              │ RPC调用
              ↓
┌─────────────────────────────────────────┐
│         执行器集群(Executor)             │
│  ┌──────────┐  ┌──────────┐            │
│  │ Exec-1   │  │ Exec-2   │  自动注册  │
│  └──────────┘  └──────────┘            │
└─────────────────────────────────────────┘
```

**核心组件**:

1. **调度中心(Admin)**:
   - 任务管理和配置
   - 调度触发
   - 执行器管理
   - 日志查询
   - 报警通知

2. **执行器(Executor)**:
   - 接收调度请求
   - 执行任务
   - 日志回调
   - 任务注册

3. **通信协议**:
   - 基于HTTP的轻量级RPC
   - 支持RESTful接口

### 3. XXL-Job的调度流程是怎样的?

**答案**:

完整调度流程:

```
1. 任务扫描
   ┌─────────────────────────────┐
   │ 调度中心扫描待执行任务       │
   │ - 时间轮算法                │
   │ - 预读取未来5秒内的任务      │
   └──────────┬──────────────────┘
              ↓
2. 任务触发
   ┌─────────────────────────────┐
   │ 选择执行器                   │
   │ - 根据路由策略选择执行器     │
   │ - 发送RPC调用请求           │
   └──────────┬──────────────────┘
              ↓
3. 任务执行
   ┌─────────────────────────────┐
   │ 执行器接收任务               │
   │ - 解析任务参数              │
   │ - 调用JobHandler            │
   │ - 记录执行日志              │
   └──────────┬──────────────────┘
              ↓
4. 结果回调
   ┌─────────────────────────────┐
   │ 回调执行结果                │
   │ - 回调成功/失败状态         │
   │ - 回调执行日志              │
   │ - 触发报警(如果失败)        │
   └─────────────────────────────┘
```

**关键点**:
- 调度中心使用**时间轮算法**高效扫描任务
- 执行器使用**线程池**执行任务
- 通过**DB锁**保证集群调度一致性

### 4. XXL-Job支持哪些路由策略?分别适用于什么场景?

**答案**:

| 路由策略 | 说明 | 适用场景 |
|---------|------|---------|
| FIRST | 第一个 | 固定使用某个执行器 |
| LAST | 最后一个 | 固定使用某个执行器 |
| ROUND | 轮询 | **最常用**,负载均衡 |
| RANDOM | 随机 | 负载均衡 |
| CONSISTENT_HASH | 一致性HASH | 需要绑定数据到固定节点 |
| LEAST_FREQUENTLY_USED | 最不经常使用 | 根据执行频率分配 |
| LEAST_RECENTLY_USED | 最近最久未使用 | 根据使用时间分配 |
| FAILOVER | 故障转移 | **高可用场景**,自动切换 |
| BUSYOVER | 忙碌转移 | 避免单点过载 |
| SHARDING_BROADCAST | 分片广播 | **大数据量任务**并行处理 |

**最佳实践**:
- **普通任务**: 使用ROUND轮询
- **需要高可用**: 使用FAILOVER故障转移
- **大数据量**: 使用SHARDING_BROADCAST分片广播
- **数据绑定**: 使用CONSISTENT_HASH一致性哈希

### 5. 什么是任务分片?如何实现?

**答案**:

**任务分片**是将一个任务分配给多个执行器并行执行,适用于大数据量处理场景。

**实现原理**:

```java
@XxlJob("shardingJobHandler")
public void shardingJobHandler() throws Exception {
    // 获取分片参数
    int shardIndex = XxlJobHelper.getShardIndex();  // 当前分片序号(0,1,2...)
    int shardTotal = XxlJobHelper.getShardTotal();  // 总分片数

    // 方案1: 根据ID取模分片
    List<Order> orders = orderDao.queryAll();
    for (Order order : orders) {
        if (order.getId() % shardTotal == shardIndex) {
            // 处理属于当前分片的数据
            processOrder(order);
        }
    }

    // 方案2: 分页查询分片
    int pageSize = 100;
    int pageNo = shardIndex;  // 从分片索引开始
    while (true) {
        List<Order> page = orderDao.queryPage(pageNo, pageSize);
        if (page.isEmpty()) break;

        // 处理数据
        page.forEach(this::processOrder);

        pageNo += shardTotal;  // 跳过其他分片的页
    }
}
```

**场景举例**:

假设有3个执行器,需要处理10000条订单:
- 执行器1(shardIndex=0): 处理ID % 3 == 0的订单
- 执行器2(shardIndex=1): 处理ID % 3 == 1的订单
- 执行器3(shardIndex=2): 处理ID % 3 == 2的订单

**优势**:
- 并行处理,提升效率
- 自动负载均衡
- 支持动态扩容

## 进阶篇

### 6. XXL-Job如何保证调度的一致性?

**答案**:

XXL-Job通过**数据库悲观锁**保证集群环境下调度的一致性。

**实现原理**:

```sql
-- 调度时先获取锁
SELECT * FROM xxl_job_lock
WHERE lock_name = 'schedule_lock'
FOR UPDATE;

-- 执行调度逻辑
-- 1. 扫描待执行任务
-- 2. 触发任务调度
-- 3. 更新任务状态

-- 提交事务,释放锁
COMMIT;
```

**流程**:

```
调度中心节点1                    调度中心节点2
     │                               │
     ├─ 尝试获取DB锁                  │
     │  (FOR UPDATE)                  │
     ↓                                │
  获取成功                            ├─ 尝试获取DB锁
     │                               ↓
  执行调度                         等待(阻塞)
     │                               │
  提交事务,释放锁                     │
     ↓                               ↓
                                  获取锁成功
                                     │
                                  执行调度
```

**优点**:
- 实现简单
- 保证强一致性

**缺点**:
- 性能受限于数据库
- 存在单点瓶颈

**优化方案**:
- 使用时间轮算法减少锁竞争
- 预读取任务减少扫描次数
- 异步触发提高并发

### 7. XXL-Job的执行器注册流程是怎样的?

**答案**:

执行器采用**自动注册**机制:

```
执行器启动                          调度中心
    │                                 │
    ├─ 1. 启动内嵌Server              │
    │    (Netty/Jetty)                │
    ↓                                 │
 创建注册线程                          │
    │                                 │
    ├─ 2. 发送注册请求 ───────────────>│
    │    POST /api/registry           │
    │    {                            │
    │      appname: "executor",       ↓
    │      address: "127.0.0.1:9999"  接收注册
    │    }                            │
    │                                 ├─ 存储执行器信息
    │                                 ↓
    │<─────────────────────────── 3. 返回成功
    │                                 │
    ├─ 4. 定时发送心跳                 │
    │    (默认30秒)  ─────────────────>│
    │                                 ├─ 更新最后心跳时间
    │                                 ↓
    │                                检测超时(90秒)
    │                                 │
    ├─ 5. 应用关闭                     │
    │                                 │
    ├─ 发送注销请求 ──────────────────>│
    │    POST /api/registryRemove     ↓
    │                              删除执行器信息
```

**关键代码**:

```java
// ExecutorRegistryThread.java
public void start() {
    // 注册线程
    registryThread = new Thread(() -> {
        while (!toStop) {
            try {
                // 注册执行器
                RegistryParam registryParam = new RegistryParam(
                    RegistryConfig.RegistType.EXECUTOR.name(),
                    appname,
                    address
                );
                adminBiz.registry(registryParam);

                // 等待30秒
                TimeUnit.SECONDS.sleep(30);
            } catch (Exception e) {
                // 失败重试
            }
        }
    });
    registryThread.start();
}
```

**注意事项**:
- 执行器地址可自动获取或手动指定
- 心跳超时(90秒)后自动摘除
- 优雅停机时主动注销

### 8. XXL-Job如何实现故障转移(Failover)?

**答案**:

当路由策略为**FAILOVER**时,实现自动故障转移:

**执行流程**:

```java
// ExecutorRouteFailover.java
public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {

    // 遍历所有执行器地址
    for (String address : addressList) {
        try {
            // 1. 尝试调用执行器
            ReturnT<String> result = runExecutor(triggerParam, address);

            // 2. 调用成功,返回结果
            if (result.getCode() == ReturnT.SUCCESS_CODE) {
                return result;
            }

        } catch (Exception e) {
            // 3. 调用失败,尝试下一个执行器
            logger.warn("执行器[{}]调用失败,尝试下一个", address);
        }
    }

    // 4. 所有执行器都失败
    return new ReturnT<>(ReturnT.FAIL_CODE, "所有执行器调用失败");
}
```

**流程示意**:

```
调度中心
   │
   ├─ 选择执行器1
   │     │
   │     ├─ 调用失败(超时/异常)
   │     ↓
   ├─ 选择执行器2
   │     │
   │     ├─ 调用失败
   │     ↓
   ├─ 选择执行器3
   │     │
   │     ├─ 调用成功 ✓
   │     ↓
   └─ 返回成功
```

**应用场景**:
- 关键任务,必须执行成功
- 执行器可能不稳定
- 需要高可用保证

**注意事项**:
- 需要保证任务幂等性(可能重复调用)
- 可能增加调度延迟
- 建议配合重试策略使用

### 9. XXL-Job的阻塞处理策略有哪些?分别如何使用?

**答案**:

当任务执行时间超过调度周期时,需要选择阻塞处理策略:

| 策略 | 说明 | 适用场景 |
|-----|------|---------|
| **单机串行** | 后续调度进入队列,串行执行 | 必须保证顺序执行 |
| **丢弃后续调度** | 丢弃后续调度请求 | 可以容忍跳过某些调度 |
| **覆盖之前调度** | 终止正在执行的任务,执行新任务 | 只关心最新状态 |

**实现原理**:

```java
// JobThread.java
public class JobThread extends Thread {

    private LinkedBlockingQueue<TriggerParam> triggerQueue = new LinkedBlockingQueue<>();

    public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {

        // 获取阻塞策略
        String blockStrategy = triggerParam.getExecutorBlockStrategy();

        if (ExecutorBlockStrategyEnum.DISCARD_LATER.name().equals(blockStrategy)) {
            // 丢弃后续调度
            if (triggerQueue.size() > 0) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "任务执行中,丢弃后续调度");
            }

        } else if (ExecutorBlockStrategyEnum.COVER_EARLY.name().equals(blockStrategy)) {
            // 覆盖之前调度
            triggerQueue.clear();
            // 停止当前运行的任务
            stopThread();
        }

        // 单机串行 - 直接加入队列
        triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
    }
}
```

**场景示例**:

```
任务每5秒执行一次,但单次执行需要8秒

单机串行:
00:00 执行任务1 ─────────────> (8秒)
      00:05 任务2排队
      00:10 任务3排队
                             00:08 任务1完成
                             00:08 开始执行任务2 ────> (8秒)
                                                  00:16 开始执行任务3

丢弃后续调度:
00:00 执行任务1 ─────────────> (8秒)
      00:05 任务2丢弃 ✗
      00:10 任务3丢弃 ✗
                             00:08 任务1完成

覆盖之前调度:
00:00 执行任务1 ────>
      00:05 停止任务1,执行任务2 ────>
            00:10 停止任务2,执行任务3 ───────> (8秒)
```

### 10. 如何保证XXL-Job任务的幂等性?

**答案**:

任务幂等性是分布式任务调度的关键,需要从多个层面保证:

**方案1: 使用分布式锁**

```java
@XxlJob("idempotentJob")
public void idempotentJob() {
    String lockKey = "xxljob:lock:" + XxlJobHelper.getJobId();

    // 尝试获取Redis分布式锁
    boolean locked = redisLock.tryLock(lockKey, 30, TimeUnit.SECONDS);
    if (!locked) {
        XxlJobHelper.log("任务正在执行中,跳过");
        return;
    }

    try {
        // 执行业务逻辑
        doBusinessLogic();

    } finally {
        redisLock.unlock(lockKey);
    }
}
```

**方案2: 数据库唯一索引**

```java
@XxlJob("orderSyncJob")
public void orderSyncJob() {
    List<Order> orders = getOrders();

    for (Order order : orders) {
        try {
            // 使用唯一索引保证幂等
            // CREATE UNIQUE INDEX idx_order_id ON sync_log(order_id)
            syncLogDao.insert(new SyncLog(order.getId()));

            // 同步订单
            syncOrder(order);

        } catch (DuplicateKeyException e) {
            // 已同步过,跳过
            XxlJobHelper.log("订单{}已同步,跳过", order.getId());
        }
    }
}
```

**方案3: 业务状态标记**

```java
@XxlJob("statusJob")
public void statusJob() {
    // 查询待处理数据
    List<Task> tasks = taskDao.queryByStatus(TaskStatus.PENDING);

    for (Task task : tasks) {
        // 更新状态为处理中(乐观锁)
        int updated = taskDao.updateStatus(
            task.getId(),
            TaskStatus.PROCESSING,
            TaskStatus.PENDING  // 只有pending状态才能更新
        );

        if (updated == 0) {
            // 状态已变更,跳过
            continue;
        }

        try {
            // 处理任务
            processTask(task);

            // 更新为完成
            taskDao.updateStatus(task.getId(), TaskStatus.COMPLETED, TaskStatus.PROCESSING);

        } catch (Exception e) {
            // 失败回滚状态
            taskDao.updateStatus(task.getId(), TaskStatus.PENDING, TaskStatus.PROCESSING);
        }
    }
}
```

**方案4: 任务执行记录表**

```java
@XxlJob("recordJob")
public void recordJob() {
    // 生成任务执行标识
    String taskKey = generateTaskKey();  // 基于日期+业务key

    // 检查是否已执行
    TaskRecord record = recordDao.selectByKey(taskKey);
    if (record != null && record.isSuccess()) {
        XxlJobHelper.log("任务{}已执行成功,跳过", taskKey);
        return;
    }

    // 记录任务开始
    recordDao.insert(new TaskRecord(taskKey, TaskRecord.Status.RUNNING));

    try {
        // 执行任务
        doTask();

        // 标记成功
        recordDao.updateStatus(taskKey, TaskRecord.Status.SUCCESS);

    } catch (Exception e) {
        // 标记失败
        recordDao.updateStatus(taskKey, TaskRecord.Status.FAILED);
        throw e;
    }
}
```

**最佳实践总结**:

1. **任务层面**: 使用分布式锁防止并发执行
2. **数据层面**: 使用唯一索引、状态机、记录表防止重复处理
3. **业务层面**: 设计幂等的业务逻辑
4. **监控层面**: 记录执行日志便于排查

## 架构设计篇

### 11. XXL-Job调度中心如何实现高可用?

**答案**:

**架构方案**:

```
         Nginx/LVS (负载均衡)
              │
     ┌────────┴────────┐
     │                 │
  Admin-1           Admin-2
     │                 │
     └────────┬────────┘
              │
          MySQL主从
         (共享数据)
```

**实现要点**:

1. **调度中心集群部署**
   ```properties
   # 执行器配置多个调度中心地址
   xxl.job.admin.addresses=http://admin1:8080/xxl-job-admin,http://admin2:8080/xxl-job-admin
   ```

2. **DB锁保证调度一致性**
   - 多个调度中心同时运行
   - 通过DB锁保证同一时刻只有一个节点调度
   - 其他节点standby待命

3. **MySQL高可用**
   ```
   主从复制 + 故障自动切换
   - 主库负责写入
   - 从库负责读取
   - MHA/MMM自动故障转移
   ```

4. **执行器自动切换**
   - 执行器向所有调度中心注册
   - 调度中心之间数据同步(通过DB)
   - 任意调度中心宕机不影响调度

**高可用验证**:

| 故障场景 | 影响 | 恢复时间 |
|---------|------|---------|
| 单个调度中心宕机 | 无影响,其他节点继续调度 | 0秒 |
| MySQL主库宕机 | 调度暂停 | MHA自动切换,30秒内 |
| 单个执行器宕机 | 该节点任务失败,其他节点正常 | 0秒(故障转移) |
| 全部执行器宕机 | 任务无法执行 | 需人工恢复 |

### 12. 大数据量任务如何优化性能?

**答案**:

**优化策略**:

**1. 使用分片广播**

```java
@XxlJob("bigDataJob")
public void bigDataJob() {
    int shardIndex = XxlJobHelper.getShardIndex();
    int shardTotal = XxlJobHelper.getShardTotal();

    // 并行处理,10个执行器可提升10倍性能
    processBigData(shardIndex, shardTotal);
}
```

**2. 批量处理**

```java
@XxlJob("batchJob")
public void batchJob() {
    int batchSize = 1000;  // 每批1000条
    int pageNo = 0;

    while (true) {
        List<Data> dataList = queryData(pageNo, batchSize);
        if (dataList.isEmpty()) break;

        // 批量插入/更新
        batchProcess(dataList);

        pageNo++;
    }
}
```

**3. 异步处理**

```java
@XxlJob("asyncJob")
public void asyncJob() {
    List<Data> dataList = queryData();

    // 使用线程池异步处理
    List<CompletableFuture<Void>> futures = dataList.stream()
        .map(data -> CompletableFuture.runAsync(
            () -> processData(data),
            executorService
        ))
        .collect(Collectors.toList());

    // 等待全部完成
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
}
```

**4. 数据预加载**

```java
@XxlJob("preloadJob")
public void preloadJob() {
    // 预加载配置数据到缓存
    Map<String, Config> configMap = loadAllConfigs();

    // 批量查询,减少数据库交互
    List<Data> dataList = queryAllData();

    // 使用内存数据处理
    for (Data data : dataList) {
        Config config = configMap.get(data.getConfigId());
        processData(data, config);
    }
}
```

**5. 合理设置调度频率**

```
低频大批量 vs 高频小批量

低频大批量 (推荐):
- 每30分钟执行一次
- 每次处理10000条数据
- 减少调度开销

高频小批量:
- 每1分钟执行一次
- 每次处理300条数据
- 调度开销大,不推荐
```

**性能对比**:

| 优化项 | 优化前 | 优化后 | 提升 |
|-------|--------|--------|------|
| 分片广播(10节点) | 100分钟 | 10分钟 | 10倍 |
| 批量处理(1000条/批) | 50分钟 | 5分钟 | 10倍 |
| 异步处理(20线程) | 30分钟 | 3分钟 | 10倍 |
| 数据预加载 | 20分钟 | 2分钟 | 10倍 |

### 13. XXL-Job与其他任务调度框架对比?

**答案**:

| 特性 | XXL-Job | Quartz | Elastic-Job | Spring Task |
|-----|---------|--------|-------------|-------------|
| **分布式调度** | ✅ 原生支持 | ❌ 需要集群方案 | ✅ 原生支持 | ❌ 单机 |
| **管理界面** | ✅ 完善的Web UI | ❌ 需要自己开发 | ⚠️ 较简陋 | ❌ 无 |
| **任务分片** | ✅ 支持 | ❌ 不支持 | ✅ 支持 | ❌ 不支持 |
| **动态修改** | ✅ 支持 | ⚠️ 需重启 | ✅ 支持 | ❌ 需重启 |
| **执行日志** | ✅ 完善 | ⚠️ 需自己实现 | ⚠️ 简单 | ❌ 无 |
| **报警监控** | ✅ 支持 | ❌ 需自己实现 | ⚠️ 简单 | ❌ 无 |
| **故障转移** | ✅ 支持 | ❌ 不支持 | ✅ 支持 | ❌ 不支持 |
| **学习成本** | 🟢 低 | 🟡 中 | 🟡 中 | 🟢 低 |
| **社区活跃度** | 🟢 高 | 🟡 中 | 🟡 中 | 🟢 高 |

**选型建议**:

```
Spring Task:
  - 适用场景: 单机应用,简单定时任务
  - 优点: 简单易用,无需额外部署
  - 缺点: 功能有限,不支持分布式

Quartz:
  - 适用场景: 复杂调度逻辑,需要高度定制
  - 优点: 功能强大,灵活性高
  - 缺点: 配置复杂,需要自己实现很多功能

Elastic-Job:
  - 适用场景: 依赖ZooKeeper的分布式环境
  - 优点: 分布式协调好,支持任务分片
  - 缺点: 依赖ZK,运维复杂

XXL-Job (推荐):
  - 适用场景: 分布式应用,需要任务管理和监控
  - 优点: 功能完善,开箱即用,管理方便
  - 缺点: 调度中心强依赖MySQL
```

**实际项目选型**:

```
小型项目(单机):
  └─ Spring Task (够用)

中型项目(分布式):
  └─ XXL-Job (首选)

大型项目(复杂调度):
  ├─ XXL-Job (通用任务)
  └─ Elastic-Job (特殊需求)
```

## 实战应用篇

### 14. 如何使用XXL-Job实现订单超时自动取消?

**答案**:

**方案设计**:

```java
/**
 * 订单超时取消任务
 * 场景: 用户下单30分钟未支付,自动取消订单
 * 调度策略: 每分钟执行一次
 * 路由策略: 分片广播(支持多节点并行)
 */
@XxlJob("orderTimeoutCancelJob")
public void orderTimeoutCancelJob() {
    XxlJobHelper.log("开始执行订单超时取消任务");

    // 获取分片参数
    int shardIndex = XxlJobHelper.getShardIndex();
    int shardTotal = XxlJobHelper.getShardTotal();

    // 查询30分钟前创建的待支付订单
    LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(30);

    int pageSize = 100;
    int pageNo = shardIndex;  // 从分片索引开始
    int cancelCount = 0;

    while (true) {
        // 分页查询超时订单
        List<Order> timeoutOrders = orderDao.queryTimeoutOrders(
            timeoutTime,
            pageNo,
            pageSize,
            shardTotal
        );

        if (timeoutOrders.isEmpty()) {
            break;
        }

        // 处理每个订单
        for (Order order : timeoutOrders) {
            try {
                // 幂等性检查
                if (order.getStatus() != OrderStatus.WAIT_PAY) {
                    continue;
                }

                // 取消订单(包含库存回退等操作)
                boolean success = orderService.cancelOrder(order.getId());

                if (success) {
                    cancelCount++;
                    XxlJobHelper.log("取消订单成功: {}", order.getOrderNo());

                    // 发送取消通知
                    notifyService.sendCancelNotify(order);
                }

            } catch (Exception e) {
                XxlJobHelper.log("取消订单失败: {}, 原因: {}",
                    order.getOrderNo(), e.getMessage());
            }
        }

        // 下一页(跳过其他分片)
        pageNo += shardTotal;
    }

    XxlJobHelper.log("订单超时取消任务完成,共取消{}笔订单", cancelCount);
    XxlJobHelper.handleSuccess();
}
```

**数据库查询**:

```sql
-- 查询超时订单(支持分片)
SELECT * FROM t_order
WHERE status = 'WAIT_PAY'
  AND create_time < #{timeoutTime}
  AND MOD(id, #{shardTotal}) = #{shardIndex}  -- 分片条件
ORDER BY id
LIMIT #{pageSize}
```

**配置**:

```
任务名称: 订单超时取消
Cron表达式: 0 */1 * * * ?  (每分钟执行)
路由策略: 分片广播
阻塞策略: 单机串行
```

### 15. 如何使用XXL-Job实现数据同步任务?

**答案**:

**场景**: 每天凌晨2点将订单数据从MySQL同步到Elasticsearch

```java
@XxlJob("orderDataSyncJob")
public void orderDataSyncJob() {
    XxlJobHelper.log("开始同步订单数据到ES");

    // 获取同步时间范围(昨天的数据)
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDateTime startTime = yesterday.atStartOfDay();
    LocalDateTime endTime = yesterday.atTime(23, 59, 59);

    // 获取分片参数
    int shardIndex = XxlJobHelper.getShardIndex();
    int shardTotal = XxlJobHelper.getShardTotal();

    int batchSize = 500;  // 每批500条
    int pageNo = shardIndex;
    int syncCount = 0;

    try {
        while (true) {
            // 分页查询订单数据
            List<Order> orders = orderDao.queryByDateRange(
                startTime,
                endTime,
                pageNo,
                batchSize,
                shardTotal
            );

            if (orders.isEmpty()) {
                break;
            }

            // 转换为ES文档
            List<OrderDocument> documents = orders.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

            // 批量写入ES
            BulkRequest bulkRequest = new BulkRequest();
            for (OrderDocument doc : documents) {
                IndexRequest request = new IndexRequest("order_index")
                    .id(doc.getId())
                    .source(doc.toMap());
                bulkRequest.add(request);
            }

            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.hasFailures()) {
                XxlJobHelper.log("批量写入ES失败: {}", bulkResponse.buildFailureMessage());
            } else {
                syncCount += documents.size();
                XxlJobHelper.log("成功同步{}条订单数据", documents.size());
            }

            // 下一页
            pageNo += shardTotal;
        }

        XxlJobHelper.log("订单数据同步完成,共同步{}条数据", syncCount);
        XxlJobHelper.handleSuccess();

    } catch (Exception e) {
        XxlJobHelper.log("同步失败: {}", e.getMessage());
        XxlJobHelper.handleFail(e.getMessage());
    }
}
```

**配置**:

```
任务名称: 订单数据同步
Cron表达式: 0 0 2 * * ?  (每天凌晨2点)
路由策略: 分片广播
阻塞策略: 单机串行
失败重试: 3次
```

---

## 总结

XXL-Job是一个优秀的分布式任务调度平台,掌握其核心原理和最佳实践对于构建稳定可靠的分布式系统至关重要。

**核心要点**:
1. 理解调度流程和架构设计
2. 掌握路由策略和阻塞策略的应用场景
3. 保证任务的幂等性
4. 合理使用分片广播处理大数据量
5. 做好监控和报警

**学习建议**:
1. 先理解基础概念和使用
2. 深入源码理解实现原理
3. 在实际项目中应用最佳实践
4. 持续关注性能优化和高可用设计
