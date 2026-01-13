# OKX Finance 撮合引擎架构设计

## 1. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Trading Service                          │
│                                                              │
│  ┌────────────┐      ┌──────────────────────────────────┐  │
│  │  Order     │      │    Matching Engine V2            │  │
│  │  Service   │─────▶│                                   │  │
│  └────────────┘      │  ┌────────────────────────────┐  │  │
│                      │  │   Order Book Manager       │  │  │
│                      │  │  ┌──────────────────────┐  │  │  │
│                      │  │  │   BTC-USDT Book     │  │  │  │
│                      │  │  │   ETH-USDT Book     │  │  │  │
│                      │  │  │   ...               │  │  │  │
│                      │  │  └──────────────────────┘  │  │  │
│                      │  └────────────────────────────┘  │  │
│                      │                                   │  │
│                      │  ┌────────────────────────────┐  │  │
│                      │  │   Matching Queue           │  │  │
│                      │  │  ┌──────────────────────┐  │  │  │
│                      │  │  │   BTC-USDT Queue    │  │  │  │
│                      │  │  │   ETH-USDT Queue    │  │  │  │
│                      │  │  └──────────────────────┘  │  │  │
│                      │  └────────────────────────────┘  │  │
│                      │                                   │  │
│                      │  ┌────────────────────────────┐  │  │
│                      │  │   Thread Pool              │  │  │
│                      │  │   [Thread-1] [Thread-2]... │  │  │
│                      │  └────────────────────────────┘  │  │
│                      └──────────────────────────────────┘  │
│                                  │                          │
│                                  ▼                          │
│                      ┌──────────────────────────────────┐  │
│                      │    Match Listener                │  │
│                      │  - onMatch()                     │  │
│                      │  - onOrderFilled()               │  │
│                      │  - onOrderPartiallyFilled()      │  │
│                      └──────────────────────────────────┘  │
│                                  │                          │
└──────────────────────────────────┼──────────────────────────┘
                                   ▼
                        ┌──────────────────────┐
                        │   Database & Cache   │
                        │  - Order Table       │
                        │  - Trade Table       │
                        │  - Redis Cache       │
                        └──────────────────────┘
```

## 2. 订单簿数据结构

### 2.1 OrderBook 结构

```
OrderBook (BTC-USDT)
├── symbol: "BTC-USDT"
├── bidLevels (TreeMap - 降序)
│   ├── 50100.00 → PriceLevel
│   │   └── orders: [Order1, Order2, Order3]
│   ├── 50099.00 → PriceLevel
│   │   └── orders: [Order4]
│   └── 50098.00 → PriceLevel
│       └── orders: [Order5, Order6]
└── askLevels (TreeMap - 升序)
    ├── 50101.00 → PriceLevel
    │   └── orders: [Order7, Order8]
    ├── 50102.00 → PriceLevel
    │   └── orders: [Order9]
    └── 50103.00 → PriceLevel
        └── orders: [Order10]
```

### 2.2 数据结构特点

| 组件 | 数据结构 | 特性 |
|------|---------|------|
| BidLevels | ConcurrentSkipListMap (降序) | 线程安全、有序、最优买价在首位 |
| AskLevels | ConcurrentSkipListMap (升序) | 线程安全、有序、最优卖价在首位 |
| PriceLevel | LinkedList | FIFO队列、保证时间优先 |
| OrderIndex | ConcurrentHashMap | O(1)快速查找订单 |

## 3. 撮合流程详解

### 3.1 限价买单撮合流程

```
新建限价买单 (BUY 50100 USDT, 数量 1 BTC)
         ↓
   加入撮合队列
         ↓
   获取最优卖价 (50101 USDT)
         ↓
   价格匹配判断: 50100 >= 50101 ?
         ↓ NO
   加入买单订单簿
         ↓
   状态: NEW
```

```
新建限价买单 (BUY 50102 USDT, 数量 1 BTC)
         ↓
   加入撮合队列
         ↓
   获取最优卖价 (50101 USDT)
         ↓
   价格匹配判断: 50102 >= 50101 ?
         ↓ YES
   按 50101 USDT 成交
         ↓
   更新买卖双方订单
         ↓
   生成成交记录
         ↓
   触发 onMatch 事件
         ↓
   状态: FILLED
```

### 3.2 市价单撮合流程

```
新建市价卖单 (SELL, 数量 2 BTC)
         ↓
   加入撮合队列
         ↓
┌─ 循环开始 ─────────────────┐
│  获取最优买价 (50100 USDT) │
│         ↓                  │
│  成交 1 BTC @ 50100       │
│         ↓                  │
│  获取下一最优买价          │
│  (50099 USDT)             │
│         ↓                  │
│  成交 1 BTC @ 50099       │
└─ 循环结束 ─────────────────┘
         ↓
   剩余数量 = 0
         ↓
   状态: FILLED
```

## 4. 核心类关系图

```
┌──────────────────┐
│ MatchingEngineV2 │
└────────┬─────────┘
         │
         │ has-many
         ▼
┌──────────────────┐        has-many      ┌─────────────┐
│    OrderBook     │◀──────────────────────│ PriceLevel  │
└────────┬─────────┘                       └──────┬──────┘
         │                                        │
         │ has-one                                │ has-many
         ▼                                        ▼
┌──────────────────┐                       ┌─────────────┐
│   Statistics     │                       │    Order    │
└──────────────────┘                       └─────────────┘

┌──────────────────┐        listens        ┌─────────────┐
│ MatchingEngineV2 │───────────────────────▶│MatchListener│
└──────────────────┘                       └─────────────┘
         │
         │ produces
         ▼
┌──────────────────┐
│   MatchResult    │
└──────────────────┘
```

## 5. 线程模型

### 5.1 线程池设计

```
ThreadPoolExecutor
├── CorePoolSize: CPU核心数
├── MaxPoolSize: CPU核心数 * 2
├── QueueCapacity: 10000
└── RejectionPolicy: CallerRunsPolicy
```

### 5.2 并发控制

```
交易对1 (BTC-USDT)
  ├── Queue: [Order1, Order2, ...]
  └── Thread-1: 处理该交易对的所有订单

交易对2 (ETH-USDT)
  ├── Queue: [Order3, Order4, ...]
  └── Thread-2: 处理该交易对的所有订单

交易对3 (BNB-USDT)
  ├── Queue: [Order5, Order6, ...]
  └── Thread-3: 处理该交易对的所有订单
```

**优点：**
- 每个交易对独立队列，避免锁竞争
- 同一交易对串行处理，保证撮合顺序
- 不同交易对并行处理，提高吞吐量

## 6. 性能优化策略

### 6.1 内存优化

1. **订单簿分层**
   - 只保留活跃价格层级
   - 定期清理空的价格层级

2. **对象池**
   - 复用MatchResult对象
   - 减少GC压力

3. **批量操作**
   - 批量更新数据库
   - 批量发送通知

### 6.2 算法优化

1. **快速路径**
   ```java
   // 快速判断：没有对手盘直接加入订单簿
   if (oppositeBook.isEmpty()) {
       addToOrderBook(order);
       return;
   }
   ```

2. **价格索引**
   ```java
   // TreeMap的firstKey()操作是O(1)
   BigDecimal bestPrice = bidLevels.firstKey();
   ```

3. **避免深度拷贝**
   - 使用引用传递
   - 只在必要时复制数据

## 7. 容错设计

### 7.1 异常处理

```
try {
    撮合订单
} catch (Exception e) {
    订单状态 → REJECTED
    记录错误日志
    发送告警通知
}
```

### 7.2 数据一致性

1. **订单状态同步**
   ```
   内存订单簿 ←→ 数据库
   (实时)      (异步持久化)
   ```

2. **恢复机制**
   ```
   系统启动
     ↓
   从数据库加载未完成订单
     ↓
   重建订单簿
     ↓
   恢复撮合服务
   ```

## 8. 监控指标

### 8.1 性能指标

| 指标 | 说明 | 阈值 |
|------|------|------|
| 撮合延迟 | 订单提交到撮合完成的时间 | < 1ms |
| 队列深度 | 待撮合订单数量 | < 1000 |
| TPS | 每秒处理订单数 | > 10000 |
| 订单簿深度 | 每个价格层级的订单数 | < 100 |

### 8.2 业务指标

| 指标 | 说明 |
|------|------|
| 成交率 | 成交订单数 / 总订单数 |
| 部分成交率 | 部分成交订单数 / 总订单数 |
| 取消率 | 取消订单数 / 总订单数 |
| 拒绝率 | 拒绝订单数 / 总订单数 |

## 9. 扩展性设计

### 9.1 水平扩展

```
                  ┌─────────────┐
                  │  Load       │
                  │  Balancer   │
                  └──────┬──────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ Engine Node 1 │ │ Engine Node 2 │ │ Engine Node 3 │
│ BTC-USDT     │ │ ETH-USDT     │ │ BNB-USDT     │
│ LTC-USDT     │ │ XRP-USDT     │ │ ADA-USDT     │
└───────────────┘ └───────────────┘ └───────────────┘
```

### 9.2 分片策略

按交易对哈希分片：
```java
int nodeIndex = symbol.hashCode() % nodeCount;
```

## 10. 测试策略

### 10.1 单元测试

- 订单簿增删改查
- 价格层级管理
- 撮合算法正确性

### 10.2 集成测试

- 完整撮合流程
- 事件通知机制
- 数据库同步

### 10.3 压力测试

- 高并发订单提交
- 极端订单簿深度
- 长时间运行稳定性

### 10.4 测试用例示例

```java
@Test
public void testLimitOrderMatching() {
    // 1. 提交卖单
    Order sellOrder = createOrder("BTC-USDT", SELL, LIMIT, "50000", "1");
    engine.submitOrder(sellOrder);

    // 2. 提交买单（价格匹配）
    Order buyOrder = createOrder("BTC-USDT", BUY, LIMIT, "50000", "1");
    engine.submitOrder(buyOrder);

    // 3. 验证结果
    assertEquals(OrderStatus.FILLED, sellOrder.getStatus());
    assertEquals(OrderStatus.FILLED, buyOrder.getStatus());
    assertEquals(new BigDecimal("1"), sellOrder.getExecutedQuantity());
}
```

## 11. 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                     Production                          │
│                                                         │
│  ┌─────────────┐         ┌─────────────┐              │
│  │ API Gateway │────────▶│   Trading   │              │
│  │   (8080)    │         │   Service   │              │
│  └─────────────┘         │   (8083)    │              │
│                          │             │              │
│                          │ ┌─────────┐ │              │
│                          │ │ Matching│ │              │
│                          │ │ Engine  │ │              │
│                          │ └─────────┘ │              │
│                          └──────┬──────┘              │
│                                 │                      │
│                    ┌────────────┴────────────┐        │
│                    ▼                         ▼         │
│              ┌──────────┐              ┌──────────┐   │
│              │  MySQL   │              │  Redis   │   │
│              │  (3306)  │              │  (6379)  │   │
│              └──────────┘              └──────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 12. 总结

OKX Finance 撮合引擎采用了以下关键技术：

1. **高性能数据结构**：ConcurrentSkipListMap + LinkedList
2. **无锁并发**：每个交易对独立队列
3. **价格-时间优先**：标准交易所撮合算法
4. **事件驱动**：解耦撮合和业务逻辑
5. **可监控**：完整的性能和业务指标
6. **可扩展**：支持水平扩展和分片

这是一个生产级的撮合引擎实现，可以支撑中小型交易所的业务需求。
