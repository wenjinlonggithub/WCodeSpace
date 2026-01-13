package com.okx.finance.common.constant;

/**
 * 订单类型常量类
 * 定义交易所支持的所有订单类型
 *
 * <p>订单类型分类：
 * 1. 基础类型：LIMIT（限价单）、MARKET（市价单）
 * 2. 条件类型：STOP_LOSS（止损）、TAKE_PROFIT（止盈）
 *
 * <p>使用场景：
 * - LIMIT：适合想以指定价格成交的场景
 * - MARKET：适合想立即成交的场景
 * - STOP_LOSS：用于止损，防止亏损扩大
 * - TAKE_PROFIT：用于止盈，锁定利润
 *
 * @author OKX Finance Team
 * @version 1.0
 */
public class OrderType {
    /**
     * 限价单（Limit Order）
     * 必须指定价格，只有达到或优于指定价格才会成交
     *
     * <p>特点：
     * - 需要指定价格和数量
     * - 价格达到时才成交
     * - 可能不会立即成交
     * - 保证成交价格不会比指定价格差
     *
     * <p>示例：
     * 限价买单：价格50000，只有市场卖价 <= 50000时才成交
     * 限价卖单：价格50000，只有市场买价 >= 50000时才成交
     */
    public static final String LIMIT = "LIMIT";

    /**
     * 市价单（Market Order）
     * 不指定价格，按当前市场最优价格立即成交
     *
     * <p>特点：
     * - 不需要指定价格，只需指定数量
     * - 立即成交（如果有对手盘）
     * - 成交价格不确定，取决于当时的市场价格
     * - 可能会产生滑点（实际成交价与预期价的差异）
     *
     * <p>示例：
     * 市价买单：以当前最低卖价买入
     * 市价卖单：以当前最高买价卖出
     */
    public static final String MARKET = "MARKET";

    /**
     * 止损单（Stop Loss Order）
     * 市价止损单，当价格达到止损价时，以市价单执行
     *
     * <p>用途：
     * 用于限制损失，防止亏损进一步扩大
     *
     * <p>触发机制：
     * - 买入止损：当市场价格上涨至止损价时触发
     * - 卖出止损：当市场价格下跌至止损价时触发
     *
     * <p>示例：
     * 持有BTC，当前价格50000，设置止损价48000
     * 如果价格跌至48000，自动按市价卖出，止损离场
     */
    public static final String STOP_LOSS = "STOP_LOSS";

    /**
     * 限价止损单（Stop Loss Limit Order）
     * 当价格达到止损价时，以限价单执行
     *
     * <p>与止损单的区别：
     * - STOP_LOSS：触发后以市价执行（保证成交）
     * - STOP_LOSS_LIMIT：触发后以限价执行（保证价格）
     *
     * <p>特点：
     * - 需要指定止损价和限价
     * - 触发后可能不成交（如果限价不合理）
     * - 适合对成交价格有严格要求的场景
     */
    public static final String STOP_LOSS_LIMIT = "STOP_LOSS_LIMIT";

    /**
     * 止盈单（Take Profit Order）
     * 市价止盈单，当价格达到目标价时，以市价单执行
     *
     * <p>用途：
     * 用于锁定利润，自动获利了结
     *
     * <p>触发机制：
     * - 买入止盈：当市场价格下跌至目标价时触发
     * - 卖出止盈：当市场价格上涨至目标价时触发
     *
     * <p>示例：
     * 持有BTC，成本价45000，当前价格50000，设置止盈价52000
     * 如果价格涨至52000，自动按市价卖出，获利离场
     */
    public static final String TAKE_PROFIT = "TAKE_PROFIT";

    /**
     * 限价止盈单（Take Profit Limit Order）
     * 当价格达到目标价时，以限价单执行
     *
     * <p>与止盈单的区别：
     * - TAKE_PROFIT：触发后以市价执行（保证成交）
     * - TAKE_PROFIT_LIMIT：触发后以限价执行（保证价格）
     *
     * <p>特点：
     * - 需要指定止盈价和限价
     * - 触发后可能不成交（如果限价不合理）
     * - 适合对成交价格有严格要求的场景
     */
    public static final String TAKE_PROFIT_LIMIT = "TAKE_PROFIT_LIMIT";
}
