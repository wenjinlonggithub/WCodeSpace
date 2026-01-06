# 中间件技术专家技能
*中间件就像现代城市的基础设施，水电煤气样样不能少*

## 消息中间件
*消息中间件就像城市的邮政系统，负责信息传递与沟通*

### Apache Kafka
> Kafka就像高速公路的收费站，车流量大、速度快，但功能相对简单

- **架构设计**
  - Topic分区策略
  - 副本机制与一致性
  - ISR(In-Sync Replicas)管理
  - 日志段(Log Segment)优化

- **性能调优**
  - Producer批量发送
  - Consumer并发消费
  - 压缩算法选择
  - 网络参数优化

- **运维管理**
  - 集群监控指标
  - 分区平衡策略
  - 数据备份与恢复
  - 版本升级方案

### RabbitMQ
> RabbitMQ就像智能邮局，功能齐全、路由灵活，适合复杂的业务场景

- **消息路由**
  - Exchange类型选择
  - Routing Key设计
  - Binding绑定策略
  - 死信队列配置

- **可靠性保证**
  - 消息持久化配置
  - 确认机制(Publisher Confirms)
  - 消费者确认(Consumer ACK)
  - 事务消息处理

- **集群架构**
  - 普通集群配置
  - 镜像队列设置
  - 仲裁队列(Quorum Queue)
  - 联邦集群部署

### RocketMQ
> RocketMQ就像国产高铁，性价比高、功能全面，特别适合电商场景

- **消息模型**
  - 普通消息处理
  - 顺序消息保证
  - 事务消息实现
  - 延时消息调度

- **集群部署**
  - NameServer集群
  - Broker主从配置
  - 消费者负载均衡
  - 消息存储优化

## 数据库中间件
*数据库中间件就像交通管理系统，指挥数据的流向和分配*

### ShardingSphere
> ShardingSphere就像数据库的“智能交通系统”，自动路由、负载均衡
- **分库分表**
  - 分片键设计
  - 分片算法实现
  - 广播表配置
  - 绑定表关联

- **读写分离**
  - 主从数据源配置
  - 负载均衡策略
  - 数据一致性处理
  - 故障转移机制

- **分布式事务**
  - XA两阶段提交
  - BASE柔性事务
  - Saga长事务
  - 本地事务表

### MyCAT
> MyCAT就像老牌的数据库中间件，功能稳定、社区成熟
- **路由规则**
  - 表分片规则
  - 数据节点映射
  - 全局表配置
  - ER表关系

- **SQL解析**
  - 复杂查询处理
  - 跨分片Join
  - 子查询处理
  - 聚合函数优化

## 缓存中间件
*缓存就像记忆系统，越快的记忆效果越好*

### Redis集群
> Redis就像“超级大脑”，反应极快、功能丰富
- **部署模式**
  - 主从复制配置
  - 哨兵高可用
  - Cluster分片集群
  - 混合部署策略

- **数据结构优化**
  - String类型优化
  - Hash结构设计
  - List队列应用
  - Set/ZSet排序

- **性能调优**
  - 内存使用优化
  - 持久化策略
  - 网络参数调整
  - 慢查询分析

### Hazelcast
> Hazelcast就像Java界的“分布式内存”，天然支持Java对象
- **分布式数据结构**
  - IMap分布式Map
  - IQueue分布式队列
  - ITopic发布订阅
  - ILock分布式锁

- **集群管理**
  - 成员发现机制
  - 分区策略
  - 数据备份配置
  - 网络分区处理

## 配置中心
*配置中心就像系统的“控制中心”，统一指挥各个服务*

### Nacos
> Nacos就像阿里的“万能遥控器”，配置管理+服务发现一站式服务
- **配置管理**
  - 配置分组设计
  - 命名空间隔离
  - 配置变更推送
  - 灰度发布配置

- **服务发现**
  - 服务注册机制
  - 健康检查配置
  - 负载均衡策略
  - 服务元数据

### Apollo
> Apollo就像携程的“企业级配置管家”，专业、稳定、功能全
- **配置发布**
  - 环境管理
  - 集群配置
  - 配置继承关系
  - 发布审核流程

- **权限管控**
  - 角色权限设计
  - 操作审计日志
  - 配置变更追踪
  - 回滚机制

## API网关
*API网关就像智能门卫，既要验证身份又要指引路线*

### Spring Cloud Gateway
> Spring Cloud Gateway就像Spring家族的“智能门卫”，与Spring生态完美融合
- **路由配置**
  - 断言(Predicate)规则
  - 过滤器(Filter)链
  - 动态路由更新
  - 负载均衡策略

- **安全控制**
  - 认证过滤器
  - 限流熔断
  - 请求验证
  - 黑白名单

### Kong Gateway
> Kong就像“专业保安公司”，功能强大、插件丰富，但需要专业技能
- **插件系统**
  - 认证插件配置
  - 限流插件使用
  - 日志插件集成
  - 自定义插件开发

- **负载均衡**
  - 上游服务配置
  - 健康检查
  - 故障转移
  - 蓝绿部署

## 搜索引擎
*搜索引擎就像图书馆的“智能检索系统”，帮你从海量数据中快速找到所需*

### Elasticsearch
> Elasticsearch就像“谷歌搜索引擎”，功能强大、扩展性好
- **索引设计**
  - Mapping映射配置
  - 分片与副本设置
  - 索引模板设计
  - 别名管理策略

- **查询优化**
  - DSL查询语法
  - 聚合查询优化
  - 分页查询处理
  - 高亮搜索实现

- **集群运维**
  - 节点角色配置
  - 集群状态监控
  - 数据备份恢复
  - 性能调优策略

### Solr
> Solr就像“传统图书馆检索系统”，功能稳定、配置灵活
- **核心配置**
  - Schema设计
  - 索引字段配置
  - 分词器选择
  - 相关性评分

- **集群部署**
  - SolrCloud配置
  - 分片策略
  - 副本管理
  - Zookeeper集成

## 典型中间件应用实战案例

### 案例一：某电商平台Kafka消息系统架构设计
*实战背景：电商平台从单体架构拆分为微服务，需要可靠的消息传递机制*

**业务挑战**
> 消息中间件就像城市的邮政系统，既要保证信件不丢失，又要及时送达

- **消息量巨大**：日均消息量达到10亿条，峰值每秒100万条
- **业务场景复杂**：订单、支付、库存、物流等多种业务消息类型
- **可靠性要求高**：关键业务消息不允许丢失，如支付成功通知
- **实时性要求**：用户下单后需要秒级更新库存和推送信息

**Kafka集群架构设计**
```yaml
# Kafka集群配置 - server.properties
broker.id=1
listeners=PLAINTEXT://kafka1.internal:9092,SSL://kafka1.internal:9093
log.dirs=/data/kafka-logs-1,/data/kafka-logs-2,/data/kafka-logs-3

# 分片和副本策略
num.network.threads=8
num.io.threads=16
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# 日志配置
num.partitions=12          # 默认分区数，可以提高并发度
default.replication.factor=3    # 3副本确保高可用
min.insync.replicas=2      # 至少2个副本同步才算成功

# 日志保留策略
log.retention.hours=168    # 保留7天
log.segment.bytes=1073741824   # 1GB一个段文件
log.retention.check.interval.ms=300000

# 压缩配置
compression.type=snappy    # 使用snappy压缩，性能和压缩比平衡
log.cleanup.policy=delete
```

**Topic设计策略**
```java
// Topic命名规范和分区策略
public class TopicDesignStrategy {
    
    // 按业务域划分Topic
    public static final String ORDER_EVENTS = "ecommerce.order.events";
    public static final String PAYMENT_EVENTS = "ecommerce.payment.events"; 
    public static final String INVENTORY_EVENTS = "ecommerce.inventory.events";
    public static final String NOTIFICATION_EVENTS = "ecommerce.notification.events";
    
    // Topic配置管理
    @Component
    public class KafkaTopicManager {
        
        @Autowired
        private KafkaAdmin kafkaAdmin;
        
        @PostConstruct
        public void createTopics() {
            Map<String, Object> configs = new HashMap<>();
            configs.put("cleanup.policy", "delete");
            configs.put("retention.ms", "604800000"); // 7天
            configs.put("compression.type", "snappy");
            
            List<NewTopic> topics = Arrays.asList(
                // 订单Topic：按用户ID分区，提高并发处理能力
                TopicBuilder.name(ORDER_EVENTS)
                    .partitions(24)  // 24个分区支持高并发
                    .replicas(3)     // 3副本保证可用性
                    .configs(configs)
                    .build(),
                    
                // 支付Topic：按交易ID分区，确保同一交易的消息有序
                TopicBuilder.name(PAYMENT_EVENTS)
                    .partitions(12)
                    .replicas(3)
                    .config("min.insync.replicas", "2") // 至少2个副本确认
                    .build(),
                    
                // 库存Topic：按商品SKU分区，避免热点商品冲突
                TopicBuilder.name(INVENTORY_EVENTS)
                    .partitions(36)  // 更多分区应对热点商品
                    .replicas(3)
                    .config("retention.ms", "86400000") // 1天，库存变化频繁
                    .build()
            );
            
            CreateTopicsResult result = kafkaAdmin.createTopics(topics);
            result.all().whenComplete((void, throwable) -> {
                if (throwable != null) {
                    log.error("创建Topic失败", throwable);
                } else {
                    log.info("所有Topic创建成功");
                }
            });
        }
    }
}
```

**高可靠性消息发送**
```java
// 可靠性消息生产者设计
@Service
public class ReliableMessageProducer {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private MessageOutboxService messageOutboxService;
    
    // 事务消息：确保业务操作和消息发送的一致性
    @Transactional
    public void sendTransactionalMessage(String topic, String key, Object message) {
        try {
            // 1. 先保存到本地消息表（Outbox模式）
            MessageOutbox outboxMessage = MessageOutbox.builder()
                .messageId(UUID.randomUUID().toString())
                .topic(topic)
                .messageKey(key)
                .payload(JsonUtils.toJson(message))
                .status(MessageStatus.PENDING)
                .createTime(Instant.now())
                .build();
            
            messageOutboxService.save(outboxMessage);
            
            // 2. 执行业务操作（在同一事务中）
            // 业务逻辑在调用方执行...
            
            // 3. 事务提交成功后，异步发送消息
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendMessageAsync(outboxMessage);
                    }
                }
            );
            
        } catch (Exception e) {
            log.error("事务消息发送失败", e);
            throw new MessageSendException("消息发送失败", e);
        }
    }
    
    // 异步发送消息，支持重试
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendMessageAsync(MessageOutbox outboxMessage) {
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                outboxMessage.getTopic(),
                outboxMessage.getMessageKey(),
                outboxMessage.getPayload()
            );
            
            // 设置消息头
            record.headers().add("messageId", outboxMessage.getMessageId().getBytes());
            record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());
            
            // 发送消息并处理回调
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
            
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    // 更新消息状态为已发送
                    messageOutboxService.updateStatus(outboxMessage.getMessageId(), 
                        MessageStatus.SENT, null);
                    
                    log.info("消息发送成功: messageId={}, offset={}", 
                        outboxMessage.getMessageId(), result.getRecordMetadata().offset());
                }
                
                @Override
                public void onFailure(Throwable ex) {
                    // 更新消息状态为失败
                    messageOutboxService.updateStatus(outboxMessage.getMessageId(), 
                        MessageStatus.FAILED, ex.getMessage());
                    
                    log.error("消息发送失败: messageId=" + outboxMessage.getMessageId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("异步发送消息失败: messageId=" + outboxMessage.getMessageId(), e);
            throw e;
        }
    }
    
    // 消息重发机制 - 定时任务扫描失败消息
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void retryFailedMessages() {
        List<MessageOutbox> failedMessages = messageOutboxService.findFailedMessages();
        
        for (MessageOutbox message : failedMessages) {
            if (message.getRetryCount() < 5) { // 最多重试5次
                log.info("重试发送失败消息: messageId={}", message.getMessageId());
                sendMessageAsync(message);
                messageOutboxService.incrementRetryCount(message.getMessageId());
            } else {
                // 重试次数超限，移到死信队列
                log.error("消息重试次数超限，移入死信队列: messageId={}", message.getMessageId());
                messageOutboxService.updateStatus(message.getMessageId(), 
                    MessageStatus.DEAD_LETTER, "重试次数超限");
            }
        }
    }
}
```

**消息消费者优化**
```java
// 高性能消息消费者
@Component
public class OrderEventConsumer {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private MessageDeduplicator messageDeduplicator;
    
    // 批量消费提高吞吐量
    @KafkaListener(
        topics = "ecommerce.order.events",
        groupId = "order-processor-group",
        containerFactory = "batchKafkaListenerContainerFactory",
        concurrency = "12" // 12个消费者线程
    )
    public void handleOrderEvents(
            @Payload List<ConsumerRecord<String, OrderEvent>> records,
            Acknowledgment acknowledgment) {
        
        List<OrderEvent> validEvents = new ArrayList<>();
        
        try {
            for (ConsumerRecord<String, OrderEvent> record : records) {
                String messageId = extractMessageId(record);
                
                // 消息去重：防止重复消费
                if (messageDeduplicator.isDuplicate(messageId)) {
                    log.warn("检测到重复消息，跳过处理: messageId={}", messageId);
                    continue;
                }
                
                // 消息格式验证
                OrderEvent event = record.value();
                if (validateOrderEvent(event)) {
                    validEvents.add(event);
                    messageDeduplicator.recordMessage(messageId);
                } else {
                    log.error("订单事件格式无效: {}", JsonUtils.toJson(event));
                    // 发送到错误处理队列
                    sendToErrorQueue(record, "消息格式无效");
                }
            }
            
            // 批量处理有效事件
            if (!validEvents.isEmpty()) {
                orderService.batchProcessOrderEvents(validEvents);
            }
            
            // 手动提交offset
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("批量处理订单事件失败", e);
            // 不确认消息，触发重试
            throw new RuntimeException("消息处理失败", e);
        }
    }
    
    // 单个消息处理 - 用于重要消息的精确控制
    @KafkaListener(
        topics = "ecommerce.payment.events",
        groupId = "payment-processor-group"
    )
    public void handlePaymentEvent(
            @Payload PaymentEvent event,
            @Header("messageId") String messageId,
            ConsumerRecord<String, PaymentEvent> record) {
        
        try {
            // 幂等性检查
            if (messageDeduplicator.isDuplicate(messageId)) {
                log.info("支付事件已处理过，跳过: messageId={}", messageId);
                return;
            }
            
            // 处理支付事件
            PaymentResult result = orderService.processPaymentEvent(event);
            
            if (result.isSuccess()) {
                messageDeduplicator.recordMessage(messageId);
                log.info("支付事件处理成功: orderId={}, messageId={}", 
                    event.getOrderId(), messageId);
            } else {
                log.error("支付事件处理失败: orderId={}, reason={}", 
                    event.getOrderId(), result.getErrorMessage());
                throw new PaymentProcessException(result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("处理支付事件异常: messageId=" + messageId, e);
            
            // 根据异常类型决定重试策略
            if (e instanceof RetryableException) {
                throw e; // 可重试异常，让Kafka重试
            } else {
                // 不可重试异常，发送到死信队列
                sendToDeadLetterQueue(record, e.getMessage());
            }
        }
    }
}

// 消费者配置
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // 消费者优化配置
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 手动提交
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);     // 每次最多拉取100条
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5分钟
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);    // 30秒
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10秒
        
        // 网络优化
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);        // 最少1KB才返回
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);       // 最多等待500ms
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    // 批量消费监听器容器
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // 批量消费配置
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // 错误处理
        factory.setErrorHandler(new BatchLoggingErrorHandler());
        
        // 重试配置
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback(context -> {
            ConsumerRecord<String, Object> record = (ConsumerRecord<String, Object>) context.getAttribute("record");
            log.error("消息重试失败，发送到死信队列: {}", record);
            // 发送到死信队列的逻辑
            return null;
        });
        
        return factory;
    }
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 初始1秒
        backOffPolicy.setMultiplier(2.0);        // 每次翻倍
        backOffPolicy.setMaxInterval(30000);     // 最大30秒
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}
```

**Kafka监控与运维**
```java
// Kafka集群监控
@Component
public class KafkaClusterMonitor {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private KafkaAdmin kafkaAdmin;
    
    @Scheduled(fixedRate = 30000) // 每30秒监控一次
    public void monitorKafkaCluster() {
        try {
            // 监控Topic信息
            ListTopicsResult topicsResult = kafkaAdmin.listTopics();
            Set<String> topicNames = topicsResult.names().get(10, TimeUnit.SECONDS);
            
            meterRegistry.gauge("kafka.topic.count", topicNames.size());
            
            // 监控消费者组信息
            monitorConsumerGroups();
            
            // 监控分区信息
            monitorPartitionInfo(topicNames);
            
        } catch (Exception e) {
            log.error("Kafka集群监控失败", e);
        }
    }
    
    private void monitorConsumerGroups() {
        try {
            AdminClient adminClient = AdminClient.create(getAdminProperties());
            
            ListConsumerGroupsResult groupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> groups = groupsResult.all().get();
            
            for (ConsumerGroupListing group : groups) {
                String groupId = group.groupId();
                
                // 获取消费者组偏移量信息
                Map<TopicPartition, OffsetAndMetadata> offsets = adminClient
                    .listConsumerGroupOffsets(groupId)
                    .partitionsToOffsetAndMetadata()
                    .get();
                
                // 计算消费延迟
                for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                    TopicPartition partition = entry.getKey();
                    long consumerOffset = entry.getValue().offset();
                    
                    // 获取最新offset
                    long latestOffset = getLatestOffset(partition);
                    long lag = latestOffset - consumerOffset;
                    
                    // 记录消费延迟指标
                    meterRegistry.gauge("kafka.consumer.lag", 
                        Tags.of("group", groupId, "topic", partition.topic(), 
                               "partition", String.valueOf(partition.partition())), 
                        lag);
                    
                    // 消费延迟告警
                    if (lag > 10000) { // 延迟超过1万条消息
                        alertService.sendAlert("KAFKA_CONSUMER_LAG_HIGH", 
                            String.format("消费者组 %s 在 %s 上的延迟达到 %d 条消息", 
                                groupId, partition, lag));
                    }
                }
            }
            
            adminClient.close();
            
        } catch (Exception e) {
            log.error("监控消费者组失败", e);
        }
    }
    
    private void monitorPartitionInfo(Set<String> topicNames) {
        try (AdminClient adminClient = AdminClient.create(getAdminProperties())) {
            
            DescribeTopicsResult topicsResult = adminClient.describeTopics(topicNames);
            Map<String, TopicDescription> topicDescriptions = topicsResult.all().get();
            
            for (Map.Entry<String, TopicDescription> entry : topicDescriptions.entrySet()) {
                String topicName = entry.getKey();
                TopicDescription description = entry.getValue();
                
                int partitionCount = description.partitions().size();
                meterRegistry.gauge("kafka.topic.partition.count", 
                    Tags.of("topic", topicName), partitionCount);
                
                // 检查分区分布
                Set<Integer> brokerIds = new HashSet<>();
                for (TopicPartitionInfo partition : description.partitions()) {
                    brokerIds.add(partition.leader().id());
                }
                
                // 如果分区集中在少数broker上，发出告警
                if (brokerIds.size() < Math.ceil(partitionCount * 0.6)) {
                    alertService.sendAlert("KAFKA_PARTITION_SKEWED", 
                        String.format("Topic %s 的分区分布不均匀，仅分布在 %d 个broker上", 
                            topicName, brokerIds.size()));
                }
            }
            
        } catch (Exception e) {
            log.error("监控分区信息失败", e);
        }
    }
    
    private Map<String, Object> getAdminProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
            "kafka1:9092,kafka2:9092,kafka3:9092");
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        return props;
    }
}
```

**Kafka优化成果**
- 消息吞吐量：从每秒10万条提升到100万条
- 消息延迟：端到端延迟控制在50ms以内
- 可用性：集群可用性达到99.95%
- 存储优化：通过压缩和分区策略，存储效率提升40%

### 案例二：某金融公司Redis缓存架构优化
*实战背景：金融交易系统需要毫秒级响应，缓存成为核心组件*

**缓存挑战分析**
> Redis缓存就像银行的快速取款机，既要快速响应，又要保证数据正确

**性能瓶颈现象**
- 缓存穿透：恶意请求查询不存在的数据，直接打到数据库
- 缓存击穿：热点数据过期时大量请求并发访问数据库
- 缓存雪崩：大量缓存同时失效导致数据库压力激增
- 数据一致性：缓存与数据库数据不一致问题

**Redis集群架构设计**
```yaml
# Redis Cluster配置
# redis.conf 主要配置
port 7000
cluster-enabled yes
cluster-config-file nodes-7000.conf
cluster-node-timeout 15000
cluster-announce-ip 192.168.1.100
cluster-announce-port 7000
cluster-announce-bus-port 17000

# 内存优化配置
maxmemory 4gb
maxmemory-policy allkeys-lru
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64

# 持久化配置
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes

# AOF配置
appendonly yes
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
```

**多级缓存架构**
```java
// 多级缓存管理器
@Component
public class MultiLevelCacheManager {
    
    private final Cache<String, Object> localCache;      // L1: 本地缓存
    private final RedisTemplate<String, Object> redisTemplate; // L2: Redis缓存
    private final MeterRegistry meterRegistry;
    
    public MultiLevelCacheManager(RedisTemplate<String, Object> redisTemplate, 
                                 MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        
        // 本地缓存配置：Caffeine
        this.localCache = Caffeine.newBuilder()
            .maximumSize(10000)                    // 最大1万个条目
            .expireAfterWrite(5, TimeUnit.MINUTES) // 5分钟过期
            .expireAfterAccess(2, TimeUnit.MINUTES) // 2分钟未访问过期
            .recordStats()                         // 开启统计
            .removalListener((key, value, cause) -> {
                log.debug("本地缓存移除: key={}, cause={}", key, cause);
            })
            .build();
    }
    
    // 获取缓存数据
    public <T> T get(String key, Class<T> type, Supplier<T> dataLoader) {
        // L1: 尝试从本地缓存获取
        T value = getFromLocalCache(key, type);
        if (value != null) {
            meterRegistry.counter("cache.hit", "level", "local").increment();
            return value;
        }
        
        // L2: 尝试从Redis获取
        value = getFromRedisCache(key, type);
        if (value != null) {
            meterRegistry.counter("cache.hit", "level", "redis").increment();
            // 回写到本地缓存
            localCache.put(key, value);
            return value;
        }
        
        // L3: 从数据源加载（数据库等）
        meterRegistry.counter("cache.miss", "key", key).increment();
        return loadAndCache(key, type, dataLoader);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getFromLocalCache(String key, Class<T> type) {
        try {
            Object cached = localCache.getIfPresent(key);
            return cached != null ? type.cast(cached) : null;
        } catch (Exception e) {
            log.warn("本地缓存读取失败: key={}", key, e);
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getFromRedisCache(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            return cached != null ? type.cast(cached) : null;
        } catch (Exception e) {
            log.warn("Redis缓存读取失败: key={}", key, e);
            return null;
        }
    }
    
    private <T> T loadAndCache(String key, Class<T> type, Supplier<T> dataLoader) {
        // 使用分布式锁防止缓存击穿
        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取锁，最多等待100ms
            if (lock.tryLock(100, 30, TimeUnit.MILLISECONDS)) {
                try {
                    // 双重检查：锁内再次检查缓存
                    T value = getFromRedisCache(key, type);
                    if (value != null) {
                        localCache.put(key, value);
                        return value;
                    }
                    
                    // 从数据源加载
                    Timer.Sample sample = Timer.start(meterRegistry);
                    value = dataLoader.get();
                    sample.stop("cache.load.duration", "key", key);
                    
                    if (value != null) {
                        // 设置随机过期时间，防止缓存雪崩
                        Duration expiration = Duration.ofMinutes(30 + ThreadLocalRandom.current().nextInt(10));
                        redisTemplate.opsForValue().set(key, value, expiration);
                        localCache.put(key, value);
                    } else {
                        // 缓存空值，防止缓存穿透
                        redisTemplate.opsForValue().set(key, NULL_VALUE, Duration.ofMinutes(5));
                    }
                    
                    return value;
                    
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("获取分布式锁超时: key={}", key);
                // 锁获取失败，直接从数据源加载（降级方案）
                return dataLoader.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: key={}", key, e);
            return dataLoader.get();
        } catch (Exception e) {
            log.error("分布式锁操作失败: key={}", key, e);
            return dataLoader.get();
        }
    }
    
    // 删除缓存
    public void evict(String key) {
        localCache.invalidate(key);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("删除Redis缓存失败: key={}", key, e);
        }
    }
    
    // 批量删除缓存
    public void evictByPattern(String pattern) {
        // 删除本地缓存中匹配的key
        localCache.asMap().keySet().removeIf(key -> key.matches(pattern));
        
        // 删除Redis中匹配的key
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("批量删除Redis缓存失败: pattern={}", pattern, e);
        }
    }
}
```

**缓存一致性保障**
```java
// 缓存与数据库一致性管理
@Service
public class CacheConsistencyManager {
    
    @Autowired
    private MultiLevelCacheManager cacheManager;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    // 延迟双删策略
    @Transactional
    public void updateDataWithCache(String key, Object newData, 
                                   Supplier<Void> databaseOperation) {
        try {
            // 第一次删除缓存
            cacheManager.evict(key);
            
            // 执行数据库操作
            databaseOperation.get();
            
            // 发布异步事件，延迟删除缓存
            eventPublisher.publishEvent(new CacheDelayedEvictEvent(key));
            
        } catch (Exception e) {
            log.error("数据更新失败，回滚操作: key={}", key, e);
            throw e;
        }
    }
    
    // 监听延迟删除事件
    @EventListener
    @Async
    public void handleDelayedEvict(CacheDelayedEvictEvent event) {
        try {
            // 延迟500ms再次删除缓存，确保主从复制延迟的数据不会被缓存
            Thread.sleep(500);
            cacheManager.evict(event.getKey());
            log.debug("延迟删除缓存: key={}", event.getKey());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("延迟删除缓存被中断: key={}", event.getKey());
        } catch (Exception e) {
            log.error("延迟删除缓存失败: key={}", event.getKey(), e);
        }
    }
    
    // Canal监听MySQL binlog变化，实现缓存自动失效
    @Component
    public class CacheInvalidationListener {
        
        @Autowired
        private CanalConnector canalConnector;
        
        @PostConstruct
        public void startListening() {
            new Thread(() -> {
                while (true) {
                    try {
                        canalConnector.connect();
                        canalConnector.subscribe(".*\\..*"); // 订阅所有表的变化
                        
                        while (true) {
                            Message message = canalConnector.getWithoutAck(1000);
                            
                            if (message.getId() != -1 && !message.getEntries().isEmpty()) {
                                processMessage(message);
                                canalConnector.ack(message.getId());
                            } else {
                                Thread.sleep(1000);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Canal监听异常", e);
                        try {
                            Thread.sleep(5000); // 等待5秒后重试
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }, "canal-cache-invalidation").start();
        }
        
        private void processMessage(Message message) {
            for (Entry entry : message.getEntries()) {
                if (entry.getEntryType() == EntryType.ROWDATA) {
                    RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
                    String tableName = entry.getHeader().getTableName();
                    
                    for (RowData rowData : rowChange.getRowDatasList()) {
                        switch (rowChange.getEventType()) {
                            case UPDATE:
                            case DELETE:
                                invalidateCacheForTable(tableName, rowData);
                                break;
                            case INSERT:
                                // 新增数据通常不需要删除缓存
                                break;
                        }
                    }
                }
            }
        }
        
        private void invalidateCacheForTable(String tableName, RowData rowData) {
            // 根据表名和行数据生成缓存key模式
            String cachePattern = generateCachePattern(tableName, rowData);
            cacheManager.evictByPattern(cachePattern);
            
            log.info("根据数据库变化失效缓存: table={}, pattern={}", tableName, cachePattern);
        }
        
        private String generateCachePattern(String tableName, RowData rowData) {
            // 根据业务规则生成缓存key模式
            // 例如：用户表变化时，失效用户相关的所有缓存
            switch (tableName) {
                case "users":
                    String userId = extractUserId(rowData);
                    return "user:*:" + userId + ":*";
                case "accounts":
                    String accountId = extractAccountId(rowData);
                    return "account:" + accountId + ":*";
                default:
                    return tableName + ":*";
            }
        }
    }
}
```

**Redis性能监控**
```java
// Redis性能监控和告警
@Component
public class RedisPerformanceMonitor {
    
    @Autowired
    private LettuceConnectionFactory connectionFactory;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 30000) // 每30秒监控一次
    public void monitorRedisPerformance() {
        try {
            StatefulRedisConnection<String, String> connection = 
                connectionFactory.getConnection().getNativeConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            // 获取Redis信息
            String info = commands.info("all");
            Map<String, String> infoMap = parseRedisInfo(info);
            
            // 监控内存使用
            long usedMemory = Long.parseLong(infoMap.get("used_memory"));
            long maxMemory = Long.parseLong(infoMap.getOrDefault("maxmemory", "0"));
            if (maxMemory > 0) {
                double memoryUsageRatio = (double) usedMemory / maxMemory;
                meterRegistry.gauge("redis.memory.usage.ratio", memoryUsageRatio);
                
                // 内存使用率告警
                if (memoryUsageRatio > 0.85) {
                    alertService.sendAlert("REDIS_MEMORY_HIGH", 
                        String.format("Redis内存使用率: %.2f%%", memoryUsageRatio * 100));
                }
            }
            
            // 监控连接数
            int connectedClients = Integer.parseInt(infoMap.get("connected_clients"));
            meterRegistry.gauge("redis.connected.clients", connectedClients);
            
            // 监控命令执行统计
            long totalCommands = Long.parseLong(infoMap.get("total_commands_processed"));
            meterRegistry.gauge("redis.commands.total", totalCommands);
            
            // 监控键空间命中率
            long keyspaceHits = Long.parseLong(infoMap.getOrDefault("keyspace_hits", "0"));
            long keyspaceMisses = Long.parseLong(infoMap.getOrDefault("keyspace_misses", "0"));
            if (keyspaceHits + keyspaceMisses > 0) {
                double hitRatio = (double) keyspaceHits / (keyspaceHits + keyspaceMisses);
                meterRegistry.gauge("redis.keyspace.hit.ratio", hitRatio);
                
                // 命中率过低告警
                if (hitRatio < 0.8) {
                    alertService.sendAlert("REDIS_HIT_RATIO_LOW", 
                        String.format("Redis缓存命中率: %.2f%%", hitRatio * 100));
                }
            }
            
            // 监控慢查询
            List<Object> slowlog = commands.slowlogGet(10);
            if (!slowlog.isEmpty()) {
                meterRegistry.gauge("redis.slowlog.count", slowlog.size());
                
                log.warn("Redis存在{}个慢查询", slowlog.size());
                for (Object entry : slowlog) {
                    log.warn("慢查询详情: {}", entry);
                }
            }
            
        } catch (Exception e) {
            log.error("Redis性能监控失败", e);
        }
    }
    
    // 监控集群状态
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void monitorClusterHealth() {
        try {
            StatefulRedisClusterConnection<String, String> clusterConnection = 
                connectionFactory.getClusterConnection().getNativeConnection();
            RedisAdvancedClusterCommands<String, String> commands = clusterConnection.sync();
            
            // 获取集群节点信息
            String clusterNodes = commands.clusterNodes();
            String[] nodes = clusterNodes.split("\n");
            
            int masterCount = 0;
            int slaveCount = 0;
            int failedCount = 0;
            
            for (String node : nodes) {
                if (node.contains("master")) {
                    masterCount++;
                    if (node.contains("fail")) {
                        failedCount++;
                    }
                } else if (node.contains("slave")) {
                    slaveCount++;
                    if (node.contains("fail")) {
                        failedCount++;
                    }
                }
            }
            
            meterRegistry.gauge("redis.cluster.master.count", masterCount);
            meterRegistry.gauge("redis.cluster.slave.count", slaveCount);
            meterRegistry.gauge("redis.cluster.failed.count", failedCount);
            
            // 节点故障告警
            if (failedCount > 0) {
                alertService.sendAlert("REDIS_CLUSTER_NODE_FAILED", 
                    String.format("Redis集群有%d个节点故障", failedCount));
            }
            
        } catch (Exception e) {
            log.error("Redis集群健康检查失败", e);
        }
    }
    
    private Map<String, String> parseRedisInfo(String info) {
        Map<String, String> infoMap = new HashMap<>();
        String[] lines = info.split("\n");
        
        for (String line : lines) {
            if (line.contains(":") && !line.startsWith("#")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    infoMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        
        return infoMap;
    }
}
```

**Redis优化成果**
- 缓存命中率：从70%提升到95%以上
- 响应时间：平均响应时间从50ms降低到5ms
- 并发能力：支持并发量从1万QPS提升到10万QPS  
- 内存利用率：通过数据结构优化，内存使用效率提升30%
- 可用性：集群可用性达到99.99%

这两个详细的案例展示了中间件在实际生产环境中的应用，涵盖了架构设计、性能优化、监控告警等各个方面，为中间件的选型和使用提供了实战参考。