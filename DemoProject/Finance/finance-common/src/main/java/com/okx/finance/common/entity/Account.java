package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 账户实体类
 * 存储用户的资产信息，支持多币种
 *
 * <p>账户类型说明：
 * 1 - 现货账户（用于现货交易）
 * 2 - 合约账户（用于合约交易）
 * 3 - 杠杆账户（用于杠杆交易）
 *
 * <p>余额关系：
 * totalBalance = availableBalance + frozenBalance
 *
 * <p>资金流转：
 * - 下单时：availableBalance → frozenBalance
 * - 成交时：frozenBalance → 0（扣除）
 * - 撤单时：frozenBalance → availableBalance
 *
 * @author OKX Finance Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {
    /**
     * 用户ID
     * 关联user表的主键
     */
    private Long userId;

    /**
     * 币种代码
     * 例如：BTC, ETH, USDT等
     * 大写字母，与交易对中的币种一致
     */
    private String currency;

    /**
     * 可用余额
     * 可以用于交易、提现的金额
     * 精度：小数点后18位
     */
    private BigDecimal availableBalance;

    /**
     * 冻结余额
     * 已下单但未成交的金额
     * 交易完成后会从冻结余额中扣除
     */
    private BigDecimal frozenBalance;

    /**
     * 总余额
     * = 可用余额 + 冻结余额
     * 仅用于展示，更新时需要同步计算
     */
    private BigDecimal totalBalance;

    /**
     * 账户类型
     * 1-现货账户，2-合约账户，3-杠杆账户
     * 不同账户类型的资金独立管理
     */
    private Integer accountType;
}
