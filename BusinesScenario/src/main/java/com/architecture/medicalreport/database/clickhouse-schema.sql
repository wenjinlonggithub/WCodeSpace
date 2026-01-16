-- ========================================
-- ClickHouse 数据库设计 (OLAP方案)
-- 适用场景: 数据量 > 500万，复杂多维分析
-- ========================================

-- 1. 门诊记录宽表 (明细数据)
CREATE TABLE IF NOT EXISTS medical_outpatient_wide (
    record_id String,                           -- 记录ID
    record_date Date,                           -- 就诊日期
    record_time DateTime,                       -- 就诊时间
    patient_id String,                          -- 患者ID
    patient_name String,                        -- 患者姓名
    patient_age UInt8,                          -- 年龄
    patient_gender Enum8('M'=1, 'F'=2),        -- 性别 M:男 F:女
    patient_id_card String,                     -- 身份证号
    patient_phone String,                       -- 联系电话
    doctor_id String,                           -- 医生ID
    doctor_name String,                         -- 医生姓名
    doctor_title String,                        -- 医生职称
    department_id String,                       -- 科室ID
    department_name String,                     -- 科室名称
    diagnosis String,                           -- 诊断
    prescription_count UInt16,                  -- 处方数量
    prescription_amount Decimal(10,2),          -- 处方金额
    exam_amount Decimal(10,2),                  -- 检查费
    treatment_amount Decimal(10,2),             -- 治疗费
    total_amount Decimal(10,2),                 -- 总金额
    insurance_type Enum8('self'=1, 'medical'=2, 'commercial'=3), -- 医保类型
    insurance_amount Decimal(10,2),             -- 医保支付金额
    self_amount Decimal(10,2),                  -- 自费金额
    payment_status Enum8('unpaid'=0, 'paid'=1, 'refund'=2),  -- 支付状态
    payment_method Enum8('cash'=1, 'alipay'=2, 'wechat'=3, 'card'=4), -- 支付方式
    visit_type Enum8('first'=1, 'followup'=2, 'emergency'=3),  -- 就诊类型
    create_time DateTime,                       -- 创建时间
    update_time DateTime                        -- 更新时间
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(record_date)              -- 按月分区，方便数据管理和查询优化
ORDER BY (record_date, department_id, doctor_id)  -- 排序键，影响查询性能
SETTINGS index_granularity = 8192;              -- 索引粒度

-- 索引说明:
-- 1. 主键索引: (record_date, department_id, doctor_id) 用于快速定位
-- 2. 分区索引: 按月分区，查询时只扫描相关分区

-- 2. 住院记录宽表
CREATE TABLE IF NOT EXISTS medical_inpatient_wide (
    admission_id String,                        -- 住院ID
    admission_date Date,                        -- 入院日期
    discharge_date Nullable(Date),              -- 出院日期
    patient_id String,                          -- 患者ID
    patient_name String,                        -- 患者姓名
    patient_age UInt8,                          -- 年龄
    patient_gender Enum8('M'=1, 'F'=2),        -- 性别
    doctor_id String,                           -- 主治医生ID
    doctor_name String,                         -- 主治医生姓名
    department_id String,                       -- 科室ID
    department_name String,                     -- 科室名称
    ward_id String,                             -- 病区ID
    bed_no String,                              -- 床位号
    diagnosis_admission String,                 -- 入院诊断
    diagnosis_discharge String,                 -- 出院诊断
    hospitalization_days UInt16,                -- 住院天数
    bed_fee Decimal(10,2),                      -- 床位费
    drug_fee Decimal(10,2),                     -- 药品费
    exam_fee Decimal(10,2),                     -- 检查费
    treatment_fee Decimal(10,2),                -- 治疗费
    surgery_fee Decimal(10,2),                  -- 手术费
    total_amount Decimal(10,2),                 -- 总费用
    insurance_amount Decimal(10,2),             -- 医保支付
    self_amount Decimal(10,2),                  -- 自费金额
    status Enum8('inpatient'=1, 'discharged'=2, 'transferred'=3), -- 状态
    create_time DateTime,
    update_time DateTime
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(admission_date)
ORDER BY (admission_date, department_id, doctor_id)
SETTINGS index_granularity = 8192;

-- 3. 日汇总报表 (自动聚合)
CREATE TABLE IF NOT EXISTS medical_daily_summary (
    report_date Date,                           -- 报表日期
    department_id String,                       -- 科室ID
    department_name String,                     -- 科室名称
    outpatient_count UInt32,                    -- 门诊人次
    inpatient_count UInt32,                     -- 住院人次
    total_revenue Decimal(18,2),                -- 总收入
    outpatient_revenue Decimal(18,2),           -- 门诊收入
    inpatient_revenue Decimal(18,2),            -- 住院收入
    drug_revenue Decimal(18,2),                 -- 药品收入
    exam_revenue Decimal(18,2),                 -- 检查收入
    treatment_revenue Decimal(18,2),            -- 治疗收入
    insurance_amount Decimal(18,2),             -- 医保支付
    self_amount Decimal(18,2),                  -- 自费金额
    avg_outpatient_cost Decimal(10,2),          -- 门诊均次费用
    avg_inpatient_cost Decimal(10,2),           -- 住院均次费用
    create_time DateTime
) ENGINE = SummingMergeTree()                   -- SummingMergeTree 自动聚合相同主键的数据
PARTITION BY toYYYYMM(report_date)
ORDER BY (report_date, department_id)
SETTINGS index_granularity = 8192;

-- 4. 物化视图: 门诊日汇总自动计算
CREATE MATERIALIZED VIEW IF NOT EXISTS medical_outpatient_daily_mv
TO medical_daily_summary
AS SELECT
    toDate(record_date) as report_date,
    department_id,
    any(department_name) as department_name,
    count() as outpatient_count,
    0 as inpatient_count,
    sum(total_amount) as total_revenue,
    sum(total_amount) as outpatient_revenue,
    0 as inpatient_revenue,
    sum(prescription_amount) as drug_revenue,
    sum(exam_amount) as exam_revenue,
    sum(treatment_amount) as treatment_revenue,
    sum(insurance_amount) as insurance_amount,
    sum(self_amount) as self_amount,
    avg(total_amount) as avg_outpatient_cost,
    0 as avg_inpatient_cost,
    now() as create_time
FROM medical_outpatient_wide
WHERE payment_status = 1  -- 只统计已支付的
GROUP BY report_date, department_id;

-- 5. 物化视图: 住院日汇总自动计算
CREATE MATERIALIZED VIEW IF NOT EXISTS medical_inpatient_daily_mv
TO medical_daily_summary
AS SELECT
    toDate(admission_date) as report_date,
    department_id,
    any(department_name) as department_name,
    0 as outpatient_count,
    count() as inpatient_count,
    sum(total_amount) as total_revenue,
    0 as outpatient_revenue,
    sum(total_amount) as inpatient_revenue,
    sum(drug_fee) as drug_revenue,
    sum(exam_fee) as exam_revenue,
    sum(treatment_fee) as treatment_revenue,
    sum(insurance_amount) as insurance_amount,
    sum(self_amount) as self_amount,
    0 as avg_outpatient_cost,
    avg(total_amount) as avg_inpatient_cost,
    now() as create_time
FROM medical_inpatient_wide
WHERE status = 2  -- 只统计已出院的
GROUP BY report_date, department_id;

-- 6. 医生工作量统计表
CREATE TABLE IF NOT EXISTS medical_doctor_workload (
    report_date Date,                           -- 统计日期
    doctor_id String,                           -- 医生ID
    doctor_name String,                         -- 医生姓名
    department_id String,                       -- 科室ID
    department_name String,                     -- 科室名称
    outpatient_count UInt32,                    -- 门诊量
    inpatient_count UInt32,                     -- 住院管理人数
    surgery_count UInt32,                       -- 手术台数
    total_revenue Decimal(18,2),                -- 创收金额
    avg_outpatient_time UInt16,                 -- 平均接诊时间(分钟)
    create_time DateTime
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(report_date)
ORDER BY (report_date, doctor_id)
SETTINGS index_granularity = 8192;

-- 7. 月度汇总表 (预聚合，查询更快)
CREATE TABLE IF NOT EXISTS medical_monthly_summary (
    report_month String,                        -- 报表月份 YYYY-MM
    department_id String,                       -- 科室ID
    department_name String,                     -- 科室名称
    outpatient_count UInt32,                    -- 门诊总人次
    inpatient_count UInt32,                     -- 住院总人次
    total_revenue Decimal(18,2),                -- 总收入
    outpatient_revenue Decimal(18,2),           -- 门诊收入
    inpatient_revenue Decimal(18,2),            -- 住院收入
    drug_revenue Decimal(18,2),                 -- 药品收入
    insurance_amount Decimal(18,2),             -- 医保金额
    self_amount Decimal(18,2),                  -- 自费金额
    year_over_year_growth Decimal(10,2),        -- 同比增长率
    month_over_month_growth Decimal(10,2),      -- 环比增长率
    create_time DateTime
) ENGINE = ReplacingMergeTree(create_time)      -- ReplacingMergeTree 保留最新版本
PARTITION BY substring(report_month, 1, 4)      -- 按年分区
ORDER BY (report_month, department_id)
SETTINGS index_granularity = 8192;

-- 8. 年度汇总表
CREATE TABLE IF NOT EXISTS medical_annual_summary (
    report_year UInt16,                         -- 报表年份
    department_id String,                       -- 科室ID
    department_name String,                     -- 科室名称
    outpatient_count UInt32,                    -- 年度门诊总人次
    inpatient_count UInt32,                     -- 年度住院总人次
    total_revenue Decimal(20,2),                -- 年度总收入
    drug_revenue Decimal(20,2),                 -- 药品收入
    insurance_amount Decimal(20,2),             -- 医保金额
    self_amount Decimal(20,2),                  -- 自费金额
    year_over_year_growth Decimal(10,2),        -- 同比增长率
    create_time DateTime
) ENGINE = ReplacingMergeTree(create_time)
ORDER BY (report_year, department_id)
SETTINGS index_granularity = 8192;

-- ========================================
-- 常用查询示例
-- ========================================

-- 查询1: 查询某科室某月的门诊量
SELECT
    toYYYYMM(record_date) as month,
    department_name,
    count() as patient_count,
    sum(total_amount) as revenue
FROM medical_outpatient_wide
WHERE record_date BETWEEN '2025-01-01' AND '2025-01-31'
    AND department_id = 'DEPT001'
GROUP BY month, department_name;

-- 查询2: 查询年度各科室收入排名
SELECT
    department_name,
    sum(total_revenue) as total_revenue,
    rank() OVER (ORDER BY sum(total_revenue) DESC) as ranking
FROM medical_daily_summary
WHERE report_date BETWEEN '2025-01-01' AND '2025-12-31'
GROUP BY department_name
ORDER BY total_revenue DESC
LIMIT 10;

-- 查询3: 医生工作量排名
SELECT
    doctor_name,
    department_name,
    sum(outpatient_count) as total_patients,
    sum(total_revenue) as total_revenue
FROM medical_doctor_workload
WHERE report_date BETWEEN '2025-01-01' AND '2025-12-31'
GROUP BY doctor_name, department_name
ORDER BY total_patients DESC
LIMIT 20;

-- 查询4: 月度趋势分析
SELECT
    toYYYYMM(report_date) as month,
    sum(outpatient_count) as outpatient_count,
    sum(total_revenue) as revenue,
    avg(avg_outpatient_cost) as avg_cost
FROM medical_daily_summary
WHERE report_date BETWEEN '2025-01-01' AND '2025-12-31'
GROUP BY month
ORDER BY month;

-- 查询5: 医保占比分析
SELECT
    department_name,
    sum(total_revenue) as total_revenue,
    sum(insurance_amount) as insurance_amount,
    round(sum(insurance_amount) / sum(total_revenue) * 100, 2) as insurance_ratio
FROM medical_daily_summary
WHERE report_date BETWEEN '2025-01-01' AND '2025-12-31'
GROUP BY department_name
ORDER BY total_revenue DESC;

-- ========================================
-- 性能优化建议
-- ========================================

-- 1. 对于频繁查询的字段，可以创建二级索引
-- ALTER TABLE medical_outpatient_wide ADD INDEX idx_patient_id patient_id TYPE bloom_filter(0.01) GRANULARITY 1;

-- 2. 对于大表，定期删除历史分区
-- ALTER TABLE medical_outpatient_wide DROP PARTITION '202401';

-- 3. 对于汇总表，定期执行OPTIMIZE合并数据
-- OPTIMIZE TABLE medical_daily_summary FINAL;

-- 4. 监控查询性能
-- SELECT query, query_duration_ms, read_rows, read_bytes
-- FROM system.query_log
-- WHERE type = 'QueryFinish'
-- ORDER BY query_duration_ms DESC
-- LIMIT 10;
