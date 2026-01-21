# Spring Cloud Config 源码学习与实战

## 一、核心概念

### 1.1 Config Server
集中化的配置服务器，从Git/SVN等版本控制系统读取配置文件。

### 1.2 Config Client
微服务客户端，从Config Server获取配置信息。

### 1.3 核心注解
- `@EnableConfigServer`: 启用配置服务器
- `@RefreshScope`: 支持配置动态刷新

## 二、核心流程

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Git Repo   │◄─────│ Config Server│◄─────│Config Client │
│              │      │              │      │              │
│ application- │      │ Port: 8888   │      │ Microservice │
│   dev.yml    │      │              │      │              │
└──────────────┘      └──────────────┘      └──────────────┘
                             │
                             │ HTTP API
                             ▼
                      /{app}/{profile}/{label}
```

## 三、配置文件示例

### 3.1 Config Server (application.yml)
```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          username: ${GIT_USERNAME}
          password: ${GIT_PASSWORD}
          default-label: master
          search-paths: '{application}'
          # 克隆仓库到本地
          clone-on-start: true

  # 对称加密密钥
  encrypt:
    key: mySecretKey
```

### 3.2 Config Client (bootstrap.yml)
```yaml
spring:
  application:
    name: user-service
  profiles:
    active: dev
  cloud:
    config:
      uri: http://localhost:8888
      label: master
      profile: ${spring.profiles.active}
      # 快速失败
      fail-fast: true
      # 重试配置
      retry:
        max-attempts: 6
        initial-interval: 1000

management:
  endpoints:
    web:
      exposure:
        include: refresh
```

## 四、业务使用案例

### 案例1：电商订单服务配置管理

**场景描述**：
订单服务需要在不同环境使用不同的支付网关、数据库连接等配置。

**配置文件** (order-service-prod.yml):
```yaml
database:
  url: jdbc:mysql://prod-cluster:3306/order_db
  username: order_user
  password: '{cipher}AQA8K3sT9k2...' # 加密后的密码

payment:
  gateway:
    url: https://pay.production.com
    merchant-id: PROD_12345
    api-key: '{cipher}BQB9L4uU0l3...'
  timeout: 5000
  retry: 3

order:
  auto-cancel-minutes: 30
  max-items-per-order: 100
```

**Java代码使用**:
```java
@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentConfig {
    private String url;
    private String merchantId;
    private String apiKey;
    // getters and setters
}
```

### 案例2：动态刷新配置

**场景**：运营活动期间，需要动态调整库存预警阈值。

1. 修改Git配置文件：
```yaml
inventory:
  warning-threshold: 50  # 从100改为50
```

2. 触发刷新：
```bash
curl -X POST http://user-service:8080/actuator/refresh
```

3. 配合Spring Cloud Bus实现所有实例同步刷新：
```bash
curl -X POST http://config-server:8888/actuator/bus-refresh
```

## 五、核心源码分析

### 5.1 EnvironmentRepository
负责从存储介质（Git/SVN/文件系统）读取配置。

```java
public interface EnvironmentRepository {
    Environment findOne(String application, String profile, String label);
}
```

### 5.2 配置加载优先级
1. /{application}-{profile}.yml
2. /{application}.yml
3. /application-{profile}.yml
4. /application.yml

### 5.3 加密解密流程
```java
@Endpoint(id = "encrypt")
public class EncryptionController {

    @WriteOperation
    public String encrypt(@RequestBody String data) {
        return "{cipher}" + textEncryptor.encrypt(data);
    }

    @WriteOperation
    public String decrypt(@RequestBody String data) {
        return textEncryptor.decrypt(data);
    }
}
```

## 六、最佳实践

1. **敏感信息加密**：数据库密码、API密钥等必须加密存储
2. **配置分层**：公共配置放在application.yml，特定配置放在{app}-{profile}.yml
3. **快速失败**：开启fail-fast，启动时立即发现配置问题
4. **版本管理**：使用Git的label（分支/标签）管理配置版本
5. **监控告警**：监控Config Server健康状态，配置变更审计

## 七、常见问题

1. **配置不生效**：检查profile、label是否正确
2. **刷新失败**：确认@RefreshScope注解和actuator端点已开启
3. **加密失败**：检查encrypt.key是否配置
