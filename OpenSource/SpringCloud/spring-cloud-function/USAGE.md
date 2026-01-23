# Spring Cloud Function 使用指南

## 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 2. 测试函数

#### 2.1 测试字符串转大写函数

```bash
curl -X POST http://localhost:8080/uppercase \
  -H "Content-Type: text/plain" \
  -d "hello world"
```

响应：
```
HELLO WORLD
```

#### 2.2 测试字符串反转函数

```bash
curl -X POST http://localhost:8080/reverse \
  -H "Content-Type: text/plain" \
  -d "hello"
```

响应：
```
olleh
```

#### 2.3 测试时间戳生成器（Supplier）

```bash
curl -X GET http://localhost:8080/timestamp
```

响应：
```
1706012345678
```

#### 2.4 测试订单计算函数

```bash
curl -X POST http://localhost:8080/calculateOrder \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER001",
    "price": 100.0,
    "quantity": 2
  }'
```

响应：
```json
{
  "orderId": "ORDER001",
  "subtotal": 200.0,
  "tax": 20.0,
  "total": 220.0
}
```

#### 2.5 测试积分计算函数

```bash
curl -X POST http://localhost:8080/calculatePoints \
  -H "Content-Type: application/json" \
  -d "250.0"
```

响应：
```
270
```

说明：250元消费 = 250基础积分 + 20奖励积分（每满100元奖励10积分）

#### 2.6 测试邮箱验证函数

```bash
# 有效邮箱
curl -X POST http://localhost:8080/validateEmail \
  -H "Content-Type: text/plain" \
  -d "user@example.com"
```

响应：
```json
{
  "valid": true,
  "message": "邮箱格式正确"
}
```

```bash
# 无效邮箱
curl -X POST http://localhost:8080/validateEmail \
  -H "Content-Type: text/plain" \
  -d "invalid-email"
```

响应：
```json
{
  "valid": false,
  "message": "邮箱格式错误"
}
```

## 函数组合

### 配置函数组合

在 `application.yml` 中配置：

```yaml
spring:
  cloud:
    function:
      definition: uppercase|reverse
```

### 测试组合函数

```bash
curl -X POST http://localhost:8080/uppercase,reverse \
  -H "Content-Type: text/plain" \
  -d "hello"
```

响应：
```
OLLEH
```

处理流程：`hello` → `uppercase` → `HELLO` → `reverse` → `OLLEH`

## 编程方式调用

### 使用 FunctionCatalog

```java
@Autowired
private FunctionCatalog functionCatalog;

public void callFunction() {
    // 查找函数
    Function<String, String> uppercase =
        functionCatalog.lookup(Function.class, "uppercase");

    // 调用函数
    String result = uppercase.apply("hello");
    System.out.println(result); // HELLO
}
```

### 直接注入函数 Bean

```java
@Autowired
@Qualifier("uppercase")
private Function<String, String> uppercase;

public void callFunction() {
    String result = uppercase.apply("hello");
    System.out.println(result); // HELLO
}
```

## 消息驱动场景

### 集成 Spring Cloud Stream

#### 1. 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-kafka</artifactId>
</dependency>
```

#### 2. 配置绑定

```yaml
spring:
  cloud:
    stream:
      bindings:
        # Consumer 函数绑定
        orderEventProcessor-in-0:
          destination: order-events
          group: order-service

        # Function 函数绑定（输入）
        orderToNotification-in-0:
          destination: order-events
          group: notification-service

        # Function 函数绑定（输出）
        orderToNotification-out-0:
          destination: notification-events
```

#### 3. 发送测试消息

使用 Kafka 命令行工具：

```bash
# 发送订单事件
kafka-console-producer --broker-list localhost:9092 --topic order-events

# 输入 JSON 消息
{"orderId":"ORDER001","userId":"USER001","amount":1500.0,"timestamp":"2024-01-20T10:00:00"}
```

## 单元测试

### 测试纯函数

```java
@Test
void testUppercaseFunction() {
    Function<String, String> uppercase = String::toUpperCase;
    String result = uppercase.apply("hello");
    assertEquals("HELLO", result);
}
```

### 测试业务函数

```java
@Test
void testCalculateOrder() {
    BusinessFunctions businessFunctions = new BusinessFunctions();
    Function<Order, OrderResult> calculateOrder =
        businessFunctions.calculateOrder();

    Order order = new Order("ORDER001", 100.0, 2);
    OrderResult result = calculateOrder.apply(order);

    assertEquals(220.0, result.getTotal());
}
```

### 测试消息处理

```java
@Test
void testOrderToNotification() {
    MessageProcessingFunctions msgFunctions = new MessageProcessingFunctions();
    Function<Message<OrderEvent>, Message<NotificationEvent>> converter =
        msgFunctions.orderToNotification();

    OrderEvent orderEvent = new OrderEvent("ORDER001", "USER001", 1500.0, LocalDateTime.now());
    Message<OrderEvent> inputMessage = MessageBuilder
        .withPayload(orderEvent)
        .setHeader("eventType", "ORDER_CREATED")
        .build();

    Message<NotificationEvent> outputMessage = converter.apply(inputMessage);

    assertNotNull(outputMessage);
    assertEquals("USER001", outputMessage.getPayload().getUserId());
}
```

## 部署到 Serverless

### AWS Lambda

#### 1. 添加 AWS 适配器依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-function-adapter-aws</artifactId>
</dependency>
```

#### 2. 创建 Handler

```java
public class FunctionHandler
    extends SpringBootRequestHandler<String, String> {
}
```

#### 3. 打包部署

```bash
mvn clean package
aws lambda create-function \
  --function-name uppercase-function \
  --runtime java11 \
  --handler com.architecture.function.FunctionHandler \
  --zip-file fileb://target/spring-cloud-function-1.0-SNAPSHOT.jar
```

### Azure Functions

#### 1. 添加 Azure 适配器依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-function-adapter-azure</artifactId>
</dependency>
```

#### 2. 创建 Handler

```java
public class UppercaseHandler extends AzureSpringBootRequestHandler<String, String> {
    @FunctionName("uppercase")
    public String execute(
        @HttpTrigger(name = "req") String input,
        ExecutionContext context) {
        return handleRequest(input, context);
    }
}
```

## 性能优化

### 1. 使用函数组合减少网络调用

**不推荐**：
```
Client → Service A (uppercase) → Service B (reverse) → Client
```

**推荐**：
```yaml
spring:
  cloud:
    function:
      definition: uppercase|reverse
```

```
Client → Service (uppercase|reverse) → Client
```

### 2. 异步处理

```java
@Bean
public Function<Flux<String>, Flux<String>> asyncUppercase() {
    return flux -> flux
        .map(String::toUpperCase)
        .subscribeOn(Schedulers.parallel());
}
```

### 3. 批量处理

```java
@Bean
public Function<List<Order>, List<OrderResult>> batchCalculateOrders() {
    return orders -> orders.stream()
        .map(this::calculateOrder)
        .collect(Collectors.toList());
}
```

## 常见问题

### Q1: 如何指定函数名称？

默认情况下，函数名称就是 Bean 的方法名。如果需要自定义：

```java
@Bean("myCustomName")
public Function<String, String> uppercase() {
    return String::toUpperCase;
}
```

调用时使用：`POST /myCustomName`

### Q2: 如何处理异常？

```java
@Bean
public Function<String, String> safeUppercase() {
    return value -> {
        try {
            return value.toUpperCase();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    };
}
```

### Q3: 如何传递多个参数？

使用包装对象：

```java
public static class CalculateRequest {
    private double price;
    private int quantity;
    // getters and setters
}

@Bean
public Function<CalculateRequest, Double> calculate() {
    return req -> req.getPrice() * req.getQuantity();
}
```

### Q4: 如何返回不同的 HTTP 状态码？

使用 `Message` 包装返回值：

```java
@Bean
public Function<String, Message<String>> processWithStatus() {
    return value -> {
        if (value == null || value.isEmpty()) {
            return MessageBuilder
                .withPayload("Invalid input")
                .setHeader("http_statusCode", 400)
                .build();
        }
        return MessageBuilder
            .withPayload(value.toUpperCase())
            .setHeader("http_statusCode", 200)
            .build();
    };
}
```

## 最佳实践

### 1. 保持函数纯净

**推荐**：
```java
@Bean
public Function<String, String> uppercase() {
    return String::toUpperCase; // 纯函数，无副作用
}
```

**不推荐**：
```java
@Bean
public Function<String, String> uppercase() {
    return value -> {
        System.out.println(value); // 副作用
        saveToDatabase(value);      // 副作用
        return value.toUpperCase();
    };
}
```

### 2. 使用函数组合而非嵌套调用

**推荐**：
```yaml
spring:
  cloud:
    function:
      definition: trim|lowercase|validate
```

**不推荐**：
```java
@Bean
public Function<String, String> process() {
    return value -> validate(lowercase(trim(value)));
}
```

### 3. 合理使用 Consumer 和 Supplier

- **Consumer**: 用于消息消费、日志记录等无需返回值的场景
- **Supplier**: 用于定时任务、数据生成等无需输入的场景
- **Function**: 用于数据转换、业务计算等需要输入输出的场景

### 4. 错误处理

```java
@Bean
public Function<String, Result> processWithErrorHandling() {
    return value -> {
        try {
            String processed = process(value);
            return Result.success(processed);
        } catch (ValidationException e) {
            return Result.error("VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            return Result.error("INTERNAL_ERROR", "处理失败");
        }
    };
}
```

## 监控和日志

### 启用 Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 查看函数信息

```bash
curl http://localhost:8080/actuator/functions
```

### 添加日志

```java
@Bean
public Function<String, String> uppercaseWithLog() {
    return value -> {
        log.info("Processing: {}", value);
        String result = value.toUpperCase();
        log.info("Result: {}", result);
        return result;
    };
}
```

## 总结

Spring Cloud Function 提供了一种简洁、可移植的方式来构建云原生应用。通过本指南，你应该能够：

1. ✅ 创建和测试基本函数
2. ✅ 使用函数组合构建复杂逻辑
3. ✅ 集成消息驱动架构
4. ✅ 部署到 Serverless 平台
5. ✅ 编写单元测试
6. ✅ 应用最佳实践

更多信息请参考：
- [Spring Cloud Function 官方文档](https://spring.io/projects/spring-cloud-function)
- [README.md](./README.md) - 原理和业务场景详解
