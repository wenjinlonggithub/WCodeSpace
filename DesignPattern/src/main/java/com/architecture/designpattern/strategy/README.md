# 策略模式 (Strategy Pattern) - 完整实现

## 📚 项目概述

这是一个完整的策略模式实现，包含核心框架、经典示例、企业级应用、面试题精讲等全套内容。

## 🗂️ 文件结构

```
strategy/
├── StrategyPattern.java           # 🏗️ 核心策略框架
├── ClassicExamples.java          # 📖 经典应用示例  
├── EnterpriseExamples.java       # 🏢 企业级应用示例
├── StrategyDemo.java             # 🎬 完整演示程序
├── StrategyInterviewAndAnalysis.java # 🎯 面试题和源码分析
├── demo/                         # 📁 简单演示示例
│   ├── Strategy.java
│   ├── Addition.java
│   ├── Subtraction.java
│   ├── Calculate.java
│   ├── Calculator.java
│   └── Client.java
└── README.md                     # 📋 本说明文件
```

## 🚀 快速开始

### 编译所有文件
```bash
# 在项目根目录下执行
javac -cp "src/main/java" src/main/java/com/architecture/designpattern/strategy/*.java
```

### 运行完整演示
```bash
# 运行主演示程序
java -cp "src/main/java" com.architecture.designpattern.strategy.StrategyDemo
```

### 运行特定模块
```bash
# 运行经典示例
java -cp "src/main/java" com.architecture.designpattern.strategy.ClassicExamples

# 运行企业级示例  
java -cp "src/main/java" com.architecture.designpattern.strategy.EnterpriseExamples

# 运行面试题演示
java -cp "src/main/java" com.architecture.designpattern.strategy.StrategyInterviewAndAnalysis
```

## 📋 功能模块详解

### 1. 核心策略框架 (`StrategyPattern.java`)

提供策略模式的基础设施：

- **Strategy<T,R>** - 基础策略接口（函数式接口）
- **AbstractStrategy<T,R>** - 抽象策略基类（提供前后置处理）
- **StrategyContext<T,R>** - 策略上下文（持有和执行策略）
- **StrategyRegistry<T,R>** - 策略注册表（管理多个策略）
- **StrategyChain<T>** - 策略链执行器（链式策略组合）
- **ConditionalStrategySelector<T,R>** - 条件策略选择器（基于条件自动选择）

```java
// 基本使用示例
Strategy<String, String> upperCase = input -> input.toUpperCase();
StrategyContext<String, String> context = new StrategyContext<>(upperCase);
String result = context.execute("hello world");
```

### 2. 经典应用示例 (`ClassicExamples.java`)

包含两个经典的策略模式应用：

#### 🧮 计算器策略
- 加法策略 (AdditionStrategy)
- 减法策略 (SubtractionStrategy)  
- 乘法策略 (MultiplicationStrategy)
- 除法策略 (DivisionStrategy)

```java
Calculator calc = new Calculator();
double result = calc.calculate("ADD", 10.5, 5.2);
```

#### 💰 支付策略
- 支付宝策略 (AlipayStrategy)
- 微信支付策略 (WechatPayStrategy)
- 银行卡策略 (BankCardStrategy)

支持：
- 金额验证
- 货币类型检查
- 手续费计算
- 自动策略选择（基于金额）

```java
PaymentProcessor processor = new PaymentProcessor();
PaymentRequest request = new PaymentRequest("ORDER001", new BigDecimal("299.99"), "CNY");
PaymentResult result = processor.pay("ALIPAY", request);
```

### 3. 企业级应用示例 (`EnterpriseExamples.java`)

包含三个企业级策略应用：

#### 📨 消息推送策略
- 邮件推送策略 (EmailPushStrategy)
- 短信推送策略 (SmsStrategy)  
- APP推送策略 (AppPushStrategy)

特性：
- 消息优先级支持
- 自动渠道选择
- 异步处理支持
- 重试机制

```java
MessagePushManager manager = new MessagePushManager();
Message msg = new Message("MSG001", "系统通知", "您有新消息", "user@example.com", MessagePriority.HIGH);
PushResult result = manager.autoPush(msg);
```

#### 🔐 用户认证策略
- 用户名密码认证 (UsernamePasswordAuthStrategy)
- OAuth认证 (OAuthAuthStrategy)

特性：
- 失败次数限制
- 令牌有效期管理
- 双因子认证支持

```java
AuthenticationManager authManager = new AuthenticationManager();
AuthRequest request = new AuthRequest("admin", "password123", "192.168.1.100");
AuthResult result = authManager.authenticate("USERNAME_PASSWORD", request);
```

#### 🗄️ 缓存策略
- 内存缓存策略 (MemoryCacheStrategy)
- Redis缓存策略 (RedisCacheStrategy)

特性：
- TTL支持
- 过期自动清理
- 容量限制
- 操作结果反馈

```java
CacheManager cacheManager = new CacheManager();
CacheRequest request = new CacheRequest("user:1001", "用户数据", 300);
CacheResult result = cacheManager.put("MEMORY", request);
```

### 4. 完整演示程序 (`StrategyDemo.java`)

提供完整的演示和总结：
- 基础概念演示
- 所有模块的完整演示
- 策略模式优势总结
- 适用场景说明

### 5. 面试题和源码分析 (`StrategyInterviewAndAnalysis.java`)

#### 🎯 面试题精讲
1. **策略模式基本概念** - 定义、组成、适用场景
2. **策略模式vs状态模式** - 区别对比和代码示例
3. **策略模式的优缺点** - 详细分析和代码对比
4. **策略模式的实际应用** - 实际项目场景举例
5. **策略模式的改进和优化** - 函数式接口、枚举策略等

#### 🔍 源码分析
1. **JDK中的策略模式** - Comparator、ThreadPoolExecutor等
2. **开源框架中的策略模式** - Spring、MyBatis、Netty等
3. **最佳实践** - 设计原则、性能优化、扩展性考虑

## 🎯 核心特性

### ✨ 设计亮点
- **类型安全**：全面使用泛型确保类型安全
- **函数式支持**：支持Lambda表达式和函数式编程
- **链式调用**：支持策略链式组合执行
- **条件选择**：支持基于条件的自动策略选择
- **注册管理**：提供策略注册表统一管理
- **扩展友好**：易于扩展新的策略实现

### 🛡️ 企业级特性
- **异常处理**：完善的异常处理机制
- **日志记录**：详细的执行日志
- **性能优化**：策略缓存和重用
- **监控支持**：执行结果和性能监控
- **配置驱动**：支持外部配置和动态调整

### 🔧 代码质量
- **无重复类名**：严格避免类名冲突
- **清晰命名**：使用描述性的类名和方法名
- **完整文档**：详细的JavaDoc文档
- **最佳实践**：遵循Java编程最佳实践

## 📖 使用指南

### 基础使用
```java
// 1. 定义策略
Strategy<String, Integer> lengthStrategy = String::length;
Strategy<String, Integer> wordCountStrategy = s -> s.split(" ").length;

// 2. 使用策略上下文
StrategyContext<String, Integer> context = new StrategyContext<>(lengthStrategy);
int result = context.execute("Hello World");

// 3. 动态切换策略
context.setStrategy(wordCountStrategy);
int wordCount = context.execute("Hello World");
```

### 高级使用
```java
// 1. 策略注册表
StrategyRegistry<String, String> registry = new StrategyRegistry<>();
registry.register("UPPER", String::toUpperCase);
registry.register("LOWER", String::toLowerCase);

String result = registry.get("UPPER").execute("hello");

// 2. 条件策略选择
ConditionalStrategySelector<String, String> selector = new ConditionalStrategySelector<>();
selector.when(s -> s.length() > 5, String::toUpperCase)
        .otherwise(String::toLowerCase);

String result = selector.execute("Hello World");

// 3. 策略链
StrategyChain<String> chain = new StrategyChain<>();
chain.addStrategy(String::trim)
     .addStrategy(String::toLowerCase)
     .addStrategy(s -> s.replace(" ", "_"));

String result = chain.execute("  Hello World  ");
```

## 🎓 学习建议

### 初学者
1. 先理解策略模式的基本概念
2. 运行 `StrategyDemo` 查看完整演示
3. 学习 `ClassicExamples` 中的经典应用
4. 阅读面试题部分加深理解

### 进阶学习
1. 研究企业级示例的实现细节
2. 分析源码中的最佳实践
3. 尝试扩展新的策略实现
4. 结合实际项目应用策略模式

### 面试准备
1. 熟练掌握策略模式的定义和组成
2. 理解策略模式vs状态模式的区别
3. 掌握策略模式的优缺点
4. 能举出实际应用场景的例子
5. 了解策略模式的优化方法

## 🤝 贡献指南

欢迎贡献新的策略示例和改进建议！

## 📝 更新日志

- **v2.0.0** - 完全重写，提供完整的策略模式实现
  - 添加核心策略框架
  - 提供经典和企业级示例
  - 增加面试题和源码分析
  - 支持函数式编程和高级特性

---

🎉 **策略模式学习愉快！**