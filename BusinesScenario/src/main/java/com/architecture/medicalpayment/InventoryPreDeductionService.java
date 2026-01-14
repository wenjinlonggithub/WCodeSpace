package com.architecture.medicalpayment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 库存预扣服务
 *
 * 实现方案2：预扣库存+补偿机制
 * 核心功能：
 * 1. 预扣库存：下单时冻结库存
 * 2. 确认扣库存：支付成功后真实扣减
 * 3. 释放库存：支付失败或超时释放
 *
 * @author architecture
 */
@Slf4j
@Service
public class InventoryPreDeductionService {

    @Autowired
    private HisInventoryClient hisInventoryClient;

    @Autowired
    private InventoryPreDeductionRepositoryV2 preDeductionRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "inventory:lock:";
    private static final int PRE_DEDUCTION_EXPIRE_MINUTES = 30;

    /**
     * 预扣库存
     *
     * 业务流程：
     * 1. 调用HIS接口预扣库存（HIS标记为冻结状态）
     * 2. 本地记录预扣记录
     * 3. 设置Redis分布式锁，防止重复预扣
     *
     * @param orderNo 预订单号
     * @param drugId 药品ID
     * @param quantity 数量
     * @return 预扣结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PreDeductionResult preDeduct(String orderNo, Long drugId, Integer quantity) {
        log.info("开始预扣库存, orderNo={}, drugId={}, quantity={}", orderNo, drugId, quantity);

        // 1. 幂等性校验：防止重复预扣
        InventoryPreDeduction existRecord = preDeductionRepository.findByOrderNo(orderNo);
        if (existRecord != null) {
            log.warn("订单已存在预扣记录, orderNo={}, status={}", orderNo, existRecord.getStatus());
            return PreDeductionResult.builder()
                    .success(existRecord.getStatus() == PreDeductionStatus.LOCKED)
                    .message("订单已存在预扣记录")
                    .hisLockId(existRecord.getHisLockId())
                    .build();
        }

        // 2. 获取分布式锁，防止并发问题
        String lockKey = LOCK_KEY_PREFIX + drugId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, orderNo, 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            log.warn("获取库存锁失败，当前有其他订单正在处理, drugId={}", drugId);
            return PreDeductionResult.builder()
                    .success(false)
                    .message("系统繁忙，请稍后重试")
                    .build();
        }

        try {
            // 3. 调用HIS接口预扣库存
            HisInventoryLockResponse hisResponse = hisInventoryClient.lockInventory(
                    HisInventoryLockRequest.builder()
                            .orderNo(orderNo)
                            .drugId(drugId)
                            .quantity(quantity)
                            .expireMinutes(PRE_DEDUCTION_EXPIRE_MINUTES)
                            .build()
            );

            if (!hisResponse.isSuccess()) {
                log.warn("HIS预扣库存失败, orderNo={}, drugId={}, message={}",
                        orderNo, drugId, hisResponse.getMessage());
                return PreDeductionResult.builder()
                        .success(false)
                        .message(hisResponse.getMessage())
                        .build();
            }

            // 4. 保存预扣记录
            InventoryPreDeduction preDeduction = new InventoryPreDeduction();
            preDeduction.setOrderNo(orderNo);
            preDeduction.setDrugId(drugId);
            preDeduction.setQuantity(quantity);
            preDeduction.setHisLockId(hisResponse.getLockId());
            preDeduction.setStatus(PreDeductionStatus.LOCKED);
            preDeduction.setExpireTime(LocalDateTime.now().plusMinutes(PRE_DEDUCTION_EXPIRE_MINUTES));
            preDeduction.setCreatedTime(LocalDateTime.now());
            preDeduction.setUpdatedTime(LocalDateTime.now());

            preDeductionRepository.save(preDeduction);

            log.info("预扣库存成功, orderNo={}, drugId={}, hisLockId={}",
                    orderNo, drugId, hisResponse.getLockId());

            return PreDeductionResult.builder()
                    .success(true)
                    .message("预扣库存成功")
                    .hisLockId(hisResponse.getLockId())
                    .build();

        } catch (Exception e) {
            log.error("预扣库存异常, orderNo={}, drugId={}", orderNo, drugId, e);
            return PreDeductionResult.builder()
                    .success(false)
                    .message("系统异常，请稍后重试")
                    .build();
        } finally {
            // 5. 释放分布式锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 确认扣库存
     *
     * 业务场景：支付成功回调后调用
     * 流程：
     * 1. 查询预扣记录
     * 2. 调用HIS接口确认扣库存（将冻结库存转为真实扣减）
     * 3. 更新预扣记录状态为已确认
     *
     * @param orderNo 订单号
     * @return 确认结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfirmDeductionResult confirmDeduction(String orderNo) {
        log.info("开始确认扣库存, orderNo={}", orderNo);

        // 1. 查询预扣记录
        InventoryPreDeduction preDeduction = preDeductionRepository.findByOrderNo(orderNo);
        if (preDeduction == null) {
            log.error("预扣记录不存在, orderNo={}", orderNo);
            return ConfirmDeductionResult.builder()
                    .success(false)
                    .message("预扣记录不存在")
                    .build();
        }

        // 2. 幂等性校验
        if (preDeduction.getStatus() == PreDeductionStatus.CONFIRMED) {
            log.warn("库存已确认扣减，无需重复操作, orderNo={}", orderNo);
            return ConfirmDeductionResult.builder()
                    .success(true)
                    .message("库存已确认扣减")
                    .build();
        }

        // 3. 状态校验
        if (preDeduction.getStatus() != PreDeductionStatus.LOCKED) {
            log.error("预扣记录状态异常, orderNo={}, status={}", orderNo, preDeduction.getStatus());
            return ConfirmDeductionResult.builder()
                    .success(false)
                    .message("预扣记录状态异常")
                    .build();
        }

        try {
            // 4. 调用HIS接口确认扣库存
            HisInventoryConfirmResponse hisResponse = hisInventoryClient.confirmDeduction(
                    HisInventoryConfirmRequest.builder()
                            .orderNo(orderNo)
                            .lockId(preDeduction.getHisLockId())
                            .build()
            );

            if (!hisResponse.isSuccess()) {
                log.error("HIS确认扣库存失败, orderNo={}, message={}", orderNo, hisResponse.getMessage());

                // 如果HIS返回库存不足，记录异常，需要人工介入或自动退款
                if ("INSUFFICIENT_INVENTORY".equals(hisResponse.getErrorCode())) {
                    // 发送告警
                    sendAlert(orderNo, "支付成功但HIS库存不足");
                }

                return ConfirmDeductionResult.builder()
                        .success(false)
                        .message(hisResponse.getMessage())
                        .needRefund(true) // 需要退款
                        .build();
            }

            // 5. 更新预扣记录状态
            preDeduction.setStatus(PreDeductionStatus.CONFIRMED);
            preDeduction.setUpdatedTime(LocalDateTime.now());
            preDeductionRepository.save(preDeduction);

            log.info("确认扣库存成功, orderNo={}", orderNo);

            return ConfirmDeductionResult.builder()
                    .success(true)
                    .message("确认扣库存成功")
                    .needRefund(false)
                    .build();

        } catch (Exception e) {
            log.error("确认扣库存异常, orderNo={}", orderNo, e);

            // 发送告警
            sendAlert(orderNo, "确认扣库存异常: " + e.getMessage());

            return ConfirmDeductionResult.builder()
                    .success(false)
                    .message("系统异常")
                    .needRefund(true)
                    .build();
        }
    }

    /**
     * 释放库存
     *
     * 业务场景：
     * 1. 用户支付失败
     * 2. 用户支付超时
     * 3. 补偿任务扫描到超时的预扣记录
     *
     * @param orderNo 订单号
     * @return 释放结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ReleaseInventoryResult releaseInventory(String orderNo) {
        log.info("开始释放库存, orderNo={}", orderNo);

        // 1. 查询预扣记录
        InventoryPreDeduction preDeduction = preDeductionRepository.findByOrderNo(orderNo);
        if (preDeduction == null) {
            log.warn("预扣记录不存在, orderNo={}", orderNo);
            return ReleaseInventoryResult.builder()
                    .success(true)
                    .message("预扣记录不存在，无需释放")
                    .build();
        }

        // 2. 幂等性校验
        if (preDeduction.getStatus() == PreDeductionStatus.RELEASED) {
            log.warn("库存已释放，无需重复操作, orderNo={}", orderNo);
            return ReleaseInventoryResult.builder()
                    .success(true)
                    .message("库存已释放")
                    .build();
        }

        // 3. 状态校验：已确认的不能释放
        if (preDeduction.getStatus() == PreDeductionStatus.CONFIRMED) {
            log.error("库存已确认扣减，不能释放, orderNo={}", orderNo);
            return ReleaseInventoryResult.builder()
                    .success(false)
                    .message("库存已确认扣减，不能释放")
                    .build();
        }

        try {
            // 4. 调用HIS接口释放库存
            HisInventoryReleaseResponse hisResponse = hisInventoryClient.releaseInventory(
                    HisInventoryReleaseRequest.builder()
                            .orderNo(orderNo)
                            .lockId(preDeduction.getHisLockId())
                            .build()
            );

            if (!hisResponse.isSuccess()) {
                log.warn("HIS释放库存失败, orderNo={}, message={}", orderNo, hisResponse.getMessage());
                // 释放失败不影响业务，记录日志即可
            }

            // 5. 更新预扣记录状态
            preDeduction.setStatus(PreDeductionStatus.RELEASED);
            preDeduction.setUpdatedTime(LocalDateTime.now());
            preDeductionRepository.save(preDeduction);

            log.info("释放库存成功, orderNo={}", orderNo);

            return ReleaseInventoryResult.builder()
                    .success(true)
                    .message("释放库存成功")
                    .build();

        } catch (Exception e) {
            log.error("释放库存异常, orderNo={}", orderNo, e);
            return ReleaseInventoryResult.builder()
                    .success(false)
                    .message("系统异常")
                    .build();
        }
    }

    /**
     * 发送告警
     */
    private void sendAlert(String orderNo, String message) {
        // TODO: 接入告警系统（钉钉、邮件、短信等）
        log.error("【告警】订单异常, orderNo={}, message={}", orderNo, message);
    }
}

// ============== 以下是相关的DTO和实体类 ==============

/**
 * 预扣库存结果
 */
@lombok.Data
@lombok.Builder
class PreDeductionResult {
    private boolean success;
    private String message;
    private String hisLockId;
}

/**
 * 确认扣库存结果
 */
@lombok.Data
@lombok.Builder
class ConfirmDeductionResult {
    private boolean success;
    private String message;
    private boolean needRefund; // 是否需要退款
}

/**
 * 释放库存结果
 */
@lombok.Data
@lombok.Builder
class ReleaseInventoryResult {
    private boolean success;
    private String message;
}

/**
 * 预扣库存状态
 */
enum PreDeductionStatus {
    LOCKED,     // 已锁定
    CONFIRMED,  // 已确认
    RELEASED    // 已释放
}

/**
 * 预扣库存记录实体
 */
@lombok.Data
class InventoryPreDeduction {
    private Long id;
    private String orderNo;
    private Long drugId;
    private Integer quantity;
    private String hisLockId;
    private PreDeductionStatus status;
    private LocalDateTime expireTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

// ============== HIS接口相关类（模拟） ==============

interface HisInventoryClient {
    HisInventoryLockResponse lockInventory(HisInventoryLockRequest request);
    HisInventoryConfirmResponse confirmDeduction(HisInventoryConfirmRequest request);
    HisInventoryReleaseResponse releaseInventory(HisInventoryReleaseRequest request);
}

@lombok.Data
@lombok.Builder
class HisInventoryLockRequest {
    private String orderNo;
    private Long drugId;
    private Integer quantity;
    private Integer expireMinutes;
}

@lombok.Data
class HisInventoryLockResponse {
    private boolean success;
    private String message;
    private String lockId; // HIS返回的锁ID
}

@lombok.Data
@lombok.Builder
class HisInventoryConfirmRequest {
    private String orderNo;
    private String lockId;
}

@lombok.Data
class HisInventoryConfirmResponse {
    private boolean success;
    private String message;
    private String errorCode;
}

@lombok.Data
@lombok.Builder
class HisInventoryReleaseRequest {
    private String orderNo;
    private String lockId;
}

@lombok.Data
class HisInventoryReleaseResponse {
    private boolean success;
    private String message;
}

interface InventoryPreDeductionRepositoryV2 {
    InventoryPreDeduction findByOrderNo(String orderNo);
    void save(InventoryPreDeduction preDeduction);
}
