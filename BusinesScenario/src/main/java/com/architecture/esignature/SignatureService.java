package com.architecture.esignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 签署服务
 * 负责管理签署状态
 */
@Service
public class SignatureService {

    private static final Logger log = LoggerFactory.getLogger(SignatureService.class);

    // 模拟数据库存储
    private final ConcurrentMap<String, SignatureEntity> signatureDatabase = new ConcurrentHashMap<>();

    /**
     * 更新签署状态
     *
     * SQL:
     * UPDATE signature SET status = ? WHERE signature_id = ?
     */
    public void updateStatus(String signatureId, String status) {
        SignatureEntity entity = signatureDatabase.get(signatureId);

        if (entity == null) {
            // 签署不存在,创建新的
            entity = new SignatureEntity();
            entity.setSignatureId(signatureId);
            entity.setStatus(status);
            entity.setVersion(1);
            signatureDatabase.put(signatureId, entity);

            log.info("创建签署记录: signatureId={}, status={}", signatureId, status);
        } else {
            // 更新状态
            String oldStatus = entity.getStatus();
            entity.setStatus(status);

            log.info("更新签署状态: signatureId={}, {} -> {}",
                    signatureId, oldStatus, status);
        }
    }

    /**
     * 状态流转 (CAS操作)
     * 只有当前状态是expectedOldStatus时,才更新为newStatus
     *
     * SQL (使用乐观锁):
     * UPDATE signature
     * SET status = ?, version = version + 1
     * WHERE signature_id = ? AND status = ? AND version = ?
     *
     * @return true=更新成功, false=状态已经变更过
     */
    public boolean transitionStatus(String signatureId, String expectedOldStatus, String newStatus) {
        SignatureEntity entity = signatureDatabase.get(signatureId);

        if (entity == null) {
            log.warn("签署不存在: signatureId={}", signatureId);
            return false;
        }

        // 检查状态是否匹配
        if (!expectedOldStatus.equals(entity.getStatus())) {
            log.info("状态不匹配,跳过更新: signatureId={}, expected={}, actual={}",
                    signatureId, expectedOldStatus, entity.getStatus());
            return false;
        }

        // 使用CAS操作更新状态
        synchronized (entity) {
            // 再次检查状态 (双重检查)
            if (!expectedOldStatus.equals(entity.getStatus())) {
                return false;
            }

            // 更新状态和版本号
            entity.setStatus(newStatus);
            entity.setVersion(entity.getVersion() + 1);

            log.info("状态流转成功: signatureId={}, {} -> {}, version={}",
                    signatureId, expectedOldStatus, newStatus, entity.getVersion());

            return true;
        }
    }

    /**
     * 查询签署状态
     */
    public String getStatus(String signatureId) {
        SignatureEntity entity = signatureDatabase.get(signatureId);
        return entity != null ? entity.getStatus() : null;
    }

    /**
     * 初始化签署记录 (用于测试)
     */
    public void initSignature(String signatureId) {
        SignatureEntity entity = new SignatureEntity();
        entity.setSignatureId(signatureId);
        entity.setStatus("PENDING");
        entity.setVersion(1);

        signatureDatabase.put(signatureId, entity);

        log.info("初始化签署记录: signatureId={}", signatureId);
    }

    /**
     * 清空所有签署 (用于测试)
     */
    public void clearAll() {
        signatureDatabase.clear();
        log.info("清空所有签署记录");
    }
}

/**
 * 签署实体
 *
 * 数据库表结构:
 * CREATE TABLE signature (
 *     signature_id VARCHAR(64) PRIMARY KEY,
 *     doctor_id VARCHAR(64) NOT NULL,
 *     status VARCHAR(32) NOT NULL,
 *     version INT NOT NULL DEFAULT 1,
 *     create_time DATETIME NOT NULL,
 *     update_time DATETIME NOT NULL
 * );
 */
class SignatureEntity {
    private String signatureId;
    private String status;
    private Integer version;

    // Getters and Setters
    public String getSignatureId() { return signatureId; }
    public void setSignatureId(String signatureId) { this.signatureId = signatureId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
