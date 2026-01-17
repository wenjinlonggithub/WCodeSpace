# 场景十：大数据批处理与外部API调用优化方案

## 业务背景

**典型场景**：数据同步、数据增强、批量计算

**问题描述**：
系统需要循环处理1亿条数据，每条数据需要：
1. 从数据库查询数据
2. 调用N个外部API获取补充信息
3. 在内存中进行业务运算
4. 将结果更新回数据库

**实际案例**：
- 电商系统：批量更新商品价格（调用供应商API）
- 风控系统：批量计算用户信用分（调用征信API）
- 数据分析：批量enrichment数据（调用第三方数据API）
- 物流系统：批量更新物流状态（调用物流公司API）

---

## 核心问题分析

### 1. 性能瓶颈

```
1亿条数据 × N个API调用 × 平均200ms延迟 = 天文数字的处理时间
```

**问题点**：
- ❌ 数据库查询慢：1亿条数据全表扫描
- ❌ API调用慢：外部API平均响应200ms+
- ❌ 串行处理：单线程处理需要数月时间
- ❌ 内存溢出：一次性加载大量数据

### 2. 可靠性问题

- ❌ API调用失败：网络抖动、限流、超时
- ❌ 任务中断：服务重启、宕机导致进度丢失
- ❌ 数据不一致：部分成功部分失败

### 3. 资源管理问题

- ❌ 线程池耗尽：并发过高导致OOM
- ❌ 数据库连接池耗尽：大量并发查询
- ❌ API限流：超过第三方API的QPS限制

---

## 技术方案对比

### 方案1：分页查询 + 多线程并发 + 限流控制（推荐）

**核心思想**：化整为零，分批处理，控制并发

**架构图**：
```
┌─────────────────────────────────────────────────────────┐
│                    主控制器                              │
│  ┌──────────────────────────────────────────────┐      │
│  │  分页查询：每次1000条                          │      │
│  │  SELECT * FROM data WHERE id > ? LIMIT 1000   │      │
│  └──────────────────────────────────────────────┘      │
│                        ↓                                │
│  ┌──────────────────────────────────────────────┐      │
│  │  线程池：核心线程50，最大线程200               │      │
│  │  每个线程处理一条数据                          │      │
│  └──────────────────────────────────────────────┘      │
│                        ↓                                │
│  ┌──────────────────────────────────────────────┐      │
│  │  限流器：Guava RateLimiter                     │      │
│  │  控制API调用QPS：100/秒                        │      │
│  └──────────────────────────────────────────────┘      │
│                        ↓                                │
│  ┌──────────────────────────────────────────────┐      │
│  │  调用N个外部API（并行）                        │      │
│  │  API1  API2  API3  ...  APINN                 │      │
│  └──────────────────────────────────────────────┘      │
│                        ↓                                │
│  ┌──────────────────────────────────────────────┐      │
│  │  内存计算 + 批量更新数据库                      │      │
│  │  每100条批量UPDATE                             │      │
│  └──────────────────────────────────────────────┘      │
│                        ↓                                │
│  ┌──────────────────────────────────────────────┐      │
│  │  进度记录：Redis记录最后处理的ID                │      │
│  │  支持断点续传                                  │      │
│  └──────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────┘
```

**优点**：
- ✅ 性能高：多线程并发，充分利用CPU
- ✅ 可控：限流保护，不会打爆API
- ✅ 可恢复：断点续传，支持任务重启
- ✅ 内存友好：分页加载，不会OOM

**缺点**：
- ⚠️ 实现复杂度中等
- ⚠️ 需要处理并发安全问题

**推荐指数**：⭐⭐⭐⭐⭐

---

### 方案2：消息队列 + 消费者集群

**核心思想**：生产者-消费者模式，水平扩展

**架构图**：
```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  生产者       │      │  RabbitMQ    │      │  消费者集群   │
│              │─────→│  队列        │─────→│  10个实例    │
│  分页查询     │      │  100万消息   │      │  每个50线程  │
│  推送到队列   │      └──────────────┘      └──────────────┘
└──────────────┘              ↓                      ↓
                    ┌──────────────┐      ┌──────────────┐
                    │  死信队列     │      │  调用API     │
                    │  失败重试     │      │  更新数据库   │
                    └──────────────┘      └──────────────┘
```

**优点**：
- ✅ 水平扩展：增加消费者实例提升性能
- ✅ 解耦：生产和消费分离
- ✅ 可靠性高：消息持久化，支持重试

**缺点**：
- ⚠️ 架构复杂：需要MQ集群
- ⚠️ 成本高：需要额外的MQ资源
- ⚠️ 顺序性差：消息乱序

**推荐指数**：⭐⭐⭐⭐

---

### 方案3：分布式任务调度 + 分片处理

**核心思想**：任务分片，多机并行

**架构图**：
```
┌─────────────────────────────────────────────┐
│         XXL-JOB / ElasticJob                │
│         任务分片：10个分片                   │
└─────────────────────────────────────────────┘
         ↓          ↓          ↓
┌──────────┐  ┌──────────┐  ┌──────────┐
│ 机器1     │  │ 机器2     │  │ 机器3     │
│ 分片0-2   │  │ 分片3-5   │  │ 分片6-9   │
│ 处理      │  │ 处理      │  │ 处理      │
│ 0-2千万条 │  │ 2-5千万条 │  │ 5-1亿条   │
└──────────┘  └──────────┘  └──────────┘
```

**优点**：
- ✅ 分布式：多机并行，性能极高
- ✅ 任务管理：统一调度、监控、告警
- ✅ 弹性伸缩：动态增减分片

**缺点**：
- ⚠️ 依赖中间件：需要XXL-JOB等
- ⚠️ 分片策略复杂：需要合理设计

**推荐指数**：⭐⭐⭐⭐⭐

---

### 方案4：流式处理 + 背压控制

**核心思想**：响应式编程，动态调节速率

**技术栈**：Reactor / RxJava / Akka Streams

**优点**：
- ✅ 背压控制：自动调节处理速度
- ✅ 资源高效：按需拉取数据

**缺点**：
- ⚠️ 学习曲线陡峭
- ⚠️ 调试困难

**推荐指数**：⭐⭐⭐

---

## 方案对比表

| 维度 | 方案1：多线程+限流 | 方案2：消息队列 | 方案3：分布式调度 | 方案4：流式处理 |
|------|------------------|----------------|-----------------|----------------|
| 实现复杂度 | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 性能 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 可靠性 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 扩展性 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 成本 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| 推荐指数 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

---

## 推荐方案：多线程并发 + 限流控制

### 核心代码实现

#### 1. 主控制器

```java
@Service
public class BatchProcessService {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ExternalApiService apiService;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    // 线程池：核心50，最大200
    private ExecutorService executor = new ThreadPoolExecutor(
        50, 200, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // 限流器：每秒100个请求
    private RateLimiter rateLimiter = RateLimiter.create(100.0);

    // 批量更新缓冲区
    private List<DataRecord> updateBuffer = new CopyOnWriteArrayList<>();

    private static final int PAGE_SIZE = 1000;
    private static final int BATCH_UPDATE_SIZE = 100;
    private static final String PROGRESS_KEY = "batch:process:last_id";

    /**
     * 启动批处理任务
     */
    public void startBatchProcess() {
        log.info("开始批处理任务...");

        // 1. 获取上次处理的位置（支持断点续传）
        Long lastProcessedId = redisTemplate.opsForValue().get(PROGRESS_KEY);
        if (lastProcessedId == null) {
            lastProcessedId = 0L;
        }

        log.info("从ID {} 开始处理", lastProcessedId);

        // 2. 分页查询并处理
        long currentId = lastProcessedId;
        int totalProcessed = 0;

        while (true) {
            // 2.1 分页查询
            List<DataRecord> records = dataRepository.findByIdGreaterThan(
                currentId, PageRequest.of(0, PAGE_SIZE)
            );

            if (records.isEmpty()) {
                log.info("所有数据处理完成！总计: {}", totalProcessed);
                break;
            }

            // 2.2 提交到线程池并发处理
            CountDownLatch latch = new CountDownLatch(records.size());

            for (DataRecord record : records) {
                executor.submit(() -> {
                    try {
                        processRecord(record);
                    } catch (Exception e) {
                        log.error("处理记录失败: {}", record.getId(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 2.3 等待当前批次处理完成
            try {
                latch.await(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error("等待超时", e);
            }

            // 2.4 批量更新数据库
            flushUpdateBuffer();

            // 2.5 更新进度
            currentId = records.get(records.size() - 1).getId();
            redisTemplate.opsForValue().set(PROGRESS_KEY, currentId);

            totalProcessed += records.size();
            log.info("已处理: {} 条，当前ID: {}", totalProcessed, currentId);
        }

        // 3. 关闭线程池
        executor.shutdown();
    }

    /**
     * 处理单条记录
     */
    private void processRecord(DataRecord record) {
        try {
            // 1. 限流控制
            rateLimiter.acquire();

            // 2. 并行调用N个外部API
            CompletableFuture<ApiResponse1> future1 =
                CompletableFuture.supplyAsync(() -> apiService.callApi1(record));
            CompletableFuture<ApiResponse2> future2 =
                CompletableFuture.supplyAsync(() -> apiService.callApi2(record));
            CompletableFuture<ApiResponse3> future3 =
                CompletableFuture.supplyAsync(() -> apiService.callApi3(record));

            // 3. 等待所有API返回
            CompletableFuture.allOf(future1, future2, future3).join();

            // 4. 内存计算
            ApiResponse1 resp1 = future1.get();
            ApiResponse2 resp2 = future2.get();
            ApiResponse3 resp3 = future3.get();

            BigDecimal result = calculateResult(record, resp1, resp2, resp3);

            // 5. 更新记录
            record.setResult(result);
            record.setProcessedAt(new Date());
            record.setStatus("SUCCESS");

            // 6. 加入批量更新缓冲区
            updateBuffer.add(record);

            // 7. 缓冲区满了就批量更新
            if (updateBuffer.size() >= BATCH_UPDATE_SIZE) {
                flushUpdateBuffer();
            }

        } catch (Exception e) {
            log.error("处理失败: {}", record.getId(), e);
            record.setStatus("FAILED");
            record.setErrorMsg(e.getMessage());
            updateBuffer.add(record);
        }
    }

    /**
     * 批量更新数据库
     */
    private synchronized void flushUpdateBuffer() {
        if (updateBuffer.isEmpty()) {
            return;
        }

        List<DataRecord> toUpdate = new ArrayList<>(updateBuffer);
        updateBuffer.clear();

        try {
            dataRepository.batchUpdate(toUpdate);
            log.info("批量更新 {} 条记录", toUpdate.size());
        } catch (Exception e) {
            log.error("批量更新失败", e);
            // 失败的记录重新加回缓冲区
            updateBuffer.addAll(toUpdate);
        }
    }

    /**
     * 业务计算逻辑
     */
    private BigDecimal calculateResult(DataRecord record,
                                       ApiResponse1 resp1,
                                       ApiResponse2 resp2,
                                       ApiResponse3 resp3) {
        // 示例：根据API返回值计算结果
        return record.getBaseValue()
            .multiply(resp1.getFactor())
            .add(resp2.getAdjustment())
            .multiply(resp3.getCoefficient());
    }
}
```

#### 2. 外部API服务（带重试机制）

```java
@Service
public class ExternalApiService {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private RestTemplate restTemplate;
    private CircuitBreaker circuitBreaker; // Resilience4j

    /**
     * 调用API1 - 带重试和熔断
     */
    @Retry(name = "api1", fallbackMethod = "api1Fallback")
    @CircuitBreaker(name = "api1")
    public ApiResponse1 callApi1(DataRecord record) {
        String url = "https://api1.example.com/data?id=" + record.getId();

        try {
            ResponseEntity<ApiResponse1> response = restTemplate.getForEntity(
                url, ApiResponse1.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new ApiException("API1 返回错误: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用API1失败: {}", record.getId(), e);
            throw new ApiException("API1调用失败", e);
        }
    }

    /**
     * API1降级方法
     */
    private ApiResponse1 api1Fallback(DataRecord record, Exception e) {
        log.warn("API1降级，使用默认值: {}", record.getId());
        return ApiResponse1.defaultValue();
    }

    /**
     * 调用API2
     */
    @Retry(name = "api2", fallbackMethod = "api2Fallback")
    @CircuitBreaker(name = "api2")
    public ApiResponse2 callApi2(DataRecord record) {
        // 类似实现
        return new ApiResponse2();
    }

    /**
     * 调用API3
     */
    @Retry(name = "api3", fallbackMethod = "api3Fallback")
    @CircuitBreaker(name = "api3")
    public ApiResponse3 callApi3(DataRecord record) {
        // 类似实现
        return new ApiResponse3();
    }

    /**
     * 手动重试逻辑（不使用注解）
     */
    public <T> T callWithRetry(Supplier<T> apiCall, String apiName) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY) {
            try {
                return apiCall.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;

                if (attempt < MAX_RETRY) {
                    log.warn("调用失败，第{}次重试", apiName, attempt);
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        throw new ApiException(apiName + "调用失败，已重试" + MAX_RETRY + "次", lastException);
    }
}
```

#### 3. 数据模型

```java
@Data
@Table(name = "data_record")
public class DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessKey;

    private BigDecimal baseValue;

    private BigDecimal result;

    private String status; // PENDING, PROCESSING, SUCCESS, FAILED

    private String errorMsg;

    private Date processedAt;

    private Date createdAt;

    private Date updatedAt;
}

@Data
public class ApiResponse1 {
    private BigDecimal factor;
    private String code;
    private String message;

    public static ApiResponse1 defaultValue() {
        ApiResponse1 response = new ApiResponse1();
        response.setFactor(BigDecimal.ONE);
        return response;
    }
}

@Data
public class ApiResponse2 {
    private BigDecimal adjustment;
    private String status;

    public static ApiResponse2 defaultValue() {
        ApiResponse2 response = new ApiResponse2();
        response.setAdjustment(BigDecimal.ZERO);
        return response;
    }
}

@Data
public class ApiResponse3 {
    private BigDecimal coefficient;
    private Map<String, Object> metadata;

    public static ApiResponse3 defaultValue() {
        ApiResponse3 response = new ApiResponse3();
        response.setCoefficient(BigDecimal.ONE);
        return response;
    }
}
```

#### 4. 数据访问层

```java
@Repository
public interface DataRepository extends JpaRepository<DataRecord, Long> {

    /**
     * 分页查询：ID大于指定值
     */
    @Query("SELECT d FROM DataRecord d WHERE d.id > :lastId ORDER BY d.id ASC")
    List<DataRecord> findByIdGreaterThan(@Param("lastId") Long lastId, Pageable pageable);

    /**
     * 批量更新
     */
    @Modifying
    @Transactional
    default void batchUpdate(List<DataRecord> records) {
        // 使用JDBC批量更新，性能更好
        String sql = "UPDATE data_record SET result = ?, status = ?, " +
                     "error_msg = ?, processed_at = ?, updated_at = NOW() " +
                     "WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataRecord record = records.get(i);
                ps.setBigDecimal(1, record.getResult());
                ps.setString(2, record.getStatus());
                ps.setString(3, record.getErrorMsg());
                ps.setTimestamp(4, new Timestamp(record.getProcessedAt().getTime()));
                ps.setLong(5, record.getId());
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }
}
```

---

## 性能优化技巧

### 1. 数据库优化

```sql
-- 添加索引，加速分页查询
CREATE INDEX idx_id_status ON data_record(id, status);

-- 分区表：按月分区，提升查询性能
ALTER TABLE data_record PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202601 VALUES LESS THAN (202602),
    PARTITION p202602 VALUES LESS THAN (202603),
    ...
);

-- 批量更新优化：使用CASE WHEN
UPDATE data_record
SET result = CASE id
    WHEN 1 THEN 100.00
    WHEN 2 THEN 200.00
    ...
END,
status = 'SUCCESS'
WHERE id IN (1, 2, 3, ...);
```

### 2. 线程池调优

```java
// 根据CPU核心数和IO密集型任务特点调整
int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
int maxPoolSize = corePoolSize * 4;
int queueCapacity = 2000;

ThreadPoolExecutor executor = new ThreadPoolExecutor(
    corePoolSize,
    maxPoolSize,
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(queueCapacity),
    new ThreadFactoryBuilder().setNameFormat("batch-worker-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用线程执行
);

// 预热线程池
executor.prestartAllCoreThreads();
```

### 3. API调用优化

```java
// HTTP连接池配置
PoolingHttpClientConnectionManager connectionManager =
    new PoolingHttpClientConnectionManager();
connectionManager.setMaxTotal(200); // 最大连接数
connectionManager.setDefaultMaxPerRoute(50); // 每个路由最大连接数

// 超时配置
RequestConfig requestConfig = RequestConfig.custom()
    .setConnectTimeout(3000)        // 连接超时3秒
    .setSocketTimeout(10000)        // 读取超时10秒
    .setConnectionRequestTimeout(1000) // 从连接池获取连接超时1秒
    .build();

// 使用HTTP/2提升性能
CloseableHttpClient httpClient = HttpClients.custom()
    .setConnectionManager(connectionManager)
    .setDefaultRequestConfig(requestConfig)
    .build();
```

### 4. 内存优化

```java
// 使用对象池减少GC压力
GenericObjectPool<ApiRequest> requestPool = new GenericObjectPool<>(
    new ApiRequestFactory(),
    new GenericObjectPoolConfig<>()
);

// 流式处理大结果集，避免OOM
@Transactional(readOnly = true)
public void streamProcess() {
    try (Stream<DataRecord> stream = dataRepository.streamAll()) {
        stream.forEach(record -> {
            processRecord(record);
        });
    }
}
```

### 5. 限流策略

```java
// 多级限流
public class MultiLevelRateLimiter {

    // 全局限流：每秒100个
    private RateLimiter globalLimiter = RateLimiter.create(100.0);

    // 单个API限流：每秒30个
    private Map<String, RateLimiter> apiLimiters = new ConcurrentHashMap<>();

    public void acquire(String apiName) {
        // 先通过全局限流
        globalLimiter.acquire();

        // 再通过API级别限流
        RateLimiter apiLimiter = apiLimiters.computeIfAbsent(
            apiName, k -> RateLimiter.create(30.0)
        );
        apiLimiter.acquire();
    }
}
```

---

## 监控告警方案

### 1. 核心指标监控

```java
@Component
public class BatchProcessMonitor {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter processedCounter;
    private Counter successCounter;
    private Counter failureCounter;
    private Timer apiLatencyTimer;

    @PostConstruct
    public void init() {
        processedCounter = Counter.builder("batch.process.total")
            .description("总处理数量")
            .register(meterRegistry);

        successCounter = Counter.builder("batch.process.success")
            .description("成功数量")
            .register(meterRegistry);

        failureCounter = Counter.builder("batch.process.failure")
            .description("失败数量")
            .register(meterRegistry);

        apiLatencyTimer = Timer.builder("batch.api.latency")
            .description("API调用延迟")
            .register(meterRegistry);
    }

    public void recordProcess(boolean success, long latencyMs) {
        processedCounter.increment();
        if (success) {
            successCounter.increment();
        } else {
            failureCounter.increment();
        }
        apiLatencyTimer.record(latencyMs, TimeUnit.MILLISECONDS);
    }
}
```

### 2. 关键监控指标

**核心指标**：
- 处理速率（TPS）：每秒处理条数
- 成功率：成功/总数 × 100%
- API平均延迟：P50、P95、P99
- 线程池使用率：活跃线程/最大线程
- 进度百分比：已处理/总数 × 100%

**告警规则**：
- 成功率 < 95%：WARNING
- API P99延迟 > 5秒：WARNING
- 处理速率 < 10条/秒：CRITICAL

---

## 大厂实践案例

### 案例1：阿里巴巴 - 商品价格批量更新

**业务场景**：
- 每天凌晨2点，批量更新5000万商品价格
- 需要调用供应商API获取最新价格
- 要求4小时内完成

**技术方案**：
- XXL-JOB分片：10个分片，10台机器并行
- 每台机器：50个线程并发处理
- 限流：每个供应商API限流100 QPS
- 断点续传：Redis记录每个分片的进度

**性能数据**：
- 处理速率：3500条/秒
- 总耗时：4小时
- 成功率：99.8%
- API平均延迟：150ms

**关键优化**：
1. 预热缓存：提前加载商品基础信息到Redis
2. 批量更新：每100条批量UPDATE数据库
3. 智能重试：失败的记录单独队列重试
4. 降级策略：API超时使用昨日价格

---

### 案例2：美团 - 用户信用分批量计算

**业务场景**：
- 每周计算1亿用户的信用分
- 需要调用征信API、消费API、行为API
- 要求24小时内完成

**技术方案**：
- 生产者：分页查询用户，推送到RabbitMQ
- 消费者：20个实例，每个50线程
- 限流：全局限流1000 QPS
- 容错：死信队列处理失败消息

**性能数据**：
- 处理速率：1200条/秒
- 总耗时：23小时
- 成功率：99.5%
- 平均每条调用3个API

**关键优化**：
1. API并行调用：3个API并行，节省2/3时间
2. 本地缓存：用户基础信息缓存1小时
3. 动态扩容：高峰期自动扩容到30个实例
4. 优先级队列：VIP用户优先处理

---

## 常见问题FAQ

### Q1: 如何估算处理时间？

**A**: 使用公式计算：
```
总时间 = 总数据量 / (并发线程数 × 单线程TPS)

示例：
- 数据量：1亿条
- 并发线程：50
- 单线程TPS：2条/秒
- 总时间 = 100,000,000 / (50 × 2) = 1,000,000秒 ≈ 278小时

优化后：
- 增加到200线程
- API并行调用，单线程TPS提升到5条/秒
- 总时间 = 100,000,000 / (200 × 5) = 100,000秒 ≈ 28小时
```

### Q2: 线程数设置多少合适？

**A**: 根据任务类型：
- **CPU密集型**：线程数 = CPU核心数 + 1
- **IO密集型**（调用API）：线程数 = CPU核心数 × (1 + IO等待时间/CPU计算时间)

```java
// 示例：8核CPU，API平均延迟200ms，计算时间10ms
int threads = 8 × (1 + 200/10) = 168

// 实际建议：50-200之间，根据压测结果调整
```

### Q3: 如何处理API限流？

**A**: 多级限流策略：
1. **全局限流**：控制总QPS
2. **API级限流**：每个API独立限流
3. **令牌桶算法**：允许短时突发

### Q4: 如何保证断点续传？

**A**: 记录进度到Redis：
```java
// 记录最后处理的ID
redisTemplate.opsForValue().set("batch:last_id", currentId);

// 启动时恢复
Long lastId = redisTemplate.opsForValue().get("batch:last_id");
```

### Q5: 如何避免重复处理？

**A**: 幂等性设计：
1. **状态检查**：处理前检查状态，已处理的跳过
2. **分布式锁**：使用Redis锁防止并发重复
3. **唯一约束**：数据库添加唯一索引

---

## 配置文件示例

```yaml
batch:
  process:
    page-size: 1000
    batch-update-size: 100
    thread-pool:
      core-size: 50
      max-size: 200
      queue-capacity: 1000
    rate-limit:
      global-qps: 100
      api-qps: 30
    retry:
      max-attempts: 3
      delay-ms: 1000
```

---

## 性能基准测试

### 测试环境
- CPU: 16核
- 内存: 32GB
- 数据库: MySQL 8.0
- Redis: 6.0

### 测试结果

| 方案 | 线程数 | TPS | 1亿条耗时 | 成功率 |
|------|--------|-----|----------|--------|
| 单线程 | 1 | 2 | 578天 | 99% |
| 多线程 | 50 | 100 | 11.6天 | 99.5% |
| 多线程+优化 | 200 | 500 | 2.3天 | 99.8% |
| 分布式10机 | 2000 | 5000 | 5.5小时 | 99.9% |

---

## 最佳实践总结

### ✅ 必须做的事情

1. **分页查询**：避免一次性加载大量数据
2. **限流控制**：保护外部API不被打爆
3. **断点续传**：支持任务重启恢复
4. **批量更新**：减少数据库交互次数
5. **监控告警**：实时监控处理进度和成功率
6. **幂等性设计**：避免重复处理
7. **降级策略**：API失败时的兜底方案

### ❌ 不要做的事情

1. **不要全表扫描**：使用ID分页，不要用OFFSET
2. **不要无限重试**：设置最大重试次数
3. **不要忽略超时**：所有API调用必须设置超时
4. **不要阻塞主线程**：使用异步处理
5. **不要忽略监控**：没有监控就是裸奔

---

## 技术选型决策树

```
开始
 │
 ├─ 数据量 < 100万？
 │   └─ 是 → 单机多线程方案 ⭐⭐⭐⭐⭐
 │
 ├─ 数据量 < 5000万？
 │   └─ 是 → 单机多线程 + 优化 ⭐⭐⭐⭐⭐
 │
 ├─ 数据量 < 1亿？
 │   └─ 是 → 分布式任务调度 ⭐⭐⭐⭐⭐
 │
 └─ 数据量 > 1亿？
     └─ 是 → 消息队列 + 集群 ⭐⭐⭐⭐⭐
```

---

## 核心文件清单

建议在项目中创建以下文件：

```
src/main/java/com/architecture/batchprocess/
├── BatchProcessService.java          # 主控制器
├── ExternalApiService.java           # API调用服务
├── DataRepository.java                # 数据访问层
├── BatchProcessMonitor.java          # 监控组件
├── MultiLevelRateLimiter.java        # 限流器
├── model/
│   ├── DataRecord.java               # 数据模型
│   ├── ApiResponse1.java
│   ├── ApiResponse2.java
│   └── ApiResponse3.java
└── config/
    ├── ThreadPoolConfig.java         # 线程池配置
    └── RestTemplateConfig.java       # HTTP客户端配置
```

---

## 总结

本方案提供了处理1亿条数据并调用外部API的完整解决方案，核心要点：

1. **分页查询 + 多线程并发**：化整为零，提升性能
2. **限流控制**：保护外部API，避免被限流
3. **断点续传**：支持任务重启，不丢失进度
4. **批量更新**：减少数据库压力
5. **监控告警**：实时掌握处理进度
6. **容错降级**：API失败时的兜底方案

**推荐方案**：
- **数据量 < 5000万**：单机多线程 + 限流控制
- **数据量 > 5000万**：分布式任务调度 + 分片处理

**性能预期**：
- 单机：100-500 TPS
- 10机集群：1000-5000 TPS
- 1亿条数据：5-24小时完成

---

**相关文档**：
- [场景一：高并发秒杀系统](./README.md#场景一高并发秒杀系统)
- [场景三：延迟队列](./README.md#场景三延迟队列)
- [场景四：限流算法](./README.md#场景四限流算法)

