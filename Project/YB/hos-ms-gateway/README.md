# Hospital Microservice Gateway (hos-ms-gateway)

## 项目简介

基于 Spring Cloud Gateway 构建的微服务网关，为医院管理系统提供统一的流量入口、认证鉴权、路由转发和日志记录等功能。

## 技术栈

- **Spring Boot 3.5.5**
- **Spring Cloud Gateway 4.1.0**
- **Nacos 2.4.0** - 服务发现与配置中心
- **Spring Cloud OpenFeign** - 声明式服务调用
- **Spring WebFlux 6.1.2** - 响应式编程
- **Lombok** - 简化代码
- **Hutool 5.4.1** - 工具类库
- **Fastjson 1.2.83** - JSON 处理

## 核心功能

### 1. 动态路由配置

通过 Nacos 配置中心实现路由的动态加载和热更新，无需重启网关即可修改路由规则。

**实现原理**：
- `DynamicRouteConfig` 监听 Nacos 配置变化
- 启动时从 Nacos 加载路由配置（data-id: routes）
- 配置变更时自动触发路由刷新
- 支持 YAML 格式的路由定义

**关键代码**：`src/main/java/com/hospital/gateway/config/DynamicRouteConfig.java:37-75`

### 2. Token 认证与鉴权

基于 Bearer Token 的认证机制，集成 Passport 服务进行 Token 校验。

**实现细节**：
- 从请求头 `Authorization` 中提取 Bearer Token
- 通过 Feign 调用 Passport 服务验证 Token 有效性
- 使用 Hutool 的 TimedCache 缓存 Token 验证结果（有效期 30 分钟）
- 验证成功后将 `appId` 注入到请求头传递给下游服务
- 生成或传递 Trace ID（`X-HOS-TRACE-ID`）用于链路追踪

**性能优化**：
- Token 缓存机制避免重复调用认证服务
- 缓存命中直接放行，降低认证延迟

**关键代码**：`src/main/java/com/hospital/gateway/filter/TokenFilter.java:35-113`

### 3. 请求/响应日志记录

全链路日志记录，支持请求参数、响应数据、执行时间的完整追踪。

**实现原理**：
- 基于 `GlobalFilter` 实现请求和响应的拦截
- 区分处理 Form 和 JSON 两种 Content-Type
- 使用 `ServerHttpRequestDecorator` 装饰器模式解决 Request Body 只能读取一次的问题
- 通过 `ServerHttpResponseDecorator` 拦截响应数据
- 记录执行时间、路由目标、AppId 等关键信息

**日志字段**：
```java
LogObj {
    schema,          // 协议类型（http/https）
    path,            // 请求路径
    targetServer,    // 目标服务 ID
    appId,           // 应用 ID
    params,          // 请求参数
    requestTime,     // 请求时间
    responseTime,    // 响应时间
    executeTime,     // 执行耗时（毫秒）
    responseData     // 响应数据（仅 JSON 格式）
}
```

**关键代码**：`src/main/java/com/hospital/gateway/filter/log/LogFilter.java:51-196`

### 4. 跨域配置（CORS）

支持跨域请求，解决前后端分离架构下的跨域问题。

**配置特性**：
- 动态设置 `Access-Control-Allow-Origin`（从请求头获取）
- 支持携带凭证（Credentials）
- 允许自定义请求头和方法
- 自动处理 OPTIONS 预检请求

**关键代码**：`src/main/java/com/hospital/gateway/config/CorsConfig.java:22-49`

### 5. 全局异常处理

统一的异常处理机制，返回标准化的 JSON 错误响应。

**异常类型处理**：
- `NotFoundException` - 返回 404
- `BusinessException` - 返回业务异常信息
- 其他异常 - 返回 500 服务器异常

**响应格式**：
```json
{
  "code": 500,
  "msg": "错误信息"
}
```

**关键代码**：`src/main/java/com/hospital/gateway/exception/JsonExceptionHandler.java:26-102`

### 6. 服务发现与负载均衡

集成 Nacos 实现服务自动发现和客户端负载均衡。

**配置**：
- 启用服务发现定位器（`spring.cloud.gateway.discovery.locator.enabled=true`）
- 服务 ID 自动转换为小写（`lower-case-service-id=true`）
- Spring Cloud LoadBalancer 提供负载均衡支持

## 架构设计

### 请求处理流程

```
客户端请求
    ↓
[CORS Filter] - 跨域处理
    ↓
[TokenFilter (Order: -99)] - Token 认证 + Trace ID 注入
    ↓
[LogFilter (Order: -10)] - 请求日志记录
    ↓
[Gateway Handler] - 路由匹配与转发
    ↓
[下游微服务]
    ↓
[LogFilter] - 响应日志记录
    ↓
[Global Exception Handler] - 异常处理
    ↓
返回客户端
```

### Filter 执行顺序

1. **TokenFilter** (Order: -99) - 最先执行，确保所有请求都已认证
2. **LogFilter** (Order: -10) - 在认证后记录日志
3. 其他 Spring Cloud Gateway 内置 Filter

## 项目结构

```
src/main/java/com/hospital/gateway/
├── config/
│   ├── ConfigBean.java              # 配置 Bean
│   ├── CorsConfig.java              # CORS 跨域配置
│   └── DynamicRouteConfig.java      # 动态路由配置
├── controller/
│   └── ServerController.java        # 控制器
├── dto/
│   └── CheckTokenResp.java          # Token 校验响应 DTO
├── enums/
│   └── ResultCodeEnum.java          # 结果码枚举
├── exception/
│   ├── BusinessException.java       # 业务异常
│   ├── ErrorHandlerConfiguration.java # 异常处理配置
│   └── JsonExceptionHandler.java    # JSON 异常处理器
├── feign/
│   └── PassportFeign.java           # Passport 服务 Feign 客户端
├── filter/
│   ├── TokenFilter.java             # Token 认证过滤器
│   └── log/
│       ├── LogFilter.java           # 日志过滤器
│       └── LogObj.java              # 日志对象
├── response/
│   ├── BaseResponse.java            # 基础响应
│   ├── ResponseCode.java            # 响应码
│   └── SimpleResponse.java          # 简单响应
└── GatewayApp.java                  # 应用启动类
```

## 配置说明

### application.yml

```yaml
server:
  port: 9990                          # 网关端口

spring:
  application:
    name: hos-gateway                 # 服务名称
  profiles:
    active: test                      # 激活环境配置
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true               # 启用服务发现
          lower-case-service-id: true # 服务 ID 小写

gateway:
  routes:
    data-id: routes                   # Nacos 路由配置 data-id
```

### 环境变量

需要在对应的 `application-{profile}.yml` 中配置：

- `spring.cloud.nacos.discovery.server-addr` - Nacos 服务地址
- `spring.cloud.nacos.config.server-addr` - Nacos 配置中心地址
- `pass.passport-domain` - Passport 认证服务域名

## 部署说明

### 本地开发

```bash
# 1. 确保 Nacos 服务已启动
# 2. 在 Nacos 配置中心创建路由配置（data-id: routes）
# 3. 启动应用
mvn spring-boot:run
```

### Docker 部署

项目提供了多个 Dockerfile：
- `Dockerfile` - 标准部署
- `Dockerfile-new` - 新版本部署
- `Dockerfile-prod` - 生产环境部署

```bash
docker build -t hos-ms-gateway:latest .
docker run -d -p 9990:9990 hos-ms-gateway:latest
```

### Kubernetes 部署

使用 `deploy.yaml` 进行 K8s 部署：

```bash
kubectl apply -f deploy.yaml
```

## 性能优化

1. **Token 缓存**：30 分钟的 TimedCache，减少认证服务调用
2. **响应式编程**：基于 WebFlux 的非阻塞 I/O，提升并发性能
3. **动态路由**：避免硬编码，支持热更新
4. **连接池管理**：（可选）通过 `reactor.netty.pool.leasingStrategy=lifo` 优化连接池策略

## 监控与日志

- **链路追踪**：通过 `X-HOS-TRACE-ID` 实现全链路追踪
- **MDC 支持**：在日志中自动注入 Trace ID
- **请求日志**：记录完整的请求/响应数据和执行时间
- **异常日志**：全局异常捕获和日志记录

## 扩展开发

### 添加自定义 Filter

```java
@Component
public class CustomFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 自定义逻辑
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0; // 设置执行优先级
    }
}
```

### 配置动态路由（Nacos）

在 Nacos 配置中心创建配置（data-id: routes）：

```yaml
routes:
  - id: service-route
    uri: lb://service-name          # lb:// 表示负载均衡
    predicates:
      - Path=/api/service/**
    filters:
      - StripPrefix=2
```

## 常见问题

**Q: Token 缓存失效时间如何调整？**
A: 修改 `TokenFilter.java:41` 中的缓存时间参数（单位：毫秒）

**Q: 如何排除某些路径不进行 Token 认证？**
A: 在 `TokenFilter` 中添加路径白名单判断逻辑

**Q: 日志过多影响性能怎么办？**
A: 可以在 `LogFilter` 中添加路径过滤，或调整日志级别，仅记录异常请求

**Q: 如何动态更新路由而不重启？**
A: 在 Nacos 配置中心修改 routes 配置即可，网关会自动监听并更新

## 依赖服务

- **Nacos** - 服务发现与配置中心
- **Passport 服务** - Token 认证服务

## 作者

@lvzeqiang

## License

Copyright © 2022 com.architecture
