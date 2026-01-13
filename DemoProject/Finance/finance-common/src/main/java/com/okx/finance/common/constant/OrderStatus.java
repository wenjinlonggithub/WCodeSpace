package com.okx.finance.common.constant;

/**
 * 订单状态常量类
 * 定义订单在生命周期中的所有可能状态
 *
 * <p>状态流转图：
 * <pre>
 *                    ┌──────────┐
 *                    │   NEW    │ 新建订单
 *                    └────┬─────┘
 *                         │
 *            ┌────────────┼────────────┐
 *            │            │            │
 *            ▼            ▼            ▼
 *      ┌─────────┐  ┌──────────┐  ┌─────────┐
 *      │REJECTED │  │PARTIALLY │  │ FILLED  │
 *      │已拒绝   │  │  FILLED  │  │完全成交 │
 *      └─────────┘  │部分成交   │  └─────────┘
 *                   └─────┬────┘
 *                         │
 *                    ┌────┴─────┐
 *                    │          │
 *                    ▼          ▼
 *              ┌─────────┐  ┌─────────┐
 *              │ FILLED  │  │CANCELED │
 *              │完全成交 │  │ 已取消  │
 *              └─────────┘  └─────────┘
 * </pre>
 *
 * @author OKX Finance Team
 * @version 1.0
 */
public class OrderStatus {
    /**
     * 新建订单
     * 订单已提交到系统，等待撮合
     * 此状态的订单可以被取消
     */
    public static final String NEW = "NEW";

    /**
     * 部分成交
     * 订单已部分成交，还有剩余数量未成交
     * 剩余部分继续留在订单簿中等待撮合
     * 此状态的订单可以被取消（取消剩余未成交部分）
     */
    public static final String PARTIALLY_FILLED = "PARTIALLY_FILLED";

    /**
     * 完全成交
     * 订单的全部数量已成交
     * 这是订单的最终状态之一，不可再变更
     */
    public static final String FILLED = "FILLED";

    /**
     * 已取消
     * 订单被用户主动取消，或因其他原因被系统取消
     * 这是订单的最终状态之一，不可再变更
     * 取消后，冻结的资金会被释放
     */
    public static final String CANCELED = "CANCELED";

    /**
     * 取消中
     * 订单正在执行取消操作
     * 这是一个中间状态，会很快转变为CANCELED
     * 用于标识取消请求已接收但尚未完成
     */
    public static final String PENDING_CANCEL = "PENDING_CANCEL";

    /**
     * 已拒绝
     * 订单因不满足条件而被系统拒绝
     * 可能的原因：
     * - 余额不足
     * - 价格不合理
     * - 数量不符合要求
     * - 风控拦截
     * 这是订单的最终状态之一，不可再变更
     */
    public static final String REJECTED = "REJECTED";

    /**
     * 已过期
     * 订单超过有效期而失效
     * 适用于有时间限制的订单类型（如IOC、FOK）
     * 这是订单的最终状态之一，不可再变更
     */
    public static final String EXPIRED = "EXPIRED";
}
