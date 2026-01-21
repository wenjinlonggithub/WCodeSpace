# Spring Cloud Netflix (Eureka) 源码学习与实战

## 一、核心架构

```
┌────────────────────────────────────────────────────────────┐
│                    Eureka Server                           │
│                                                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Registry   │  │  Read Write  │  │  Read Only   │   │
│  │  (注册表)     │─▶│    Cache     │─▶│    Cache     │   │
│  │              │  │  (Guava)     │  │  (定时同步)   │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                            │
└────────────┬───────────────────────────┬──────────────────┘
             │                           │
     注册/心跳/下线              获取服务列表
             │                           │
┌────────────▼────────┐     ┌───────────▼──────────┐
│  Service Provider   │     │  Service Consumer    │
│  (服务提供者)        │     │  (服务消费者)         │
│                     │     │                      │
│  - 注册服务         │     │  - 获取服务列表      │
│  - 30s心跳         │     │  - 30s更新缓存      │
│  - 下线通知         │     │  - 负载均衡选择     │
└─────────────────────┘     └──────────────────────┘
```

## 二、核心机制

### 2.1 服务注册流程

```java
// DiscoveryClient.java
boolean register() {
    try {
        // 1. 构建实例信息
        InstanceInfo instanceInfo = new InstanceInfo.Builder()
            .setInstanceId(instanceId)
            .setAppName(appName)
            .setIPAddr(ipAddress)
            .setPort(port)
            .setStatus(InstanceStatus.UP)
            .build();

        // 2. 发送注册请求
        eurekaTransport.registrationClient.register(instanceInfo);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

### 2.2 心跳续约机制

```java
// HeartbeatThread
class HeartbeatThread implements Runnable {
    public void run() {
        if (renew()) {
            lastSuccessfulHeartbeatTimestamp = System.currentTimeMillis();
        }
    }

    boolean renew() {
        // 发送PUT请求：/apps/{appName}/{instanceId}
        EurekaHttpResponse<InstanceInfo> httpResponse =
            eurekaTransport.registrationClient.sendHeartBeat(
                instanceInfo.getAppName(),
                instanceInfo.getId(),
                instanceInfo,
                null
            );
        return httpResponse.getStatusCode() == 200;
    }
}
```

### 2.3 服务剔除机制

```java
// AbstractInstanceRegistry.java
public void evict() {
    // 1. 计算续约阈值
    int renewalThreshold = (int) (numberOfRenewsPerMinThreshold * 0.85);

    // 2. 自我保护判断
    if (isLeaseExpirationEnabled() &&
        getNumOfRenewsInLastMin() > renewalThreshold) {

        // 3. 查找过期实例
        List<Lease<InstanceInfo>> expiredLeases = new ArrayList<>();
        for (Lease<InstanceInfo> lease : registryMap.values()) {
            if (lease.isExpired()) {
                expiredLeases.add(lease);
            }
        }

        // 4. 剔除过期实例
        for (Lease<InstanceInfo> expiredLease : expiredLeases) {
            cancel(expiredLease.getHolder().getId());
        }
    }
}
```

### 2.4 自我保护机制

**触发条件**：
- 15分钟内心跳失败比例 > 15%
- 网络分区导致大量实例心跳失败

**保护措施**：
- 暂停剔除过期实例
- 保留注册表中的所有实例
- 继续接受新注册和查询请求

## 三、配置详解

### 3.1 Eureka Server配置

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    # Server不需要注册自己
    register-with-eureka: false
    # Server不需要获取注册信息
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/

  server:
    # 关闭自我保护（开发环境）
    enable-self-preservation: false
    # 清理间隔（默认60秒）
    eviction-interval-timer-in-ms: 60000
    # 期望续约数量阈值
    renewal-percent-threshold: 0.85
    # 响应缓存更新间隔
    response-cache-update-interval-ms: 30000
```

### 3.2 Eureka Client配置

```yaml
spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
    # 从Eureka获取注册信息
    fetch-registry: true
    # 注册到Eureka
    register-with-eureka: true
    # 获取服务列表间隔（默认30秒）
    registry-fetch-interval-seconds: 30

  instance:
    # 实例ID（唯一标识）
    instance-id: ${spring.cloud.client.ip-address}:${spring.application.name}:${server.port}
    # 优先使用IP
    prefer-ip-address: true
    # 心跳间隔（默认30秒）
    lease-renewal-interval-in-seconds: 30
    # 租约过期时间（默认90秒）
    lease-expiration-duration-in-seconds: 90
    # 元数据
    metadata-map:
      version: 1.0.0
      region: cn-north-1
      zone: zone-1
      cluster: cluster-a
```

## 四、业务使用案例

### 案例1：电商微服务架构

**架构设计**：
```
Eureka Server (HA部署)
  ├─ eureka-server-1 (8761)
  └─ eureka-server-2 (8762)

微服务集群：
  ├─ user-service (3 instances)
  ├─ order-service (3 instances)
  ├─ product-service (2 instances)
  ├─ payment-service (2 instances)
  └─ inventory-service (2 instances)
```

**服务调用链**：
```
用户下单流程：
1. API Gateway -> Order Service (获取用户信息)
2. Order Service -> User Service (验证用户)
3. Order Service -> Product Service (检查商品)
4. Order Service -> Inventory Service (扣减库存)
5. Order Service -> Payment Service (创建支付)
```

### 案例2：多数据中心部署

```yaml
# 北京机房 (eureka-beijing)
eureka:
  client:
    region: cn-north
    availability-zones:
      cn-north: zone-1, zone-2
    service-url:
      zone-1: http://eureka-bj-1:8761/eureka/
      zone-2: http://eureka-bj-2:8762/eureka/

# 上海机房 (eureka-shanghai)
eureka:
  client:
    region: cn-east
    availability-zones:
      cn-east: zone-1, zone-2
    service-url:
      zone-1: http://eureka-sh-1:8761/eureka/
      zone-2: http://eureka-sh-2:8762/eureka/
```

### 案例3：灰度发布

```java
@Component
public class VersionBasedLoadBalancer {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 根据版本号选择服务实例
     */
    public ServiceInstance chooseInstance(String serviceId, String version) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        // 过滤指定版本的实例
        List<ServiceInstance> versionInstances = instances.stream()
            .filter(instance -> version.equals(
                instance.getMetadata().get("version")
            ))
            .collect(Collectors.toList());

        if (versionInstances.isEmpty()) {
            // 降级：使用默认版本
            return instances.get(0);
        }

        // 随机选择一个
        int index = ThreadLocalRandom.current().nextInt(versionInstances.size());
        return versionInstances.get(index);
    }
}
```

## 五、最佳实践

### 5.1 高可用部署

1. **Eureka Server集群**：
   - 至少3个节点
   - 相互注册，互为备份
   - 分布在不同可用区

2. **服务实例冗余**：
   - 关键服务至少2个实例
   - 分布在不同机器/容器

### 5.2 性能优化

1. **调整缓存时间**：
```yaml
eureka:
  server:
    # 响应缓存更新间隔（默认30秒，可缩短到10秒）
    response-cache-update-interval-ms: 10000
```

2. **优化心跳频率**：
```yaml
eureka:
  instance:
    # 快速发现故障实例（开发环境）
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
```

### 5.3 监控告警

关键指标：
- 注册实例数量
- 心跳成功率
- 服务剔除数量
- 自我保护状态

## 六、常见问题

1. **服务下线延迟**：
   - 原因：多级缓存
   - 解决：缩短缓存更新间隔

2. **自我保护误触发**：
   - 原因：网络抖动
   - 解决：调整阈值或关闭（仅开发环境）

3. **实例ID冲突**：
   - 原因：使用hostname，多实例相同
   - 解决：使用IP:PORT作为实例ID
