# 服务网格架构 (Service Mesh)

## 一、架构概述

服务网格(Service Mesh)是一个专用的基础设施层,用于处理服务间通信。它通过边车代理(Sidecar Proxy)模式,在应用层之外统一管理微服务间的网络通信,包括服务发现、负载均衡、熔断降级、安全认证等功能。

## 二、解决的业务问题

### 1. 微服务通信的痛点
- **通信逻辑耦合**: 服务发现、重试、超时等逻辑嵌入业务代码
- **多语言支持难**: 每种语言都要实现一套通信逻辑
- **可观测性差**: 难以统一监控和追踪
- **安全管理复杂**: 服务间认证授权难以统一
- **流量控制困难**: 灰度发布、A/B测试实现复杂

### 2. 服务网格的解决方案
- **通信下沉**: 将通信逻辑从应用代码中剥离
- **语言无关**: 通过Sidecar统一处理通信
- **统一可观测**: 集中收集指标、日志、链路
- **安全加固**: 统一的mTLS加密和身份认证
- **流量管理**: 灵活的路由规则和策略

### 3. 适用场景
- **大规模微服务**: 服务数量超过50个
- **多语言环境**: Java、Go、Python混合
- **安全要求高**: 需要服务间加密通信
- **复杂流量管理**: 灰度、金丝雀发布
- **云原生应用**: Kubernetes环境

## 三、核心架构

### 1. 服务网格架构图

```
┌──────────────────────────────────────────────────┐
│             Control Plane (控制平面)              │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐ │
│  │ Pilot  │  │Citadel │  │Galley  │  │Telemetry│ │
│  │服务发现 │  │ 安全   │  │ 配置   │  │  遥测   │ │
│  └────────┘  └────────┘  └────────┘  └────────┘ │
└──────────┬───────────────────────────────────────┘
           │ 配置下发
           ▼
┌──────────────────────────────────────────────────┐
│             Data Plane (数据平面)                 │
│  ┌─────────────┐         ┌─────────────┐         │
│  │  Service A  │         │  Service B  │         │
│  │  ┌───────┐  │         │  ┌───────┐  │         │
│  │  │  App  │  │◄───────►│  │  App  │  │         │
│  │  └───┬───┘  │         │  └───┬───┘  │         │
│  │      │      │         │      │      │         │
│  │  ┌───▼───┐  │         │  ┌───▼───┐  │         │
│  │  │Envoy  │  │◄───────►│  │Envoy  │  │         │
│  │  │Sidecar│  │  流量   │  │Sidecar│  │         │
│  │  └───────┘  │         │  └───────┘  │         │
│  └─────────────┘         └─────────────┘         │
└──────────────────────────────────────────────────┘
```

### 2. 核心组件

#### Sidecar代理模式
```yaml
# Kubernetes Pod配置
apiVersion: v1
kind: Pod
metadata:
  name: order-service
spec:
  containers:
    # 业务容器
    - name: order-app
      image: order-service:v1.0
      ports:
        - containerPort: 8080

    # Sidecar代理容器
    - name: envoy-sidecar
      image: envoyproxy/envoy:v1.24
      ports:
        - containerPort: 15001  # 代理端口
      volumeMounts:
        - name: envoy-config
          mountPath: /etc/envoy

  volumes:
    - name: envoy-config
      configMap:
        name: envoy-config

# 流量流向:
# Client → Sidecar(15001) → App(8080) → Sidecar → Remote Service
```

#### Istio示例配置

##### 虚拟服务 (VirtualService)
```yaml
# 定义路由规则
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service
spec:
  hosts:
    - order-service
  http:
    # 金丝雀发布:90%流量到v1,10%到v2
    - match:
        - headers:
            user-type:
              exact: premium
      route:
        - destination:
            host: order-service
            subset: v2
          weight: 100

    - route:
        - destination:
            host: order-service
            subset: v1
          weight: 90
        - destination:
            host: order-service
            subset: v2
          weight: 10

    # 超时和重试
    timeout: 10s
    retries:
      attempts: 3
      perTryTimeout: 2s
      retryOn: 5xx,reset,connect-failure
```

##### 目标规则 (DestinationRule)
```yaml
# 定义服务的子集和流量策略
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: order-service
spec:
  host: order-service

  # 流量策略
  trafficPolicy:
    # 连接池
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 50
        http2MaxRequests: 100
        maxRequestsPerConnection: 2

    # 负载均衡
    loadBalancer:
      simple: LEAST_REQUEST
      # consistentHash:  # 一致性哈希
      #   httpHeaderName: x-user-id

    # 熔断器
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
      minHealthPercent: 50

  # 定义版本子集
  subsets:
    - name: v1
      labels:
        version: v1

    - name: v2
      labels:
        version: v2
      trafficPolicy:
        # v2版本的特殊策略
        connectionPool:
          tcp:
            maxConnections: 50
```

##### 网关 (Gateway)
```yaml
# 南北向流量入口
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: order-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
    - port:
        number: 80
        name: http
        protocol: HTTP
      hosts:
        - "order.example.com"
      tls:
        httpsRedirect: true  # 重定向到HTTPS

    - port:
        number: 443
        name: https
        protocol: HTTPS
      hosts:
        - "order.example.com"
      tls:
        mode: SIMPLE
        credentialName: order-tls-secret
---
# 绑定VirtualService到Gateway
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-ingress
spec:
  hosts:
    - "order.example.com"
  gateways:
    - order-gateway
  http:
    - match:
        - uri:
            prefix: "/api/orders"
      route:
        - destination:
            host: order-service
            port:
              number: 8080
```

## 四、核心功能

### 1. 流量管理

#### 灰度发布
```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service-canary
spec:
  hosts:
    - order-service
  http:
    # 基于请求头的灰度
    - match:
        - headers:
            canary:
              exact: "true"
      route:
        - destination:
            host: order-service
            subset: v2

    # 默认流量
    - route:
        - destination:
            host: order-service
            subset: v1
```

#### A/B测试
```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service-ab
spec:
  hosts:
    - order-service
  http:
    # Android用户使用v2
    - match:
        - headers:
            user-agent:
              regex: ".*Android.*"
      route:
        - destination:
            host: order-service
            subset: v2

    # iOS用户使用v1
    - match:
        - headers:
            user-agent:
              regex: ".*iPhone.*"
      route:
        - destination:
            host: order-service
            subset: v1
```

#### 超时和重试
```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service-retry
spec:
  hosts:
    - order-service
  http:
    - route:
        - destination:
            host: order-service

      # 超时配置
      timeout: 5s

      # 重试配置
      retries:
        attempts: 3
        perTryTimeout: 2s
        retryOn: 5xx,reset,connect-failure,refused-stream
```

### 2. 安全管理

#### mTLS加密
```yaml
# 命名空间级别启用mTLS
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: default
spec:
  mtls:
    mode: STRICT  # STRICT/PERMISSIVE/DISABLE

---
# 服务级别mTLS
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: order-service-mtls
spec:
  selector:
    matchLabels:
      app: order-service
  mtls:
    mode: STRICT
```

#### 授权策略
```yaml
# 基于角色的访问控制
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: order-service-authz
spec:
  selector:
    matchLabels:
      app: order-service

  # 拒绝规则优先
  action: DENY
  rules:
    - from:
        - source:
            namespaces: ["external"]

---
# 允许规则
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: order-service-allow
spec:
  selector:
    matchLabels:
      app: order-service
  action: ALLOW
  rules:
    # 只允许user-service访问
    - from:
        - source:
            principals: ["cluster.local/ns/default/sa/user-service"]
      to:
        - operation:
            methods: ["GET", "POST"]
            paths: ["/api/orders/*"]

    # 允许管理员访问所有接口
    - when:
        - key: request.auth.claims[role]
          values: ["admin"]
```

#### JWT认证
```yaml
apiVersion: security.istio.io/v1beta1
kind: RequestAuthentication
metadata:
  name: order-jwt
spec:
  selector:
    matchLabels:
      app: order-service
  jwtRules:
    - issuer: "https://auth.example.com"
      jwksUri: "https://auth.example.com/.well-known/jwks.json"
      audiences:
        - "order-service"

---
# 要求JWT认证
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: require-jwt
spec:
  selector:
    matchLabels:
      app: order-service
  action: DENY
  rules:
    - from:
        - source:
            notRequestPrincipals: ["*"]
```

### 3. 可观测性

#### 指标收集
```yaml
# Prometheus配置
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
data:
  prometheus.yml: |
    scrape_configs:
      - job_name: 'istio-mesh'
        kubernetes_sd_configs:
          - role: endpoints
            namespaces:
              names: ['istio-system']

        relabel_configs:
          - source_labels: [__meta_kubernetes_service_name]
            action: keep
            regex: istio-telemetry

      - job_name: 'envoy-stats'
        metrics_path: /stats/prometheus
        kubernetes_sd_configs:
          - role: pod

# 常用指标:
# - istio_requests_total: 请求总数
# - istio_request_duration_milliseconds: 请求延迟
# - istio_request_bytes: 请求大小
# - istio_response_bytes: 响应大小
```

#### 分布式追踪
```yaml
# Jaeger配置
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jaeger
spec:
  template:
    spec:
      containers:
        - name: jaeger
          image: jaegertracing/all-in-one:1.35
          env:
            - name: COLLECTOR_ZIPKIN_HTTP_PORT
              value: "9411"
          ports:
            - containerPort: 16686  # UI
            - containerPort: 14268  # Collector

---
# Istio启用追踪
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  meshConfig:
    enableTracing: true
    defaultConfig:
      tracing:
        sampling: 100  # 采样率100%
        zipkin:
          address: jaeger-collector.istio-system:9411
```

#### 访问日志
```yaml
# 启用访问日志
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: mesh-default
spec:
  accessLogging:
    - providers:
        - name: envoy
      # 日志格式
      format: |
        [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%"
        %RESPONSE_CODE% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT%
        %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)%
        "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%"
        "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%"
```

## 五、实战案例

### 电商系统服务网格部署

```yaml
# 1. 订单服务部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service-v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
      version: v1
  template:
    metadata:
      labels:
        app: order-service
        version: v1
    spec:
      containers:
        - name: order-service
          image: order-service:1.0
          ports:
            - containerPort: 8080

---
# 2. Service定义
apiVersion: v1
kind: Service
metadata:
  name: order-service
spec:
  selector:
    app: order-service
  ports:
    - port: 8080
      targetPort: 8080

---
# 3. 流量管理
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service
spec:
  hosts:
    - order-service
  http:
    - match:
        - uri:
            prefix: "/api/orders"
      route:
        - destination:
            host: order-service
            port:
              number: 8080
      timeout: 10s
      retries:
        attempts: 3
        perTryTimeout: 2s

---
# 4. 熔断配置
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: order-service
spec:
  host: order-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 50
        maxRequestsPerConnection: 2
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 30s
```

## 六、面试高频问题

### 1. 什么是服务网格?它解决了什么问题?

**答案要点**:
- 服务网格是处理服务间通信的基础设施层
- 通过Sidecar代理统一管理网络通信
- 解决微服务通信复杂性、可观测性、安全性问题

### 2. Sidecar模式是什么?

**答案**:
- 在每个服务Pod中注入代理容器
- 代理拦截所有进出流量
- 应用无感知,无需修改代码

### 3. Istio的架构是怎样的?

**控制平面**:
- Pilot: 服务发现和流量管理
- Citadel: 证书管理和安全
- Galley: 配置管理
- Telemetry: 遥测数据收集

**数据平面**:
- Envoy Sidecar: 代理所有流量

### 4. 如何实现金丝雀发布?

**答案**:
```yaml
# 基于流量比例
- route:
    - destination:
        host: order-service
        subset: v1
      weight: 90
    - destination:
        host: order-service
        subset: v2
      weight: 10
```

### 5. 服务网格如何实现熔断?

**答案**:
```yaml
outlierDetection:
  consecutiveErrors: 5  # 连续5次错误
  interval: 30s  # 检测间隔
  baseEjectionTime: 30s  # 驱逐时间
  maxEjectionPercent: 50  # 最多驱逐50%实例
```

### 6. 服务网格和API网关有什么区别?

**API网关**:
- 北向流量(外部→内部)
- 单一入口
- 协议转换、认证授权
- Kong, Nginx

**服务网格**:
- 东西向流量(服务间)
- 分布式代理
- 服务发现、负载均衡、熔断
- Istio, Linkerd

### 7. 服务网格的性能开销如何?

**开销**:
- 延迟增加: 1-2ms(Sidecar代理)
- 资源消耗: 每个Sidecar ~50MB内存
- CPU开销: 5-10%

**优化**:
- 使用HTTP/2
- 调整Envoy参数
- 选择性注入Sidecar

### 8. 何时应该使用服务网格?

**适合**:
- 微服务数量 > 50
- 多语言环境
- 复杂流量管理需求
- Kubernetes环境

**不适合**:
- 服务数量少
- 单体应用
- 性能要求极高
- 团队经验不足

### 9. 服务网格如何实现零信任安全?

**措施**:
- mTLS: 服务间加密通信
- 身份认证: JWT验证
- 授权策略: RBAC控制
- 审计日志: 记录所有访问

### 10. Istio和Linkerd有什么区别?

**Istio**:
- 功能丰富
- 复杂度高
- Envoy代理
- 社区活跃

**Linkerd**:
- 轻量级
- 易于使用
- Rust编写的代理
- 性能更好

## 七、总结

服务网格的关键要点:
1. **Sidecar模式**: 无侵入式代理
2. **统一管理**: 集中配置流量策略
3. **可观测性**: 指标、日志、追踪
4. **安全加固**: mTLS和零信任
5. **云原生**: 适合Kubernetes环境
