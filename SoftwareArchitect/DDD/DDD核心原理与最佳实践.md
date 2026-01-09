# DDD核心原理与最佳实践

## 一、DDD核心原理

### 1. 统一语言 (Ubiquitous Language)

#### 原理说明
统一语言是领域专家和开发团队共同使用的语言，它贯穿整个项目的需求分析、设计、开发和测试过程。

#### 实践要点
```java
// 错误示例：技术术语
public class UserService {
    public void persistUser(UserEntity entity) {
        userDAO.insert(entity);
    }
}

// 正确示例：业务语言
public class CustomerService {
    public void registerCustomer(Customer customer) {
        customerRepository.save(customer);
    }
}
```

#### 建立统一语言的步骤
1. **领域专家访谈**：深入了解业务术语
2. **术语词汇表**：建立并维护业务词汇表
3. **代码映射**：确保代码中的类名、方法名与业务术语一致
4. **持续完善**：在项目过程中不断完善和统一术语

### 2. 限界上下文 (Bounded Context)

#### 原理说明
限界上下文定义了模型的明确边界，在边界内模型具有特定的含义和职责。

#### 上下文映射模式

```java
// 订单上下文中的Customer
public class Customer {
    private CustomerId id;
    private String name;
    private Address shippingAddress;
    private CreditLimit creditLimit;
    
    public boolean canPlaceOrder(Money orderAmount) {
        return creditLimit.allows(orderAmount);
    }
}

// 用户管理上下文中的Customer
public class Customer {
    private CustomerId id;
    private String name;
    private Email email;
    private List<Role> roles;
    
    public boolean hasPermission(Permission permission) {
        return roles.stream().anyMatch(role -> role.hasPermission(permission));
    }
}
```

#### 上下文集成策略

1. **共享内核 (Shared Kernel)**
```java
// 共享的值对象
public class CustomerId {
    private final String value;
    
    public CustomerId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("客户ID不能为空");
        }
        this.value = value;
    }
}
```

2. **防腐层 (Anti-Corruption Layer)**
```java
@Component
public class PaymentGatewayAdapter {
    private final ExternalPaymentService externalService;
    
    public PaymentResult processPayment(Payment payment) {
        // 将领域对象转换为外部服务格式
        ExternalPaymentRequest request = convertToExternalFormat(payment);
        
        // 调用外部服务
        ExternalPaymentResponse response = externalService.process(request);
        
        // 将外部响应转换为领域对象
        return convertToDomainFormat(response);
    }
}
```

### 3. 聚合设计原则

#### 原则一：聚合边界内保证强一致性

```java
public class Order extends AggregateRoot {
    private List<OrderItem> orderItems;
    private Money totalAmount;
    
    public void addOrderItem(Product product, int quantity) {
        OrderItem item = new OrderItem(product, quantity);
        this.orderItems.add(item);
        
        // 立即更新总金额，保证一致性
        recalculateTotalAmount();
    }
    
    private void recalculateTotalAmount() {
        this.totalAmount = orderItems.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}
```

#### 原则二：聚合间通过ID引用

```java
public class Order {
    private OrderId id;
    private CustomerId customerId; // 通过ID引用，而不是直接引用Customer对象
    private List<OrderItem> orderItems;
    
    // 如果需要Customer信息，通过Repository获取
    public Customer getCustomer(CustomerRepository customerRepository) {
        return customerRepository.findById(this.customerId)
            .orElseThrow(() -> new CustomerNotFoundException(this.customerId));
    }
}
```

#### 原则三：一个事务只修改一个聚合

```java
@Service
@Transactional
public class OrderApplicationService {
    
    public void confirmOrder(OrderId orderId) {
        // 只加载和修改Order聚合
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.confirm();
        orderRepository.save(order);
        
        // 通过事件通知其他聚合
        domainEventPublisher.publish(new OrderConfirmedEvent(orderId));
    }
}
```

### 4. 领域事件设计

#### 事件设计原则

```java
// 事件应该表达业务事实
public class OrderConfirmedEvent extends DomainEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
    private final LocalDateTime confirmedAt;
    
    public OrderConfirmedEvent(OrderId orderId, CustomerId customerId, 
                              Money totalAmount, LocalDateTime confirmedAt) {
        super(UUID.randomUUID().toString(), LocalDateTime.now());
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.confirmedAt = confirmedAt;
    }
}
```

#### 事件处理模式

```java
// 同步事件处理
@EventListener
@Transactional
public void handleOrderConfirmed(OrderConfirmedEvent event) {
    // 在同一事务中处理
    updateCustomerStatistics(event.getCustomerId(), event.getTotalAmount());
}

// 异步事件处理
@EventListener
@Async
public void handleOrderConfirmedAsync(OrderConfirmedEvent event) {
    // 异步处理，不影响主流程
    sendConfirmationEmail(event.getCustomerId(), event.getOrderId());
}
```

## 二、设计模式最佳实践

### 1. 值对象设计模式

#### 不可变性设计

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "金额不能为空");
        this.currency = Objects.requireNonNull(currency, "货币不能为空");
        
        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new IllegalArgumentException("金额精度超出货币允许范围");
        }
    }
    
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("货币类型不匹配");
        }
    }
}
```

#### 自验证模式

```java
public class Email {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private final String value;
    
    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱地址不能为空");
        }
        
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("邮箱地址格式不正确: " + value);
        }
        
        this.value = value.toLowerCase().trim();
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

### 2. 实体设计模式

#### 身份标识设计

```java
public abstract class EntityId {
    protected final String value;
    
    protected EntityId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("实体ID不能为空");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityId entityId = (EntityId) o;
        return Objects.equals(value, entityId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

public class OrderId extends EntityId {
    public OrderId(String value) {
        super(value);
    }
    
    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }
    
    public static OrderId of(String value) {
        return new OrderId(value);
    }
}
```

#### 实体生命周期管理

```java
public class Product extends Entity<ProductId> {
    private ProductId id;
    private String name;
    private Money price;
    private int stock;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 工厂方法
    public static Product create(String name, Money price, int initialStock) {
        Product product = new Product();
        product.id = ProductId.generate();
        product.name = name;
        product.price = price;
        product.stock = initialStock;
        product.status = ProductStatus.ACTIVE;
        product.createdAt = LocalDateTime.now();
        product.updatedAt = LocalDateTime.now();
        
        return product;
    }
    
    // 业务方法
    public void updatePrice(Money newPrice) {
        if (newPrice.isNegativeOrZero()) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }
        
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("减少数量必须大于0");
        }
        
        if (this.stock < quantity) {
            throw new InsufficientStockException("库存不足");
        }
        
        this.stock -= quantity;
        this.updatedAt = LocalDateTime.now();
        
        if (this.stock == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }
}
```

### 3. 仓储模式

#### 接口设计

```java
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByDateRange(LocalDateTime start, LocalDateTime end);
    
    void save(Order order);
    void delete(OrderId id);
    
    // 复杂查询方法
    Page<Order> findBySpecification(OrderSpecification spec, Pageable pageable);
    long countByStatus(OrderStatus status);
}
```

#### 规格模式实现

```java
public interface OrderSpecification {
    Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb);
    
    default OrderSpecification and(OrderSpecification other) {
        return (root, query, cb) -> cb.and(
            this.toPredicate(root, query, cb),
            other.toPredicate(root, query, cb)
        );
    }
    
    default OrderSpecification or(OrderSpecification other) {
        return (root, query, cb) -> cb.or(
            this.toPredicate(root, query, cb),
            other.toPredicate(root, query, cb)
        );
    }
}

public class OrderByCustomerSpec implements OrderSpecification {
    private final CustomerId customerId;
    
    public OrderByCustomerSpec(CustomerId customerId) {
        this.customerId = customerId;
    }
    
    @Override
    public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("customerId"), customerId.getValue());
    }
}
```

### 4. 工厂模式

#### 聚合工厂

```java
@Component
public class OrderFactory {
    
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    
    public Order createOrder(CreateOrderRequest request) {
        // 验证客户
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));
        
        if (!customer.isActive()) {
            throw new InactiveCustomerException("客户未激活");
        }
        
        // 创建订单
        Order order = new Order(
            OrderId.generate(),
            customer.getId(),
            new Address(request.getShippingAddress())
        );
        
        // 添加订单项
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            
            validateProductAvailability(product, itemRequest.getQuantity());
            
            order.addOrderItem(product, itemRequest.getQuantity());
        }
        
        return order;
    }
    
    private void validateProductAvailability(Product product, int quantity) {
        if (!product.isAvailable()) {
            throw new ProductUnavailableException(product.getId());
        }
        
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getId(), quantity);
        }
    }
}
```

## 三、架构模式

### 1. 分层架构

```
┌─────────────────────────────────────┐
│          用户界面层 (UI Layer)        │
│  Controller, DTO, View Model        │
├─────────────────────────────────────┤
│          应用层 (Application)        │
│  Application Service, Command/Query  │
├─────────────────────────────────────┤
│           领域层 (Domain)            │
│  Entity, Value Object, Service       │
├─────────────────────────────────────┤
│        基础设施层 (Infrastructure)    │
│  Repository Impl, Database, MQ       │
└─────────────────────────────────────┘
```

### 2. 六边形架构 (端口适配器)

```java
// 端口 (接口)
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
}

public interface PaymentGateway {
    PaymentResult processPayment(Payment payment);
}

// 适配器 (实现)
@Repository
public class JpaOrderRepository implements OrderRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public void save(Order order) {
        entityManager.persist(order);
    }
}

@Component
public class StripePaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult processPayment(Payment payment) {
        // 调用Stripe API
        return new PaymentResult(true, "SUCCESS");
    }
}
```

### 3. CQRS (命令查询职责分离)

```java
// 命令端
@Component
public class CreateOrderCommandHandler {
    
    @Transactional
    public OrderCreatedEvent handle(CreateOrderCommand command) {
        Order order = orderFactory.createOrder(command);
        orderRepository.save(order);
        
        return new OrderCreatedEvent(order.getId(), order.getCustomerId());
    }
}

// 查询端
@Component
public class OrderQueryService {
    
    public List<OrderSummaryDTO> getOrdersByCustomer(CustomerId customerId) {
        return orderReadModelRepository.findByCustomerId(customerId)
            .stream()
            .map(this::toSummaryDTO)
            .collect(Collectors.toList());
    }
}
```

## 四、测试策略

### 1. 单元测试

```java
@ExtendWith(MockitoExtension.class)
class OrderTest {
    
    @Test
    void should_calculate_total_amount_correctly() {
        // Given
        Order order = createTestOrder();
        Product product = createTestProduct(new Money(BigDecimal.valueOf(100), Currency.CNY));
        
        // When
        order.addOrderItem(product, 2);
        
        // Then
        Money expectedTotal = new Money(BigDecimal.valueOf(200), Currency.CNY);
        assertEquals(expectedTotal, order.getTotalAmount());
    }
    
    @Test
    void should_throw_exception_when_adding_unavailable_product() {
        // Given
        Order order = createTestOrder();
        Product unavailableProduct = createUnavailableProduct();
        
        // When & Then
        assertThrows(ProductUnavailableException.class, () -> {
            order.addOrderItem(unavailableProduct, 1);
        });
    }
}
```

### 2. 集成测试

```java
@SpringBootTest
@Transactional
class OrderApplicationServiceIntegrationTest {
    
    @Autowired
    private OrderApplicationService orderService;
    
    @Test
    void should_create_order_successfully() {
        // Given
        CreateOrderCommand command = createValidOrderCommand();
        
        // When
        OrderDTO result = orderService.createOrder(command);
        
        // Then
        assertNotNull(result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
    }
}
```

这些原理和实践为DDD项目提供了坚实的理论基础和实施指导。