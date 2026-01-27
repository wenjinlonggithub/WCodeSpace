package com.distributed.transaction.tcc;

import java.util.UUID;

/**
 * TCC 模式示例
 * 演示电商下单场景的 TCC 分布式事务
 */
public class TCCDemo {

    public static void main(String[] args) {
        // 创建协调器
        TCCTransactionCoordinator coordinator = new TCCTransactionCoordinator();

        // 创建参与者
        OrderServiceTCCParticipant orderService = new OrderServiceTCCParticipant();
        StockServiceTCCParticipant stockService = new StockServiceTCCParticipant();
        AccountServiceTCCParticipant accountService = new AccountServiceTCCParticipant();

        // 注册参与者
        coordinator.registerParticipant(orderService);
        coordinator.registerParticipant(stockService);
        coordinator.registerParticipant(accountService);

        System.out.println("\n========== 初始状态 ==========");
        System.out.println("用户余额: " + accountService.getBalance(1L));
        System.out.println("商品库存: " + stockService.getStock(1001L));

        // 场景1：正常流程 - 所有参与者都成功
        System.out.println("\n\n========== 场景1：正常流程 ==========");
        String transactionId1 = UUID.randomUUID().toString();
        boolean result1 = coordinator.executeTransaction(transactionId1);

        System.out.println("\n========== 场景1执行后状态 ==========");
        System.out.println("事务结果: " + (result1 ? "成功" : "失败"));
        System.out.println("用户余额: " + accountService.getBalance(1L));
        System.out.println("商品库存: " + stockService.getStock(1001L));
        OrderServiceTCCParticipant.Order order1 = orderService.getOrder(transactionId1);
        System.out.println("订单状态: " + (order1 != null ? order1.getStatus() : "不存在"));

        // 场景2：测试幂等性 - 重复执行同一个事务
        System.out.println("\n\n========== 场景2：测试幂等性 ==========");
        System.out.println("重复执行事务: " + transactionId1);
        boolean result2 = coordinator.executeTransaction(transactionId1);

        System.out.println("\n========== 场景2执行后状态 ==========");
        System.out.println("事务结果: " + (result2 ? "成功" : "失败"));
        System.out.println("用户余额: " + accountService.getBalance(1L));
        System.out.println("商品库存: " + stockService.getStock(1001L));

        // 场景3：异常流程 - 模拟余额不足
        System.out.println("\n\n========== 场景3：余额不足场景 ==========");
        // 先消耗大部分余额
        for (int i = 0; i < 8; i++) {
            String tid = UUID.randomUUID().toString();
            coordinator.executeTransaction(tid);
        }

        System.out.println("\n当前用户余额: " + accountService.getBalance(1L));
        System.out.println("当前商品库存: " + stockService.getStock(1001L));

        String transactionId3 = UUID.randomUUID().toString();
        boolean result3 = coordinator.executeTransaction(transactionId3);

        System.out.println("\n========== 场景3执行后状态 ==========");
        System.out.println("事务结果: " + (result3 ? "成功" : "失败"));
        System.out.println("用户余额: " + accountService.getBalance(1L));
        System.out.println("商品库存: " + stockService.getStock(1001L));
        OrderServiceTCCParticipant.Order order3 = orderService.getOrder(transactionId3);
        System.out.println("订单状态: " + (order3 != null ? order3.getStatus() : "不存在"));

        // 场景4：异常流程 - 模拟库存不足
        System.out.println("\n\n========== 场景4：库存不足场景 ==========");
        // 创建新的参与者，设置较少的库存
        StockServiceTCCParticipant stockService2 = new StockServiceTCCParticipant();
        TCCTransactionCoordinator coordinator2 = new TCCTransactionCoordinator();
        coordinator2.registerParticipant(new OrderServiceTCCParticipant());
        coordinator2.registerParticipant(stockService2);
        coordinator2.registerParticipant(new AccountServiceTCCParticipant());

        // 先消耗大部分库存
        for (int i = 0; i < 19; i++) {
            String tid = UUID.randomUUID().toString();
            coordinator2.executeTransaction(tid);
        }

        System.out.println("\n当前商品库存: " + stockService2.getStock(1001L));

        String transactionId4 = UUID.randomUUID().toString();
        boolean result4 = coordinator2.executeTransaction(transactionId4);

        System.out.println("\n========== 场景4执行后状态 ==========");
        System.out.println("事务结果: " + (result4 ? "成功" : "失败"));
        System.out.println("商品库存: " + stockService2.getStock(1001L));

        // 总结
        System.out.println("\n\n========== TCC 演示总结 ==========");
        System.out.println("1. 场景1展示了正常流程：Try → Confirm，事务成功");
        System.out.println("2. 场景2展示了幂等性：重复执行同一事务，不会重复扣减资源");
        System.out.println("3. 场景3展示了余额不足的情况：Try 失败 → Cancel，事务回滚");
        System.out.println("4. 场景4展示了库存不足的情况：Try 失败 → Cancel，事务回滚");
        System.out.println("\nTCC 的特点：");
        System.out.println("- 最终一致性：通过补偿机制保证数据最终一致");
        System.out.println("- 高性能：不需要长时间锁定资源");
        System.out.println("- 业务侵入：需要实现 Try、Confirm、Cancel 三个接口");
        System.out.println("- 幂等性：Confirm 和 Cancel 必须支持幂等");
        System.out.println("- 资源预留：Try 阶段预留资源，Confirm 阶段使用预留资源");
        System.out.println("\nTCC vs 2PC：");
        System.out.println("- TCC 不依赖资源管理器（如数据库）的分布式事务支持");
        System.out.println("- TCC 性能更好，不需要长时间锁定资源");
        System.out.println("- TCC 对业务侵入更大，需要实现三个接口");
        System.out.println("- TCC 是最终一致性，2PC 是强一致性");
    }
}
