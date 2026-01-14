package com.architecture.esignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 签署日志服务
 * 负责记录签署日志
 */
@Service
public class SignatureLogService {

    private static final Logger log = LoggerFactory.getLogger(SignatureLogService.class);

    // 模拟数据库存储 (实际应该使用Mapper操作数据库)
    private final ConcurrentMap<String, SignatureLog> logDatabase = new ConcurrentHashMap<>();

    // 模拟唯一约束 (实际由数据库保证)
    private final ConcurrentMap<String, Boolean> uniqueIndex = new ConcurrentHashMap<>();

    // ================ 问题代码: 直接插入,不检查重复 ================

    /**
     * 有问题的实现: 直接插入,不做任何检查
     * 问题: 并发时会产生重复记录
     */
    public void recordLogDirectly(SignatureCallbackDTO dto) {
        SignatureLog logEntity = new SignatureLog();
        logEntity.setId(generateId());
        logEntity.setSignatureId(dto.getSignatureId());
        logEntity.setDoctorId(dto.getDoctorId());
        logEntity.setDoctorName(dto.getDoctorName());
        logEntity.setOperationType("SIGN_COMPLETE");
        logEntity.setCreateTime(new Date());

        // 直接插入 (问题: 没有检查是否已存在)
        logDatabase.put(logEntity.getId(), logEntity);

        log.info("记录签署日志: signatureId={}, logId={}",
                dto.getSignatureId(), logEntity.getId());
    }

    // ================ 解决方案1: 数据库唯一约束 ================

    /**
     * 方案1: 利用数据库唯一约束防止重复插入
     * 在数据库中添加: UNIQUE INDEX uk_signature_id (signature_id)
     *
     * 优点:
     * - 简单可靠,数据库层面保证
     * - 性能好,不需要额外的查询
     * - 天然支持并发
     */
    public void recordLogWithUniqueConstraint(SignatureCallbackDTO dto) {
        try {
            SignatureLog logEntity = new SignatureLog();
            logEntity.setId(generateId());
            logEntity.setSignatureId(dto.getSignatureId());
            logEntity.setDoctorId(dto.getDoctorId());
            logEntity.setDoctorName(dto.getDoctorName());
            logEntity.setOperationType("SIGN_COMPLETE");
            logEntity.setCreateTime(new Date());

            // 模拟插入数据库 (实际使用Mapper)
            insertWithUniqueCheck(logEntity);

            log.info("记录签署日志成功: signatureId={}, logId={}",
                    dto.getSignatureId(), logEntity.getId());

        } catch (DuplicateKeyException e) {
            // 重复插入,说明已经记录过了
            log.warn("签署日志已存在,忽略重复记录: signatureId={}",
                    dto.getSignatureId());

            // 不抛出异常,正常返回 (幂等性)
        }
    }

    /**
     * 模拟数据库插入 + 唯一约束检查
     * 实际SQL:
     * INSERT INTO signature_log (id, signature_id, doctor_id, doctor_name, operation_type, create_time)
     * VALUES (?, ?, ?, ?, ?, ?)
     *
     * 表结构包含唯一索引:
     * UNIQUE INDEX uk_signature_id (signature_id)
     */
    private void insertWithUniqueCheck(SignatureLog logEntity) {
        // 模拟唯一约束检查
        Boolean exists = uniqueIndex.putIfAbsent(logEntity.getSignatureId(), true);
        if (exists != null) {
            // 违反唯一约束
            throw new DuplicateKeyException(
                "Duplicate entry '" + logEntity.getSignatureId() +
                "' for key 'uk_signature_id'"
            );
        }

        // 插入成功
        logDatabase.put(logEntity.getId(), logEntity);
    }

    // ================ 解决方案2: 先查后插 ================

    /**
     * 方案2: 先查询是否存在,不存在再插入
     * 注意: 必须配合分布式锁使用,否则仍然有并发问题
     *
     * 适用场景:
     * - 重复请求比较频繁,避免大量的DuplicateKeyException
     * - 需要在查询和插入之间做额外的业务逻辑
     */
    public void recordLogCheckThenInsert(SignatureCallbackDTO dto) {
        // 先查询是否已存在
        boolean exists = isLogExists(dto.getSignatureId());
        if (exists) {
            log.info("签署日志已存在,跳过插入: signatureId={}", dto.getSignatureId());
            return;
        }

        // 不存在,插入
        SignatureLog logEntity = new SignatureLog();
        logEntity.setId(generateId());
        logEntity.setSignatureId(dto.getSignatureId());
        logEntity.setDoctorId(dto.getDoctorId());
        logEntity.setDoctorName(dto.getDoctorName());
        logEntity.setOperationType("SIGN_COMPLETE");
        logEntity.setCreateTime(new Date());

        logDatabase.put(logEntity.getId(), logEntity);

        log.info("记录签署日志: signatureId={}, logId={}",
                dto.getSignatureId(), logEntity.getId());
    }

    /**
     * 检查签署日志是否已存在
     *
     * SQL:
     * SELECT COUNT(*) FROM signature_log WHERE signature_id = ?
     */
    public boolean isLogExists(String signatureId) {
        // 模拟数据库查询
        boolean exists = uniqueIndex.containsKey(signatureId);

        log.debug("检查签署日志是否存在: signatureId={}, exists={}",
                signatureId, exists);

        return exists;
    }

    // ================ 辅助方法 ================

    /**
     * 生成日志ID
     */
    private String generateId() {
        return "LOG_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 查询签署日志数量 (用于测试验证)
     */
    public int countLogs(String signatureId) {
        return (int) logDatabase.values().stream()
                .filter(log -> log.getSignatureId().equals(signatureId))
                .count();
    }

    /**
     * 清空日志 (用于测试)
     */
    public void clearLogs() {
        logDatabase.clear();
        uniqueIndex.clear();
        log.info("清空所有签署日志");
    }

    /**
     * 获取所有日志 (用于测试验证)
     */
    public java.util.Collection<SignatureLog> getAllLogs() {
        return logDatabase.values();
    }
}

/**
 * 签署日志实体
 *
 * 数据库表结构:
 * CREATE TABLE signature_log (
 *     id VARCHAR(64) PRIMARY KEY,
 *     signature_id VARCHAR(64) NOT NULL,
 *     doctor_id VARCHAR(64) NOT NULL,
 *     doctor_name VARCHAR(128) NOT NULL,
 *     operation_type VARCHAR(32) NOT NULL,
 *     create_time DATETIME NOT NULL,
 *     UNIQUE INDEX uk_signature_id (signature_id)  -- 唯一索引,防止重复
 * );
 */
class SignatureLog {
    private String id;
    private String signatureId;
    private String doctorId;
    private String doctorName;
    private String operationType;
    private Date createTime;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSignatureId() { return signatureId; }
    public void setSignatureId(String signatureId) { this.signatureId = signatureId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "SignatureLog{" +
                "id='" + id + '\'' +
                ", signatureId='" + signatureId + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", operationType='" + operationType + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
