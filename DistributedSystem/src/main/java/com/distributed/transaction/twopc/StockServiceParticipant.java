package com.distributed.transaction.twopc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存服务参与者
 * 模拟库存扣减操作
 */
public class StockServiceParticipant implements Participant {

    private final String name = "StockService";

    // 模拟库存数据库
    private final Map<Long, Integer> productStocks = new ConcurrentHashMap<>();

    // 模拟预留库存（用于准备阶段）
    private final Map<String, ReservedStock> reservedStocks = new ConcurrentHashMap<>();

    public StockServiceParticipant() {
        // 初始化一些测试数据
        productStocks.put(1001L, 100);
        productStocks.put(1002L, 50);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean prepare(String transactionId) {
        try {
            // 模拟业务逻辑：检查库存并预留
            Long productId = 1001L;
            Integer quantity = 5;

            System.out.println("  [" + name + "] 检查库存...");
            Integer stock = productStocks.get(productId);

            if (stock == null) {
                System.out.println("  [" + name + "] 商品不存在");
                return false;
            }

            if (stock < quantity) {
                System.out.println("  [" + name + "] 库存不足: 当前库存=" + stock + ", 需要扣减=" + quantity);
                return false;
            }

            // 预留库存
            reservedStocks.put(transactionId, new ReservedStock(productId, quantity));
            System.out.println("  [" + name + "] 预留库存成功: productId=" + productId + ", quantity=" + quantity);

            // 模拟网络延迟
            Thread.sleep(100);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] 准备阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void commit(String transactionId) {
        try {
            ReservedStock reserved = reservedStocks.get(transactionId);
            if (reserved == null) {
                System.err.println("  [" + name + "] 未找到预留库存记录");
                return;
            }

            // 扣减库存
            Integer stock = productStocks.get(reserved.productId);
            Integer newStock = stock - reserved.quantity;
            productStocks.put(reserved.productId, newStock);

            System.out.println("  [" + name + "] 扣减库存成功: productId=" + reserved.productId +
                    ", 扣减数量=" + reserved.quantity + ", 新库存=" + newStock);

            // 清除预留记录
            reservedStocks.remove(transactionId);

            // 模拟网络延迟
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("  [" + name + "] 提交阶段异常: " + e.getMessage());
        }
    }

    @Override
    public void rollback(String transactionId) {
        try {
            ReservedStock reserved = reservedStocks.get(transactionId);
            if (reserved == null) {
                System.out.println("  [" + name + "] 未找到预留库存记录，无需回滚");
                return;
            }

            // 释放预留库存
            reservedStocks.remove(transactionId);
            System.out.println("  [" + name + "] 释放预留库存成功: productId=" + reserved.productId +
                    ", quantity=" + reserved.quantity);

            // 模拟网络延迟
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("  [" + name + "] 回滚阶段异常: " + e.getMessage());
        }
    }

    /**
     * 获取商品库存（用于测试）
     */
    public Integer getStock(Long productId) {
        return productStocks.get(productId);
    }

    /**
     * 预留库存记录
     */
    private static class ReservedStock {
        Long productId;
        Integer quantity;

        ReservedStock(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
