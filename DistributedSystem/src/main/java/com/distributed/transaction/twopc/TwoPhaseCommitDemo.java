package com.distributed.transaction.twopc;

import java.math.BigDecimal;
import java.util.UUID;

/**

 * 两阶段提交（2PC）演示程序
 * 
 * 本程序演示了分布式系统中两阶段提交协议的工作原理，通过模拟电商下单场景，
 * 展示了如何确保跨多个服务的数据一致性。程序包含三个核心服务：
 * 1. 订单服务 - 负责创建和管理订单
 * 2. 库存服务 - 管理商品库存
 * 3. 账户服务 - 处理用户资金
 * 
 * 两阶段提交协议包括：
 * 阶段一（准备阶段）- 协调器询问所有参与者是否可以提交事务
 * 阶段二（提交/回滚阶段）- 根据参与者响应决定全局提交或回滚
 * 
 * 程序演示三种场景：正常提交、余额不足导致回滚、库存不足导致回滚
 * 演示电商下单场景的分布式事务
 */
public class TwoPhaseCommitDemo {

    public static void main(String[] args) {
        // 创建协调器
        TwoPhaseCommitCoordinator coordinator = new TwoPhaseCommitCoordinator();

        // 创建参与者
        OrderServiceParticipant orderService = new OrderServiceParticipant();
        StockServiceParticipant stockService = new StockServiceParticipant();
        AccountServiceParticipant accountService = new AccountServiceParticipant();

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
        OrderServiceParticipant.Order order1 = orderService.getOrder(transactionId1);
        System.out.println("订单状态: " + (order1 != null ? order1.getStatus() : "不存在"));

        // 场景2：异常流程 - 模拟余额不足
        System.out.println("\n\n========== 场景2：余额不足场景 ==========");
        // 先消耗大部分余额
        for (int i = 0; i < 8; i++) {
            String tid = UUID.randomUUID().toString();
            coordinator.executeTransaction(tid);
        }

        System.out.println("\n当前用户余额: " + accountService.getBalance(1L));
        System.out.println("当前商品库存: " + stockService.getStock(1001L));

        String transactionId2 = UUID.randomUUID().toString();
        boolean result2 = coordinator.executeTransaction(transactionId2);

        System.out.println("\n========== 场景2执行后状态 ==========");
        System.out.println("事务结果: " + (result2 ? "成功" : "失败"));
        System.out.println("用户余额: " + accountService.getBalance(1L));
        System.out.println("商品库存: " + stockService.getStock(1001L));
        OrderServiceParticipant.Order order2 = orderService.getOrder(transactionId2);
        System.out.println("订单状态: " + (order2 != null ? order2.getStatus() : "不存在"));

        // 场景3：异常流程 - 模拟库存不足
        System.out.println("\n\n========== 场景3：库存不足场景 ==========");
        // 创建新的参与者，设置较少的库存
        StockServiceParticipant stockService2 = new StockServiceParticipant();
        // 先消耗大部分库存
        TwoPhaseCommitCoordinator coordinator2 = new TwoPhaseCommitCoordinator();
        coordinator2.registerParticipant(new OrderServiceParticipant());
        coordinator2.registerParticipant(stockService2);
        coordinator2.registerParticipant(new AccountServiceParticipant());

        for (int i = 0; i < 19; i++) {
            String tid = UUID.randomUUID().toString();
            coordinator2.executeTransaction(tid);
        }

        System.out.println("\n当前商品库存: " + stockService2.getStock(1001L));

        String transactionId3 = UUID.randomUUID().toString();
        boolean result3 = coordinator2.executeTransaction(transactionId3);

        System.out.println("\n========== 场景3执行后状态 ==========");
        System.out.println("事务结果: " + (result3 ? "成功" : "失败"));
        System.out.println("商品库存: " + stockService2.getStock(1001L));

        // 总结
        System.out.println("\n\n========== 2PC 演示总结 ==========");
        System.out.println("1. 场景1展示了正常流程，所有参与者都成功，事务提交");
        System.out.println("2. 场景2展示了余额不足的情况，事务回滚，保证数据一致性");
        System.out.println("3. 场景3展示了库存不足的情况，事务回滚，保证数据一致性");
        System.out.println("\n2PC 的特点：");
        System.out.println("- 强一致性：保证所有参与者的数据一致");
        System.out.println("- 同步阻塞：参与者在等待协调器指令时会阻塞");
        System.out.println("- 单点故障：协调器故障会导致整个系统不可用");
        System.out.println("- 性能较低：需要多次网络通信");
    }
}
