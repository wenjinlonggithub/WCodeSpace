package com.distributed.transaction.tcc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存服务 TCC 参与者
 * 实现库存扣减的 TCC 模式
 */
public class StockServiceTCCParticipant implements TCCParticipant {

    private final String name = "StockService";

    // 模拟库存数据库
    private final Map<Long, Integer> productStocks = new ConcurrentHashMap<>();

    // 模拟预留库存记录
    private final Map<String, ReservedRecord> reservedRecords = new ConcurrentHashMap<>();

    // 记录已处理的事务（用于幂等性）
    private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();

    public StockServiceTCCParticipant() {
        // 初始化测试数据
        productStocks.put(1001L, 100);
        productStocks.put(1002L, 50);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean tryExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (reservedRecords.containsKey(transactionId)) {
                System.out.println("  [" + name + "] Try 阶段已执行，跳过（幂等性）");
                return true;
            }

            // 模拟业务参数
            Long productId = 1001L;
            Integer quantity = 5;

            System.out.println("  [" + name + "] Try 阶段：检查库存并预留");

            // 1. 检查商品是否存在
            Integer stock = productStocks.get(productId);
            if (stock == null) {
                System.out.println("  [" + name + "] 商品不存在");
                return false;
            }

            // 2. 检查库存是否充足
            if (stock < quantity) {
                System.out.println("  [" + name + "] 库存不足: 当前库存=" + stock + ", 需要扣减=" + quantity);
                return false;
            }

            // 3. 预留库存（不实际扣减）
            ReservedRecord record = new ReservedRecord(productId, quantity);
            reservedRecords.put(transactionId, record);

            System.out.println("  [" + name + "] 预留库存成功: productId=" + productId + ", quantity=" + quantity +
                    ", 当前库存=" + stock);

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Try 阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean confirmExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (processedTransactions.contains(transactionId + ":CONFIRM")) {
                System.out.println("  [" + name + "] Confirm 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Confirm 阶段：扣减预留的库存");

            // 获取预留记录
            ReservedRecord record = reservedRecords.get(transactionId);
            if (record == null) {
                System.err.println("  [" + name + "] 未找到预留记录");
                return false;
            }

            // 扣减库存
            Integer stock = productStocks.get(record.productId);
            Integer newStock = stock - record.quantity;
            productStocks.put(record.productId, newStock);

            System.out.println("  [" + name + "] 扣减库存成功: productId=" + record.productId +
                    ", 扣减数量=" + record.quantity + ", 新库存=" + newStock);

            // 清除预留记录
            reservedRecords.remove(transactionId);

            // 记录已处理（幂等性）
            processedTransactions.add(transactionId + ":CONFIRM");

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Confirm 阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (processedTransactions.contains(transactionId + ":CANCEL")) {
                System.out.println("  [" + name + "] Cancel 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Cancel 阶段：释放预留的库存");

            // 获取预留记录
            ReservedRecord record = reservedRecords.get(transactionId);
            if (record == null) {
                System.out.println("  [" + name + "] 未找到预留记录，无需取消");
                return true;
            }

            // 释放预留库存
            reservedRecords.remove(transactionId);

            System.out.println("  [" + name + "] 释放预留库存成功: productId=" + record.productId + ", quantity=" + record.quantity);

            // 记录已处理（幂等性）
            processedTransactions.add(transactionId + ":CANCEL");

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Cancel 阶段异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取商品库存（用于测试）
     */
    public Integer getStock(Long productId) {
        return productStocks.get(productId);
    }

    /**
     * 预留记录
     */
    private static class ReservedRecord {
        Long productId;
        Integer quantity;

        ReservedRecord(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
