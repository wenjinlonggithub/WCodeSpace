package com.architecture.esignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 签署异步处理服务
 * 负责更新签署状态和记录日志
 */
@Service
public class SignatureAsyncService {

    private static final Logger log = LoggerFactory.getLogger(SignatureAsyncService.class);

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private SignatureLogService logService;

    @Autowired
    private RedisDistributedLock redisLock;

    // ================ 问题代码: 会产生重复日志 ================

    /**
     * 有问题的异步处理
     * 问题: 没有任何保护措施,并发执行时会产生重复日志
     */
    @Async
    public void processSignatureBuggy(SignatureCallbackDTO dto) {
        log.info("异步任务开始: signatureId={}, thread={}",
                dto.getSignatureId(), Thread.currentThread().getName());

        try {
            // 模拟业务处理耗时
            Thread.sleep(100);

            // 更新签署状态
            signatureService.updateStatus(dto.getSignatureId(), "COMPLETED");

            // 记录日志 (问题: 没有检查是否已存在,直接插入)
            logService.recordLogDirectly(dto);

            log.info("异步任务完成: signatureId={}", dto.getSignatureId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("异步任务被中断: signatureId={}", dto.getSignatureId(), e);
        } catch (Exception e) {
            log.error("异步任务执行失败: signatureId={}", dto.getSignatureId(), e);
        }
    }

    // ================ 解决方案1: 数据库唯一约束 ================

    /**
     * 方案1: 使用数据库唯一约束防止重复
     * 优点: 简单可靠,性能好
     */
    @Async
    public void processSignatureWithUniqueConstraint(SignatureCallbackDTO dto) {
        // 恢复traceId(用于日志追踪)
        if (dto.getTraceId() != null) {
            MDC.put("traceId", dto.getTraceId());
        }

        log.info("异步任务开始: signatureId={}, thread={}",
                dto.getSignatureId(), Thread.currentThread().getName());

        try {
            // 模拟业务处理
            Thread.sleep(100);

            // 更新签署状态
            signatureService.updateStatus(dto.getSignatureId(), "COMPLETED");

            // 记录日志 (内部会捕获DuplicateKeyException)
            logService.recordLogWithUniqueConstraint(dto);

            log.info("异步任务完成: signatureId={}", dto.getSignatureId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("异步任务被中断: signatureId={}", dto.getSignatureId(), e);
        } catch (Exception e) {
            log.error("异步任务执行失败: signatureId={}", dto.getSignatureId(), e);
        } finally {
            MDC.clear();
        }
    }

    // ================ 解决方案2: 分布式锁 + 先查后插 ================

    /**
     * 方案2: 分布式锁保护整个异步任务
     * 优点: 完全避免并发,强一致性
     * 缺点: 性能略低
     */
    @Async
    public void processSignatureWithLock(SignatureCallbackDTO dto) {
        String lockKey = "sign:process:" + dto.getSignatureId();

        log.info("异步任务开始: signatureId={}, thread={}",
                dto.getSignatureId(), Thread.currentThread().getName());

        try {
            // 获取锁 (注意: 异步任务也需要获取锁!)
            boolean locked = redisLock.tryLock(lockKey, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取锁失败,任务可能已被其他线程处理: signatureId={}",
                        dto.getSignatureId());
                return;
            }

            log.info("获取锁成功,开始处理: signatureId={}", dto.getSignatureId());

            // 先检查是否已经处理过
            boolean exists = logService.isLogExists(dto.getSignatureId());
            if (exists) {
                log.info("签署日志已存在,跳过处理: signatureId={}", dto.getSignatureId());
                return;
            }

            // 模拟业务处理
            Thread.sleep(100);

            // 更新签署状态
            signatureService.updateStatus(dto.getSignatureId(), "COMPLETED");

            // 记录日志
            logService.recordLogWithUniqueConstraint(dto);

            log.info("异步任务完成: signatureId={}", dto.getSignatureId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("异步任务被中断: signatureId={}", dto.getSignatureId(), e);
        } catch (Exception e) {
            log.error("异步任务执行失败: signatureId={}", dto.getSignatureId(), e);
        } finally {
            redisLock.unlock(lockKey);
            log.info("释放锁: signatureId={}", dto.getSignatureId());
        }
    }

    // ================ 解决方案3: 同步执行 ================

    /**
     * 方案3: 同步执行(不使用@Async)
     * 优点: 简单,调用方的锁可以直接保护
     * 缺点: 阻塞调用方,响应时间变长
     * 适用: 业务逻辑快,对响应时间要求不高的场景
     */
    public void processSignatureSync(SignatureCallbackDTO dto) {
        log.info("同步处理开始: signatureId={}", dto.getSignatureId());

        try {
            // 先检查是否已经处理过
            boolean exists = logService.isLogExists(dto.getSignatureId());
            if (exists) {
                log.info("签署日志已存在,跳过处理: signatureId={}", dto.getSignatureId());
                return;
            }

            // 模拟业务处理
            Thread.sleep(50);

            // 更新签署状态
            signatureService.updateStatus(dto.getSignatureId(), "COMPLETED");

            // 记录日志
            logService.recordLogWithUniqueConstraint(dto);

            log.info("同步处理完成: signatureId={}", dto.getSignatureId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("处理被中断: signatureId={}", dto.getSignatureId(), e);
        } catch (Exception e) {
            log.error("处理失败: signatureId={}", dto.getSignatureId(), e);
            throw new RuntimeException("签署处理失败", e);
        }
    }

    // ================ 解决方案4: 状态机 ================

    /**
     * 方案4: 使用状态机保证只处理一次
     * 优点: 利用数据库的CAS操作,天然防重复
     * 适用: 有明确状态流转的业务场景
     */
    @Async
    public void processSignatureWithStateMachine(SignatureCallbackDTO dto) {
        log.info("异步任务开始(状态机): signatureId={}", dto.getSignatureId());

        try {
            // 尝试状态流转: PENDING -> COMPLETED
            boolean success = signatureService.transitionStatus(
                dto.getSignatureId(),
                "PENDING",
                "COMPLETED"
            );

            if (!success) {
                log.info("状态已经是COMPLETED,跳过处理: signatureId={}",
                        dto.getSignatureId());
                return;
            }

            log.info("状态流转成功,继续处理: signatureId={}", dto.getSignatureId());

            // 模拟业务处理
            Thread.sleep(100);

            // 记录日志 (状态流转成功后才记录)
            logService.recordLogWithUniqueConstraint(dto);

            log.info("异步任务完成(状态机): signatureId={}", dto.getSignatureId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("异步任务被中断: signatureId={}", dto.getSignatureId(), e);
        } catch (Exception e) {
            log.error("异步任务执行失败: signatureId={}", dto.getSignatureId(), e);

            // 回滚状态
            signatureService.updateStatus(dto.getSignatureId(), "FAILED");
        }
    }
}
