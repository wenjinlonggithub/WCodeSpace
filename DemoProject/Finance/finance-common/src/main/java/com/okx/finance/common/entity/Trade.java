package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 成交记录实体类
 * 存储每一笔订单成交的详细信息
 *
 * <p>说明：
 * 一个订单可能产生多笔成交记录（部分成交）
 * 每笔成交记录对应订单簿中的一次撮合
 *
 * <p>成交记录用途：
 * 1. 统计交易量和交易额
 * 2. 计算手续费
 * 3. 生成K线数据
 * 4. 展示最新成交
 *
 * @author OKX Finance Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Trade extends BaseEntity {
    /**
     * 成交ID
     * 业务层面的成交唯一标识
     * 格式：32位随机字符串（UUID去掉横线）
     */
    private String tradeId;

    /**
     * 订单ID
     * 关联order表，标识该成交属于哪个订单
     * 一个订单可能有多条成交记录
     */
    private String orderId;

    /**
     * 交易对
     * 格式：BASE-QUOTE，例如：BTC-USDT
     * 与订单的symbol字段一致
     */
    private String symbol;

    /**
     * 成交价格
     * 实际成交的价格
     * 撮合引擎中为Maker订单的价格
     * 精度：小数点后18位
     */
    private BigDecimal price;

    /**
     * 成交数量
     * 实际成交的数量
     * 精度：小数点后18位
     * 单位：基础货币（BASE）
     */
    private BigDecimal quantity;

    /**
     * 成交方向
     * BUY-买入，SELL-卖出
     * 记录主动成交方（Taker）的方向
     */
    private String side;

    /**
     * 手续费
     * 该笔成交收取的手续费
     * 通常按成交金额的一定比例收取
     * Taker费率通常高于Maker费率
     */
    private BigDecimal fee;

    /**
     * 手续费币种
     * 手续费使用的币种
     * 通常为交易对的计价货币（QUOTE）
     * 例如：BTC-USDT交易对，手续费币种为USDT
     */
    private String feeCurrency;
}
