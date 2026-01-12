# 事件驱动架构 (Event-Driven Architecture, EDA)

## 一、架构概述

事件驱动架构是一种以事件为核心的软件架构模式,系统组件通过产生、检测和响应事件来进行通信。事件表示系统中发生的重要状态变化,组件之间通过事件实现松耦合的异步通信。

## 二、解决的业务问题

### 1. 传统同步调用的痛点
- **强耦合**: 服务间直接调用,紧密耦合
- **级联故障**: 一个服务故障影响整个调用链
- **性能瓶颈**: 同步等待导致响应时间长
- **扩展困难**: 添加新功能需要修改现有代码
- **实时性差**: 难以实现实时通知和处理

### 2. 事件驱动的解决方案
- **松耦合**: 事件生产者和消费者解耦
- **故障隔离**: 事件消费失败不影响生产者
- **高性能**: 异步处理,提高吞吐量
- **易扩展**: 新增消费者无需修改生产者
- **实时响应**: 事件发生即可触发处理

### 3. 适用场景
- **高并发系统**: 秒杀、抢票等
- **实时通知**: 消息推送、状态同步
- **复杂业务流程**: 订单处理、工作流
- **数据同步**: 跨系统数据同步
- **审计日志**: 操作记录、合规要求

## 三、核心概念

### 1. 基础架构

```
事件生产者 ──► 事件总线 ──► 事件消费者1
                 │
                 ├──► 事件消费者2
                 │
                 └──► 事件消费者3
```

### 2. 事件类型

#### 领域事件 (Domain Event)
```java
// 领域事件:表示领域中发生的重要业务事件
public class OrderCreatedEvent {
    private final String orderId;
    private final String userId;
    private final BigDecimal totalAmount;
    private final Instant occurredOn;

    public OrderCreatedEvent(String orderId, String userId,
                           BigDecimal totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.occurredOn = Instant.now();
    }
    // getters
}

public class OrderPaidEvent {
    private final String orderId;
    private final String paymentId;
    private final BigDecimal amount;
    private final Instant paidAt;
    // constructor, getters
}

public class OrderCancelledEvent {
    private final String orderId;
    private final String reason;
    private final Instant cancelledAt;
    // constructor, getters
}
```

#### 集成事件 (Integration Event)
```java
// 集成事件:用于跨系统通信
public class ProductInventoryChangedEvent {
    private final String productId;
    private final int oldQuantity;
    private final int newQuantity;
    private final String source;  // 来源系统
    private final Instant timestamp;
    // constructor, getters
}
```

### 3. 事件发布

```java
// 事件发布器接口
public interface EventPublisher {
    void publish(DomainEvent event);
    void publish(List<DomainEvent> events);
}

// Spring事件发布器实现
@Component
public class SpringEventPublisher implements EventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}

// 在聚合中发布事件
public class Order {
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();

    public void submit() {
        validateCanSubmit();
        this.status = OrderStatus.SUBMITTED;
        this.submittedAt = Instant.now();

        // 记录事件
        registerEvent(new OrderSubmittedEvent(
            this.id,
            this.userId,
            this.totalAmount
        ));
    }

    protected void registerEvent(DomainEvent event) {
        this.uncommittedEvents.add(event);
    }

    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }
}

// 保存聚合时发布事件
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Transactional
    public void submitOrder(String orderId) {
        Order order = orderRepository.findById(orderId);
        order.submit();

        // 保存聚合
        orderRepository.save(order);

        // 发布事件
        eventPublisher.publish(order.getUncommittedEvents());
        order.clearUncommittedEvents();
    }
}
```

### 4. 事件订阅

```java
// Spring事件监听
@Component
public class OrderEventListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private NotificationService notificationService;

    // 订单创建事件处理
    @EventListener
    @Async  // 异步处理
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("处理订单创建事件: {}", event.getOrderId());

        // 发送确认邮件
        emailService.sendOrderConfirmation(
            event.getUserId(),
            event.getOrderId()
        );
    }

    // 订单支付事件处理
    @EventListener
    @Async
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("处理订单支付事件: {}", event.getOrderId());

        // 扣减库存
        inventoryService.deductStock(event.getOrderId());

        // 推送通知
        notificationService.notifyOrderPaid(event.getOrderId());
    }

    // 订单取消事件处理
    @EventListener
    @Async
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("处理订单取消事件: {}", event.getOrderId());

        // 恢复库存
        inventoryService.restoreStock(event.getOrderId());

        // 退款
        paymentService.refund(event.getOrderId());
    }
}

// 消息队列事件监听
@Component
public class RabbitMQEventListener {

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreatedFromMQ(OrderCreatedEvent event) {
        // 处理从MQ接收的事件
        processOrderCreated(event);
    }

    @RabbitListener(queues = "order.paid.queue")
    public void handleOrderPaidFromMQ(OrderPaidEvent event) {
        processOrderPaid(event);
    }
}
```

### 5. 事件总线模式

```java
// 事件总线接口
public interface EventBus {
    void publish(DomainEvent event);
    void subscribe(Class<? extends DomainEvent> eventType,
                  EventHandler handler);
}

// 事件处理器接口
@FunctionalInterface
public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
}

// 内存事件总线实现
@Component
public class InMemoryEventBus implements EventBus {

    private final Map<Class<?>, List<EventHandler>> subscribers =
        new ConcurrentHashMap<>();

    @Autowired
    private ExecutorService executorService;

    @Override
    public void publish(DomainEvent event) {
        List<EventHandler> handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            handlers.forEach(handler ->
                executorService.submit(() -> handler.handle(event))
            );
        }
    }

    @Override
    public void subscribe(Class<? extends DomainEvent> eventType,
                         EventHandler handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
                  .add(handler);
    }
}

// 使用事件总线
@Component
public class EventSubscriberConfiguration {

    @Autowired
    private EventBus eventBus;

    @Autowired
    private EmailService emailService;

    @PostConstruct
    public void configure() {
        // 订阅订单创建事件
        eventBus.subscribe(OrderCreatedEvent.class, event -> {
            emailService.sendOrderConfirmation(event.getUserId());
        });

        // 订阅订单支付事件
        eventBus.subscribe(OrderPaidEvent.class, event -> {
            inventoryService.deductStock(event.getOrderId());
        });
    }
}
```

### 6. 消息队列集成

```java
// RabbitMQ配置
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("order.exchange", true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created.queue", true);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue("order.paid.queue", true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(orderExchange())
            .with("order.created");
    }

    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder
            .bind(orderPaidQueue())
            .to(orderExchange())
            .with("order.paid");
    }
}

// 事件发布到MQ
@Service
public class RabbitMQEventPublisher implements EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void publish(DomainEvent event) {
        String routingKey = getRoutingKey(event);

        rabbitTemplate.convertAndSend(
            "order.exchange",
            routingKey,
            event,
            message -> {
                // 设置消息属性
                message.getMessageProperties().setContentType("application/json");
                message.getMessageProperties().setDeliveryMode(
                    MessageDeliveryMode.PERSISTENT
                );
                return message;
            }
        );
    }

    private String getRoutingKey(DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            return "order.created";
        } else if (event instanceof OrderPaidEvent) {
            return "order.paid";
        }
        return "order.default";
    }
}

// Kafka配置
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                  StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                  JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

// Kafka事件发布
@Service
public class KafkaEventPublisher implements EventPublisher {

    @Autowired
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Override
    public void publish(DomainEvent event) {
        String topic = getTopicName(event);
        String key = getEventKey(event);

        kafkaTemplate.send(topic, key, event)
            .addCallback(
                result -> log.info("事件发布成功: {}", event),
                ex -> log.error("事件发布失败: {}", event, ex)
            );
    }

    private String getTopicName(DomainEvent event) {
        return "order-events";
    }

    private String getEventKey(DomainEvent event) {
        if (event instanceof OrderCreatedEvent e) {
            return e.getOrderId();
        }
        return UUID.randomUUID().toString();
    }
}
```

### 7. 事件存储

```java
// 事件存储实体
@Entity
@Table(name = "event_store")
public class StoredEvent {
    @Id
    private String id;
    private String aggregateId;
    private String eventType;
    @Column(columnDefinition = "TEXT")
    private String eventData;
    private int version;
    private Instant occurredOn;
    private Instant storedOn;

    // getters, setters
}

// 事件存储仓储
@Repository
public interface EventStoreRepository extends JpaRepository<StoredEvent, String> {
    List<StoredEvent> findByAggregateIdOrderByVersionAsc(String aggregateId);
    List<StoredEvent> findByOccurredOnAfter(Instant since);
}

// 事件存储服务
@Service
public class EventStoreService {

    @Autowired
    private EventStoreRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public void store(String aggregateId, DomainEvent event) {
        StoredEvent storedEvent = new StoredEvent();
        storedEvent.setId(UUID.randomUUID().toString());
        storedEvent.setAggregateId(aggregateId);
        storedEvent.setEventType(event.getClass().getName());

        try {
            storedEvent.setEventData(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(e);
        }

        storedEvent.setVersion(getNextVersion(aggregateId));
        storedEvent.setOccurredOn(event.getOccurredOn());
        storedEvent.setStoredOn(Instant.now());

        repository.save(storedEvent);
    }

    public List<DomainEvent> getEvents(String aggregateId) {
        return repository.findByAggregateIdOrderByVersionAsc(aggregateId)
            .stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    private DomainEvent deserializeEvent(StoredEvent storedEvent) {
        try {
            Class<?> eventClass = Class.forName(storedEvent.getEventType());
            return (DomainEvent) objectMapper.readValue(
                storedEvent.getEventData(),
                eventClass
            );
        } catch (Exception e) {
            throw new EventDeserializationException(e);
        }
    }

    private int getNextVersion(String aggregateId) {
        return repository.findByAggregateIdOrderByVersionAsc(aggregateId)
            .stream()
            .mapToInt(StoredEvent::getVersion)
            .max()
            .orElse(0) + 1;
    }
}
```

## 四、事件处理模式

### 1. 至少一次投递 (At Least Once Delivery)

```java
@Component
public class ReliableEventHandler {

    @RabbitListener(queues = "order.created.queue", ackMode = "MANUAL")
    public void handleOrderCreated(OrderCreatedEvent event,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            // 幂等性检查
            if (isDuplicate(event)) {
                log.warn("重复事件,跳过: {}", event.getOrderId());
                channel.basicAck(tag, false);
                return;
            }

            // 处理事件
            processOrderCreated(event);

            // 记录已处理
            markAsProcessed(event);

            // 手动确认
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("处理事件失败: {}", event, e);

            try {
                // 拒绝消息并重新入队
                channel.basicNack(tag, false, true);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    private boolean isDuplicate(OrderCreatedEvent event) {
        // 检查事件是否已处理
        return processedEventRepository.existsByEventId(event.getEventId());
    }

    private void markAsProcessed(OrderCreatedEvent event) {
        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(event.getEventId());
        processed.setProcessedAt(Instant.now());
        processedEventRepository.save(processed);
    }
}
```

### 2. 事件重试机制

```java
@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 重试策略:最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        // 退避策略:指数退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 1秒
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);  // 最大10秒

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}

@Service
public class EventHandlerWithRetry {

    @Autowired
    private RetryTemplate retryTemplate;

    public void handleEvent(DomainEvent event) {
        retryTemplate.execute(context -> {
            log.info("处理事件,尝试次数: {}", context.getRetryCount());
            return processEvent(event);
        }, context -> {
            log.error("事件处理失败,已重试{}次", context.getRetryCount());
            // 发送到死信队列
            sendToDeadLetterQueue(event);
            return null;
        });
    }
}
```

### 3. 事件顺序保证

```java
// Kafka分区保证顺序
@Service
public class OrderedEventPublisher {

    @Autowired
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public void publish(DomainEvent event, String partitionKey) {
        // 使用订单ID作为分区键,保证同一订单的事件有序
        kafkaTemplate.send("order-events", partitionKey, event);
    }
}

// 顺序消费
@Component
public class OrderedEventConsumer {

    @KafkaListener(
        topics = "order-events",
        concurrency = "1",  // 单线程消费,保证顺序
        groupId = "order-processor"
    )
    public void consume(DomainEvent event) {
        // 按顺序处理事件
        processEvent(event);
    }
}
```

## 五、面试高频问题

### 1. 什么是事件驱动架构?它解决了什么问题?

**答案要点**:
- 事件驱动架构是通过事件进行组件间通信的架构模式
- 解决强耦合、级联故障、性能瓶颈等问题
- 实现松耦合、异步处理、易扩展

### 2. 事件驱动和消息驱动有什么区别?

**事件驱动**:
- 关注"发生了什么"
- 事件是不可变的事实
- 事件携带状态变化信息
- 可以有多个消费者

**消息驱动**:
- 关注"执行什么操作"
- 消息是命令或请求
- 通常有明确的接收者
- 请求-响应模式

### 3. 如何保证事件的可靠投递?

**答案要点**:
- **持久化事件**: 存储到数据库或消息队列
- **事务性发件箱**: 本地事务+轮询发送
- **消息确认机制**: 消费者手动ACK
- **重试机制**: 失败自动重试
- **死信队列**: 无法处理的消息

**代码示例**:
```java
// 事务性发件箱模式
@Transactional
public void createOrder(CreateOrderCommand cmd) {
    // 1. 业务操作
    Order order = orderRepository.save(new Order(cmd));

    // 2. 保存事件到发件箱表
    OutboxEvent outboxEvent = new OutboxEvent();
    outboxEvent.setAggregateId(order.getId());
    outboxEvent.setEventType("OrderCreated");
    outboxEvent.setEventData(serialize(event));
    outboxEventRepository.save(outboxEvent);

    // 3. 异步任务轮询发件箱,发送事件
}

@Scheduled(fixedDelay = 5000)
public void sendOutboxEvents() {
    List<OutboxEvent> events = outboxEventRepository
        .findByProcessedFalse();

    for (OutboxEvent event : events) {
        try {
            eventPublisher.publish(deserialize(event));
            event.setProcessed(true);
            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("发送事件失败", e);
        }
    }
}
```

### 4. 如何处理事件的幂等性?

**答案要点**:
- **事件ID**: 每个事件有唯一ID
- **去重表**: 记录已处理的事件ID
- **业务幂等**: 设计幂等的业务逻辑
- **版本号**: 使用乐观锁

### 5. 事件驱动架构如何保证数据一致性?

**答案要点**:
- **最终一致性**: 通过事件异步同步数据
- **Saga模式**: 处理分布式事务
- **补偿机制**: 失败时回滚
- **事件溯源**: 通过事件重建状态

### 6. 事件和命令有什么区别?

**事件**:
- 描述过去发生的事情
- 不可变
- 可以有多个消费者
- 示例: OrderCreated, PaymentCompleted

**命令**:
- 描述要执行的操作
- 可以被拒绝
- 通常有单一处理者
- 示例: CreateOrder, ProcessPayment

### 7. 如何处理事件消费失败?

**策略**:
1. **重试**: 自动重试几次
2. **死信队列**: 无法处理的进入DLQ
3. **补偿**: 执行补偿操作
4. **告警**: 通知运维人员
5. **人工介入**: 手动处理异常

### 8. 事件驱动架构有哪些缺点?

**缺点**:
- 复杂度高
- 调试困难
- 最终一致性
- 需要额外基础设施
- 事件版本管理

### 9. 如何处理事件版本兼容?

**策略**:
- **向后兼容**: 新增字段可选
- **事件转换**: 转换旧版本事件
- **多版本并存**: 同时支持多个版本

### 10. 什么时候应该使用事件驱动架构?

**适合**:
- 高并发场景
- 需要实时响应
- 复杂业务流程
- 需要审计日志

**不适合**:
- 简单CRUD
- 强一致性要求
- 实时性要求极高

## 六、实战案例:电商订单处理

### 事件流程

```
1. OrderCreated
   ├─> 发送确认邮件
   ├─> 锁定库存
   └─> 创建支付单

2. PaymentCompleted
   ├─> 扣减库存
   ├─> 创建物流单
   └─> 更新订单状态

3. OrderShipped
   ├─> 发送物流通知
   └─> 更新库存状态

4. OrderCancelled
   ├─> 恢复库存
   ├─> 退款
   └─> 发送取消通知
```

## 七、总结

事件驱动架构的关键要点:
1. **松耦合**: 通过事件解耦组件
2. **异步处理**: 提高系统吞吐量
3. **可扩展**: 易于添加新功能
4. **可靠性**: 事件持久化和重试
5. **最终一致性**: 接受数据延迟
