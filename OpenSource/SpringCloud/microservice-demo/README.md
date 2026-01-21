# Spring Cloud 微服务完整案例 - 电商系统

## 一、项目架构

```
┌──────────────────────────────────────────────────────────────────┐
│                          Client (Web/App)                        │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                          │
│  - 路由转发                                                       │
│  - 统一认证                                                       │
│  - 限流熔断                                                       │
│  - 日志追踪                                                       │
└──────┬─────────┬─────────┬─────────┬─────────┬──────────────────┘
       │         │         │         │         │
       ▼         ▼         ▼         ▼         ▼
┌──────────┐ ┌───────┐ ┌────────┐ ┌────────┐ ┌─────────┐
│  User    │ │ Order │ │Product │ │Payment │ │Inventory│
│ Service  │ │Service│ │Service │ │Service │ │ Service │
│  (8001)  │ │(8002) │ │ (8003) │ │ (8004) │ │  (8005) │
└────┬─────┘ └───┬───┘ └────┬───┘ └────┬───┘ └────┬────┘
     │           │          │          │          │
     └───────────┴──────────┴──────────┴──────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │  Eureka Server (8761)  │
              │  服务注册与发现         │
              └────────────────────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │ Config Server (8888)   │
              │  集中配置管理           │
              └────────────────────────┘
```

## 二、服务列表

| 服务名称 | 端口 | 功能描述 | 技术栈 |
|---------|------|---------|--------|
| eureka-server | 8761 | 服务注册中心 | Eureka Server |
| config-server | 8888 | 配置中心 | Config Server + Git |
| api-gateway | 8080 | API网关 | Gateway + Loadbalancer |
| user-service | 8001 | 用户服务 | OpenFeign + MyBatis |
| order-service | 8002 | 订单服务 | OpenFeign + MyBatis |
| product-service | 8003 | 商品服务 | MyBatis |
| payment-service | 8004 | 支付服务 | OpenFeign |
| inventory-service | 8005 | 库存服务 | MyBatis |

## 三、核心业务流程

### 3.1 用户下单流程

```
1. 用户请求 → API Gateway (认证、限流)
2. Gateway → Order Service (创建订单)
3. Order Service → User Service (验证用户)
4. Order Service → Product Service (查询商品信息)
5. Order Service → Inventory Service (扣减库存)
6. Order Service → Payment Service (创建支付)
7. 返回订单信息
```

### 3.2 调用链示例

```java
// API Gateway
POST /api/order/create
  ↓
// Order Service
@PostMapping("/create")
public OrderDTO createOrder(@RequestBody CreateOrderRequest request) {
    // 1. 调用User Service
    UserDTO user = userServiceClient.getUserById(request.getUserId());

    // 2. 调用Product Service
    ProductDTO product = productServiceClient.getProduct(request.getProductId());

    // 3. 调用Inventory Service
    inventoryServiceClient.decreaseStock(request.getProductId(), request.getQuantity());

    // 4. 调用Payment Service
    PaymentDTO payment = paymentServiceClient.createPayment(...);

    // 5. 保存订单
    return saveOrder(user, product, payment);
}
```

## 四、技术要点

### 4.1 服务注册与发现

所有微服务启动时自动注册到Eureka Server：

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.cloud.client.ip-address}:${server.port}
```

### 4.2 配置中心

从Config Server获取配置：

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      profile: ${spring.profiles.active}
      label: master
```

### 4.3 服务调用 (OpenFeign)

声明式HTTP客户端：

```java
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable Long id);
}
```

### 4.4 熔断降级

使用Resilience4j实现熔断：

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        sliding-window-size: 10
```

### 4.5 网关路由

统一入口，动态路由：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=2
```

## 五、项目结构

```
microservice-demo/
├── eureka-server/           # 注册中心
├── config-server/           # 配置中心
├── api-gateway/             # API网关
├── common/                  # 公共模块
│   ├── common-core/         # 核心工具类
│   └── common-api/          # 公共API定义
└── services/                # 业务服务
    ├── user-service/        # 用户服务
    ├── order-service/       # 订单服务
    ├── product-service/     # 商品服务
    ├── payment-service/     # 支付服务
    └── inventory-service/   # 库存服务
```

## 六、快速启动

### 6.1 启动顺序

1. **启动Eureka Server** (8761)
2. **启动Config Server** (8888)
3. **启动业务服务** (8001-8005)
4. **启动API Gateway** (8080)

### 6.2 访问地址

- Eureka Dashboard: http://localhost:8761
- Config Server: http://localhost:8888/{app}/{profile}
- API Gateway: http://localhost:8080

### 6.3 测试接口

```bash
# 查询用户
curl http://localhost:8080/api/user/1

# 创建订单
curl -X POST http://localhost:8080/api/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 100,
    "quantity": 2
  }'

# 查询订单
curl http://localhost:8080/api/order/1
```

## 七、核心代码示例

### 7.1 订单服务 (Order Service)

```java
@Service
public class OrderService {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. 验证用户
        UserDTO user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询商品
        ProductDTO product = productServiceClient.getProduct(request.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 3. 检查库存并扣减
        boolean stockDecreased = inventoryServiceClient.decreaseStock(
            request.getProductId(),
            request.getQuantity()
        );
        if (!stockDecreased) {
            throw new BusinessException("库存不足");
        }

        try {
            // 4. 创建支付单
            BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));
            PaymentDTO payment = paymentServiceClient.createPayment(
                user.getId(),
                totalAmount
            );

            // 5. 保存订单
            Order order = new Order();
            order.setUserId(user.getId());
            order.setProductId(product.getId());
            order.setQuantity(request.getQuantity());
            order.setTotalAmount(totalAmount);
            order.setPaymentId(payment.getId());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            orderRepository.save(order);

            return OrderConverter.toDTO(order);

        } catch (Exception e) {
            // 回滚库存
            inventoryServiceClient.increaseStock(
                request.getProductId(),
                request.getQuantity()
            );
            throw e;
        }
    }
}
```

### 7.2 网关认证过滤器

```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (WHITE_LIST.contains(path)) {
            return chain.filter(exchange);
        }

        // 验证Token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isEmpty(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // 解析Token
            Claims claims = JwtUtil.parseToken(token);

            // 添加用户信息到Header
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Name", claims.get("username", String.class))
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100; // 优先级最高
    }
}
```

## 八、监控与运维

### 8.1 健康检查

所有服务集成Spring Boot Actuator：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 8.2 分布式追踪

集成Sleuth实现调用链追踪：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

### 8.3 日志聚合

统一日志格式，包含TraceId：

```
2024-01-20 10:30:15.123 [order-service,abc123,def456] INFO  - 创建订单成功
```

## 九、部署架构

### 9.1 Docker部署

每个服务打包为Docker镜像：

```dockerfile
FROM openjdk:11-jre-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 9.2 Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: order-service:1.0.0
        ports:
        - containerPort: 8002
```

## 十、最佳实践

1. **服务拆分原则**：按业务领域拆分，保持服务单一职责
2. **接口设计**：RESTful风格，版本管理
3. **异常处理**：统一异常处理，友好错误提示
4. **事务管理**：分布式事务使用Seata
5. **缓存策略**：多级缓存，缓存预热
6. **限流降级**：关键接口必须限流和降级
7. **监控告警**：实时监控，及时告警
8. **灰度发布**：新功能先灰度，再全量

## 十一、常见问题

### Q1: 服务调用超时如何处理？
A: 设置合理的超时时间，实现重试机制和降级策略。

### Q2: 如何保证分布式事务？
A: 使用Seata AT模式或TCC模式。

### Q3: 如何防止缓存雪崩？
A: 设置随机过期时间，使用熔断器。

### Q4: 服务数量多，配置管理困难？
A: 使用Config Server统一管理，动态刷新。
