package com.okx.finance.trading.engine.listener;

import com.okx.finance.trading.engine.model.MatchResult;
import com.okx.finance.trading.engine.model.Trade;
import com.okx.finance.trading.mapper.OrderMapper;
import com.okx.finance.trading.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 默认撮合监听器实现
 */
@Slf4j
@Component
public class DefaultMatchListener implements MatchListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TradeService tradeService;

    @Override
    public void onMatch(MatchResult matchResult) {
        log.info("撮合成功 - Symbol: {}, Price: {}, Quantity: {}, TakerOrder: {}, MakerOrder: {}",
                matchResult.getSymbol(),
                matchResult.getMatchPrice(),
                matchResult.getMatchQuantity(),
                matchResult.getTakerOrder().getOrderId(),
                matchResult.getMakerOrder().getOrderId());

        // 更新订单状态
        orderMapper.update(matchResult.getTakerOrder());
        orderMapper.update(matchResult.getMakerOrder());

        // 生成成交记录
        Trade trade = new Trade();
        trade.setSymbol(matchResult.getSymbol());
        trade.setTakerOrderId(matchResult.getTakerOrder().getOrderId());
        trade.setMakerOrderId(matchResult.getMakerOrder().getOrderId());
        trade.setPrice(matchResult.getMatchPrice());
        trade.setQuantity(matchResult.getMatchQuantity());
        trade.setAmount(matchResult.getMatchAmount());
        trade.setSide(matchResult.getTakerOrder().getSide());
        trade.setTimestamp(matchResult.getMatchTime());

        tradeService.saveTrade(trade);
    }

    @Override
    public void onOrderFilled(String orderId) {
        log.info("订单完全成交 - OrderId: {}", orderId);
        // 可以发送通知给用户
    }

    @Override
    public void onOrderPartiallyFilled(String orderId, Trade trade) {
        log.info("订单部分成交 - OrderId: {}, TradeQuantity: {}", orderId, trade.getQuantity());
        // 可以发送通知给用户
    }

    @Override
    public void onOrderCanceled(String orderId) {
        log.info("订单已取消 - OrderId: {}", orderId);
        // 可以发送通知给用户
    }
}
