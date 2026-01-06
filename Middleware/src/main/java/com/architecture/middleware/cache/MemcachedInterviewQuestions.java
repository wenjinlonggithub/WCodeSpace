package com.architecture.middleware.cache;

/**
 * Memcached最新面试问题汇总（2024-2025年）
 * 
 * 涵盖内存管理、性能优化、集群部署、与Redis对比等方面
 */

import org.springframework.stereotype.Component;

@Component
public class MemcachedInterviewQuestions {

    /**
     * ====================
     * 一、基础概念问题
     * ====================
     */

    /**
     * Q1: Memcached的基本原理和特点？
     * 
     * A: Memcached分布式内存缓存系统：
     * 
     * 1. 基本原理
     *    - 内存存储：数据完全存储在内存中，重启后丢失
     *    - Key-Value：简单的键值对存储模型
     *    - 分布式：客户端负责分片和路由
     *    - 无状态：服务器间不通信，无主从关系
     * 
     * 2. 核心特点
     *    ✓ 高性能：纯内存操作，响应时间微秒级
     *    ✓ 简单协议：基于文本或二进制的简单协议
     *    ✓ 多线程：支持多线程并发处理请求
     *    ✓ LRU淘汰：自动淘汰最近最少使用的数据
     *    ✓ 稳定可靠：久经考验，生产环境广泛使用
     * 
     * 3. 架构组成
     *    - 客户端：负责哈希算法、故障转移、数据分片
     *    - 服务端：存储数据、处理请求、内存管理
     *    - 协议层：定义客户端和服务端通信格式
     *    - 网络层：基于TCP的可靠网络传输
     * 
     * 4. 适用场景
     *    - 页面缓存：缓存动态生成的页面内容
     *    - 数据库查询缓存：缓存频繁查询的数据库结果
     *    - 会话存储：Web应用的Session存储
     *    - 计算结果缓存：缓存复杂计算的中间结果
     *    - API响应缓存：缓存外部API调用结果
     */
    public void memcachedBasics() {
        System.out.println("Memcached：内存存储 + K-V模型 + 分布式 + 高性能 + 简单协议");
    }

    /**
     * Q2: Memcached的内存管理机制Slab Allocator？
     * 
     * A: Slab Allocator内存分配器详解：
     * 
     * 1. 设计目标
     *    - 减少内存碎片：避免频繁malloc/free导致的碎片
     *    - 提高分配效率：预分配内存，快速分配和回收
     *    - 防止内存泄漏：统一管理内存分配
     *    - 支持大数据：处理不同大小的数据对象
     * 
     * 2. 分级存储结构
     *    
     *    Slab Class分级：
     *    ```
     *    Class 1:  88字节   (最小chunk)
     *    Class 2:  112字节  (88 * 1.25)
     *    Class 3:  144字节  (112 * 1.25)
     *    ...
     *    Class N:  1MB      (最大chunk)
     *    ```
     *    
     *    增长因子（Growth Factor）：
     *    - 默认：1.25倍增长
     *    - 可调：-f参数调整增长速度
     *    - 影响：影响内存利用率和class数量
     * 
     * 3. 内存分配流程
     *    
     *    分配算法：
     *    1. 根据数据大小选择合适的Slab Class
     *    2. 在该Class的空闲链表中查找可用chunk
     *    3. 如果有空闲chunk，直接分配返回
     *    4. 如果无空闲chunk，申请新的slab页面
     *    5. 将新slab页面按class大小切分成chunks
     *    6. 返回第一个chunk，其余加入空闲链表
     * 
     * 4. 内存回收机制
     *    
     *    回收流程：
     *    1. item被删除或过期时触发回收
     *    2. 将chunk标记为空闲状态
     *    3. 加入到对应class的空闲链表头部
     *    4. 更新统计信息（空闲chunk数量）
     *    5. 不会立即归还给操作系统
     * 
     * 5. 内存重分配
     *    
     *    Slab Rebalancing：
     *    - 检测各class内存使用不均
     *    - 将空闲页面从一个class移动到另一个class
     *    - 在运行时动态调整内存分配
     *    - 提高整体内存利用效率
     * 
     * 6. 优缺点分析
     *    
     *    优点：
     *    ✓ 内存碎片少
     *    ✓ 分配效率高
     *    ✓ 内存管理简单
     *    ✓ 适合固定大小分配
     *    
     *    缺点：
     *    ✗ 内存浪费（大小不匹配）
     *    ✗ 配置复杂（增长因子选择）
     *    ✗ 冷启动慢（需要预热各class）
     */
    public void slabAllocator() {
        System.out.println("Slab分配器：分级管理 + 预分配 + 空闲链表 + 动态重分配");
    }

    /**
     * Q3: Memcached的LRU淘汰算法实现？
     * 
     * A: LRU（Least Recently Used）淘汰机制：
     * 
     * 1. LRU基本原理
     *    - 最近最少使用：淘汰最长时间未访问的数据
     *    - 局部性原理：最近访问的数据更可能再次被访问
     *    - 时间排序：按访问时间对数据进行排序
     *    - 自动管理：无需手动干预的自动淘汰
     * 
     * 2. 数据结构设计
     *    
     *    双向链表：
     *    ```c
     *    typedef struct {
     *        item *heads[POWER_LARGEST];  // 各class LRU链表头
     *        item *tails[POWER_LARGEST];  // 各class LRU链表尾  
     *        unsigned int sizes[POWER_LARGEST]; // 各class item数量
     *    } item_stats_t;
     *    
     *    typedef struct _stritem {
     *        struct _stritem *next;    // 哈希冲突链表
     *        struct _stritem *prev;    // LRU双向链表前驱
     *        struct _stritem *h_next;  // LRU双向链表后继
     *        rel_time_t time;          // 最后访问时间
     *        // ... 其他字段
     *    } item;
     *    ```
     * 
     * 3. LRU操作流程
     *    
     *    访问更新（GET操作）：
     *    1. 查找到目标item
     *    2. 从当前LRU位置移除（unlink）
     *    3. 插入到LRU链表头部（link）
     *    4. 更新item的访问时间
     *    5. 该item变为最近访问
     * 
     *    插入操作（SET操作）：
     *    1. 创建新的item
     *    2. 插入到哈希表
     *    3. 插入到LRU链表头部
     *    4. 设置当前时间戳
     *    5. 成为最新访问的item
     * 
     * 4. 淘汰触发条件
     *    - 内存分配失败：申请新chunk时内存不足
     *    - 达到内存上限：-m参数设置的内存限制
     *    - Class内存不足：特定class的slab用完
     *    - 手动触发：flush_all命令清空所有数据
     * 
     * 5. 淘汰算法实现
     *    
     *    淘汰流程：
     *    1. 从对应class的LRU链表尾部开始
     *    2. 检查item是否可以淘汰：
     *       - 引用计数为0（没有客户端引用）
     *       - 没有被锁定（不在处理中）
     *       - 满足淘汰条件
     *    3. 移除符合条件的item
     *    4. 从哈希表和LRU链表中删除
     *    5. 释放item占用的内存
     *    6. 重复直到获得足够内存
     * 
     * 6. 优化策略
     *    - 批量淘汰：一次性淘汰多个item提高效率
     *    - 过期优先：优先淘汰已过期的item
     *    - 分级LRU：每个slab class独立LRU链表
     *    - 懒惰删除：延迟删除过期item直到访问时
     */
    public void lruEviction() {
        System.out.println("LRU淘汰：双向链表 + 按class分级 + 尾部淘汰 + 批量处理");
    }

    /**
     * ====================
     * 二、性能和优化问题
     * ====================
     */

    /**
     * Q4: Memcached的多线程模型？
     * 
     * A: Memcached多线程架构设计：
     * 
     * 1. 线程架构
     *    
     *    主线程（Main Thread）：
     *    - 职责：监听端口、接受连接、分发连接
     *    - 数量：1个
     *    - 工作：执行accept()系统调用
     *    - 特点：不处理具体的客户端请求
     * 
     *    工作线程（Worker Thread）：
     *    - 职责：处理客户端请求、执行缓存操作
     *    - 数量：默认4个（-t参数配置）
     *    - 工作：读取命令、处理业务、返回响应
     *    - 特点：每个线程独立的事件循环
     * 
     * 2. 线程间通信
     *    
     *    管道通信机制：
     *    ```c
     *    typedef struct {
     *        int read_fd;    // 读取描述符
     *        int write_fd;   // 写入描述符
     *        evutil_socket_t notify_receive_fd;  // 接收通知
     *        evutil_socket_t notify_send_fd;     // 发送通知
     *    } LIBEVENT_THREAD;
     *    ```
     * 
     *    通信流程：
     *    1. 主线程accept新连接
     *    2. 选择负载最轻的worker线程
     *    3. 通过管道发送连接描述符给worker
     *    4. Worker线程从管道读取连接信息
     *    5. Worker线程接管连接并加入事件循环
     * 
     * 3. 事件驱动模型
     *    
     *    基于libevent：
     *    - 每个worker线程维护独立的event_base
     *    - 使用epoll/kqueue等高效I/O复用机制
     *    - 非阻塞I/O处理多个并发连接
     *    - 事件回调函数处理读写事件
     * 
     *    连接状态机：
     *    ```c
     *    enum conn_states {
     *        conn_listening,   // 监听状态
     *        conn_new_cmd,    // 等待新命令
     *        conn_waiting,    // 等待更多数据
     *        conn_read,       // 读取请求数据
     *        conn_parse_cmd,  // 解析命令
     *        conn_write,      // 写入响应数据
     *        conn_nread,      // 读取固定字节数
     *        conn_swallow,    // 丢弃数据
     *        conn_closing,    // 关闭连接
     *        conn_mwrite      // 批量写入
     *    };
     *    ```
     * 
     * 4. 锁机制
     *    
     *    锁的类型和用途：
     *    - Cache Lock：保护缓存数据的读写
     *    - Stats Lock：保护全局统计信息
     *    - Slabs Lock：保护内存分配操作
     *    - Connection Lock：保护连接列表操作
     * 
     *    锁优化策略：
     *    - 细粒度锁：减小锁的粒度降低竞争
     *    - 无锁操作：使用原子操作替代锁
     *    - 读写锁：区分读写操作使用不同锁
     *    - Lock-free数据结构：无锁队列和栈
     * 
     * 5. 性能优化
     *    - CPU亲和性：绑定线程到特定CPU核心
     *    - NUMA优化：考虑NUMA架构的内存访问
     *    - 批量处理：批量处理网络I/O操作
     *    - 零拷贝：减少数据在用户态和内核态间拷贝
     */
    public void multithreadingModel() {
        System.out.println("多线程：主线程监听 + Worker处理 + 管道通信 + 事件驱动 + 锁优化");
    }

    /**
     * Q5: Memcached性能调优策略？
     * 
     * A: Memcached性能优化全方位策略：
     * 
     * 1. 服务端优化
     *    
     *    (1) 内存配置
     *    ```bash
     *    # 设置最大内存使用量（2GB）
     *    memcached -m 2048
     *    
     *    # 调整增长因子（减少内存浪费）
     *    memcached -f 1.15
     *    
     *    # 设置最大连接数
     *    memcached -c 10000
     *    ```
     * 
     *    (2) 线程配置
     *    ```bash
     *    # 设置工作线程数（通常等于CPU核心数）
     *    memcached -t 8
     *    
     *    # 禁用CAS（减少内存使用）
     *    memcached -C
     *    
     *    # 启用大页面支持
     *    memcached -L
     *    ```
     * 
     * 2. 操作系统优化
     *    
     *    (1) 内核参数
     *    ```bash
     *    # TCP参数优化
     *    echo 'net.core.somaxconn = 65535' >> /etc/sysctl.conf
     *    echo 'net.core.netdev_max_backlog = 5000' >> /etc/sysctl.conf
     *    echo 'net.ipv4.tcp_max_syn_backlog = 65535' >> /etc/sysctl.conf
     *    
     *    # 内存参数优化
     *    echo 'vm.swappiness = 0' >> /etc/sysctl.conf
     *    echo 'vm.overcommit_memory = 1' >> /etc/sysctl.conf
     *    ```
     * 
     *    (2) 文件描述符
     *    ```bash
     *    # 增加文件描述符限制
     *    echo '* soft nofile 65535' >> /etc/security/limits.conf
     *    echo '* hard nofile 65535' >> /etc/security/limits.conf
     *    
     *    # 调整进程限制
     *    ulimit -n 65535
     *    ulimit -u 65535
     *    ```
     * 
     * 3. 客户端优化
     *    
     *    (1) 连接池配置
     *    ```java
     *    // Spring Boot中的配置
     *    memcached:
     *      servers: server1:11211,server2:11211
     *      pool:
     *        min-connections: 10
     *        max-connections: 50
     *        max-idle-time: 300
     *        maintain-network: true
     *    ```
     * 
     *    (2) 序列化优化
     *    ```java
     *    // 使用高效的序列化方式
     *    - Kryo：性能优秀，体积小
     *    - FST：Java对象序列化，兼容性好
     *    - Protobuf：跨语言，性能和体积平衡
     *    - Avro：schema演进，支持压缩
     *    ```
     * 
     * 4. 数据设计优化
     *    
     *    (1) Key设计
     *    - 简短有意义：user:123而不是user_information_123
     *    - 避免热点：使用hash分散热点key
     *    - 版本控制：key中包含版本信息支持更新
     *    - 命名规范：统一的key命名规范
     * 
     *    (2) Value设计
     *    - 合理大小：避免过大或过小的value
     *    - 数据压缩：大数据压缩后存储
     *    - 分片存储：大对象拆分为多个小对象
     *    - 冗余设计：适当冗余提高性能
     * 
     * 5. 监控和调试
     *    
     *    (1) 性能指标
     *    ```bash
     *    # 查看基本统计
     *    echo "stats" | nc localhost 11211
     *    
     *    # 查看各slab class统计  
     *    echo "stats slabs" | nc localhost 11211
     *    
     *    # 查看item大小分布
     *    echo "stats sizes" | nc localhost 11211
     *    ```
     * 
     *    (2) 关键指标
     *    - 命中率：get_hits / (get_hits + get_misses)
     *    - 淘汰率：evictions / total_items
     *    - 连接数：curr_connections / max_connections
     *    - 内存使用率：bytes / limit_maxbytes
     *    - 网络吞吐：bytes_read + bytes_written
     */
    public void performanceTuning() {
        System.out.println("性能调优：服务端配置 + 系统参数 + 客户端优化 + 数据设计 + 监控分析");
    }

    /**
     * ====================
     * 三、分布式和高可用问题
     * ====================
     */

    /**
     * Q6: Memcached的分布式实现方案？
     * 
     * A: Memcached分布式部署策略：
     * 
     * 1. 客户端分片（推荐方案）
     *    
     *    (1) 一致性哈希
     *    算法原理：
     *    - 构建哈希环：将0到2^32-1的数值空间组织成环
     *    - 节点映射：每个memcached节点映射到环上的位置
     *    - 数据定位：根据key的哈希值顺时针查找最近节点
     *    - 容错性：节点故障时数据迁移最小化
     * 
     *    实现示例：
     *    ```java
     *    public class ConsistentHash {
     *        private TreeMap<Long, String> ring = new TreeMap<>();
     *        private int virtualNodes = 150; // 虚拟节点数量
     *        
     *        public void addNode(String node) {
     *            for (int i = 0; i < virtualNodes; i++) {
     *                String virtualNode = node + "#" + i;
     *                long hash = hash(virtualNode);
     *                ring.put(hash, node);
     *            }
     *        }
     *        
     *        public String getNode(String key) {
     *            long hash = hash(key);
     *            Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
     *            if (entry == null) {
     *                entry = ring.firstEntry(); // 环形结构
     *            }
     *            return entry.getValue();
     *        }
     *    }
     *    ```
     * 
     *    (2) 简单哈希
     *    ```java
     *    // 简单取模方式
     *    int serverIndex = Math.abs(key.hashCode()) % serverList.size();
     *    String server = serverList.get(serverIndex);
     *    ```
     * 
     * 2. 代理分片方案
     *    
     *    常用代理：
     *    - twemproxy（nutcracker）：Twitter开源的代理
     *    - mcrouter：Facebook开源的路由器
     *    - codis：豌豆荚开源的Redis代理（也支持Memcached）
     * 
     *    twemproxy配置：
     *    ```yaml
     *    alpha:
     *      listen: 127.0.0.1:22121
     *      hash: fnv1a_64
     *      distribution: ketama
     *      auto_eject_hosts: true
     *      redis: false
     *      server_retry_timeout: 2000
     *      servers:
     *       - 127.0.0.1:11211:1 server1
     *       - 127.0.0.1:11212:1 server2
     *       - 127.0.0.1:11213:1 server3
     *    ```
     * 
     * 3. 故障处理策略
     *    
     *    (1) 节点故障检测
     *    - 心跳检测：定期ping节点检查存活状态
     *    - 超时检测：请求超时认为节点故障
     *    - 错误计数：连续错误超过阈值标记故障
     * 
     *    (2) 故障转移机制
     *    - 剔除故障节点：将故障节点从可用列表移除
     *    - 数据重分布：将故障节点的数据重新分配
     *    - 自动恢复：故障节点恢复后重新加入集群
     * 
     * 4. 扩容缩容策略
     *    
     *    平滑扩容：
     *    1. 添加新节点到集群配置
     *    2. 更新客户端的节点列表
     *    3. 逐步迁移部分数据到新节点
     *    4. 验证数据完整性
     *    5. 完成扩容操作
     * 
     *    缩容处理：
     *    1. 标记要下线的节点
     *    2. 停止向该节点写入新数据
     *    3. 将该节点的数据迁移到其他节点
     *    4. 验证数据迁移完成
     *    5. 从集群中移除节点
     */
    public void distributedDeployment() {
        System.out.println("分布式：一致性哈希 + 客户端分片 + 故障转移 + 平滑扩容");
    }

    /**
     * Q7: Memcached高可用部署方案？
     * 
     * A: Memcached高可用架构设计：
     * 
     * 1. 数据冗余策略
     *    
     *    (1) 主从复制（客户端实现）
     *    ```java
     *    public void set(String key, Object value) {
     *        String primaryServer = getServer(key);
     *        String replicaServer = getReplicaServer(key);
     *        
     *        // 写入主服务器
     *        primaryClient.set(key, value);
     *        // 异步写入从服务器
     *        asyncWrite(replicaServer, key, value);
     *    }
     *    
     *    public Object get(String key) {
     *        String primaryServer = getServer(key);
     *        Object value = primaryClient.get(key);
     *        
     *        if (value == null) {
     *            // 主服务器未命中，尝试从服务器
     *            String replicaServer = getReplicaServer(key);
     *            value = replicaClient.get(key);
     *        }
     *        return value;
     *    }
     *    ```
     * 
     *    (2) 多副本策略
     *    - 每个key在多个节点存储
     *    - 客户端负责维护副本一致性
     *    - 读取时优先选择最近的副本
     *    - 写入时更新所有副本
     * 
     * 2. 故障检测和切换
     *    
     *    (1) 健康检查机制
     *    ```java
     *    @Component
     *    public class MemcachedHealthChecker {
     *        
     *        @Scheduled(fixedDelay = 5000) // 5秒检查一次
     *        public void healthCheck() {
     *            for (MemcachedServer server : servers) {
     *                try {
     *                    // 发送简单的stats命令检查存活
     *                    Map<String, String> stats = server.getStats();
     *                    server.setHealthy(true);
     *                } catch (Exception e) {
     *                    server.setHealthy(false);
     *                    handleServerFailure(server);
     *                }
     *            }
     *        }
     *    }
     *    ```
     * 
     *    (2) 故障切换流程
     *    1. 检测到节点故障
     *    2. 将故障节点标记为不可用
     *    3. 将流量切换到备用节点
     *    4. 通知监控系统
     *    5. 启动故障恢复流程
     * 
     * 3. 缓存预热策略
     *    
     *    (1) 数据预热
     *    ```java
     *    @EventListener
     *    public void onServerRecover(ServerRecoveryEvent event) {
     *        MemcachedServer server = event.getServer();
     *        
     *        // 从数据库预热热点数据
     *        List<String> hotKeys = getHotKeys();
     *        for (String key : hotKeys) {
     *            Object value = database.get(key);
     *            if (value != null) {
     *                server.set(key, value);
     *            }
     *        }
     *    }
     *    ```
     * 
     *    (2) 渐进式预热
     *    - 新节点上线后逐步承担流量
     *    - 根据实际访问情况填充缓存
     *    - 监控缓存命中率指导预热策略
     * 
     * 4. 监控和告警
     *    
     *    关键监控指标：
     *    ```bash
     *    # 可用性监控
     *    - 节点存活状态
     *    - 响应时间
     *    - 连接成功率
     *    
     *    # 性能监控
     *    - 命中率 (get_hits / total_gets)
     *    - QPS (cmd_get + cmd_set per second)
     *    - 内存使用率 (bytes / limit_maxbytes)
     *    - 网络吞吐量
     *    
     *    # 容量监控
     *    - 当前item数量
     *    - 内存使用量
     *    - 连接数
     *    - 淘汰频率
     *    ```
     * 
     * 5. 灾备方案
     *    
     *    (1) 跨机房部署
     *    - 主机房：承担主要流量
     *    - 备机房：故障时接管流量
     *    - 数据同步：定期同步热点数据
     * 
     *    (2) 降级策略
     *    - 缓存失效时直接访问数据库
     *    - 限制非核心功能的缓存使用
     *    - 使用本地缓存作为L2缓存
     *    - 返回默认值或静态内容
     */
    public void highAvailability() {
        System.out.println("高可用：数据冗余 + 故障切换 + 缓存预热 + 监控告警 + 灾备方案");
    }

    /**
     * ====================
     * 四、对比分析问题
     * ====================
     */

    /**
     * Q8: Memcached与Redis的详细对比？
     * 
     * A: Memcached vs Redis技术对比：
     * 
     * 1. 数据类型支持
     *    
     *    Memcached：
     *    ✗ 仅支持String类型
     *    ✗ 简单的Key-Value存储
     *    ✗ 无复杂数据结构
     *    
     *    Redis：
     *    ✓ 丰富数据类型：String、Hash、List、Set、ZSet
     *    ✓ 高级数据结构：Bitmap、HyperLogLog、GEO
     *    ✓ 支持数据结构操作
     * 
     * 2. 持久化机制
     *    
     *    Memcached：
     *    ✗ 无持久化支持
     *    ✗ 重启后数据丢失
     *    ✗ 纯内存存储
     *    
     *    Redis：
     *    ✓ RDB快照持久化
     *    ✓ AOF日志持久化  
     *    ✓ 混合持久化模式
     *    ✓ 数据安全性高
     * 
     * 3. 集群和分布式
     *    
     *    Memcached：
     *    - 客户端分片：需要客户端实现分布式逻辑
     *    - 无原生集群：服务器间无通信
     *    - 简单架构：部署和维护相对简单
     *    - 一致性哈希：客户端实现数据分布
     *    
     *    Redis：
     *    - 原生集群：Redis Cluster自动分片
     *    - 主从复制：内置复制机制
     *    - 哨兵模式：自动故障转移
     *    - 复杂架构：功能丰富但运维复杂
     * 
     * 4. 内存管理
     *    
     *    Memcached：
     *    - Slab Allocator：预分配固定大小内存块
     *    - 内存利用：可能存在内存浪费
     *    - LRU淘汰：按Slab Class分别进行LRU
     *    - 内存碎片：碎片问题相对较少
     *    
     *    Redis：
     *    - 动态分配：根据实际需要分配内存
     *    - 内存优化：紧凑的内存布局
     *    - 多种淘汰：支持多种淘汰策略
     *    - 内存碎片：可能存在碎片问题
     * 
     * 5. 性能特点
     *    
     *    Memcached：
     *    - 多线程：支持多线程并发
     *    - 简单协议：协议开销小
     *    - 纯内存：极高的访问速度
     *    - CPU多核：能充分利用多核CPU
     *    
     *    Redis：
     *    - 单线程：主处理线程为单线程
     *    - 复杂功能：支持更多复杂操作
     *    - I/O多路复用：高效的网络处理
     *    - 6.0后多线程：网络I/O支持多线程
     * 
     * 6. 使用场景对比
     *    
     *    选择Memcached的场景：
     *    - 简单缓存需求：只需要Key-Value缓存
     *    - 高并发读写：需要极高的读写性能
     *    - 大规模集群：需要简单的横向扩展
     *    - 内存充足：不需要持久化存储
     *    - 多线程优势：CPU多核心环境
     *    
     *    选择Redis的场景：
     *    - 复杂数据结构：需要List、Set、Hash等
     *    - 数据持久化：需要数据安全保证
     *    - 高级功能：需要发布订阅、事务等
     *    - 运维便利：需要完整的集群解决方案
     *    - 功能丰富：一体化的缓存和存储方案
     * 
     * 7. 性能基准测试
     *    
     *    典型性能数据（仅供参考）：
     *    ```
     *    操作类型       Memcached    Redis
     *    SET操作        ~100K QPS    ~80K QPS
     *    GET操作        ~120K QPS    ~100K QPS
     *    内存开销       较小          较大
     *    CPU使用        充分利用多核   单核限制
     *    延迟           微秒级        微秒级
     *    ```
     */
    public void memcachedVsRedis() {
        System.out.println("对比Redis：数据类型 + 持久化 + 集群 + 内存管理 + 性能 + 场景");
    }

    /**
     * ====================
     * 五、实际应用问题
     * ====================
     */

    /**
     * Q9: 在微服务架构中如何使用Memcached？
     * 
     * A: 微服务中的Memcached应用实践：
     * 
     * 1. 架构设计
     *    
     *    服务层次：
     *    ```
     *    API Gateway
     *         ↓
     *    Business Services (User, Order, Product...)
     *         ↓
     *    Cache Layer (Memcached Cluster)
     *         ↓
     *    Data Layer (MySQL, MongoDB...)
     *    ```
     * 
     * 2. 缓存策略
     *    
     *    (1) Cache-Aside模式（推荐）
     *    ```java
     *    @Service
     *    public class UserService {
     *        
     *        @Autowired
     *        private MemcachedClient memcachedClient;
     *        
     *        @Autowired
     *        private UserRepository userRepository;
     *        
     *        public User getUserById(Long id) {
     *            String key = "user:" + id;
     *            
     *            // 1. 先查缓存
     *            User user = (User) memcachedClient.get(key);
     *            if (user != null) {
     *                return user; // 缓存命中
     *            }
     *            
     *            // 2. 缓存未命中，查数据库
     *            user = userRepository.findById(id);
     *            if (user != null) {
     *                // 3. 写入缓存，设置1小时过期
     *                memcachedClient.set(key, 3600, user);
     *            }
     *            return user;
     *        }
     *        
     *        public void updateUser(User user) {
     *            // 1. 更新数据库
     *            userRepository.save(user);
     *            
     *            // 2. 删除缓存（懒惰删除）
     *            String key = "user:" + user.getId();
     *            memcachedClient.delete(key);
     *        }
     *    }
     *    ```
     * 
     *    (2) Write-Through模式
     *    ```java
     *    public void createOrder(Order order) {
     *        // 同时写入数据库和缓存
     *        orderRepository.save(order);
     *        String key = "order:" + order.getId();
     *        memcachedClient.set(key, 3600, order);
     *    }
     *    ```
     * 
     * 3. 缓存设计模式
     *    
     *    (1) 多级缓存
     *    ```java
     *    @Component
     *    public class MultiLevelCache {
     *        
     *        private final Map<String, Object> localCache = new ConcurrentHashMap<>();
     *        private final MemcachedClient memcachedClient;
     *        
     *        public Object get(String key) {
     *            // L1: 本地缓存
     *            Object value = localCache.get(key);
     *            if (value != null) return value;
     *            
     *            // L2: Memcached
     *            value = memcachedClient.get(key);
     *            if (value != null) {
     *                localCache.put(key, value);
     *                return value;
     *            }
     *            
     *            // L3: 数据库
     *            value = database.get(key);
     *            if (value != null) {
     *                memcachedClient.set(key, 3600, value);
     *                localCache.put(key, value);
     *            }
     *            return value;
     *        }
     *    }
     *    ```
     * 
     *    (2) 缓存预热
     *    ```java
     *    @EventListener
     *    public void onApplicationReady(ApplicationReadyEvent event) {
     *        // 预热热点数据
     *        List<Product> hotProducts = productService.getHotProducts();
     *        for (Product product : hotProducts) {
     *            String key = "product:" + product.getId();
     *            memcachedClient.set(key, 7200, product);
     *        }
     *    }
     *    ```
     * 
     * 4. 配置管理
     *    
     *    Spring Boot配置：
     *    ```yaml
     *    memcached:
     *      servers: 
     *        - host: memcached-1.service.consul
     *          port: 11211
     *          weight: 1
     *        - host: memcached-2.service.consul  
     *          port: 11211
     *          weight: 1
     *      pool:
     *        init-connections: 10
     *        min-connections: 5
     *        max-connections: 50
     *        max-idle-time: 300
     *        socket-timeout: 3000
     *        socket-connect-timeout: 1000
     *      failover: true
     *      hash-algorithm: KETAMA_HASH
     *    ```
     * 
     * 5. 监控和运维
     *    
     *    (1) 指标监控
     *    ```java
     *    @Component
     *    public class MemcachedMetrics {
     *        
     *        private final MeterRegistry meterRegistry;
     *        private final MemcachedClient client;
     *        
     *        @Scheduled(fixedDelay = 30000)
     *        public void recordMetrics() {
     *            Map<SocketAddress, Map<String, String>> stats = 
     *                client.getStats();
     *                
     *            for (Map.Entry<SocketAddress, Map<String, String>> entry : 
     *                 stats.entrySet()) {
     *                 
     *                Map<String, String> serverStats = entry.getValue();
     *                String server = entry.getKey().toString();
     *                
     *                // 记录命中率
     *                long hits = Long.parseLong(serverStats.get("get_hits"));
     *                long misses = Long.parseLong(serverStats.get("get_misses"));
     *                double hitRate = hits / (double)(hits + misses);
     *                
     *                Gauge.builder("memcached.hit.rate")
     *                     .tag("server", server)
     *                     .register(meterRegistry, () -> hitRate);
     *            }
     *        }
     *    }
     *    ```
     * 
     *    (2) 告警规则
     *    - 命中率低于80%：可能需要优化缓存策略
     *    - 内存使用率高于90%：需要扩容或清理
     *    - 连接数接近上限：需要调整连接池配置
     *    - 响应时间超过阈值：可能存在网络或服务问题
     * 
     * 6. 最佳实践
     *    - 统一Key命名规范：service:entity:id格式
     *    - 合理设置TTL：根据业务特点设置过期时间
     *    - 缓存空值：防止缓存穿透
     *    - 异步更新：使用消息队列异步更新缓存
     *    - 降级策略：缓存不可用时的降级处理
     */
    public void microserviceUsage() {
        System.out.println("微服务应用：Cache-Aside + 多级缓存 + 配置管理 + 监控告警 + 最佳实践");
    }
}