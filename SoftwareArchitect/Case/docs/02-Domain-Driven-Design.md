# 领域驱动设计 (Domain-Driven Design, DDD)

## 一、架构概述

领域驱动设计(DDD)是一种软件开发方法论,强调通过建立领域模型来解决复杂业务问题。DDD的核心思想是将业务领域的知识和逻辑封装在领域模型中,使软件设计与业务需求保持一致。

## 二、解决的业务问题

### 1. 传统开发的痛点
- **业务与技术脱节**: 开发人员不理解业务,业务人员不懂技术
- **代码难以维护**: 业务逻辑散落在各处,难以理解和修改
- **需求频繁变更**: 业务变化时代码改动巨大
- **团队沟通成本高**: 缺乏统一的业务语言
- **系统边界不清**: 模块职责混乱,耦合严重

### 2. DDD的解决方案
- **统一语言(Ubiquitous Language)**: 业务和技术团队使用相同的术语
- **领域模型**: 将业务逻辑封装在领域对象中
- **限界上下文(Bounded Context)**: 明确划分子域边界
- **分层架构**: 将领域逻辑与基础设施解耦
- **聚合(Aggregate)**: 保证业务规则的一致性

### 3. 适用场景
- **复杂业务领域**: 金融、保险、医疗、电商等业务逻辑复杂的系统
- **长期维护项目**: 需要长期演进的企业级应用
- **团队协作**: 多团队协作开发的大型项目
- **业务频繁变化**: 需要快速响应业务变化的系统

## 三、核心概念

### 1. 统一语言 (Ubiquitous Language)

业务专家和开发团队使用相同的术语来描述业务概念。

**示例**:
```
传统命名:
- UserData, OrderInfo, ProductRecord

DDD命名(统一语言):
- Customer(客户), Order(订单), Product(商品)
- PlaceOrder(下单), CancelOrder(取消订单)
- ShippingAddress(收货地址), PaymentMethod(支付方式)
```

### 2. 限界上下文 (Bounded Context)

将大型系统划分为多个限界上下文,每个上下文有自己的领域模型。

```
电商系统限界上下文:
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   订单上下文     │  │   商品上下文     │  │   用户上下文     │
│                 │  │                 │  │                 │
│ Order           │  │ Product         │  │ Customer        │
│ OrderItem       │  │ Category        │  │ Address         │
│ Payment         │  │ Inventory       │  │ PaymentMethod   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                    │                    │
         └────────────────────┴────────────────────┘
                         上下文映射
```

### 3. 分层架构

```
┌─────────────────────────────────────────┐
│      用户接口层 (User Interface)         │  Controller, DTO
├─────────────────────────────────────────┤
│      应用层 (Application)                │  Application Service
├─────────────────────────────────────────┤
│      领域层 (Domain)                     │  Entity, Value Object
│                                         │  Aggregate, Domain Service
├─────────────────────────────────────────┤
│      基础设施层 (Infrastructure)         │  Repository Impl, Message
└─────────────────────────────────────────┘
```

### 4. 领域模型核心要素

#### 实体 (Entity)
具有唯一标识的对象,通过ID区分。

```java
@Entity
public class Order {
    @Id
    private OrderId id;  // 唯一标识
    private CustomerId customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private Instant createdAt;

    // 领域行为
    public void addItem(Product product, int quantity) {
        if (this.status != OrderStatus.DRAFT) {
            throw new IllegalStateException("只能在草稿状态添加商品");
        }
        OrderItem item = new OrderItem(product, quantity);
        this.items.add(item);
        this.calculateTotalAmount();
    }

    public void submit() {
        if (this.items.isEmpty()) {
            throw new IllegalStateException("订单不能为空");
        }
        this.status = OrderStatus.SUBMITTED;
        this.createdAt = Instant.now();
        // 发布领域事件
        DomainEventPublisher.publish(new OrderSubmittedEvent(this.id));
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}
```

#### 值对象 (Value Object)
没有唯一标识,通过属性值判断相等性。

```java
@Embeddable
public class Money {
    private BigDecimal amount;
    private Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("货币类型不一致");
        }
        return new Money(
            this.amount.add(other.amount),
            this.currency
        );
    }

    public Money multiply(int quantity) {
        return new Money(
            this.amount.multiply(BigDecimal.valueOf(quantity)),
            this.currency
        );
    }

    // 值对象是不可变的
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) &&
               currency.equals(money.currency);
    }
}
```

#### 聚合 (Aggregate)
一组相关对象的集合,作为数据修改的单元。

```java
// Order是聚合根
public class Order {
    private OrderId id;  // 聚合根ID
    private CustomerId customerId;
    private List<OrderItem> items;  // 聚合内部实体
    private ShippingAddress shippingAddress;  // 值对象
    private OrderStatus status;

    // 通过聚合根访问内部对象
    public void changeShippingAddress(ShippingAddress newAddress) {
        if (this.status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("已发货订单不能修改地址");
        }
        this.shippingAddress = newAddress;
    }

    // 聚合内部的一致性由聚合根保证
    public void removeItem(ProductId productId) {
        this.items.removeIf(item ->
            item.getProductId().equals(productId)
        );
        this.calculateTotalAmount();

        if (this.items.isEmpty()) {
            this.status = OrderStatus.CANCELLED;
        }
    }
}

// OrderItem是聚合内部实体,不能独立存在
class OrderItem {
    private ProductId productId;
    private String productName;
    private Money unitPrice;
    private int quantity;

    Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }
}
```

#### 领域服务 (Domain Service)
不属于任何实体或值对象的领域逻辑。

```java
@DomainService
public class PricingService {

    /**
     * 计算订单总价(包含优惠)
     * 这个逻辑涉及Order和Coupon两个聚合,所以放在领域服务中
     */
    public Money calculateOrderPrice(Order order, List<Coupon> coupons) {
        Money originalPrice = order.getTotalAmount();
        Money discount = Money.ZERO;

        for (Coupon coupon : coupons) {
            if (coupon.isApplicableTo(order)) {
                discount = discount.add(
                    coupon.calculateDiscount(originalPrice)
                );
            }
        }

        return originalPrice.subtract(discount);
    }
}

@DomainService
public class TransferService {

    /**
     * 转账服务
     * 涉及两个Account聚合的协调,所以使用领域服务
     */
    public void transfer(Account from, Account to, Money amount) {
        from.debit(amount);  // 扣款
        to.credit(amount);   // 入账

        // 发布领域事件
        DomainEventPublisher.publish(
            new MoneyTransferredEvent(from.getId(), to.getId(), amount)
        );
    }
}
```

#### 仓储 (Repository)
负责聚合的持久化和重建。

```java
// 仓储接口定义在领域层
public interface OrderRepository {
    Order findById(OrderId id);
    void save(Order order);
    void delete(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findByStatus(OrderStatus status);
}

// 仓储实现在基础设施层
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    @Autowired
    private JpaOrderRepository jpaRepository;

    @Override
    public Order findById(OrderId id) {
        return jpaRepository.findById(id.getValue())
            .map(this::toDomain)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public void save(Order order) {
        OrderPO po = toDataModel(order);
        jpaRepository.save(po);
    }

    // 领域模型与数据模型转换
    private Order toDomain(OrderPO po) {
        return Order.builder()
            .id(new OrderId(po.getId()))
            .customerId(new CustomerId(po.getCustomerId()))
            .items(po.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList()))
            .build();
    }

    private OrderPO toDataModel(Order order) {
        // 转换逻辑
    }
}
```

#### 领域事件 (Domain Event)
领域中发生的重要业务事件。

```java
public class OrderSubmittedEvent implements DomainEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Instant occurredOn;

    public OrderSubmittedEvent(OrderId orderId, CustomerId customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.occurredOn = Instant.now();
    }
}

// 领域事件发布
public class Order {
    public void submit() {
        this.status = OrderStatus.SUBMITTED;
        // 发布事件
        registerEvent(new OrderSubmittedEvent(this.id, this.customerId));
    }
}

// 事件监听处理
@Component
public class OrderEventHandler {

    @EventListener
    public void handleOrderSubmitted(OrderSubmittedEvent event) {
        // 发送订单确认邮件
        emailService.sendOrderConfirmation(event.getCustomerId());

        // 通知库存系统锁定库存
        inventoryService.reserveStock(event.getOrderId());
    }
}
```

### 5. 应用服务 (Application Service)

应用服务协调领域对象完成用例,但不包含业务逻辑。

```java
@Service
@Transactional
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PricingService pricingService;

    /**
     * 创建订单用例
     */
    public OrderId createOrder(CreateOrderCommand command) {
        // 1. 获取聚合
        Customer customer = customerRepository.findById(
            command.getCustomerId()
        );

        // 2. 创建订单聚合
        Order order = new Order(customer.getId());

        // 3. 添加订单项
        for (OrderItemDTO item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId());
            order.addItem(product, item.getQuantity());
        }

        // 4. 应用优惠券(调用领域服务)
        if (command.getCouponIds() != null) {
            List<Coupon> coupons = command.getCouponIds().stream()
                .map(couponRepository::findById)
                .collect(Collectors.toList());

            Money finalPrice = pricingService.calculateOrderPrice(order, coupons);
            order.applyDiscount(finalPrice);
        }

        // 5. 提交订单
        order.submit();

        // 6. 持久化
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 取消订单用例
     */
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.getOrderId());

        // 业务逻辑在聚合内部
        order.cancel(command.getReason());

        orderRepository.save(order);
    }
}
```

## 四、战术设计模式

### 1. 工厂模式 (Factory)

```java
public class OrderFactory {

    public Order createOrder(Customer customer, CreateOrderRequest request) {
        // 复杂的对象创建逻辑
        Order order = new Order(
            OrderId.generate(),
            customer.getId(),
            request.getShippingAddress()
        );

        // 添加商品
        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId());
            order.addItem(product, item.getQuantity());
        }

        // 设置默认值
        order.setStatus(OrderStatus.DRAFT);
        order.setCreatedAt(Instant.now());

        return order;
    }
}
```

### 2. 规约模式 (Specification)

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
}

// 客户信用评估规约
public class CreditWorthyCustomerSpec implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getCreditScore() >= 600 &&
               customer.hasNoPendingPayments() &&
               customer.getAccountAge().toMonths() >= 6;
    }
}

// 大额订单规约
public class LargeOrderSpec implements Specification<Order> {

    private final Money threshold;

    public LargeOrderSpec(Money threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getTotalAmount().isGreaterThan(threshold);
    }
}

// 使用规约
public class OrderService {

    public void processOrder(Order order, Customer customer) {
        Specification<Customer> creditWorthy = new CreditWorthyCustomerSpec();
        Specification<Order> largeOrder = new LargeOrderSpec(
            new Money(10000, Currency.CNY)
        );

        if (largeOrder.isSatisfiedBy(order)) {
            if (!creditWorthy.isSatisfiedBy(customer)) {
                throw new InsufficientCreditException();
            }
        }

        // 处理订单
    }
}
```

## 五、战略设计

### 1. 上下文映射图 (Context Map)

```
┌─────────────────┐         ┌─────────────────┐
│   订单上下文     │◄───U────┤   商品上下文     │
│                 │    D    │                 │
│  Order          │         │  Product        │
│  Customer       │         │  Inventory      │
└────────┬────────┘         └─────────────────┘
         │                           ▲
         │ ACL                       │ OHS
         │                           │
         ▼                           │
┌─────────────────┐         ┌─────────────────┐
│   支付上下文     │─────────┤   物流上下文     │
│                 │   PS    │                 │
│  Payment        │         │  Shipment       │
│  Transaction    │         │  Tracking       │
└─────────────────┘         └─────────────────┘

关系说明:
U/D: Upstream(上游) / Downstream(下游)
ACL: Anti-Corruption Layer(防腐层)
OHS: Open Host Service(开放主机服务)
PS: Published Language(发布语言)
```

### 2. 防腐层 (Anti-Corruption Layer)

```java
// 订单上下文不直接依赖第三方支付系统
public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}

// 防腐层适配器
@Component
public class AlipayAdapter implements PaymentGateway {

    private final AlipayClient alipayClient;

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // 将领域模型转换为第三方API模型
        AlipayTradePayRequest alipayRequest = new AlipayTradePayRequest();
        alipayRequest.setOutTradeNo(request.getOrderId().toString());
        alipayRequest.setTotalAmount(request.getAmount().toString());
        alipayRequest.setSubject(request.getDescription());

        // 调用第三方API
        AlipayTradePayResponse response = alipayClient.execute(alipayRequest);

        // 将第三方响应转换为领域模型
        return new PaymentResult(
            response.isSuccess(),
            response.getTradeNo(),
            parsePaymentStatus(response.getTradeStatus())
        );
    }

    private PaymentStatus parsePaymentStatus(String alipayStatus) {
        // 转换逻辑,隔离外部变化
        return switch (alipayStatus) {
            case "TRADE_SUCCESS" -> PaymentStatus.SUCCESS;
            case "WAIT_BUYER_PAY" -> PaymentStatus.PENDING;
            default -> PaymentStatus.FAILED;
        };
    }
}
```

## 六、面试高频问题

### 1. 什么是DDD?它解决了什么问题?

**答案要点**:
- DDD是一种软件开发方法论,强调以业务领域为核心进行设计
- 解决业务与技术脱节、代码难维护、需求变更困难等问题
- 通过统一语言、领域模型、限界上下文等概念建立清晰的业务边界
- 将业务逻辑封装在领域对象中,使代码更贴近业务

### 2. 实体和值对象有什么区别?

**答案要点**:
- **实体(Entity)**:
  - 有唯一标识(ID)
  - 通过ID判断相等性
  - 有生命周期
  - 示例: Order, Customer, Product

- **值对象(Value Object)**:
  - 没有唯一标识
  - 通过属性值判断相等性
  - 不可变(Immutable)
  - 示例: Money, Address, DateRange

**代码示例**:
```java
// 实体: 两个订单即使属性相同,只要ID不同就是不同的订单
Order order1 = new Order(OrderId.of(1), ...);
Order order2 = new Order(OrderId.of(2), ...);
order1.equals(order2); // false,因为ID不同

// 值对象: 两个Money对象属性相同就是相等的
Money money1 = new Money(100, Currency.CNY);
Money money2 = new Money(100, Currency.CNY);
money1.equals(money2); // true,因为金额和货币都相同
```

### 3. 什么是聚合?为什么需要聚合?

**答案要点**:
- 聚合是一组相关对象的集合,作为数据修改的单元
- 聚合根是聚合的入口,外部只能通过聚合根访问聚合内部对象
- 目的: 保证业务规则的一致性,控制事务边界

**聚合设计原则**:
1. 一个事务只修改一个聚合
2. 通过ID引用其他聚合
3. 聚合边界内保证强一致性
4. 聚合边界外保证最终一致性

**示例**:
```java
// 订单是聚合根
public class Order {
    private OrderId id;
    private List<OrderItem> items;  // 聚合内部实体

    // 通过聚合根方法访问内部对象
    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(product, quantity);
        this.items.add(item);
        this.calculateTotalAmount();  // 保证一致性
    }
}

// 错误示范:直接修改聚合内部对象
order.getItems().add(new OrderItem(...));  // 绕过了聚合根的业务规则
```

### 4. 领域服务和应用服务有什么区别?

**答案要点**:
- **领域服务(Domain Service)**:
  - 包含领域逻辑
  - 不属于任何实体或值对象
  - 协调多个聚合
  - 无状态
  - 示例: 转账服务、定价服务

- **应用服务(Application Service)**:
  - 不包含业务逻辑
  - 协调领域对象完成用例
  - 处理事务、权限、日志等
  - 调用领域服务和仓储
  - 示例: OrderApplicationService

**代码示例**:
```java
// 领域服务:包含业务逻辑
@DomainService
public class TransferService {
    public void transfer(Account from, Account to, Money amount) {
        from.debit(amount);  // 业务规则
        to.credit(amount);   // 业务规则
    }
}

// 应用服务:协调领域对象
@Service
public class AccountApplicationService {
    public void transfer(TransferCommand cmd) {
        Account from = accountRepo.findById(cmd.getFromId());
        Account to = accountRepo.findById(cmd.getToId());

        transferService.transfer(from, to, cmd.getAmount());  // 调用领域服务

        accountRepo.save(from);
        accountRepo.save(to);
    }
}
```

### 5. 如何划分限界上下文?

**答案要点**:
- 按业务能力划分
- 按组织结构划分(康威定律)
- 按术语含义划分(同一个词在不同上下文有不同含义)
- 按变化频率划分

**示例**:
```
电商系统上下文划分:
- 订单上下文: Order, OrderItem, Payment
- 商品上下文: Product, Category, Inventory
- 用户上下文: Customer, Address, Credential
- 营销上下文: Coupon, Campaign, Discount

注意:"商品"在不同上下文有不同含义:
- 订单上下文: 商品是订单项的一部分,关注价格和数量
- 商品上下文: 商品是独立实体,关注库存和分类
- 营销上下文: 商品是促销对象,关注折扣和活动
```

### 6. DDD中的仓储模式和DAO有什么区别?

**答案要点**:
- **仓储(Repository)**:
  - 面向领域对象
  - 操作聚合根
  - 接口定义在领域层,实现在基础设施层
  - 封装复杂查询逻辑
  - 示例: OrderRepository, CustomerRepository

- **DAO(Data Access Object)**:
  - 面向数据表
  - 操作数据库记录
  - 通常在持久层
  - 简单的CRUD操作
  - 示例: OrderDAO

**代码对比**:
```java
// 仓储:操作领域对象
public interface OrderRepository {
    Order findById(OrderId id);  // 返回领域对象
    List<Order> findPendingOrders();  // 业务查询
    void save(Order order);  // 保存聚合
}

// DAO:操作数据记录
public interface OrderDAO {
    OrderPO selectById(Long id);  // 返回数据对象
    List<OrderPO> selectAll();
    void insert(OrderPO po);
}
```

### 7. 如何处理跨聚合的业务逻辑?

**答案要点**:
1. **领域服务**: 协调多个聚合
2. **领域事件**: 异步解耦,最终一致性
3. **应用服务**: 在应用层协调多个聚合
4. **Saga模式**: 长事务处理

**示例**:
```java
// 方案1: 领域服务
@DomainService
public class OrderFulfillmentService {
    public void fulfill(Order order, Inventory inventory) {
        order.markAsPaid();
        inventory.reserve(order.getItems());
    }
}

// 方案2: 领域事件
public class Order {
    public void submit() {
        this.status = OrderStatus.SUBMITTED;
        registerEvent(new OrderSubmittedEvent(this.id));
    }
}

@EventListener
public void handleOrderSubmitted(OrderSubmittedEvent event) {
    Inventory inventory = inventoryRepo.findByOrderId(event.getOrderId());
    inventory.reserve(...);
}
```

### 8. DDD中的四层架构各层职责是什么?

**答案要点**:
- **用户接口层**:
  - 处理HTTP请求
  - 数据校验
  - DTO转换

- **应用层**:
  - 编排用例流程
  - 事务管理
  - 调用领域服务

- **领域层**:
  - 核心业务逻辑
  - 实体、值对象、聚合
  - 领域服务、领域事件

- **基础设施层**:
  - 数据持久化
  - 消息发送
  - 外部服务调用

**依赖关系**:
```
用户接口层 ──► 应用层 ──► 领域层
                          ▲
                          │
                    基础设施层(依赖倒置)
```

### 9. 贫血模型和充血模型有什么区别?

**答案要点**:
- **贫血模型**:
  - 对象只有getter/setter
  - 业务逻辑在Service层
  - 违反面向对象原则

- **充血模型**:
  - 对象包含业务逻辑
  - 符合DDD思想
  - 更易维护和理解

**代码对比**:
```java
// 贫血模型
public class Order {
    private Long id;
    private BigDecimal amount;
    // 只有getter/setter
}

public class OrderService {
    public void pay(Order order, BigDecimal amount) {
        if (order.getAmount().equals(amount)) {
            order.setStatus("PAID");
        }
    }
}

// 充血模型
public class Order {
    private OrderId id;
    private Money amount;
    private OrderStatus status;

    // 业务逻辑在领域对象内部
    public void pay(Money paymentAmount) {
        if (!this.amount.equals(paymentAmount)) {
            throw new PaymentAmountMismatchException();
        }
        this.status = OrderStatus.PAID;
        registerEvent(new OrderPaidEvent(this.id));
    }
}
```

### 10. DDD适合什么样的项目?什么情况不适合?

**适合的场景**:
- 业务逻辑复杂
- 长期维护的项目
- 需求频繁变化
- 多团队协作
- 企业级应用

**不适合的场景**:
- 简单CRUD应用
- 短期项目或原型
- 团队不熟悉DDD
- 业务逻辑简单

## 七、实战案例:电商订单系统

### 领域模型设计

```java
// 聚合根:订单
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
    private Money originalAmount;
    private Money discountAmount;
    private Money finalAmount;
    private ShippingAddress shippingAddress;
    private OrderStatus status;

    public void addItem(Product product, int quantity) {
        validateCanModify();
        OrderItem item = OrderItem.create(product, quantity);
        this.items.add(item);
        recalculateAmounts();
    }

    public void applyCoupon(Coupon coupon) {
        if (!coupon.isApplicableTo(this)) {
            throw new CouponNotApplicableException();
        }
        this.discountAmount = coupon.calculateDiscount(this.originalAmount);
        recalculateAmounts();
    }

    public void submit() {
        validateCanSubmit();
        this.status = OrderStatus.SUBMITTED;
        registerEvent(new OrderSubmittedEvent(this));
    }

    private void validateCanModify() {
        if (this.status != OrderStatus.DRAFT) {
            throw new OrderCannotBeModifiedException();
        }
    }

    private void validateCanSubmit() {
        if (this.items.isEmpty()) {
            throw new EmptyOrderException();
        }
        if (this.shippingAddress == null) {
            throw new ShippingAddressRequiredException();
        }
    }

    private void recalculateAmounts() {
        this.originalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
        this.finalAmount = originalAmount.subtract(discountAmount);
    }
}
```

## 八、总结

DDD是一种强大的软件开发方法论,特别适合复杂业务领域。关键要点:

1. **以业务为核心**: 代码应该反映业务,而不是技术实现
2. **统一语言**: 团队使用相同的业务术语
3. **限界上下文**: 明确划分子域边界
4. **充血模型**: 将业务逻辑放在领域对象中
5. **聚合设计**: 控制事务边界,保证一致性
6. **分层架构**: 领域层独立,不依赖基础设施
