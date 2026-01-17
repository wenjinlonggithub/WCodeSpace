# DDD代码实战面试题

## 基于现有代码库的实战面试场景

### 场景一：订单聚合设计分析

**面试题：请分析以下Order聚合的设计，指出其优缺点并提出改进建议**

```java
// 现有Order聚合代码
public class Order {
    private Long id;
    private Customer customer;
    private List<OrderItem> orderItems;
    private OrderStatus status;
    private Address shippingAddress;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private Money totalAmount;

    public void addOrderItem(Product product, int quantity) {
        if (!product.isAvailable()) {
            throw new IllegalStateException("商品不可用: " + product.getName());
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException("库存不足: " + product.getName());
        }

        OrderItem orderItem = new OrderItem(product, quantity);
        this.orderItems.add(orderItem);
        
        product.decreaseStock(quantity);  // 直接修改Product
        
        calculateTotalAmount();
    }
}
```

**参考答案：**

**优点：**
1. 聚合根正确封装了业务逻辑
2. 维护了订单总金额的一致性
3. 进行了必要的业务规则验证

**缺点：**
1. **跨聚合直接修改**：直接修改Product对象违反了聚合边界
2. **事务边界不清晰**：一个事务修改了两个聚合
3. **缺少领域事件**：没有发布相关的领域事件

**改进建议：**
```java
public class Order {
    // ... 其他字段
    
    public void addOrderItem(Product product, int quantity) {
        // 1. 验证业务规则
        if (!product.isAvailable()) {
            throw new IllegalStateException("商品不可用: " + product.getName());
        }
        
        // 2. 创建订单项（不直接修改Product）
        OrderItem orderItem = new OrderItem(product.getId(), 
            product.getName(), product.getPrice(), quantity);
        this.orderItems.add(orderItem);
        
        // 3. 重新计算总金额
        calculateTotalAmount();
        
        // 4. 发布领域事件，由事件处理器处理库存扣减
        addDomainEvent(new OrderItemAddedEvent(this.id, product.getId(), quantity));
    }
}
```

### 场景二：Money值对象扩展

**面试题：现有Money值对象需要支持汇率转换功能，请设计实现方案**

```java
// 现有Money值对象
public class Money {
    private final BigDecimal amount;
    private final String currency;
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("货币类型不匹配");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

**参考答案：**

**方案一：引入汇率服务（推荐）**
```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency;  // 使用Currency值对象
    
    public Money convertTo(Currency targetCurrency, ExchangeRateService rateService) {
        if (this.currency.equals(targetCurrency)) {
            return this;
        }
        
        BigDecimal rate = rateService.getRate(this.currency, targetCurrency);
        BigDecimal convertedAmount = this.amount.multiply(rate);
        
        return new Money(convertedAmount, targetCurrency);
    }
    
    public Money add(Money other, ExchangeRateService rateService) {
        Money convertedOther = other.convertTo(this.currency, rateService);
        return new Money(this.amount.add(convertedOther.amount), this.currency);
    }
}

// Currency值对象
public class Currency {
    private final String code;
    private final String name;
    private final int decimalPlaces;
    
    public Currency(String code) {
        this.code = validateCurrencyCode(code);
        this.name = getCurrencyName(code);
        this.decimalPlaces = getDecimalPlaces(code);
    }
}

// 汇率服务接口
public interface ExchangeRateService {
    BigDecimal getRate(Currency from, Currency to);
}
```

**方案二：工厂模式创建**
```java
public class MoneyFactory {
    private final ExchangeRateService exchangeRateService;
    
    public Money createMoney(BigDecimal amount, String currencyCode) {
        Currency currency = new Currency(currencyCode);
        return new Money(amount, currency);
    }
    
    public Money convertMoney(Money source, String targetCurrencyCode) {
        Currency targetCurrency = new Currency(targetCurrencyCode);
        return source.convertTo(targetCurrency, exchangeRateService);
    }
}
```

### 场景三：应用服务重构

**面试题：以下应用服务存在什么问题？如何重构？**

```java
@Service
public class OrderApplicationService {
    
    @Transactional
    public OrderDTO addOrderItem(AddOrderItemRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

        order.addOrderItem(product, request.getQuantity());
        
        orderRepository.save(order);
        productRepository.save(product);  // 保存两个聚合

        return convertToDTO(order);
    }
}
```

**参考答案：**

**问题分析：**
1. **违反聚合边界**：一个事务修改了两个聚合
2. **缺少业务验证**：没有验证订单状态是否允许添加商品
3. **异常处理不完善**：没有处理并发冲突
4. **缺少领域事件**：没有发布相关事件

**重构方案：**
```java
@Service
public class OrderApplicationService {
    
    @Transactional
    public OrderDTO addOrderItem(AddOrderItemRequest request) {
        // 1. 参数验证
        validateRequest(request);
        
        // 2. 加载订单聚合
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));
        
        // 3. 验证订单状态
        if (!order.canAddItems()) {
            throw new OrderStateException("当前订单状态不允许添加商品");
        }
        
        // 4. 加载商品信息（只读）
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
        
        // 5. 检查库存（通过领域服务）
        if (!inventoryService.checkAvailability(product.getId(), request.getQuantity())) {
            throw new InsufficientStockException(product.getName());
        }
        
        // 6. 添加订单项（只修改Order聚合）
        order.addOrderItem(product.getId(), product.getName(), 
                          product.getPrice(), request.getQuantity());
        
        // 7. 保存订单
        orderRepository.save(order);
        
        // 8. 发布领域事件（异步处理库存扣减）
        domainEventPublisher.publish(new OrderItemAddedEvent(
            order.getId(), product.getId(), request.getQuantity()));
        
        return convertToDTO(order);
    }
    
    // 事件处理器（在另一个事务中处理）
    @EventHandler
    @Transactional
    public void handle(OrderItemAddedEvent event) {
        Product product = productRepository.findById(event.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(event.getProductId()));
        
        product.decreaseStock(event.getQuantity());
        productRepository.save(product);
    }
}
```

### 场景四：复杂查询设计

**面试题：设计一个订单查询服务，支持多条件查询和分页**

**参考答案：**

**方案一：规格模式 + CQRS**
```java
// 查询规格接口
public interface OrderSpecification {
    Predicate toPredicate(Root<OrderQueryModel> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}

// 具体规格实现
public class OrderByCustomerSpec implements OrderSpecification {
    private final Long customerId;
    
    public OrderByCustomerSpec(Long customerId) {
        this.customerId = customerId;
    }
    
    @Override
    public Predicate toPredicate(Root<OrderQueryModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("customerId"), customerId);
    }
}

public class OrderByStatusSpec implements OrderSpecification {
    private final OrderStatus status;
    
    @Override
    public Predicate toPredicate(Root<OrderQueryModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("status"), status);
    }
}

public class OrderByDateRangeSpec implements OrderSpecification {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    
    @Override
    public Predicate toPredicate(Root<OrderQueryModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.between(root.get("orderTime"), startDate, endDate);
    }
}

// 组合规格
public class CompositeOrderSpecification implements OrderSpecification {
    private final List<OrderSpecification> specifications;
    private final LogicalOperator operator;
    
    @Override
    public Predicate toPredicate(Root<OrderQueryModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = specifications.stream()
            .map(spec -> spec.toPredicate(root, query, cb))
            .collect(Collectors.toList());
        
        return operator == LogicalOperator.AND 
            ? cb.and(predicates.toArray(new Predicate[0]))
            : cb.or(predicates.toArray(new Predicate[0]));
    }
}

// 查询服务
@Service
public class OrderQueryService {
    
    public PageResult<OrderSummaryDTO> searchOrders(OrderSearchCriteria criteria) {
        // 1. 构建查询规格
        OrderSpecification spec = buildSpecification(criteria);
        
        // 2. 执行查询
        Page<OrderQueryModel> page = orderQueryRepository.findAll(spec, criteria.getPageable());
        
        // 3. 转换为DTO
        List<OrderSummaryDTO> dtos = page.getContent().stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(dtos, page.getTotalElements(), page.getNumber(), page.getSize());
    }
    
    private OrderSpecification buildSpecification(OrderSearchCriteria criteria) {
        List<OrderSpecification> specs = new ArrayList<>();
        
        if (criteria.getCustomerId() != null) {
            specs.add(new OrderByCustomerSpec(criteria.getCustomerId()));
        }
        
        if (criteria.getStatus() != null) {
            specs.add(new OrderByStatusSpec(criteria.getStatus()));
        }
        
        if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
            specs.add(new OrderByDateRangeSpec(criteria.getStartDate(), criteria.getEndDate()));
        }
        
        return new CompositeOrderSpecification(specs, LogicalOperator.AND);
    }
}
```

**方案二：查询对象模式**
```java
public class OrderQuery {
    private Long customerId;
    private OrderStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String customerName;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Pageable pageable;
    
    // Builder模式构建查询
    public static class Builder {
        private OrderQuery query = new OrderQuery();
        
        public Builder customerId(Long customerId) {
            query.customerId = customerId;
            return this;
        }
        
        public Builder status(OrderStatus status) {
            query.status = status;
            return this;
        }
        
        public Builder dateRange(LocalDateTime start, LocalDateTime end) {
            query.startDate = start;
            query.endDate = end;
            return this;
        }
        
        public Builder amountRange(BigDecimal min, BigDecimal max) {
            query.minAmount = min;
            query.maxAmount = max;
            return this;
        }
        
        public Builder pageable(Pageable pageable) {
            query.pageable = pageable;
            return this;
        }
        
        public OrderQuery build() {
            return query;
        }
    }
}

// 使用示例
OrderQuery query = new OrderQuery.Builder()
    .customerId(123L)
    .status(OrderStatus.CONFIRMED)
    .dateRange(startDate, endDate)
    .amountRange(new BigDecimal("100"), new BigDecimal("1000"))
    .pageable(PageRequest.of(0, 20))
    .build();

PageResult<OrderSummaryDTO> result = orderQueryService.searchOrders(query);
```

### 场景五：领域事件设计

**面试题：为订单确认流程设计领域事件机制**

**参考答案：**

```java
// 领域事件基类
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String aggregateId;
    
    protected DomainEvent(String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.aggregateId = aggregateId;
    }
}

// 订单确认事件
public class OrderConfirmedEvent extends DomainEvent {
    private final Long orderId;
    private final Long customerId;
    private final Money totalAmount;
    private final List<OrderItemData> items;
    
    public OrderConfirmedEvent(Long orderId, Long customerId, Money totalAmount, List<OrderItemData> items) {
        super(orderId.toString());
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.items = items;
    }
}

// 聚合根基类
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

// 修改后的Order聚合
public class Order extends AggregateRoot {
    // ... 其他字段
    
    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("订单不能为空");
        }
        
        this.status = OrderStatus.CONFIRMED;
        
        // 发布领域事件
        List<OrderItemData> itemData = orderItems.stream()
            .map(item -> new OrderItemData(item.getProduct().getId(), item.getQuantity()))
            .collect(Collectors.toList());
            
        addDomainEvent(new OrderConfirmedEvent(this.id, this.customer.getId(), this.totalAmount, itemData));
    }
}

// 事件发布器
@Component
public class DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    
    public void publishEvents(AggregateRoot aggregate) {
        List<DomainEvent> events = aggregate.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
}

// 应用服务中发布事件
@Service
public class OrderApplicationService {
    
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        
        order.confirm();
        orderRepository.save(order);
        
        // 发布领域事件
        domainEventPublisher.publishEvents(order);
    }
}

// 事件处理器
@Component
public class OrderEventHandler {
    
    @EventListener
    @Async
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // 1. 发送确认邮件
        emailService.sendOrderConfirmationEmail(event.getCustomerId(), event.getOrderId());
        
        // 2. 更新客户积分
        customerService.addPoints(event.getCustomerId(), calculatePoints(event.getTotalAmount()));
        
        // 3. 记录审计日志
        auditService.logOrderConfirmation(event.getOrderId(), event.getOccurredOn());
        
        // 4. 通知库存系统
        inventoryService.confirmStockReservation(event.getItems());
    }
    
    @EventListener
    @Transactional
    public void handleOrderConfirmedForReporting(OrderConfirmedEvent event) {
        // 更新报表数据
        reportingService.updateOrderStatistics(event);
    }
}
```

### 场景六：性能优化

**面试题：订单列表查询性能较差，如何优化？**

**参考答案：**

**问题分析：**
1. N+1查询问题
2. 大表查询性能问题
3. 复杂关联查询

**优化方案：**

**方案一：CQRS + 读模型优化**
```java
// 读模型
@Entity
@Table(name = "order_read_model")
public class OrderReadModel {
    @Id
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime orderTime;
    private String shippingAddress;
    private int itemCount;
    
    // 冗余常用查询字段，避免关联查询
}

// 读模型更新器
@Component
public class OrderReadModelUpdater {
    
    @EventListener
    @Transactional
    public void updateReadModel(OrderConfirmedEvent event) {
        OrderReadModel readModel = orderReadModelRepository.findById(event.getOrderId())
            .orElse(new OrderReadModel());
        
        // 更新读模型
        readModel.setOrderId(event.getOrderId());
        readModel.setStatus("CONFIRMED");
        readModel.setTotalAmount(event.getTotalAmount().getAmount());
        // ... 其他字段更新
        
        orderReadModelRepository.save(readModel);
    }
}

// 优化后的查询服务
@Service
public class OrderQueryService {
    
    public PageResult<OrderListDTO> getOrderList(OrderListQuery query) {
        // 直接查询读模型，避免复杂关联
        Page<OrderReadModel> page = orderReadModelRepository.findByQuery(query);
        
        List<OrderListDTO> dtos = page.getContent().stream()
            .map(this::convertToListDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(dtos, page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
```

**方案二：缓存策略**
```java
@Service
public class CachedOrderQueryService {
    
    @Cacheable(value = "orderList", key = "#query.hashCode()")
    public PageResult<OrderListDTO> getOrderList(OrderListQuery query) {
        return orderQueryService.getOrderList(query);
    }
    
    @CacheEvict(value = "orderList", allEntries = true)
    public void evictOrderListCache() {
        // 当订单状态变更时清除缓存
    }
    
    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        evictOrderListCache();
    }
}
```

**方案三：数据库优化**
```sql
-- 创建复合索引
CREATE INDEX idx_order_customer_status_time ON orders(customer_id, status, order_time);
CREATE INDEX idx_order_status_time ON orders(status, order_time);

-- 分区表（按时间分区）
CREATE TABLE orders_2024 PARTITION OF orders 
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

### 场景七：事件溯源与快照优化

**面试题：基于现有Order聚合，设计事件溯源机制并实现快照优化**

**参考答案：**

```java
// 领域事件基类
public abstract class DomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final Long version;
    private final LocalDateTime occurredOn;
    
    protected DomainEvent(String aggregateId, Long version) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.version = version;
        this.occurredOn = LocalDateTime.now();
    }
}

// 订单相关事件
public class OrderCreatedEvent extends DomainEvent {
    private final Long customerId;
    private final Address shippingAddress;
    
    public OrderCreatedEvent(String orderId, Long version, Long customerId, Address shippingAddress) {
        super(orderId, version);
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
    }
}

public class OrderItemAddedEvent extends DomainEvent {
    private final Long productId;
    private final String productName;
    private final Money price;
    private final int quantity;
    
    public OrderItemAddedEvent(String orderId, Long version, Long productId, 
                              String productName, Money price, int quantity) {
        super(orderId, version);
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
}

public class OrderConfirmedEvent extends DomainEvent {
    private final Money totalAmount;
    private final LocalDateTime confirmedAt;
    
    public OrderConfirmedEvent(String orderId, Long version, Money totalAmount) {
        super(orderId, version);
        this.totalAmount = totalAmount;
        this.confirmedAt = LocalDateTime.now();
    }
}

// 支持事件溯源的Order聚合
public class Order extends AggregateRoot {
    private Long id;
    private Customer customer;
    private List<OrderItem> orderItems;
    private OrderStatus status;
    private Address shippingAddress;
    private Money totalAmount;
    private Long version;
    
    // 默认构造函数（用于事件重放）
    public Order() {
        this.orderItems = new ArrayList<>();
        this.version = 0L;
    }
    
    // 业务构造函数
    public Order(Long id, Customer customer, Address shippingAddress) {
        this();
        applyEvent(new OrderCreatedEvent(id.toString(), nextVersion(), 
                                       customer.getId(), shippingAddress));
    }
    
    // 事件应用方法
    public void apply(DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            apply((OrderCreatedEvent) event);
        } else if (event instanceof OrderItemAddedEvent) {
            apply((OrderItemAddedEvent) event);
        } else if (event instanceof OrderConfirmedEvent) {
            apply((OrderConfirmedEvent) event);
        }
        this.version = event.getVersion();
    }
    
    private void apply(OrderCreatedEvent event) {
        this.id = Long.valueOf(event.getAggregateId());
        this.customer = new Customer(event.getCustomerId());
        this.shippingAddress = event.getShippingAddress();
        this.status = OrderStatus.PENDING;
        this.totalAmount = Money.ZERO;
    }
    
    private void apply(OrderItemAddedEvent event) {
        OrderItem item = new OrderItem(
            new Product(event.getProductId(), event.getProductName(), event.getPrice()),
            event.getQuantity()
        );
        this.orderItems.add(item);
        calculateTotalAmount();
    }
    
    private void apply(OrderConfirmedEvent event) {
        this.status = OrderStatus.CONFIRMED;
    }
    
    // 业务方法
    public void addOrderItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("只能向待处理订单添加商品");
        }
        
        applyEvent(new OrderItemAddedEvent(id.toString(), nextVersion(),
                                         product.getId(), product.getName(), 
                                         product.getPrice(), quantity));
    }
    
    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("订单不能为空");
        }
        
        applyEvent(new OrderConfirmedEvent(id.toString(), nextVersion(), totalAmount));
    }
    
    private void applyEvent(DomainEvent event) {
        apply(event);
        addDomainEvent(event);
    }
    
    private Long nextVersion() {
        return version + 1;
    }
}

// 快照
@Entity
@Table(name = "order_snapshots")
public class OrderSnapshot {
    @Id
    private String aggregateId;
    private Long version;
    private String snapshotData;
    private LocalDateTime createdAt;
    
    // 构造函数和getter/setter
}

// 事件溯源仓储
@Repository
public class EventSourcingOrderRepository implements OrderRepository {
    
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final ObjectMapper objectMapper;
    
    private static final int SNAPSHOT_FREQUENCY = 10; // 每10个事件创建一次快照
    
    @Override
    public Optional<Order> findById(Long orderId) {
        String aggregateId = orderId.toString();
        
        // 1. 尝试加载最新快照
        Optional<OrderSnapshot> snapshot = snapshotStore.findLatest(aggregateId);
        
        Order order;
        Long fromVersion = 0L;
        
        if (snapshot.isPresent()) {
            // 从快照恢复
            order = deserializeSnapshot(snapshot.get());
            fromVersion = snapshot.get().getVersion();
        } else {
            // 创建空聚合
            order = new Order();
        }
        
        // 2. 加载快照之后的事件
        List<DomainEvent> events = eventStore.findByAggregateId(aggregateId, fromVersion);
        
        // 3. 重放事件
        for (DomainEvent event : events) {
            order.apply(event);
        }
        
        return order.getVersion() > 0 ? Optional.of(order) : Optional.empty();
    }
    
    @Override
    @Transactional
    public void save(Order order) {
        List<DomainEvent> uncommittedEvents = order.getUncommittedEvents();
        
        // 1. 保存事件
        for (DomainEvent event : uncommittedEvents) {
            eventStore.save(event);
        }
        
        // 2. 检查是否需要创建快照
        if (shouldCreateSnapshot(order.getVersion())) {
            createSnapshot(order);
        }
        
        // 3. 标记事件为已提交
        order.markEventsAsCommitted();
    }
    
    private boolean shouldCreateSnapshot(Long version) {
        return version % SNAPSHOT_FREQUENCY == 0;
    }
    
    private void createSnapshot(Order order) {
        try {
            String snapshotData = objectMapper.writeValueAsString(order);
            OrderSnapshot snapshot = new OrderSnapshot(
                order.getId().toString(),
                order.getVersion(),
                snapshotData,
                LocalDateTime.now()
            );
            snapshotStore.save(snapshot);
        } catch (Exception e) {
            // 快照创建失败不应影响业务流程
            log.warn("创建快照失败: {}", e.getMessage());
        }
    }
    
    private Order deserializeSnapshot(OrderSnapshot snapshot) {
        try {
            return objectMapper.readValue(snapshot.getSnapshotData(), Order.class);
        } catch (Exception e) {
            throw new RuntimeException("快照反序列化失败", e);
        }
    }
}

// 事件存储
@Repository
public class JpaEventStore implements EventStore {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public void save(DomainEvent event) {
        EventEntity entity = new EventEntity(
            event.getEventId(),
            event.getAggregateId(),
            event.getClass().getSimpleName(),
            serializeEvent(event),
            event.getVersion(),
            event.getOccurredOn()
        );
        entityManager.persist(entity);
    }
    
    @Override
    public List<DomainEvent> findByAggregateId(String aggregateId, Long fromVersion) {
        List<EventEntity> entities = entityManager
            .createQuery("SELECT e FROM EventEntity e WHERE e.aggregateId = :aggregateId " +
                        "AND e.version > :fromVersion ORDER BY e.version", EventEntity.class)
            .setParameter("aggregateId", aggregateId)
            .setParameter("fromVersion", fromVersion)
            .getResultList();
        
        return entities.stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }
    
    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("事件序列化失败", e);
        }
    }
    
    private DomainEvent deserializeEvent(EventEntity entity) {
        try {
            Class<?> eventClass = Class.forName("com.architecture.domain.event." + entity.getEventType());
            return (DomainEvent) objectMapper.readValue(entity.getEventData(), eventClass);
        } catch (Exception e) {
            throw new RuntimeException("事件反序列化失败", e);
        }
    }
}
```

### 场景八：复杂业务规则验证

**面试题：为订单系统设计复杂的业务规则验证机制，支持规则组合和动态配置**

**参考答案：**

```java
// 业务规则接口
public interface BusinessRule<T> {
    String getRuleName();
    boolean isSatisfied(T target);
    String getViolationMessage();
    RulePriority getPriority();
}

// 规则优先级
public enum RulePriority {
    HIGH(1), MEDIUM(2), LOW(3);
    
    private final int level;
    
    RulePriority(int level) {
        this.level = level;
    }
}

// 规则验证结果
public class ValidationResult {
    private final boolean valid;
    private final List<String> violations;
    
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }
    
    public static ValidationResult failure(List<String> violations) {
        return new ValidationResult(false, violations);
    }
    
    public ValidationResult combine(ValidationResult other) {
        if (this.valid && other.valid) {
            return success();
        }
        
        List<String> allViolations = new ArrayList<>(this.violations);
        allViolations.addAll(other.violations);
        return failure(allViolations);
    }
}

// 订单业务规则实现
public class OrderMinimumAmountRule implements BusinessRule<Order> {
    private final Money minimumAmount;
    
    public OrderMinimumAmountRule(Money minimumAmount) {
        this.minimumAmount = minimumAmount;
    }
    
    @Override
    public String getRuleName() {
        return "OrderMinimumAmount";
    }
    
    @Override
    public boolean isSatisfied(Order order) {
        return order.getTotalAmount().getAmount().compareTo(minimumAmount.getAmount()) >= 0;
    }
    
    @Override
    public String getViolationMessage() {
        return "订单金额不能低于最低限额: " + minimumAmount;
    }
    
    @Override
    public RulePriority getPriority() {
        return RulePriority.HIGH;
    }
}

public class CustomerCreditLimitRule implements BusinessRule<Order> {
    private final CustomerRepository customerRepository;
    
    @Override
    public String getRuleName() {
        return "CustomerCreditLimit";
    }
    
    @Override
    public boolean isSatisfied(Order order) {
        Customer customer = customerRepository.findById(order.getCustomer().getId())
            .orElseThrow(() -> new IllegalArgumentException("客户不存在"));
        
        Money currentCredit = customer.getCurrentCreditUsage();
        Money orderAmount = order.getTotalAmount();
        Money totalCredit = currentCredit.add(orderAmount);
        
        return totalCredit.getAmount().compareTo(customer.getCreditLimit().getAmount()) <= 0;
    }
    
    @Override
    public String getViolationMessage() {
        return "订单金额超出客户信用额度";
    }
    
    @Override
    public RulePriority getPriority() {
        return RulePriority.HIGH;
    }
}

public class ProductAvailabilityRule implements BusinessRule<Order> {
    private final ProductRepository productRepository;
    
    @Override
    public String getRuleName() {
        return "ProductAvailability";
    }
    
    @Override
    public boolean isSatisfied(Order order) {
        return order.getOrderItems().stream()
            .allMatch(item -> {
                Product product = productRepository.findById(item.getProduct().getId())
                    .orElse(null);
                return product != null && product.isAvailable() && 
                       product.getStock() >= item.getQuantity();
            });
    }
    
    @Override
    public String getViolationMessage() {
        return "订单中包含不可用或库存不足的商品";
    }
    
    @Override
    public RulePriority getPriority() {
        return RulePriority.MEDIUM;
    }
}

// 组合规则
public class CompositeBusinessRule<T> implements BusinessRule<T> {
    private final List<BusinessRule<T>> rules;
    private final LogicalOperator operator;
    
    public enum LogicalOperator {
        AND, OR
    }
    
    public CompositeBusinessRule(List<BusinessRule<T>> rules, LogicalOperator operator) {
        this.rules = rules.stream()
            .sorted(Comparator.comparing(rule -> rule.getPriority().getLevel()))
            .collect(Collectors.toList());
        this.operator = operator;
    }
    
    @Override
    public String getRuleName() {
        return "CompositeRule_" + operator.name();
    }
    
    @Override
    public boolean isSatisfied(T target) {
        if (operator == LogicalOperator.AND) {
            return rules.stream().allMatch(rule -> rule.isSatisfied(target));
        } else {
            return rules.stream().anyMatch(rule -> rule.isSatisfied(target));
        }
    }
    
    @Override
    public String getViolationMessage() {
        return "组合规则验证失败";
    }
    
    @Override
    public RulePriority getPriority() {
        return rules.stream()
            .map(BusinessRule::getPriority)
            .min(Comparator.comparing(RulePriority::getLevel))
            .orElse(RulePriority.LOW);
    }
}

// 规则引擎
@Service
public class BusinessRuleEngine {
    private final Map<String, List<BusinessRule<?>>> ruleRegistry = new ConcurrentHashMap<>();
    
    public <T> void registerRule(Class<T> targetType, BusinessRule<T> rule) {
        String key = targetType.getSimpleName();
        ruleRegistry.computeIfAbsent(key, k -> new ArrayList<>()).add(rule);
    }
    
    @SuppressWarnings("unchecked")
    public <T> ValidationResult validate(T target) {
        String key = target.getClass().getSimpleName();
        List<BusinessRule<?>> rules = ruleRegistry.getOrDefault(key, Collections.emptyList());
        
        List<String> violations = new ArrayList<>();
        
        for (BusinessRule<?> rule : rules) {
            BusinessRule<T> typedRule = (BusinessRule<T>) rule;
            if (!typedRule.isSatisfied(target)) {
                violations.add(typedRule.getViolationMessage());
                
                // 高优先级规则失败时立即返回
                if (typedRule.getPriority() == RulePriority.HIGH) {
                    return ValidationResult.failure(violations);
                }
            }
        }
        
        return violations.isEmpty() ? 
            ValidationResult.success() : 
            ValidationResult.failure(violations);
    }
    
    public <T> ValidationResult validateWithDetails(T target) {
        String key = target.getClass().getSimpleName();
        List<BusinessRule<?>> rules = ruleRegistry.getOrDefault(key, Collections.emptyList());
        
        Map<RulePriority, List<String>> violationsByPriority = new EnumMap<>(RulePriority.class);
        
        for (BusinessRule<?> rule : rules) {
            @SuppressWarnings("unchecked")
            BusinessRule<T> typedRule = (BusinessRule<T>) rule;
            
            if (!typedRule.isSatisfied(target)) {
                violationsByPriority
                    .computeIfAbsent(typedRule.getPriority(), k -> new ArrayList<>())
                    .add(String.format("[%s] %s", typedRule.getRuleName(), 
                                     typedRule.getViolationMessage()));
            }
        }
        
        if (violationsByPriority.isEmpty()) {
            return ValidationResult.success();
        }
        
        List<String> allViolations = violationsByPriority.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(RulePriority::getLevel)))
            .flatMap(entry -> entry.getValue().stream())
            .collect(Collectors.toList());
        
        return ValidationResult.failure(allViolations);
    }
}

// 规则配置
@Configuration
public class BusinessRuleConfiguration {
    
    @Bean
    public BusinessRuleEngine businessRuleEngine(
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        
        BusinessRuleEngine engine = new BusinessRuleEngine();
        
        // 注册订单规则
        engine.registerRule(Order.class, new OrderMinimumAmountRule(
            new Money(new BigDecimal("50.00"), "CNY")));
        engine.registerRule(Order.class, new CustomerCreditLimitRule(customerRepository));
        engine.registerRule(Order.class, new ProductAvailabilityRule(productRepository));
        
        // 注册组合规则
        List<BusinessRule<Order>> criticalRules = Arrays.asList(
            new CustomerCreditLimitRule(customerRepository),
            new ProductAvailabilityRule(productRepository)
        );
        engine.registerRule(Order.class, 
            new CompositeBusinessRule<>(criticalRules, CompositeBusinessRule.LogicalOperator.AND));
        
        return engine;
    }
}

// 在聚合中使用规则引擎
public class Order extends AggregateRoot {
    // ... 其他字段和方法
    
    public void confirm(BusinessRuleEngine ruleEngine) {
        // 执行业务规则验证
        ValidationResult result = ruleEngine.validate(this);
        
        if (!result.isValid()) {
            throw new BusinessRuleViolationException(
                "订单确认失败: " + String.join(", ", result.getViolations()));
        }
        
        this.status = OrderStatus.CONFIRMED;
        addDomainEvent(new OrderConfirmedEvent(this.id, this.totalAmount));
    }
}

// 应用服务中的使用
@Service
public class OrderApplicationService {
    
    private final BusinessRuleEngine ruleEngine;
    
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // 使用规则引擎验证
        order.confirm(ruleEngine);
        
        orderRepository.save(order);
    }
}
```

这些新增的案例展示了更高级的DDD实践，包括事件溯源、快照优化、复杂业务规则验证等企业级场景，可以深入考察候选人对DDD高级概念的理解和实现能力。