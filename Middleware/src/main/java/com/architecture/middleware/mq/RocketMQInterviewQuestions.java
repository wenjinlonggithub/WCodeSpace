package com.architecture.middleware.mq;

/**
 * RocketMQ最新面试问题汇总（2024-2025年）
 * 
 * 涵盖分布式事务、高可用架构、性能调优、顺序消息等方面
 */

import org.springframework.stereotype.Component;

@Component
public class RocketMQInterviewQuestions {

    /**
     * ====================
     * 一、基础架构问题
     * ====================
     */

    /**
     * Q1: RocketMQ的整体架构及核心组件？
     * 
     * A: RocketMQ分布式消息架构：
     * 
     * 1. 核心组件
     *    
     *    (1) NameServer（注册中心）
     *    - 作用：管理Broker路由信息
     *    - 特点：无状态、可集群部署
     *    - 功能：Broker注册、路由发现、故障剔除
     *    - 优势：去中心化设计，避免单点故障
     * 
     *    (2) Broker（消息服务器）
     *    - 作用：存储消息、处理生产消费请求
     *    - 模式：Master-Slave主从架构
     *    - 功能：消息存储、索引服务、HA保证
     *    - 特点：支持分布式集群部署
     * 
     *    (3) Producer（消息生产者）
     *    - 发送模式：同步、异步、单向
     *    - 负载均衡：支持多种队列选择策略
     *    - 事务支持：分布式事务消息
     *    - 故障容错：自动故障规避和恢复
     * 
     *    (4) Consumer（消息消费者）
     *    - 消费模式：Push、Pull两种模式
     *    - 消费类型：集群消费、广播消费
     *    - 负载均衡：自动队列分配和重平衡
     *    - 消费顺序：支持顺序和并发消费
     * 
     * 2. 架构特点
     *    - 去中心化：NameServer集群无状态
     *    - 高可用：主从架构+自动切换
     *    - 高性能：零拷贝、顺序写、异步刷盘
     *    - 可扩展：水平扩展Broker和队列
     *    - 强一致：支持分布式事务
     */
    public void rocketmqArchitecture() {
        System.out.println("架构：NameServer + Broker集群 + Producer/Consumer + 主从HA");
    }

    /**
     * Q2: RocketMQ与Kafka的主要区别？
     * 
     * A: RocketMQ vs Kafka对比：
     * 
     * 1. 架构设计
     *    RocketMQ：
     *    ✓ 去中心化NameServer
     *    ✓ 主从架构自动切换
     *    ✓ Topic队列数可动态调整
     *    ✓ 支持事务消息
     *    
     *    Kafka：
     *    ✓ 依赖ZooKeeper（3.0+可选）
     *    ✓ 分区副本机制
     *    ✓ 分区数创建后难调整
     *    ✓ 事务支持相对较晚
     * 
     * 2. 消息顺序
     *    RocketMQ：
     *    - 支持全局顺序和分区顺序
     *    - 提供顺序消息专门的API
     *    - 队列级别严格顺序保证
     *    
     *    Kafka：
     *    - 分区内有序，分区间无序
     *    - 需要业务层保证顺序性
     * 
     * 3. 消息查询
     *    RocketMQ：
     *    ✓ 支持按Message Key查询
     *    ✓ 支持按时间范围查询  
     *    ✓ 提供管理控制台
     *    
     *    Kafka：
     *    ✗ 查询能力相对较弱
     *    ✗ 主要通过offset查询
     * 
     * 4. 消息重试和死信
     *    RocketMQ：
     *    ✓ 内置重试机制
     *    ✓ 自动死信队列处理
     *    ✓ 支持延迟消息
     *    
     *    Kafka：
     *    ✗ 需要业务层实现重试
     *    ✗ 无内置死信机制
     * 
     * 5. 运维复杂度
     *    RocketMQ：
     *    - 部署相对简单
     *    - 管理工具较完善
     *    - 中文文档丰富
     *    
     *    Kafka：
     *    - 配置参数较多
     *    - 运维复杂度较高
     *    - 社区生态更成熟
     */
    public void rocketmqVsKafka() {
        System.out.println("对比：架构 + 顺序性 + 查询能力 + 重试机制 + 运维复杂度");
    }

    /**
     * Q3: RocketMQ的消息存储模型？
     * 
     * A: RocketMQ三层存储架构：
     * 
     * 1. CommitLog（物理存储层）
     *    - 设计：所有Topic消息顺序写入同一文件
     *    - 文件大小：1GB（可配置）
     *    - 命名规则：20位起始偏移量（如00000000000000000000）
     *    - 消息格式：包含完整消息内容和元数据
     *    - 优势：顺序写入，磁盘I/O性能最优
     * 
     * 2. ConsumeQueue（逻辑队列层）
     *    - 作用：每个Topic的每个队列对应一个ConsumeQueue文件
     *    - 内容：存储消息在CommitLog中的位置索引
     *    - 格式：每条记录20字节（offset + size + tagHashCode）
     *    - 功能：支持按队列消费和Tag过滤
     * 
     * 3. IndexFile（索引层）
     *    - 用途：支持按Message Key和时间范围查询
     *    - 结构：Header + 500万个Hash槽 + 2000万个Index条目
     *    - 冲突解决：链表法处理Hash冲突
     *    - 查询性能：O(1)时间复杂度
     * 
     * 存储优势：
     * - 顺序写入：CommitLog顺序写，性能接近内存
     * - 零拷贝：使用mmap和sendfile优化I/O
     * - 批量读取：ConsumeQueue支持批量读取
     * - 快速检索：IndexFile支持按Key快速查找
     */
    public void storageModel() {
        System.out.println("存储模型：CommitLog顺序写 + ConsumeQueue索引 + IndexFile检索");
    }

    /**
     * ====================
     * 二、分布式事务问题
     * ====================
     */

    /**
     * Q4: RocketMQ分布式事务消息实现原理？
     * 
     * A: 分布式事务消息详解：
     * 
     * 1. 应用场景
     *    - 订单支付：扣款成功后发送订单状态变更消息
     *    - 账户转账：A账户扣款后通知B账户入账
     *    - 库存扣减：下单成功后扣减库存
     *    - 数据同步：主库更新后同步到从库或缓存
     * 
     * 2. 实现原理（两阶段提交）
     *    
     *    Phase 1：发送Half消息
     *    ```java
     *    // 1. 发送Half消息到Broker
     *    TransactionSendResult result = producer.sendMessageInTransaction(msg, arg);
     *    // 2. Half消息存储在特殊Topic：RMQ_SYS_TRANS_HALF_TOPIC
     *    // 3. 消费者无法看到Half消息
     *    ```
     * 
     *    Phase 2：执行本地事务
     *    ```java
     *    @Override
     *    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
     *        try {
     *            // 执行本地事务（如数据库操作）
     *            doLocalTransaction(arg);
     *            return LocalTransactionState.COMMIT_MESSAGE;  // 提交
     *        } catch (Exception e) {
     *            return LocalTransactionState.ROLLBACK_MESSAGE; // 回滚
     *        }
     *    }
     *    ```
     * 
     *    Phase 3：事务确认
     *    - COMMIT：Half消息转为正常消息，消费者可见
     *    - ROLLBACK：Half消息标记删除，不会投递
     *    - UNKNOWN：等待回查机制处理
     * 
     * 3. 事务回查机制
     *    
     *    回查触发条件：
     *    - Half消息超过6秒未收到确认
     *    - 网络异常导致确认丢失
     *    - Producer重启导致状态丢失
     * 
     *    回查实现：
     *    ```java
     *    @Override
     *    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
     *        // 根据消息内容检查本地事务状态
     *        String orderId = new String(msg.getBody());
     *        if (isOrderPaid(orderId)) {
     *            return LocalTransactionState.COMMIT_MESSAGE;
     *        } else if (isOrderCancelled(orderId)) {
     *            return LocalTransactionState.ROLLBACK_MESSAGE;
     *        } else {
     *            return LocalTransactionState.UNKNOW; // 继续回查
     *        }
     *    }
     *    ```
     * 
     * 4. 配置参数
     *    ```properties
     *    # 事务消息回查间隔（默认60秒）
     *    transactionCheckInterval=60000
     *    # 最大回查次数（默认15次）
     *    transactionCheckMax=15
     *    # 事务消息超时时间（默认6小时）
     *    transactionTimeOut=21600000
     *    ```
     * 
     * 5. 最佳实践
     *    - 保证本地事务幂等性
     *    - 合理设计回查逻辑
     *    - 监控事务消息处理状态
     *    - 设置合适的超时时间
     */
    public void transactionalMessage() {
        System.out.println("事务消息：Half消息 → 本地事务 → 二次确认 → 回查机制");
    }

    /**
     * Q5: RocketMQ如何保证消息的可靠性？
     * 
     * A: 消息可靠性保证机制：
     * 
     * 1. 发送端可靠性
     *    
     *    (1) 发送确认
     *    - 同步发送：等待Broker确认后返回
     *    - 异步发送：回调函数获取发送结果
     *    - SendStatus状态检查：SEND_OK表示成功
     * 
     *    (2) 重试机制
     *    ```java
     *    // 设置重试次数
     *    producer.setRetryTimesWhenSendFailed(3);
     *    // 异步发送重试次数
     *    producer.setRetryTimesWhenSendAsyncFailed(3);
     *    ```
     * 
     *    (3) 故障规避
     *    - 自动避开故障Broker
     *    - 延迟容错机制
     *    - 自动恢复机制
     * 
     * 2. 存储端可靠性
     *    
     *    (1) 刷盘策略
     *    ```properties
     *    # 同步刷盘：消息写入磁盘后返回
     *    flushDiskType=SYNC_FLUSH
     *    # 异步刷盘：写入页缓存后返回（默认）
     *    flushDiskType=ASYNC_FLUSH
     *    ```
     * 
     *    (2) 主从同步
     *    ```properties
     *    # 同步复制：消息同步到Slave后返回
     *    brokerRole=SYNC_MASTER
     *    # 异步复制：写入Master后返回（默认）
     *    brokerRole=ASYNC_MASTER
     *    ```
     * 
     *    (3) DLedger模式
     *    - 基于Raft协议
     *    - 强一致性保证
     *    - 自动故障切换
     * 
     * 3. 消费端可靠性
     *    
     *    (1) 消费确认
     *    ```java
     *    @Override
     *    public ConsumeOrderlyStatus consumeMessage(
     *            List<MessageExt> msgs,
     *            ConsumeOrderlyContext context) {
     *        try {
     *            // 处理业务逻辑
     *            processMessage(msgs);
     *            return ConsumeOrderlyStatus.SUCCESS;  // 消费成功
     *        } catch (Exception e) {
     *            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT; // 稍后重试
     *        }
     *    }
     *    ```
     * 
     *    (2) 重试机制
     *    - 最大重试次数：默认16次
     *    - 重试间隔：递增延迟（10s, 30s, 1m, 2m, 3m, ...）
     *    - 死信队列：超过重试次数自动进入DLQ
     * 
     * 4. 端到端可靠性
     *    - 消息去重：业务层实现幂等性
     *    - 消息追踪：全链路监控
     *    - 补偿机制：异常情况的补偿处理
     */
    public void messageReliability() {
        System.out.println("可靠性：发送确认 + 刷盘同步 + 消费确认 + 重试机制");
    }

    /**
     * ====================
     * 三、顺序消息问题
     * ====================
     */

    /**
     * Q6: RocketMQ如何保证消息顺序？
     * 
     * A: 顺序消息实现机制：
     * 
     * 1. 顺序类型
     *    
     *    (1) 全局顺序
     *    - 整个Topic只有一个队列
     *    - 所有消息严格按发送顺序消费
     *    - 性能较低，不推荐
     * 
     *    (2) 分区顺序（推荐）
     *    - Topic有多个队列
     *    - 相关消息发送到同一队列
     *    - 队列内顺序，队列间并行
     * 
     * 2. 顺序发送实现
     *    
     *    使用MessageQueueSelector：
     *    ```java
     *    // 根据订单ID选择队列，保证同一订单的消息有序
     *    SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
     *        @Override
     *        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
     *            String orderId = (String) arg;
     *            int index = orderId.hashCode() % mqs.size();
     *            return mqs.get(index);
     *        }
     *    }, orderId);
     *    ```
     * 
     * 3. 顺序消费实现
     *    
     *    使用MessageListenerOrderly：
     *    ```java
     *    consumer.registerMessageListener(new MessageListenerOrderly() {
     *        @Override
     *        public ConsumeOrderlyStatus consumeMessage(
     *                List<MessageExt> msgs,
     *                ConsumeOrderlyContext context) {
     *            // 顺序处理消息
     *            for (MessageExt msg : msgs) {
     *                processMessage(msg);
     *            }
     *            return ConsumeOrderlyStatus.SUCCESS;
     *        }
     *    });
     *    ```
     * 
     * 4. 顺序保证机制
     *    
     *    (1) Producer端
     *    - 同一业务ID的消息发送到同一队列
     *    - 发送失败时重试不换队列
     *    - 使用同步发送避免乱序
     * 
     *    (2) Broker端
     *    - 队列内消息严格FIFO
     *    - 单线程写入保证顺序
     *    - 消息存储按接收顺序
     * 
     *    (3) Consumer端
     *    - 每个队列只能有一个消费线程
     *    - 顺序锁机制保证串行消费
     *    - 消费失败时暂停当前队列
     * 
     * 5. 性能优化
     *    - 增加队列数量提高并行度
     *    - 合理设计分区key
     *    - 避免热点队列
     *    - 监控消费延迟
     */
    public void orderedMessage() {
        System.out.println("顺序消息：队列选择 → 单线程消费 → 顺序锁 → 失败处理");
    }

    /**
     * Q7: RocketMQ延迟消息实现？
     * 
     * A: 延迟消息实现机制：
     * 
     * 1. 延迟级别定义
     *    RocketMQ支持18个延迟级别：
     *    ```
     *    1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     *    ```
     *    
     *    设置延迟级别：
     *    ```java
     *    Message msg = new Message("TopicTest", "TagA", "Hello RocketMQ".getBytes());
     *    // 设置延迟级别3，即10秒后投递
     *    msg.setDelayTimeLevel(3);
     *    producer.send(msg);
     *    ```
     * 
     * 2. 实现原理
     *    
     *    (1) 消息存储
     *    - 延迟消息先存储在SCHEDULE_TOPIC_XXXX
     *    - 按延迟级别创建不同队列
     *    - 设置投递时间戳
     * 
     *    (2) 延迟投递
     *    - ScheduleMessageService定时扫描
     *    - 检查消息是否到达投递时间
     *    - 到时间后投递到真实Topic
     *    - Consumer才能消费到消息
     * 
     * 3. 使用场景
     *    - 订单超时取消：30分钟后检查订单状态
     *    - 定时提醒：会议前10分钟发送提醒
     *    - 重试间隔：失败后延迟重试
     *    - 定时任务：简单的定时任务实现
     * 
     * 4. 配置优化
     *    ```properties
     *    # 自定义延迟级别（需要重启Broker）
     *    messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     *    # 延迟队列拉取间隔
     *    scheduleAsyncDeliverDelay=200
     *    ```
     * 
     * 5. 限制和注意事项
     *    - 只支持预设的18个延迟级别
     *    - 不支持任意时间延迟
     *    - 延迟时间不能过长（最大2小时）
     *    - 可能有轻微的时间误差
     */
    public void delayedMessage() {
        System.out.println("延迟消息：延迟级别 → 临时存储 → 定时扫描 → 到期投递");
    }

    /**
     * ====================
     * 四、集群和高可用问题
     * ====================
     */

    /**
     * Q8: RocketMQ集群部署模式？
     * 
     * A: RocketMQ集群部署策略：
     * 
     * 1. 部署架构
     *    
     *    (1) NameServer集群
     *    - 部署模式：无状态集群
     *    - 节点数量：2-3个节点（奇数推荐）
     *    - 数据同步：无需同步，各节点独立
     *    - 故障处理：客户端自动切换
     * 
     *    (2) Broker集群
     *    - 主从模式：每个Master配1-2个Slave
     *    - 多Master：多个Master节点负载分担
     *    - 混合模式：Master-Slave + 多Master
     * 
     * 2. 常用部署模式
     *    
     *    (1) 多Master模式
     *    ```
     *    优点：配置简单，性能最高
     *    缺点：Master宕机期间不可用
     *    适用：对可用性要求不高的场景
     *    ```
     * 
     *    (2) 多Master多Slave异步复制
     *    ```
     *    优点：性能高，Master宕机时Slave可读
     *    缺点：Master宕机可能丢少量消息
     *    适用：对性能要求高，允许少量丢失
     *    ```
     * 
     *    (3) 多Master多Slave同步复制
     *    ```
     *    优点：数据可靠性最高，无消息丢失
     *    缺点：性能稍低，延迟较高
     *    适用：对数据一致性要求极高
     *    ```
     * 
     *    (4) DLedger自动切换模式
     *    ```
     *    优点：自动故障切换，运维简单
     *    缺点：资源消耗较大，配置复杂
     *    适用：对自动化运维要求高
     *    ```
     * 
     * 3. 配置示例
     *    
     *    Master配置：
     *    ```properties
     *    brokerClusterName=DefaultCluster
     *    brokerName=broker-a
     *    brokerId=0
     *    brokerRole=SYNC_MASTER
     *    flushDiskType=ASYNC_FLUSH
     *    ```
     * 
     *    Slave配置：
     *    ```properties
     *    brokerClusterName=DefaultCluster
     *    brokerName=broker-a  
     *    brokerId=1
     *    brokerRole=SLAVE
     *    flushDiskType=ASYNC_FLUSH
     *    ```
     * 
     * 4. 容量规划
     *    - 消息量评估：日均消息数 × 消息大小
     *    - 存储容量：消息量 × 保存天数 × 副本数
     *    - 网络带宽：峰值TPS × 消息大小 × 2
     *    - 服务器配置：CPU 16核+，内存 32GB+，磁盘 SSD
     */
    public void clusterDeployment() {
        System.out.println("集群部署：NameServer集群 + Broker主从 + 容量规划 + 配置优化");
    }

    /**
     * Q9: RocketMQ的故障切换机制？
     * 
     * A: 故障切换和恢复机制：
     * 
     * 1. 传统主从故障切换
     *    
     *    故障检测：
     *    - NameServer检测Broker心跳超时（120秒）
     *    - 从路由表中移除故障Broker
     *    - 通知Producer/Consumer更新路由
     * 
     *    故障处理：
     *    - Master宕机：Slave提供读服务，写服务不可用
     *    - Slave宕机：不影响Master正常服务
     *    - 手动切换：运维人员手动提升Slave为Master
     * 
     * 2. DLedger自动切换（推荐）
     *    
     *    基于Raft协议：
     *    - Leader选举：故障时自动选举新Leader
     *    - 数据同步：强一致性数据复制
     *    - 脑裂处理：多数派原则避免脑裂
     * 
     *    配置DLedger：
     *    ```properties
     *    enableDLegerCommitLog=true
     *    dLegerGroup=DefaultCluster
     *    dLegerPeers=n0-127.0.0.1:40911;n1-127.0.0.1:40912;n2-127.0.0.1:40913
     *    dLegerSelfId=n0
     *    ```
     * 
     * 3. 客户端故障处理
     *    
     *    Producer故障处理：
     *    - 自动重试机制
     *    - 故障Broker规避
     *    - 延迟容错策略
     * 
     *    Consumer故障处理：
     *    - 重新平衡机制
     *    - 消费进度恢复
     *    - 自动重连机制
     * 
     * 4. 监控和告警
     *    - Broker存活状态监控
     *    - 主从同步延迟监控
     *    - 消息堆积监控
     *    - 异常日志告警
     */
    public void failoverMechanism() {
        System.out.println("故障切换：心跳检测 + 路由更新 + DLedger选举 + 客户端重连");
    }

    /**
     * ====================
     * 五、性能调优问题
     * ====================
     */

    /**
     * Q10: RocketMQ性能优化策略？
     * 
     * A: 全方位性能优化：
     * 
     * 1. Producer优化
     *    
     *    (1) 批量发送
     *    ```java
     *    // 设置批量发送大小
     *    producer.setMaxMessageSize(4194304); // 4MB
     *    
     *    List<Message> messages = new ArrayList<>();
     *    // 批量添加消息
     *    producer.send(messages);
     *    ```
     * 
     *    (2) 异步发送
     *    ```java
     *    // 异步发送提高吞吐量
     *    producer.send(msg, new SendCallback() {
     *        @Override
     *        public void onSuccess(SendResult sendResult) {}
     *        
     *        @Override
     *        public void onException(Throwable e) {}
     *    });
     *    ```
     * 
     *    (3) 参数优化
     *    ```java
     *    producer.setCompressMsgBodyOverHowMuch(4096); // 压缩阈值
     *    producer.setSendMsgTimeout(10000); // 发送超时
     *    producer.setRetryTimesWhenSendFailed(2); // 重试次数
     *    ```
     * 
     * 2. Consumer优化
     *    
     *    (1) 并发消费
     *    ```java
     *    // 设置消费线程数
     *    consumer.setConsumeThreadMin(20);
     *    consumer.setConsumeThreadMax(64);
     *    
     *    // 设置消费批次大小
     *    consumer.setConsumeMessageBatchMaxSize(32);
     *    ```
     * 
     *    (2) 消费优化
     *    ```java
     *    // 设置拉取间隔
     *    consumer.setPullInterval(0);
     *    // 设置拉取批次大小
     *    consumer.setPullBatchSize(32);
     *    ```
     * 
     * 3. Broker优化
     *    
     *    (1) JVM参数
     *    ```bash
     *    -server -Xms8g -Xmx8g
     *    -XX:+UseG1GC
     *    -XX:G1HeapRegionSize=16m
     *    -XX:G1ReservePercent=25
     *    -XX:InitiatingHeapOccupancyPercent=30
     *    ```
     * 
     *    (2) Broker配置
     *    ```properties
     *    # 发送线程池大小
     *    sendMessageThreadPoolNums=128
     *    # 拉取线程池大小  
     *    pullMessageThreadPoolNums=128
     *    # 刷盘线程数
     *    flushCommitLogLeastPages=4
     *    # 删除文件线程数
     *    deleteWhen=04
     *    ```
     * 
     * 4. 系统优化
     *    
     *    (1) 磁盘优化
     *    ```bash
     *    # 使用SSD磁盘
     *    # RAID10提高性能和可靠性
     *    # 调整磁盘调度算法
     *    echo deadline > /sys/block/sdb/queue/scheduler
     *    ```
     * 
     *    (2) 网络优化
     *    ```bash
     *    # TCP参数调优
     *    net.core.rmem_max = 134217728
     *    net.core.wmem_max = 134217728
     *    net.ipv4.tcp_rmem = 4096 87380 134217728
     *    net.ipv4.tcp_wmem = 4096 65536 134217728
     *    ```
     * 
     * 5. 监控指标
     *    - TPS：每秒处理消息数
     *    - RT：消息处理延迟
     *    - 消息堆积：Consumer Lag
     *    - 系统资源：CPU、内存、磁盘、网络
     */
    public void performanceTuning() {
        System.out.println("性能优化：批量发送 + 并发消费 + JVM调优 + 系统参数");
    }

    /**
     * ====================
     * 六、实际应用问题
     * ====================
     */

    /**
     * Q11: 如何设计基于RocketMQ的电商订单系统？
     * 
     * A: 电商订单系统设计：
     * 
     * 1. 业务场景分析
     *    订单流程：下单 → 支付 → 发货 → 收货 → 评价
     *    涉及服务：订单服务、支付服务、库存服务、物流服务、通知服务
     * 
     * 2. Topic设计
     *    ```
     *    order-events：订单相关事件
     *    ├── OrderCreated：订单创建
     *    ├── OrderPaid：订单支付  
     *    ├── OrderShipped：订单发货
     *    └── OrderCompleted：订单完成
     *    
     *    payment-events：支付相关事件
     *    ├── PaymentInitiated：支付发起
     *    ├── PaymentSuccess：支付成功
     *    └── PaymentFailed：支付失败
     *    
     *    inventory-events：库存相关事件
     *    ├── StockReduced：库存扣减
     *    └── StockRestored：库存恢复
     *    ```
     * 
     * 3. 消息设计
     *    ```json
     *    {
     *      "eventId": "evt_123456",
     *      "eventType": "OrderCreated",
     *      "timestamp": 1640995200000,
     *      "orderId": "ORD_789012",
     *      "userId": "USER_345678",
     *      "data": {
     *        "products": [{"id": "PROD_001", "quantity": 2}],
     *        "totalAmount": 299.99,
     *        "shippingAddress": {...}
     *      }
     *    }
     *    ```
     * 
     * 4. 事务消息应用
     *    
     *    订单支付场景：
     *    ```java
     *    @Transactional
     *    public void processPayment(PaymentRequest request) {
     *        // 发送事务消息
     *        producer.sendMessageInTransaction(paymentMsg, request);
     *    }
     *    
     *    @Override
     *    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
     *        try {
     *            // 执行支付扣款
     *            paymentService.processPayment((PaymentRequest) arg);
     *            return LocalTransactionState.COMMIT_MESSAGE;
     *        } catch (Exception e) {
     *            return LocalTransactionState.ROLLBACK_MESSAGE;
     *        }
     *    }
     *    ```
     * 
     * 5. 顺序消息应用
     *    
     *    订单状态变更：
     *    ```java
     *    // 同一订单的状态变更消息发送到同一队列
     *    producer.send(orderStateMsg, new MessageQueueSelector() {
     *        @Override
     *        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
     *            String orderId = (String) arg;
     *            return mqs.get(orderId.hashCode() % mqs.size());
     *        }
     *    }, orderId);
     *    ```
     * 
     * 6. 延迟消息应用
     *    
     *    订单超时取消：
     *    ```java
     *    // 下单后30分钟检查订单状态
     *    Message timeoutMsg = new Message("order-timeout", orderId.getBytes());
     *    timeoutMsg.setDelayTimeLevel(16); // 30分钟
     *    producer.send(timeoutMsg);
     *    ```
     * 
     * 7. 监控和运维
     *    - 消息积压监控：及时发现Consumer处理能力不足
     *    - 重试消息监控：关注业务处理异常
     *    - 事务消息监控：监控事务回查频率
     *    - 业务指标监控：订单成功率、支付成功率等
     */
    public void ecommerceOrderSystem() {
        System.out.println("订单系统：事件设计 + 事务消息 + 顺序消息 + 延迟消息 + 监控运维");
    }

    /**
     * Q12: RocketMQ在微服务架构中的最佳实践？
     * 
     * A: 微服务架构最佳实践：
     * 
     * 1. 服务解耦
     *    
     *    (1) 事件驱动架构
     *    ```
     *    用户服务 → UserRegistered事件 → [积分服务, 营销服务, 通知服务]
     *    订单服务 → OrderCreated事件 → [库存服务, 支付服务, 物流服务]
     *    ```
     * 
     *    (2) 异步处理
     *    - 降低服务间依赖
     *    - 提高系统响应速度
     *    - 提升系统容错能力
     * 
     * 2. 数据一致性
     *    
     *    (1) 最终一致性
     *    - 通过消息保证最终一致
     *    - 业务补偿机制
     *    - 幂等性设计
     * 
     *    (2) Saga模式
     *    ```
     *    下单saga：创建订单 → 扣库存 → 生成支付单 → 创建物流单
     *    补偿saga：取消物流 → 取消支付 → 恢复库存 → 取消订单
     *    ```
     * 
     * 3. 消息治理
     *    
     *    (1) Topic规划
     *    - 按业务域划分：user-domain, order-domain, payment-domain
     *    - 按事件类型：entity-events, process-events, notification-events
     *    - 版本管理：支持向后兼容的消息格式演进
     * 
     *    (2) 消息标准化
     *    ```json
     *    {
     *      "version": "1.0",
     *      "eventId": "uuid",
     *      "eventType": "OrderCreated",
     *      "timestamp": 1640995200000,
     *      "source": "order-service",
     *      "traceId": "trace-123",
     *      "data": {...}
     *    }
     *    ```
     * 
     * 4. 运维管理
     *    
     *    (1) 多环境隔离
     *    - 开发环境：dev-cluster
     *    - 测试环境：test-cluster  
     *    - 生产环境：prod-cluster
     * 
     *    (2) 配置管理
     *    ```yaml
     *    rocketmq:
     *      nameServer: ${ROCKETMQ_NAME_SERVER:localhost:9876}
     *      producer:
     *        group: ${spring.application.name}-producer
     *        retryTimesWhenSendFailed: 3
     *      consumer:
     *        group: ${spring.application.name}-consumer
     *        consumeThreadMin: 20
     *    ```
     * 
     * 5. 监控和追踪
     *    - 分布式追踪：TraceId在消息中传递
     *    - 指标监控：消息生产消费速率、延迟、错误率
     *    - 告警机制：消息堆积、处理异常告警
     *    - 链路分析：端到端性能分析
     */
    public void microserviceBestPractices() {
        System.out.println("微服务实践：服务解耦 + 数据一致性 + 消息治理 + 运维管理 + 监控追踪");
    }
}