package com.architecture.seckill;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务核心实现
 *
 * 技术要点：
 * 1. Redis预减库存（原子性）
 * 2. 本地缓存防止重复请求
 * 3. 消息队列异步下单
 * 4. 数据库乐观锁防超卖
 */
public class SeckillService {

    private static final Logger logger = LoggerFactory.getLogger(SeckillService.class);

    private final RedisStockService redisStockService;
    private final OrderQueueService orderQueueService;

    // 本地缓存：记录已经秒杀成功的用户，防止重复请求Redis
    // Key: userId_productId, Value: true
    private final Cache<String, Boolean> localSuccessCache;

    // 本地缓存：记录已售罄的商品，防止无效请求
    private final Cache<Long, Boolean> soldOutCache;

    public SeckillService(RedisStockService redisStockService, OrderQueueService orderQueueService) {
        this.redisStockService = redisStockService;
        this.orderQueueService = orderQueueService;

        // 本地缓存配置：最多10万条记录，5分钟过期
        this.localSuccessCache = CacheBuilder.newBuilder()
                .maximumSize(100000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();

        this.soldOutCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    /**
     * 秒杀接口
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 秒杀结果
     */
    public SeckillResult doSeckill(Long userId, Long productId) {

        // 1. 检查本地缓存：商品是否已售罄
        if (soldOutCache.getIfPresent(productId) != null) {
            logger.info("商品已售罄(本地缓存), productId={}", productId);
            return SeckillResult.fail("商品已售罄");
        }

        // 2. 检查本地缓存：用户是否已经秒杀成功过
        String userProductKey = userId + "_" + productId;
        if (localSuccessCache.getIfPresent(userProductKey) != null) {
            logger.info("用户重复请求, userId={}, productId={}", userId, productId);
            return SeckillResult.fail("请勿重复抢购");
        }

        // 3. Redis预减库存（原子操作）
        long remainStock = redisStockService.decrStock(productId);

        if (remainStock < 0) {
            // 库存不足，标记售罄
            soldOutCache.put(productId, true);
            logger.info("商品已售罄, productId={}", productId);
            return SeckillResult.fail("商品已售罄");
        }

        // 4. 库存扣减成功，记录到本地缓存
        localSuccessCache.put(userProductKey, true);

        // 5. 发送到消息队列，异步创建订单
        SeckillMessage message = new SeckillMessage(userId, productId, System.currentTimeMillis());
        boolean sendResult = orderQueueService.sendOrder(message);

        if (!sendResult) {
            // 消息发送失败，回滚Redis库存
            redisStockService.incrStock(productId);
            localSuccessCache.invalidate(userProductKey);
            logger.error("消息队列发送失败, userId={}, productId={}", userId, productId);
            return SeckillResult.fail("系统繁忙，请稍后再试");
        }

        logger.info("秒杀请求成功, userId={}, productId={}, remainStock={}", userId, productId, remainStock);
        return SeckillResult.success("抢购成功，订单生成中...");
    }

    /**
     * 初始化商品库存到Redis
     *
     * @param productId 商品ID
     * @param stock 库存数量
     */
    public void initStock(Long productId, int stock) {
        redisStockService.setStock(productId, stock);
        soldOutCache.invalidate(productId);
        logger.info("初始化库存成功, productId={}, stock={}", productId, stock);
    }

    /**
     * 秒杀结果
     */
    public static class SeckillResult {
        private boolean success;
        private String message;

        public static SeckillResult success(String message) {
            SeckillResult result = new SeckillResult();
            result.success = true;
            result.message = message;
            return result;
        }

        public static SeckillResult fail(String message) {
            SeckillResult result = new SeckillResult();
            result.success = false;
            result.message = message;
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 秒杀消息
     */
    public static class SeckillMessage {
        private Long userId;
        private Long productId;
        private Long timestamp;

        public SeckillMessage(Long userId, Long productId, Long timestamp) {
            this.userId = userId;
            this.productId = productId;
            this.timestamp = timestamp;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getProductId() {
            return productId;
        }

        public Long getTimestamp() {
            return timestamp;
        }
    }
}
