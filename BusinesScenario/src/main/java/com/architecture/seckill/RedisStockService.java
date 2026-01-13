package com.architecture.seckill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis库存服务
 *
 * 核心技术点：
 * 1. 使用Redis的DECR命令，原子性扣减库存
 * 2. DECR返回值为扣减后的值
 * 3. Redis单线程模型，天然支持高并发
 */
public class RedisStockService {

    private static final Logger logger = LoggerFactory.getLogger(RedisStockService.class);

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    private final JedisPool jedisPool;

    public RedisStockService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 设置商品库存
     *
     * @param productId 商品ID
     * @param stock 库存数量
     */
    public void setStock(Long productId, int stock) {
        String key = STOCK_KEY_PREFIX + productId;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, String.valueOf(stock));
            // 设置过期时间，防止Redis内存泄漏（秒杀活动一般1小时）
            jedis.expire(key, 3600);
        } catch (Exception e) {
            logger.error("设置库存失败, productId={}, stock={}", productId, stock, e);
            throw new RuntimeException("设置库存失败", e);
        }
    }

    /**
     * 获取当前库存
     *
     * @param productId 商品ID
     * @return 库存数量，不存在返回-1
     */
    public long getStock(Long productId) {
        String key = STOCK_KEY_PREFIX + productId;
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            return value == null ? -1 : Long.parseLong(value);
        } catch (Exception e) {
            logger.error("获取库存失败, productId={}", productId, e);
            return -1;
        }
    }

    /**
     * 原子性扣减库存
     *
     * @param productId 商品ID
     * @return 扣减后的库存数量
     */
    public long decrStock(Long productId) {
        String key = STOCK_KEY_PREFIX + productId;
        try (Jedis jedis = jedisPool.getResource()) {
            // DECR命令：原子性递减，返回递减后的值
            // 如果key不存在，先初始化为0再递减，返回-1
            Long result = jedis.decr(key);
            logger.debug("扣减库存, productId={}, remainStock={}", productId, result);
            return result;
        } catch (Exception e) {
            logger.error("扣减库存失败, productId={}", productId, e);
            throw new RuntimeException("扣减库存失败", e);
        }
    }

    /**
     * 回滚库存（消息发送失败时）
     *
     * @param productId 商品ID
     * @return 增加后的库存数量
     */
    public long incrStock(Long productId) {
        String key = STOCK_KEY_PREFIX + productId;
        try (Jedis jedis = jedisPool.getResource()) {
            Long result = jedis.incr(key);
            logger.debug("回滚库存, productId={}, remainStock={}", productId, result);
            return result;
        } catch (Exception e) {
            logger.error("回滚库存失败, productId={}", productId, e);
            throw new RuntimeException("回滚库存失败", e);
        }
    }

    /**
     * 使用Lua脚本扣减库存（更严谨的方案）
     *
     * 优势：
     * 1. 先判断库存是否充足，再扣减
     * 2. 整个过程原子性
     *
     * @param productId 商品ID
     * @param count 扣减数量
     * @return true-成功，false-库存不足
     */
    public boolean decrStockByLua(Long productId, int count) {
        String key = STOCK_KEY_PREFIX + productId;

        // Lua脚本：先判断库存，再扣减
        String luaScript =
                "local stock = redis.call('get', KEYS[1]) " +
                "if not stock or tonumber(stock) < tonumber(ARGV[1]) then " +
                "    return 0 " +
                "end " +
                "redis.call('decrby', KEYS[1], ARGV[1]) " +
                "return 1";

        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(luaScript, 1, key, String.valueOf(count));
            boolean success = "1".equals(result.toString());
            logger.debug("Lua脚本扣减库存, productId={}, count={}, success={}", productId, count, success);
            return success;
        } catch (Exception e) {
            logger.error("Lua脚本扣减库存失败, productId={}, count={}", productId, count, e);
            return false;
        }
    }
}
