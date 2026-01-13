# 场景三：延迟队列

## 业务背景
在电商、支付等系统中，经常需要在一段时间后执行某个任务，例如：
- 订单创建后30分钟未支付，自动取消订单
- 外卖订单送达后，1小时自动确认收货
- 优惠券过期前3天，推送提醒消息
- 定时任务调度

## 业务描述
用户下单后，订单状态为"待支付"，需要在30分钟后检查：
- 如果已支付，不处理
- 如果未支付，取消订单，释放库存

直接使用定时任务扫描的问题：
- 每分钟扫描一次数据库，压力大
- 实时性差，最多可能延迟1分钟
- 订单量大时，扫描效率低

## 核心瓶颈

### 1. 定时扫描方案的问题
```java
@Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
public void cancelUnpaidOrder() {
    // 查询30分钟前创建的未支付订单
    List<Order> orders = orderDao.findUnpaidOrders(30);
    for (Order order : orders) {
        cancelOrder(order);
    }
}
```
问题：
- 数据库压力大（每分钟全表扫描）
- 实时性差（最多延迟1分钟）
- 性能随订单量增长而下降

### 2. 精确延迟时间
需要支持秒级甚至毫秒级的延迟精度

### 3. 高可用
服务重启时，延迟任务不能丢失

### 4. 扩展性
支持水平扩展，多实例消费

## 技术实现方案

### 方案一：基于Redis的Sorted Set

#### 实现原理
1. 使用Redis的Sorted Set（有序集合）
2. Score设置为任务执行时间戳
3. 定时任务扫描到期的任务

#### 核心代码
```java
// 添加延迟任务
ZADD delay_queue ${executeTime} ${taskId}

// 扫描到期任务
ZRANGEBYSCORE delay_queue 0 ${currentTime}

// 删除已处理任务
ZREM delay_queue ${taskId}
```

#### 优势
- 实现简单
- 性能高（Redis单机10万QPS）
- 支持持久化

#### 劣势
- 需要定时扫描（1秒1次），有轻微延迟
- 多实例消费需要加锁

### 方案二：基于RabbitMQ的延迟队列

#### 实现原理
利用RabbitMQ的死信队列（DLX）+ TTL：
1. 消息发送到延迟交换机
2. 消息带TTL（过期时间）
3. 消息过期后进入死信队列
4. 消费者监听死信队列

#### 优势
- 高可用（RabbitMQ集群）
- 自动推送，无需轮询
- 支持多实例消费

#### 劣势
- 配置复杂
- 延迟时间固定（需要预先创建多个队列）

### 方案三：基于RocketMQ/Pulsar的延迟消息

#### 实现原理
直接支持延迟消息：
```java
// RocketMQ支持18个延迟级别
// 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
message.setDelayTimeLevel(3); // 10秒
```

#### 优势
- 开箱即用
- 高性能、高可用

#### 劣势
- 延迟级别固定，不支持任意时间

### 方案四：时间轮算法（内存实现）

#### 实现原理
类似时钟，将时间划分为槽位（Slot），每个槽位存储该时刻需要执行的任务。

#### 优势
- 性能极高（O(1)时间复杂度）
- 精度高（毫秒级）

#### 劣势
- 内存存储，重启丢失
- 单机方案，不支持分布式

### 方案对比

| 方案 | 延迟精度 | 性能 | 可靠性 | 复杂度 | 适用场景 |
|------|---------|------|--------|--------|---------|
| Redis Sorted Set | 秒级 | 高 | 中 | 低 | 通用场景 |
| RabbitMQ死信队列 | 秒级 | 中 | 高 | 中 | 延迟时间固定 |
| RocketMQ延迟消息 | 固定级别 | 高 | 高 | 低 | 延迟级别固定 |
| 时间轮算法 | 毫秒级 | 极高 | 低 | 中 | 单机、高性能 |

### 推荐方案
- **通用场景**：Redis Sorted Set（简单、高效）
- **高可用场景**：RabbitMQ死信队列
- **高性能场景**：RocketMQ延迟消息
- **单机、高精度场景**：时间轮算法

## 关键代码实现

见以下文件：
- `RedisDelayQueue.java` - 基于Redis Sorted Set的延迟队列
- `TimeWheelDelayQueue.java` - 基于时间轮算法的延迟队列
- `RabbitMQDelayQueueExample.java` - 基于RabbitMQ的延迟队列示例

## 实际应用案例

### 案例1：订单超时取消
```java
// 创建订单时，添加延迟任务
Order order = createOrder(userId, productId);
delayQueue.offer(new CancelOrderTask(order.getId()), 30, TimeUnit.MINUTES);

// 30分钟后，任务被执行
public void handleCancelOrderTask(CancelOrderTask task) {
    Order order = orderDao.getById(task.getOrderId());
    if (order.getStatus() == UNPAID) {
        cancelOrder(order);
    }
}
```

### 案例2：延迟重试
```java
// 调用第三方API失败，5秒后重试
if (!callThirdPartyApi()) {
    delayQueue.offer(new RetryTask(apiRequest), 5, TimeUnit.SECONDS);
}
```
