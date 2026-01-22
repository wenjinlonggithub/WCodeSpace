# Spring Cloud Alibaba 完整案例指南

## 项目结构

```
spring-cloud-alibaba/
├── nacos-discovery/          # Nacos服务发现案例
│   ├── docs/
│   │   └── nacos-principle.md   # Nacos原理详解
│   └── src/main/java/
│       └── com/alibaba/nacos/discovery/
│           ├── NacosProviderApplication.java      # 服务提供者
│           ├── consumer/
│           │   ├── NacosConsumerApplication.java  # 服务消费者
│           │   └── controller/ConsumerController.java
│           ├── controller/ProviderController.java
│           └── model/ServiceInfo.java
├── nacos-config/            # Nacos配置中心案例
├── sentinel-demo/           # Sentinel流控熔断案例
├── seata-demo/             # Seata分布式事务案例
└── rocketmq-demo/          # RocketMQ消息队列案例
```

## 一、Nacos服务发现案例

### 1.1 快速启动

#### 启动Nacos Server
```bash
# 下载Nacos (https://github.com/alibaba/nacos/releases)
# 解压后进入bin目录

# Windows启动 (standalone模式)
startup.cmd -m standalone

# Linux/Mac启动
sh startup.sh -m standalone

# 访问控制台: http://localhost:8848/nacos
# 默认账号密码: nacos/nacos
```

#### 启动服务提供者
```bash
# 进入nacos-discovery目录
cd nacos-discovery

# 启动Provider (端口8081)
mvn spring-boot:run -Dspring-boot.run.profiles=provider

# 可以启动多个实例测试负载均衡 (修改端口)
mvn spring-boot:run -Dspring-boot.run.profiles=provider -Dserver.port=8083
```

#### 启动服务消费者
```bash
# 启动Consumer (端口8082)
mvn spring-boot:run -Dspring-boot.run.profiles=consumer
```

### 1.2 测试接口

#### Provider接口
```bash
# 1. Hello接口
curl "http://localhost:8081/provider/hello?name=Test"
# 响应: Hello Test, from nacos-provider:8081

# 2. 获取服务信息
curl http://localhost:8081/provider/info

# 3. 查询服务实例
curl http://localhost:8081/provider/instances/nacos-provider

# 4. 健康检查
curl http://localhost:8081/provider/health
```

#### Consumer接口
```bash
# 1. 调用Provider (自动负载均衡)
curl "http://localhost:8082/consumer/call/hello?name=Consumer"

# 2. 手动选择实例调用
curl http://localhost:8082/consumer/call/info

# 3. 批量调用 (验证负载均衡)
curl "http://localhost:8082/consumer/call/batch?count=10"

# 4. POST请求
curl -X POST http://localhost:8082/consumer/call/process \
  -H "Content-Type: application/json" \
  -d "test data"
```

### 1.3 Nacos控制台操作

1. 访问 http://localhost:8848/nacos
2. 进入"服务管理" -> "服务列表"
3. 可以看到注册的服务: nacos-provider, nacos-consumer
4. 点击"详情"查看服务实例信息
5. 点击"编辑"可以修改权重、元数据等

### 1.4 核心特性演示

#### 负载均衡
启动多个Provider实例,Consumer会自动进行负载均衡:
```bash
# 终端1: 启动Provider实例1
mvn spring-boot:run -Dspring-boot.run.profiles=provider -Dserver.port=8081

# 终端2: 启动Provider实例2
mvn spring-boot:run -Dspring-boot.run.profiles=provider -Dserver.port=8083

# 终端3: 启动Provider实例3
mvn spring-boot:run -Dspring-boot.run.profiles=provider -Dserver.port=8084

# 终端4: 批量调用观察负载均衡
curl "http://localhost:8082/consumer/call/batch?count=10"
```

#### 服务下线
1. 停止某个Provider实例
2. 等待15-30秒 (心跳超时)
3. 再次调用Consumer,不会路由到已下线的实例

#### 权重调整
1. 在Nacos控制台修改实例权重 (0-1)
2. 权重越高,被调用的概率越大
3. 权重为0则不会被调用

## 二、关键代码解析

### 2.1 服务注册

```java
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现
public class NacosProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosProviderApplication.class, args);
    }
}
```

配置文件:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848  # Nacos地址
        namespace: public             # 命名空间
        group: DEFAULT_GROUP          # 分组
        cluster-name: DEFAULT         # 集群
        weight: 1.0                   # 权重
        ephemeral: true               # 临时实例
```

### 2.2 服务调用

```java
@Configuration
public class Config {
    @Bean
    @LoadBalanced  // 启用负载均衡
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
public class ConsumerController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/call")
    public String call() {
        // 使用服务名调用
        return restTemplate.getForObject(
            "http://nacos-provider/provider/hello",
            String.class
        );
    }
}
```

### 2.3 DiscoveryClient使用

```java
@Autowired
private DiscoveryClient discoveryClient;

// 获取所有服务
List<String> services = discoveryClient.getServices();

// 获取指定服务的实例
List<ServiceInstance> instances =
    discoveryClient.getInstances("nacos-provider");

// 遍历实例
for (ServiceInstance instance : instances) {
    String host = instance.getHost();
    int port = instance.getPort();
    Map<String, String> metadata = instance.getMetadata();
}
```

## 三、进阶特性

### 3.1 命名空间隔离

用于环境隔离 (开发/测试/生产):

```yaml
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev-namespace-id  # 开发环境
        # namespace: test-namespace-id  # 测试环境
        # namespace: prod-namespace-id  # 生产环境
```

### 3.2 集群优先调用

配置集群名称,优先调用同集群实例:

```yaml
spring:
  cloud:
    nacos:
      discovery:
        cluster-name: beijing  # 北京集群
```

### 3.3 元数据路由

基于元数据进行灰度发布:

```yaml
# Provider配置
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v2.0  # 新版本
          gray: true     # 灰度标识
```

```java
// Consumer根据元数据选择实例
@Autowired
private DiscoveryClient discoveryClient;

List<ServiceInstance> instances =
    discoveryClient.getInstances("nacos-provider");

// 筛选灰度实例
List<ServiceInstance> grayInstances = instances.stream()
    .filter(i -> "true".equals(i.getMetadata().get("gray")))
    .collect(Collectors.toList());
```

### 3.4 持久化实例

持久化实例由Nacos主动进行健康检查:

```yaml
spring:
  cloud:
    nacos:
      discovery:
        ephemeral: false  # 持久化实例
```

## 四、监控与运维

### 4.1 Actuator端点

```bash
# 查看Nacos服务发现信息
curl http://localhost:8081/actuator/nacos-discovery

# 查看健康状态
curl http://localhost:8081/actuator/health

# 查看所有端点
curl http://localhost:8081/actuator
```

### 4.2 日志级别调整

```yaml
logging:
  level:
    com.alibaba.nacos: DEBUG  # Nacos日志
    org.springframework.cloud.client.loadbalancer: DEBUG  # 负载均衡日志
```

### 4.3 常见问题排查

#### 服务注册失败
- 检查Nacos Server是否启动
- 检查server-addr配置是否正确
- 检查网络连接
- 查看日志: `logging.level.com.alibaba.nacos=DEBUG`

#### 服务调用失败
- 确认服务已注册到Nacos
- 检查服务名是否正确
- 确认LoadBalancer依赖已添加
- 检查@LoadBalanced注解

#### 负载均衡不生效
- 确认启动了多个Provider实例
- 检查实例权重是否为0
- 验证实例是否健康

## 五、性能优化

### 5.1 心跳优化

```yaml
spring:
  cloud:
    nacos:
      discovery:
        heart-beat-interval: 5      # 心跳间隔(秒)
        heart-beat-timeout: 15      # 心跳超时(秒)
        ip-delete-timeout: 30       # IP删除超时(秒)
```

### 5.2 本地缓存

Nacos客户端会缓存服务列表,定时更新:

```yaml
spring:
  cloud:
    nacos:
      discovery:
        cache-registry-interval: 30000  # 缓存刷新间隔(毫秒)
```

### 5.3 连接池配置

```java
@Bean
public RestTemplate restTemplate() {
    HttpComponentsClientHttpRequestFactory factory =
        new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    factory.setConnectionRequestTimeout(3000);
    return new RestTemplate(factory);
}
```

## 六、最佳实践

1. **命名规范**
   - 服务名: 小写字母、数字、中划线
   - 分组: 环境-模块 (如: prod-order)
   - 命名空间: 按环境划分

2. **健康检查**
   - 实现自定义健康检查逻辑
   - 合理设置超时时间
   - 监控心跳成功率

3. **负载均衡**
   - 合理设置权重
   - 使用元数据实现灰度发布
   - 优先调用同集群实例

4. **高可用**
   - Nacos集群部署 (3节点以上)
   - 配置多个server-addr
   - 启用本地缓存容灾

5. **安全加固**
   - 启用Nacos鉴权
   - 使用命名空间隔离
   - 配置访问控制列表

## 七、参考文档

- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Alibaba文档](https://spring-cloud-alibaba-group.github.io/github-pages/2023/zh-cn/index.html)
- [Nacos架构原理](./docs/nacos-principle.md)

## 八、下一步

完成Nacos服务发现后,可以继续学习:

1. **Nacos Config** - 配置中心功能
2. **Sentinel** - 流量控制和熔断降级
3. **Seata** - 分布式事务
4. **RocketMQ** - 消息队列集成
