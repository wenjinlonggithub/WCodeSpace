# DDD高级实战案例

## 案例一：电商平台订单系统重构

### 背景
某电商平台订单系统存在以下问题：
- 业务逻辑分散在多个Service类中
- 数据一致性难以保证
- 代码可读性差，维护困难
- 性能瓶颈频发

### 重构方案

#### 1. 领域建模

```java
// 订单聚合根
public class Order extends AggregateRoot {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> orderItems;
    private OrderStatus status;
    private Address shippingAddress;
    private Money totalAmount;
    private LocalDateTime orderTime;
    
    // 业务不变量：订单总金额必须等于所有订单项金额之和
    private void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
    
    // 业务行为：添加订单项
    public void addOrderItem(Product product, int quantity, Money unitPrice) {
        validateOrderStatus();
        validateProduct(product, quantity);
        
        OrderItem item = new OrderItem(product.getId(), quantity, unitPrice);
        this.orderItems.add(item);
        calculateTotalAmount();
        
        // 发布领域事件
        addDomainEvent(new OrderItemAddedEvent(this.id, product.getId(), quantity));
    }
    
    // 业务行为：确认订单
    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new OrderException("订单不能为空");
        }
        
        if (status != OrderStatus.PENDING) {
            throw new OrderException("只能确认待处理订单");
        }
        
        this.status = OrderStatus.CONFIRMED;
        addDomainEvent(new OrderConfirmedEvent(this.id, this.customerId, this.totalAmount));
    }
    
    private void validateOrderStatus() {
        if (status != OrderStatus.PENDING) {
            throw new OrderException("订单状态不允许修改");
        }
    }
    
    private void validateProduct(Product product, int quantity) {
        if (!product.isAvailable()) {
            throw new OrderException("商品不可用: " + product.getName());
        }
        
        if (quantity <= 0) {
            throw new OrderException("商品数量必须大于0");
        }
    }
}

// 订单项实体
public class OrderItem {
    private OrderItemId id;
    private ProductId productId;
    private String productName;
    private Money unitPrice;
    private int quantity;
    private Money subtotal;
    
    public OrderItem(ProductId productId, int quantity, Money unitPrice) {
        this.id = OrderItemId.generate();
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(quantity);
    }
    
    public Money getSubtotal() {
        return subtotal;
    }
}
```

#### 2. 应用服务重构

```java
@Service
@Transactional
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public OrderDTO createOrder(CreateOrderCommand command) {
        // 1. 验证命令
        validateCreateOrderCommand(command);
        
        // 2. 加载聚合
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.getCustomerId()));
        
        // 3. 创建订单聚合
        Order order = new Order(
            OrderId.generate(),
            customer.getId(),
            new Address(command.getShippingAddress())
        );
        
        // 4. 添加订单项
        for (OrderItemRequest itemRequest : command.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            
            order.addOrderItem(product, itemRequest.getQuantity(), product.getPrice());
        }
        
        // 5. 保存聚合
        orderRepository.save(order);
        
        // 6. 发布领域事件
        eventPublisher.publishEvents(order);
        
        return OrderDTO.from(order);
    }
    
    public void confirmOrder(ConfirmOrderCommand command) {
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        
        order.confirm();
        orderRepository.save(order);
        eventPublisher.publishEvents(order);
    }
}
```

#### 3. 事件驱动架构

```java
// 领域事件
public class OrderConfirmedEvent extends DomainEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
    private final List<OrderItemData> items;
    
    public OrderConfirmedEvent(OrderId orderId, CustomerId customerId, 
                              Money totalAmount, List<OrderItemData> items) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.items = items;
    }
}

// 事件处理器
@Component
public class OrderEventHandler {
    
    @EventListener
    @Async
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // 1. 扣减库存
        inventoryService.decreaseStock(event.getItems());
        
        // 2. 发送确认邮件
        emailService.sendOrderConfirmation(event.getCustomerId(), event.getOrderId());
        
        // 3. 更新客户积分
        loyaltyService.addPoints(event.getCustomerId(), event.getTotalAmount());
        
        // 4. 更新报表数据
        reportingService.updateOrderStatistics(event);
    }
    
    @EventListener
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        // 库存预留成功后的处理
        paymentService.processPayment(event.getOrderId(), event.getAmount());
    }
}
```

### 重构效果

1. **业务逻辑集中**：订单相关的业务规则都封装在Order聚合中
2. **数据一致性**：通过聚合边界保证数据一致性
3. **可扩展性**：通过事件驱动实现松耦合
4. **可测试性**：领域对象易于单元测试

## 案例二：金融系统账户管理

### 业务场景
设计一个银行账户系统，支持转账、存款、取款等操作，需要保证资金安全和数据一致性。

### 领域建模

```java
// 账户聚合根
public class Account extends AggregateRoot {
    private AccountId id;
    private CustomerId customerId;
    private AccountNumber accountNumber;
    private Money balance;
    private AccountStatus status;
    private List<Transaction> transactions;
    
    // 业务不变量：余额不能为负数
    public void withdraw(Money amount, String description) {
        validateWithdrawal(amount);
        
        Money newBalance = balance.subtract(amount);
        if (newBalance.isNegative()) {
            throw new InsufficientFundsException("余额不足");
        }
        
        this.balance = newBalance;
        Transaction transaction = new Transaction(
            TransactionId.generate(),
            TransactionType.WITHDRAWAL,
            amount,
            description,
            LocalDateTime.now()
        );
        
        this.transactions.add(transaction);
        addDomainEvent(new MoneyWithdrawnEvent(this.id, amount, newBalance));
    }
    
    public void deposit(Money amount, String description) {
        validateDeposit(amount);
        
        this.balance = balance.add(amount);
        Transaction transaction = new Transaction(
            TransactionId.generate(),
            TransactionType.DEPOSIT,
            amount,
            description,
            LocalDateTime.now()
        );
        
        this.transactions.add(transaction);
        addDomainEvent(new MoneyDepositedEvent(this.id, amount, balance));
    }
    
    private void validateWithdrawal(Money amount) {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountException("账户未激活");
        }
        
        if (amount.isNegativeOrZero()) {
            throw new AccountException("取款金额必须大于0");
        }
    }
    
    private void validateDeposit(Money amount) {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountException("账户未激活");
        }
        
        if (amount.isNegativeOrZero()) {
            throw new AccountException("存款金额必须大于0");
        }
    }
}

// 转账领域服务
@Service
public class TransferDomainService {
    
    public void transfer(Account fromAccount, Account toAccount, 
                        Money amount, String description) {
        // 验证转账条件
        validateTransfer(fromAccount, toAccount, amount);
        
        // 执行转账
        fromAccount.withdraw(amount, "转账给 " + toAccount.getAccountNumber());
        toAccount.deposit(amount, "来自 " + fromAccount.getAccountNumber() + " 的转账");
        
        // 发布转账事件
        TransferCompletedEvent event = new TransferCompletedEvent(
            fromAccount.getId(),
            toAccount.getId(),
            amount,
            description
        );
        
        DomainEventPublisher.publish(event);
    }
    
    private void validateTransfer(Account fromAccount, Account toAccount, Money amount) {
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new TransferException("不能向自己转账");
        }
        
        if (amount.isNegativeOrZero()) {
            throw new TransferException("转账金额必须大于0");
        }
        
        if (!fromAccount.canWithdraw(amount)) {
            throw new TransferException("余额不足");
        }
    }
}
```

### 应用服务

```java
@Service
@Transactional
public class AccountApplicationService {
    
    private final AccountRepository accountRepository;
    private final TransferDomainService transferService;
    
    public void transfer(TransferCommand command) {
        Account fromAccount = accountRepository.findById(command.getFromAccountId())
            .orElseThrow(() -> new AccountNotFoundException(command.getFromAccountId()));
        
        Account toAccount = accountRepository.findById(command.getToAccountId())
            .orElseThrow(() -> new AccountNotFoundException(command.getToAccountId()));
        
        Money amount = new Money(command.getAmount(), command.getCurrency());
        
        // 使用领域服务执行转账
        transferService.transfer(fromAccount, toAccount, amount, command.getDescription());
        
        // 保存聚合
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
```

## 案例三：库存管理系统

### 业务场景
电商平台的库存管理，需要处理商品入库、出库、预留、释放等操作。

### 领域建模

```java
// 库存聚合根
public class Inventory extends AggregateRoot {
    private InventoryId id;
    private ProductId productId;
    private int totalStock;
    private int availableStock;
    private int reservedStock;
    private List<StockMovement> movements;
    
    // 业务不变量：总库存 = 可用库存 + 预留库存
    private void validateInvariant() {
        if (totalStock != availableStock + reservedStock) {
            throw new InventoryException("库存数据不一致");
        }
    }
    
    // 预留库存
    public void reserve(int quantity, String reason) {
        if (quantity <= 0) {
            throw new InventoryException("预留数量必须大于0");
        }
        
        if (availableStock < quantity) {
            throw new InsufficientStockException("可用库存不足");
        }
        
        this.availableStock -= quantity;
        this.reservedStock += quantity;
        
        StockMovement movement = new StockMovement(
            MovementType.RESERVE,
            quantity,
            reason,
            LocalDateTime.now()
        );
        this.movements.add(movement);
        
        validateInvariant();
        addDomainEvent(new StockReservedEvent(this.productId, quantity));
    }
    
    // 释放预留库存
    public void releaseReservation(int quantity, String reason) {
        if (quantity <= 0) {
            throw new InventoryException("释放数量必须大于0");
        }
        
        if (reservedStock < quantity) {
            throw new InventoryException("预留库存不足");
        }
        
        this.reservedStock -= quantity;
        this.availableStock += quantity;
        
        StockMovement movement = new StockMovement(
            MovementType.RELEASE,
            quantity,
            reason,
            LocalDateTime.now()
        );
        this.movements.add(movement);
        
        validateInvariant();
        addDomainEvent(new StockReleasedEvent(this.productId, quantity));
    }
    
    // 确认出库
    public void confirmOutbound(int quantity, String reason) {
        if (quantity <= 0) {
            throw new InventoryException("出库数量必须大于0");
        }
        
        if (reservedStock < quantity) {
            throw new InventoryException("预留库存不足");
        }
        
        this.reservedStock -= quantity;
        this.totalStock -= quantity;
        
        StockMovement movement = new StockMovement(
            MovementType.OUTBOUND,
            quantity,
            reason,
            LocalDateTime.now()
        );
        this.movements.add(movement);
        
        validateInvariant();
        addDomainEvent(new StockOutboundEvent(this.productId, quantity));
    }
}

// 库存管理领域服务
@Service
public class InventoryManagementService {
    
    public boolean checkAvailability(ProductId productId, int requiredQuantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new InventoryNotFoundException(productId));
        
        return inventory.getAvailableStock() >= requiredQuantity;
    }
    
    public void reserveForOrder(OrderId orderId, List<OrderItem> items) {
        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException(item.getProductId()));
            
            inventory.reserve(item.getQuantity(), "订单预留: " + orderId);
            inventoryRepository.save(inventory);
        }
    }
}
```

## 案例四：会员积分系统

### 业务场景
设计一个会员积分系统，支持积分获取、消费、过期、等级升级等功能。

### 领域建模

```java
// 会员聚合根
public class Member extends AggregateRoot {
    private MemberId id;
    private String name;
    private Email email;
    private MemberLevel level;
    private int totalPoints;
    private int availablePoints;
    private List<PointTransaction> pointTransactions;
    
    // 获得积分
    public void earnPoints(int points, String source, LocalDateTime earnedAt) {
        if (points <= 0) {
            throw new MemberException("积分必须大于0");
        }
        
        this.totalPoints += points;
        this.availablePoints += points;
        
        PointTransaction transaction = new PointTransaction(
            TransactionId.generate(),
            PointTransactionType.EARN,
            points,
            source,
            earnedAt
        );
        
        this.pointTransactions.add(transaction);
        
        // 检查是否需要升级
        checkLevelUpgrade();
        
        addDomainEvent(new PointsEarnedEvent(this.id, points, source));
    }
    
    // 消费积分
    public void spendPoints(int points, String purpose) {
        if (points <= 0) {
            throw new MemberException("消费积分必须大于0");
        }
        
        if (availablePoints < points) {
            throw new InsufficientPointsException("可用积分不足");
        }
        
        this.availablePoints -= points;
        
        PointTransaction transaction = new PointTransaction(
            TransactionId.generate(),
            PointTransactionType.SPEND,
            points,
            purpose,
            LocalDateTime.now()
        );
        
        this.pointTransactions.add(transaction);
        addDomainEvent(new PointsSpentEvent(this.id, points, purpose));
    }
    
    // 积分过期
    public void expirePoints(int points, LocalDateTime expiredAt) {
        if (points <= 0) {
            throw new MemberException("过期积分必须大于0");
        }
        
        if (availablePoints < points) {
            throw new MemberException("可用积分不足");
        }
        
        this.availablePoints -= points;
        
        PointTransaction transaction = new PointTransaction(
            TransactionId.generate(),
            PointTransactionType.EXPIRE,
            points,
            "积分过期",
            expiredAt
        );
        
        this.pointTransactions.add(transaction);
        addDomainEvent(new PointsExpiredEvent(this.id, points));
    }
    
    private void checkLevelUpgrade() {
        MemberLevel newLevel = MemberLevel.calculateLevel(totalPoints);
        if (newLevel.isHigherThan(this.level)) {
            MemberLevel oldLevel = this.level;
            this.level = newLevel;
            addDomainEvent(new MemberLevelUpgradedEvent(this.id, oldLevel, newLevel));
        }
    }
}

// 会员等级值对象
public class MemberLevel {
    private final String name;
    private final int requiredPoints;
    private final double discountRate;
    
    public static final MemberLevel BRONZE = new MemberLevel("青铜", 0, 0.95);
    public static final MemberLevel SILVER = new MemberLevel("白银", 1000, 0.90);
    public static final MemberLevel GOLD = new MemberLevel("黄金", 5000, 0.85);
    public static final MemberLevel PLATINUM = new MemberLevel("铂金", 10000, 0.80);
    
    public static MemberLevel calculateLevel(int totalPoints) {
        if (totalPoints >= PLATINUM.requiredPoints) return PLATINUM;
        if (totalPoints >= GOLD.requiredPoints) return GOLD;
        if (totalPoints >= SILVER.requiredPoints) return SILVER;
        return BRONZE;
    }
    
    public boolean isHigherThan(MemberLevel other) {
        return this.requiredPoints > other.requiredPoints;
    }
}
```

## 总结

这些案例展示了DDD在不同业务场景中的应用：

1. **订单系统**：展示了聚合设计、事件驱动架构
2. **账户管理**：展示了领域服务、业务不变量
3. **库存管理**：展示了复杂业务规则、数据一致性
4. **积分系统**：展示了值对象设计、状态变更

每个案例都体现了DDD的核心思想：
- 以业务为中心的建模
- 明确的聚合边界
- 丰富的领域模型
- 事件驱动的架构