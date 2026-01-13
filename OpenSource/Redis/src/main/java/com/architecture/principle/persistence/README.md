# Redis 持久化模块 💾

## 📚 模块简介

本模块包含Redis两种持久化机制的实现：RDB（Redis Database）和AOF（Append Only File）。

**持久化是什么？**
> 将内存中的数据保存到磁盘，防止数据丢失，实现数据的持久化存储

---

## 📂 文件列表

| 文件名 | 类型 | 说明 |
|--------|------|------|
| **PersistenceRDB.java** | 代码 | RDB持久化实现（快照）|
| **PersistenceAOF.java** | 代码 | AOF持久化实现（日志）|

---

## 🎯 两种持久化方式

### RDB（Redis Database）- 快照持久化

**原理：**
> 定期将内存中的所有数据保存到一个RDB文件中（全量快照）

**特点：**
- ✅ 文件小，恢复快
- ✅ 性能影响小（fork子进程）
- ❌ 可能丢失最后一次快照后的数据
- ❌ 数据量大时fork较慢

**适用场景：**
- 数据备份
- 容灾恢复
- 可以接受分钟级数据丢失

### AOF（Append Only File）- 日志持久化

**原理：**
> 记录每一个写命令到日志文件中（增量日志）

**特点：**
- ✅ 数据更安全（最多丢失1秒数据）
- ✅ 可读性好（文本格式）
- ❌ 文件大，恢复慢
- ❌ 性能影响稍大

**适用场景：**
- 对数据安全要求高
- 不能接受数据丢失
- 需要审计日志

---

## 🚀 快速开始

### RDB持久化示例

```bash
# 编译
javac -d ../../../../../../target/classes PersistenceRDB.java

# 运行
java -cp ../../../../../../target/classes com.architecture.principle.persistence.PersistenceRDB
```

**代码示例：**
```java
// 创建RDB持久化实例
PersistenceRDB rdb = new PersistenceRDB("dump.rdb");

// 写入数据
rdb.set("user:1001", "张三");
rdb.set("user:1002", "李四");

// 方式1：同步保存（阻塞）
rdb.save();

// 方式2：异步保存（非阻塞，推荐）
rdb.bgsave();

// 从RDB文件恢复数据
PersistenceRDB rdb2 = new PersistenceRDB("dump.rdb");
rdb2.loadFromFile();
String value = rdb2.get("user:1001");  // "张三"
```

### AOF持久化示例

```bash
# 编译
javac -d ../../../../../../target/classes PersistenceAOF.java

# 运行
java -cp ../../../../../../target/classes com.architecture.principle.persistence.PersistenceAOF
```

**代码示例：**
```java
// 创建AOF持久化实例
// 三种同步策略：ALWAYS, EVERYSEC, NO
PersistenceAOF aof = new PersistenceAOF("appendonly.aof", SyncPolicy.EVERYSEC);

// 写入数据（自动记录到AOF文件）
aof.set("key1", "value1");
aof.set("key2", "value2");

// AOF重写（压缩日志文件）
aof.rewrite();

// 从AOF文件恢复数据
PersistenceAOF aof2 = new PersistenceAOF("appendonly.aof", SyncPolicy.EVERYSEC);
aof2.loadFromFile();
String value = aof2.get("key1");  // "value1"
```

---

## 💡 核心概念

### RDB核心概念

#### 1. SAVE命令（同步保存）
```java
public void save() {
    // 阻塞主线程
    // 直接在主线程中生成RDB文件
    // 适合：关闭服务器前手动保存
}
```

**特点：**
- ⚠️ 阻塞主线程，停止处理客户端请求
- ✅ 简单可靠
- ❌ 数据量大时耗时长

#### 2. BGSAVE命令（异步保存）
```java
public void bgsave() {
    // 非阻塞
    // fork子进程生成RDB文件
    // 适合：生产环境定期备份
}
```

**特点：**
- ✅ 不阻塞主线程
- ✅ 适合大数据量
- ⚠️ fork时短暂阻塞

#### 3. RDB文件格式
```
[REDIS][版本][数据库][键值对...][校验和]

示例内容：
- 所有键值对的快照
- 包含数据类型、过期时间等元信息
```

### AOF核心概念

#### 1. 三种同步策略

**ALWAYS（最安全）**
```java
SyncPolicy.ALWAYS
// 每个写命令都立即fsync到磁盘
// 优点：最安全，最多丢失1条命令
// 缺点：性能最差
```

**EVERYSEC（推荐）**
```java
SyncPolicy.EVERYSEC
// 每秒fsync一次
// 优点：性能好，最多丢失1秒数据
// 缺点：可能丢失1秒数据
```

**NO（最快）**
```java
SyncPolicy.NO
// 由操作系统决定何时fsync
// 优点：性能最好
// 缺点：可能丢失较多数据（几十秒）
```

#### 2. AOF重写
```java
public void rewrite() {
    // 问题：AOF文件会越来越大
    // 解决：重写AOF文件，去除冗余命令

    // 示例：
    // 原AOF：
    //   SET key1 value1
    //   SET key1 value2
    //   SET key1 value3

    // 重写后：
    //   SET key1 value3
}
```

**触发条件：**
- 文件大小超过阈值（如64MB）
- 文件大小是上次重写后的2倍

#### 3. AOF文件格式
```
*3        # 3个参数
$3        # 第1个参数长度3
SET       # 命令
$4        # 第2个参数长度4
key1      # 键
$6        # 第3个参数长度6
value1    # 值
```

---

## 📊 RDB vs AOF 对比

### 详细对比表

| 对比项 | RDB | AOF |
|-------|-----|-----|
| **文件大小** | 小（压缩） | 大（文本） |
| **恢复速度** | 快 | 慢 |
| **数据安全性** | 低（可能丢失分钟级数据） | 高（最多丢失1秒） |
| **性能影响** | 小（定期） | 稍大（持续） |
| **文件可读性** | 否（二进制） | 是（文本） |
| **适用场景** | 备份、容灾 | 对数据要求高 |

### 性能对比

**写入性能：**
```
RDB: 几乎无影响（异步）
AOF ALWAYS: 50% 性能下降
AOF EVERYSEC: 10% 性能下降
AOF NO: 5% 性能下降
```

**恢复性能：**
```
RDB: 1GB数据约需10秒
AOF: 1GB数据约需1分钟（取决于命令数量）
```

### 数据安全性对比

**场景：Redis突然宕机**

```
RDB:
  - 丢失最后一次快照后的所有数据
  - 如果每5分钟快照，最多丢失5分钟数据

AOF ALWAYS:
  - 最多丢失1条命令

AOF EVERYSEC:
  - 最多丢失1秒数据

AOF NO:
  - 可能丢失几十秒数据
```

---

## 🎯 使用场景

### 场景1：只用RDB
```
适合：
- 对数据完整性要求不高
- 可以接受分钟级数据丢失
- 需要快速备份和恢复

示例：
- 缓存服务器
- 会话存储
- 数据分析（可重新计算）
```

### 场景2：只用AOF
```
适合：
- 对数据完整性要求高
- 不能接受数据丢失
- 需要审计日志

示例：
- 订单系统
- 支付系统
- 用户数据存储
```

### 场景3：RDB + AOF（推荐）
```
推荐配置：
- RDB：每小时或每天备份
- AOF：EVERYSEC策略
- 混合持久化：AOF包含RDB快照

优势：
- 快速恢复（优先用RDB）
- 数据安全（AOF补充）
- 最佳平衡
```

---

## 🔧 配置建议

### RDB配置
```bash
# redis.conf

# 自动保存策略
save 900 1      # 900秒内至少1个key变化，保存
save 300 10     # 300秒内至少10个key变化，保存
save 60 10000   # 60秒内至少10000个key变化，保存

# RDB文件名
dbfilename dump.rdb

# 保存路径
dir /var/lib/redis

# 压缩
rdbcompression yes

# 校验
rdbchecksum yes
```

### AOF配置
```bash
# redis.conf

# 开启AOF
appendonly yes

# AOF文件名
appendfilename "appendonly.aof"

# 同步策略
appendfsync everysec  # always / everysec / no

# AOF重写
auto-aof-rewrite-percentage 100  # 文件大小是上次2倍时重写
auto-aof-rewrite-min-size 64mb   # 最小64MB才重写

# 混合持久化（Redis 4.0+）
aof-use-rdb-preamble yes
```

---

## 🔍 常见问题

### Q1: RDB的fork会阻塞吗？
```
答案：会短暂阻塞

过程：
1. fork子进程 - 短暂阻塞（几毫秒到几百毫秒）
2. 子进程生成RDB - 不阻塞
3. 传输RDB - 不阻塞

影响因素：
- 内存大小：内存越大，fork越慢
- 操作系统：Linux的Copy-On-Write优化

优化：
- 控制Redis实例大小（不超过10GB）
- 使用更快的CPU
```

### Q2: AOF文件损坏怎么办？
```
问题：Redis异常关闭，AOF文件可能损坏

解决：
# 检查AOF文件
redis-check-aof appendonly.aof

# 修复AOF文件
redis-check-aof --fix appendonly.aof

注意：
- 修复可能丢失部分数据
- 建议先备份原文件
```

### Q3: 如何选择持久化策略？
```
决策树：

1. 能接受数据丢失吗？
   ├─ 是 → 只用RDB
   └─ 否 → 继续

2. 性能要求高吗？
   ├─ 是 → RDB + AOF(EVERYSEC)
   └─ 否 → RDB + AOF(ALWAYS)

3. 数据量大吗？
   ├─ 是 → 考虑主从复制 + 持久化
   └─ 否 → RDB + AOF(EVERYSEC)

推荐：
生产环境 → RDB + AOF(EVERYSEC)
```

### Q4: 混合持久化是什么？
```
概念：
AOF文件包含RDB快照 + 增量AOF命令

格式：
[RDB快照][AOF命令1][AOF命令2]...

优势：
- 恢复速度快（先加载RDB）
- 数据完整（AOF补充）
- 文件较小（减少重写）

配置：
aof-use-rdb-preamble yes  # Redis 4.0+
```

---

## 🎓 实战练习

### 练习1：测试RDB性能
```java
// 写入100万条数据，测试save和bgsave的性能差异
long start = System.currentTimeMillis();
for (int i = 0; i < 1000000; i++) {
    rdb.set("key" + i, "value" + i);
}
rdb.bgsave();  // 或 rdb.save()
long end = System.currentTimeMillis();
System.out.println("耗时: " + (end - start) + "ms");
```

### 练习2：比较三种AOF策略
```java
// 测试ALWAYS、EVERYSEC、NO三种策略的性能
PersistenceAOF aof = new PersistenceAOF("test.aof", SyncPolicy.EVERYSEC);

long start = System.currentTimeMillis();
for (int i = 0; i < 10000; i++) {
    aof.set("key" + i, "value" + i);
}
long end = System.currentTimeMillis();
System.out.println("EVERYSEC耗时: " + (end - start) + "ms");
```

### 练习3：模拟AOF重写
```java
// 1. 写入大量数据
for (int i = 0; i < 1000; i++) {
    aof.set("key" + (i % 100), "value" + i);  // 重复写入
}

// 2. 查看AOF文件大小
File file = new File("appendonly.aof");
System.out.println("重写前: " + file.length());

// 3. 执行重写
aof.rewrite();

// 4. 查看重写后大小
System.out.println("重写后: " + file.length());
```

---

## 📈 性能优化

### RDB优化
```
1. 控制实例大小
   - 单实例不超过10GB
   - 拆分为多个小实例

2. 优化fork性能
   - 关闭Transparent Huge Pages
   - 使用更快的CPU

3. 合理设置保存策略
   - 不要太频繁（增加IO压力）
   - 不要太少（增加数据丢失风险）
```

### AOF优化
```
1. 选择合适的同步策略
   - 生产环境推荐EVERYSEC

2. 定期重写AOF
   - 控制文件大小
   - 提高恢复速度

3. 使用SSD
   - 减少fsync延迟
   - 提高写入性能

4. 开启混合持久化
   - 减少AOF文件大小
   - 提高恢复速度
```

---

## 🔗 相关资源

### 官方文档
- [Redis Persistence](https://redis.io/topics/persistence)
- [Redis RDB](https://redis.io/docs/manual/persistence/#rdb-advantages)
- [Redis AOF](https://redis.io/docs/manual/persistence/#aof-advantages)

### 推荐阅读
- 《Redis设计与实现》- 第10章（RDB持久化）
- 《Redis设计与实现》- 第11章（AOF持久化）

### 源码位置
- Redis源码: `redis/src/rdb.c`
- Redis源码: `redis/src/aof.c`

---

## 📞 使用建议

### 生产环境推荐配置
```bash
# 开启RDB
save 900 1
save 300 10
save 60 10000
rdbcompression yes

# 开启AOF
appendonly yes
appendfsync everysec

# 混合持久化
aof-use-rdb-preamble yes

# AOF重写
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
```

### 监控指标
```
- RDB最后保存时间
- AOF文件大小
- AOF重写次数
- fork耗时
- 磁盘IO使用率
```

---

**开始探索Redis持久化机制吧！** 💾

建议先运行代码示例，观察RDB和AOF的实际效果！
