package com.architecture.consistency.local_message;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 本地消息表 + 定时补偿示例
 *
 * 核心思想：
 * 1. 订单服务在本地事务中同时写入订单表和本地消息表
 * 2. 保证订单和消息的强一致性（要么都成功，要么都失败）
 * 3. 定时任务扫描本地消息表，将未发送成功的消息进行补偿
 * 4. 支持重试和指数退避策略
 *
 * 优点：
 * - 强一致性保证
 * - 可靠性高，消息不会丢失
 * - 支持故障恢复
 *
 * 缺点：
 * - 需要额外的消息表
 * - 需要定时任务维护
 * - 有一定的延迟
 */
@Slf4j
public class LocalMessageExample {

    public static void main(String[] args) throws InterruptedException {
        log.info("=== 本地消息表 + 定时补偿示例开始 ===\n");

        // 初始化组件
        LocalMessageDAO messageDAO = new LocalMessageDAO();
        TransactionalOrderService orderService = new TransactionalOrderService(messageDAO);
        MessageCompensationJob compensationJob = new MessageCompensationJob(messageDAO, 3, 10);

        // 启动补偿任务
        compensationJob.start();

        log.info("\n【场景1】创建订单，自动写入本地消息表");
        String orderId1 = orderService.createOrderWithMessage(
                "user001",
                "iPhone 15 Pro",
                new BigDecimal("7999.00")
        );

        // 等待补偿任务处理
        TimeUnit.SECONDS.sleep(5);

        log.info("\n" + "=".repeat(80) + "\n");

        log.info("【场景2】批量创建订单");
        for (int i = 2; i <= 5; i++) {
            orderService.createOrderWithMessage(
                    "user00" + i,
                    "商品" + i,
                    new BigDecimal(String.valueOf(1000 * i))
            );
            TimeUnit.MILLISECONDS.sleep(100);
        }

        // 等待补偿任务处理
        TimeUnit.SECONDS.sleep(8);

        log.info("\n" + "=".repeat(80) + "\n");

        log.info("【场景3】查看消息处理统计");
        var stats = messageDAO.getStatistics();
        log.info("消息统计: {}", stats);

        // 停止补偿任务
        compensationJob.stop();

        log.info("\n=== 本地消息表 + 定时补偿示例结束 ===");
        System.exit(0);
    }
}
