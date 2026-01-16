-- ========================================
-- MySQL 数据库设计 (OLTP + 轻量级报表方案)
-- 适用场景: 数据量 < 500万，预算有限
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS medical_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_db;

-- 1. 门诊记录表 (分区表)
CREATE TABLE IF NOT EXISTS outpatient_record (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    record_id VARCHAR(64) NOT NULL COMMENT '记录ID',
    record_date DATE NOT NULL COMMENT '就诊日期',
    record_time DATETIME NOT NULL COMMENT '就诊时间',
    patient_id VARCHAR(64) NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(100) NOT NULL COMMENT '患者姓名',
    patient_age TINYINT UNSIGNED COMMENT '年龄',
    patient_gender ENUM('M', 'F') COMMENT '性别',
    doctor_id VARCHAR(64) NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(100) NOT NULL COMMENT '医生姓名',
    department_id VARCHAR(64) NOT NULL COMMENT '科室ID',
    department_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    diagnosis VARCHAR(500) COMMENT '诊断',
    prescription_amount DECIMAL(10,2) DEFAULT 0 COMMENT '处方金额',
    exam_amount DECIMAL(10,2) DEFAULT 0 COMMENT '检查费',
    treatment_amount DECIMAL(10,2) DEFAULT 0 COMMENT '治疗费',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    insurance_type ENUM('self', 'medical', 'commercial') COMMENT '医保类型',
    insurance_amount DECIMAL(10,2) DEFAULT 0 COMMENT '医保支付',
    self_amount DECIMAL(10,2) DEFAULT 0 COMMENT '自费金额',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态 0:未支付 1:已支付 2:已退费',
    payment_method ENUM('cash', 'alipay', 'wechat', 'card') COMMENT '支付方式',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_record_id (record_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_department_id (department_id),
    INDEX idx_record_date (record_date),
    INDEX idx_payment_status (payment_status),
    -- 复合索引: 报表查询优化
    INDEX idx_report_query (record_date, department_id, payment_status),
    INDEX idx_doctor_report (record_date, doctor_id, payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊记录表'
PARTITION BY RANGE (TO_DAYS(record_date)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')),
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')),
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')),
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')),
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')),
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')),
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- 2. 住院记录表 (分区表)
CREATE TABLE IF NOT EXISTS inpatient_record (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admission_id VARCHAR(64) NOT NULL COMMENT '住院ID',
    admission_date DATE NOT NULL COMMENT '入院日期',
    discharge_date DATE COMMENT '出院日期',
    patient_id VARCHAR(64) NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(100) NOT NULL COMMENT '患者姓名',
    patient_age TINYINT UNSIGNED COMMENT '年龄',
    patient_gender ENUM('M', 'F') COMMENT '性别',
    doctor_id VARCHAR(64) NOT NULL COMMENT '主治医生ID',
    doctor_name VARCHAR(100) NOT NULL COMMENT '主治医生姓名',
    department_id VARCHAR(64) NOT NULL COMMENT '科室ID',
    department_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    hospitalization_days SMALLINT UNSIGNED COMMENT '住院天数',
    bed_fee DECIMAL(10,2) DEFAULT 0 COMMENT '床位费',
    drug_fee DECIMAL(10,2) DEFAULT 0 COMMENT '药品费',
    exam_fee DECIMAL(10,2) DEFAULT 0 COMMENT '检查费',
    treatment_fee DECIMAL(10,2) DEFAULT 0 COMMENT '治疗费',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总费用',
    insurance_amount DECIMAL(10,2) DEFAULT 0 COMMENT '医保支付',
    self_amount DECIMAL(10,2) DEFAULT 0 COMMENT '自费金额',
    status TINYINT DEFAULT 1 COMMENT '状态 1:住院中 2:已出院 3:转院',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE INDEX uk_admission_id (admission_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_department_id (department_id),
    INDEX idx_admission_date (admission_date),
    INDEX idx_discharge_date (discharge_date),
    INDEX idx_status (status),
    INDEX idx_report_query (admission_date, department_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院记录表'
PARTITION BY RANGE (TO_DAYS(admission_date)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')),
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')),
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')),
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')),
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')),
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')),
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- 3. 日汇总报表 (预聚合表，T+1更新)
CREATE TABLE IF NOT EXISTS report_daily_summary (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_date DATE NOT NULL COMMENT '报表日期',
    department_id VARCHAR(64) NOT NULL COMMENT '科室ID',
    department_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    outpatient_count INT UNSIGNED DEFAULT 0 COMMENT '门诊人次',
    inpatient_count INT UNSIGNED DEFAULT 0 COMMENT '住院人次',
    total_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '总收入',
    outpatient_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '门诊收入',
    inpatient_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '住院收入',
    drug_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '药品收入',
    exam_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '检查收入',
    treatment_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '治疗收入',
    insurance_amount DECIMAL(18,2) DEFAULT 0 COMMENT '医保支付',
    self_amount DECIMAL(18,2) DEFAULT 0 COMMENT '自费金额',
    avg_outpatient_cost DECIMAL(10,2) DEFAULT 0 COMMENT '门诊均次费用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE INDEX uk_report_dept (report_date, department_id),
    INDEX idx_report_date (report_date),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日汇总报表';

-- 4. 月汇总报表
CREATE TABLE IF NOT EXISTS report_monthly_summary (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_month VARCHAR(7) NOT NULL COMMENT '报表月份 YYYY-MM',
    department_id VARCHAR(64) NOT NULL COMMENT '科室ID',
    department_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    outpatient_count INT UNSIGNED DEFAULT 0 COMMENT '门诊总人次',
    inpatient_count INT UNSIGNED DEFAULT 0 COMMENT '住院总人次',
    total_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '总收入',
    outpatient_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '门诊收入',
    inpatient_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '住院收入',
    drug_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '药品收入',
    insurance_amount DECIMAL(18,2) DEFAULT 0 COMMENT '医保金额',
    self_amount DECIMAL(18,2) DEFAULT 0 COMMENT '自费金额',
    year_over_year_growth DECIMAL(10,2) DEFAULT 0 COMMENT '同比增长率',
    month_over_month_growth DECIMAL(10,2) DEFAULT 0 COMMENT '环比增长率',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE INDEX uk_report_dept (report_month, department_id),
    INDEX idx_report_month (report_month),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月汇总报表';

-- 5. 年度汇总报表
CREATE TABLE IF NOT EXISTS report_annual_summary (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_year SMALLINT UNSIGNED NOT NULL COMMENT '报表年份',
    department_id VARCHAR(64) NOT NULL COMMENT '科室ID',
    department_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    outpatient_count INT UNSIGNED DEFAULT 0 COMMENT '年度门诊总人次',
    inpatient_count INT UNSIGNED DEFAULT 0 COMMENT '年度住院总人次',
    total_revenue DECIMAL(20,2) DEFAULT 0 COMMENT '年度总收入',
    drug_revenue DECIMAL(20,2) DEFAULT 0 COMMENT '药品收入',
    insurance_amount DECIMAL(20,2) DEFAULT 0 COMMENT '医保金额',
    self_amount DECIMAL(20,2) DEFAULT 0 COMMENT '自费金额',
    year_over_year_growth DECIMAL(10,2) DEFAULT 0 COMMENT '同比增长率',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE INDEX uk_report_dept (report_year, department_id),
    INDEX idx_report_year (report_year),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年度汇总报表';

-- 6. 医生工作量统计表
CREATE TABLE IF NOT EXISTS report_doctor_workload (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_date DATE NOT NULL COMMENT '统计日期',
    doctor_id VARCHAR(64) NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(100) NOT NULL COMMENT '医生姓名',
    department_id VARCHAR(64) NOT NULL COMMENT '科室ID',
    department_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    outpatient_count INT UNSIGNED DEFAULT 0 COMMENT '门诊量',
    inpatient_count INT UNSIGNED DEFAULT 0 COMMENT '住院管理人数',
    total_revenue DECIMAL(18,2) DEFAULT 0 COMMENT '创收金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE INDEX uk_report_doctor (report_date, doctor_id),
    INDEX idx_report_date (report_date),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生工作量统计表';

-- 7. 异步报表任务表
CREATE TABLE IF NOT EXISTS report_async_task (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型',
    query_params JSON COMMENT '查询参数',
    status TINYINT DEFAULT 0 COMMENT '状态 0:待处理 1:处理中 2:已完成 3:失败',
    result_url VARCHAR(500) COMMENT '结果文件URL',
    error_msg TEXT COMMENT '错误信息',
    create_user VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',

    UNIQUE INDEX uk_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_create_user (create_user),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步报表任务表';

-- ========================================
-- 定时任务SQL - 日汇总数据更新 (每天凌晨1点执行)
-- ========================================

-- 更新昨日门诊汇总数据
DELIMITER $$
CREATE PROCEDURE update_daily_outpatient_summary(IN p_report_date DATE)
BEGIN
    INSERT INTO report_daily_summary (
        report_date, department_id, department_name,
        outpatient_count, outpatient_revenue,
        drug_revenue, exam_revenue, treatment_revenue,
        insurance_amount, self_amount,
        avg_outpatient_cost
    )
    SELECT
        DATE(record_date) as report_date,
        department_id,
        department_name,
        COUNT(*) as outpatient_count,
        SUM(total_amount) as outpatient_revenue,
        SUM(prescription_amount) as drug_revenue,
        SUM(exam_amount) as exam_revenue,
        SUM(treatment_amount) as treatment_revenue,
        SUM(insurance_amount) as insurance_amount,
        SUM(self_amount) as self_amount,
        AVG(total_amount) as avg_outpatient_cost
    FROM outpatient_record
    WHERE DATE(record_date) = p_report_date
        AND payment_status = 1
    GROUP BY DATE(record_date), department_id, department_name
    ON DUPLICATE KEY UPDATE
        outpatient_count = VALUES(outpatient_count),
        outpatient_revenue = VALUES(outpatient_revenue),
        drug_revenue = VALUES(drug_revenue),
        exam_revenue = VALUES(exam_revenue),
        treatment_revenue = VALUES(treatment_revenue),
        insurance_amount = insurance_amount + VALUES(insurance_amount),
        self_amount = self_amount + VALUES(self_amount),
        avg_outpatient_cost = VALUES(avg_outpatient_cost),
        update_time = NOW();
END$$
DELIMITER ;

-- 更新月度汇总数据
DELIMITER $$
CREATE PROCEDURE update_monthly_summary(IN p_report_month VARCHAR(7))
BEGIN
    INSERT INTO report_monthly_summary (
        report_month, department_id, department_name,
        outpatient_count, inpatient_count, total_revenue,
        outpatient_revenue, inpatient_revenue,
        drug_revenue, insurance_amount, self_amount
    )
    SELECT
        p_report_month as report_month,
        department_id,
        department_name,
        SUM(outpatient_count) as outpatient_count,
        SUM(inpatient_count) as inpatient_count,
        SUM(total_revenue) as total_revenue,
        SUM(outpatient_revenue) as outpatient_revenue,
        SUM(inpatient_revenue) as inpatient_revenue,
        SUM(drug_revenue) as drug_revenue,
        SUM(insurance_amount) as insurance_amount,
        SUM(self_amount) as self_amount
    FROM report_daily_summary
    WHERE DATE_FORMAT(report_date, '%Y-%m') = p_report_month
    GROUP BY department_id, department_name
    ON DUPLICATE KEY UPDATE
        outpatient_count = VALUES(outpatient_count),
        inpatient_count = VALUES(inpatient_count),
        total_revenue = VALUES(total_revenue),
        outpatient_revenue = VALUES(outpatient_revenue),
        inpatient_revenue = VALUES(inpatient_revenue),
        drug_revenue = VALUES(drug_revenue),
        insurance_amount = VALUES(insurance_amount),
        self_amount = VALUES(self_amount),
        update_time = NOW();
END$$
DELIMITER ;

-- ========================================
-- 常用查询示例
-- ========================================

-- 查询1: 查询年度各科室收入汇总（使用月汇总表，性能更好）
SELECT
    department_name,
    SUM(total_revenue) as total_revenue,
    SUM(outpatient_count) as outpatient_count,
    SUM(inpatient_count) as inpatient_count
FROM report_monthly_summary
WHERE report_month BETWEEN '2025-01' AND '2025-12'
GROUP BY department_name
ORDER BY total_revenue DESC;

-- 查询2: 查询某科室的月度趋势
SELECT
    report_month,
    outpatient_count,
    inpatient_count,
    total_revenue,
    month_over_month_growth
FROM report_monthly_summary
WHERE department_id = 'DEPT001'
    AND report_month BETWEEN '2025-01' AND '2025-12'
ORDER BY report_month;

-- 查询3: 医生工作量排名 (TOP 10)
SELECT
    doctor_name,
    department_name,
    SUM(outpatient_count) as total_patients,
    SUM(total_revenue) as total_revenue
FROM report_doctor_workload
WHERE report_date BETWEEN '2025-01-01' AND '2025-12-31'
GROUP BY doctor_id, doctor_name, department_name
ORDER BY total_patients DESC
LIMIT 10;

-- ========================================
-- 性能优化建议
-- ========================================

-- 1. 定期分析表，更新统计信息
-- ANALYZE TABLE outpatient_record;
-- ANALYZE TABLE inpatient_record;

-- 2. 定期优化表
-- OPTIMIZE TABLE report_daily_summary;

-- 3. 监控慢查询
-- SET GLOBAL slow_query_log = ON;
-- SET GLOBAL long_query_time = 2;

-- 4. 查看索引使用情况
-- EXPLAIN SELECT * FROM outpatient_record WHERE record_date = '2025-01-01' AND department_id = 'DEPT001';
