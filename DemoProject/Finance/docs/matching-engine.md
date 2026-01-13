# 撮合引擎技术文档

## 概述

OKX Finance 撮合引擎是一个高性能、专业级的订单撮合系统，支持多交易对并发撮合，采用价格-时间优先原则。

## 核心架构

### 1. 系统组件

```
MatchingEngineV2 (撮合引擎核心)
├── OrderBook (订单簿)
│   ├── BidLevels (买单价格层级)
│   └── AskLevels (卖单价格层级)
├── MatchingQueue (撮合队列)
├── MatchListener (撮合监听器)
└── EngineMonitor (性能监控)
```

### 2. 核心类说明

#### OrderBook (订单簿)
- 管理单个交易对的所有订单
- 买单按价格降序排列（价格从高到低）
- 卖单按价格升序排列（价格从低到高）
- 支持快速查询最优买卖价

#### PriceLevel (价格层级)
- 管理同一价格的所有订单
- 使用队列保证时间优先
- 维护该价格的总挂单量

#### MatchResult (撮合结果)
- 记录每次撮合的详细信息
- 包含买卖双方订单、成交价格、成交数量

## 撮合算法

### 1. 价格-时间优先原则

**价格优先：**
- 买单：价格高的优先成交
- 卖单：价格低的优先成交

**时间优先：**
- 同一价格的订单，先提交的先成交

### 2. 限价单撮合流程

```
1. 新订单进入撮合队列
2. 检查对手盘最优价格
3. 判断价格是否匹配：
   - 买单：买价 >= 卖价
   - 卖单：卖价 <= 买价
4. 如果匹配：
   - 按maker价格成交
   - 更新双方订单状态
   - 生成成交记录
   - 从订单簿移除已完全成交的订单
5. 如果不匹配或部分成交：
   - 将剩余数量加入订单簿
```

### 3. 市价单撮合流程

```
1. 新订单进入撮合队列
2. 直接与对手盘最优价格成交
3. 按对手盘价格成交
4. 持续成交直到：
   - 订单完全成交
   - 对手盘没有挂单
5. 市价单不加入订单簿
```

## 订单状态流转

```
NEW (新建)
  ↓
PARTIALLY_FILLED (部分成交)
  ↓
FILLED (完全成交)

或

NEW → CANCELED (取消)
NEW → REJECTED (拒绝)
```

## 性能特性

### 1. 高性能设计

- **内存撮合**：所有订单簿数据保存在内存中
- **并发处理**：每个交易对独立队列和线程
- **无锁设计**：使用ConcurrentSkipListMap实现无锁订单簿
- **异步处理**：订单提交和撮合解耦

### 2. 数据结构选择

| 组件 | 数据结构 | 时间复杂度 | 原因 |
|------|---------|-----------|------|
| 价格层级 | TreeMap | O(log n) | 需要有序查找最优价格 |
| 同价订单 | LinkedList | O(1) | FIFO队列，时间优先 |
| 订单索引 | HashMap | O(1) | 快速查找订单 |

### 3. 性能指标

- **撮合延迟**：< 1ms （内存操作）
- **吞吐量**：> 10,000 TPS/交易对
- **订单簿深度**：支持100档深度实时查询

## 事件通知机制

### MatchListener 接口

```java
public interface MatchListener {
    // 撮合成功
    void onMatch(MatchResult matchResult);

    // 订单完全成交
    void onOrderFilled(String orderId);

    // 订单部分成交
    void onOrderPartiallyFilled(String orderId, Trade trade);

    // 订单取消
    void onOrderCanceled(String orderId);
}
```

### 事件流转

```
订单提交 → 撮合处理 → 生成撮合结果 → 触发事件 → 更新数据库 → 发送通知
```

## 使用示例

### 1. 提交限价单

```java
Order order = new Order();
order.setSymbol("BTC-USDT");
order.setOrderType(OrderType.LIMIT);
order.setSide(OrderSide.BUY);
order.setPrice(new BigDecimal("50000"));
order.setQuantity(new BigDecimal("0.1"));

matchingEngine.submitOrder(order);
```

### 2. 提交市价单

```java
Order order = new Order();
order.setSymbol("BTC-USDT");
order.setOrderType(OrderType.MARKET);
order.setSide(OrderSide.SELL);
order.setQuantity(new BigDecimal("0.1"));

matchingEngine.submitOrder(order);
```

### 3. 取消订单

```java
matchingEngine.cancelOrder(order);
```

### 4. 查询深度

```java
Map<String, Object> depth = matchingEngine.getOrderBookDepth("BTC-USDT", 20);
```

## 配置参数

```yaml
matching:
  engine:
    thread-pool-size: 8          # 撮合线程数
    queue-capacity: 10000         # 订单队列容量
    taker-fee-rate: 0.001        # Taker手续费率 0.1%
    maker-fee-rate: 0.0008       # Maker手续费率 0.08%
    enable-depth-cache: true      # 启用深度缓存
    enable-match-log: true        # 启用撮合日志
    max-depth: 100               # 最大订单簿深度
```

## 监控指标

### EngineMonitor 提供的指标

- 总订单数
- 成交订单数
- 取消订单数
- 拒绝订单数
- 总成交笔数
- 平均撮合延迟
- 最大撮合延迟
- 最小撮合延迟

### 查询监控信息

```java
String stats = engineMonitor.getStatistics();
System.out.println(stats);
```

## 扩展性

### 1. 支持新订单类型

可以通过扩展 `OrderType` 和在撮合引擎中添加对应的处理逻辑来支持新订单类型：

- 止损单 (STOP_LOSS)
- 止盈单 (TAKE_PROFIT)
- 冰山订单 (ICEBERG)
- 时间加权订单 (TWAP)

### 2. 分布式扩展

当前撮合引擎为单机版本，可以通过以下方式实现分布式：

1. **按交易对分片**：不同交易对路由到不同服务器
2. **使用消息队列**：Kafka/RocketMQ 作为订单队列
3. **分布式锁**：Redis/ZooKeeper 保证并发安全
4. **状态同步**：通过日志或数据库同步订单状态

## 安全性

### 1. 价格保护

- 限制订单价格波动范围
- 防止错误价格导致的异常成交

### 2. 数量保护

- 限制单笔订单最大数量
- 限制用户持仓量

### 3. 频率限制

- 限制订单提交频率
- 防止恶意刷单

## 故障处理

### 1. 订单持久化

所有订单变更都会同步到数据库，保证数据不丢失。

### 2. 恢复机制

系统重启时，可以从数据库重建订单簿：

1. 加载所有未完成订单
2. 按价格-时间排序
3. 重建订单簿结构

### 3. 异常处理

- 撮合异常：订单标记为REJECTED
- 数据库异常：重试机制
- 内存溢出：限制订单簿深度

## 性能优化建议

1. **内存管理**：定期清理已完成订单
2. **批量更新**：订单状态批量更新到数据库
3. **缓存预热**：启动时预加载活跃交易对
4. **监控告警**：设置性能指标阈值告警

## 未来规划

- [ ] 支持期货合约撮合
- [ ] 支持杠杆交易
- [ ] 撮合引擎集群化
- [ ] 实时推送WebSocket
- [ ] 机器学习预测最优价格
