# MySQL 面试题汇总

## 基础篇

### 1. MySQL 的存储引擎有哪些？各有什么特点？

**InnoDB**（默认）：
- 支持事务（ACID）
- 支持行级锁
- 支持外键
- 支持崩溃恢复
- 适合高并发写操作

**MyISAM**：
- 不支持事务
- 支持表级锁
- 查询速度快
- 适合读多写少场景

**Memory**：
- 数据存储在内存中
- 速度快但数据易丢失
- 适合临时表

### 2. InnoDB 和 MyISAM 的区别？

| 特性 | InnoDB | MyISAM |
|------|--------|--------|
| 事务支持 | 支持 | 不支持 |
| 锁粒度 | 行级锁 | 表级锁 |
| 外键 | 支持 | 不支持 |
| 崩溃恢复 | 支持 | 不支持 |
| 全文索引 | 5.6+ 支持 | 支持 |
| 表空间 | 较大 | 较小 |

### 3. 什么是索引？索引的类型有哪些？

索引是帮助 MySQL 高效获取数据的数据结构。

**索引类型**：
- **B+Tree 索引**：最常用，适合范围查询
- **Hash 索引**：等值查询快，不支持范围查询
- **全文索引**：用于文本搜索
- **空间索引**：用于地理数据

**按功能分类**：
- 主键索引（PRIMARY KEY）
- 唯一索引（UNIQUE）
- 普通索引（INDEX）
- 组合索引（多列索引）

### 4. 什么是聚簇索引和非聚簇索引？

**聚簇索引**：
- 数据行和索引存储在一起
- InnoDB 的主键索引是聚簇索引
- 一个表只能有一个聚簇索引

**非聚簇索引**（二级索引）：
- 索引和数据分开存储
- 叶子节点存储主键值
- 需要回表查询完整数据

### 5. 什么是覆盖索引？

查询的列都在索引中，不需要回表查询。

```sql
-- 假设有索引 idx_name_age(name, age)
SELECT name, age FROM user WHERE name = 'Tom';  -- 覆盖索引
SELECT name, age, address FROM user WHERE name = 'Tom';  -- 需要回表
```

## 进阶篇

### 6. MySQL 的事务隔离级别有哪些？

1. **READ UNCOMMITTED**（读未提交）
   - 可能出现脏读、不可重复读、幻读

2. **READ COMMITTED**（读已提交）
   - 解决脏读
   - 可能出现不可重复读、幻读

3. **REPEATABLE READ**（可重复读，MySQL 默认）
   - 解决脏读、不可重复读
   - InnoDB 通过 MVCC 和间隙锁解决幻读

4. **SERIALIZABLE**（串行化）
   - 解决所有问题
   - 性能最差

### 7. 什么是 MVCC？

多版本并发控制（Multi-Version Concurrency Control）。

**实现原理**：
- 每行记录有隐藏字段：`DB_TRX_ID`（事务ID）、`DB_ROLL_PTR`（回滚指针）
- Undo Log 保存历史版本
- Read View 判断数据可见性

**优点**：
- 读不加锁，提高并发性能
- 实现一致性非锁定读

### 8. MySQL 的锁有哪些类型？

**按粒度分**：
- 表级锁：锁整张表
- 行级锁：锁特定行
- 页级锁：锁数据页

**按类型分**：
- 共享锁（S锁）：读锁，多个事务可同时持有
- 排他锁（X锁）：写锁，独占

**InnoDB 特有**：
- 记录锁（Record Lock）
- 间隙锁（Gap Lock）
- 临键锁（Next-Key Lock）= 记录锁 + 间隙锁

### 9. 什么是死锁？如何避免？

两个或多个事务互相等待对方释放锁。

**避免方法**：
- 按相同顺序访问资源
- 缩短事务时间
- 降低隔离级别
- 使用合理的索引减少锁范围
- 设置锁等待超时 `innodb_lock_wait_timeout`

### 10. 如何优化慢查询？

**定位慢查询**：
```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 1;
SET GLOBAL long_query_time = 2;
```

**优化方法**：
1. 添加合适的索引
2. 避免 SELECT *
3. 避免在 WHERE 中使用函数或计算
4. 使用 LIMIT 限制结果集
5. 优化 JOIN 查询
6. 分析执行计划（EXPLAIN）
7. 考虑分库分表

### 11. EXPLAIN 的关键字段含义？

```sql
EXPLAIN SELECT * FROM user WHERE age > 20;
```

**关键字段**：
- **type**：访问类型（system > const > eq_ref > ref > range > index > ALL）
- **possible_keys**：可能使用的索引
- **key**：实际使用的索引
- **rows**：扫描的行数
- **Extra**：额外信息
  - Using index：覆盖索引
  - Using where：使用 WHERE 过滤
  - Using filesort：文件排序（需优化）
  - Using temporary：使用临时表（需优化）

### 12. 什么是索引失效？常见场景有哪些？

**索引失效场景**：
1. 使用函数或计算：`WHERE YEAR(create_time) = 2024`
2. 类型转换：`WHERE phone = 12345678901`（phone 是 VARCHAR）
3. 模糊查询前缀通配：`WHERE name LIKE '%Tom'`
4. OR 条件：`WHERE name = 'Tom' OR age = 20`（age 无索引）
5. 不等于：`WHERE age != 20`
6. IS NULL / IS NOT NULL（视情况）
7. 组合索引不遵循最左前缀原则

### 13. 什么是最左前缀原则？

组合索引 `idx_abc(a, b, c)` 可以支持：
- `WHERE a = 1`
- `WHERE a = 1 AND b = 2`
- `WHERE a = 1 AND b = 2 AND c = 3`

不支持：
- `WHERE b = 2`
- `WHERE c = 3`
- `WHERE b = 2 AND c = 3`

### 14. MySQL 的主从复制原理？

**三个线程**：
1. **Master - Binlog Dump 线程**：读取 binlog 发送给 Slave
2. **Slave - I/O 线程**：接收 binlog 写入 Relay Log
3. **Slave - SQL 线程**：执行 Relay Log 中的事件

**复制方式**：
- **异步复制**：Master 不等待 Slave 确认
- **半同步复制**：至少一个 Slave 确认后才返回
- **全同步复制**：所有 Slave 确认后才返回

### 15. 如何保证主从一致性？

**问题**：
- 主从延迟
- 网络故障
- Slave 宕机

**解决方案**：
1. 使用半同步复制
2. 并行复制（多线程 SQL 线程）
3. 读写分离时，写后读从主库
4. 使用 GTID（全局事务标识符）
5. 监控主从延迟

## 高级篇

### 16. 分库分表的策略有哪些？

**垂直拆分**：
- 垂直分库：按业务模块拆分
- 垂直分表：按字段拆分（冷热数据分离）

**水平拆分**：
- 水平分库：按数据行拆分到不同库
- 水平分表：按数据行拆分到不同表

**分片策略**：
- Range：按范围（如按时间）
- Hash：按哈希值
- 一致性哈希
- 地理位置

### 17. 分库分表后如何处理跨库 JOIN？

**解决方案**：
1. 应用层组装数据
2. 数据冗余（反范式设计）
3. 全局表（字典表同步到所有库）
4. 使用中间件（ShardingSphere、MyCat）
5. 使用 ES 等搜索引擎

### 18. 如何设计一个高可用的 MySQL 架构？

**方案**：
1. **主从复制 + 读写分离**
   - 一主多从
   - 写主库，读从库

2. **双主复制**
   - 互为主从
   - 避免 ID 冲突

3. **MHA（Master High Availability）**
   - 自动故障转移
   - 主库宕机时自动切换

4. **MGR（MySQL Group Replication）**
   - 多主模式
   - 自动故障检测和恢复

5. **云服务**
   - RDS 高可用版
   - 自动备份和恢复

### 19. 大表如何优化？

**优化策略**：
1. **索引优化**：添加合适索引
2. **分区表**：按时间或范围分区
3. **归档历史数据**：迁移到历史表
4. **读写分离**：减轻主库压力
5. **缓存**：Redis 缓存热点数据
6. **分库分表**：水平拆分
7. **优化查询**：避免全表扫描
8. **硬件升级**：SSD、增加内存

### 20. MySQL 的 Binlog 有哪些格式？

1. **STATEMENT**
   - 记录 SQL 语句
   - 日志量小
   - 可能导致主从不一致（如 NOW()、UUID()）

2. **ROW**
   - 记录每行数据变化
   - 日志量大
   - 保证主从一致

3. **MIXED**
   - 混合模式
   - 一般用 STATEMENT，特殊情况用 ROW

### 21. 如何进行 MySQL 性能调优？

**硬件层面**：
- 使用 SSD
- 增加内存
- 使用更快的 CPU

**配置层面**：
```ini
# InnoDB 缓冲池大小（建议物理内存的 70-80%）
innodb_buffer_pool_size = 8G

# 日志文件大小
innodb_log_file_size = 512M

# 连接数
max_connections = 1000

# 查询缓存（5.7 后不推荐）
query_cache_size = 0
```

**SQL 层面**：
- 优化索引
- 避免慢查询
- 使用批量操作
- 合理使用事务

**架构层面**：
- 读写分离
- 分库分表
- 使用缓存

### 22. 什么是 Buffer Pool？

InnoDB 的内存缓冲区，缓存表数据和索引。

**组成**：
- 数据页
- 索引页
- Undo 页
- 插入缓冲
- 自适应哈希索引
- 锁信息

**管理算法**：
- 改进的 LRU 算法
- 分为 young 区和 old 区
- 避免全表扫描污染缓存

### 23. MySQL 的 Redo Log 和 Undo Log 的区别？

**Redo Log**（重做日志）：
- 记录物理修改（数据页的变化）
- 用于崩溃恢复
- 保证持久性（D）
- 循环写入

**Undo Log**（回滚日志）：
- 记录逻辑修改（相反操作）
- 用于事务回滚
- 实现 MVCC
- 保证原子性（A）

### 24. 什么是两阶段提交（2PC）？

保证 Redo Log 和 Binlog 的一致性。

**流程**：
1. **Prepare 阶段**：写入 Redo Log，状态为 prepare
2. **Commit 阶段**：写入 Binlog，然后提交 Redo Log

**作用**：
- 保证主从一致性
- 保证崩溃恢复的正确性

### 25. 如何实现 MySQL 的高并发写入？

**优化方案**：
1. **批量插入**：`INSERT INTO ... VALUES (...), (...), (...)`
2. **使用事务**：减少提交次数
3. **禁用索引**：导入时禁用，导入后重建
4. **调整参数**：
   ```ini
   innodb_flush_log_at_trx_commit = 2
   sync_binlog = 0
   ```
5. **分库分表**：分散写压力
6. **使用队列**：异步写入
7. **使用 SSD**：提升 I/O 性能

## 实战篇

### 26. 如何排查 MySQL CPU 100% 的问题？

**排查步骤**：
1. 查看慢查询日志
2. 查看当前执行的 SQL：`SHOW PROCESSLIST`
3. 分析执行计划：`EXPLAIN`
4. 检查锁等待：`SHOW ENGINE INNODB STATUS`
5. 查看表结构和索引
6. 检查是否有大事务
7. 检查是否有死锁

### 27. 如何备份和恢复 MySQL 数据？

**备份方式**：
1. **逻辑备份**：
   ```bash
   mysqldump -u root -p database > backup.sql
   ```

2. **物理备份**：
   - 直接复制数据文件
   - 使用 XtraBackup（热备份）

3. **Binlog 备份**：
   - 增量备份
   - 用于时间点恢复

**恢复**：
```bash
mysql -u root -p database < backup.sql
```

### 28. 如何处理 MySQL 主从延迟？

**原因**：
- 主库写入压力大
- 从库配置低
- 网络延迟
- 大事务
- 锁等待

**解决方案**：
1. 使用并行复制
2. 升级从库硬件
3. 优化慢查询
4. 拆分大事务
5. 使用半同步复制
6. 读写分离时，写后读主库

### 29. 如何设计一个订单表？

```sql
CREATE TABLE `order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

**设计要点**：
- 使用 BIGINT 作为主键
- 订单号使用唯一索引
- 金额使用 DECIMAL
- 添加必要的索引
- 使用枚举值表示状态
- 记录创建和更新时间

### 30. 如何实现分布式唯一 ID？

**方案**：
1. **UUID**：简单但无序，影响索引性能
2. **数据库自增 ID**：
   - 单库：AUTO_INCREMENT
   - 多库：设置不同的起始值和步长
3. **Redis INCR**：高性能，需考虑持久化
4. **雪花算法（Snowflake）**：
   - 64 位：1位符号 + 41位时间戳 + 10位机器ID + 12位序列号
   - 趋势递增，性能高
5. **美团 Leaf**：号段模式或雪花模式
6. **百度 UidGenerator**：基于雪花算法
