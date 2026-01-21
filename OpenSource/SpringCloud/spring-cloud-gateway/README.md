# Spring Cloud Gateway 源码学习与实战

## 一、核心架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Request                        │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Gateway Handler Mapping                         │
│  (匹配路由：根据Predicates找到对应的Route)                   │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Gateway Web Handler                             │
│  (执行过滤器链：Global Filter + Route Filter)                │
└───────────────────────────┬─────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
  ┌──────────┐        ┌──────────┐       ┌──────────┐
  │  Filter  │        │  Filter  │       │  Filter  │
  │   -100   │   →    │   -99    │  →    │    0     │
  │  (Auth)  │        │  (Log)   │       │ (Limit)  │
  └──────────┘        └──────────┘       └──────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Proxied Service                            │
│            (lb://user-service, lb://order-service)           │
└─────────────────────────────────────────────────────────────┘
```

## 二、核心概念

### 2.1 Route（路由）
路由是网关的基础元素，包含：
- **ID**: 唯一标识
- **URI**: 目标地址
- **Predicates**: 匹配条件
- **Filters**: 过滤器

### 2.2 Predicate（断言）
匹配HTTP请求的条件，内置断言：
- Path: 路径匹配
- Method: 方法匹配
- Header: 请求头匹配
- Query: 参数匹配
- Cookie: Cookie匹配
- Host: 主机匹配
- RemoteAddr: IP匹配
- Weight: 权重路由
- Before/After/Between: 时间匹配

### 2.3 Filter（过滤器）
对请求和响应进行处理：
- **Global Filter**: 全局过滤器，应用于所有路由
- **Gateway Filter**: 路由过滤器，应用于特定路由

## 三、配置示例

### 3.1 application.yml完整配置
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      # 全局CORS配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods: "*"
            allowed-headers: "*"
            allow-credentials: true

      # 路由配置
      routes:
        # ==================== 用户服务 ====================
        - id: user_service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
            - Method=GET,POST,PUT,DELETE
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@ipKeyResolver}"
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,INTERNAL_SERVER_ERROR
                methods: GET
                backoff:
                  firstBackoff: 10ms
                  maxBackoff: 50ms
                  factor: 2

        # ==================== 订单服务 ====================
        - id: order_service
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
            - Header=Authorization, Bearer.*
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: orderServiceCircuitBreaker
                fallbackUri: forward:/fallback/order

        # ==================== 秒杀活动 ====================
        - id: flash_sale
          uri: lb://sale-service
          predicates:
            - Path=/api/flash-sale/**
            - TimeBased=9,18,true  # 工作日9:00-18:00
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200

      # 默认过滤器（应用于所有路由）
      default-filters:
        - AddResponseHeader=X-Response-Default,Gateway

  # Redis配置（用于限流）
  redis:
    host: localhost
    port: 6379
    password:
    database: 0

# 日志配置
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
```

## 四、业务使用案例

### 案例1：电商API网关

**场景描述**：
统一电商平台所有微服务的访问入口，实现认证、限流、日志、熔断等功能。

**服务架构**：
```
Client
  │
  ▼
Gateway (8080)
  ├─► User Service (用户服务)
  ├─► Order Service (订单服务)
  ├─► Product Service (商品服务)
  ├─► Payment Service (支付服务)
  └─► Inventory Service (库存服务)
```

**路由规则**：
```yaml
routes:
  # 用户登录（白名单，不需要认证）
  - id: auth_login
    uri: lb://user-service
    predicates:
      - Path=/api/auth/login
    filters:
      - StripPrefix=2

  # 用户信息查询（需要认证）
  - id: user_info
    uri: lb://user-service
    predicates:
      - Path=/api/user/**
      - Header=Authorization
    filters:
      - StripPrefix=2

  # 订单创建（需要认证 + 限流）
  - id: create_order
    uri: lb://order-service
    predicates:
      - Path=/api/order/create
      - Method=POST
    filters:
      - StripPrefix=2
      - name: RequestRateLimiter
        args:
          key-resolver: "#{@userKeyResolver}"
          redis-rate-limiter.replenishRate: 1
          redis-rate-limiter.burstCapacity: 1

  # 支付接口（需要认证 + 熔断）
  - id: payment
    uri: lb://payment-service
    predicates:
      - Path=/api/payment/**
    filters:
      - StripPrefix=2
      - name: CircuitBreaker
        args:
          name: paymentCircuitBreaker
          fallbackUri: forward:/fallback/payment
```

### 案例2：灰度发布

**场景**：新版本功能灰度发布，10%流量走新版本。

```yaml
routes:
  # 90%流量 -> 稳定版本
  - id: stable_version
    uri: lb://user-service
    predicates:
      - Path=/api/user/**
      - Weight=user-service, 90
    filters:
      - AddRequestHeader=Version, stable

  # 10%流量 -> 新版本
  - id: canary_version
    uri: lb://user-service-canary
    predicates:
      - Path=/api/user/**
      - Weight=user-service, 10
    filters:
      - AddRequestHeader=Version, canary
```

### 案例3：动态路由

**场景**：从配置中心动态加载路由配置。

```java
@Component
public class DynamicRouteService {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 添加路由
     */
    public void addRoute(RouteDefinition definition) {
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 删除路由
     */
    public void deleteRoute(String routeId) {
        routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
```

## 五、核心源码分析

### 5.1 路由匹配流程
```java
// RoutePredicateHandlerMapping.java
protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
    return this.routeLocator.getRoutes()
        .filter(route -> {
            exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, route.getId());
            return route.getPredicate().test(exchange);
        })
        .next()
        .doOnNext(route -> {
            validateRoute(route, exchange);
        });
}
```

### 5.2 过滤器执行顺序
```java
// FilteringWebHandler.java
public Mono<Void> handle(ServerWebExchange exchange) {
    Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
    List<GatewayFilter> gatewayFilters = route.getFilters();

    // 组合全局过滤器和路由过滤器
    List<GatewayFilter> combined = new ArrayList<>(this.globalFilters);
    combined.addAll(gatewayFilters);

    // 排序并执行
    AnnotationAwareOrderComparator.sort(combined);
    return new DefaultGatewayFilterChain(combined).filter(exchange);
}
```

## 六、性能优化

1. **连接池调优**：
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 30s
```

2. **缓存路由定义**：减少路由匹配开销

3. **使用Netty优化**：调整EventLoop线程数

## 七、最佳实践

1. **统一认证**：在网关层实现认证，下游服务不再验证
2. **限流保护**：防止恶意请求和突发流量
3. **熔断降级**：及时熔断故障服务，返回友好提示
4. **日志监控**：记录所有请求，便于问题排查
5. **动态路由**：支持运行时修改路由配置
