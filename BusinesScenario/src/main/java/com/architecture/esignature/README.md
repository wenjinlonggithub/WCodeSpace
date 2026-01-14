# 场景九: 电子签名回调重复日志问题

## 快速导航

- [场景说明](场景说明_电子签名回调重复日志问题.md) - 详细的问题分析和解决方案
- [数据库脚本](database.sql) - 数据库表结构和索引
- [测试演示](#运行测试) - 如何运行测试代码

## 文件说明

```
esignature/
├── 场景说明_电子签名回调重复日志问题.md    # 详细场景分析
├── README.md                               # 本文件
├── database.sql                            # 数据库脚本
├── SignatureCallbackController.java        # 回调接口(包含问题代码和5种解决方案)
├── SignatureAsyncService.java              # 异步处理服务
├── SignatureLogService.java                # 日志记录服务
├── SignatureService.java                   # 签署状态服务
├── RedisDistributedLock.java               # Redis分布式锁
└── SignatureDuplicateLogTest.java          # 测试演示类
```

## 核心问题

医生完成电子签名后,易签宝回调业务系统的签署接口。系统使用了基于签署ID的Redis分布式锁,并通过异步程序处理签署状态更新和日志记录。

**问题现象: 同一次签署产生了两条签署日志!**

## 根本原因

```java
// 有问题的代码
String lockKey = "sign:lock:" + signatureId;
try {
    redisLock.tryLock(lockKey, 10, TimeUnit.SECONDS);

    // 提交异步任务
    asyncService.processSignature(dto);  // 异步执行

    return Result.success();
} finally {
    redisLock.unlock(lockKey);  // ❌ 立即释放锁!
}
```

**时序图:**
```
时间   请求A        异步任务A     请求B        异步任务B
 ├─→ 获取锁✓
 ├─→ 提交任务 ──→
 ├─→ 释放锁
 |                            ├─→ 获取锁✓
 |                            ├─→ 提交任务 ──→
 |                            ├─→ 释放锁
 |            ├─→ 记录日志✓               ├─→ 记录日志✓
 |                                              ↑
 |                                         重复日志!
```

**问题核心:** Redis锁只保护了同步部分,异步任务在锁释放后才执行,导致并发产生重复日志。

## 解决方案对比

| 方案 | 实现难度 | 可靠性 | 性能 | 推荐指数 | 适用场景 |
|------|---------|--------|------|---------|---------|
| 1. 数据库唯一约束 | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 所有场景(推荐) |
| 2. 锁覆盖异步任务 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | 强一致性要求 |
| 3. Token幂等性 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 防止回调重试 |
| 4. 状态机 | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | 复杂业务流程 |
| 5. 先查后插 | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | 重复请求频繁 |

## 推荐方案

### 方案1: 数据库唯一约束 (⭐⭐⭐⭐⭐ 推荐)

**数据库添加唯一索引:**
```sql
ALTER TABLE signature_log
ADD UNIQUE INDEX uk_signature_id (signature_id);
```

**业务代码捕获异常:**
```java
try {
    signatureLogMapper.insert(log);
} catch (DuplicateKeyException e) {
    log.warn("签署日志已存在,忽略: {}", signatureId);
}
```

**优点:**
- 简单可靠,数据库层面保证
- 性能好,不需要额外查询
- 天然支持并发
- 即使业务代码有bug也能兜底

**查看完整代码:**
- `SignatureCallbackController.handleCallbackWithUniqueConstraint()`
- `SignatureLogService.recordLogWithUniqueConstraint()`

## 运行测试

### 1. 编译项目

```bash
cd D:\develop\20\WCodeSpace\BusinesScenario
mvn clean compile
```

### 2. 运行测试类

```bash
# 方式1: 使用Maven
mvn exec:java -Dexec.mainClass="com.architecture.esignature.SignatureDuplicateLogTest"

# 方式2: 使用IDE
直接运行 SignatureDuplicateLogTest.main() 方法
```

### 3. 测试输出示例

```
================================================================================
电子签名回调重复日志问题 - 测试演示
================================================================================

--------------------------------------------------------------------------------
【测试1】重现问题: 有问题的实现
--------------------------------------------------------------------------------
场景: 易签宝同时发起2次回调 (模拟网络重试或并发)

结果验证:
  签署ID: SIGN_TEST_001
  日志数量: 2 (预期: 2条,因为有问题)

❌ 问题重现成功! 产生了重复日志!
原因: Redis锁在异步任务执行前就释放了,两个异步任务并发执行

日志明细:
  - SignatureLog{id='LOG_1736841234567_123', signatureId='SIGN_TEST_001', ...}
  - SignatureLog{id='LOG_1736841234678_456', signatureId='SIGN_TEST_001', ...}

--------------------------------------------------------------------------------
【测试2】解决方案1: 数据库唯一约束
--------------------------------------------------------------------------------
方案: 在signature_log表的signature_id字段添加唯一索引

结果验证:
  签署ID: SIGN_TEST_002
  日志数量: 1 (预期: 1条)

✅ 解决方案有效! 只产生了1条日志!
说明: 数据库唯一约束成功阻止了重复插入

优点:
  ✅ 简单可靠,数据库层面保证
  ✅ 性能好,不需要额外的查询
  ✅ 天然支持并发
  ✅ 即使业务代码有bug也能兜底

... (更多测试结果)
```

## 接口说明

项目提供了多个版本的回调接口,用于演示不同的实现方式:

| 接口路径 | 说明 | 是否有问题 |
|---------|------|----------|
| `/api/signature/callback/v1-buggy` | 有问题的实现 | ❌ 会产生重复日志 |
| `/api/signature/callback/v2-unique-constraint` | 数据库唯一约束 | ✅ 推荐 |
| `/api/signature/callback/v3-lock-async` | 锁覆盖异步任务 | ✅ 推荐 |
| `/api/signature/callback/v4-token` | Token幂等性 | ✅ 推荐 |
| `/api/signature/callback/v5-check-then-insert` | 先查后插 | ✅ 可用 |

### 测试接口

```bash
# 模拟易签宝回调
curl -X POST http://localhost:8080/api/signature/callback/v2-unique-constraint \
  -H "Content-Type: application/json" \
  -d '{
    "signatureId": "SIGN_20260114_001",
    "doctorId": "DOC_001",
    "doctorName": "张三医生",
    "signatureTime": 1736841234567
  }'

# 模拟并发回调测试
curl http://localhost:8080/api/signature/test/simulate-callback?signatureId=SIGN_TEST_999
```

## 数据库表结构

```sql
-- 签署主表
CREATE TABLE signature (
    signature_id VARCHAR(64) PRIMARY KEY,
    doctor_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_doctor_id (doctor_id)
);

-- 签署日志表
CREATE TABLE signature_log (
    id VARCHAR(64) PRIMARY KEY,
    signature_id VARCHAR(64) NOT NULL,
    doctor_id VARCHAR(64) NOT NULL,
    doctor_name VARCHAR(128) NOT NULL,
    operation_type VARCHAR(32) NOT NULL,
    create_time DATETIME NOT NULL,
    -- 关键: 唯一索引,防止重复
    UNIQUE INDEX uk_signature_id (signature_id)
);
```

## 监控和排查

### 1. 查询重复日志

```sql
-- 查找重复的签署日志
SELECT signature_id, COUNT(*) as cnt
FROM signature_log
GROUP BY signature_id
HAVING cnt > 1;
```

### 2. 添加监控告警

```java
@Aspect
@Component
public class SignatureLogAspect {

    @Around("execution(* SignatureLogService.recordLog(..))")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (DuplicateKeyException e) {
            // 发现重复插入,发送告警
            alertService.sendAlert("发现重复签署日志", signatureId);
            metricsService.incrementCounter("signature.log.duplicate");
            throw e;
        }
    }
}
```

### 3. 日志追踪

在整个调用链路中使用TraceId:

```java
String traceId = UUID.randomUUID().toString();
MDC.put("traceId", traceId);

log.info("收到签署回调: signatureId={}, traceId={}", signatureId, traceId);
```

## 最佳实践

### 多层防护策略 (推荐)

结合多种方案,构建纵深防御:

```
第1层: Token幂等性验证
  ↓  防止易签宝回调重试
第2层: Redis分布式锁
  ↓  防止并发请求
第3层: 业务状态检查
  ↓  先查后插,避免不必要的操作
第4层: 数据库唯一约束
  ↓  最后一道防线,数据库层面保证
```

## 性能优化

1. **批量查询在线状态** (如果需要)
   ```java
   List<String> signatureIds = ...;
   Map<String, Boolean> existsMap = logService.batchCheckExists(signatureIds);
   ```

2. **本地缓存**
   ```java
   @Cacheable(value = "signatureLog", key = "#signatureId", expire = 60)
   public boolean isLogExists(String signatureId) {
       ...
   }
   ```

3. **异步日志记录** (非关键路径)
   ```java
   @Async
   public void recordAuditLog(SignatureCallbackDTO dto) {
       // 审计日志可以异步记录
   }
   ```

## 常见问题

### Q1: 为什么不直接用synchronized?

A: synchronized只能锁住单个JVM内的线程,分布式环境下多个服务实例无法互斥。

### Q2: Redis分布式锁如果过期了怎么办?

A: 使用Redisson的看门狗机制,自动续期。或者设置足够长的超时时间。

### Q3: 数据库唯一约束影响性能吗?

A: 影响很小。唯一索引的查询和插入性能都很好,是最推荐的方案。

### Q4: 易签宝回调失败会重试多少次?

A: 需要查看易签宝的文档。通常会重试3-5次,间隔逐渐增加。

### Q5: 如果两个请求间隔很长,还会重复吗?

A: 如果使用数据库唯一约束,无论间隔多久都不会重复。

## 相关资料

- [分布式锁实现](../distributedlock/) - 场景二
- [接口幂等性设计](../idempotent/) - 场景五
- [Redis官方文档 - 分布式锁](https://redis.io/docs/manual/patterns/distributed-locks/)
- [MySQL官方文档 - 唯一索引](https://dev.mysql.com/doc/refman/8.0/en/create-index.html)

## 总结

这个场景展示了一个常见但容易被忽视的并发问题:**分布式锁的作用范围不足**。

**核心要点:**
1. ✅ 分布式锁要覆盖整个业务逻辑(包括异步任务)
2. ✅ 数据库唯一约束是最简单、最可靠的防线
3. ✅ 第三方回调要考虑重试机制
4. ✅ 多层防护,纵深防御
5. ✅ 完善的监控和日志追踪

**面试加分项:**
- 能清晰描述问题的根本原因(锁的范围不足)
- 能对比多种解决方案的优劣
- 能给出生产环境的最佳实践(多层防护)
- 能说明监控和排查方法
