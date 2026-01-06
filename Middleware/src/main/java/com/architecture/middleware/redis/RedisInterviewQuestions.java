package com.architecture.middleware.redis;

/**
 * Redis最新面试问题汇总（2024-2025年）
 * 
 * 涵盖基础概念、高级特性、性能优化、分布式部署等方面
 */

import org.springframework.stereotype.Component;

@Component
public class RedisInterviewQuestions {

    /**
     * ====================
     * 一、基础概念类问题
     * ====================
     */

    /**
     * Q1: Redis为什么这么快？
     * 
     * A: Redis高性能的原因：
     * 1. 内存存储：数据存储在内存中，避免磁盘I/O
     * 2. 单线程模型：避免线程上下文切换和锁竞争
     * 3. I/O多路复用：使用epoll/kqueue等机制
     * 4. 高效的数据结构：针对不同场景优化的数据结构
     * 5. 简单的协议：RESP协议简单高效
     */
    public void whyRedisFast() {
        System.out.println("Redis性能优势：内存 + 单线程 + I/O多路复用 + 优化数据结构");
    }

    /**
     * Q2: Redis的数据类型及底层实现？
     * 
     * A: Redis 5种基本数据类型：
     * 
     * 1. String（字符串）
     *    - 底层：SDS（Simple Dynamic String）
     *    - 应用：缓存、计数器、分布式锁
     * 
     * 2. Hash（哈希）
     *    - 底层：ziplist（小数据）/ hashtable（大数据）
     *    - 应用：对象存储、用户信息缓存
     * 
     * 3. List（列表）
     *    - 底层：quicklist（双向链表 + 压缩列表）
     *    - 应用：消息队列、时间线
     * 
     * 4. Set（集合）
     *    - 底层：intset（整数）/ hashtable（字符串）
     *    - 应用：标签、好友关系、去重
     * 
     * 5. ZSet（有序集合）
     *    - 底层：ziplist（小数据）/ skiplist + hashtable（大数据）
     *    - 应用：排行榜、范围查询
     */
    public void redisDataTypes() {
        System.out.println("Redis数据类型：String/Hash/List/Set/ZSet + 对应底层实现");
    }

    /**
     * Q3: Redis持久化方式有哪些？
     * 
     * A: Redis两种持久化方式：
     * 
     * 1. RDB（Redis Database）
     *    - 原理：创建数据快照存储到磁盘
     *    - 优点：文件小、恢复快、对性能影响小
     *    - 缺点：可能丢失最后一次快照后的数据
     *    - 配置：save 900 1（900秒内有1个key变化就保存）
     * 
     * 2. AOF（Append Only File）
     *    - 原理：记录每个写操作命令
     *    - 优点：数据安全性高、可读性强
     *    - 缺点：文件大、恢复慢
     *    - 配置：appendfsync everysec
     * 
     * 3. 混合持久化（Redis 4.0+）
     *    - RDB + AOF增量
     *    - 兼具两者优点
     */
    public void redisPersistence() {
        System.out.println("持久化：RDB快照 + AOF日志 + 混合持久化");
    }

    /**
     * ====================
     * 二、高级特性问题
     * ====================
     */

    /**
     * Q4: Redis主从复制原理？
     * 
     * A: 主从复制流程：
     * 
     * 1. 连接建立
     *    - Slave发送PSYNC命令给Master
     *    - 携带复制ID和偏移量
     * 
     * 2. 数据同步
     *    - 全量同步：Master执行BGSAVE，发送RDB给Slave
     *    - 增量同步：发送复制积压缓冲区命令
     * 
     * 3. 命令传播
     *    - Master将写命令实时发送给Slave
     *    - Slave执行命令保持数据一致
     * 
     * 4. 心跳检测
     *    - 定期PING/PONG确保连接正常
     */
    public void masterSlaveReplication() {
        System.out.println("主从复制：连接建立 -> 数据同步 -> 命令传播 -> 心跳检测");
    }

    /**
     * Q5: Redis Sentinel哨兵机制？
     * 
     * A: Sentinel功能：
     * 
     * 1. 监控（Monitoring）
     *    - 检查Master和Slave是否正常运行
     *    - 通过PING命令检测
     * 
     * 2. 提醒（Notification）
     *    - 当实例有问题时通知管理员
     *    - 支持邮件、脚本等通知方式
     * 
     * 3. 自动故障迁移（Automatic Failover）
     *    - Master下线时自动选举新Master
     *    - 更新其他Slave的复制目标
     * 
     * 4. 配置提供者（Configuration Provider）
     *    - 客户端连接Sentinel获取Master信息
     *    - 故障转移时更新客户端配置
     * 
     * 故障转移流程：
     * 1. 主观下线（SDOWN）：单个Sentinel认为Master下线
     * 2. 客观下线（ODOWN）：多数Sentinel认为Master下线
     * 3. 选举领导者：选出负责故障转移的Sentinel
     * 4. 故障转移：选择新Master，更新配置
     */
    public void sentinelMechanism() {
        System.out.println("Sentinel：监控 + 提醒 + 自动故障转移 + 配置提供");
    }

    /**
     * Q6: Redis Cluster集群原理？
     * 
     * A: Cluster特性：
     * 
     * 1. 数据分片
     *    - 16384个槽位（0-16383）
     *    - 槽位分配给不同节点
     *    - CRC16(key) % 16384计算槽位
     * 
     * 2. 节点发现
     *    - Gossip协议交换节点信息
     *    - MEET/PING/PONG/FAIL消息
     * 
     * 3. 故障检测
     *    - 节点间相互监控
     *    - 超过半数节点认为某节点下线才标记为FAIL
     * 
     * 4. 故障转移
     *    - 从节点自动提升为主节点
     *    - 重新分配槽位
     * 
     * 5. 扩容缩容
     *    - 动态添加/删除节点
     *    - 槽位迁移保证数据一致性
     */
    public void clusterArchitecture() {
        System.out.println("Cluster：数据分片 + 节点发现 + 故障转移 + 动态扩容");
    }

    /**
     * ====================
     * 三、缓存问题处理
     * ====================
     */

    /**
     * Q7: 缓存雪崩、穿透、击穿问题及解决方案？
     * 
     * A: 三大缓存问题：
     * 
     * 1. 缓存雪崩
     *    - 问题：大量缓存同时失效，请求直接打到数据库
     *    - 解决：
     *      a. 设置不同的过期时间（加随机值）
     *      b. 使用多级缓存
     *      c. 缓存预热
     *      d. 限流降级
     * 
     * 2. 缓存穿透
     *    - 问题：查询不存在的数据，缓存和数据库都没有
     *    - 解决：
     *      a. 布隆过滤器
     *      b. 缓存空结果（设置较短TTL）
     *      c. 参数校验
     * 
     * 3. 缓存击穿
     *    - 问题：热点数据过期，大量请求同时访问数据库
     *    - 解决：
     *      a. 分布式锁
     *      b. 热点数据永不过期
     *      c. 互斥锁
     */
    public void cacheProblems() {
        System.out.println("缓存问题：雪崩（批量失效） + 穿透（查不存在） + 击穿（热点失效）");
    }

    /**
     * Q8: Redis分布式锁实现及问题？
     * 
     * A: 分布式锁实现：
     * 
     * 1. 基础实现
     *    SET key value NX EX seconds
     *    - NX：key不存在才设置
     *    - EX：设置过期时间
     * 
     * 2. 进阶实现（Redisson）
     *    - 可重入锁
     *    - 锁续期机制（看门狗）
     *    - 公平锁/非公平锁
     * 
     * 3. 问题及解决：
     *    a. 死锁：设置过期时间
     *    b. 误删锁：使用唯一标识
     *    c. 原子性：使用Lua脚本
     *    d. 主从异步：使用RedLock算法
     * 
     * 4. RedLock算法
     *    - 多个独立Redis实例
     *    - 必须在大多数实例上获得锁
     *    - 考虑时钟漂移问题
     */
    public void distributedLock() {
        System.out.println("分布式锁：SET NX EX + Redisson + RedLock算法");
    }

    /**
     * ====================
     * 四、性能优化问题
     * ====================
     */

    /**
     * Q9: Redis性能优化策略？
     * 
     * A: 优化策略：
     * 
     * 1. 数据结构优化
     *    - 选择合适的数据类型
     *    - 控制key和value大小
     *    - 使用pipeline批量操作
     * 
     * 2. 内存优化
     *    - 设置合理的过期时间
     *    - 使用内存淘汰策略
     *    - 开启压缩（hash-ziplist-*）
     * 
     * 3. 网络优化
     *    - 使用连接池
     *    - 开启keepalive
     *    - 减少网络往返次数
     * 
     * 4. 持久化优化
     *    - RDB：调整save参数
     *    - AOF：选择合适的fsync策略
     *    - 避免主库做持久化
     * 
     * 5. 集群优化
     *    - 读写分离
     *    - 数据分片
     *    - 就近访问
     */
    public void performanceOptimization() {
        System.out.println("性能优化：数据结构 + 内存 + 网络 + 持久化 + 集群");
    }

    /**
     * Q10: Redis大key问题及解决？
     * 
     * A: 大key问题：
     * 
     * 1. 危害
     *    - 内存占用过多
     *    - 网络传输慢
     *    - 阻塞Redis处理
     *    - 主从复制延迟
     * 
     * 2. 识别方法
     *    - redis-cli --bigkeys
     *    - SCAN + MEMORY USAGE
     *    - Redis监控告警
     * 
     * 3. 解决方案
     *    a. 数据分片：将大key拆分为多个小key
     *    b. 数据压缩：使用压缩算法
     *    c. 异步删除：使用UNLINK代替DEL
     *    d. 分批处理：使用SCAN分批处理
     *    e. 设置TTL：定期清理过期数据
     * 
     * 4. 预防措施
     *    - 设计时考虑数据大小
     *    - 监控key大小
     *    - 定期检查和清理
     */
    public void bigKeyProblem() {
        System.out.println("大key问题：识别 + 拆分 + 压缩 + 异步删除");
    }

    /**
     * ====================
     * 五、最新特性问题
     * ====================
     */

    /**
     * Q11: Redis 6.0/7.0新特性？
     * 
     * A: Redis 6.0新特性：
     * 
     * 1. 多线程I/O
     *    - 网络I/O使用多线程
     *    - 命令执行仍是单线程
     *    - 提升网络处理性能
     * 
     * 2. ACL访问控制
     *    - 用户权限管理
     *    - 命令级别权限控制
     *    - 支持多用户
     * 
     * 3. RESP3协议
     *    - 新的通信协议
     *    - 更丰富的数据类型
     *    - 更好的客户端支持
     * 
     * 4. 客户端缓存
     *    - 客户端侧缓存
     *    - 服务端通知失效
     *    - 减少网络请求
     * 
     * Redis 7.0新特性：
     * 1. Redis Functions（替代Lua脚本）
     * 2. Sharded Pub/Sub
     * 3. 更好的内存优化
     * 4. 多线程AOF重写
     */
    public void newFeatures() {
        System.out.println("新特性：多线程I/O + ACL + RESP3 + Functions");
    }

    /**
     * Q12: Redis与其他缓存技术对比？
     * 
     * A: 缓存技术对比：
     * 
     * 1. Redis vs Memcached
     *    - 数据类型：Redis丰富，Memcached仅string
     *    - 持久化：Redis支持，Memcached不支持
     *    - 分布式：Redis Cluster，Memcached一致性哈希
     *    - 性能：单线程vs多线程，各有优势
     * 
     * 2. Redis vs 本地缓存（Caffeine/Guava）
     *    - 分布式：Redis支持，本地缓存不支持
     *    - 性能：本地缓存更快，Redis有网络开销
     *    - 内存：Redis可扩展，本地缓存受限
     *    - 一致性：Redis强一致，本地缓存弱一致
     * 
     * 3. Redis vs 数据库
     *    - 性能：Redis内存存储，数据库磁盘存储
     *    - 持久性：数据库更可靠，Redis可能丢失数据
     *    - 功能：数据库功能丰富，Redis功能特定
     *    - 成本：Redis成本高，数据库成本相对低
     */
    public void technologyComparison() {
        System.out.println("技术对比：数据类型 + 持久化 + 分布式 + 性能");
    }

    /**
     * ====================
     * 六、实际应用问题
     * ====================
     */

    /**
     * Q13: 如何设计一个高并发的计数器系统？
     * 
     * A: 计数器系统设计：
     * 
     * 1. 单机计数器
     *    - 使用INCR/INCRBY命令
     *    - 设置合理的过期时间
     *    - 考虑数据持久化
     * 
     * 2. 分布式计数器
     *    - 分片计数：将计数分散到多个key
     *    - 异步聚合：定期汇总分片结果
     *    - 预分配：预先创建足够的分片
     * 
     * 3. 高并发优化
     *    - 使用pipeline批量操作
     *    - 本地缓存 + 异步刷新
     *    - 读写分离
     * 
     * 4. 数据一致性
     *    - 最终一致性：允许短暂不一致
     *    - 幂等操作：保证重复操作安全
     *    - 补偿机制：定期校正计数
     */
    public void counterSystemDesign() {
        System.out.println("计数器系统：分片计数 + 异步聚合 + 本地缓存");
    }

    /**
     * Q14: Redis在微服务架构中的应用？
     * 
     * A: 微服务中Redis应用：
     * 
     * 1. 分布式缓存
     *    - 服务间共享数据
     *    - 减少数据库压力
     *    - 提升响应速度
     * 
     * 2. 分布式锁
     *    - 防止重复操作
     *    - 资源互斥访问
     *    - 保证数据一致性
     * 
     * 3. 分布式会话
     *    - Session共享
     *    - 用户状态管理
     *    - 单点登录（SSO）
     * 
     * 4. 消息队列
     *    - 服务间解耦
     *    - 异步处理
     *    - 削峰填谷
     * 
     * 5. 限流熔断
     *    - 接口限流
     *    - 服务熔断
     *    - 流量控制
     * 
     * 6. 配置中心
     *    - 动态配置
     *    - 配置同步
     *    - 灰度发布
     */
    public void microserviceApplication() {
        System.out.println("微服务应用：缓存 + 锁 + 会话 + 消息 + 限流 + 配置");
    }

    /**
     * Q15: Redis监控和运维最佳实践？
     * 
     * A: 监控运维实践：
     * 
     * 1. 关键指标监控
     *    - 内存使用率：used_memory / maxmemory
     *    - QPS/TPS：instantaneous_ops_per_sec
     *    - 命中率：keyspace_hits / (keyspace_hits + keyspace_misses)
     *    - 慢查询：slowlog-log-slower-than
     *    - 连接数：connected_clients
     * 
     * 2. 告警策略
     *    - 内存使用率 > 80%
     *    - 主从延迟 > 1s
     *    - 连接数 > 阈值
     *    - 慢查询增多
     * 
     * 3. 日常运维
     *    - 定期备份数据
     *    - 监控日志文件
     *    - 检查集群状态
     *    - 清理过期数据
     * 
     * 4. 性能调优
     *    - 分析慢查询日志
     *    - 优化数据结构
     *    - 调整配置参数
     *    - 升级硬件资源
     * 
     * 5. 故障处理
     *    - 制定应急预案
     *    - 建立故障处理流程
     *    - 定期演练
     *    - 总结经验教训
     */
    public void monitoringAndOps() {
        System.out.println("监控运维：指标监控 + 告警策略 + 日常运维 + 性能调优 + 故障处理");
    }
}