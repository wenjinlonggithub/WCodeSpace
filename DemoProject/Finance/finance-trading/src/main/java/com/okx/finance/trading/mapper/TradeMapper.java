package com.okx.finance.trading.mapper;

import com.okx.finance.common.entity.Trade;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeMapper {

    @Insert("INSERT INTO trade (id, trade_id, order_id, symbol, price, quantity, side, fee, fee_currency, create_time) " +
            "VALUES (#{id}, #{tradeId}, #{orderId}, #{symbol}, #{price}, #{quantity}, #{side}, #{fee}, #{feeCurrency}, NOW())")
    void insert(Trade trade);
}
