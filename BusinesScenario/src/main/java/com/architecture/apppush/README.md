# 场景八：APP在线状态检测与智能推送

## 业务背景
APP消息推送系统中，需要根据用户的在线状态决定推送方式：
- **在线**：通过长连接（WebSocket/MQTT）实时推送
- **离线**：通过第三方推送服务（极光/友盟/FCM）推送到系统通知栏

这样可以：
1. 避免重复推送（在线消息+离线通知）
2. 节省推送成本（第三方推送按量收费）
3. 提升用户体验（在线时无需系统通知打扰）

## 业务描述
某社交APP的消息推送场景：
- 用户A给用户B发送聊天消息
- 如果B在线：通过WebSocket直接推送，B立即收到消息
- 如果B离线：调用极光推送，B手机收到系统通知

问题：如何准确判断用户是否在线？

## 核心瓶颈

### 1. 在线状态的定义
- APP在前台运行？
- APP在后台运行？
- WebSocket连接是否建立？
- 多端登录如何处理？

### 2. 状态的实时性
- 网络波动导致的短暂断线
- APP切到后台
- 手机锁屏
- 系统杀进程

### 3. 分布式环境
- 多个WebSocket服务器节点
- 用户可能连接到不同的节点
- 状态需要全局共享

### 4. 心跳检测
- 心跳间隔设置（太短浪费流量，太长不够实时）
- 网络抖动处理
- 心跳超时判定

## 技术实现方案

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         客户端（APP）                          │
│  ┌──────────┐        ┌──────────┐        ┌──────────┐       │
│  │  登录    │───────▶│建立长连接 │───────▶│ 心跳保活  │       │
│  └──────────┘        └──────────┘        └──────────┘       │
│       │                    │                    │            │
└───────│────────────────────│────────────────────│────────────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────┐
│                      后端服务集群                              │
│  ┌──────────┐        ┌──────────┐        ┌──────────┐       │
│  │ 认证服务  │       │ WebSocket │       │ 心跳监听  │       │
│  └──────────┘        │   服务    │       └──────────┘       │
│       │              └──────────┘              │            │
│       │                    │                    │            │
│       └────────────────────┼────────────────────┘            │
│                            ▼                                 │
│                     ┌─────────────┐                          │
│                     │   Redis     │                          │
│                     │ 在线状态存储 │                          │
│                     └─────────────┘                          │
│                            ▲                                 │
│                            │                                 │
│                     ┌─────────────┐                          │
│                     │  推送服务    │                          │
│                     │1.查询在线状态│                          │
│                     │2.决定推送方式│                          │
│                     └─────────────┘                          │
│                       │          │                           │
└───────────────────────│──────────│───────────────────────────┘
                        │          │
                ┌───────┘          └───────┐
                ▼                          ▼
        ┌──────────────┐          ┌──────────────┐
        │ WebSocket推送 │          │ 极光/FCM推送  │
        │  (在线时)     │          │  (离线时)     │
        └──────────────┘          └──────────────┘
```

### 方案一：基于WebSocket长连接 + Redis（推荐）

#### 实现流程

**1. 客户端登录建立长连接**
```
APP启动/用户登录
    ↓
建立WebSocket连接（携带token）
    ↓
服务端验证token
    ↓
写入Redis：online:userId = {serverId, connectTime, deviceId}
    ↓
连接建立成功
```

**2. 心跳保活机制**
```
客户端：每30秒发送心跳包
    ↓
服务端：收到心跳，更新Redis过期时间（60秒）
    ↓
如果60秒内无心跳，Redis自动过期，判定为离线
```

**3. 推送时判断在线状态**
```
推送服务接收到消息
    ↓
查询Redis：GET online:userId
    ↓
存在 → 在线：发送到对应的WebSocket服务器
    ↓
不存在 → 离线：调用极光推送
```

#### 数据结构设计

**Redis Key设计**
```
# 用户在线状态
Key: online:user:{userId}
Value: {
    "serverId": "ws-server-01",     # 连接的服务器ID
    "connectTime": 1704038400000,   # 连接时间戳
    "deviceId": "device123",        # 设备ID
    "platform": "android",          # 平台
    "appVersion": "1.0.0"          # APP版本
}
TTL: 60秒（心跳间隔30秒，超时时间60秒）

# 服务器维度的在线用户集合（用于服务器下线时批量处理）
Key: online:server:{serverId}
Value: Set<userId>
TTL: 无

# 用户的多端登录
Key: online:user:devices:{userId}
Value: Set<deviceId>
TTL: 无
```

### 方案二：基于MQTT

#### 特点
- MQTT是专为IoT设计的轻量级协议
- 内置心跳机制（KeepAlive）
- 支持QoS质量等级
- 适合弱网环境

#### 实现
使用MQTT Broker（如EMQ X、Mosquitto）
- 客户端订阅主题：`user/{userId}/message`
- 服务端判断在线：检查MQTT Broker的连接状态
- 推送消息：发布到对应主题

### 方案三：基于长轮询（兼容方案）

适用于不支持WebSocket的环境
- 客户端定时请求：`/api/message/poll?userId=xxx`
- 服务端hang住请求30秒，有消息立即返回
- 判断在线：Redis记录最后一次轮询时间

## 关键技术点

### 1. 心跳机制设计

**客户端（伪代码）**
```java
// 每30秒发送一次心跳
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    webSocket.send(new HeartbeatMessage());
}, 0, 30, TimeUnit.SECONDS);
```

**服务端**
```java
// 收到心跳，更新Redis
@OnMessage("heartbeat")
public void onHeartbeat(String userId) {
    // 更新在线状态，重置过期时间
    redisTemplate.expire("online:user:" + userId, 60, TimeUnit.SECONDS);
}
```

### 2. 连接断开处理

**主动断开**
```java
@OnClose
public void onClose(String userId) {
    // 删除Redis中的在线状态
    redisTemplate.delete("online:user:" + userId);
}
```

**异常断开**
- Redis TTL自动过期
- 无需额外处理

### 3. 多端登录处理

**互踢策略**
```java
// 新设备登录，踢掉旧设备
String oldDeviceId = redis.get("online:user:" + userId);
if (oldDeviceId != null) {
    // 向旧设备发送踢出通知
    sendKickoutMessage(oldDeviceId);
}
redis.set("online:user:" + userId, newDeviceId);
```

**多端同时在线**
```java
// 允许多端登录（手机+平板+PC）
redis.sadd("online:user:devices:" + userId, deviceId);

// 推送时，向所有设备推送
Set<String> devices = redis.smembers("online:user:devices:" + userId);
for (String device : devices) {
    sendToDevice(device, message);
}
```

### 4. 推送优先级

```java
public void pushMessage(String userId, Message message) {
    // 1. 查询在线状态
    boolean online = isUserOnline(userId);

    if (online) {
        // 2. 在线：WebSocket推送
        webSocketServer.sendToUser(userId, message);
    } else {
        // 3. 离线：极光推送
        jpushClient.push(userId, message);
    }
}
```

## 边界情况处理

### 1. 网络抖动

**问题**：WiFi切4G时短暂断线

**方案**：缓冲期机制
```java
// WebSocket断开时，不立即删除在线状态，而是延迟5秒
@OnClose
public void onClose(String userId) {
    scheduler.schedule(() -> {
        // 5秒后再检查，如果没有重连，才删除
        if (!isReconnected(userId)) {
            redis.delete("online:user:" + userId);
        }
    }, 5, TimeUnit.SECONDS);
}
```

### 2. APP切后台

**问题**：iOS切后台，WebSocket可能被系统挂起

**方案一**：后台保活（iOS有限制）
```swift
// iOS后台保活
UIApplication.shared.beginBackgroundTask {
    // 保持连接
}
```

**方案二**：标记为"后台在线"
```java
// Redis存储状态
{
    "status": "background",  // foreground/background
    "lastActiveTime": 1704038400000
}

// 推送时决策
if (status == "foreground") {
    // 前台：WebSocket推送
} else if (status == "background") {
    // 后台：发送静默推送（iOS APNs静默推送）
} else {
    // 离线：普通推送
}
```

### 3. 服务器重启

**问题**：WebSocket服务器重启，所有连接断开

**方案**：自动重连机制
```java
// 客户端检测到断开，自动重连
@OnClose
public void onClose() {
    reconnect();
}

private void reconnect() {
    // 指数退避重连：1s, 2s, 4s, 8s...
    for (int i = 0; i < 5; i++) {
        Thread.sleep((long) Math.pow(2, i) * 1000);
        if (tryConnect()) {
            break;
        }
    }
}
```

## 性能优化

### 1. 批量查询在线状态

```java
// 批量推送时，一次性查询所有用户状态
List<String> userIds = Arrays.asList("user1", "user2", "user3");

// 使用Redis Pipeline
Pipeline pipeline = redis.pipelined();
Map<String, Response<String>> responses = new HashMap<>();
for (String userId : userIds) {
    responses.put(userId, pipeline.get("online:user:" + userId));
}
pipeline.sync();

// 分类推送
List<String> onlineUsers = new ArrayList<>();
List<String> offlineUsers = new ArrayList<>();
for (String userId : userIds) {
    if (responses.get(userId).get() != null) {
        onlineUsers.add(userId);
    } else {
        offlineUsers.add(userId);
    }
}

// 批量推送
webSocketServer.batchSend(onlineUsers, message);
jpushClient.batchPush(offlineUsers, message);
```

### 2. 本地缓存

```java
// 使用Caffeine缓存在线状态（1秒过期）
Cache<String, Boolean> onlineCache = Caffeine.newBuilder()
    .expireAfterWrite(1, TimeUnit.SECONDS)
    .build();

public boolean isOnline(String userId) {
    return onlineCache.get(userId, key -> {
        return redis.exists("online:user:" + userId);
    });
}
```

## 监控指标

```
1. 在线用户数：redis.dbsize()
2. 心跳丢失率：(心跳超时次数 / 总心跳次数) * 100%
3. 推送成功率：(成功推送数 / 总推送数) * 100%
4. 平均推送延迟：消息发送时间 - 消息生成时间
5. WebSocket连接数：每个服务器节点的连接数
```

## 关键代码实现

见以下文件：
- `OnlineStatusManager.java` - 在线状态管理服务
- `WebSocketServer.java` - WebSocket服务器
- `HeartbeatMonitor.java` - 心跳监控
- `SmartPushService.java` - 智能推送服务

## 总结

| 方案 | 优势 | 劣势 | 适用场景 |
|------|------|------|---------|
| WebSocket + Redis | 实时性高、实现简单 | 需要维护长连接 | 社交APP、IM |
| MQTT | 协议轻量、弱网优化 | 需要额外Broker | IoT、消息推送 |
| 长轮询 | 兼容性好 | 性能较差 | 兼容旧系统 |

**推荐方案**：WebSocket + Redis + 心跳机制
