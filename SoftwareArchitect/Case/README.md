# 软件架构设计案例集 (Software Architecture Case Studies)

这是一个全面的软件架构设计学习项目,包含了多种主流架构模式的**理论文档**和**完整代码实现**。

## 📂 项目结构

```
SoftwareArchitect/Case/
├── docs/                           # 架构设计理论文档
│   ├── README.md                   # 文档总览
│   ├── 01-Microservices-Architecture.md
│   ├── 02-Domain-Driven-Design.md
│   ├── 03-CQRS-Architecture.md
│   ├── 04-Event-Driven-Architecture.md
│   ├── 05-Hexagonal-Architecture.md
│   ├── 06-Layered-Architecture.md
│   ├── 07-Service-Mesh-Architecture.md
│   └── 08-Serverless-Architecture.md
│
└── src/main/java/com/architecture/  # 架构代码实现
    ├── layered/                     # 分层架构实现 ✅
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   ├── dto/
    │   └── LayeredArchitectureDemo.java
    │
    ├── hexagonal/                   # 六边形架构实现 ✅
    │   ├── domain/
    │   ├── application/
    │   │   ├── port/
    │   │   │   ├── in/
    │   │   │   └── out/
    │   │   └── service/
    │   ├── adapter/
    │   │   ├── in/
    │   │   └── out/
    │   └── HexagonalArchitectureDemo.java
    │
    ├── eventdriven/                 # 事件驱动架构实现 ✅
    │   ├── EventBus.java
    │   ├── *Event.java
    │   ├── *Service.java
    │   └── EventDrivenArchitectureDemo.java
    │
    ├── ddd/                         # DDD架构实现 (待完善)
    ├── cqrs/                        # CQRS架构实现 (待完善)
    └── microservices/               # 微服务架构实现 (待完善)
```

## 🎯 项目特色

### 1. 理论与实践结合
- ✅ **8篇详细的架构理论文档** - 每篇包含原理、案例、面试题
- ✅ **3个完整的代码实现** - 可直接运行的Demo程序
- ✅ **丰富的代码注释** - 每个类都有详细的职责说明

### 2. 循序渐进的学习路径
```
入门 → 分层架构 (理解职责分离)
      ↓
进阶 → 六边形架构 (理解依赖倒置)
      ↓
高级 → 事件驱动架构 (理解异步解耦)
      ↓
专家 → DDD + CQRS + 微服务 (理解复杂系统设计)
```

### 3. 实战导向
- 所有代码都基于**电商订单系统**这个实际业务场景
- 可以直接运行,看到完整的执行流程和输出
- 包含正常流程和异常处理

## 🚀 快速开始

### 方式1: 阅读理论文档

```bash
# 查看所有架构文档
cd SoftwareArchitect/Case/docs
# 按顺序阅读 01 → 08
```

**推荐阅读顺序**:
1. 06-Layered-Architecture.md (最基础)
2. 05-Hexagonal-Architecture.md (理解依赖倒置)
3. 04-Event-Driven-Architecture.md (理解异步)
4. 02-Domain-Driven-Design.md (理解DDD)
5. 03-CQRS-Architecture.md (理解读写分离)
6. 01-Microservices-Architecture.md (理解分布式)
7. 07-Service-Mesh-Architecture.md (理解服务治理)
8. 08-Serverless-Architecture.md (理解云原生)

### 方式2: 运行代码实现

```bash
# 进入代码目录
cd SoftwareArchitect/Case/src/main/java/com/architecture

# 运行分层架构Demo
java layered.LayeredArchitectureDemo

# 运行六边形架构Demo
java hexagonal.HexagonalArchitectureDemo

# 运行事件驱动架构Demo
java eventdriven.EventDrivenArchitectureDemo
```

### 方式3: 在IDE中学习

1. 用IntelliJ IDEA打开项目
2. 找到 `src/main/java/com/architecture` 目录
3. 选择一个架构的 `*Demo.java` 文件
4. 右键 → Run
5. 查看控制台输出

## 📚 已实现的架构

### 1️⃣ 分层架构 (Layered Architecture) ✅

**理论**: [docs/06-Layered-Architecture.md](docs/06-Layered-Architecture.md)

**代码**: `src/main/java/com/architecture/layered/`

**运行**: `LayeredArchitectureDemo.java`

**特点**:
- 最经典的架构模式
- 职责清晰: Controller → Service → Repository
- 适合中小型Web应用

**演示场景**:
- 创建订单
- 查询订单
- 取消订单
- 支付订单

---

### 2️⃣ 六边形架构 (Hexagonal Architecture) ✅

**理论**: [docs/05-Hexagonal-Architecture.md](docs/05-Hexagonal-Architecture.md)

**代码**: `src/main/java/com/architecture/hexagonal/`

**运行**: `HexagonalArchitectureDemo.java`

**特点**:
- 依赖倒置: 核心不依赖外部
- 端口适配器模式
- 高可测试性

**核心概念**:
- **Domain**: 领域模型,纯业务逻辑
- **Port**: 端口,定义接口
- **Adapter**: 适配器,实现接口
- **Application**: 应用服务,编排用例

**演示场景**:
- 创建订单(展示完整的依赖倒置)
- 错误处理(商品不存在、数量无效)
- 事件发布(领域事件)

---

### 3️⃣ 事件驱动架构 (Event-Driven Architecture) ✅

**理论**: [docs/04-Event-Driven-Architecture.md](docs/04-Event-Driven-Architecture.md)

**代码**: `src/main/java/com/architecture/eventdriven/`

**运行**: `EventDrivenArchitectureDemo.java`

**特点**:
- 松耦合: 通过事件解耦
- 异步处理: 提高响应速度
- 易扩展: 可随时添加监听器

**事件流**:
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

**演示场景**:
- 订阅事件
- 发布订单创建事件
- 发布订单支付事件
- 多个服务响应事件

## 📖 架构对比表

| 架构 | 复杂度 | 可维护性 | 可测试性 | 性能 | 适用场景 |
|-----|-------|---------|---------|-----|---------|
| 分层架构 | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | 中小型Web应用 |
| 六边形架构 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 复杂业务领域 |
| 事件驱动 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 高并发系统 |
| DDD | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | 复杂业务领域 |
| CQRS | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 读写分离 |
| 微服务 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | 大规模系统 |

## 💡 学习建议

### 初学者 (0-2年经验)
1. ✅ 先学**分层架构** - 理解基本的职责分离
2. ✅ 运行代码,理解Controller/Service/Repository的职责
3. ✅ 尝试修改代码,添加新功能

### 进阶者 (2-5年经验)
1. ✅ 学习**六边形架构** - 理解依赖倒置原则
2. ✅ 对比分层架构和六边形架构的区别
3. ✅ 理解端口适配器模式的价值
4. ✅ 学习**事件驱动架构** - 理解异步解耦

### 高级者 (5+年经验)
1. 📖 深入学习**DDD** - 复杂领域建模
2. 📖 学习**CQRS** - 读写分离优化
3. 📖 学习**微服务架构** - 分布式系统设计
4. 📖 学习**服务网格** - 微服务治理

## 🎓 面试准备

每个架构文档都包含**10个左右的高频面试题**,例如:

**分层架构**:
- Service层和DAO层有什么区别?
- 如何避免Service层过于臃肿?
- DTO、Entity、VO有什么区别?

**六边形架构**:
- 什么是端口?什么是适配器?
- 如何实现依赖倒置?
- 和分层架构有什么区别?

**事件驱动架构**:
- 如何保证事件的可靠投递?
- 如何处理事件的幂等性?
- 和消息驱动有什么区别?

## 🔧 技术栈

- **语言**: Java 8+
- **构建**: Maven
- **架构**: 纯Java实现,无Spring依赖(便于理解核心概念)

## 📝 代码特色

所有代码都遵循:
- ✅ **详细注释** - 每个类都有职责说明
- ✅ **清晰命名** - 一看就懂的变量和方法名
- ✅ **完整Demo** - 可以直接运行看效果
- ✅ **输出说明** - 清晰的控制台输出
- ✅ **架构总结** - 每个Demo都有架构特点总结

## 🤝 贡献

欢迎贡献代码和文档!

如果你想添加新的架构实现或改进现有代码,请提交Pull Request。

## 📄 许可

本项目仅供学习和参考使用。

## 🌟 推荐学习顺序

```
第一周: 阅读分层架构文档 + 运行代码
第二周: 阅读六边形架构文档 + 运行代码 + 对比差异
第三周: 阅读事件驱动架构文档 + 运行代码
第四周: 阅读DDD文档 + 理解领域建模
第五周: 阅读CQRS文档 + 理解读写分离
第六周: 阅读微服务架构文档 + 理解分布式系统
```

## 📞 联系方式

如有问题或建议,欢迎提交Issue!
