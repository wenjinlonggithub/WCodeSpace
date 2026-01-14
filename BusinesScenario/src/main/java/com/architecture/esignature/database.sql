-- ============================================================================
-- 电子签名回调场景 - 数据库表结构
-- ============================================================================

-- 1. 签署主表
-- 记录医生的签署信息
-- ============================================================================
CREATE TABLE signature (
    signature_id VARCHAR(64) PRIMARY KEY COMMENT '签署ID,主键',
    doctor_id VARCHAR(64) NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(128) NOT NULL COMMENT '医生姓名',
    document_id VARCHAR(64) NOT NULL COMMENT '文档ID',
    document_name VARCHAR(255) NOT NULL COMMENT '文档名称',

    -- 状态字段 (用于状态机)
    status VARCHAR(32) NOT NULL COMMENT '签署状态: PENDING-待签署, COMPLETED-已完成, FAILED-失败',

    -- 版本号 (用于乐观锁)
    version INT NOT NULL DEFAULT 1 COMMENT '版本号,用于状态流转的CAS操作',

    -- 易签宝相关字段
    esign_flow_id VARCHAR(128) DEFAULT NULL COMMENT '易签宝流程ID',
    esign_callback_token VARCHAR(128) DEFAULT NULL COMMENT '回调Token,用于幂等性验证',

    -- 时间字段
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    signature_time DATETIME DEFAULT NULL COMMENT '签署完成时间',

    -- 索引
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_document_id (document_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签署主表';


-- 2. 签署日志表 (核心: 唯一索引防止重复)
-- 记录签署的操作日志,用于审计追踪
-- ============================================================================
CREATE TABLE signature_log (
    id VARCHAR(64) PRIMARY KEY COMMENT '日志ID,主键',

    -- 关联字段
    signature_id VARCHAR(64) NOT NULL COMMENT '签署ID',
    doctor_id VARCHAR(64) NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(128) NOT NULL COMMENT '医生姓名',

    -- 操作信息
    operation_type VARCHAR(32) NOT NULL COMMENT '操作类型: SIGN_COMPLETE-完成签署, SIGN_FAILED-签署失败',
    operation_desc VARCHAR(512) DEFAULT NULL COMMENT '操作描述',

    -- 追踪信息
    trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    request_ip VARCHAR(64) DEFAULT NULL COMMENT '请求IP',

    -- 时间字段
    create_time DATETIME NOT NULL COMMENT '创建时间',

    -- ========================================================================
    -- 关键: 唯一索引,防止重复插入签署日志
    -- 这是解决重复日志问题的核心方案!
    -- ========================================================================
    UNIQUE INDEX uk_signature_id (signature_id),

    -- 其他索引
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_create_time (create_time),
    INDEX idx_operation_type (operation_type)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签署日志表';


-- 3. 签署详细日志表 (可选)
-- 如果一个签署需要记录多条日志(如多个操作步骤),使用此表
-- ============================================================================
CREATE TABLE signature_detail_log (
    id VARCHAR(64) PRIMARY KEY COMMENT '日志ID,主键',
    signature_id VARCHAR(64) NOT NULL COMMENT '签署ID',
    doctor_id VARCHAR(64) NOT NULL COMMENT '医生ID',
    operation_type VARCHAR(32) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(512) DEFAULT NULL COMMENT '操作描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',

    -- ========================================================================
    -- 组合唯一索引: 同一个签署的同一种操作类型只能记录一次
    -- 适用场景: 一个签署有多个操作步骤,每个步骤只能记录一次
    -- 例如: SIGN_START(发起签署), SIGN_COMPLETE(完成签署), SIGN_NOTIFY(通知发送)
    -- ========================================================================
    UNIQUE INDEX uk_signature_operation (signature_id, operation_type),

    INDEX idx_doctor_id (doctor_id),
    INDEX idx_create_time (create_time)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签署详细日志表';


-- ============================================================================
-- 测试数据
-- ============================================================================

-- 插入测试签署记录
INSERT INTO signature (
    signature_id, doctor_id, doctor_name, document_id, document_name,
    status, version, create_time, update_time
) VALUES
(
    'SIGN_20260114_001',
    'DOC_001',
    '张三医生',
    'DOCUMENT_001',
    '患者李四的诊断报告',
    'PENDING',
    1,
    NOW(),
    NOW()
);


-- ============================================================================
-- 常用查询SQL
-- ============================================================================

-- 查询1: 查找重复的签署日志
-- 用于检测是否存在重复日志问题
-- ----------------------------------------------------------------------------
SELECT
    signature_id,
    COUNT(*) as log_count
FROM signature_log
GROUP BY signature_id
HAVING log_count > 1
ORDER BY log_count DESC;


-- 查询2: 查看某个签署的所有日志
-- 用于排查单个签署的日志记录情况
-- ----------------------------------------------------------------------------
SELECT
    id,
    signature_id,
    doctor_name,
    operation_type,
    create_time,
    trace_id
FROM signature_log
WHERE signature_id = 'SIGN_20260114_001'
ORDER BY create_time;


-- 查询3: 统计签署日志数量
-- 用于监控日志记录情况
-- ----------------------------------------------------------------------------
SELECT
    DATE(create_time) as log_date,
    operation_type,
    COUNT(*) as log_count
FROM signature_log
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(create_time), operation_type
ORDER BY log_date DESC, operation_type;


-- 查询4: 查找长时间未完成的签署
-- 用于监控签署流程异常
-- ----------------------------------------------------------------------------
SELECT
    signature_id,
    doctor_name,
    document_name,
    status,
    create_time,
    TIMESTAMPDIFF(MINUTE, create_time, NOW()) as pending_minutes
FROM signature
WHERE status = 'PENDING'
  AND create_time < DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY create_time;


-- ============================================================================
-- 性能优化建议
-- ============================================================================

-- 1. 如果签署日志表数据量很大,建议分区
-- ALTER TABLE signature_log
-- PARTITION BY RANGE (TO_DAYS(create_time)) (
--     PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
--     PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
--     ...
-- );

-- 2. 定期归档历史日志 (保留最近3个月的数据)
-- DELETE FROM signature_log
-- WHERE create_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 3. 分析慢查询,添加必要的索引
-- SHOW CREATE TABLE signature_log;
-- EXPLAIN SELECT ... FROM signature_log WHERE ...;


-- ============================================================================
-- 回滚脚本
-- ============================================================================

-- 如果需要删除这些表,执行以下SQL
-- DROP TABLE IF EXISTS signature_detail_log;
-- DROP TABLE IF EXISTS signature_log;
-- DROP TABLE IF EXISTS signature;
