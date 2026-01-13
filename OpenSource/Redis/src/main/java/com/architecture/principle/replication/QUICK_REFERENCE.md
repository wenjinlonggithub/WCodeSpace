# Redis 主从复制 - 快速参考卡 📋

## 🎯 一句话总结

**主从复制 = 主节点写，从节点自动同步，实现数据备份和读写分离**

---

## 📊 核心概念（必背3个）

### 1️⃣ Replication ID（复制ID）
```
是什么：UUID格式的唯一标识符
作用：标识一个数据集版本
何时变：主节点重启、从节点升级为主节点

示例：20336f56-ec82-46d4-b171-ce50b20ff2b1
```

### 2️⃣ Replication Offset（复制偏移量）
```
是什么：记录已复制的字节数
作用：判断主从同步进度
如何用：
  - 主节点Offset: 1000
  - 从节点Offset: 900
  → 从节点落后100字节
```

### 3️⃣ Replication Backlog（复制积压缓冲区）
```
是什么：环形缓冲区（默认1MB）
作用：保存最近的写命令，支持增量复制
何时满：超过1MB，最旧的命令被覆盖
```

---

## 🔄 两种复制方式

### 全量复制（FULLRESYNC）

**触发条件：**
- ✅ 首次连接：`PSYNC ? -1`
- ✅ ID不匹配：连接到新主节点
- ✅ Offset太旧：超出Backlog范围

**执行流程：**
```
1. 从节点发送：PSYNC ? -1
2. 主节点响应：+FULLRESYNC replid offset
3. 主节点生成：RDB快照
4. 主节点发送：RDB文件
5. 从节点加载：RDB数据
6. 主节点发送：缓冲区命令
7. 完成：进入命令传播模式
```

**时间开销：** O(N)，N为数据量

### 增量复制（CONTINUE）

**触发条件：**
- ✅ ID匹配：同一个主节点
- ✅ Offset有效：在Backlog范围内

**执行流程：**
```
1. 从节点发送：PSYNC replid offset
2. 主节点响应：+CONTINUE
3. 主节点发送：缺失的命令
4. 从节点执行：命令并更新Offset
5. 完成：进入命令传播模式
```

**时间开销：** O(M)，M为缺失命令数

---

## 📡 命令传播

**什么时候：** 复制完成后，持续进行

**怎么做：**
```
主节点每次写操作：
  1. 执行命令
  2. 更新Offset
  3. 记录到Backlog
  4. 异步发送给所有从节点
```

**特点：**
- ⚡ 异步传播，不等待确认
- 📦 批量发送，提高效率
- 🔁 持续进行，直到断开

---

## 🎨 运行命令速查

### 简化版
```bash
# Windows
.\run_simple_replication.bat

# Linux/Mac
javac -d target/classes -sourcepath src/main/java \
  src/main/java/com/architecture/principle/SimpleReplicationDemo.java
java -cp target/classes com.architecture.principle.SimpleReplicationDemo
```

### 完整版
```bash
# Windows
.\run_full_replication.bat

# Linux/Mac
javac -d target/classes -sourcepath src/main/java \
  src/main/java/com/architecture/principle/RedisReplication.java
java -cp target/classes com.architecture.principle.RedisReplication
```

---

## 💻 代码片段速查

### 主节点核心代码
```java
// 1. 初始化
this.replicationId = UUID.randomUUID().toString();
this.offset = 0;
this.backlog = new ReplicationBacklog(1MB);

// 2. 处理PSYNC
if (slaveReplId.equals("?") || slaveOffset == -1) {
    fullResync(slave);  // 全量
} else if (canPartialResync(slaveOffset)) {
    partialResync(slave, slaveOffset);  // 增量
}

// 3. 写命令时传播
data.put(key, value);
offset++;
propagateCommand("SET " + key + " " + value);
```

### 从节点核心代码
```java
// 1. 连接主节点
socket.connect(new InetSocketAddress(masterHost, masterPort));
out.println("PSYNC " + masterReplId + " " + offset);

// 2. 处理FULLRESYNC
if (response.startsWith("+FULLRESYNC")) {
    String[] parts = response.split(" ");
    masterReplId = parts[1];
    offset = Long.parseLong(parts[2]);
    receiveRDB();
}

// 3. 处理CONTINUE
if (response.equals("+CONTINUE")) {
    receiveMissingCommands();
}
```

---

## 🔍 问题排查速查

### Q: 为什么无法增量复制？

**可能原因：**
1. ❌ Replication ID不匹配 → 执行全量复制
2. ❌ Offset超出Backlog → 执行全量复制
3. ❌ Backlog被覆盖 → 增大Backlog

**解决方案：**
```bash
# 查看主节点信息
INFO replication

# 调整Backlog大小
repl-backlog-size = 断线时间(秒) × 每秒写入量(字节)
```

### Q: 主从数据不一致？

**可能原因：**
1. ⏰ 复制延迟（网络慢）
2. 🔄 命令还在传播中
3. 🐛 从节点执行失败

**检查方法：**
```java
// 比较Offset
masterOffset == slaveOffset  // 应该相等

// 比较数据量
master.getData().size() == slave.getData().size()
```

### Q: 全量复制太慢？

**优化方案：**
1. 📉 减少数据量：定期清理
2. 🚀 增加带宽：网络优化
3. ⚡ 无磁盘复制：减少IO
4. 🔗 级联复制：树形结构

---

## 📐 性能速查

### 时间复杂度
| 操作 | 复杂度 | 说明 |
|------|--------|------|
| 全量复制 | O(N) | N=数据量 |
| 增量复制 | O(M) | M=缺失命令数 |
| 命令传播 | O(K) | K=从节点数 |
| 查找命令 | O(M) | M=Backlog大小 |

### 空间复杂度
| 组件 | 空间 | 说明 |
|------|------|------|
| 数据存储 | O(N) | N=键值对数量 |
| Backlog | O(1) | 固定1MB |
| 从节点列表 | O(K) | K=从节点数 |

### 网络开销
| 场景 | 数据量 | 示例 |
|------|-------|------|
| 首次连接 | RDB + Backlog | 100MB + 1MB |
| 短暂断线 | 缺失命令 | 几KB |
| 长时间断线 | RDB + Backlog | 100MB + 1MB |

---

## 🎯 面试题速查

### 基础题（必会）

**Q1: 什么是主从复制？**
```
主从复制是Redis的数据备份和读写分离方案：
- 主节点：负责写操作
- 从节点：自动同步主节点数据，负责读操作
- 优势：高可用、负载分担、数据备份
```

**Q2: 全量复制和增量复制的区别？**
```
全量复制：
- 触发：首次连接、ID不匹配
- 传输：所有数据（RDB）
- 缺点：慢、占带宽

增量复制：
- 触发：短暂断线、Offset有效
- 传输：缺失命令
- 优点：快、省带宽
```

**Q3: Replication Offset的作用？**
```
作用：
1. 记录复制进度
2. 判断主从同步状态
3. 确定增量复制起点

示例：
主节点Offset: 1000
从节点Offset: 900
→ 从节点落后100字节
→ 需要传输900-1000的命令
```

### 进阶题（加分）

**Q4: Backlog满了会怎样？**
```
场景：
Backlog只有1MB，从节点断线很久，缺失命令>1MB

结果：
1. Backlog中最旧命令被覆盖
2. 无法找到从节点需要的命令
3. 退化为全量复制

解决：
根据实际调大Backlog：
repl-backlog-size = 断线时间 × 每秒写入量
```

**Q5: 主从数据一定一致吗？**
```
答案：不一定，有延迟

原因：
1. 命令传播是异步的
2. 网络有延迟
3. 从节点处理需要时间

结果：
主节点写入后，从节点可能还没收到
→ 读从节点会读到旧数据（最终一致性）

解决：
1. 读主节点（强一致性）
2. 使用WAIT命令（等待复制完成）
3. 监控lag值（复制延迟）
```

**Q6: 如何优化复制性能？**
```
优化方向：
1. 减少全量复制：
   - 调大Backlog
   - 避免主节点重启
   - 使用持久化

2. 优化网络：
   - 增加带宽
   - 减少网络跳转
   - 使用压缩

3. 减少数据量：
   - 定期清理
   - 设置过期时间
   - 使用合适的数据结构

4. 架构优化：
   - 无磁盘复制
   - 级联复制
   - 分片集群
```

---

## 📚 文档速查

### 快速查找

| 我想... | 看哪个文档 |
|---------|-----------|
| 快速入门 | README_Replication.md |
| 理解原理 | RedisReplication_Explanation.md |
| 看代码 | SimpleReplicationDemo.java |
| 深入研究 | RedisReplication.java |
| 查看测试 | REPLICATION_VERIFICATION.md |
| 项目总结 | COMPLETION_SUMMARY.md |
| 速查 | QUICK_REFERENCE.md（本文档）|

### 文档位置
```
D:\develop\20\WCodeSpace\OpenSource\Redis\src\main\java\com\architecture\principle\

├── SimpleReplicationDemo.java          # 简化版代码
├── RedisReplication.java               # 完整版代码
├── README_Replication.md               # 学习指南
├── RedisReplication_Explanation.md     # 详细解析
├── REPLICATION_VERIFICATION.md         # 验证报告
├── COMPLETION_SUMMARY.md               # 完成总结
└── QUICK_REFERENCE.md                  # 快速参考（本文档）
```

---

## ⚡ 常用命令速查

### Redis CLI命令
```bash
# 查看复制信息
INFO replication

# 配置从节点
REPLICAOF 主节点IP 端口

# 取消复制（升级为主节点）
REPLICAOF NO ONE

# 调整Backlog大小
CONFIG SET repl-backlog-size 10485760  # 10MB

# 等待复制完成
WAIT 1 1000  # 等待1个从节点，超时1秒
```

### Java代码命令
```java
// Jedis
jedis.set("key", "value");
jedis.get("key");

// 查看复制信息
String info = jedis.info("replication");

// 配置从节点
jedis.slaveof("127.0.0.1", 6379);

// 取消复制
jedis.slaveofNoOne();
```

---

## 🎓 学习路径速查

### 30分钟快速了解
```
1. 阅读本快速参考卡（10分钟）
2. 运行SimpleReplicationDemo（5分钟）
3. 观察输出，理解流程（15分钟）
```

### 2小时深入理解
```
1. 阅读README_Replication.md（30分钟）
2. 阅读SimpleReplicationDemo.java（60分钟）
3. 运行并修改代码测试（30分钟）
```

### 4小时完全掌握
```
1. 阅读RedisReplication_Explanation.md（90分钟）
2. 阅读RedisReplication.java（120分钟）
3. 对比两个版本差异（30分钟）
```

---

## 💡 记忆口诀

```
主从复制三要素：
  ID标识数据集
  Offset记进度
  Backlog存命令

两种复制方式：
  全量慢但全
  增量快但限

一个传播过程：
  异步不等待
  实时又高效
```

---

## 🔗 相关资源速查

### 官方文档
- https://redis.io/topics/replication
- https://redis.io/commands/psync

### 推荐书籍
- 《Redis设计与实现》第15章
- 《Redis深度历险》主从同步章节

### 源码位置
- redis/src/replication.c
- redis/src/rdb.c

---

**打印提示：** 建议打印本快速参考卡，随时查阅 📄

**更新时间：** 2026-01-13

**状态：** ✅ 完整可用

---

**快速学习，高效掌握！Happy Learning! 🚀**
