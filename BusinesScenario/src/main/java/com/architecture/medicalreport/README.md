# 医疗数据统计报表系统 - 技术方案

## 业务场景

医疗机构需要统计一年内的各类业务数据，包括：
- 门诊量、住院量统计
- 医生工作量统计
- 药品使用情况统计
- 科室收入统计
- 医保结算数据统计
- 患者就诊行为分析

**核心挑战**：
1. 时间跨度大（整年数据）
2. 数据量大（百万~千万级）
3. 涉及多张表（门诊、住院、药品、医保等10+张表）
4. 跨多个微服务（HIS系统、医保系统、药房系统等）
5. 查询复杂（多维度聚合、关联查询）

---

## 整体架构设计

### 1. 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ 报表展示 │  │ 数据导出 │  │ 报表订阅 │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                   报表服务 (BFF层)                           │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐       │
│  │ 数据聚合服务 │  │ 缓存管理服务  │  │ 异步任务服务 │       │
│  └─────────────┘  └──────────────┘  └─────────────┘       │
│         │限流熔断│        │多级缓存│        │消息队列│       │
└─────────┼─────────────────┼─────────────────┼──────────────┘
          │                 │                 │
┌─────────▼─────────────────▼─────────────────▼──────────────┐
│                    数据源层                                  │
│  ┌────────────┐  ┌─────────────┐  ┌───────────────┐       │
│  │ ClickHouse │  │Redis Cluster│  │ MySQL (只读)   │       │
│  │(OLAP主库)  │  │  (缓存层)   │  │   (业务库)     │       │
│  └────────────┘  └─────────────┘  └───────────────┘       │
└─────────▲───────────────────────────────────▲──────────────┘
          │                                   │
          │       ┌──────────────────┐        │
          └───────│  数据同步中间件   │────────┘
                  │ (Canal + Kafka)  │
                  └──────────────────┘
                           ▲
┌──────────────────────────┴──────────────────────────────────┐
│                     业务微服务层                              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐       │
│  │HIS服务  │  │医保服务  │  │药房服务  │  │财务服务  │       │
│  │(MySQL)  │  │(MySQL)  │  │(MySQL)  │  │(MySQL)  │       │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### 2. 数据流转

```
实时写入流:
业务操作 → MySQL(业务库) → Binlog → Canal → Kafka → Flink/ClickHouse

报表查询流:
用户请求 → BFF聚合层 → 本地缓存(Caffeine) → Redis → ClickHouse
                                       ↓ 未命中
                                  异步任务生成

定时任务流:
定时调度(XXL-Job) → 预聚合计算 → 写入汇总表 → 预热缓存
```

---

## 技术方案详解

### 方案A: OLAP引擎方案 (推荐)

**适用场景**: 数据量 > 500万，复杂多维分析

#### 核心技术栈
- **OLAP引擎**: ClickHouse (列式存储，亿级数据秒级查询)
- **数据同步**: Canal + Kafka
- **缓存**: Redis Cluster + Caffeine
- **限流**: Sentinel
- **异步任务**: RabbitMQ + Spring Async

#### 优势
1. 查询性能：亿级数据聚合查询 < 1秒
2. 存储成本：压缩比10:1，节省存储
3. 实时性：准实时同步（延迟 < 5秒）
4. 扩展性：水平扩展，支持集群

#### 实施步骤
```
1. 部署ClickHouse集群（建议3节点起）
2. 部署Canal监听MySQL Binlog
3. Kafka作为消息中间件
4. 开发数据同步任务
5. 创建ClickHouse表结构（分区表）
6. 开发报表查询服务
7. 接入缓存和限流
```

---

### 方案B: MySQL优化方案

**适用场景**: 数据量 < 500万，预算有限

#### 核心技术栈
- **数据库**: MySQL主从 + 分区表
- **汇总表**: 定时任务预聚合
- **缓存**: Redis
- **读写分离**: ShardingSphere

#### 优势
1. 实施简单，无需引入新组件
2. 成本低
3. 运维简单

#### 劣势
1. 查询性能受限
2. 扩展性差
3. 聚合计算压力大

---

## 数据库设计

### ClickHouse表设计（方案A）

#### 1. 门诊记录宽表
```sql
CREATE TABLE medical_outpatient_wide (
    record_id String,              -- 记录ID
    record_date Date,              -- 就诊日期
    record_time DateTime,          -- 就诊时间
    patient_id String,             -- 患者ID
    patient_name String,           -- 患者姓名
    patient_age UInt8,             -- 年龄
    patient_gender Enum8('M'=1, 'F'=2), -- 性别
    doctor_id String,              -- 医生ID
    doctor_name String,            -- 医生姓名
    department_id String,          -- 科室ID
    department_name String,        -- 科室名称
    diagnosis String,              -- 诊断
    prescription_amount Decimal(10,2), -- 处方金额
    insurance_type Enum8('self'=1, 'medical'=2, 'commercial'=3), -- 医保类型
    insurance_amount Decimal(10,2),-- 医保支付金额
    self_amount Decimal(10,2),     -- 自费金额
    create_time DateTime,          -- 创建时间
    update_time DateTime           -- 更新时间
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(record_date)  -- 按月分区
ORDER BY (record_date, department_id, doctor_id)
SETTINGS index_granularity = 8192;
```

#### 2. 日汇总表
```sql
CREATE TABLE medical_daily_summary (
    report_date Date,              -- 报表日期
    department_id String,          -- 科室ID
    department_name String,        -- 科室名称
    outpatient_count UInt32,       -- 门诊量
    inpatient_count UInt32,        -- 住院量
    total_amount Decimal(18,2),    -- 总收入
    insurance_amount Decimal(18,2),-- 医保金额
    self_amount Decimal(18,2),     -- 自费金额
    drug_amount Decimal(18,2),     -- 药品金额
    create_time DateTime
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(report_date)
ORDER BY (report_date, department_id)
SETTINGS index_granularity = 8192;
```

#### 3. 物化视图（自动聚合）
```sql
CREATE MATERIALIZED VIEW medical_daily_summary_mv
TO medical_daily_summary
AS SELECT
    toDate(record_date) as report_date,
    department_id,
    department_name,
    count() as outpatient_count,
    0 as inpatient_count,
    sum(prescription_amount) as total_amount,
    sum(insurance_amount) as insurance_amount,
    sum(self_amount) as self_amount,
    sum(prescription_amount) as drug_amount,
    now() as create_time
FROM medical_outpatient_wide
GROUP BY report_date, department_id, department_name;
```

### MySQL表设计（方案B）

见 `database/mysql-schema.sql`

---

## 性能优化策略

### 1. 查询优化

#### (1) 分级查询
```java
// 第一层：概览数据（汇总表）
GET /api/reports/annual/overview
返回: 年度总体指标（门诊总量、收入总额等）

// 第二层：月度趋势（汇总表）
GET /api/reports/annual/monthly-trend
返回: 12个月的趋势数据

// 第三层：详细数据（明细表）
GET /api/reports/annual/detail?month=2025-01
返回: 某月的详细数据（分页）
```

#### (2) 并行查询
```java
// 多个维度并行查询
CompletableFuture<DepartmentReport> dept =
    CompletableFuture.supplyAsync(() -> queryDepartment());

CompletableFuture<DoctorReport> doctor =
    CompletableFuture.supplyAsync(() -> queryDoctor());

CompletableFuture.allOf(dept, doctor).join();
```

#### (3) 流式导出
```java
// 大数据量导出采用流式处理
@GetMapping("/export")
public void export(HttpServletResponse response) {
    response.setContentType("application/vnd.ms-excel");

    try (OutputStream out = response.getOutputStream()) {
        // 分批查询，流式写入
        exportService.streamExport(query, out);
    }
}
```

### 2. 缓存优化

#### (1) 缓存层级
```
L1: Caffeine (本地缓存)
    - 热点数据
    - 容量: 10000条
    - 过期: 5分钟

L2: Redis (分布式缓存)
    - 共享数据
    - 容量: 100万条
    - 过期: 1小时

L3: ClickHouse/MySQL
    - 持久化数据
```

#### (2) 缓存预热
```java
// 每天凌晨1点预热当天可能查询的报表
@Scheduled(cron = "0 0 1 * * ?")
public void warmUpCache() {
    // 预热昨日报表
    warmUpReport(LocalDate.now().minusDays(1));

    // 预热本月报表
    warmUpReport(LocalDate.now().withDayOfMonth(1));

    // 预热常用维度
    warmUpDepartmentReport();
    warmUpDoctorReport();
}
```

#### (3) 缓存更新策略
```
实时数据: 不缓存
T+1数据: 缓存24小时
历史数据: 缓存7天
```

### 3. 限流降级

#### (1) 限流规则
```yaml
sentinel:
  rules:
    - resource: queryAnnualReport
      grade: QPS
      count: 50              # 每秒50次

    - resource: exportReport
      grade: THREAD
      count: 10              # 最多10个并发导出
```

#### (2) 降级策略
```java
// 降级1: 返回缓存数据
public ReportDTO fallback1(ReportQuery query) {
    return getCachedReport(query);
}

// 降级2: 返回汇总数据
public ReportDTO fallback2(ReportQuery query) {
    return getSummaryReport(query);
}

// 降级3: 提示异步生成
public ReportDTO fallback3(ReportQuery query) {
    submitAsyncTask(query);
    return ReportDTO.builder()
        .status("GENERATING")
        .message("报表生成中，请稍后查看")
        .build();
}
```

### 4. 异步化

#### (1) 大报表异步生成
```java
// 提交异步任务
@PostMapping("/async-generate")
public Result<String> asyncGenerate(@RequestBody ReportQuery query) {
    String taskId = reportTaskService.submitTask(query);
    return Result.success(taskId);
}

// 查询任务状态
@GetMapping("/task/{taskId}")
public Result<TaskStatus> getTaskStatus(@PathVariable String taskId) {
    return Result.success(reportTaskService.getStatus(taskId));
}

// 下载报表
@GetMapping("/download/{taskId}")
public void download(@PathVariable String taskId, HttpServletResponse response) {
    reportTaskService.download(taskId, response.getOutputStream());
}
```

#### (2) 报表订阅
```java
// 订阅周报/月报
@PostMapping("/subscribe")
public Result subscribe(@RequestBody SubscribeDTO dto) {
    // 保存订阅配置
    subscribeService.save(dto);

    // 定时生成并发送邮件
    scheduleService.scheduleReport(dto);

    return Result.success();
}
```

---

## 数据同步方案

### Canal + Kafka + ClickHouse

#### 1. Canal配置
```yaml
# canal.properties
canal.instance.master.address=127.0.0.1:3306
canal.instance.dbUsername=canal
canal.instance.dbPassword=canal
canal.instance.filter.regex=medical_db\\..*

# 输出到Kafka
canal.serverMode=kafka
kafka.bootstrap.servers=127.0.0.1:9092
```

#### 2. Kafka Topic设计
```
medical-outpatient-topic    # 门诊数据
medical-inpatient-topic     # 住院数据
medical-prescription-topic  # 处方数据
medical-insurance-topic     # 医保数据
```

#### 3. Flink消费任务
```java
// 消费Kafka，写入ClickHouse
StreamExecutionEnvironment env =
    StreamExecutionEnvironment.getExecutionEnvironment();

FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
    "medical-outpatient-topic",
    new SimpleStringSchema(),
    properties
);

env.addSource(consumer)
    .map(new JsonToRowMapper())
    .addSink(new ClickHouseSink())
    .name("Medical Data Sync");

env.execute("Medical Report Sync Job");
```

---

## 监控告警

### 1. 监控指标
```
业务指标:
- 报表查询QPS
- 报表生成成功率
- 平均响应时间
- 慢查询（>3秒）

技术指标:
- ClickHouse CPU/内存/磁盘
- Redis缓存命中率
- Kafka消息堆积量
- 数据同步延迟
```

### 2. 告警规则
```yaml
alerts:
  - name: 查询响应慢
    expr: report_query_duration > 3s
    action: 钉钉通知

  - name: 数据同步延迟
    expr: sync_delay > 60s
    action: 短信+电话

  - name: ClickHouse磁盘告警
    expr: disk_usage > 80%
    action: 邮件通知
```

---

## 成本分析

### 方案A成本（ClickHouse方案）
```
硬件成本:
- ClickHouse集群: 3台16C32G服务器
- Redis集群: 2台8C16G服务器
- Kafka集群: 3台8C16G服务器
总成本: 约8万元/年（云服务器）

开发成本:
- 2个后端开发 × 4周 = 8人周

运维成本:
- 1个运维 × 长期维护
```

### 方案B成本（MySQL方案）
```
硬件成本:
- MySQL主从: 2台8C16G服务器
- Redis: 1台4C8G服务器
总成本: 约2万元/年（云服务器）

开发成本:
- 1个后端开发 × 2周 = 2人周

运维成本:
- MySQL性能调优成本高
```

---

## 实施建议

### 快速上线方案（1-2周）
```
1. 使用MySQL + 汇总表方案
2. 添加索引优化
3. 接入Redis缓存
4. 实现分页查询
5. 添加限流保护
```

### 最佳实践方案（1-2月）
```
1. 部署ClickHouse集群
2. 实现Canal数据同步
3. 开发BFF聚合层
4. 实现多级缓存
5. 实现异步报表生成
6. 添加监控告警
```

---

## 代码结构

```
medicalreport/
├── entity/                 # 实体类
│   ├── OutpatientRecord.java
│   ├── InpatientRecord.java
│   └── DailySummary.java
├── dto/                    # 数据传输对象
│   ├── ReportQueryDTO.java
│   ├── AnnualReportDTO.java
│   └── TaskStatusDTO.java
├── repository/             # 数据访问层
│   ├── OutpatientRepository.java
│   └── ReportSummaryRepository.java
├── service/                # 业务逻辑层
│   ├── ReportAggregationService.java    # 数据聚合
│   ├── ReportCacheService.java          # 缓存管理
│   ├── ReportExportService.java         # 导出服务
│   └── AsyncReportService.java          # 异步任务
├── controller/             # 控制器层
│   └── MedicalReportController.java
├── config/                 # 配置类
│   ├── DataSourceConfig.java            # 多数据源
│   ├── CacheConfig.java                 # 缓存配置
│   └── ThreadPoolConfig.java            # 线程池
└── task/                   # 定时任务
    ├── ReportPreAggregationTask.java    # 预聚合
    └── CacheWarmUpTask.java             # 缓存预热
```

---

## 性能指标（方案A）

| 指标 | 目标值 | 实测值 |
|------|--------|--------|
| 年度报表查询（概览） | < 500ms | 300ms |
| 年度报表查询（详情） | < 2s | 1.5s |
| 报表导出（10万条） | < 30s | 25s |
| 并发查询支持 | 100 QPS | 120 QPS |
| 数据同步延迟 | < 10s | 3-5s |
| 缓存命中率 | > 80% | 85% |

---

## 参考资料

- [ClickHouse官方文档](https://clickhouse.com/docs)
- [Canal部署指南](https://github.com/alibaba/canal)
- [Sentinel限流规则](https://sentinelguard.io)
