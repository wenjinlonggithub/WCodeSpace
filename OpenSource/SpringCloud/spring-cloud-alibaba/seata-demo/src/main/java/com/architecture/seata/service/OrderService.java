package com.architecture.seata.service;

import com.architecture.seata.entity.Order;
import com.architecture.seata.feign.AccountClient;
import com.architecture.seata.feign.StorageClient;
import com.architecture.seata.repository.OrderRepository;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单服务
 *
 * 核心实现原理：
 * 1. @GlobalTransactional注解标记全局事务
 * 2. Seata通过TC(Transaction Coordinator)协调分布式事务
 * 3. AT模式下自动记录SQL的前后镜像到undo_log表
 * 4. 如果任何服务失败，TC通知所有参与者回滚
 *
 * 执行流程：
 * 1. TM(Transaction Manager)向TC申请开启全局事务，获取XID
 * 2. 各RM(Resource Manager)在本地事务中执行业务SQL
 * 3. RM向TC注册分支事务，记录undo_log
 * 4. 所有分支事务提交后，TC通知所有RM删除undo_log
 * 5. 如有异常，TC通知RM根据undo_log回滚
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StorageClient storageClient;
    private final AccountClient accountClient;

    /**
     * 创建订单（分布式事务）
     *
     * @GlobalTransactional注解实现原理：
     * 1. 通过AOP拦截方法调用
     * 2. 开启全局事务，获取全局事务ID(XID)
     * 3. XID通过ThreadLocal和RPC上下文传播到下游服务
     * 4. 各微服务通过XID关联到同一个全局事务
     * 5. 任一服务异常，触发全局回滚
     */
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public Order create(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("开始创建订单: userId={}, productId={}, count={}, money={}",
                 userId, productId, count, money);

        // 1. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(money);
        order.setStatus(0); // 创建中
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);
        log.info("订单创建成功: orderId={}", order.getId());

        // 2. 扣减库存（远程调用）
        // Seata会自动传播XID到库存服务
        log.info("开始扣减库存");
        storageClient.deduct(productId, count);
        log.info("库存扣减成功");

        // 3. 扣减账户余额（远程调用）
        // Seata会自动传播XID到账户服务
        log.info("开始扣减账户余额");
        accountClient.deduct(userId, money);
        log.info("账户余额扣减成功");

        // 4. 更新订单状态
        order.setStatus(1); // 已完结
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);
        log.info("订单状态更新成功");

        return order;
    }

    /**
     * 创建订单（模拟异常场景）
     * 用于测试分布式事务回滚
     */
    @GlobalTransactional(name = "create-order-with-exception", rollbackFor = Exception.class)
    public Order createWithException(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("开始创建订单（模拟异常）");

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(money);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);

        // 扣减库存
        storageClient.deduct(productId, count);

        // 模拟异常
        throw new RuntimeException("模拟业务异常，触发分布式事务回滚");
    }
}
