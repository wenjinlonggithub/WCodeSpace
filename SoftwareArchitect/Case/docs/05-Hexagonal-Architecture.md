# 六边形架构 (Hexagonal Architecture)

## 一、架构概述

六边形架构(也称为端口和适配器架构 Ports and Adapters Architecture)是一种软件架构模式,旨在将应用程序的核心业务逻辑与外部依赖(数据库、UI、消息队列等)隔离开来,使系统更加灵活、可测试和可维护。

## 二、解决的业务问题

### 1. 传统分层架构的痛点
- **外部依赖渗透**: 业务逻辑依赖具体的技术实现
- **难以测试**: 需要依赖数据库等外部系统
- **技术锁定**: 更换技术框架成本高
- **边界模糊**: 层与层之间职责不清晰

### 2. 六边形架构的解决方案
- **依赖倒置**: 核心业务不依赖外部,外部依赖核心
- **易于测试**: 可以用Mock替换外部依赖
- **技术无关**: 核心业务与技术实现解耦
- **边界清晰**: 通过端口定义明确的边界

### 3. 适用场景
- **复杂业务逻辑**: 需要保护核心业务规则
- **多渠道接入**: Web、移动端、API等
- **技术迁移**: 需要频繁更换技术栈
- **高可测试性**: 要求高单元测试覆盖率

## 三、核心概念

### 1. 架构图

```
             外部世界
    ┌──────────────────────────┐
    │      Primary Adapters     │  ← 驱动适配器(主动方)
    │  (Web UI, REST API...)    │
    └────────────┬───────────────┘
                 │
         ┌───────▼────────┐
         │  Primary Ports │  ← 输入端口
         │  (Use Cases)   │
         └───────┬────────┘
                 │
    ┌────────────▼─────────────┐
    │    Application Core      │  ← 核心领域
    │   (Domain Model,         │
    │    Business Logic)       │
    └────────────┬─────────────┘
                 │
         ┌───────▼────────┐
         │ Secondary Ports│  ← 输出端口
         │ (Repositories, │
         │  Gateways...)  │
         └───────┬────────┘
                 │
    ┌────────────▼───────────────┐
    │   Secondary Adapters       │  ← 被驱动适配器(被动方)
    │ (Database, Message Queue...) │
    └────────────────────────────┘
```

### 2. 核心组件

#### 领域模型 (Domain Model)
```java
// 核心业务实体,不依赖任何外部框架
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;

    public Order(OrderId id, CustomerId customerId) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.totalAmount = Money.ZERO;
        this.status = OrderStatus.DRAFT;
    }

    // 纯业务逻辑,不依赖外部
    public void addItem(Product product, int quantity) {
        if (this.status != OrderStatus.DRAFT) {
            throw new IllegalStateException("只能在草稿状态添加商品");
        }

        OrderItem item = new OrderItem(product.getId(), quantity, product.getPrice());
        this.items.add(item);
        this.recalculateTotalAmount();
    }

    public void submit() {
        validateCanSubmit();
        this.status = OrderStatus.SUBMITTED;
    }

    private void validateCanSubmit() {
        if (items.isEmpty()) {
            throw new OrderValidationException("订单不能为空");
        }
        if (totalAmount.isZero()) {
            throw new OrderValidationException("订单金额不能为0");
        }
    }

    private void recalculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    // Getters
    public OrderId getId() { return id; }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
}
```

#### 输入端口 (Primary Port - Use Case Interface)
```java
// 定义应用的用例接口,在核心层
public interface CreateOrderUseCase {
    OrderId execute(CreateOrderCommand command);
}

public interface GetOrderUseCase {
    OrderDTO execute(OrderId orderId);
}

public interface CancelOrderUseCase {
    void execute(CancelOrderCommand command);
}

// 命令对象
public class CreateOrderCommand {
    private final CustomerId customerId;
    private final List<OrderItemDTO> items;
    private final Address shippingAddress;

    // Constructor, Getters
}
```

#### 输出端口 (Secondary Port - Repository Interface)
```java
// 仓储接口定义在领域层,不依赖具体实现
public interface OrderRepository {
    Order findById(OrderId id);
    void save(Order order);
    void delete(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
}

// 外部服务接口
public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}

public interface EmailService {
    void sendOrderConfirmation(CustomerId customerId, OrderId orderId);
}

public interface InventoryService {
    boolean checkStock(ProductId productId, int quantity);
    void reserveStock(ProductId productId, int quantity);
}
```

#### 应用服务 (Application Service - Use Case Implementation)
```java
// 实现用例接口,协调领域对象和输出端口
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final OrderFactory orderFactory;

    // 构造函数注入(依赖倒置)
    public CreateOrderService(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        InventoryService inventoryService,
        OrderFactory orderFactory
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.orderFactory = orderFactory;
    }

    @Override
    public OrderId execute(CreateOrderCommand command) {
        // 1. 验证库存
        for (OrderItemDTO item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId());
            boolean hasStock = inventoryService.checkStock(
                product.getId(),
                item.getQuantity()
            );
            if (!hasStock) {
                throw new InsufficientStockException(product.getId());
            }
        }

        // 2. 创建订单(使用工厂)
        Order order = orderFactory.createFrom(command);

        // 3. 添加商品
        for (OrderItemDTO item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId());
            order.addItem(product, item.getQuantity());
        }

        // 4. 提交订单
        order.submit();

        // 5. 保存
        orderRepository.save(order);

        // 6. 预留库存
        for (OrderItemDTO item : command.getItems()) {
            inventoryService.reserveStock(
                item.getProductId(),
                item.getQuantity()
            );
        }

        return order.getId();
    }
}
```

### 3. 主适配器 (Primary Adapter)

#### REST API适配器
```java
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    // 构造函数注入
    public OrderRestController(
        CreateOrderUseCase createOrderUseCase,
        GetOrderUseCase getOrderUseCase,
        CancelOrderUseCase cancelOrderUseCase
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody CreateOrderRequest request
    ) {
        // 将HTTP请求转换为领域命令
        CreateOrderCommand command = mapToCommand(request);

        // 调用用例
        OrderId orderId = createOrderUseCase.execute(command);

        // 返回HTTP响应
        OrderResponse response = new OrderResponse(orderId.getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String id) {
        OrderDTO order = getOrderUseCase.execute(new OrderId(id));
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
        @PathVariable String id,
        @RequestBody CancelOrderRequest request
    ) {
        CancelOrderCommand command = new CancelOrderCommand(
            new OrderId(id),
            request.getReason()
        );
        cancelOrderUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    private CreateOrderCommand mapToCommand(CreateOrderRequest request) {
        // HTTP请求 -> 领域命令
        return new CreateOrderCommand(
            new CustomerId(request.getCustomerId()),
            request.getItems(),
            request.getShippingAddress()
        );
    }
}
```

#### GraphQL适配器
```java
@Component
public class OrderGraphQLController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    @MutationMapping
    public OrderDTO createOrder(@Argument CreateOrderInput input) {
        CreateOrderCommand command = mapToCommand(input);
        OrderId orderId = createOrderUseCase.execute(command);
        return getOrderUseCase.execute(orderId);
    }

    @QueryMapping
    public OrderDTO order(@Argument String id) {
        return getOrderUseCase.execute(new OrderId(id));
    }
}
```

#### 消息队列适配器
```java
@Component
public class OrderMessageListener {

    private final CreateOrderUseCase createOrderUseCase;

    @RabbitListener(queues = "order.create.queue")
    public void handleCreateOrderMessage(CreateOrderMessage message) {
        CreateOrderCommand command = mapToCommand(message);
        createOrderUseCase.execute(command);
    }

    private CreateOrderCommand mapToCommand(CreateOrderMessage message) {
        // 消息 -> 领域命令
        return new CreateOrderCommand(
            new CustomerId(message.getCustomerId()),
            message.getItems(),
            message.getShippingAddress()
        );
    }
}
```

### 4. 次适配器 (Secondary Adapter)

#### JPA仓储适配器
```java
// JPA实体(基础设施层的数据模型)
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    private String id;
    private String customerId;
    private BigDecimal totalAmount;
    private String currency;
    private String status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItemJpaEntity> items;

    // Getters, Setters
}

// Spring Data JPA接口
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {
    List<OrderJpaEntity> findByCustomerId(String customerId);
}

// 适配器实现:将JPA转换为领域模型
@Component
public class OrderRepositoryJpaAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    public OrderRepositoryJpaAdapter(
        OrderJpaRepository jpaRepository,
        OrderMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order findById(OrderId id) {
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = mapper.toEntity(order);
        jpaRepository.save(entity);
    }

    @Override
    public void delete(OrderId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.getValue())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}

// 领域模型与数据模型映射器
@Component
public class OrderMapper {

    public Order toDomain(OrderJpaEntity entity) {
        Order order = new Order(
            new OrderId(entity.getId()),
            new CustomerId(entity.getCustomerId())
        );

        // 重建订单状态
        for (OrderItemJpaEntity itemEntity : entity.getItems()) {
            // 添加订单项...
        }

        return order;
    }

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId().getValue());
        entity.setCustomerId(order.getCustomerId().getValue());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setStatus(order.getStatus().name());

        // 映射订单项...

        return entity;
    }
}
```

#### 第三方支付适配器
```java
@Component
public class AlipayAdapter implements PaymentGateway {

    private final AlipayClient alipayClient;

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // 将领域请求转换为第三方API请求
        AlipayTradePayRequest alipayRequest = new AlipayTradePayRequest();
        alipayRequest.setOutTradeNo(request.getOrderId());
        alipayRequest.setTotalAmount(request.getAmount().toString());
        alipayRequest.setSubject(request.getDescription());

        // 调用第三方API
        try {
            AlipayTradePayResponse response = alipayClient.execute(alipayRequest);

            // 将第三方响应转换为领域模型
            return new PaymentResult(
                response.isSuccess(),
                response.getTradeNo(),
                mapPaymentStatus(response.getTradeStatus())
            );
        } catch (AlipayApiException e) {
            throw new PaymentProcessingException(e);
        }
    }

    private PaymentStatus mapPaymentStatus(String alipayStatus) {
        return switch (alipayStatus) {
            case "TRADE_SUCCESS" -> PaymentStatus.SUCCESS;
            case "WAIT_BUYER_PAY" -> PaymentStatus.PENDING;
            default -> PaymentStatus.FAILED;
        };
    }
}

// 可以轻松切换到其他支付方式
@Component
public class WeChatPayAdapter implements PaymentGateway {
    // 实现微信支付...
}
```

#### Redis缓存适配器
```java
@Component
public class OrderCacheAdapter {

    private final RedisTemplate<String, Order> redisTemplate;

    public Optional<Order> get(OrderId id) {
        String key = "order:" + id.getValue();
        Order order = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(order);
    }

    public void put(Order order) {
        String key = "order:" + order.getId().getValue();
        redisTemplate.opsForValue().set(key, order, 1, TimeUnit.HOURS);
    }

    public void evict(OrderId id) {
        String key = "order:" + id.getValue();
        redisTemplate.delete(key);
    }
}
```

## 四、测试策略

### 1. 领域模型测试
```java
// 纯领域逻辑测试,不需要任何外部依赖
class OrderTest {

    @Test
    void shouldCalculateTotalAmountWhenAddingItems() {
        // Given
        Order order = new Order(OrderId.generate(), new CustomerId("123"));
        Product product1 = new Product(
            ProductId.generate(),
            "商品1",
            new Money(100, Currency.CNY)
        );
        Product product2 = new Product(
            ProductId.generate(),
            "商品2",
            new Money(200, Currency.CNY)
        );

        // When
        order.addItem(product1, 2);  // 200
        order.addItem(product2, 1);  // 200

        // Then
        assertEquals(new Money(400, Currency.CNY), order.getTotalAmount());
    }

    @Test
    void shouldThrowExceptionWhenSubmittingEmptyOrder() {
        // Given
        Order order = new Order(OrderId.generate(), new CustomerId("123"));

        // When & Then
        assertThrows(OrderValidationException.class, () -> order.submit());
    }
}
```

### 2. 用例测试
```java
class CreateOrderServiceTest {

    private CreateOrderService service;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        // 使用Mock对象,不需要真实的数据库
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryService = mock(InventoryService.class);
        OrderFactory orderFactory = new OrderFactory();

        service = new CreateOrderService(
            orderRepository,
            productRepository,
            inventoryService,
            orderFactory
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand(
            new CustomerId("user123"),
            List.of(new OrderItemDTO(new ProductId("prod1"), 2)),
            new Address("北京市...")
        );

        Product product = new Product(
            new ProductId("prod1"),
            "商品1",
            new Money(100, Currency.CNY)
        );

        when(productRepository.findById(any())).thenReturn(product);
        when(inventoryService.checkStock(any(), anyInt())).thenReturn(true);

        // When
        OrderId orderId = service.execute(command);

        // Then
        assertNotNull(orderId);
        verify(orderRepository).save(any(Order.class));
        verify(inventoryService).reserveStock(any(), eq(2));
    }

    @Test
    void shouldThrowExceptionWhenStockInsufficient() {
        // Given
        CreateOrderCommand command = createCommand();

        when(productRepository.findById(any())).thenReturn(createProduct());
        when(inventoryService.checkStock(any(), anyInt())).thenReturn(false);

        // When & Then
        assertThrows(
            InsufficientStockException.class,
            () -> service.execute(command)
        );

        verify(orderRepository, never()).save(any());
    }
}
```

### 3. 集成测试
```java
@SpringBootTest
@Transactional
class OrderIntegrationTest {

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCreateOrderEndToEnd() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand(...);

        // When
        OrderId orderId = createOrderUseCase.execute(command);

        // Then
        Order order = orderRepository.findById(orderId);
        assertNotNull(order);
        assertEquals(OrderStatus.SUBMITTED, order.getStatus());
    }
}
```

## 五、面试高频问题

### 1. 什么是六边形架构?它的核心思想是什么?

**答案要点**:
- 六边形架构是将应用核心与外部依赖隔离的架构模式
- 核心思想:依赖倒置,外部依赖核心
- 通过端口(接口)和适配器(实现)实现解耦
- 核心业务不依赖具体技术实现

### 2. 六边形架构和分层架构有什么区别?

**分层架构**:
- 上层依赖下层
- 业务层依赖数据访问层
- 技术细节向上渗透

**六边形架构**:
- 核心不依赖外部
- 依赖倒置原则
- 外部适配核心

### 3. 什么是端口(Port)?什么是适配器(Adapter)?

**端口(Port)**:
- 定义核心与外部的交互接口
- 主端口:应用的用例接口
- 次端口:外部依赖的接口

**适配器(Adapter)**:
- 实现端口接口
- 主适配器:驱动应用(REST, UI)
- 次适配器:被应用驱动(Database, MQ)

### 4. 六边形架构如何实现依赖倒置?

**传统方式**:
```java
// 业务层依赖数据层
class OrderService {
    private OrderDAO orderDAO;  // 依赖具体实现
}
```

**六边形架构**:
```java
// 核心定义接口
public interface OrderRepository {
    Order findById(OrderId id);
}

// 核心依赖抽象
class OrderService {
    private OrderRepository repository;  // 依赖抽象
}

// 基础设施实现接口
class OrderRepositoryJpaAdapter implements OrderRepository {
    // 具体实现
}
```

### 5. 六边形架构的优势是什么?

**优势**:
1. **高可测试性**: 可以Mock外部依赖
2. **技术无关**: 易于更换技术栈
3. **业务清晰**: 核心业务不被技术污染
4. **多渠道支持**: 可以添加不同的适配器
5. **延迟决策**: 技术选型可以推迟

### 6. 如何组织六边形架构的代码结构?

**推荐结构**:
```
src/
├── domain/              # 领域层(核心)
│   ├── model/          # 领域模型
│   ├── service/        # 领域服务
│   └── exception/      # 领域异常
├── application/         # 应用层
│   ├── port/
│   │   ├── in/        # 输入端口(用例接口)
│   │   └── out/       # 输出端口(仓储接口)
│   └── service/        # 用例实现
└── infrastructure/      # 基础设施层
    ├── adapter/
    │   ├── in/         # 主适配器(REST, MQ...)
    │   └── out/        # 次适配器(JPA, Redis...)
    └── config/         # 配置
```

### 7. 六边形架构适合什么场景?

**适合**:
- 复杂业务逻辑
- 需要高可测试性
- 多渠道接入
- 技术栈不稳定

**不适合**:
- 简单CRUD
- 快速原型开发
- 团队不熟悉DDD

### 8. 如何在六边形架构中处理事务?

**方案**:
```java
// 应用服务层处理事务
@Service
@Transactional  // 事务边界
public class CreateOrderService implements CreateOrderUseCase {

    @Override
    public OrderId execute(CreateOrderCommand command) {
        // 1. 创建订单
        Order order = orderFactory.create(command);

        // 2. 保存(同一事务)
        orderRepository.save(order);

        // 3. 预留库存(同一事务)
        inventoryService.reserve(order.getItems());

        return order.getId();
    }
}
```

### 9. 六边形架构和洋葱架构有什么区别?

**相似点**:
- 都强调依赖倒置
- 核心业务独立
- 分层思想

**不同点**:
- 六边形:强调端口和适配器
- 洋葱:强调同心圆分层
- 表达方式不同,本质类似

### 10. 如何在现有项目中引入六边形架构?

**渐进式重构**:
1. **识别核心领域**: 提取业务逻辑
2. **定义端口**: 创建接口
3. **实现适配器**: 封装技术实现
4. **逐步迁移**: 一个模块一个模块重构

**示例**:
```java
// 第一步:提取仓储接口
public interface OrderRepository {
    Order findById(String id);
}

// 第二步:原有DAO实现接口
class OrderDAOAdapter implements OrderRepository {
    private OrderDAO dao;  // 保留原有DAO

    public Order findById(String id) {
        OrderPO po = dao.selectById(id);
        return mapToDomain(po);
    }
}

// 第三步:业务层依赖接口
class OrderService {
    private OrderRepository repository;  // 改为依赖接口
}
```

## 六、实战案例

### 完整示例

```java
// 1. 领域模型(domain/model)
public class Order {
    private OrderId id;
    private Money totalAmount;
    private OrderStatus status;
    // 业务逻辑...
}

// 2. 输入端口(application/port/in)
public interface CreateOrderUseCase {
    OrderId execute(CreateOrderCommand command);
}

// 3. 输出端口(application/port/out)
public interface OrderRepository {
    void save(Order order);
}

// 4. 应用服务(application/service)
@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderRepository orderRepository;

    public OrderId execute(CreateOrderCommand command) {
        Order order = Order.create(command);
        orderRepository.save(order);
        return order.getId();
    }
}

// 5. REST适配器(infrastructure/adapter/in/web)
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping("/orders")
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = map(request);
        OrderId id = createOrderUseCase.execute(command);
        return new OrderResponse(id);
    }
}

// 6. JPA适配器(infrastructure/adapter/out/persistence)
@Component
public class OrderJpaAdapter implements OrderRepository {
    private final OrderJpaRepository jpaRepository;

    public void save(Order order) {
        OrderJpaEntity entity = map(order);
        jpaRepository.save(entity);
    }
}
```

## 七、总结

六边形架构的关键要点:
1. **依赖倒置**: 核心定义接口,外部实现
2. **端口适配器**: 通过接口隔离外部依赖
3. **高可测试性**: 可以Mock外部依赖
4. **技术无关**: 核心业务不依赖技术
5. **清晰边界**: 明确的层次职责
