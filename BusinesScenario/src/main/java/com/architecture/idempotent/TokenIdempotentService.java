package com.architecture.idempotent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.UUID;

/**
 * 基于Token的幂等性实现
 *
 * 实现流程：
 * 1. 客户端请求获取Token
 * 2. 服务端生成Token并存储到Redis
 * 3. 客户端携带Token请求业务接口
 * 4. 服务端验证并删除Token（Lua脚本保证原子性）
 * 5. Token存在则执行业务，不存在则拒绝（重复请求）
 *
 * 适用场景：
 * - 前端重复点击
 * - 表单重复提交
 */
public class TokenIdempotentService {

    private static final Logger logger = LoggerFactory.getLogger(TokenIdempotentService.class);

    private static final String TOKEN_KEY_PREFIX = "idempotent:token:";
    private static final int TOKEN_EXPIRE_TIME = 300; // Token有效期5分钟

    private final JedisPool jedisPool;

    // Lua脚本：验证并删除Token（原子性）
    private static final String VERIFY_AND_DELETE_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    redis.call('del', KEYS[1]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    public TokenIdempotentService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 生成Token
     *
     * @return Token字符串
     */
    public String createToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_KEY_PREFIX + token;

        try (Jedis jedis = jedisPool.getResource()) {
            // 存储Token到Redis，设置过期时间
            jedis.setex(key, TOKEN_EXPIRE_TIME, token);
            logger.info("生成幂等Token: {}", token);
            return token;
        } catch (Exception e) {
            logger.error("生成Token失败", e);
            throw new RuntimeException("生成Token失败", e);
        }
    }

    /**
     * 验证并删除Token（原子性操作）
     *
     * @param token Token字符串
     * @return true-验证成功（首次请求），false-验证失败（重复请求）
     */
    public boolean checkAndDeleteToken(String token) {
        if (token == null || token.isEmpty()) {
            logger.warn("Token为空");
            return false;
        }

        String key = TOKEN_KEY_PREFIX + token;

        try (Jedis jedis = jedisPool.getResource()) {
            // 使用Lua脚本保证原子性
            Object result = jedis.eval(
                    VERIFY_AND_DELETE_SCRIPT,
                    Collections.singletonList(key),
                    Collections.singletonList(token)
            );

            boolean success = Long.valueOf(1).equals(result);

            if (success) {
                logger.info("Token验证成功（首次请求）: {}", token);
            } else {
                logger.warn("Token验证失败（重复请求或已过期）: {}", token);
            }

            return success;
        } catch (Exception e) {
            logger.error("验证Token失败, token={}", token, e);
            // 异常时拒绝请求（fail-close策略）
            return false;
        }
    }

    /**
     * 使用示例：订单创建接口
     */
    public static class OrderController {

        private final TokenIdempotentService idempotentService;

        public OrderController(TokenIdempotentService idempotentService) {
            this.idempotentService = idempotentService;
        }

        /**
         * 获取Token接口
         *
         * 前端在进入下单页面时调用此接口获取Token
         */
        public String getToken() {
            return idempotentService.createToken();
        }

        /**
         * 创建订单接口
         *
         * 前端提交订单时携带Token
         */
        public String createOrder(CreateOrderRequest request) {
            // 1. 验证并删除Token
            boolean valid = idempotentService.checkAndDeleteToken(request.getToken());

            if (!valid) {
                return "重复提交，请勿重复下单";
            }

            // 2. 执行业务逻辑
            try {
                // 创建订单
                String orderId = doCreateOrder(request);
                logger.info("订单创建成功: {}", orderId);
                return orderId;

            } catch (Exception e) {
                logger.error("订单创建失败", e);

                // 业务执行失败，需要回滚Token（重新生成一个）
                // 或者返回错误，让用户重新获取Token
                throw e;
            }
        }

        private String doCreateOrder(CreateOrderRequest request) {
            // 实际的订单创建逻辑
            return "ORDER_" + System.currentTimeMillis();
        }
    }

    /**
     * 创建订单请求
     */
    public static class CreateOrderRequest {
        private String token;
        private String userId;
        private String productId;
        private int quantity;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        // 其他getter/setter省略...
    }

    /**
     * 完整流程示例
     */
    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        TokenIdempotentService idempotentService = new TokenIdempotentService(jedisPool);
        OrderController controller = new OrderController(idempotentService);

        // 1. 用户进入下单页面，前端调用获取Token
        String token = controller.getToken();
        logger.info("前端获取Token: {}", token);

        // 2. 用户填写订单信息并提交（第一次）
        CreateOrderRequest request = new CreateOrderRequest();
        request.setToken(token);
        //request.setUserId("user123");
        //request.setProductId("product456");

        String result1 = controller.createOrder(request);
        logger.info("第一次提交结果: {}", result1);

        // 3. 用户由于网络问题，又点击了一次提交按钮（第二次）
        String result2 = controller.createOrder(request);
        logger.info("第二次提交结果: {}", result2); // 应该返回"重复提交"

        jedisPool.close();
    }
}
