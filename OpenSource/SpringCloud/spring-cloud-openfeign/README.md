# Spring Cloud OpenFeign 源码学习与实战

## 一、核心原理

```
@FeignClient 接口
      │
      ▼
扫描并注册 (FeignClientsRegistrar)
      │
      ▼
创建动态代理 (Feign.Builder)
      │
      ▼
构建HTTP请求 (RequestTemplate)
      │
      ▼
负载均衡 (Ribbon/LoadBalancer)
      │
      ▼
发送HTTP请求 (Client)
      │
      ├─> 成功 -> 解码响应
      └─> 失败 -> 降级处理
```

## 二、核心组件

### 2.1 FeignClient
声明式HTTP客户端接口

```java
@FeignClient(
    name = "user-service",              // 服务名
    url = "",                            // 直接URL（可选）
    fallback = UserServiceFallback.class,// 降级类
    configuration = FeignConfig.class,   // 配置类
    path = "/api"                        // 统一前缀
)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}
```

### 2.2 Encoder & Decoder
请求编码和响应解码

```java
@Bean
public Encoder feignEncoder() {
    return new JacksonEncoder();
}

@Bean
public Decoder feignDecoder() {
    return new JacksonDecoder();
}
```

### 2.3 Interceptor
请求拦截器

```java
@Bean
public RequestInterceptor requestInterceptor() {
    return template -> {
        // 添加认证头
        template.header("Authorization", getToken());
        // 添加追踪ID
        template.header("X-Trace-Id", generateTraceId());
    };
}
```

## 三、配置详解

### 3.1 application.yml配置

```yaml
spring:
  application:
    name: order-service

feign:
  # 启用熔断器
  circuitbreaker:
    enabled: true

  # HTTP客户端配置
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50

  # 压缩配置
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true

  # 日志级别
  client:
    config:
      default:
        # 连接超时
        connectTimeout: 5000
        # 读取超时
        readTimeout: 10000
        # 日志级别
        loggerLevel: FULL

      # 特定服务配置
      user-service:
        connectTimeout: 3000
        readTimeout: 5000
        loggerLevel: BASIC

# Ribbon配置（负载均衡）
user-service:
  ribbon:
    # 最大重试次数
    MaxAutoRetries: 1
    # 切换实例重试次数
    MaxAutoRetriesNextServer: 1
    # 连接超时
    ConnectTimeout: 3000
    # 读取超时
    ReadTimeout: 5000

# 日志
logging:
  level:
    com.architecture.openfeign: DEBUG
```

## 四、业务使用案例

### 案例1：电商订单服务

**场景**：订单服务需要调用多个微服务

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

    /**
     * 创建订单（调用4个服务）
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. 验证用户
        UserDTO user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询商品信息
        ProductDTO product = productServiceClient.getProduct(request.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 3. 检查并扣减库存
        boolean success = inventoryServiceClient.decreaseStock(
            request.getProductId(),
            request.getQuantity()
        );
        if (!success) {
            throw new BusinessException("库存不足");
        }

        // 4. 创建支付单
        PaymentDTO payment = paymentServiceClient.createPayment(
            user.getId(),
            product.getPrice() * request.getQuantity()
        );

        // 5. 保存订单
        return saveOrder(user, product, payment);
    }
}
```

### 案例2：多服务聚合查询

```java
/**
 * 用户中心 - 聚合多个服务数据
 */
@RestController
public class UserCenterController {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private CouponServiceClient couponServiceClient;

    @GetMapping("/user-center/{userId}")
    public UserCenterVO getUserCenter(@PathVariable Long userId) {
        // 并行调用多个服务
        CompletableFuture<UserDTO> userFuture = CompletableFuture.supplyAsync(
            () -> userServiceClient.getUserById(userId)
        );

        CompletableFuture<List<OrderDTO>> ordersFuture = CompletableFuture.supplyAsync(
            () -> orderServiceClient.getUserOrders(userId)
        );

        CompletableFuture<List<CouponDTO>> couponsFuture = CompletableFuture.supplyAsync(
            () -> couponServiceClient.getUserCoupons(userId)
        );

        // 等待所有调用完成
        CompletableFuture.allOf(userFuture, ordersFuture, couponsFuture).join();

        return new UserCenterVO(
            userFuture.join(),
            ordersFuture.join(),
            couponsFuture.join()
        );
    }
}
```

### 案例3：文件上传/下载

```java
@FeignClient(name = "file-service")
public interface FileServiceClient {

    /**
     * 上传文件
     */
    @PostMapping(value = "/upload",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile file);

    /**
     * 下载文件
     */
    @GetMapping(value = "/download/{fileId}",
                produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    Response downloadFile(@PathVariable String fileId);
}
```

## 五、源码分析

### 5.1 动态代理创建

```java
// FeignClientFactoryBean.java
public Object getObject() {
    return getTarget();
}

<T> T getTarget() {
    FeignContext context = applicationContext.getBean(FeignContext.class);
    Feign.Builder builder = feign(context);

    // 构建动态代理
    return builder.target(type, url);
}
```

### 5.2 负载均衡

```java
// LoadBalancerFeignClient.java
public Response execute(Request request, Request.Options options) {
    URI uri = URI.create(request.url());
    String serviceName = uri.getHost();

    // 从Ribbon获取服务实例
    ServiceInstance instance = loadBalancer.choose(serviceName);

    // 替换URL
    String actualUrl = reconstructURI(instance, uri);

    // 发送请求
    return delegate.execute(request, options);
}
```

## 六、性能优化

### 6.1 连接池配置

```yaml
feign:
  httpclient:
    enabled: true
    max-connections: 200           # 最大连接数
    max-connections-per-route: 50  # 每个路由最大连接数
    connection-timeout: 2000       # 连接超时
    connection-timer-repeat: 3000  # 连接池清理间隔
```

### 6.2 请求/响应压缩

```yaml
feign:
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true
      useGzipDecoder: true
```

### 6.3 批量请求优化

```java
// 不推荐：多次调用
for (Long userId : userIds) {
    UserDTO user = userServiceClient.getUserById(userId);
}

// 推荐：批量调用
List<UserDTO> users = userServiceClient.batchGetUsers(userIds);
```

## 七、最佳实践

1. **统一异常处理**：使用ErrorDecoder自定义异常解析
2. **超时配置**：根据业务设置合理的超时时间
3. **降级策略**：为所有Feign Client配置fallback
4. **日志记录**：生产环境使用BASIC级别，开发环境使用FULL
5. **重试机制**：只对幂等接口启用重试
6. **连接池**：使用Apache HttpClient或OkHttp替代默认客户端

## 八、常见问题

1. **超时问题**：区分connectTimeout和readTimeout
2. **重试问题**：POST请求默认不重试
3. **404问题**：检查path路径拼接
4. **序列化问题**：配置正确的Encoder/Decoder
