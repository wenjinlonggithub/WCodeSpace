# Kafka 面试题与答案

## 基础概念题

### 1. 什么是 Apache Kafka？
**答案**: Apache Kafka 是一个分布式流处理平台，主要用于构建实时数据管道和流应用程序。它具有高吞吐量、低延迟、持久性和容错性等特点。

### 2. Kafka 的核心组件有哪些？
**答案**: 
- **Producer**: 消息生产者，向 Kafka 发送消息
- **Consumer**: 消息消费者，从 Kafka 读取消息
- **Broker**: Kafka 服务器节点，负责存储和转发消息
- **Topic**: 消息主题，消息的逻辑分类
- **Partition**: 分区，Topic 的物理分割
- **ZooKeeper**: 集群元数据管理和协调服务

### 3. 什么是 Topic 和 Partition？
**答案**: 
- **Topic**: 是消息的逻辑分类，类似于数据库中的表
- **Partition**: 是 Topic 的物理分割，每个分区是一个有序的消息序列，支持并行处理和负载均衡

### 4. Kafka 如何保证消息的顺序性？
**答案**: Kafka 在分区级别保证消息顺序性。同一个分区内的消息是有序的，但不同分区之间的消息顺序无法保证。如果需要全局顺序，可以使用单分区 Topic。

## 架构设计题

### 5. Kafka 的存储机制是什么？
**答案**: 
- Kafka 使用**日志结构存储**，消息以追加方式写入磁盘
- 每个分区对应一个日志文件，分为多个**段文件（Segment）**
- 使用**零拷贝技术**提高 I/O 性能
- 支持**日志压缩**和**日志清理**策略

### 6. 解释 Kafka 的副本机制
**答案**: 
- 每个分区可以有多个**副本（Replica）**
- 副本分为**Leader** 和 **Follower**
- 只有 Leader 处理读写请求，Follower 同步 Leader 的数据
- **ISR（In-Sync Replicas）**：与 Leader 保持同步的副本集合
- 当 Leader 失效时，从 ISR 中选举新的 Leader

### 7. 什么是消费者组（Consumer Group）？
**答案**: 
- 消费者组是一组消费者的逻辑集合
- 同一个消费者组内的消费者**共同消费**一个 Topic 的所有分区
- 每个分区只能被组内的**一个消费者**消费
- 支持**水平扩展**和**故障转移**

## 性能优化题

### 8. 如何优化 Kafka Producer 的性能？
**答案**: 
```java
// 批量发送
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
props.put(ProducerConfig.LINGER_MS_CONFIG, 10);

// 压缩
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

// 异步发送
props.put(ProducerConfig.ACKS_CONFIG, "1");

// 幂等性
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
```

### 9. 如何优化 Kafka Consumer 的性能？
**答案**: 
```java
// 批量拉取
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 50000);

// 并行消费
// 增加消费者实例数量

// 手动提交
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
```

### 10. Kafka 如何实现高吞吐量？
**答案**: 
- **顺序写磁盘**: 利用磁盘顺序写的高性能
- **零拷贝**: 减少数据在内存中的复制
- **批量处理**: 批量发送和接收消息
- **压缩**: 减少网络传输和存储开销
- **分区并行**: 支持多分区并行处理

## 一致性和可靠性题

### 11. Kafka 的 ACK 机制有哪些选项？
**答案**: 
- **acks=0**: 不等待服务器确认，最高性能但可能丢失消息
- **acks=1**: 等待 Leader 确认，平衡性能和可靠性
- **acks=all/-1**: 等待所有 ISR 副本确认，最高可靠性

### 12. 如何保证 Kafka 消息不丢失？
**答案**: 
- **Producer 端**: 设置 `acks=all`，启用重试和幂等性
- **Broker 端**: 设置合适的副本数量，配置 `min.insync.replicas`
- **Consumer 端**: 手动提交 offset，确保消息处理完成后再提交

### 13. 如何处理 Kafka 消息重复消费？
**答案**: 
- **幂等性设计**: 确保消息处理逻辑是幂等的
- **去重机制**: 使用唯一标识符进行去重
- **事务支持**: 使用 Kafka 事务保证 exactly-once 语义
- **手动提交**: 精确控制 offset 提交时机

## 运维监控题

### 14. Kafka 的重要监控指标有哪些？
**答案**: 
- **吞吐量**: 每秒消息数、字节数
- **延迟**: 端到端延迟、生产延迟、消费延迟
- **可用性**: Broker 状态、分区状态、副本同步状态
- **资源使用**: CPU、内存、磁盘、网络使用率
- **消费者滞后**: Consumer Lag

### 15. 如何处理 Kafka 集群扩容？
**答案**: 
- **添加 Broker**: 动态添加新的 Broker 节点
- **分区重分配**: 使用 `kafka-reassign-partitions.sh` 工具
- **数据迁移**: 逐步迁移分区到新节点
- **监控验证**: 确保数据迁移完成且集群稳定

## 高级应用题

### 16. 什么是 Kafka Streams？
**答案**: 
Kafka Streams 是一个轻量级的流处理库，用于构建实时流处理应用程序。特点包括：
- **无需额外集群**: 作为应用程序库运行
- **状态存储**: 支持有状态的流处理
- **容错性**: 自动故障恢复和状态恢复
- **精确一次**: 支持 exactly-once 处理语义

### 17. Kafka Connect 的作用是什么？
**答案**: 
Kafka Connect 是一个数据集成框架，用于连接 Kafka 和外部系统：
- **Source Connector**: 从外部系统导入数据到 Kafka
- **Sink Connector**: 从 Kafka 导出数据到外部系统
- **分布式**: 支持分布式部署和扩展
- **容错**: 自动故障恢复和重试机制

### 18. 如何设计一个基于 Kafka 的事件驱动架构？
**答案**: 
```java
// 事件发布
public class OrderService {
    public void createOrder(Order order) {
        // 业务逻辑
        saveOrder(order);
        
        // 发布事件
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        eventPublisher.publish("order-events", event);
    }
}

// 事件消费
@EventHandler
public class InventoryService {
    @KafkaListener(topics = "order-events")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 更新库存
        updateInventory(event.getOrder());
    }
}
```

## 故障排查题

### 19. 如何排查 Kafka 消费延迟问题？
**答案**: 
1. **检查 Consumer Lag**: 使用 `kafka-consumer-groups.sh` 查看滞后情况
2. **分析消费速度**: 检查消费者处理能力是否跟上生产速度
3. **网络问题**: 检查网络延迟和带宽
4. **资源瓶颈**: 检查 CPU、内存、磁盘 I/O
5. **配置优化**: 调整批量大小、并发度等参数

### 20. Kafka 集群出现脑裂如何处理？
**答案**: 
1. **检查 ZooKeeper**: 确保 ZooKeeper 集群正常
2. **网络分区**: 检查网络连接和防火墙设置
3. **配置检查**: 验证 `min.insync.replicas` 等配置
4. **手动干预**: 必要时手动选举 Leader
5. **预防措施**: 合理配置副本数和 ISR 最小值

## 编程实战题

### 21. 实现一个自定义的 Kafka 分区器
**答案**: 
```java
public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                        Object value, byte[] valueBytes, Cluster cluster) {
        if (key == null) {
            return 0;
        }
        
        // 基于 key 的哈希值分区
        int numPartitions = cluster.partitionCountForTopic(topic);
        return Math.abs(key.hashCode()) % numPartitions;
    }
    
    @Override
    public void configure(Map<String, ?> configs) {}
    
    @Override
    public void close() {}
}
```

### 22. 实现一个 Kafka Streams 应用进行实时聚合
**答案**: 
```java
public class RealTimeAggregationApp {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String, String> events = builder.stream("user-events");
        
        // 实时计数
        KTable<String, Long> eventCounts = events
            .groupByKey()
            .count();
        
        // 时间窗口聚合
        KTable<Windowed<String>, Long> windowedCounts = events
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
            .count();
        
        eventCounts.toStream().to("event-counts");
        
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```

## 场景设计题

### 23. 设计一个日志收集系统
**答案**: 
```
应用服务器 → Filebeat → Kafka → Logstash → Elasticsearch → Kibana

架构要点：
1. 使用 Filebeat 收集日志文件
2. Kafka 作为消息缓冲和解耦
3. Logstash 进行日志解析和转换
4. Elasticsearch 存储和索引
5. Kibana 可视化和查询
```

### 24. 设计一个实时推荐系统
**答案**: 
```
用户行为 → Kafka → Kafka Streams → 特征工程 → 模型预测 → 推荐结果

关键组件：
1. 用户行为事件收集
2. 实时特征计算
3. 在线模型服务
4. 推荐结果缓存
5. A/B 测试框架
```

### 25. 如何保证 Kafka 在大促期间的稳定性？
**答案**: 
1. **容量规划**: 提前评估流量峰值，预留足够资源
2. **限流熔断**: 实现生产者限流和消费者熔断机制
3. **监控告警**: 完善的监控体系和及时告警
4. **故障预案**: 制定详细的故障处理预案
5. **压力测试**: 提前进行全链路压力测试
6. **弹性扩容**: 支持快速水平扩容能力