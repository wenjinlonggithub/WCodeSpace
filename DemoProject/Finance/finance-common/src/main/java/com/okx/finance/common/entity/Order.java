package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 订单实体类
 * 存储用户的交易订单信息
 *
 * <p>订单状态流转：
 * NEW → PARTIALLY_FILLED → FILLED（正常成交）
 * NEW → CANCELED（用户取消）
 * NEW → REJECTED（系统拒绝）
 * NEW → EXPIRED（订单过期）
 *
 * <p>订单类型说明：
 * LIMIT - 限价单：指定价格，只有达到指定价格才会成交
 * MARKET - 市价单：按当前市场最优价格立即成交
 * STOP_LOSS - 止损单：价格达到止损价格时触发
 * STOP_LOSS_LIMIT - 限价止损单：触发后按限价单执行
 * TAKE_PROFIT - 止盈单：价格达到止盈价格时触发
 * TAKE_PROFIT_LIMIT - 限价止盈单：触发后按限价单执行
 *
 * @author OKX Finance Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {
    /**
     * 用户ID
     * 关联user表的主键，标识订单所属用户
     */
    private Long userId;

    /**
     * 订单ID
     * 业务层面的订单唯一标识
     * 格式：32位随机字符串（UUID去掉横线）
     * 用于对外展示和查询
     */
    private String orderId;

    /**
     * 交易对
     * 格式：BASE-QUOTE，例如：BTC-USDT, ETH-USDT
     * BASE是基础货币，QUOTE是计价货币
     */
    private String symbol;

    /**
     * 订单类型
     * LIMIT-限价单，MARKET-市价单
     * STOP_LOSS-止损单，TAKE_PROFIT-止盈单等
     * 详见OrderType常量类
     */
    private String orderType;

    /**
     * 买卖方向
     * BUY-买入（做多），SELL-卖出（做空）
     * 详见OrderSide常量类
     */
    private String side;

    /**
     * 委托价格
     * 限价单必填，市价单为空
     * 精度：小数点后18位
     * 单位：计价货币（QUOTE）
     */
    private BigDecimal price;

    /**
     * 委托数量
     * 要买入或卖出的数量
     * 精度：小数点后18位
     * 单位：基础货币（BASE）
     */
    private BigDecimal quantity;

    /**
     * 已成交数量
     * 已经成交的数量，初始值为0
     * executedQuantity <= quantity
     * 当 executedQuantity == quantity 时订单完全成交
     */
    private BigDecimal executedQuantity;

    /**
     * 已成交金额
     * 已成交部分的总金额（价格 × 数量）
     * 用于计算平均成交价：executedAmount / executedQuantity
     * 单位：计价货币（QUOTE）
     */
    private BigDecimal executedAmount;

    /**
     * 订单状态
     * NEW-新建，PARTIALLY_FILLED-部分成交，FILLED-完全成交
     * CANCELED-已取消，REJECTED-已拒绝，EXPIRED-已过期
     * 详见OrderStatus常量类
     */
    private String status;

    /**
     * 有效期类型
     * GTC-Good Till Cancel（一直有效直到取消）
     * IOC-Immediate Or Cancel（立即成交否则取消）
     * FOK-Fill Or Kill（全部成交否则取消）
     * GTX-Good Till Crossing（只做Maker）
     */
    private String timeInForce;
}
