package com.architecture.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务Feign客户端
 *
 * 库存服务职责:
 * 1. 商品库存查询
 * 2. 库存扣减(下单时)
 * 3. 库存回滚(订单取消时)
 *
 * 注意事项:
 * - 库存扣减操作是非幂等的，不能重试
 * - 需要考虑并发扣减的原子性
 * - 扣减失败需要有补偿机制
 *
 * @author architecture
 */
@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {

    /**
     * 扣减库存
     *
     * 实现原理:
     * UPDATE inventory
     * SET stock = stock - #{quantity}
     * WHERE product_id = #{productId}
     *   AND stock >= #{quantity}
     *
     * 并发控制:
     * 1. 使用数据库行锁(SELECT FOR UPDATE)
     * 2. 使用Redis分布式锁
     * 3. 使用版本号乐观锁
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return true-成功, false-失败(库存不足)
     */
    @PostMapping("/inventory/decrease")
    Boolean decreaseStock(
        @RequestParam("productId") Long productId,
        @RequestParam("quantity") Integer quantity
    );

    /**
     * 增加库存(回滚操作)
     *
     * 使用场景:
     * 1. 订单取消后归还库存
     * 2. 支付超时后归还库存
     * 3. 订单创建失败后归还库存
     *
     * @param productId 商品ID
     * @param quantity 增加数量
     * @return true-成功, false-失败
     */
    @PostMapping("/inventory/increase")
    Boolean increaseStock(
        @RequestParam("productId") Long productId,
        @RequestParam("quantity") Integer quantity
    );

    /**
     * 查询库存
     *
     * @param productId 商品ID
     * @return 剩余库存数量
     */
    @PostMapping("/inventory/query")
    Integer getStock(@RequestParam("productId") Long productId);
}
