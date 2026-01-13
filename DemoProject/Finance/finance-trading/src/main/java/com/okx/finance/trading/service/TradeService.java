package com.okx.finance.trading.service;

import com.okx.finance.trading.engine.model.Trade;
import com.okx.finance.trading.mapper.TradeMapper;
import com.okx.finance.common.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TradeService {

    @Autowired
    private TradeMapper tradeMapper;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(4);

    public void saveTrade(Trade trade) {
        trade.setId(idGenerator.nextId());
        trade.setTradeId(UUID.randomUUID().toString().replace("-", ""));

        com.okx.finance.common.entity.Trade tradeEntity = new com.okx.finance.common.entity.Trade();
        tradeEntity.setId(trade.getId());
        tradeEntity.setTradeId(trade.getTradeId());
        tradeEntity.setOrderId(trade.getTakerOrderId());
        tradeEntity.setSymbol(trade.getSymbol());
        tradeEntity.setPrice(trade.getPrice());
        tradeEntity.setQuantity(trade.getQuantity());
        tradeEntity.setSide(trade.getSide());
        tradeEntity.setFee(trade.getTakerFee() != null ? trade.getTakerFee() : java.math.BigDecimal.ZERO);
        tradeEntity.setFeeCurrency(trade.getSymbol().split("-")[1]);

        tradeMapper.insert(tradeEntity);
    }
}
