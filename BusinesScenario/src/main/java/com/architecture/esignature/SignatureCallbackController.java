package com.architecture.esignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 电子签名回调接口
 * 场景: 易签宝完成签署后回调此接口
 */
@RestController
@RequestMapping("/api/signature")
public class SignatureCallbackController {

    private static final Logger log = LoggerFactory.getLogger(SignatureCallbackController.class);

    @Autowired
    private SignatureAsyncService asyncService;

    @Autowired
    private RedisDistributedLock redisLock;

    @Autowired
    private SignatureLogService logService;

    // ================ 问题代码: 会产生重复日志 ================

    /**
     * 有问题的实现: Redis锁释放太早,异步任务不在锁保护范围内
     * 问题: 两个请求都能获取锁,提交两个异步任务,产生两条日志
     */
    @PostMapping("/callback/v1-buggy")
    public Result handleCallbackBuggy(@RequestBody SignatureCallbackDTO dto) {
        String lockKey = "sign:lock:" + dto.getSignatureId();

        log.info("收到签署回调: signatureId={}, doctor={}",
                dto.getSignatureId(), dto.getDoctorName());

        try {
            // 获取Redis锁
            boolean locked = redisLock.tryLock(lockKey, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取锁失败: signatureId={}", dto.getSignatureId());
                return Result.fail("系统繁忙,请稍后重试");
            }

            log.info("获取锁成功,提交异步任务: signatureId={}", dto.getSignatureId());

            // 提交异步任务
            asyncService.processSignatureBuggy(dto);

            return Result.success("处理成功");

        } finally {
            // 问题所在: 立即释放锁,异步任务还未执行!
            redisLock.unlock(lockKey);
            log.info("释放锁: signatureId={}", dto.getSignatureId());
        }
    }

    // ================ 解决方案1: 数据库唯一约束 ================

    /**
     * 方案1: 数据库唯一约束 + 异常捕获
     * 优点: 简单可靠,数据库层面保证不重复
     * 适用: 所有场景(推荐作为基础方案)
     */
    @PostMapping("/callback/v2-unique-constraint")
    public Result handleCallbackWithUniqueConstraint(@RequestBody SignatureCallbackDTO dto) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("收到签署回调: signatureId={}, doctor={}, traceId={}",
                dto.getSignatureId(), dto.getDoctorName(), traceId);

        try {
            // 直接提交异步任务,不用担心重复
            dto.setTraceId(traceId);
            asyncService.processSignatureWithUniqueConstraint(dto);

            return Result.success("处理成功");

        } finally {
            MDC.clear();
        }
    }

    // ================ 解决方案2: 锁覆盖异步任务 ================

    /**
     * 方案2: 分布式锁包含异步任务执行
     * 优点: 强一致性,完全避免并发
     * 缺点: 性能略低(同步等待)
     * 适用: 对一致性要求极高的场景
     */
    @PostMapping("/callback/v3-lock-async")
    public Result handleCallbackWithLockAsync(@RequestBody SignatureCallbackDTO dto) {
        String lockKey = "sign:lock:" + dto.getSignatureId();

        log.info("收到签署回调: signatureId={}, doctor={}",
                dto.getSignatureId(), dto.getDoctorName());

        try {
            // 获取锁,超时时间要足够长,覆盖整个业务逻辑
            boolean locked = redisLock.tryLock(lockKey, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取锁失败,可能正在处理: signatureId={}", dto.getSignatureId());
                return Result.success("处理中");
            }

            log.info("获取锁成功: signatureId={}", dto.getSignatureId());

            // 同步执行业务逻辑(或者异步执行但等待完成)
            asyncService.processSignatureSync(dto);

            return Result.success("处理成功");

        } finally {
            redisLock.unlock(lockKey);
            log.info("释放锁: signatureId={}", dto.getSignatureId());
        }
    }

    // ================ 解决方案3: Token幂等性 ================

    /**
     * 方案3: Token幂等性机制
     * 优点: 天然防止重复回调,性能好
     * 适用: 防止第三方回调重试
     *
     * 使用流程:
     * 1. 发起签署时生成callbackToken,存储到Redis
     * 2. 将callbackToken传给易签宝
     * 3. 易签宝回调时带上callbackToken
     * 4. 验证token,删除成功说明是第一次,删除失败说明是重复回调
     */
    @PostMapping("/callback/v4-token")
    public Result handleCallbackWithToken(
            @RequestParam String signatureId,
            @RequestParam String callbackToken,
            @RequestBody SignatureCallbackDTO dto) {

        log.info("收到签署回调: signatureId={}, token={}, doctor={}",
                signatureId, callbackToken, dto.getDoctorName());

        // 验证并消费token
        String tokenKey = "callback:token:" + callbackToken;
        Boolean deleted = redisLock.deleteToken(tokenKey);

        if (Boolean.FALSE.equals(deleted)) {
            log.warn("重复回调(token已被消费): signatureId={}, token={}",
                    signatureId, callbackToken);
            return Result.success("重复请求,已忽略");
        }

        log.info("Token验证成功,处理签署: signatureId={}", signatureId);

        // 处理签署
        asyncService.processSignatureWithUniqueConstraint(dto);

        return Result.success("处理成功");
    }

    // ================ 解决方案4: 先查后插 ================

    /**
     * 方案4: 先查后插 + 分布式锁
     * 优点: 避免不必要的插入尝试
     * 缺点: 多一次查询,性能略低
     * 适用: 重复请求频繁的场景
     */
    @PostMapping("/callback/v5-check-then-insert")
    public Result handleCallbackCheckThenInsert(@RequestBody SignatureCallbackDTO dto) {
        String lockKey = "sign:lock:" + dto.getSignatureId();

        log.info("收到签署回调: signatureId={}, doctor={}",
                dto.getSignatureId(), dto.getDoctorName());

        try {
            boolean locked = redisLock.tryLock(lockKey, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取锁失败: signatureId={}", dto.getSignatureId());
                return Result.success("处理中");
            }

            // 先检查是否已经处理过
            boolean exists = logService.isLogExists(dto.getSignatureId());
            if (exists) {
                log.info("签署日志已存在,跳过处理: signatureId={}", dto.getSignatureId());
                return Result.success("已处理");
            }

            // 处理签署
            asyncService.processSignatureSync(dto);

            return Result.success("处理成功");

        } finally {
            redisLock.unlock(lockKey);
        }
    }

    // ================ 辅助方法: 模拟易签宝回调 ================

    /**
     * 测试接口: 模拟易签宝发起回调
     * 用于测试重复回调的情况
     */
    @GetMapping("/test/simulate-callback")
    public Result simulateCallback(@RequestParam String signatureId) {
        SignatureCallbackDTO dto = new SignatureCallbackDTO();
        dto.setSignatureId(signatureId);
        dto.setDoctorName("张三");
        dto.setDoctorId("DOC_001");
        dto.setSignatureTime(System.currentTimeMillis());

        log.info("模拟回调: signatureId={}", signatureId);

        // 模拟并发回调(两个线程同时调用)
        new Thread(() -> {
            try {
                handleCallbackBuggy(dto);
            } catch (Exception e) {
                log.error("回调失败", e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(50); // 稍微延迟,模拟网络延迟
                handleCallbackBuggy(dto);
            } catch (Exception e) {
                log.error("回调失败", e);
            }
        }).start();

        return Result.success("模拟回调已发送");
    }
}

/**
 * 签署回调DTO
 */
class SignatureCallbackDTO {
    private String signatureId;      // 签署ID
    private String doctorId;         // 医生ID
    private String doctorName;       // 医生姓名
    private Long signatureTime;      // 签署时间戳
    private String traceId;          // 链路追踪ID

    // Getters and Setters
    public String getSignatureId() { return signatureId; }
    public void setSignatureId(String signatureId) { this.signatureId = signatureId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Long getSignatureTime() { return signatureTime; }
    public void setSignatureTime(Long signatureTime) { this.signatureTime = signatureTime; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}

/**
 * 统一返回结果
 */
class Result {
    private int code;
    private String message;
    private Object data;

    public static Result success(String message) {
        Result result = new Result();
        result.code = 200;
        result.message = message;
        return result;
    }

    public static Result fail(String message) {
        Result result = new Result();
        result.code = 500;
        result.message = message;
        return result;
    }

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
