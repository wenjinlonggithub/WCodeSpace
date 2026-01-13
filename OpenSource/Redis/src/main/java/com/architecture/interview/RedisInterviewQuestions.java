package com.architecture.interview;

/**
 * Redis面试题集锦
 *
 * 涵盖Redis的核心知识点、常见面试问题及详细解答
 */
public class RedisInterviewQuestions {

    /**
     * 1. Redis为什么这么快？
     *
     * 答案：
     * 1) 基于内存：所有数据存储在内存中，读写速度极快
     * 2) 单线程模型：避免了线程切换和竞态条件的开销（Redis 6.0后I/O多线程）
     * 3) 高效的数据结构：针对不同场景优化的数据结构
     * 4) I/O多路复用：使用epoll/select等技术处理大量并发连接
     * 5) 简单的协议：RESP协议解析快速
     */
    public void question01_WhyRedisFast() {
        System.out.println("Q1: Redis为什么这么快？");
        System.out.println("1. 基于内存操作");
        System.out.println("2. 单线程模型（避免上下文切换）");
        System.out.println("3. 高效的数据结构");
        System.out.println("4. I/O多路复用");
        System.out.println("5. 简单高效的协议");
    }

    /**
     * 2. Redis的数据类型有哪些？底层数据结构是什么？
     *
     * 答案：
     * 5种基本类型：
     * 1) String：简单动态字符串（SDS）
     * 2) List：双向链表或压缩列表（ziplist）-> quicklist
     * 3) Hash：哈希表或压缩列表
     * 4) Set：哈希表或整数集合（intset）
     * 5) Sorted Set：跳表+哈希表或压缩列表
     *
     * 3种特殊类型：
     * 6) HyperLogLog：用于基数统计
     * 7) Geo：地理位置信息
     * 8) Bitmap：位图
     *
     * 3种高级类型（Redis 5.0+）：
     * 9) Stream：消息队列
     */
    public void question02_DataTypes() {
        System.out.println("Q2: Redis数据类型及底层结构");
        System.out.println("String -> SDS");
        System.out.println("List -> quicklist（双向链表+压缩列表）");
        System.out.println("Hash -> dict或ziplist");
        System.out.println("Set -> dict或intset");
        System.out.println("ZSet -> skiplist+dict或ziplist");
    }

    /**
     * 3. 什么是缓存穿透、缓存击穿、缓存雪崩？如何解决？
     *
     * 缓存穿透：查询不存在的数据，缓存和数据库都没有，导致每次请求都打到数据库
     * 解决方案：
     * 1) 布隆过滤器：判断key是否可能存在
     * 2) 缓存空值：对不存在的key也缓存，设置较短过期时间
     * 3) 接口校验：参数校验，不合法的请求直接返回
     *
     * 缓存击穿：热点key过期，大量并发请求同时访问数据库
     * 解决方案：
     * 1) 互斥锁：只让一个线程查询数据库，其他线程等待
     * 2) 热点数据永不过期：定期更新
     * 3) 逻辑过期：记录过期时间，异步更新
     *
     * 缓存雪崩：大量key同时过期，或Redis宕机，导致数据库压力骤增
     * 解决方案：
     * 1) 过期时间加随机值：避免同时过期
     * 2) 多级缓存：本地缓存+Redis
     * 3) 服务降级：限流、熔断
     * 4) Redis集群：高可用
     */
    public void question03_CacheProblems() {
        System.out.println("Q3: 缓存三大问题");
        System.out.println("\n缓存穿透：查询不存在的数据");
        System.out.println("- 布隆过滤器");
        System.out.println("- 缓存空值");

        System.out.println("\n缓存击穿：热点key过期");
        System.out.println("- 互斥锁");
        System.out.println("- 热点数据永不过期");

        System.out.println("\n缓存雪崩：大量key同时过期");
        System.out.println("- 过期时间加随机值");
        System.out.println("- 多级缓存");
        System.out.println("- 服务降级");
    }

    /**
     * 4. Redis的过期删除策略和内存淘汰策略？
     *
     * 过期删除策略：
     * 1) 惰性删除：访问时检查是否过期
     * 2) 定期删除：每隔一段时间随机检查一批key
     *
     * 内存淘汰策略（maxmemory-policy）：
     * 1) noeviction：不淘汰，内存满后写入报错（默认）
     * 2) allkeys-lru：在所有key中使用LRU算法淘汰
     * 3) allkeys-lfu：在所有key中使用LFU算法淘汰
     * 4) allkeys-random：在所有key中随机淘汰
     * 5) volatile-lru：在设置过期时间的key中使用LRU
     * 6) volatile-lfu：在设置过期时间的key中使用LFU
     * 7) volatile-random：在设置过期时间的key中随机淘汰
     * 8) volatile-ttl：淘汰即将过期的key
     */
    public void question04_ExpireStrategy() {
        System.out.println("Q4: 过期删除和内存淘汰");
        System.out.println("\n过期删除策略：");
        System.out.println("1. 惰性删除（被动）");
        System.out.println("2. 定期删除（主动）");

        System.out.println("\n内存淘汰策略：");
        System.out.println("1. noeviction（默认）");
        System.out.println("2. allkeys-lru（推荐）");
        System.out.println("3. volatile-lru");
        System.out.println("4. allkeys-random");
    }

    /**
     * 5. Redis持久化机制RDB和AOF的区别？
     *
     * RDB（Redis Database）：
     * - 全量快照，二进制文件
     * - 恢复速度快
     * - 适合备份、灾难恢复
     * - 可能丢失最后一次快照后的数据
     *
     * AOF（Append Only File）：
     * - 记录写操作日志
     * - 数据完整性好
     * - 文件较大，恢复慢
     * - 可能有bug导致数据损坏
     *
     * 混合持久化（Redis 4.0+）：
     * - RDB快照 + AOF增量日志
     * - 兼顾恢复速度和数据完整性
     */
    public void question05_Persistence() {
        System.out.println("Q5: RDB vs AOF");
        System.out.println("\nRDB：");
        System.out.println("- 全量快照");
        System.out.println("- 恢复快");
        System.out.println("- 可能丢数据");

        System.out.println("\nAOF：");
        System.out.println("- 记录写操作");
        System.out.println("- 数据完整");
        System.out.println("- 文件大、恢复慢");

        System.out.println("\n混合持久化（推荐）：");
        System.out.println("- RDB + AOF");
    }

    /**
     * 6. Redis主从复制原理？
     *
     * 全量复制：
     * 1) 从节点发送PSYNC命令
     * 2) 主节点执行BGSAVE生成RDB
     * 3) 主节点发送RDB文件给从节点
     * 4) 从节点清空旧数据，加载RDB
     * 5) 主节点发送缓冲区的写命令
     *
     * 增量复制：
     * 1) 从节点断线重连后发送PSYNC
     * 2) 主节点判断是否可以增量复制
     * 3) 发送复制偏移量之后的命令
     *
     * 复制积压缓冲区：环形缓冲区，默认1MB
     */
    public void question06_Replication() {
        System.out.println("Q6: 主从复制");
        System.out.println("\n全量复制：");
        System.out.println("1. PSYNC命令");
        System.out.println("2. BGSAVE生成RDB");
        System.out.println("3. 发送RDB文件");
        System.out.println("4. 加载RDB");
        System.out.println("5. 发送缓冲区命令");

        System.out.println("\n增量复制：");
        System.out.println("1. 断线重连");
        System.out.println("2. 发送偏移量后的命令");
    }

    /**
     * 7. Redis哨兵机制？
     *
     * 哨兵的作用：
     * 1) 监控：检查主从节点是否正常
     * 2) 通知：故障时通知管理员或客户端
     * 3) 自动故障转移：主节点故障时选举新主节点
     * 4) 配置提供：客户端连接哨兵获取主节点地址
     *
     * 故障转移流程：
     * 1) 主观下线：单个哨兵判断主节点下线
     * 2) 客观下线：多数哨兵确认主节点下线
     * 3) 选举领导哨兵：Raft算法
     * 4) 选择新主节点：优先级、复制偏移量、runid
     * 5) 修改配置：将其他从节点指向新主节点
     */
    public void question07_Sentinel() {
        System.out.println("Q7: 哨兵机制");
        System.out.println("\n作用：");
        System.out.println("1. 监控");
        System.out.println("2. 通知");
        System.out.println("3. 自动故障转移");
        System.out.println("4. 配置提供");

        System.out.println("\n故障转移：");
        System.out.println("1. 主观下线");
        System.out.println("2. 客观下线");
        System.out.println("3. 选举领导哨兵");
        System.out.println("4. 选择新主节点");
    }

    /**
     * 8. Redis集群方案？
     *
     * 1) 主从复制+哨兵：
     * - 高可用
     * - 不支持水平扩展
     *
     * 2) Redis Cluster：
     * - 官方集群方案
     * - 去中心化
     * - 16384个槽位
     * - 支持水平扩展
     *
     * 3) Codis：
     * - 豌豆荚开源
     * - 代理方式
     * - 1024个槽位
     *
     * 4) Twemproxy：
     * - Twitter开源
     * - 轻量级代理
     */
    public void question08_Cluster() {
        System.out.println("Q8: 集群方案");
        System.out.println("\n1. 主从+哨兵：高可用");
        System.out.println("2. Redis Cluster：官方方案");
        System.out.println("3. Codis：代理方式");
        System.out.println("4. Twemproxy：轻量级代理");
    }

    /**
     * 9. Redis事务？
     *
     * 命令：
     * - MULTI：开始事务
     * - EXEC：执行事务
     * - DISCARD：取消事务
     * - WATCH：监视key
     *
     * 特点：
     * 1) 单独的隔离操作：事务中的命令会按顺序执行
     * 2) 没有隔离级别：不会被其他命令插入
     * 3) 不保证原子性：某个命令失败不会回滚
     *
     * 与关系型数据库事务的区别：
     * - 不支持回滚
     * - 不支持隔离级别
     */
    public void question09_Transaction() {
        System.out.println("Q9: Redis事务");
        System.out.println("\n命令：");
        System.out.println("MULTI -> EXEC -> DISCARD");
        System.out.println("WATCH监视key");

        System.out.println("\n特点：");
        System.out.println("1. 顺序执行");
        System.out.println("2. 不会被插入");
        System.out.println("3. 不保证原子性（不支持回滚）");
    }

    /**
     * 10. 如何保证Redis和数据库的一致性？
     *
     * 常见策略：
     * 1) 先更新数据库，再删除缓存（推荐）
     * 2) 先删除缓存，再更新数据库
     * 3) 延迟双删：删除缓存->更新数据库->延迟删除缓存
     *
     * 最终一致性方案：
     * 1) 订阅binlog：Canal、Maxwell
     * 2) 消息队列：异步更新
     * 3) 定时任务：定期同步
     *
     * 强一致性方案（性能差）：
     * 1) 分布式锁：读写都加锁
     * 2) 串行化：所有操作串行执行
     */
    public void question10_Consistency() {
        System.out.println("Q10: 缓存一致性");
        System.out.println("\n推荐方案：");
        System.out.println("先更新数据库，再删除缓存");

        System.out.println("\n最终一致性：");
        System.out.println("1. 订阅binlog");
        System.out.println("2. 消息队列");
        System.out.println("3. 定时同步");

        System.out.println("\n强一致性（不推荐）：");
        System.out.println("1. 分布式锁");
        System.out.println("2. 串行化");
    }

    public static void main(String[] args) {
        RedisInterviewQuestions questions = new RedisInterviewQuestions();

        questions.question01_WhyRedisFast();
        System.out.println("\n" + "=".repeat(50) + "\n");

        questions.question03_CacheProblems();
        System.out.println("\n" + "=".repeat(50) + "\n");

        questions.question05_Persistence();
        System.out.println("\n" + "=".repeat(50) + "\n");

        questions.question10_Consistency();
    }
}
