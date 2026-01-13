package com.okx.finance.trading.engine.model;

import com.okx.finance.common.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 价格层级（Price Level）
 * 管理订单簿中同一价格的所有订单
 *
 * <p>核心概念：
 * 在订单簿中，相同价格的订单会被组织在一个价格层级中
 * 使用FIFO队列保证时间优先原则（先提交的订单先成交）
 *
 * <p>数据结构选择：
 * - 使用LinkedList作为队列，保证O(1)的入队和出队操作
 * - 维护totalQuantity字段，可以快速获取该价格的总挂单量
 *
 * <p>应用场景：
 * 1. 订单簿深度展示：显示每个价格的挂单总量
 * 2. 撮合引擎：按时间顺序处理同价订单
 * 3. 市场分析：统计不同价格的流动性
 *
 * <p>示例：
 * <pre>
 * PriceLevel (50000.00 USDT)
 * ├── Order1: 1.0 BTC (时间最早)
 * ├── Order2: 0.5 BTC
 * └── Order3: 2.0 BTC
 * totalQuantity = 3.5 BTC
 * </pre>
 *
 * @author OKX Finance Team
 * @version 1.0
 */
@Data
public class PriceLevel {
    /**
     * 价格
     * 该层级的价格值
     * 例如：50000.00 USDT
     */
    private BigDecimal price;

    /**
     * 订单队列
     * 存储该价格下的所有订单
     * 使用LinkedList实现FIFO队列，保证时间优先
     */
    private Queue<Order> orders;

    /**
     * 总挂单量
     * 该价格层级的所有订单的剩余数量总和
     * 计算公式：sum(order.quantity - order.executedQuantity)
     * 用于快速展示订单簿深度
     */
    private BigDecimal totalQuantity;

    /**
     * 构造函数
     *
     * @param price 价格层级的价格
     */
    public PriceLevel(BigDecimal price) {
        this.price = price;
        this.orders = new LinkedList<>();
        this.totalQuantity = BigDecimal.ZERO;
    }

    /**
     * 添加订单到价格层级
     *
     * <p>新订单会被添加到队列尾部，保证时间优先
     * 同时更新总挂单量
     *
     * @param order 要添加的订单
     */
    public void addOrder(Order order) {
        // 将订单加入队列尾部
        orders.offer(order);

        // 计算该订单的剩余数量
        BigDecimal remainingQuantity = order.getQuantity().subtract(order.getExecutedQuantity());

        // 累加到总挂单量
        totalQuantity = totalQuantity.add(remainingQuantity);
    }

    /**
     * 查看队首订单（不移除）
     *
     * <p>用于撮合时查看最早提交的订单
     *
     * @return 队首订单，如果队列为空则返回null
     */
    public Order peekOrder() {
        return orders.peek();
    }

    /**
     * 移除并返回队首订单
     *
     * <p>撮合成功后，如果订单完全成交，需要从队列中移除
     * 同时更新总挂单量
     *
     * @return 队首订单，如果队列为空则返回null
     */
    public Order pollOrder() {
        Order order = orders.poll();
        if (order != null) {
            // 计算该订单的剩余数量
            BigDecimal remainingQuantity = order.getQuantity().subtract(order.getExecutedQuantity());

            // 从总挂单量中减去
            totalQuantity = totalQuantity.subtract(remainingQuantity);
        }
        return order;
    }

    /**
     * 判断价格层级是否为空
     *
     * @return true-没有订单，false-有订单
     */
    public boolean isEmpty() {
        return orders.isEmpty();
    }

    /**
     * 获取订单数量
     *
     * @return 该价格层级的订单总数
     */
    public int size() {
        return orders.size();
    }

    /**
     * 移除指定订单
     *
     * <p>用于订单取消操作
     * 需要遍历队列查找订单，时间复杂度O(n)
     * 找到后移除订单并更新总挂单量
     *
     * @param order 要移除的订单
     */
    public void removeOrder(Order order) {
        // 尝试从队列中移除订单
        if (orders.remove(order)) {
            // 计算该订单的剩余数量
            BigDecimal remainingQuantity = order.getQuantity().subtract(order.getExecutedQuantity());

            // 从总挂单量中减去
            totalQuantity = totalQuantity.subtract(remainingQuantity);
        }
    }
}
