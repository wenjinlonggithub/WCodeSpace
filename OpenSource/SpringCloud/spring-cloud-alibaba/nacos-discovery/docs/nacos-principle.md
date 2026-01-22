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

## 三、关键特性

### 3.1 服务分级模型
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

### 3.2 保护阈值
当健康实例数/总实例数 < 保护阈值时,返回所有实例(包括不健康实例),防止雪崩。

### 3.3 元数据管理
支持自定义元数据,实现:
- 灰度发布
- 金丝雀部署
- 标签路由

## 四、优势与对比

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

## 七、监控指标

重要监控指标:
- 服务注册/注销QPS
- 心跳成功率
- 服务查询响应时间
- 实例健康状态
- 推送延迟时间
