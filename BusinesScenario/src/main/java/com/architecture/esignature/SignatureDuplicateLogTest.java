package com.architecture.esignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 签署重复日志问题测试
 * 演示问题的重现和各种解决方案的效果
 */
public class SignatureDuplicateLogTest {

    private static final Logger log = LoggerFactory.getLogger(SignatureDuplicateLogTest.class);

    private final SignatureLogService logService;
    private final SignatureService signatureService;
    private final SignatureAsyncService asyncService;
    private final RedisDistributedLock redisLock;

    public SignatureDuplicateLogTest() {
        this.logService = new SignatureLogService();
        this.signatureService = new SignatureService();
        this.redisLock = new RedisDistributedLock();
        this.asyncService = createAsyncService();
    }

    /**
     * 创建异步服务 (手动注入依赖,因为不在Spring容器中)
     */
    private SignatureAsyncService createAsyncService() {
        SignatureAsyncService service = new SignatureAsyncService();
        // 通过反射注入依赖 (仅用于测试)
        try {
            java.lang.reflect.Field signatureServiceField =
                SignatureAsyncService.class.getDeclaredField("signatureService");
            signatureServiceField.setAccessible(true);
            signatureServiceField.set(service, signatureService);

            java.lang.reflect.Field logServiceField =
                SignatureAsyncService.class.getDeclaredField("logService");
            logServiceField.setAccessible(true);
            logServiceField.set(service, logService);

            java.lang.reflect.Field redisLockField =
                SignatureAsyncService.class.getDeclaredField("redisLock");
            redisLockField.setAccessible(true);
            redisLockField.set(service, redisLock);

        } catch (Exception e) {
            log.error("Failed to inject dependencies", e);
        }
        return service;
    }

    public static void main(String[] args) throws Exception {
        SignatureDuplicateLogTest test = new SignatureDuplicateLogTest();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("电子签名回调重复日志问题 - 测试演示");
        System.out.println("=".repeat(80) + "\n");

        // 测试1: 重现问题
        test.testBuggyVersion();

        // 测试2: 解决方案1 - 数据库唯一约束
        test.testUniqueConstraintSolution();

        // 测试3: 解决方案2 - 分布式锁
        test.testDistributedLockSolution();

        // 测试4: 解决方案3 - 状态机
        test.testStateMachineSolution();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试完成!");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * 测试1: 重现问题
     * 模拟易签宝的并发回调,展示重复日志问题
     */
    private void testBuggyVersion() throws Exception {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("【测试1】重现问题: 有问题的实现");
        System.out.println("-".repeat(80));

        // 清空数据
        logService.clearLogs();
        signatureService.clearAll();
        redisLock.clear();

        String signatureId = "SIGN_TEST_001";
        signatureService.initSignature(signatureId);

        System.out.println("场景: 易签宝同时发起2次回调 (模拟网络重试或并发)");
        System.out.println();

        // 模拟并发回调
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        for (int i = 1; i <= 2; i++) {
            final int requestNo = i;
            executor.submit(() -> {
                try {
                    log.info(">>> 请求{} 开始处理", requestNo);

                    // 模拟回调接口的处理逻辑
                    String lockKey = "sign:lock:" + signatureId;
                    boolean locked = redisLock.tryLock(lockKey, 10, TimeUnit.SECONDS);

                    if (locked) {
                        log.info(">>> 请求{} 获取锁成功,提交异步任务", requestNo);

                        // 提交异步任务
                        SignatureCallbackDTO dto = createDTO(signatureId);
                        asyncService.processSignatureBuggy(dto);

                        // 立即释放锁 (问题所在!)
                        redisLock.unlock(lockKey);
                        log.info(">>> 请求{} 释放锁", requestNo);
                    }

                } catch (Exception e) {
                    log.error("请求" + requestNo + "失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有请求完成
        latch.await();
        Thread.sleep(500); // 等待异步任务完成

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证结果
        int logCount = logService.countLogs(signatureId);
        System.out.println("\n结果验证:");
        System.out.println("  签署ID: " + signatureId);
        System.out.println("  日志数量: " + logCount + " (预期: 2条,因为有问题)");
        System.out.println();

        if (logCount > 1) {
            System.out.println("❌ 问题重现成功! 产生了重复日志!");
            System.out.println("原因: Redis锁在异步任务执行前就释放了,两个异步任务并发执行");
        } else {
            System.out.println("⚠️  未重现问题 (可能是时序问题)");
        }

        // 打印所有日志
        System.out.println("\n日志明细:");
        logService.getAllLogs().forEach(log -> {
            if (log.getSignatureId().equals(signatureId)) {
                System.out.println("  - " + log);
            }
        });
    }

    /**
     * 测试2: 解决方案1 - 数据库唯一约束
     */
    private void testUniqueConstraintSolution() throws Exception {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("【测试2】解决方案1: 数据库唯一约束");
        System.out.println("-".repeat(80));

        // 清空数据
        logService.clearLogs();
        signatureService.clearAll();
        redisLock.clear();

        String signatureId = "SIGN_TEST_002";
        signatureService.initSignature(signatureId);

        System.out.println("方案: 在signature_log表的signature_id字段添加唯一索引");
        System.out.println("SQL: ALTER TABLE signature_log ADD UNIQUE INDEX uk_signature_id (signature_id)");
        System.out.println();

        // 模拟并发回调
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int requestNo = i;
            executor.submit(() -> {
                try {
                    log.info(">>> 请求{} 开始处理", requestNo);

                    SignatureCallbackDTO dto = createDTO(signatureId);
                    asyncService.processSignatureWithUniqueConstraint(dto);

                } catch (Exception e) {
                    log.error("请求" + requestNo + "失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Thread.sleep(500);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证结果
        int logCount = logService.countLogs(signatureId);
        System.out.println("\n结果验证:");
        System.out.println("  签署ID: " + signatureId);
        System.out.println("  日志数量: " + logCount + " (预期: 1条)");
        System.out.println();

        if (logCount == 1) {
            System.out.println("✅ 解决方案有效! 只产生了1条日志!");
            System.out.println("说明: 数据库唯一约束成功阻止了重复插入");
        } else {
            System.out.println("❌ 解决方案无效,仍然产生了" + logCount + "条日志");
        }

        System.out.println("\n优点:");
        System.out.println("  ✅ 简单可靠,数据库层面保证");
        System.out.println("  ✅ 性能好,不需要额外的查询");
        System.out.println("  ✅ 天然支持并发");
        System.out.println("  ✅ 即使业务代码有bug也能兜底");
    }

    /**
     * 测试3: 解决方案2 - 分布式锁覆盖异步任务
     */
    private void testDistributedLockSolution() throws Exception {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("【测试3】解决方案2: 分布式锁覆盖异步任务");
        System.out.println("-".repeat(80));

        // 清空数据
        logService.clearLogs();
        signatureService.clearAll();
        redisLock.clear();

        String signatureId = "SIGN_TEST_003";
        signatureService.initSignature(signatureId);

        System.out.println("方案: 异步任务内部也获取分布式锁,确保同一时间只有一个任务执行");
        System.out.println();

        // 模拟并发回调
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int requestNo = i;
            executor.submit(() -> {
                try {
                    log.info(">>> 请求{} 开始处理", requestNo);

                    SignatureCallbackDTO dto = createDTO(signatureId);
                    asyncService.processSignatureWithLock(dto);

                } catch (Exception e) {
                    log.error("请求" + requestNo + "失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Thread.sleep(500);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证结果
        int logCount = logService.countLogs(signatureId);
        System.out.println("\n结果验证:");
        System.out.println("  签署ID: " + signatureId);
        System.out.println("  日志数量: " + logCount + " (预期: 1条)");
        System.out.println();

        if (logCount == 1) {
            System.out.println("✅ 解决方案有效! 只产生了1条日志!");
            System.out.println("说明: 分布式锁成功保证了串行执行");
        } else {
            System.out.println("❌ 解决方案无效,仍然产生了" + logCount + "条日志");
        }

        System.out.println("\n优点:");
        System.out.println("  ✅ 强一致性,完全避免并发");
        System.out.println("  ✅ 适合对一致性要求极高的场景");
        System.out.println("\n缺点:");
        System.out.println("  ⚠️  性能略低(串行执行)");
        System.out.println("  ⚠️  依赖Redis,Redis故障会影响业务");
    }

    /**
     * 测试4: 解决方案3 - 状态机
     */
    private void testStateMachineSolution() throws Exception {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("【测试4】解决方案3: 状态机");
        System.out.println("-".repeat(80));

        // 清空数据
        logService.clearLogs();
        signatureService.clearAll();
        redisLock.clear();

        String signatureId = "SIGN_TEST_004";
        signatureService.initSignature(signatureId);

        System.out.println("方案: 使用数据库CAS操作,只有PENDING状态才能流转到COMPLETED");
        System.out.println("SQL: UPDATE signature SET status='COMPLETED' WHERE signature_id=? AND status='PENDING'");
        System.out.println();

        // 模拟并发回调
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int requestNo = i;
            executor.submit(() -> {
                try {
                    log.info(">>> 请求{} 开始处理", requestNo);

                    SignatureCallbackDTO dto = createDTO(signatureId);
                    asyncService.processSignatureWithStateMachine(dto);

                } catch (Exception e) {
                    log.error("请求" + requestNo + "失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Thread.sleep(500);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证结果
        int logCount = logService.countLogs(signatureId);
        String finalStatus = signatureService.getStatus(signatureId);

        System.out.println("\n结果验证:");
        System.out.println("  签署ID: " + signatureId);
        System.out.println("  最终状态: " + finalStatus);
        System.out.println("  日志数量: " + logCount + " (预期: 1条)");
        System.out.println();

        if (logCount == 1 && "COMPLETED".equals(finalStatus)) {
            System.out.println("✅ 解决方案有效! 状态正确流转,只产生了1条日志!");
            System.out.println("说明: 状态机的CAS操作保证了只有一个请求能成功流转状态");
        } else {
            System.out.println("❌ 解决方案无效,仍然产生了" + logCount + "条日志");
        }

        System.out.println("\n优点:");
        System.out.println("  ✅ 利用数据库的原子性,简单可靠");
        System.out.println("  ✅ 不依赖外部组件(如Redis)");
        System.out.println("  ✅ 适合有明确状态流转的业务场景");
        System.out.println("\n适用场景:");
        System.out.println("  - 订单状态流转: 待支付 -> 已支付 -> 已发货 -> 已完成");
        System.out.println("  - 审批流程: 待审批 -> 审批中 -> 已通过 -> 已归档");
    }

    /**
     * 创建测试DTO
     */
    private SignatureCallbackDTO createDTO(String signatureId) {
        SignatureCallbackDTO dto = new SignatureCallbackDTO();
        dto.setSignatureId(signatureId);
        dto.setDoctorId("DOC_001");
        dto.setDoctorName("张三医生");
        dto.setSignatureTime(System.currentTimeMillis());
        return dto;
    }
}
