# CQRS 架构 (Command Query Responsibility Segregation)

## 一、架构概述

CQRS(命令查询职责分离)是一种架构模式,将系统的写操作(Command)和读操作(Query)分离,使用不同的模型来处理数据的更新和查询。

## 二、解决的业务问题

### 1. 传统CRUD的痛点
- **读写冲突**: 查询和更新共享同一个数据模型,导致相互影响
- **性能瓶颈**: 复杂查询和高频写入竞争同一资源
- **扩展困难**: 读写操作无法独立扩展
- **模型复杂**: 单一模型同时满足读写需求导致过度复杂
- **缓存失效**: 写操作频繁导致缓存失效

### 2. CQRS的解决方案
- **职责分离**: 命令模型负责写,查询模型负责读
- **独立优化**: 读写模型可以独立优化
- **独立扩展**: 读写服务可以独立扩展
- **简化模型**: 每个模型只关注自己的职责
- **最终一致性**: 通过事件同步读写模型

### 3. 适用场景
- **读多写少**: 电商商品浏览、内容平台
- **复杂查询**: 需要多表关联、聚合统计的场景
- **读写性能要求不同**: 写要求强一致性,读可以接受延迟
- **事件溯源**: 需要审计日志、回溯历史状态

## 三、核心原理

### 1. 基础架构

```
客户端请求
    │
    ├──► 写请求 ──► Command Handler ──► 写模型 (Write DB)
    │                    │
    │                    ▼
    │                事件发布
    │                    │
    │                    ▼
    └──► 读请求 ──► Query Handler ◄── 读模型 (Read DB)
                                       ▲
                                       │
                                   事件订阅
```

### 2. 命令端 (Command Side)

```java
// 命令对象
public class CreateOrderCommand {
    private final String userId;
    private final List<OrderItem> items;
    private final String shippingAddress;

    // 构造函数、getter
}

// 命令处理器
@Service
@Transactional
public class OrderCommandHandler {

    private final OrderWriteRepository writeRepository;
    private final EventBus eventBus;

    public OrderId handle(CreateOrderCommand command) {
        // 1. 验证命令
        validateCommand(command);

        // 2. 创建聚合
        Order order = Order.create(
            command.getUserId(),
            command.getItems(),
            command.getShippingAddress()
        );

        // 3. 保存到写模型
        writeRepository.save(order);

        // 4. 发布领域事件
        eventBus.publish(new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount(),
            Instant.now()
        ));

        return order.getId();
    }

    public void handle(CancelOrderCommand command) {
        Order order = writeRepository.findById(command.getOrderId());
        order.cancel(command.getReason());
        writeRepository.save(order);

        eventBus.publish(new OrderCancelledEvent(
            order.getId(),
            command.getReason(),
            Instant.now()
        ));
    }

    private void validateCommand(CreateOrderCommand command) {
        if (command.getItems().isEmpty()) {
            throw new InvalidCommandException("订单不能为空");
        }
    }
}
```

### 3. 查询端 (Query Side)

```java
// 查询对象
public class GetOrderQuery {
    private final String orderId;
}

public class GetUserOrdersQuery {
    private final String userId;
    private final int page;
    private final int pageSize;
}

// 查询DTO
public class OrderQueryDTO {
    private String orderId;
    private String userId;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
    // getter/setter
}

// 查询处理器
@Service
@Transactional(readOnly = true)
public class OrderQueryHandler {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderQueryDTO handle(GetOrderQuery query) {
        // 1. 尝试从缓存获取
        String cacheKey = "order:" + query.getOrderId();
        OrderQueryDTO cached = (OrderQueryDTO) redisTemplate.opsForValue()
            .get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 从读模型查询(可以是非规范化的表)
        String sql = """
            SELECT o.id, o.user_id, o.total_amount, o.status, o.created_at,
                   oi.product_id, oi.product_name, oi.quantity, oi.price
            FROM order_read_model o
            LEFT JOIN order_item_read_model oi ON o.id = oi.order_id
            WHERE o.id = ?
        """;

        OrderQueryDTO result = jdbcTemplate.query(sql,
            new Object[]{query.getOrderId()},
            new OrderQueryDTOExtractor()
        );

        // 3. 缓存结果
        redisTemplate.opsForValue().set(cacheKey, result, 1, TimeUnit.HOURS);

        return result;
    }

    public PageResult<OrderQueryDTO> handle(GetUserOrdersQuery query) {
        // 查询优化:读模型可以使用专门的索引和数据结构
        String sql = """
            SELECT * FROM order_read_model
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """;

        List<OrderQueryDTO> orders = jdbcTemplate.query(sql,
            new Object[]{
                query.getUserId(),
                query.getPageSize(),
                query.getPage() * query.getPageSize()
            },
            new OrderQueryDTORowMapper()
        );

        int total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM order_read_model WHERE user_id = ?",
            Integer.class,
            query.getUserId()
        );

        return new PageResult<>(orders, total, query.getPage(), query.getPageSize());
    }
}
```

### 4. 事件同步

```java
// 事件监听器,同步读模型
@Component
public class OrderReadModelProjection {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    @Async
    public void on(OrderCreatedEvent event) {
        // 更新读模型
        String sql = """
            INSERT INTO order_read_model
            (id, user_id, total_amount, status, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
            event.getOrderId(),
            event.getUserId(),
            event.getTotalAmount(),
            "CREATED",
            event.getOccurredOn()
        );

        // 清除相关缓存
        redisTemplate.delete("user:orders:" + event.getUserId());
    }

    @EventListener
    @Async
    public void on(OrderCancelledEvent event) {
        // 更新读模型状态
        jdbcTemplate.update(
            "UPDATE order_read_model SET status = ? WHERE id = ?",
            "CANCELLED",
            event.getOrderId()
        );

        // 清除缓存
        redisTemplate.delete("order:" + event.getOrderId());
    }

    @EventListener
    @Async
    public void on(OrderPaidEvent event) {
        jdbcTemplate.update(
            "UPDATE order_read_model SET status = ?, paid_at = ? WHERE id = ?",
            "PAID",
            event.getPaidAt(),
            event.getOrderId()
        );

        redisTemplate.delete("order:" + event.getOrderId());
    }
}
```

### 5. 读写模型数据库设计

```sql
-- 写模型:规范化设计,保证数据一致性
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INT NOT NULL,  -- 乐观锁
    INDEX idx_user_id (user_id)
);

CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 读模型:非规范化设计,优化查询性能
CREATE TABLE order_read_model (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    user_name VARCHAR(100),  -- 冗余用户信息
    total_amount DECIMAL(10, 2),
    discount_amount DECIMAL(10, 2),
    final_amount DECIMAL(10, 2),
    item_count INT,  -- 冗余商品数量
    status VARCHAR(20),
    created_at TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    -- 添加查询需要的索引
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 读模型可以包含聚合数据
CREATE TABLE order_item_read_model (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36),
    product_id VARCHAR(36),
    product_name VARCHAR(200),  -- 冗余商品名称
    product_image VARCHAR(500),  -- 冗余商品图片
    quantity INT,
    unit_price DECIMAL(10, 2),
    subtotal DECIMAL(10, 2),
    INDEX idx_order_id (order_id)
);
```

## 四、进阶模式

### 1. Event Sourcing + CQRS

```java
// 事件存储
@Entity
public class DomainEvent {
    @Id
    private String id;
    private String aggregateId;
    private String eventType;
    private String eventData;  // JSON格式的事件数据
    private int version;
    private Instant occurredOn;
}

// 聚合通过事件重建
public class Order {
    private OrderId id;
    private OrderStatus status;
    private List<OrderItem> items;
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();

    // 从事件流重建聚合状态
    public static Order rebuild(List<DomainEvent> events) {
        Order order = new Order();
        for (DomainEvent event : events) {
            order.apply(event);
        }
        return order;
    }

    public void create(CreateOrderCommand command) {
        // 生成事件
        OrderCreatedEvent event = new OrderCreatedEvent(
            OrderId.generate(),
            command.getUserId(),
            command.getItems()
        );

        // 应用事件
        apply(event);

        // 记录未提交事件
        uncommittedEvents.add(event);
    }

    private void apply(DomainEvent event) {
        if (event instanceof OrderCreatedEvent e) {
            this.id = e.getOrderId();
            this.items = e.getItems();
            this.status = OrderStatus.CREATED;
        } else if (event instanceof OrderCancelledEvent e) {
            this.status = OrderStatus.CANCELLED;
        }
        // 其他事件处理...
    }
}

// 事件存储仓储
@Repository
public class EventStore {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveEvents(String aggregateId, List<DomainEvent> events) {
        for (DomainEvent event : events) {
            jdbcTemplate.update(
                "INSERT INTO domain_events VALUES (?, ?, ?, ?, ?, ?)",
                event.getId(),
                aggregateId,
                event.getEventType(),
                event.getEventData(),
                event.getVersion(),
                event.getOccurredOn()
            );
        }
    }

    public List<DomainEvent> getEvents(String aggregateId) {
        return jdbcTemplate.query(
            "SELECT * FROM domain_events WHERE aggregate_id = ? ORDER BY version",
            (rs, rowNum) -> mapToDomainEvent(rs),
            aggregateId
        );
    }
}
```

### 2. 多读模型

```java
// 不同场景使用不同的读模型

// 1. 列表查询读模型(简化字段,高性能)
public class OrderListQueryDTO {
    private String orderId;
    private String userId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
}

// 2. 详情查询读模型(完整信息)
public class OrderDetailQueryDTO {
    private String orderId;
    private UserInfo user;
    private List<OrderItemDetail> items;
    private ShippingInfo shipping;
    private PaymentInfo payment;
    private List<OrderStatusHistory> statusHistory;
}

// 3. 统计分析读模型
public class OrderStatisticsDTO {
    private String userId;
    private int totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private Map<String, Integer> ordersByStatus;
}

// 查询处理器根据场景选择不同的读模型
@Service
public class OrderQueryService {

    public OrderListQueryDTO getOrderList(String userId) {
        // 查询列表读模型
    }

    public OrderDetailQueryDTO getOrderDetail(String orderId) {
        // 查询详情读模型
    }

    public OrderStatisticsDTO getOrderStatistics(String userId) {
        // 查询统计读模型
    }
}
```

### 3. 读模型投影策略

```java
// 实时投影:事件发生时立即更新读模型
@EventListener
public void on(OrderCreatedEvent event) {
    updateReadModel(event);  // 同步更新
}

// 异步投影:通过消息队列异步更新
@RabbitListener(queues = "order.events")
public void handleOrderEvent(DomainEvent event) {
    updateReadModel(event);  // 异步更新
}

// 定时投影:定期批量更新读模型
@Scheduled(fixedRate = 60000)  // 每分钟
public void rebuildReadModel() {
    List<DomainEvent> events = eventStore.getEventsSince(lastProcessedTime);
    for (DomainEvent event : events) {
        updateReadModel(event);
    }
}

// 快照投影:基于快照加增量事件
public Order getOrder(String orderId) {
    // 1. 加载快照
    OrderSnapshot snapshot = snapshotStore.getSnapshot(orderId);

    // 2. 加载快照之后的事件
    List<DomainEvent> events = eventStore.getEventsSince(
        orderId,
        snapshot.getVersion()
    );

    // 3. 重建状态
    Order order = snapshot.toOrder();
    for (DomainEvent event : events) {
        order.apply(event);
    }

    return order;
}
```

## 五、面试高频问题

### 1. CQRS是什么?解决了什么问题?

**答案要点**:
- CQRS是命令查询职责分离模式
- 将系统的写操作和读操作分离,使用不同的模型
- 解决读写冲突、性能瓶颈、扩展困难等问题
- 适合读写需求差异大的场景

### 2. CQRS和传统CRUD有什么区别?

**CRUD**:
- 单一模型处理读写
- 读写共享数据库
- 强一致性
- 简单但不灵活

**CQRS**:
- 读写模型分离
- 读写可以使用不同数据库
- 最终一致性
- 复杂但灵活

### 3. CQRS如何保证数据一致性?

**答案要点**:
- **最终一致性**: 写模型通过事件异步更新读模型
- **事件顺序**: 确保事件按顺序处理
- **幂等性**: 事件处理器需要幂等
- **补偿机制**: 处理失败时的补偿逻辑

**代码示例**:
```java
@EventListener
@Transactional
public void on(OrderCreatedEvent event) {
    // 幂等性检查
    if (orderReadRepo.existsById(event.getOrderId())) {
        log.warn("订单已存在,跳过: {}", event.getOrderId());
        return;
    }

    try {
        updateReadModel(event);
    } catch (Exception e) {
        // 记录失败事件,后续重试
        saveFailedEvent(event);
        throw e;
    }
}
```

### 4. CQRS的读模型如何设计?

**设计原则**:
- **按查询需求设计**: 一个查询一个模型
- **非规范化**: 可以冗余数据,减少JOIN
- **添加索引**: 针对查询条件添加索引
- **使用缓存**: Redis等缓存热点数据

**示例**:
```sql
-- 用户订单列表查询:简化字段,添加索引
CREATE TABLE user_orders_view (
    order_id VARCHAR(36),
    user_id VARCHAR(36),
    total_amount DECIMAL(10, 2),
    status VARCHAR(20),
    created_at TIMESTAMP,
    INDEX idx_user_created (user_id, created_at)
);

-- 订单详情查询:冗余关联数据
CREATE TABLE order_detail_view (
    order_id VARCHAR(36),
    user_name VARCHAR(100),  -- 冗余
    product_names TEXT,  -- 冗余
    total_amount DECIMAL(10, 2),
    -- 更多冗余字段
    PRIMARY KEY (order_id)
);
```

### 5. CQRS一定要使用Event Sourcing吗?

**答案**:
- 不一定,CQRS和Event Sourcing是两个独立的模式
- 可以单独使用CQRS,不使用Event Sourcing
- 两者结合可以获得更多好处,但也增加复杂度

**对比**:
```
仅CQRS:
写模型 ──► 写数据库
         │
         └──► 事件 ──► 读数据库

CQRS + Event Sourcing:
命令 ──► 事件 ──► 事件存储
                    │
                    ├──► 读模型1
                    ├──► 读模型2
                    └──► 读模型3
```

### 6. CQRS的写模型和读模型可以用不同的数据库吗?

**答案**: 可以,这是CQRS的优势之一

**常见组合**:
- 写模型: MySQL(事务性)
- 读模型: Elasticsearch(全文搜索)

- 写模型: PostgreSQL
- 读模型: Redis(缓存) + MongoDB(文档查询)

- 写模型: 事件存储(Event Store)
- 读模型: 多种数据库(MySQL, ES, Redis)

### 7. CQRS如何处理最终一致性带来的问题?

**常见问题**:
1. **读不到刚写入的数据**
2. **数据延迟**
3. **用户体验问题**

**解决方案**:
```java
// 1. 返回命令执行结果,不依赖查询
public CommandResult createOrder(CreateOrderCommand cmd) {
    OrderId orderId = commandHandler.handle(cmd);
    return CommandResult.success(orderId, "订单创建成功");
}

// 2. 使用版本号检测冲突
public void updateOrder(UpdateOrderCommand cmd) {
    Order order = repository.findById(cmd.getOrderId());
    if (order.getVersion() != cmd.getExpectedVersion()) {
        throw new ConcurrentModificationException();
    }
    order.update(cmd);
}

// 3. UI显示处理中状态
// 写入后立即显示"处理中",轮询或WebSocket更新最终状态
```

### 8. CQRS适合什么场景?不适合什么场景?

**适合**:
- 读多写少的系统
- 复杂查询需求
- 读写性能要求差异大
- 需要审计日志
- 事件驱动架构

**不适合**:
- 简单CRUD应用
- 强一致性要求高
- 团队不熟悉CQRS
- 实时性要求极高

### 9. CQRS如何实现分页查询?

```java
@Service
public class OrderQueryService {

    public PageResult<OrderDTO> getOrders(OrderQueryFilter filter) {
        // 1. 从读模型查询(已优化的非规范化表)
        String sql = """
            SELECT * FROM order_read_model
            WHERE user_id = :userId
            AND status IN (:statuses)
            AND created_at BETWEEN :startDate AND :endDate
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        """;

        List<OrderDTO> orders = namedJdbcTemplate.query(sql,
            Map.of(
                "userId", filter.getUserId(),
                "statuses", filter.getStatuses(),
                "startDate", filter.getStartDate(),
                "endDate", filter.getEndDate(),
                "limit", filter.getPageSize(),
                "offset", filter.getPage() * filter.getPageSize()
            ),
            new OrderDTORowMapper()
        );

        // 2. 计算总数(可以缓存)
        int total = getTotalCount(filter);

        return new PageResult<>(orders, total);
    }

    private int getTotalCount(OrderQueryFilter filter) {
        String cacheKey = "order:count:" + filter.hashCode();
        Integer cached = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        int count = // 查询数据库
        redisTemplate.opsForValue().set(cacheKey, count, 5, TimeUnit.MINUTES);
        return count;
    }
}
```

### 10. CQRS如何处理事务?

**命令端事务**:
```java
@Transactional
public OrderId createOrder(CreateOrderCommand cmd) {
    // 1. 写入聚合
    Order order = Order.create(cmd);
    orderRepository.save(order);

    // 2. 发布事件(同一事务)
    eventPublisher.publish(new OrderCreatedEvent(order));

    return order.getId();
}
```

**跨聚合一致性**:
```java
// 使用Saga模式
public class OrderSaga {

    @Transactional
    public void createOrder(CreateOrderCommand cmd) {
        // 1. 创建订单
        Order order = orderService.create(cmd);

        // 2. 发布事件,异步处理
        eventBus.publish(new OrderCreatedEvent(order.getId()));
    }

    @EventListener
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            // 扣减库存
            inventoryService.reserve(event.getItems());
        } catch (Exception e) {
            // 补偿:取消订单
            orderService.cancel(event.getOrderId());
        }
    }
}
```

## 六、实战案例:电商订单系统

### 架构设计

```
命令端:
CreateOrderCommand ──► OrderCommandHandler ──► Order(聚合) ──► 写库(MySQL)
                                                  │
                                                  ▼
                                            OrderCreatedEvent
                                                  │
                                                  ▼
查询端:                                    EventHandler
GetOrderQuery ──► OrderQueryHandler ──► 读库(MySQL) + 缓存(Redis)
                                            ▲
                                            │
                                      OrderReadModel
```

### 技术栈
- 命令端: Spring Boot + JPA + MySQL
- 查询端: Spring Boot + MyBatis + Redis
- 事件总线: RabbitMQ / Kafka
- 缓存: Redis
- 搜索: Elasticsearch(商品搜索)

## 七、总结

CQRS是一种强大的架构模式,关键要点:

1. **职责分离**: 读写模型分离,各自优化
2. **最终一致性**: 通过事件同步读写模型
3. **性能优化**: 读模型可以非规范化,添加缓存
4. **独立扩展**: 读写服务独立扩展
5. **适度使用**: 不是所有系统都需要CQRS
6. **渐进式演进**: 可以从局部模块开始应用
