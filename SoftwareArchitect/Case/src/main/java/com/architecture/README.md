# 软件架构设计代码实现

本目录包含了多种主流软件架构设计模式的完整Java代码实现,每个架构都提供了可运行的Demo程序和详细的代码注释。

## 📚 已实现的架构

### 1. 分层架构 (Layered Architecture)

**路径**: `com.architecture.layered`

**运行**: `LayeredArchitectureDemo.java`

**架构特点**:
- 经典的三层架构: Controller → Service → Repository
- 职责清晰,易于理解和维护
- 适合中小型Web应用

**核心类**:
```
layered/
├── controller/
│   └── OrderController.java      # 表现层
├── service/
│   ├── OrderService.java          # 业务逻辑层
│   ├── InventoryService.java
│   └── NotificationService.java
├── repository/
│   ├── OrderRepository.java       # 持久层
│   └── ProductRepository.java
├── entity/
│   ├── Order.java                 # 实体
│   ├── Product.java
│   └── OrderStatus.java
└── dto/
    ├── OrderDTO.java              # 数据传输对象
    └── CreateOrderRequest.java
```

**运行示例**:
```bash
javac com/architecture/layered/LayeredArchitectureDemo.java
java com.architecture.layered.LayeredArchitectureDemo
```

---

### 2. 六边形架构 (Hexagonal Architecture)

**路径**: `com.architecture.hexagonal`

**运行**: `HexagonalArchitectureDemo.java`

**架构特点**:
- 依赖倒置原则: 核心不依赖外部
- 端口适配器模式: 通过接口隔离外部依赖
- 高可测试性,技术无关

**核心结构**:
```
hexagonal/
├── domain/                         # 领域层(核心)
│   ├── Order.java                  # 聚合根
│   ├── OrderId.java                # 值对象
│   ├── CustomerId.java
│   ├── Money.java
│   ├── Product.java
│   └── *Event.java                 # 领域事件
├── application/                    # 应用层
│   ├── port/
│   │   ├── in/                     # 输入端口
│   │   │   ├── CreateOrderUseCase.java
│   │   │   └── CreateOrderCommand.java
│   │   └── out/                    # 输出端口
│   │       ├── OrderRepository.java
│   │       └── EventPublisher.java
│   └── service/
│       └── CreateOrderService.java # 用例实现
└── adapter/                        # 适配器层
    ├── in/                         # 主适配器(驱动应用)
    │   └── console/
    │       └── ConsoleOrderController.java
    └── out/                        # 次适配器(被应用驱动)
        ├── persistence/
        │   └── InMemoryOrderRepository.java
        └── messaging/
            └── ConsoleEventPublisher.java
```

**关键概念**:
- **端口(Port)**: 定义核心与外部的交互接口
- **适配器(Adapter)**: 实现端口接口,连接外部系统
- **依赖方向**: 外部 → 端口 → 核心

**运行示例**:
```bash
javac com/architecture/hexagonal/HexagonalArchitectureDemo.java
java com.architecture.hexagonal.HexagonalArchitectureDemo
```

---

### 3. 事件驱动架构 (Event-Driven Architecture)

**路径**: `com.architecture.eventdriven`

**运行**: `EventDrivenArchitectureDemo.java`

**架构特点**:
- 松耦合: 通过事件解耦组件
- 异步处理: 提高系统响应速度
- 易扩展: 可随时添加事件监听器

**核心结构**:
```
eventdriven/
├── EventBus.java                   # 事件总线
├── EventHandler.java               # 事件处理器接口
├── OrderCreatedEvent.java          # 订单创建事件
├── OrderPaidEvent.java             # 订单支付事件
├── OrderService.java               # 事件生产者
├── EmailService.java               # 事件消费者
├── InventoryService.java           # 事件消费者
├── ShippingService.java            # 事件消费者
└── EventDrivenArchitectureDemo.java
```

**事件流程**:
```
OrderService (生产者)
    │
    ├─> OrderCreatedEvent
    │     ├─> EmailService: 发送确认邮件
    │     └─> InventoryService: 预留库存
    │
    └─> OrderPaidEvent
          ├─> EmailService: 发送支付成功邮件
          ├─> InventoryService: 扣减库存
          └─> ShippingService: 创建物流单
```

**运行示例**:
```bash
javac com/architecture/eventdriven/EventDrivenArchitectureDemo.java
java com.architecture.eventdriven.EventDrivenArchitectureDemo
```

---

## 🎯 架构对比

| 架构模式 | 复杂度 | 可维护性 | 可测试性 | 适用场景 |
|---------|-------|---------|---------|---------|
| 分层架构 | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | 中小型Web应用 |
| 六边形架构 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 复杂业务领域 |
| 事件驱动架构 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 高并发系统 |

## 📖 快速开始

### 方式1: 直接运行Demo类

每个架构都提供了独立的Demo类,可以直接运行:

```bash
# 分层架构
java com.architecture.layered.LayeredArchitectureDemo

# 六边形架构
java com.architecture.hexagonal.HexagonalArchitectureDemo

# 事件驱动架构
java com.architecture.eventdriven.EventDrivenArchitectureDemo
```

### 方式2: 在IDE中运行

1. 使用IntelliJ IDEA或Eclipse打开项目
2. 找到对应的 `*Demo.java` 文件
3. 右键 → Run

### 方式3: Maven构建

```bash
cd SoftwareArchitect/Case
mvn clean compile
mvn exec:java -Dexec.mainClass="com.architecture.layered.LayeredArchitectureDemo"
```

## 🏗️ 代码结构说明

### 分层架构
```
分层架构强调职责分离,每层只能调用下一层:
Controller → Service → Repository → Database
```

**优点**:
- 简单易懂,易于上手
- 职责清晰
- 适合团队协作

**缺点**:
- Service层容易臃肿
- 容易产生贫血模型

### 六边形架构
```
六边形架构强调依赖倒置,核心业务不依赖外部:
Adapter → Port → Application Core
```

**优点**:
- 业务逻辑纯粹
- 高可测试性
- 易于更换技术实现

**缺点**:
- 概念较抽象
- 代码量较大

### 事件驱动架构
```
事件驱动架构通过事件实现组件解耦:
Producer → EventBus → Subscriber(s)
```

**优点**:
- 松耦合
- 异步处理
- 易于扩展

**缺点**:
- 调试困难
- 最终一致性

## 💡 学习建议

### 初学者路径
1. **先学分层架构** - 理解基本的职责分离
2. **再学六边形架构** - 理解依赖倒置原则
3. **最后学事件驱动** - 理解异步解耦

### 实践建议
1. **运行Demo** - 先运行代码,看输出结果
2. **阅读代码** - 理解每个类的职责
3. **修改代码** - 尝试添加新功能
4. **对比差异** - 对比不同架构的实现方式

## 📝 代码规范

所有代码遵循以下规范:
- ✅ 详细的类和方法注释
- ✅ 清晰的变量命名
- ✅ 完整的Demo演示
- ✅ 输出结果说明
- ✅ 架构特点总结

## 🔗 相关文档

详细的架构设计文档请参考:
- [docs/01-Microservices-Architecture.md](../docs/01-Microservices-Architecture.md)
- [docs/02-Domain-Driven-Design.md](../docs/02-Domain-Driven-Design.md)
- [docs/03-CQRS-Architecture.md](../docs/03-CQRS-Architecture.md)
- [docs/04-Event-Driven-Architecture.md](../docs/04-Event-Driven-Architecture.md)
- [docs/05-Hexagonal-Architecture.md](../docs/05-Hexagonal-Architecture.md)
- [docs/06-Layered-Architecture.md](../docs/06-Layered-Architecture.md)

## 🤝 贡献

欢迎提交Issue和Pull Request来完善这些代码示例!

## 📄 许可

本项目代码仅供学习和参考使用。
