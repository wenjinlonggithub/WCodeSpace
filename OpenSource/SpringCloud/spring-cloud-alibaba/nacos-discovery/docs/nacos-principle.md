# Nacos 服务发现与注册原理详解

## 一、Nacos概述

Nacos (Dynamic Naming and Configuration Service) 是阿里巴巴开源的服务发现和配置管理平台，致力于帮助开发者发现、配置和管理微服务。

### 核心功能
- **服务注册与发现**: 动态服务实例注册和健康检查
- **配置管理**: 动态配置推送和管理
- **服务健康监测**: 提供服务健康度检查能力
- **动态DNS服务**: 支持基于DNS协议的服务发现

## 二、Nacos服务注册与发现原理

### 2.1 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                        Nacos Server                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Open API    │  │  Config      │  │  Naming      │       │
│  │              │  │  Service     │  │  Service     │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│          │                 │                 │               │
│  ┌───────┴─────────────────┴─────────────────┴────────┐     │
│  │           Consistency Protocol (Raft/Distro)       │     │
│  └───────────────────────────────────────────────────┘      │
│          │                                                   │
│  ┌───────┴────────────────────────────────────────────┐     │
│  │              Data Storage (Memory/DB)              │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
         ↑                    ↑                    ↑
         │                    │                    │
    ┌────┴────┐          ┌────┴────┐         ┌────┴────┐
    │Service A│          │Service B│         │Service C│
    │Provider │          │Provider │         │Consumer │
    └─────────┘          └─────────┘         └─────────┘
```

### 2.2 注册流程

#### 服务提供者注册流程
1. **服务启动**: 应用启动时，通过Spring Cloud集成自动触发注册
2. **构建实例信息**:
   - ServiceName (服务名)
   - IP地址
   - 端口号
   - 权重
   - 元数据
   - 健康状态
3. **发送注册请求**: 调用Nacos Open API `/nacos/v1/ns/instance`
4. **Nacos处理注册**:
   - 验证服务信息
   - 存储到服务注册表
   - 触发服务变更事件
   - 通知订阅者
5. **心跳维持**: 默认5秒发送一次心跳

#### 核心代码流程
```java
// NacosNamingService.registerInstance()
public void registerInstance(String serviceName, String groupName, Instance instance) {
    // 1. 检查心跳时间
    if (instance.isEphemeral()) {
        BeatInfo beatInfo = new BeatInfo();
        beatInfo.setServiceName(serviceName);
        beatInfo.setIp(instance.getIp());
        beatInfo.setPort(instance.getPort());

        // 2. 启动心跳任务
        beatReactor.addBeatInfo(serviceName, beatInfo);
    }

    // 3. 发送注册请求
    serverProxy.registerService(serviceName, groupName, instance);
}
```

### 2.3 服务发现流程

#### 消费者获取服务列表
1. **查询服务**: 消费者向Nacos查询服务提供者列表
2. **订阅服务变更**: 建立长连接,监听服务变更
3. **本地缓存**: 将服务列表缓存到本地
4. **定时更新**: 定时从Nacos拉取最新服务列表
5. **推送更新**: Nacos主动推送服务变更

```java
// NamingService.subscribe()
public void subscribe(String serviceName, EventListener listener) {
    // 1. 添加监听器
    eventDispatcher.addListener(serviceName, listener);

    // 2. 获取服务实例
    List<Instance> instances = hostReactor.getServiceInfo(serviceName).getHosts();

    // 3. 启动定时更新任务
    hostReactor.scheduleUpdateIfAbsent(serviceName);
}
```

### 2.4 健康检查机制

#### 两种健康检查模式

**1. 客户端心跳模式 (临时实例)**
- 客户端主动向Nacos发送心跳 (默认5s一次)
- 15秒未收到心跳标记为不健康
- 30秒未收到心跳剔除实例

```java
// BeatReactor心跳调度
public void addBeatInfo(String serviceName, BeatInfo beatInfo) {
    executorService.schedule(new BeatTask(beatInfo), 0, TimeUnit.MILLISECONDS);
}

class BeatTask implements Runnable {
    public void run() {
        // 发送心跳
        long result = serverProxy.sendBeat(beatInfo);

        // 根据服务端返回的间隔时间调度下次心跳
        executorService.schedule(new BeatTask(beatInfo),
            result > 0 ? result : DEFAULT_HEART_BEAT_INTERVAL,
            TimeUnit.MILLISECONDS);
    }
}
```

**2. 服务端健康检查模式 (持久化实例)**
- Nacos主动探测服务健康状态
- 支持TCP/HTTP/MySQL等多种探测方式

### 2.5 数据一致性协议

#### Distro协议 (临时实例)
- **AP模型**: 保证可用性和分区容错性
- **特点**:
  - 每个节点存储部分数据
  - 节点间异步复制
  - 读取本地数据,无需远程调用

```
节点1 (负责实例A,B)    节点2 (负责实例C,D)    节点3 (负责实例E,F)
      │                      │                      │
      ├─────异步复制─────────┤                      │
      │                      ├─────异步复制─────────┤
      └─────────────────异步复制──────────────────────┘
```

#### Raft协议 (持久化实例)
- **CP模型**: 保证强一致性
- **特点**:
  - Leader选举
  - 日志复制
  - 数据强一致

### 2.6 负载均衡策略

Nacos支持多种负载均衡策略:

1. **权重随机**: 根据实例权重随机选择
2. **权重轮询**: 根据权重进行轮询
3. **一致性Hash**: 根据请求参数Hash到固定实例

```java
// 权重随机算法
protected Instance chooseInstanceByRandomWeight(List<Instance> hosts) {
    // 计算总权重
    double totalWeight = 0;
    for (Instance instance : hosts) {
        totalWeight += instance.getWeight();
    }

    // 随机选择
    double randomWeight = ThreadLocalRandom.current().nextDouble(0, totalWeight);
    double currentWeight = 0;

    for (Instance instance : hosts) {
        currentWeight += instance.getWeight();
        if (randomWeight <= currentWeight) {
            return instance;
        }
    }

    return hosts.get(0);
}
```

## 三、Spring Cloud Alibaba 集成原理

### 3.1 自动配置机制

Spring Cloud Alibaba 通过 Spring Boot 的自动配置机制实现与 Nacos 的无缝集成。

#### 核心自动配置类

```java
// NacosDiscoveryAutoConfiguration
@Configuration
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
public class NacosDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosServiceRegistry nacosServiceRegistry(
            NacosDiscoveryProperties nacosDiscoveryProperties,
            NacosServiceManager nacosServiceManager) {
        return new NacosServiceRegistry(nacosServiceManager, nacosDiscoveryProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosRegistration nacosRegistration(
            NacosDiscoveryProperties nacosDiscoveryProperties,
            ApplicationContext context) {
        return new NacosRegistration(nacosDiscoveryProperties, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosAutoServiceRegistration nacosAutoServiceRegistration(
            NacosServiceRegistry registry,
            AutoServiceRegistrationProperties properties,
            NacosRegistration registration) {
        return new NacosAutoServiceRegistration(registry, properties, registration);
    }
}
```

#### 配置属性绑定

```java
@ConfigurationProperties("spring.cloud.nacos.discovery")
public class NacosDiscoveryProperties {
    private String serverAddr;          // Nacos服务器地址
    private String namespace = "";      // 命名空间
    private String group = "DEFAULT_GROUP";  // 分组
    private String clusterName = "DEFAULT";  // 集群名
    private float weight = 1.0f;        // 权重
    private boolean ephemeral = true;   // 是否临时实例
    private Map<String, String> metadata = new HashMap<>();  // 元数据
    // ... 其他配置
}
```

### 3.2 @EnableDiscoveryClient 工作原理

#### 注解定义
```java
@Import(EnableDiscoveryClientImportSelector.class)
public @interface EnableDiscoveryClient {
    boolean autoRegister() default true;
}
```

#### 导入选择器
```java
public class EnableDiscoveryClientImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        // 导入自动服务注册配置
        return new String[] {
            AutoServiceRegistrationConfiguration.class.getName()
        };
    }
}
```

#### 服务注册流程

1. **应用启动时触发**
```java
// NacosAutoServiceRegistration 实现 ApplicationListener
public class NacosAutoServiceRegistration
        extends AbstractAutoServiceRegistration<Registration> {

    @Override
    protected void register() {
        // 调用 NacosServiceRegistry 进行注册
        serviceRegistry.register(getRegistration());
    }
}
```

2. **实际注册逻辑**
```java
public class NacosServiceRegistry implements ServiceRegistry<Registration> {

    @Override
    public void register(Registration registration) {
        // 获取服务名
        String serviceId = registration.getServiceId();

        // 构建实例信息
        Instance instance = new Instance();
        instance.setIp(registration.getHost());
        instance.setPort(registration.getPort());
        instance.setWeight(nacosDiscoveryProperties.getWeight());
        instance.setClusterName(nacosDiscoveryProperties.getClusterName());
        instance.setMetadata(registration.getMetadata());
        instance.setEphemeral(nacosDiscoveryProperties.isEphemeral());

        // 调用 Nacos SDK 注册
        namingService.registerInstance(serviceId,
            nacosDiscoveryProperties.getGroup(), instance);
    }
}
```

### 3.3 @LoadBalanced 负载均衡原理

#### 注解定义与拦截器注入

```java
@Configuration
@ConditionalOnClass(RestTemplate.class)
public class LoadBalancerAutoConfiguration {

    // 收集所有标注了 @LoadBalanced 的 RestTemplate
    @LoadBalanced
    @Autowired(required = false)
    private List<RestTemplate> restTemplates = Collections.emptyList();

    @Bean
    public SmartInitializingSingleton loadBalancedRestTemplateInitializer(
            List<RestTemplateCustomizer> customizers) {
        return () -> {
            for (RestTemplate restTemplate : restTemplates) {
                // 为每个 RestTemplate 添加负载均衡拦截器
                for (RestTemplateCustomizer customizer : customizers) {
                    customizer.customize(restTemplate);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplateCustomizer restTemplateCustomizer(
            LoadBalancerInterceptor loadBalancerInterceptor) {
        return restTemplate -> {
            List<ClientHttpRequestInterceptor> interceptors =
                new ArrayList<>(restTemplate.getInterceptors());
            interceptors.add(loadBalancerInterceptor);
            restTemplate.setInterceptors(interceptors);
        };
    }
}
```

#### 负载均衡拦截器

```java
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {

    private LoadBalancerClient loadBalancer;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        URI originalUri = request.getURI();
        String serviceName = originalUri.getHost();

        // 使用负载均衡器选择实例
        return loadBalancer.execute(serviceName,
            instance -> {
                // 将服务名替换为实际的 IP:Port
                URI uri = loadBalancer.reconstructURI(instance, originalUri);
                HttpRequest serviceRequest = new ServiceRequestWrapper(request, uri);
                // 执行实际的 HTTP 请求
                return execution.execute(serviceRequest, body);
            });
    }
}
```

#### 服务实例选择

```java
public class NacosLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private NacosDiscoveryClient discoveryClient;
    private String serviceId;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 从 Nacos 获取服务实例列表
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        if (instances.isEmpty()) {
            return Mono.just(new EmptyResponse());
        }

        // 根据权重随机选择实例
        ServiceInstance instance = getInstanceByWeight(instances);

        return Mono.just(new DefaultResponse(instance));
    }

    private ServiceInstance getInstanceByWeight(List<ServiceInstance> instances) {
        // 计算总权重
        double totalWeight = instances.stream()
            .mapToDouble(i -> Double.parseDouble(
                i.getMetadata().getOrDefault("weight", "1.0")))
            .sum();

        // 权重随机
        double random = ThreadLocalRandom.current().nextDouble(0, totalWeight);
        double currentWeight = 0;

        for (ServiceInstance instance : instances) {
            double weight = Double.parseDouble(
                instance.getMetadata().getOrDefault("weight", "1.0"));
            currentWeight += weight;
            if (random <= currentWeight) {
                return instance;
            }
        }

        return instances.get(0);
    }
}
```

### 3.4 服务调用完整链路

#### 调用流程图

```
Consumer 发起请求
    ↓
RestTemplate.getForObject("http://nacos-provider/api")
    ↓
LoadBalancerInterceptor 拦截
    ↓
从 URI 中提取服务名: "nacos-provider"
    ↓
NacosLoadBalancer.choose() 选择实例
    ↓
NacosDiscoveryClient.getInstances("nacos-provider")
    ↓
从本地缓存获取实例列表
    ↓
根据权重/策略选择一个实例: 192.168.1.100:8081
    ↓
重构 URI: http://192.168.1.100:8081/api
    ↓
执行实际 HTTP 请求
    ↓
返回响应
```

#### 详细代码流程

```java
// 1. Consumer 发起调用
String result = restTemplate.getForObject(
    "http://nacos-provider/provider/hello?name=Test",
    String.class);

// 2. LoadBalancerInterceptor 拦截
public ClientHttpResponse intercept(HttpRequest request, ...) {
    URI uri = request.getURI();  // http://nacos-provider/provider/hello?name=Test
    String serviceName = uri.getHost();  // nacos-provider

    // 3. 选择服务实例
    ServiceInstance instance = loadBalancer.choose(serviceName);
    // instance: {host: "192.168.1.100", port: 8081}

    // 4. 重构 URI
    URI realUri = reconstructURI(instance, uri);
    // realUri: http://192.168.1.100:8081/provider/hello?name=Test

    // 5. 执行实际请求
    return execution.execute(newRequest, body);
}
```

### 3.5 客户端缓存与更新机制

#### 缓存结构

```java
public class HostReactor {
    // 服务实例缓存: key=serviceName@@groupName, value=ServiceInfo
    private Map<String, ServiceInfo> serviceInfoMap =
        new ConcurrentHashMap<>();

    // 定时更新任务
    private ScheduledExecutorService executor;

    // 推送接收器
    private PushReceiver pushReceiver;
}
```

#### 三级缓存机制

1. **内存缓存**
```java
public ServiceInfo getServiceInfo(String serviceName, String groupName) {
    String key = serviceName + "@@" + groupName;

    // 从内存缓存获取
    ServiceInfo serviceInfo = serviceInfoMap.get(key);

    if (serviceInfo == null) {
        // 缓存未命中,从 Nacos Server 拉取
        serviceInfo = updateServiceInfo(serviceName, groupName);
    }

    return serviceInfo;
}
```

2. **磁盘缓存**
```java
// 将服务信息持久化到本地磁盘
private void saveToDisk(ServiceInfo serviceInfo) {
    String cacheDir = System.getProperty("user.home") +
        "/nacos/naming/" + namespace;
    String fileName = serviceInfo.getKeyEncoded();

    File file = new File(cacheDir, fileName);
    // 写入磁盘
    Files.write(file.toPath(), JSON.toJSONString(serviceInfo).getBytes());
}

// 从磁盘加载缓存(容灾)
private ServiceInfo loadFromDisk(String serviceName) {
    String cacheDir = System.getProperty("user.home") +
        "/nacos/naming/" + namespace;
    String fileName = serviceName.replace("@@", "#");

    File file = new File(cacheDir, fileName);
    if (file.exists()) {
        String json = new String(Files.readAllBytes(file.toPath()));
        return JSON.parseObject(json, ServiceInfo.class);
    }
    return null;
}
```

3. **定时更新**
```java
public void scheduleUpdateIfAbsent(String serviceName, String groupName) {
    String key = serviceName + "@@" + groupName;

    if (updatingMap.containsKey(key)) {
        return;  // 已经在更新中
    }

    // 启动定时更新任务(默认10秒)
    ScheduledFuture<?> future = executor.scheduleWithFixedDelay(
        () -> updateServiceInfo(serviceName, groupName),
        0, 10, TimeUnit.SECONDS);

    updatingMap.put(key, future);
}

private ServiceInfo updateServiceInfo(String serviceName, String groupName) {
    // 从 Nacos Server 拉取最新数据
    ServiceInfo serviceInfo = serverProxy.queryList(serviceName, groupName);

    // 更新内存缓存
    serviceInfoMap.put(serviceName + "@@" + groupName, serviceInfo);

    // 持久化到磁盘
    saveToDisk(serviceInfo);

    // 触发变更事件
    if (isChanged(serviceInfo)) {
        eventDispatcher.serviceChanged(serviceInfo);
    }

    return serviceInfo;
}
```

## 四、关键特性

### 4.1 服务分级模型
```
命名空间 (Namespace)
  └── 分组 (Group)
      └── 服务 (Service)
          └── 集群 (Cluster)
              └── 实例 (Instance)
```

- **命名空间**: 实现环境隔离 (dev/test/prod)
- **分组**: 业务逻辑隔离
- **集群**: 物理隔离,就近访问

### 4.2 UDP 推送机制

Nacos 服务端通过 UDP 协议主动推送服务变更,实现实时更新。

#### 推送接收器

```java
public class PushReceiver implements Runnable {

    private DatagramSocket udpSocket;
    private ScheduledExecutorService executorService;

    @Override
    public void run() {
        try {
            // 监听 UDP 端口
            udpSocket = new DatagramSocket();

            while (true) {
                byte[] buffer = new byte[UDP_MSS];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // 阻塞接收数据
                udpSocket.receive(packet);

                // 解析推送数据
                String json = new String(packet.getData(), 0, packet.getLength());
                PushData pushData = JSON.parseObject(json, PushData.class);

                // 处理服务变更
                processServiceInfo(pushData);
            }
        } catch (Exception e) {
            log.error("UDP receiver error", e);
        }
    }

    private void processServiceInfo(PushData data) {
        String serviceName = data.getServiceName();
        ServiceInfo serviceInfo = data.getData();

        // 更新本地缓存
        hostReactor.processServiceInfo(serviceInfo);

        log.info("Received push data for service: {}, hosts: {}",
            serviceName, serviceInfo.getHosts().size());
    }
}
```

#### 推送流程

```
Nacos Server 检测到服务变更
    ↓
获取订阅该服务的客户端列表
    ↓
遍历客户端,构建 UDP 推送数据
    ↓
发送 UDP 数据包到客户端
    ↓
客户端 PushReceiver 接收数据
    ↓
更新本地服务实例缓存
    ↓
触发服务变更事件
    ↓
通知应用层监听器
```

### 4.3 保护阈值机制

当健康实例数/总实例数 < 保护阈值时,返回所有实例(包括不健康实例),防止雪崩。

#### 实现原理

```java
public class ServiceManager {

    private static final float DEFAULT_PROTECT_THRESHOLD = 0.0f;

    public List<Instance> selectInstances(Service service) {
        List<Instance> allInstances = service.allIPs();
        List<Instance> healthyInstances = allInstances.stream()
            .filter(Instance::isHealthy)
            .collect(Collectors.toList());

        // 计算健康实例占比
        float healthyRatio = (float) healthyInstances.size() / allInstances.size();

        // 判断是否触发保护阈值
        float protectThreshold = service.getProtectThreshold();
        if (protectThreshold <= 0) {
            protectThreshold = DEFAULT_PROTECT_THRESHOLD;
        }

        if (healthyRatio <= protectThreshold) {
            log.warn("Protect threshold reached! healthy/total: {}/{}",
                healthyInstances.size(), allInstances.size());

            // 返回所有实例(包括不健康的)
            return allInstances;
        }

        // 只返回健康实例
        return healthyInstances;
    }
}
```

#### 使用场景

```yaml
spring:
  cloud:
    nacos:
      discovery:
        # 设置保护阈值为 0.3
        # 当健康实例数 < 30% 时,返回所有实例
        protect-threshold: 0.3
```

### 4.4 元数据路由

支持基于元数据的灰度发布、金丝雀部署和标签路由。

#### 元数据配置

```yaml
# Provider A (生产版本)
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v1
          env: prod
          zone: beijing

# Provider B (灰度版本)
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v2
          env: gray
          zone: beijing
```

#### 自定义负载均衡规则

```java
@Configuration
public class MetadataLoadBalancerConfig {

    @Bean
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder()
            .withDiscoveryClient()
            .withZonePreference()  // 优先同区域
            .withMetadata()        // 支持元数据过滤
            .build(context);
    }
}
```

#### 基于版本的路由

```java
public class VersionRouter {

    @Autowired
    private DiscoveryClient discoveryClient;

    public ServiceInstance chooseByVersion(String serviceName, String version) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        // 根据元数据中的 version 过滤
        List<ServiceInstance> versionInstances = instances.stream()
            .filter(i -> version.equals(i.getMetadata().get("version")))
            .collect(Collectors.toList());

        if (versionInstances.isEmpty()) {
            // 降级到默认版本
            return instances.get(0);
        }

        // 随机选择一个实例
        int index = ThreadLocalRandom.current().nextInt(versionInstances.size());
        return versionInstances.get(index);
    }
}
```

#### 灰度发布策略

```java
public class GrayReleaseLoadBalancer {

    private static final String GRAY_HEADER = "X-Gray-Version";

    public ServiceInstance choose(String serviceName, HttpRequest request) {
        // 从请求头获取灰度标识
        String grayVersion = request.getHeaders().getFirst(GRAY_HEADER);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        if (grayVersion != null) {
            // 灰度流量路由到灰度实例
            List<ServiceInstance> grayInstances = instances.stream()
                .filter(i -> "true".equals(i.getMetadata().get("gray")))
                .collect(Collectors.toList());

            if (!grayInstances.isEmpty()) {
                return selectByWeight(grayInstances);
            }
        }

        // 正常流量路由到生产实例
        List<ServiceInstance> prodInstances = instances.stream()
            .filter(i -> !"true".equals(i.getMetadata().get("gray")))
            .collect(Collectors.toList());

        return selectByWeight(prodInstances);
    }
}
```

### 4.5 集群优先调用

配置集群名称,优先调用同集群实例,实现就近访问。

#### 集群配置

```yaml
# 北京机房
spring:
  cloud:
    nacos:
      discovery:
        cluster-name: beijing

# 上海机房
spring:
  cloud:
    nacos:
      discovery:
        cluster-name: shanghai
```

#### 集群优先策略

```java
public class ClusterPreferenceLoadBalancer {

    @Value("${spring.cloud.nacos.discovery.cluster-name}")
    private String localClusterName;

    public ServiceInstance choose(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        // 优先选择同集群实例
        List<ServiceInstance> sameClusterInstances = instances.stream()
            .filter(i -> localClusterName.equals(
                i.getMetadata().get("nacos.cluster")))
            .collect(Collectors.toList());

        if (!sameClusterInstances.isEmpty()) {
            log.info("Choose from same cluster: {}", localClusterName);
            return selectByWeight(sameClusterInstances);
        }

        // 同集群无可用实例,降级到所有集群
        log.warn("No instance in cluster {}, fallback to all clusters",
            localClusterName);
        return selectByWeight(instances);
    }
}
```

## 五、高级特性

### 5.1 服务订阅与监听

客户端可以订阅服务变更,实时获取服务实例变化。

#### 订阅服务

```java
@Component
public class ServiceListener {

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @PostConstruct
    public void subscribeService() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService();

        // 订阅服务变更
        namingService.subscribe("nacos-provider", event -> {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;

                log.info("Service changed: {}", namingEvent.getServiceName());
                log.info("Instances: {}", namingEvent.getInstances());

                // 处理服务变更逻辑
                handleServiceChange(namingEvent.getInstances());
            }
        });
    }

    private void handleServiceChange(List<Instance> instances) {
        // 自定义处理逻辑
        // 例如: 更新本地缓存、预热连接池等
        for (Instance instance : instances) {
            log.info("Instance: {}:{}, healthy: {}",
                instance.getIp(),
                instance.getPort(),
                instance.isHealthy());
        }
    }
}
```

### 5.2 手动服务注册与注销

除了自动注册,还可以手动控制服务的注册和注销。

```java
@Component
public class ManualServiceRegistry {

    @Autowired
    private NacosServiceManager nacosServiceManager;

    public void registerService() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService();

        // 构建实例信息
        Instance instance = new Instance();
        instance.setIp("192.168.1.100");
        instance.setPort(8080);
        instance.setWeight(1.0);
        instance.setHealthy(true);
        instance.setEphemeral(true);

        // 添加元数据
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("region", "cn-beijing");
        instance.setMetadata(metadata);

        // 注册实例
        namingService.registerInstance("my-service", instance);
        log.info("Service registered successfully");
    }

    public void deregisterService() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService();

        // 注销实例
        namingService.deregisterInstance("my-service", "192.168.1.100", 8080);
        log.info("Service deregistered successfully");
    }
}
```

### 5.3 实例权重动态调整

可以在运行时动态调整实例权重,实现流量控制。

```java
@RestController
@RequestMapping("/admin")
public class WeightController {

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private int port;

    /**
     * 动态调整当前实例权重
     */
    @PostMapping("/weight")
    public String updateWeight(@RequestParam double weight) throws NacosException {
        if (weight < 0 || weight > 1) {
            return "Weight must be between 0 and 1";
        }

        NamingService namingService = nacosServiceManager.getNamingService();

        // 获取当前实例
        List<Instance> instances = namingService.getAllInstances(serviceName);
        Instance currentInstance = instances.stream()
            .filter(i -> i.getPort() == port)
            .findFirst()
            .orElse(null);

        if (currentInstance == null) {
            return "Current instance not found";
        }

        // 更新权重
        currentInstance.setWeight(weight);
        namingService.registerInstance(serviceName, currentInstance);

        log.info("Weight updated to: {}", weight);
        return "Weight updated to: " + weight;
    }
}
```

### 5.4 健康检查自定义

实现自定义健康检查逻辑,控制实例健康状态。

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private volatile boolean healthy = true;

    @Override
    public Health health() {
        if (healthy) {
            return Health.up()
                .withDetail("status", "Service is running")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        } else {
            return Health.down()
                .withDetail("status", "Service is degraded")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        }
    }

    /**
     * 手动标记服务为不健康
     */
    public void markUnhealthy() {
        this.healthy = false;
        log.warn("Service marked as unhealthy");
    }

    /**
     * 手动标记服务为健康
     */
    public void markHealthy() {
        this.healthy = true;
        log.info("Service marked as healthy");
    }
}
```

## 六、优势与对比

### 与Eureka对比

| 特性 | Nacos | Eureka |
|------|-------|--------|
| 一致性协议 | CP+AP | AP |
| 健康检查 | 客户端心跳+服务端探测 | 客户端心跳 |
| 负载均衡 | 权重/元数据 | Ribbon |
| 配置管理 | 支持 | 不支持 |
| 雪崩保护 | 支持 | 支持 |
| 界面 | 功能丰富 | 基础 |

### 与Consul对比

| 特性 | Nacos | Consul |
|------|-------|--------|
| 一致性协议 | Distro/Raft | Raft |
| 性能 | 更高 | 较高 |
| 配置中心 | 原生支持 | 需要额外配置 |
| 多数据中心 | 支持 | 原生支持 |
| 中文社区 | 活跃 | 一般 |

## 五、最佳实践

### 5.1 命名规范
```
服务名: ${application-name}
分组: ${env}-${module}
集群: ${region}-${zone}
```

### 5.2 性能优化
1. 合理设置心跳间隔 (默认5s)
2. 启用本地缓存
3. 批量注册/注销
4. 使用持久化实例减少心跳

### 5.3 高可用部署
- 最少3个节点
- 使用外置数据库 (MySQL)
- 配置反向代理 (Nginx)

### 5.4 安全加固
- 启用鉴权
- 使用命名空间隔离
- 配置访问控制

## 六、源码关键类

```
NamingService - 服务发现核心接口
NacosNamingService - NamingService实现
BeatReactor - 心跳管理
HostReactor - 服务实例管理
PushReceiver - UDP推送接收
EventDispatcher - 事件分发
```

## 七、源码关键类

### 7.1 核心类结构

```
Spring Cloud Alibaba 层:
├── NacosDiscoveryAutoConfiguration     # 自动配置类
├── NacosServiceRegistry                # 服务注册实现
├── NacosRegistration                   # 注册信息封装
├── NacosAutoServiceRegistration        # 自动注册触发
├── NacosDiscoveryClient               # 服务发现客户端
├── NacosLoadBalancer                  # 负载均衡器
└── NacosDiscoveryProperties           # 配置属性

Nacos Client SDK 层:
├── NamingService                       # 服务发现核心接口
├── NacosNamingService                 # NamingService 实现
├── BeatReactor                        # 心跳管理
├── HostReactor                        # 服务实例管理
├── PushReceiver                       # UDP 推送接收
├── EventDispatcher                    # 事件分发
└── ServerProxy                        # 服务端代理
```

### 7.2 关键源码分析

#### NacosNamingService 核心方法

```java
public class NacosNamingService implements NamingService {

    private HostReactor hostReactor;
    private BeatReactor beatReactor;
    private ServerProxy serverProxy;
    private EventDispatcher eventDispatcher;

    /**
     * 注册实例
     */
    @Override
    public void registerInstance(String serviceName, String groupName,
            Instance instance) throws NacosException {

        // 1. 检查心跳时间
        if (instance.isEphemeral()) {
            // 临时实例启动心跳
            BeatInfo beatInfo = beatReactor.buildBeatInfo(instance);
            beatReactor.addBeatInfo(
                NamingUtils.getGroupedName(serviceName, groupName),
                beatInfo);
        }

        // 2. 发送注册请求到 Nacos Server
        serverProxy.registerService(
            NamingUtils.getGroupedName(serviceName, groupName),
            groupName, instance);
    }

    /**
     * 获取所有实例
     */
    @Override
    public List<Instance> getAllInstances(String serviceName,
            String groupName) throws NacosException {

        // 1. 订阅服务(启动定时更新和 UDP 监听)
        ServiceInfo serviceInfo = hostReactor.getServiceInfo(
            NamingUtils.getGroupedName(serviceName, groupName));

        // 2. 从缓存获取实例列表
        List<Instance> list;
        if (serviceInfo == null || CollectionUtils.isEmpty(
                list = serviceInfo.getHosts())) {
            return new ArrayList<>();
        }

        return list;
    }

    /**
     * 订阅服务变更
     */
    @Override
    public void subscribe(String serviceName, String groupName,
            EventListener listener) throws NacosException {

        // 1. 添加监听器
        eventDispatcher.addListener(serviceInfo, groupName, listener);

        // 2. 启动定时更新任务
        hostReactor.getServiceInfo(
            NamingUtils.getGroupedName(serviceName, groupName));
    }
}
```

#### BeatReactor 心跳实现

```java
public class BeatReactor {

    private ScheduledExecutorService executorService;
    private Map<String, BeatInfo> dom2Beat = new ConcurrentHashMap<>();

    /**
     * 添加心跳任务
     */
    public void addBeatInfo(String serviceName, BeatInfo beatInfo) {
        NAMING_LOGGER.info("Adding beat: {} to beat map.", beatInfo);

        String key = buildKey(serviceName, beatInfo.getIp(), beatInfo.getPort());
        BeatInfo existBeat = dom2Beat.remove(key);

        if (existBeat != null) {
            existBeat.setStopped(true);
        }

        dom2Beat.put(key, beatInfo);

        // 立即执行一次心跳
        executorService.schedule(new BeatTask(beatInfo), 0,
            TimeUnit.MILLISECONDS);
    }

    /**
     * 心跳任务
     */
    class BeatTask implements Runnable {
        BeatInfo beatInfo;

        public BeatTask(BeatInfo beatInfo) {
            this.beatInfo = beatInfo;
        }

        @Override
        public void run() {
            if (beatInfo.isStopped()) {
                return;
            }

            long nextTime = beatInfo.getPeriod();

            try {
                // 发送心跳到 Nacos Server
                JsonNode result = serverProxy.sendBeat(beatInfo, BeatReactor.this.lightBeatEnabled);

                long interval = result.get("clientBeatInterval").asLong();
                boolean lightBeatEnabled = false;

                if (result.has(CommonParams.LIGHT_BEAT_ENABLED)) {
                    lightBeatEnabled = result.get(CommonParams.LIGHT_BEAT_ENABLED).asBoolean();
                }

                BeatReactor.this.lightBeatEnabled = lightBeatEnabled;

                if (interval > 0) {
                    nextTime = interval;
                }

                // 检查服务端响应状态
                int code = NamingResponseCode.OK;
                if (result.has(CommonParams.CODE)) {
                    code = result.get(CommonParams.CODE).asInt();
                }

                if (code == NamingResponseCode.RESOURCE_NOT_FOUND) {
                    // 实例不存在,重新注册
                    Instance instance = new Instance();
                    instance.setPort(beatInfo.getPort());
                    instance.setIp(beatInfo.getIp());
                    instance.setWeight(beatInfo.getWeight());
                    instance.setMetadata(beatInfo.getMetadata());
                    instance.setClusterName(beatInfo.getCluster());
                    instance.setServiceName(beatInfo.getServiceName());
                    instance.setInstanceId(instance.getInstanceId());
                    instance.setEphemeral(true);

                    try {
                        serverProxy.registerService(beatInfo.getServiceName(),
                            NamingUtils.getGroupName(beatInfo.getServiceName()),
                            instance);
                    } catch (Exception ignore) {
                    }
                }
            } catch (NacosException ex) {
                NAMING_LOGGER.error("Send beat error", ex);
            }

            // 调度下次心跳
            executorService.schedule(new BeatTask(beatInfo), nextTime,
                TimeUnit.MILLISECONDS);
        }
    }
}
```

#### HostReactor 服务缓存管理

```java
public class HostReactor {

    private Map<String, ServiceInfo> serviceInfoMap;
    private Map<String, ScheduledFuture<?>> futureMap;
    private ScheduledExecutorService executor;
    private PushReceiver pushReceiver;
    private EventDispatcher eventDispatcher;

    public HostReactor(EventDispatcher eventDispatcher,
            NamingProxy serverProxy, String cacheDir) {

        // 初始化推送接收器
        this.pushReceiver = new PushReceiver(this);

        // 启动 UDP 监听线程
        this.executor.execute(this.pushReceiver);

        // 初始化事件分发器
        this.eventDispatcher = eventDispatcher;

        // 加载本地缓存文件(容灾)
        this.serviceInfoMap = new ConcurrentHashMap<>(DiskCache.read(cacheDir));
    }

    /**
     * 获取服务信息(带缓存)
     */
    public ServiceInfo getServiceInfo(String serviceName, String clusters) {

        String key = ServiceInfo.getKey(serviceName, clusters);

        ServiceInfo serviceObj = getServiceInfo0(serviceName, clusters);

        if (serviceObj == null) {
            // 缓存未命中,从服务端拉取
            serviceObj = new ServiceInfo(serviceName, clusters);
            serviceInfoMap.put(serviceObj.getKey(), serviceObj);
            updatingMap.put(serviceName, new Object());
            updateServiceNow(serviceName, clusters);
            updatingMap.remove(serviceName);

        } else if (updatingMap.containsKey(serviceName)) {
            // 正在更新中,等待更新完成
            synchronized (serviceObj) {
                try {
                    serviceObj.wait(DEFAULT_DELAY);
                } catch (InterruptedException e) {
                    NAMING_LOGGER.error("Waiting service update interrupted", e);
                }
            }
        }

        // 启动定时更新任务
        scheduleUpdateIfAbsent(serviceName, clusters);

        return serviceInfoMap.get(key);
    }

    /**
     * 定时更新服务信息
     */
    public void scheduleUpdateIfAbsent(String serviceName, String clusters) {
        if (futureMap.get(ServiceInfo.getKey(serviceName, clusters)) != null) {
            return;
        }

        synchronized (futureMap) {
            if (futureMap.get(ServiceInfo.getKey(serviceName, clusters)) != null) {
                return;
            }

            ScheduledFuture<?> future = addTask(new UpdateTask(serviceName, clusters));
            futureMap.put(ServiceInfo.getKey(serviceName, clusters), future);
        }
    }

    /**
     * 更新任务
     */
    public class UpdateTask implements Runnable {
        long lastRefTime = System.currentTimeMillis();
        private String clusters;
        private String serviceName;

        @Override
        public void run() {
            long delayTime = DEFAULT_DELAY;

            try {
                // 从服务端拉取最新数据
                ServiceInfo serviceObj = serverProxy.queryList(serviceName, clusters, 0, false);

                if (serviceObj != null) {
                    // 处理服务信息更新
                    processServiceInfo(serviceObj);
                    lastRefTime = serviceObj.getLastRefTime();
                    return;
                }

                // 如果长时间未更新,增加更新频率
                if (System.currentTimeMillis() - lastRefTime > lastRefreshInterval) {
                    delayTime = REFRESH_INTERVAL_ON_PUSH_EMPTY;
                }

            } catch (Throwable e) {
                NAMING_LOGGER.warn("Failed to update service: " + serviceName, e);
            } finally {
                // 调度下次更新
                executor.schedule(this, Math.min(delayTime << failedTimes, DEFAULT_DELAY * 60),
                    TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * 处理服务信息(缓存更新和事件通知)
     */
    public ServiceInfo processServiceInfo(ServiceInfo serviceInfo) {
        String serviceKey = serviceInfo.getKey();
        ServiceInfo oldService = serviceInfoMap.get(serviceKey);

        if (isEmptyOrErrorPush(serviceInfo)) {
            // 空推送,忽略
            return oldService;
        }

        // 更新内存缓存
        serviceInfoMap.put(serviceInfo.getKey(), serviceInfo);

        // 判断服务是否变更
        boolean changed = isChangedServiceInfo(oldService, serviceInfo);

        if (StringUtils.isBlank(serviceInfo.getJsonFromServer())) {
            serviceInfo.setJsonFromServer(JacksonUtils.toJson(serviceInfo));
        }

        MetricsMonitor.getServiceInfoMapSizeMonitor().set(serviceInfoMap.size());

        if (changed) {
            NAMING_LOGGER.info("Service changed: {}", serviceInfo.getKey());

            // 触发服务变更事件
            if (oldService != null) {
                notifySubscribers(serviceInfo, oldService);
            }

            // 持久化到磁盘
            DiskCache.write(serviceInfo, cacheDir);
        }

        return serviceInfo;
    }

    /**
     * 通知订阅者
     */
    private void notifySubscribers(ServiceInfo serviceInfo, ServiceInfo oldService) {
        eventDispatcher.serviceChanged(serviceInfo);

        // 通知所有监听器
        List<EventListener> listeners = eventDispatcher.getListeners(
            serviceInfo.getName(), serviceInfo.getClusters());

        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }

        for (EventListener listener : listeners) {
            NotifyCenter.publishEvent(new InstancesChangeEvent(
                serviceInfo.getName(), serviceInfo.getGroupName(),
                serviceInfo.getClusters(), serviceInfo.getHosts()));
        }
    }
}
```

## 八、监控指标

重要监控指标:
- 服务注册/注销QPS
- 心跳成功率
- 服务查询响应时间
- 实例健康状态
- 推送延迟时间
