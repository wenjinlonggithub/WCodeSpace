# Redis分布式锁面试题完整题库

> **更新时间：** 2026-01-14
> **难度分级：** ⭐基础 ⭐⭐中级 ⭐⭐⭐高级 ⭐⭐⭐⭐专家
> **覆盖范围：** 基础原理、实现细节、场景设计、架构对比

---

## 目录

- [一、基础题（⭐）](#一基础题)
- [二、中级题（⭐⭐）](#二中级题)
- [三、高级题（⭐⭐⭐）](#三高级题)
- [四、专家题（⭐⭐⭐⭐）](#四专家题)
- [五、场景设计题](#五场景设计题)
- [六、对比题](#六对比题)
- [七、代码题](#七代码题)
- [八、开放题](#八开放题)
- [九、压力面试题](#九压力面试题)

---

## 一、基础题（⭐）

### Q1: 什么是分布式锁？为什么需要它？

**标准答案：**

**定义：**
分布式锁是控制分布式系统中多个进程对共享资源互斥访问的一种机制。

**为什么需要：**
1. **分布式环境**：多台服务器，JVM锁无效
2. **共享资源**：库存、订单号、配置等需要互斥访问
3. **数据一致性**：防止超卖、重复提交、并发冲突

**类比：**
```
单机环境：synchronized/ReentrantLock（线程锁）
分布式环境：Redis/Zookeeper（进程锁）
```

**评分标准：**
- ✅ 说出定义（2分）
- ✅ 举出实际场景（3分）
- ✅ 对比JVM锁（5分）

---

### Q2: Redis分布式锁的实现命令是什么？

**标准答案：**

```redis
SET lock_key unique_value NX EX 10
```

**参数详解：**
- `lock_key`: 锁的标识
- `unique_value`: UUID，标识锁的所有者
- `NX`: Not eXists，键不存在时才设置（保证互斥）
- `EX 10`: 过期时间10秒（防止死锁）

**为什么不用SETNX？**
```java
// ❌ 错误（非原子）
jedis.setnx(key, value);
jedis.expire(key, 10);  // 两条命令，中间可能宕机

// ✅ 正确（原子）
jedis.set(key, value, SetParams.setParams().nx().ex(10));
```

**评分标准：**
- ✅ 说出SET NX EX（3分）
- ✅ 解释各参数含义（2分）
- ✅ 知道SETNX的问题（5分）

---

### Q3: 分布式锁的三个核心属性是什么？

**标准答案：**

**1. 互斥性（Mutual Exclusion / Safety）**
```
在任意时刻，只有一个客户端能持有锁
实现：SET NX
```

**2. 防死锁（Deadlock Free / Liveness A）**
```
即使客户端崩溃，锁最终也能释放
实现：EX过期时间
```

**3. 容错性（Fault Tolerance / Liveness B）**
```
大部分Redis节点存活就能正常工作
实现：Redlock算法
```

**评分标准：**
- ✅ 说出三个属性（3分）
- ✅ 解释实现方式（2分）
- ✅ 举例说明（5分）

---

### Q4: 如何释放Redis分布式锁？

**标准答案：**

**正确做法：使用Lua脚本**

```lua
-- 验证所有权后删除
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end
```

**为什么不能直接DEL？**

```java
// ❌ 错误做法
String value = jedis.get(lockKey);
if (myValue.equals(value)) {
    jedis.del(lockKey);  // 非原子，可能误删
}
```

**时序问题：**
```
Time: 0s  → Client A 获取value
Time: 1s  → 锁过期，Client B加锁
Time: 2s  → Client A 执行DEL，误删B的锁！
```

**评分标准：**
- ✅ 说出使用Lua脚本（3分）
- ✅ 写出Lua代码（2分）
- ✅ 解释为什么（5分）

---

### Q5: Redis分布式锁的优缺点？

**标准答案：**

**优点：**
1. **性能高**：基于内存，QPS 10万+
2. **实现简单**：一条SET命令即可
3. **支持超时**：自动过期，防死锁
4. **广泛使用**：生态成熟，有Redisson等工具

**缺点：**
1. **主从同步延迟**：主节点宕机可能丢锁
2. **时钟依赖**：依赖TTL，时钟漂移有影响
3. **不可重入**：默认实现不支持（需要额外实现）
4. **非公平**：无法保证FIFO

**评分标准：**
- ✅ 说出2个优点（2分）
- ✅ 说出2个缺点（3分）
- ✅ 对比其他方案（5分）

---

## 二、中级题（⭐⭐）

### Q6: 为什么要用Lua脚本解锁？Lua如何保证原子性？

**标准答案：**

**为什么需要Lua：**

问题代码：
```java
// 非原子操作，有竞态条件
String value = jedis.get(lockKey);        // 步骤1
if (lockValue.equals(value)) {            // 步骤2
    jedis.del(lockKey);                   // 步骤3
}
```

竞态条件：
```
Client A                     Client B
│
├─ get(key) = "uuid-A"
│                           (锁过期)
│                           ├─ set(key, "uuid-B") 成功
│
├─ 判断：uuid-A == uuid-A ✅
├─ del(key)  ← 误删了B的锁！
```

**Lua如何保证原子性：**

Redis源码：
```c
// scripting.c
void evalGenericCommand(client *c, int evalsha) {
    // 1. 设置全局Lua执行标志
    server.lua_caller = c;
    server.lua_running = 1;

    // 2. 执行Lua脚本（期间阻塞其他命令）
    lua_pcall(lua, 0, 1, 0);

    // 3. 清除标志
    server.lua_caller = NULL;
    server.lua_running = 0;
}
```

**关键机制：**
1. **单线程执行**：Redis主线程执行Lua，期间阻塞
2. **命令队列**：其他客户端命令进入队列等待
3. **超时控制**：默认5秒超时（`lua-time-limit`）

**追问：Lua脚本执行超时会怎样？**

答：
```
1. 超过lua-time-limit（默认5秒）
2. Redis打印警告日志
3. 可以执行SCRIPT KILL终止（如果脚本没有写操作）
4. 如果有写操作，只能SHUTDOWN NOSAVE
```

**评分标准：**
- ✅ 说出竞态条件问题（3分）
- ✅ 解释Lua原子性（3分）
- ✅ 答对追问（4分）

---

### Q7: 锁过期但业务未完成怎么办？

**标准答案：**

**问题场景：**
```
Time: 0s   → Client A 加锁（TTL=10s）
Time: 5s   → 业务处理中...
Time: 10s  → 锁自动过期 ❌
Time: 10s  → Client B 加锁成功
Time: 12s  → Client A 完成业务，误删 Client B 的锁
```

**解决方案对比：**

| 方案 | 实现 | 优点 | 缺点 |
|------|------|------|------|
| **方案1：设置足够长的TTL** | TTL = 业务时间 × 2 | 简单 | 死锁时间长 |
| **方案2：看门狗续期** | 后台线程定期续期 | 灵活，安全 | 线程开销 |
| **方案3：UUID防误删** | 解锁时验证value | 防误删 | 无法避免过期 |
| **方案4：业务幂等** | 操作可重复执行 | 彻底解决 | 实现复杂 |

**推荐方案：看门狗 + UUID + 幂等**

```java
// 看门狗实现
Thread watchDog = new Thread(() -> {
    while (!interrupted) {
        Thread.sleep(TTL / 3);
        renewal();  // 续期
    }
});
watchDog.setDaemon(true);
watchDog.start();

try {
    // 业务逻辑
    doBusinessLogic();
} finally {
    watchDog.interrupt();
    lock.unlock();
}
```

**Redisson的看门狗：**
```java
// Redisson默认实现
默认TTL: 30秒
续期周期: 10秒（TTL/3）
自动续期: 直到unlock()或客户端断开
```

**追问：如果看门狗线程也崩溃了怎么办？**

答：
```
1. 锁会自动过期（防死锁机制）
2. 业务需要实现幂等性
3. 可以使用事务或乐观锁兜底
```

**评分标准：**
- ✅ 说出问题场景（2分）
- ✅ 提出看门狗方案（3分）
- ✅ 考虑到幂等性（5分）
- ✅ 答对追问（10分）

---

### Q8: 主节点宕机导致锁丢失怎么办？

**标准答案：**

**问题根源：Redis主从复制是异步的**

```c
// replication.c
void replicationFeedSlaves(list *slaves, ...) {
    // 写入复制缓冲区（异步）
    addReplyToBuffer(slave, buf, len);
    // 不等待ACK就返回 ❌
}
```

**场景重现：**
```
架构：Master + Slave

Time: 0s   → Client A 向 Master 加锁成功
Time: 0.1s → Master 宕机（锁未同步到 Slave）
Time: 0.2s → Slave 升级为 Master
Time: 0.3s → Client B 向新 Master 加锁成功
结果：A和B同时持有锁 ❌
```

**解决方案对比：**

| 方案 | 可靠性 | 性能 | 复杂度 | 适用场景 |
|------|-------|------|-------|----------|
| **Redlock算法** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | 高可靠性要求 |
| **WAIT命令** | ⭐⭐⭐ | ⭐⭐ | ⭐⭐ | 简单场景 |
| **Zookeeper** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | 强一致性要求 |
| **业务补偿** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | 实用主义 |

**方案1：Redlock算法**

```java
// 部署5个独立Redis（非主从）
Redis1: 192.168.1.1:6379
Redis2: 192.168.1.2:6379
Redis3: 192.168.1.3:6379
Redis4: 192.168.1.4:6379
Redis5: 192.168.1.5:6379

// 加锁流程
int successCount = 0;
for (Redis redis : allRedis) {
    if (redis.set(key, value, NX, EX, TTL) == OK) {
        successCount++;
    }
}

// 超过半数才算成功
if (successCount >= 3) {
    return true;
} else {
    releaseAllLocks();
    return false;
}
```

**为什么有效？**
- 即使2个Redis宕机，仍有3个存活
- 攻击者无法同时拿到3个Redis的锁
- 容忍N/2-1个节点故障

**方案2：WAIT命令**

```java
// 加锁后等待同步
jedis.set(lockKey, lockValue, SetParams.setParams().nx().ex(10));

// 等待至少1个从节点确认（超时1000ms）
Long replicas = jedis.waitReplicas(1, 1000);

if (replicas < 1) {
    jedis.del(lockKey);  // 同步失败，释放锁
    return false;
}
```

**方案3：业务补偿（实用）**

```java
// 数据库唯一索引兜底
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"order_no"})
})

// 乐观锁版本号
UPDATE stock SET count = count - 1, version = version + 1
WHERE product_id = ? AND count > 0 AND version = ?

// 定期对账修复
```

**追问：Redlock算法有什么争议？**

答：
```
Martin Kleppmann的批评：

1. 时钟跳跃问题
   - 假设Redis1时钟快了10秒
   - Client A 在Redis1加锁，TTL=10s，但实际立即过期
   - Client B 也能在Redis1加锁
   - 两个客户端都超过半数 ❌

2. GC暂停问题
   - Client A 加锁后，发生Full GC暂停15秒
   - 锁过期，Client B 加锁
   - Client A 恢复，误以为自己还持有锁

Antirez（Redis作者）的反驳：
   - 时钟跳跃是运维问题，应该用NTP同步
   - GC暂停也影响其他分布式协议
   - Redlock是实用主义方案，不追求完美

结论：
   - 对一致性要求极高：用Zookeeper/etcd（CP系统）
   - 对性能要求高：用Redis（AP系统）+ 业务兜底
```

**评分标准：**
- ✅ 说出主从同步问题（2分）
- ✅ 提出Redlock方案（4分）
- ✅ 说出WAIT命令（2分）
- ✅ 考虑业务补偿（2分）
- ✅ 答对Redlock争议（10分）

---

### Q9: 如何实现可重入锁？

**标准答案：**

**什么是可重入？**
```
同一个线程可以多次获取同一把锁
```

**为什么需要可重入？**
```java
public void methodA() {
    lock.lock();
    try {
        // 调用methodB
        methodB();
    } finally {
        lock.unlock();
    }
}

public void methodB() {
    lock.lock();  // 如果不可重入，这里会死锁
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}
```

**实现方案：使用Hash结构**

**数据结构：**
```redis
HSET lock_key {uuid:threadId} {count}

示例：
HGETALL myLock
1) "uuid-abc-123:thread-456"
2) "3"  ← 重入了3次
```

**加锁Lua脚本：**
```lua
-- 1. 锁不存在，创建锁
if (redis.call('exists', KEYS[1]) == 0) then
    redis.call('hincrby', KEYS[1], ARGV[2], 1);  -- count = 1
    redis.call('expire', KEYS[1], ARGV[1]);
    return nil;
end;

-- 2. 锁存在且是当前线程，重入
if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then
    redis.call('hincrby', KEYS[1], ARGV[2], 1);  -- count +1
    redis.call('expire', KEYS[1], ARGV[1]);
    return nil;
end;

-- 3. 锁被其他线程持有
return redis.call('pttl', KEYS[1]);
```

**解锁Lua脚本：**
```lua
-- 1. 验证所有权
if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then
    return nil;
end;

-- 2. count -1
local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1);

-- 3. 判断是否完全释放
if (counter > 0) then
    redis.call('expire', KEYS[1], ARGV[2]);  -- 还有重入，刷新TTL
    return 0;
else
    redis.call('del', KEYS[1]);              -- count=0，删除锁
    return 1;
end;
```

**对比普通锁：**

| 特性 | 普通锁 | 可重入锁 |
|------|-------|---------|
| **数据结构** | String | Hash |
| **存储内容** | value=uuid | field=uuid:threadId, value=count |
| **重入支持** | ❌ | ✅ |
| **实现复杂度** | 简单 | 中等 |

**追问：Redisson的可重入锁如何实现？**

答：
```java
// Redisson的RLock接口
public interface RLock extends Lock {
    void lock();
    void lockInterruptibly();
    boolean tryLock(long time, TimeUnit unit);
    void unlock();

    // 获取持有次数
    int getHoldCount();

    // 是否被当前线程持有
    boolean isHeldByCurrentThread();
}

// 底层使用Hash + Lua脚本
// 线程标识：UUID + ThreadId
// 重入计数：HINCRBY
```

**评分标准：**
- ✅ 说出使用Hash（3分）
- ✅ 写出Lua脚本（4分）
- ✅ 解释重入逻辑（3分）
- ✅ 答对Redisson（10分）

---

### Q10: 如何实现公平锁？

**标准答案：**

**什么是公平锁？**
```
多个客户端按照请求顺序获取锁（FIFO）
```

**为什么Redis锁默认不公平？**
```
多个客户端同时执行SET NX
谁先到达Redis谁获取锁
网络延迟导致无序
```

**实现方案：基于ZSet实现队列**

```java
/**
 * 公平锁实现
 */
public boolean fairLock(String lockKey, String clientId, long timeout) {
    Jedis jedis = getJedis();
    String queueKey = "lock:queue:" + lockKey;

    // 1. 加入等待队列（score=时间戳）
    long score = System.currentTimeMillis();
    jedis.zadd(queueKey, score, clientId);

    long startTime = System.currentTimeMillis();

    while (true) {
        // 2. 检查是否轮到自己（队列第一个）
        Set<String> range = jedis.zrange(queueKey, 0, 0);

        if (range.contains(clientId)) {
            // 3. 轮到自己，尝试加锁
            SetParams params = SetParams.setParams().nx().ex(10);
            String result = jedis.set(lockKey, clientId, params);

            if ("OK".equals(result)) {
                // 加锁成功，移出队列
                jedis.zrem(queueKey, clientId);
                return true;
            }
        }

        // 4. 检查超时
        if (System.currentTimeMillis() - startTime > timeout) {
            jedis.zrem(queueKey, clientId);
            return false;
        }

        // 5. 短暂休眠
        Thread.sleep(100);
    }
}
```

**数据结构：**
```redis
# 等待队列
ZADD lock:queue:order_lock
  1705200001000 "client_1"
  1705200002000 "client_2"
  1705200003000 "client_3"

# 队列第一个client_1轮到获取锁
```

**优点：**
- 保证FIFO顺序
- 避免饥饿

**缺点：**
- 性能开销大（轮询）
- 复杂度高

**追问：生产环境需要公平锁吗？**

答：
```
大多数情况不需要：
1. 性能优先（非公平锁更快）
2. 锁持有时间短（饥饿概率低）
3. 重试机制可缓解（指数退避）

需要公平锁的场景：
1. 任务队列（需要FIFO）
2. 票务系统（先到先得）
3. 限流场景（公平分配）
```

**评分标准：**
- ✅ 说出使用ZSet（3分）
- ✅ 写出实现代码（4分）
- ✅ 分析优缺点（3分）
- ✅ 答对追问（10分）

---

## 三、高级题（⭐⭐⭐）

### Q11: 详细说说Redlock算法的实现原理和争议？

**标准答案：**

详见前面Q8的完整答案，这里补充更多细节：

**Redlock算法5步流程：**

```
步骤1：获取当前时间戳（毫秒）
  startTime = System.currentTimeMillis()

步骤2：依次向N个Redis实例请求加锁
  for each redis in [redis1, redis2, ..., redisN]:
      set(key, value, NX, PX, TTL)
      if success:
          successCount++

步骤3：计算耗时
  elapsed = System.currentTimeMillis() - startTime

步骤4：计算有效时间（考虑时钟漂移）
  validityTime = TTL - elapsed - clockDrift
  clockDrift = TTL * 0.01  // 1%时钟漂移

步骤5：判断是否成功
  quorum = N / 2 + 1
  if successCount >= quorum AND validityTime > 0:
      return SUCCESS
  else:
      releaseAllLocks()
      return FAILURE
```

**为什么需要时钟漂移因子？**

```
场景：
- Redis1时钟快了1秒
- Redis2时钟正常
- Redis3时钟慢了1秒

影响：
- TTL=10s在Redis1上实际是9s
- TTL=10s在Redis3上实际是11s

解决：
- 减去1%作为安全边界
- validityTime = 10000 - elapsed - 100
```

**Martin Kleppmann的批评（详细版）：**

**批评1：时钟跳跃问题**
```
场景：
1. Client A 在t=0时向5个Redis加锁，成功3个
2. Redis1的时钟突然跳跃+15秒（NTP同步或管理员手动调整）
3. Redis1上的锁立即过期（TTL=10s）
4. Client B 在t=1时加锁，在Redis1和另外2个成功
5. A和B都持有锁 ❌

Antirez反驳：
- 这是运维问题，应该用ntpd平滑调整
- 使用-x选项：ntpd -x（slew mode，缓慢调整）
```

**批评2：GC暂停问题**
```
场景：
Time: 0s   → Client A 加锁成功（3/5实例）
Time: 1s   → Client A 发生Full GC，暂停15秒
Time: 11s  → 锁过期
Time: 11s  → Client B 加锁成功
Time: 16s  → Client A GC恢复，误以为还持有锁

Antirez反驳：
- GC暂停也影响Zookeeper等其他协议
- 应该监控GC，优化JVM参数
- 业务应该有超时检测
```

**批评3：网络分区问题**
```
场景：
1. Client A 向Redis[1,2,3,4,5]加锁
2. 网络分区：A只能访问[1,2,3]，B只能访问[3,4,5]
3. A认为自己加锁成功（3/5）
4. B也认为自己加锁成功（3/5）
5. 实际上Redis3给了两个人 ❌

Antirez反驳：
- 这是所有分布式算法的问题
- Paxos/Raft也有类似问题
- 应该用网络监控避免
```

**学术界的共识：**
```
Redlock不是完美方案：
1. 依赖时钟同步（弱保证）
2. 依赖TTL准确（弱保证）
3. 无法处理GC暂停

何时使用Redlock：
✅ 对性能要求高
✅ 对一致性要求中等
✅ 有运维保障（时钟同步、GC监控）
✅ 有业务兜底（幂等、对账）

何时不用Redlock：
❌ 金融交易
❌ 强一致性要求
❌ 无法接受偶尔错误

更好的选择：
→ Zookeeper/etcd（CP系统，强一致）
→ Chubby（Google内部，基于Paxos）
```

**评分标准：**
- ✅ 说出5步流程（4分）
- ✅ 解释时钟漂移（2分）
- ✅ 说出争议（4分）
- ✅ 给出选择建议（10分）

---

### Q12: 讲讲你在项目中如何使用分布式锁的？（STAR法则）

**标准答案模板：**

**Situation（场景）：**
```
我们的电商系统秒杀场景
- 商品：iPhone 15 Pro，库存1000件
- 流量：预计10万人同时抢购
- 要求：零超卖，低延迟
```

**Task（任务）：**
```
设计分布式锁方案，防止库存超卖
```

**Action（行动）：**

```
第一阶段：单一锁方案（V1.0）

实现：
- 锁粒度：商品级别（lock:stock:product_123）
- 加锁 → 查库存 → 扣库存 → 创建订单 → 解锁
- TTL=5秒，防止死锁
- Lua脚本保证解锁原子性

问题：
- 性能瓶颈：所有请求串行，QPS<1000
- 用户体验差：大量超时

第二阶段：分段锁优化（V2.0）

实现：
- 将1000件库存分成10段，每段100件
- 每段独立加锁：lock:stock:product_123:0 ~ 9
- 用户随机选择分段

改进：
- 并发提升10倍：QPS>10000
- 锁等待时间降低90%

第三阶段：无锁方案（V3.0 最终版）

实现：
- 放弃分布式锁
- 直接使用Redis DECR原子操作
- Lua脚本：扣库存 + 创建预订单

代码：
```lua
-- 原子扣库存
local stock = redis.call('get', KEYS[1])
if tonumber(stock) > 0 then
    redis.call('decr', KEYS[1])
    redis.call('lpush', KEYS[2], ARGV[1])  -- 预订单队列
    return 1
else
    return 0
end
```

异步处理：
- 预订单队列 → 消费者创建正式订单
- 失败重试 + 补偿机制

第四阶段：兜底方案

1. 数据库唯一索引
```sql
CREATE UNIQUE INDEX idx_order_no ON orders(order_no);
```

2. 乐观锁版本号
```sql
UPDATE stock SET count = count - 1, version = version + 1
WHERE product_id = ? AND version = ? AND count > 0
```

3. 定期对账
- 每小时对比Redis库存 vs 数据库订单
- 发现差异自动修复
```

**Result（结果）：**
```
性能：
- QPS：从1000提升到50000（提升50倍）
- 响应时间：从500ms降到20ms
- 成功率：99.9%

业务：
- 零超卖（对账结果）
- 用户体验好（秒级响应）
- 系统稳定（无宕机）

监控：
- Grafana实时监控库存、QPS、错误率
- 告警：库存<10 / 错误率>0.1% / 响应时间>100ms
```

**追问1：为什么最后不用分布式锁了？**

答：
```
1. 锁本身是瓶颈
   - 即使分段，仍有锁开销
   - Redis单线程，锁操作串行

2. Redis原子操作更合适
   - DECR本身就是原子的
   - 性能更高，无锁开销

3. 异步化处理
   - 扣库存和创建订单解耦
   - 提高吞吐量

4. 多层兜底
   - 数据库唯一索引
   - 乐观锁
   - 定期对账
```

**追问2：如果Redis宕机怎么办？**

答：
```
1. 主从架构 + 哨兵
   - 自动故障转移
   - RTO < 30秒

2. 降级方案
   - 检测到Redis不可用
   - 降级到数据库锁（SELECT FOR UPDATE）
   - 性能下降但保证可用

3. 熔断限流
   - Sentinel限流
   - 超过阈值直接返回"繁忙"
   - 保护后端系统
```

**追问3：如何验证没有超卖？**

答：
```
1. 实时监控
   - Redis库存 vs 实际订单数
   - 每秒对比，差异告警

2. 定期对账
   - 每小时全量对比
   - 生成对账报告

3. 压测验证
   - 10万并发压测
   - 验证最终库存=初始库存-订单数

4. 生产验证
   - 灰度发布
   - 小流量验证
   - 逐步放量
```

**评分标准：**
- ✅ 使用STAR法则（2分）
- ✅ 方案演进过程（4分）
- ✅ 数据支撑（2分）
- ✅ 考虑兜底方案（2分）
- ✅ 答对追问（每个5分，共15分）

---

### Q13: Redis锁 vs Zookeeper锁，如何选择？

**标准答案：**

**核心对比表：**

| 维度 | Redis | Zookeeper | 备注 |
|------|-------|-----------|------|
| **理论基础** | 无（工程实现） | Paxos/ZAB协议 | ZK更严谨 |
| **CAP属性** | AP（可用性+分区容错） | CP（一致性+分区容错） | 核心区别 |
| **性能** | QPS 10万+ | QPS 1万左右 | Redis快10倍 |
| **一致性** | 最终一致 | 强一致 | ZK更可靠 |
| **实现复杂度** | 简单（SET命令） | 复杂（临时顺序节点） | Redis易用 |
| **锁释放** | 超时自动释放 | 连接断开释放 | 各有优劣 |
| **依赖** | 依赖TTL（时钟） | 不依赖时钟 | ZK更鲁棒 |
| **可重入** | 需要额外实现 | 天然支持（session） | ZK胜 |
| **公平性** | 默认不公平 | 可实现公平 | ZK胜 |
| **学习成本** | 低 | 高 | Redis易学 |
| **运维成本** | 低 | 高（需要ZK集群） | Redis胜 |

**Redis锁实现：**
```java
// 简单直接
SetParams params = SetParams.setParams().nx().ex(10);
String result = jedis.set(lockKey, lockValue, params);

if ("OK".equals(result)) {
    // 业务逻辑

    // 解锁（Lua脚本）
    unlock();
}
```

**Zookeeper锁实现：**
```java
// 基于临时顺序节点
InterProcessMutex lock = new InterProcessMutex(zkClient, "/locks/order");

try {
    lock.acquire();  // 阻塞获取

    // 业务逻辑

} finally {
    lock.release();
}
```

**ZK锁原理：**
```
/locks/order/
  ├── lock-0000000001  ← Client A创建（临时顺序节点）
  ├── lock-0000000002  ← Client B创建
  └── lock-0000000003  ← Client C创建

规则：
1. 序号最小的持有锁（lock-0000000001）
2. 其他节点watch前一个节点
3. 前一个节点删除时，下一个节点获取锁
4. 连接断开，节点自动删除（临时节点）
```

**为什么ZK是CP？**
```
写入流程（ZAB协议）：
1. Client → Leader
2. Leader → Follower（半数以上ACK）
3. Leader → Client（成功响应）

保证：
- 所有写入必须经过Leader
- 半数以上节点确认才返回
- 强一致性，但牺牲可用性（Leader选举期间不可写）
```

**为什么Redis是AP？**
```
主从复制（异步）：
1. Client → Master（立即返回）
2. Master → Slave（异步复制）

保证：
- 高可用（主节点宕机，从节点顶上）
- 但可能数据丢失（未同步的数据）
```

**选择建议：**

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| **秒杀/抢购** | Redis | 性能第一，允许短暂不一致，有兜底 |
| **库存扣减** | Redis + 数据库兜底 | 高并发，乐观锁兜底 |
| **定时任务** | Redis 或 ZK | 都可以，看现有技术栈 |
| **配置中心** | ZK | 需要强一致性 |
| **服务注册** | ZK 或 etcd | 需要CP保证 |
| **分布式协调** | ZK | 提供更多原语（watch、顺序节点） |
| **金融交易** | ZK 或 数据库锁 | 零容忍不一致 |
| **限流** | Redis | 性能要求高 |
| **缓存更新** | Redis | 临时性，性能优先 |

**实际项目经验：**
```
大多数互联网公司：
- 主要用Redis（性能 + 简单）
- 数据库唯一索引兜底
- 乐观锁补充
- 定期对账修复

强一致性场景：
- 配置中心：ZK / etcd
- 服务注册：ZK / Consul
- 分布式协调：ZK

建议：
1. 先评估一致性要求
   - 强一致 → ZK
   - 最终一致 → Redis

2. 再看性能要求
   - 高并发 → Redis
   - 中低并发 → ZK

3. 最后看运维成本
   - Redis运维简单
   - ZK需要专业团队
```

**追问：etcd和ZK如何选择？**

答：
```
etcd优势：
- 基于Raft协议（比Paxos易懂）
- HTTP/gRPC API（ZK是私有协议）
- 更好的容器化支持
- Kubernetes默认使用

ZK优势：
- 更成熟（2011 vs 2013）
- 生态更丰富
- 更多大厂案例

选择：
- 新项目 + Kubernetes → etcd
- 老项目 + 传统部署 → ZK
```

**评分标准：**
- ✅ 说出CAP属性（3分）
- ✅ 对比性能/一致性（3分）
- ✅ 给出选择建议（4分）
- ✅ 答对追问（10分）

---

## 四、专家题（⭐⭐⭐⭐）

### Q14: 设计一个分布式锁框架，需要考虑哪些方面？

**标准答案：**

这是一个开放式架构设计题，考察综合能力。

**核心功能需求：**

```
1. 基础功能
   ✅ 加锁/解锁
   ✅ 自动续期（看门狗）
   ✅ 阻塞/非阻塞获取
   ✅ 可重入

2. 高级功能
   ✅ 公平锁/非公平锁
   ✅ 读写锁
   ✅ 信号量
   ✅ 倒计时门栓

3. 可靠性
   ✅ 主从切换容错
   ✅ 网络异常处理
   ✅ 时钟漂移处理

4. 可观测性
   ✅ 监控指标
   ✅ 日志埋点
   ✅ 链路追踪
```

**架构设计：**

```
┌─────────────────────────────────────────┐
│          应用层（Application）          │
│  @DistributedLock 注解                  │
│  LockTemplate 模板方法                  │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────┴──────────────────────┐
│         核心层（Core）                   │
│  - LockManager: 锁管理器                │
│  - WatchDogManager: 看门狗管理          │
│  - LockStrategy: 锁策略（Redlock/ZK）   │
│  - RetryPolicy: 重试策略                │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────┴──────────────────────┐
│        适配层（Adapter）                 │
│  - RedisAdapter                          │
│  - ZookeeperAdapter                      │
│  - EtcdAdapter                           │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────┴──────────────────────┐
│       存储层（Storage）                  │
│  - Redis Cluster                         │
│  - Zookeeper Ensemble                    │
│  - Etcd Cluster                          │
└──────────────────────────────────────────┘
```

**关键设计点：**

**1. 接口设计：**
```java
public interface DistributedLock {
    // 尝试加锁（非阻塞）
    boolean tryLock();

    // 阻塞加锁（带超时）
    boolean lock(long timeout, TimeUnit unit) throws InterruptedException;

    // 解锁
    void unlock();

    // 是否被当前线程持有
    boolean isHeldByCurrentThread();

    // 获取重入次数
    int getHoldCount();

    // 强制释放
    void forceUnlock();
}
```

**2. 配置设计：**
```yaml
distributed-lock:
  # 存储类型
  type: redis  # redis/zookeeper/etcd

  # Redis配置
  redis:
    mode: cluster  # single/sentinel/cluster
    nodes:
      - 192.168.1.1:6379
      - 192.168.1.2:6379
    timeout: 3000
    pool:
      max-total: 100
      max-idle: 50

  # 锁配置
  lock:
    # 默认过期时间
    default-ttl: 30s

    # 看门狗配置
    watchdog:
      enabled: true
      renewal-interval: 10s  # TTL/3

    # 重试策略
    retry:
      max-attempts: 3
      initial-interval: 100ms
      multiplier: 2  # 指数退避
      max-interval: 2s

    # Redlock配置
    redlock:
      enabled: false
      quorum: 3  # N/2+1
      clock-drift-factor: 0.01

  # 监控配置
  metrics:
    enabled: true
    export: prometheus
```

**3. 看门狗设计：**
```java
public class WatchDogManager {
    // 每个锁对应一个看门狗任务
    private final Map<String, ScheduledFuture<?>> watchDogs = new ConcurrentHashMap<>();

    // 调度器（共享线程池）
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(10, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "watchdog-thread");
                t.setDaemon(true);
                return t;
            }
        });

    /**
     * 启动看门狗
     */
    public void startWatchDog(String lockKey, Runnable renewalTask, long interval) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            () -> {
                try {
                    renewalTask.run();
                } catch (Exception e) {
                    logger.error("WatchDog renewal failed: {}", lockKey, e);
                    // 告警
                    alertManager.alert("WatchDog失败", lockKey);
                }
            },
            interval,
            interval,
            TimeUnit.MILLISECONDS
        );

        watchDogs.put(lockKey, future);
    }

    /**
     * 停止看门狗
     */
    public void stopWatchDog(String lockKey) {
        ScheduledFuture<?> future = watchDogs.remove(lockKey);
        if (future != null) {
            future.cancel(true);
        }
    }
}
```

**4. 重试策略设计：**
```java
public interface RetryPolicy {
    boolean shouldRetry(int attemptNumber);
    long getRetryInterval(int attemptNumber);
}

// 指数退避
public class ExponentialBackoffRetry implements RetryPolicy {
    private final int maxAttempts;
    private final long initialInterval;
    private final double multiplier;
    private final long maxInterval;

    @Override
    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    @Override
    public long getRetryInterval(int attemptNumber) {
        long interval = (long) (initialInterval * Math.pow(multiplier, attemptNumber - 1));
        return Math.min(interval, maxInterval);
    }
}
```

**5. 监控指标设计：**
```java
public class LockMetrics {
    // Prometheus指标
    private final Counter lockAcquireTotal;      // 加锁总次数
    private final Counter lockAcquireSuccess;    // 加锁成功次数
    private final Counter lockAcquireFailed;     // 加锁失败次数
    private final Histogram lockHoldTime;        // 锁持有时间分布
    private final Histogram lockWaitTime;        // 锁等待时间分布
    private final Gauge activeLocks;             // 当前持有锁数量

    public void recordLockAcquire(boolean success, long waitTime) {
        lockAcquireTotal.inc();
        if (success) {
            lockAcquireSuccess.inc();
        } else {
            lockAcquireFailed.inc();
        }
        lockWaitTime.observe(waitTime);
    }

    public void recordLockHold(long holdTime) {
        lockHoldTime.observe(holdTime);
    }
}
```

**6. 注解支持：**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    // 锁key（支持SpEL表达式）
    String key();

    // 过期时间
    long expire() default 30;

    // 时间单位
    TimeUnit unit() default TimeUnit.SECONDS;

    // 等待时间
    long waitTime() default 0;

    // 失败策略
    FailStrategy failStrategy() default FailStrategy.THROW_EXCEPTION;

    enum FailStrategy {
        THROW_EXCEPTION,  // 抛异常
        RETURN_NULL,      // 返回null
        RETURN_DEFAULT    // 返回默认值
    }
}

// 使用示例
@DistributedLock(
    key = "'order:' + #orderId",
    expire = 10,
    waitTime = 5000,
    failStrategy = FailStrategy.THROW_EXCEPTION
)
public Order createOrder(String orderId, OrderRequest request) {
    // 业务逻辑
}
```

**7. 测试设计：**
```java
// 单元测试
@Test
public void testLock() {
    DistributedLock lock = lockManager.getLock("test_lock");
    assertTrue(lock.tryLock());
    assertTrue(lock.isHeldByCurrentThread());
    lock.unlock();
}

// 并发测试
@Test
public void testConcurrency() throws InterruptedException {
    int threadCount = 100;
    AtomicInteger counter = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
        new Thread(() -> {
            DistributedLock lock = lockManager.getLock("test_lock");
            if (lock.tryLock()) {
                try {
                    counter.incrementAndGet();
                } finally {
                    lock.unlock();
                }
            }
            latch.countDown();
        }).start();
    }

    latch.await();
    assertEquals(1, counter.get());  // 只有一个线程成功
}

// 压力测试
@Test
public void stressTest() {
    // JMH benchmark
}
```

**8. 故障演练：**
```java
// Chaos Engineering
public class LockChaosTest {
    @Test
    public void testRedisDown() {
        // 模拟Redis宕机
        stopRedis();

        // 验证降级逻辑
        DistributedLock lock = lockManager.getLock("test_lock");
        assertFalse(lock.tryLock());

        // 验证告警
        verify(alertManager).alert(eq("Redis不可用"));
    }

    @Test
    public void testNetworkPartition() {
        // 模拟网络分区
        simulateNetworkPartition();

        // 验证Redlock行为
    }

    @Test
    public void testClockSkew() {
        // 模拟时钟漂移
        adjustSystemClock(+10000);  // +10秒

        // 验证锁行为
    }
}
```

**评分标准：**
- ✅ 架构分层合理（4分）
- ✅ 接口设计良好（3分）
- ✅ 考虑可观测性（3分）
- ✅ 考虑故障演练（3分）
- ✅ 代码示例完整（3分）
- ✅ 总体设计优秀（10分）

---

### Q15: 如果让你设计一个分布式锁的性能测试方案，你会怎么做？

**标准答案：**

详见后续创建的性能测试代码文件。

---

## 五、场景设计题

### Q16: 设计一个秒杀系统的分布式锁方案

详见 `DistributedLockCases.java` 中的秒杀案例

---

### Q17: 设计一个分布式定时任务的防重方案

详见 `DistributedLockCases.java` 中的定时任务案例

---

## 六、对比题

### Q18: 悲观锁 vs 乐观锁 vs 分布式锁？

**标准答案：**

**悲观锁（Pessimistic Lock）：**
```sql
-- 数据库行锁
SELECT * FROM stock WHERE product_id = 123 FOR UPDATE;
UPDATE stock SET count = count - 1 WHERE product_id = 123;
COMMIT;
```

**特点：**
- 假设冲突一定发生
- 先加锁再操作
- 适合写多读少

**乐观锁（Optimistic Lock）：**
```sql
-- 版本号机制
SELECT count, version FROM stock WHERE product_id = 123;

UPDATE stock
SET count = count - 1, version = version + 1
WHERE product_id = 123 AND version = 100;  -- 版本号匹配才更新

-- 如果影响行数=0，说明冲突，重试
```

**特点：**
- 假设冲突很少
- 先操作后验证
- 适合读多写少

**分布式锁（Distributed Lock）：**
```java
// Redis分布式锁
SET lock:stock:123 uuid-abc NX EX 10

if success:
    // 业务逻辑
    unlock()
```

**特点：**
- 跨进程/跨机器
- 控制并发访问
- 适合分布式系统

**对比表：**

| 维度 | 悲观锁 | 乐观锁 | 分布式锁 |
|------|-------|--------|---------|
| **粒度** | 数据库行/表 | 数据库记录 | 业务资源 |
| **冲突假设** | 一定冲突 | 很少冲突 | 需要互斥 |
| **性能** | 低（锁等待） | 高（无锁） | 中（网络开销） |
| **实现** | SELECT FOR UPDATE | 版本号 | Redis/ZK |
| **适用** | 写多场景 | 读多场景 | 分布式系统 |
| **回滚** | 事务回滚 | 重试 | 手动释放 |

**选择建议：**
```
单机环境：
  写多 → 悲观锁
  读多 → 乐观锁

分布式环境：
  需要互斥 → 分布式锁
  可以重试 → 乐观锁

组合使用（最佳实践）：
  分布式锁（控制并发） + 乐观锁（数据库兜底）
```

---

## 七、代码题

### Q19: 手写一个简单的Redis分布式锁

详见项目中的 `DistributedLock.java`

---

### Q20: 手写一个可重入分布式锁

详见项目中的 `ReentrantDistributedLock.java`

---

## 八、开放题

### Q21: Redis分布式锁在高并发场景下的优化思路？

**答案要点：**

1. **减小锁粒度**
   - 商品级别 → 商品+分段级别

2. **分段锁**
   - 将库存分成N段
   - 并发提升N倍

3. **无锁化**
   - 使用Redis原子操作（DECR）
   - 避免锁开销

4. **异步化**
   - 扣库存 + 创建预订单
   - 异步消费创建正式订单

5. **多级缓存**
   - 本地缓存 + Redis缓存
   - 减少网络开销

6. **业务优化**
   - 用户限流
   - 验证码
   - 分时段发售

---

## 九、压力面试题

### Q22: 如果我说Redis分布式锁不可靠，你怎么反驳？

**答案思路：**

**1. 承认确实有问题**
```
是的，Redis分布式锁确实不是完美的：
- 主从异步复制可能丢锁
- 时钟漂移有影响
- GC暂停有风险
```

**2. 说明大多数场景够用**
```
但对于大多数互联网业务：
- 性能是第一要务（QPS 10万+）
- 可以接受短暂不一致（秒级）
- 有业务兜底方案（幂等、对账）
```

**3. 给出完整方案**
```
我们的实践：
- Redis锁（一线防护）
- 数据库唯一索引（二线防护）
- 乐观锁版本号（三线防护）
- 定期对账（最后兜底）

结果：
- 零事故运行2年
- 日均处理1亿订单
- 对账差异<0.001%
```

**4. 给出选择建议**
```
如果对一致性要求极高：
- 金融交易 → Zookeeper / 数据库锁
- 配置中心 → etcd
- 其他场景 → Redis足够
```

---

## 总结

### 面试准备清单

**必背知识点：**
- [ ] SET NX EX命令
- [ ] Lua脚本原子性
- [ ] 三个核心属性
- [ ] 看门狗机制
- [ ] Redlock算法
- [ ] Redis vs Zookeeper

**必会代码：**
- [ ] 基础版分布式锁
- [ ] Lua解锁脚本
- [ ] 看门狗实现
- [ ] 可重入锁实现

**必练场景：**
- [ ] 秒杀防超卖
- [ ] 定时任务防重
- [ ] 接口幂等性

**加分项：**
- [ ] 了解Redlock争议
- [ ] 阅读过Redisson源码
- [ ] 有实际生产经验
- [ ] 考虑过故障演练

---

**祝面试顺利！** 🎯
