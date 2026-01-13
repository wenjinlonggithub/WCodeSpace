package com.okx.finance.trading.engine.listener;

import com.okx.finance.trading.engine.model.MatchResult;
import com.okx.finance.trading.engine.model.Trade;

/**
 * 撮合监听器接口
 */
public interface MatchListener {

    /**
     * 撮合成功回调
     */
    void onMatch(MatchResult matchResult);

    /**
     * 订单完全成交回调
     */
    void onOrderFilled(String orderId);

    /**
     * 订单部分成交回调
     */
    void onOrderPartiallyFilled(String orderId, Trade trade);

    /**
     * 订单取消回调
     */
    void onOrderCanceled(String orderId);
}
