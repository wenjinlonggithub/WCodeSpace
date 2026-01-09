# DDD（领域驱动设计）面试题集

## 一、理论基础题

### 1. DDD核心概念

**Q1: 什么是领域驱动设计(DDD)？它解决了什么问题？**

**参考答案：**
- DDD是一种软件开发方法论，强调将业务领域的复杂性作为软件设计的核心
- 解决的问题：
  - 业务逻辑分散，难以维护
  - 技术实现与业务需求脱节
  - 大型系统的复杂性管理
  - 团队沟通成本高，缺乏统一语言

**Q2: 解释DDD中的"统一语言"(Ubiquitous Language)概念及其重要性**

**参考答案：**
- 统一语言是领域专家和开发团队共同使用的语言
- 重要性：
  - 减少沟通误解
  - 代码更贴近业务
  - 便于需求变更
  - 提高代码可读性

**Q3: DDD的四层架构分别是什么？各层的职责是什么？**

**参考答案：**
1. **用户界面层(User Interface Layer)**
   - 职责：展示信息、接收用户输入
   - 组件：Controller、View、DTO

2. **应用层(Application Layer)**
   - 职责：协调领域对象完成业务用例
   - 组件：ApplicationService、事务管理

3. **领域层(Domain Layer)**
   - 职责：核心业务逻辑和规则
   - 组件：Entity、ValueObject、Aggregate、DomainService

4. **基础设施层(Infrastructure Layer)**
   - 职责：技术实现和外部系统交互
   - 组件：Repository实现、数据库、消息队列

### 2. 核心构建块

**Q4: 实体(Entity)和值对象(Value Object)的区别是什么？**

**参考答案：**

| 特性 | 实体(Entity) | 值对象(Value Object) |
|------|-------------|---------------------|
| 标识性 | 有唯一标识(ID) | 无标识，基于值相等 |
| 可变性 | 可变 | 不可变 |
| 生命周期 | 有完整生命周期 | 随实体创建/销毁 |
| 相等性 | 基于ID比较 | 基于所有属性值比较 |
| 示例 | Customer、Order | Money、Address、Email |

**Q5: 什么是聚合根(Aggregate Root)？它的作用是什么？**

**参考答案：**
- 聚合根是聚合的唯一入口，外部只能通过聚合根访问聚合内的对象
- 作用：
  - 维护数据一致性边界
  - 定义事务边界
  - 封装业务不变量
  - 控制并发访问

**Q6: 仓储模式(Repository Pattern)的设计原则是什么？**

**参考答案：**
- 接口定义在领域层，实现在基础设施层
- 提供集合语义的操作方法
- 只为聚合根创建仓储
- 封装查询逻辑，避免暴露数据访问细节
- 支持单元测试，便于Mock

### 3. 高级概念

**Q7: 什么是限界上下文(Bounded Context)？如何划分？**

**参考答案：**
- 限界上下文是模型的明确边界，在边界内模型具有特定含义
- 划分原则：
  - 按业务能力划分
  - 按团队结构划分
  - 按数据一致性要求划分
  - 按技术架构划分

**Q8: 领域事件(Domain Event)的作用和实现方式？**

**参考答案：**
- 作用：
  - 实现聚合间解耦
  - 支持最终一致性
  - 记录业务事实
  - 触发后续业务流程

- 实现方式：
  - 事件发布/订阅机制
  - 消息队列
  - 事件存储
  - 异步处理

## 二、设计模式题

**Q9: 在DDD中如何使用工厂模式？请举例说明**

**参考答案：**
```java
public class OrderFactory {
    public Order createOrder(Customer customer, Address address) {
        // 验证业务规则
        if (!customer.isActive()) {
            throw new IllegalStateException("客户未激活");
        }
        
        // 生成订单ID
        Long orderId = generateOrderId();
        
        // 创建订单
        return new Order(orderId, customer, address);
    }
    
    private Long generateOrderId() {
        // 订单ID生成逻辑
        return System.currentTimeMillis();
    }
}
```

**Q10: 规格模式(Specification Pattern)在DDD中的应用场景？**

**参考答案：**
- 应用场景：
  - 复杂查询条件组合
  - 业务规则验证
  - 动态查询构建
  - 规则重用

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
}

// 使用示例
AvailableProductSpec availableSpec = new AvailableProductSpec();
AffordableProductSpec affordableSpec = new AffordableProductSpec(maxPrice);
Specification<Product> combinedSpec = availableSpec.and(affordableSpec);
```

## 三、实践应用题

**Q11: 如何设计一个电商系统的订单聚合？**

**参考答案：**
```java
public class Order {  // 聚合根
    private Long id;
    private Customer customer;
    private List<OrderItem> orderItems;  // 聚合内实体
    private OrderStatus status;
    private Address shippingAddress;
    private Money totalAmount;
    
    // 业务方法
    public void addOrderItem(Product product, int quantity) {
        // 验证业务规则
        if (!product.isAvailable()) {
            throw new IllegalStateException("商品不可用");
        }
        
        // 创建订单项
        OrderItem item = new OrderItem(product, quantity);
        this.orderItems.add(item);
        
        // 更新总金额（维护不变量）
        calculateTotalAmount();
    }
    
    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("订单不能为空");
        }
        this.status = OrderStatus.CONFIRMED;
        // 发布领域事件
        DomainEventPublisher.publish(new OrderConfirmedEvent(this.id));
    }
}
```

**Q12: 应用服务(Application Service)应该包含什么逻辑？**

**参考答案：**
应用服务应该包含：
- 用例协调逻辑
- 事务管理
- DTO转换
- 权限验证
- 异常处理

不应该包含：
- 业务规则逻辑
- 数据访问逻辑
- 技术实现细节
    
```java
@Service
@Transactional
public class OrderApplicationService {
    
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. 参数验证
        validateRequest(request);
        
        // 2. 加载聚合
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("客户不存在"));
        
        // 3. 执行业务操作（委托给领域对象）
        Order order = new Order(generateId(), customer, createAddress(request));
        
        // 4. 持久化
        orderRepository.save(order);
        
        // 5. 返回DTO
        return convertToDTO(order);
    }
}
```

## 四、架构设计题

**Q13: 如何处理跨聚合的数据一致性？**

**参考答案：**
1. **最终一致性**：通过领域事件实现
2. **Saga模式**：长事务分解为多个步骤
3. **事件溯源**：记录所有状态变更事件
4. **补偿机制**：失败时执行回滚操作

**Q14: 微服务架构中如何应用DDD？**

**参考答案：**
- 按限界上下文划分微服务
- 每个微服务有独立的数据库
- 通过API网关统一入口
- 使用事件驱动架构实现服务间通信
- 实现分布式事务管理

## 五、代码实现题

**Q15: 实现一个Money值对象，要求支持不同货币的运算**

**参考答案：**
```java
public class Money {
    private final BigDecimal amount;
    private final String currency;
    
    public Money(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为空或负数");
        }
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("货币类型不匹配");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && 
               Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
```

**Q16: 设计一个库存管理的领域服务**

**参考答案：**
```java
@Service
public class InventoryDomainService {
    
    public boolean reserveStock(Product product, int quantity) {
        if (!product.isAvailable()) {
            return false;
        }
        
        if (product.getStock() < quantity) {
            return false;
        }
        
        product.reserveStock(quantity);
        return true;
    }
    
    public void releaseReservedStock(Product product, int quantity) {
        product.releaseReservedStock(quantity);
    }
    
    public boolean canFulfillOrder(List<OrderItem> items) {
        return items.stream()
            .allMatch(item -> item.getProduct().getStock() >= item.getQuantity());
    }
}
```

## 六、性能优化题

**Q17: DDD项目中如何处理N+1查询问题？**

**参考答案：**
1. **批量加载**：使用Repository的批量查询方法
2. **预加载**：在聚合根中预加载相关对象
3. **缓存策略**：对频繁访问的数据进行缓存
4. **CQRS模式**：读写分离，优化查询性能

**Q18: 大型聚合的性能问题如何解决？**

**参考答案：**
1. **聚合拆分**：将大聚合拆分为多个小聚合
2. **懒加载**：按需加载聚合内的对象
3. **快照机制**：定期保存聚合状态快照
4. **事件溯源**：通过事件重建聚合状态

## 七、测试相关题

**Q19: 如何对DDD项目进行单元测试？**

**参考答案：**
```java
@Test
public void should_calculate_total_amount_correctly() {
    // Given
    Customer customer = new Customer(1L, "张三", email, "13800138000");
    Order order = new Order(1L, customer, address);
    Product product = new Product(1L, "商品", "描述", price, 100, category);
    
    // When
    order.addOrderItem(product, 2);
    
    // Then
    Money expectedTotal = price.multiply(2).multiply(customer.getLevel().getDiscount());
    assertEquals(expectedTotal, order.getTotalAmount());
}

@Test
public void should_throw_exception_when_stock_insufficient() {
    // Given
    Product product = new Product(1L, "商品", "描述", price, 1, category);
    
    // When & Then
    assertThrows(IllegalStateException.class, () -> {
        order.addOrderItem(product, 2);
    });
}
```

**Q20: 如何测试应用服务？**

**参考答案：**
```java
@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private OrderApplicationService orderService;
    
    @Test
    void should_create_order_successfully() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        Customer customer = mock(Customer.class);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        
        // When
        OrderDTO result = orderService.createOrder(request);
        
        // Then
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }
}
```

## 八、实际场景题

**Q21: 设计一个支付系统，如何处理支付状态的一致性？**

**参考答案：**
```java
public class Payment {
    private PaymentId id;
    private Money amount;
    private PaymentStatus status;
    private List<PaymentEvent> events;
    
    public void process() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("只能处理待支付订单");
        }
        
        // 调用第三方支付
        PaymentResult result = paymentGateway.pay(this.amount);
        
        if (result.isSuccess()) {
            this.status = PaymentStatus.SUCCESS;
            addEvent(new PaymentSuccessEvent(this.id));
        } else {
            this.status = PaymentStatus.FAILED;
            addEvent(new PaymentFailedEvent(this.id, result.getErrorCode()));
        }
    }
}
```

**Q22: 电商系统中如何设计促销活动的领域模型？**

**参考答案：**
```java
public class Promotion {
    private PromotionId id;
    private String name;
    private PromotionRule rule;
    private DateRange validPeriod;
    private PromotionStatus status;
    
    public Money calculateDiscount(Order order) {
        if (!isApplicable(order)) {
            return Money.ZERO;
        }
        
        return rule.calculateDiscount(order);
    }
    
    private boolean isApplicable(Order order) {
        return status == PromotionStatus.ACTIVE 
            && validPeriod.contains(LocalDateTime.now())
            && rule.isApplicable(order);
    }
}

// 策略模式实现不同促销规则
public interface PromotionRule {
    Money calculateDiscount(Order order);
    boolean isApplicable(Order order);
}

public class PercentageDiscountRule implements PromotionRule {
    private final BigDecimal percentage;
    
    @Override
    public Money calculateDiscount(Order order) {
        return order.getTotalAmount().multiply(percentage);
    }
}
```

## 九、架构演进题

**Q23: 从传统三层架构迁移到DDD架构的步骤？**

**参考答案：**
1. **识别领域边界**：分析业务流程，识别核心领域
2. **提取领域模型**：将业务逻辑从Service层提取到领域对象
3. **重构数据模型**：按聚合重新组织数据结构
4. **引入仓储模式**：抽象数据访问层
5. **实现应用服务**：协调领域对象完成用例
6. **逐步重构**：采用绞杀者模式逐步替换

**Q24: DDD在微服务拆分中的指导原则？**

**参考答案：**
1. **按限界上下文拆分**：每个微服务对应一个限界上下文
2. **数据独立性**：每个服务有独立的数据存储
3. **业务能力对齐**：服务边界与业务能力边界一致
4. **团队结构匹配**：服务划分与团队结构匹配
5. **避免分布式事务**：通过事件驱动实现最终一致性

## 十、综合应用题

**Q25: 设计一个完整的订单处理流程，包括库存扣减、支付、发货等环节**

**参考答案：**
```java
// 订单处理Saga
public class OrderProcessingSaga {
    
    @SagaOrchestrationStart
    public void handle(OrderCreatedEvent event) {
        // 1. 扣减库存
        commandGateway.send(new ReserveInventoryCommand(
            event.getOrderId(), event.getOrderItems()));
    }
    
    @SagaOrchestrationContinue
    public void handle(InventoryReservedEvent event) {
        // 2. 处理支付
        commandGateway.send(new ProcessPaymentCommand(
            event.getOrderId(), event.getAmount()));
    }
    
    @SagaOrchestrationContinue
    public void handle(PaymentProcessedEvent event) {
        if (event.isSuccess()) {
            // 3. 确认订单
            commandGateway.send(new ConfirmOrderCommand(event.getOrderId()));
        } else {
            // 补偿：释放库存
            commandGateway.send(new ReleaseInventoryCommand(event.getOrderId()));
        }
    }
    
    @SagaOrchestrationContinue
    public void handle(OrderConfirmedEvent event) {
        // 4. 安排发货
        commandGateway.send(new ScheduleShippingCommand(event.getOrderId()));
    }
}
```

## 十一、高级架构题

**Q26: 如何在DDD中实现CQRS模式？**

**参考答案：**
```java
// 命令模型
public class CreateOrderCommand {
    private final Long customerId;
    private final List<OrderItemRequest> items;
    private final Address shippingAddress;
    
    // 构造函数和getter
}

// 命令处理器
@Component
public class CreateOrderCommandHandler {
    
    @Transactional
    public OrderCreatedEvent handle(CreateOrderCommand command) {
        // 1. 验证命令
        validateCommand(command);
        
        // 2. 创建聚合
        Order order = orderFactory.createOrder(command);
        
        // 3. 保存聚合
        orderRepository.save(order);
        
        // 4. 发布事件
        return new OrderCreatedEvent(order.getId(), order.getCustomerId());
    }
}

// 查询模型
@Entity
@Table(name = "order_view")
public class OrderView {
    @Id
    private Long orderId;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime orderTime;
    // 优化查询的冗余字段
}

// 查询处理器
@Component
public class OrderQueryHandler {
    
    public List<OrderSummaryDTO> getOrdersByCustomer(Long customerId) {
        return orderViewRepository.findByCustomerId(customerId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}

// 事件处理器更新查询模型
@EventHandler
public class OrderViewUpdater {
    
    @Transactional
    public void on(OrderCreatedEvent event) {
        OrderView view = new OrderView();
        view.setOrderId(event.getOrderId());
        view.setCustomerName(event.getCustomerName());
        view.setStatus("CREATED");
        orderViewRepository.save(view);
    }
}
```

**Q27: 如何处理分布式系统中的数据一致性？**

**参考答案：**
```java
// Saga模式实现
@SagaOrchestrationStart
public class OrderProcessingSaga {
    
    @Autowired
    private CommandGateway commandGateway;
    
    public void handle(OrderCreatedEvent event) {
        // 步骤1: 检查库存
        commandGateway.send(new CheckInventoryCommand(
            event.getOrderId(), event.getItems()));
    }
    
    @SagaOrchestrationContinue
    public void handle(InventoryCheckedEvent event) {
        if (event.isAvailable()) {
            // 步骤2: 预留库存
            commandGateway.send(new ReserveInventoryCommand(
                event.getOrderId(), event.getItems()));
        } else {
            // 补偿: 取消订单
            commandGateway.send(new CancelOrderCommand(event.getOrderId()));
        }
    }
    
    @SagaOrchestrationContinue
    public void handle(InventoryReservedEvent event) {
        // 步骤3: 处理支付
        commandGateway.send(new ProcessPaymentCommand(
            event.getOrderId(), event.getAmount()));
    }
    
    @SagaOrchestrationContinue
    public void handle(PaymentProcessedEvent event) {
        if (event.isSuccess()) {
            // 步骤4: 确认订单
            commandGateway.send(new ConfirmOrderCommand(event.getOrderId()));
        } else {
            // 补偿: 释放库存
            commandGateway.send(new ReleaseInventoryCommand(event.getOrderId()));
            commandGateway.send(new CancelOrderCommand(event.getOrderId()));
        }
    }
}

// 事件溯源实现
@Entity
public class EventStore {
    @Id
    private String eventId;
    private String aggregateId;
    private String eventType;
    private String eventData;
    private LocalDateTime timestamp;
    private Long version;
}

public class EventSourcingRepository<T extends AggregateRoot> {
    
    public T findById(String aggregateId) {
        List<DomainEvent> events = eventStore.findByAggregateId(aggregateId);
        return replayEvents(events);
    }
    
    public void save(T aggregate) {
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        for (DomainEvent event : events) {
            eventStore.save(event);
        }
        aggregate.markEventsAsCommitted();
    }
    
    private T replayEvents(List<DomainEvent> events) {
        T aggregate = createEmptyAggregate();
        for (DomainEvent event : events) {
            aggregate.apply(event);
        }
        return aggregate;
    }
}
```

## 十二、性能与扩展性题

**Q28: 大型DDD系统如何处理性能瓶颈？**

**参考答案：**

**1. 聚合设计优化**
```java
// 问题：大聚合导致性能问题
public class Order {
    private List<OrderItem> items; // 可能有数百个商品
    private List<OrderHistory> histories; // 大量历史记录
    
    // 解决方案：聚合拆分
}

// 优化后：拆分为多个小聚合
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private OrderStatus status;
    private Money totalAmount;
    // 移除大集合，通过Repository按需加载
}

public class OrderItem {
    private OrderItemId id;
    private OrderId orderId; // 外键关联
    private ProductId productId;
    private int quantity;
}

// 按需加载
public class OrderService {
    public OrderDetailDTO getOrderDetail(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return buildDetailDTO(order, items);
    }
}
```

**2. 缓存策略**
```java
@Service
public class ProductQueryService {
    
    @Cacheable(value = "products", key = "#productId")
    public ProductDTO getProduct(Long productId) {
        return productRepository.findById(productId)
            .map(this::toDTO)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    @CacheEvict(value = "products", key = "#productId")
    public void evictProductCache(Long productId) {
        // 当产品信息更新时清除缓存
    }
    
    // 批量缓存预热
    @PostConstruct
    public void warmUpCache() {
        List<Product> hotProducts = productRepository.findHotProducts();
        hotProducts.forEach(product -> 
            cacheManager.getCache("products").put(product.getId(), toDTO(product)));
    }
}
```

**3. 异步处理**
```java
@Component
public class OrderEventHandler {
    
    @EventListener
    @Async("orderProcessingExecutor")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // 异步处理耗时操作
        emailService.sendConfirmationEmail(event.getCustomerId());
        reportingService.updateStatistics(event);
        loyaltyService.addPoints(event.getCustomerId(), event.getAmount());
    }
    
    @Bean("orderProcessingExecutor")
    public TaskExecutor orderProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("order-processing-");
        return executor;
    }
}
```

**Q29: 如何设计支持多租户的DDD系统？**

**参考答案：**
```java
// 租户上下文
public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    public static void clear() {
        TENANT_ID.remove();
    }
}

// 多租户实体基类
@MappedSuperclass
public abstract class TenantAwareEntity {
    @Column(name = "tenant_id")
    private String tenantId;
    
    @PrePersist
    public void prePersist() {
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
    }
}

// 多租户聚合根
public class Order extends TenantAwareEntity {
    private OrderId id;
    private CustomerId customerId;
    // 其他字段
    
    // 业务方法自动包含租户隔离
    public void addOrderItem(Product product, int quantity) {
        validateTenant(product.getTenantId());
        // 业务逻辑
    }
    
    private void validateTenant(String productTenantId) {
        if (!getTenantId().equals(productTenantId)) {
            throw new IllegalArgumentException("跨租户操作不被允许");
        }
    }
}

// 多租户仓储
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    
    @Override
    public Optional<Order> findById(OrderId id) {
        return entityManager.createQuery(
            "SELECT o FROM Order o WHERE o.id = :id AND o.tenantId = :tenantId", 
            Order.class)
            .setParameter("id", id)
            .setParameter("tenantId", TenantContext.getTenantId())
            .getResultStream()
            .findFirst();
    }
    
    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return entityManager.createQuery(
            "SELECT o FROM Order o WHERE o.customerId = :customerId AND o.tenantId = :tenantId", 
            Order.class)
            .setParameter("customerId", customerId)
            .setParameter("tenantId", TenantContext.getTenantId())
            .getResultList();
    }
}

// 租户拦截器
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String tenantId = extractTenantId(request);
        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler, Exception ex) {
        TenantContext.clear();
    }
    
    private String extractTenantId(HttpServletRequest request) {
        // 从Header、JWT Token或子域名中提取租户ID
        return request.getHeader("X-Tenant-ID");
    }
}
```

## 十三、安全与权限题

**Q30: 在DDD中如何实现细粒度的权限控制？**

**参考答案：**
```java
// 权限值对象
public class Permission {
    private final String resource;
    private final String action;
    
    public Permission(String resource, String action) {
        this.resource = resource;
        this.action = action;
    }
    
    public boolean matches(String resource, String action) {
        return this.resource.equals(resource) && this.action.equals(action);
    }
}

// 用户聚合包含权限
public class User {
    private UserId id;
    private String username;
    private Set<Permission> permissions;
    private Set<Role> roles;
    
    public boolean hasPermission(String resource, String action) {
        // 直接权限检查
        if (permissions.stream().anyMatch(p -> p.matches(resource, action))) {
            return true;
        }
        
        // 角色权限检查
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.matches(resource, action));
    }
    
    public boolean canAccessOrder(Order order) {
        // 资源级权限检查
        return hasPermission("order", "read") || 
               (hasPermission("order", "read_own") && order.belongsTo(this.id));
    }
}

// 权限检查服务
@Service
public class AuthorizationService {
    
    public void checkPermission(String resource, String action) {
        User currentUser = getCurrentUser();
        if (!currentUser.hasPermission(resource, action)) {
            throw new AccessDeniedException("权限不足");
        }
    }
    
    public void checkOrderAccess(Order order, String action) {
        User currentUser = getCurrentUser();
        if (!currentUser.canAccessOrder(order)) {
            throw new AccessDeniedException("无权访问此订单");
        }
    }
}

// 在聚合中集成权限检查
public class Order {
    public void cancel(User user) {
        // 业务规则检查
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("只能取消待处理订单");
        }
        
        // 权限检查
        if (!user.hasPermission("order", "cancel") && !belongsTo(user.getId())) {
            throw new AccessDeniedException("无权取消此订单");
        }
        
        this.status = OrderStatus.CANCELLED;
        addDomainEvent(new OrderCancelledEvent(this.id, user.getId()));
    }
}

// 方法级权限注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String resource();
    String action();
}

// 权限切面
@Aspect
@Component
public class PermissionAspect {
    
    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        authorizationService.checkPermission(
            requiresPermission.resource(), 
            requiresPermission.action());
    }
}

// 使用示例
@Service
public class OrderApplicationService {
    
    @RequiresPermission(resource = "order", action = "create")
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 业务逻辑
    }
    
    @RequiresPermission(resource = "order", action = "cancel")
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        User currentUser = getCurrentUser();
        order.cancel(currentUser);
        
        orderRepository.save(order);
    }
}
```

## 十四、监控与可观测性题

**Q31: 如何为DDD系统设计监控和可观测性？**

**参考答案：**
```java
// 领域事件监控
@Component
public class DomainEventMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter eventCounter;
    private final Timer eventProcessingTimer;
    
    public DomainEventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.eventCounter = Counter.builder("domain.events.published")
            .description("Domain events published")
            .register(meterRegistry);
        this.eventProcessingTimer = Timer.builder("domain.events.processing.time")
            .description("Domain event processing time")
            .register(meterRegistry);
    }
    
    @EventListener
    public void recordEvent(DomainEvent event) {
        eventCounter.increment(
            Tags.of(
                "event.type", event.getClass().getSimpleName(),
                "aggregate.type", event.getAggregateType()
            )
        );
    }
}

// 聚合操作监控
@Aspect
@Component
public class AggregateMetricsAspect {
    private final MeterRegistry meterRegistry;
    
    @Around("execution(* com.architecture.domain.aggregate.*.*(..))")
    public Object measureAggregateOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String aggregateType = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        return Timer.Sample.start(meterRegistry)
            .stop(Timer.builder("aggregate.operation.time")
                .description("Aggregate operation execution time")
                .tag("aggregate.type", aggregateType)
                .tag("method", methodName)
                .register(meterRegistry), 
                () -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable throwable) {
                        meterRegistry.counter("aggregate.operation.errors",
                            "aggregate.type", aggregateType,
                            "method", methodName,
                            "error.type", throwable.getClass().getSimpleName())
                            .increment();
                        throw new RuntimeException(throwable);
                    }
                });
    }
}

// 业务指标监控
@Component
public class BusinessMetrics {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void recordOrderCreated(OrderCreatedEvent event) {
        meterRegistry.counter("business.orders.created",
            "customer.level", event.getCustomerLevel())
            .increment();
            
        meterRegistry.gauge("business.orders.total.amount",
            Tags.of("currency", event.getAmount().getCurrency()),
            event.getAmount().getAmount().doubleValue());
    }
    
    @EventListener
    public void recordOrderCancelled(OrderCancelledEvent event) {
        meterRegistry.counter("business.orders.cancelled",
            "reason", event.getCancelReason())
            .increment();
    }
}

// 分布式追踪
@Component
public class TracingService {
    private final Tracer tracer;
    
    public void traceAggregateOperation(String aggregateType, String operation, Runnable task) {
        Span span = tracer.nextSpan()
            .name(aggregateType + "." + operation)
            .tag("aggregate.type", aggregateType)
            .tag("operation", operation)
            .start();
            
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            task.run();
        } catch (Exception e) {
            span.tag("error", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}

// 健康检查
@Component
public class DomainHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // 检查关键聚合的健康状态
            checkOrderAggregateHealth();
            checkCustomerAggregateHealth();
            
            return Health.up()
                .withDetail("domain.status", "healthy")
                .withDetail("last.check", LocalDateTime.now())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("domain.status", "unhealthy")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private void checkOrderAggregateHealth() {
        // 检查订单聚合的关键指标
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        if (pendingOrders > 10000) {
            throw new RuntimeException("待处理订单过多: " + pendingOrders);
        }
    }
}
```

这套扩展的面试题涵盖了DDD的高级概念、性能优化、安全权限、监控可观测性等企业级应用场景，可以全面考察候选人在复杂系统中应用DDD的能力。