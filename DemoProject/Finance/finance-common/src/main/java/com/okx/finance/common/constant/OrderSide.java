package com.okx.finance.common.constant;

/**
 * 订单方向常量类
 * 定义订单的买卖方向
 *
 * <p>说明：
 * 在现货交易中：
 * - BUY（买入）：使用计价货币（如USDT）购买基础货币（如BTC）
 * - SELL（卖出）：卖出基础货币（如BTC）换取计价货币（如USDT）
 *
 * <p>示例（BTC-USDT交易对）：
 * - BUY：用USDT买入BTC
 * - SELL：卖出BTC换取USDT
 *
 * <p>资金流转：
 * - BUY订单：冻结USDT，成交后获得BTC
 * - SELL订单：冻结BTC，成交后获得USDT
 *
 * @author OKX Finance Team
 * @version 1.0
 */
public class OrderSide {
    /**
     * 买入（做多）
     * 使用计价货币购买基础货币
     *
     * <p>例如：BTC-USDT交易对
     * - 冻结：USDT（price × quantity）
     * - 获得：BTC（quantity）
     */
    public static final String BUY = "BUY";

    /**
     * 卖出（做空）
     * 卖出基础货币换取计价货币
     *
     * <p>例如：BTC-USDT交易对
     * - 冻结：BTC（quantity）
     * - 获得：USDT（price × quantity）
     */
    public static final String SELL = "SELL";
}
