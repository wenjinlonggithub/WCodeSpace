package com.okx.finance.market.service;

import com.okx.finance.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MarketService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Result<?> getTicker(String symbol) {
        Map<String, Object> ticker = new HashMap<>();
        ticker.put("symbol", symbol);
        ticker.put("lastPrice", "50000.00");
        ticker.put("bidPrice", "49999.00");
        ticker.put("askPrice", "50001.00");
        ticker.put("high24h", "51000.00");
        ticker.put("low24h", "49000.00");
        ticker.put("volume24h", "1234567.89");
        ticker.put("timestamp", System.currentTimeMillis());

        return Result.success(ticker);
    }

    public Result<?> getDepth(String symbol, int limit) {
        Map<String, Object> depth = new HashMap<>();
        List<List<String>> bids = new ArrayList<>();
        List<List<String>> asks = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            bids.add(Arrays.asList("49990." + i, "1.23"));
            asks.add(Arrays.asList("50010." + i, "0.98"));
        }

        depth.put("symbol", symbol);
        depth.put("bids", bids);
        depth.put("asks", asks);
        depth.put("timestamp", System.currentTimeMillis());

        return Result.success(depth);
    }

    public Result<?> getKlines(String symbol, String interval, Long startTime, Long endTime, int limit) {
        List<List<Object>> klines = new ArrayList<>();

        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < limit; i++) {
            List<Object> kline = Arrays.asList(
                currentTime - (i * 60000),
                "50000.00",
                "50100.00",
                "49900.00",
                "50050.00",
                "123.45"
            );
            klines.add(kline);
        }

        return Result.success(klines);
    }

    public Result<?> getTrades(String symbol, int limit) {
        List<Map<String, Object>> trades = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Map<String, Object> trade = new HashMap<>();
            trade.put("id", UUID.randomUUID().toString());
            trade.put("price", "50000.00");
            trade.put("quantity", "0.123");
            trade.put("side", i % 2 == 0 ? "BUY" : "SELL");
            trade.put("timestamp", System.currentTimeMillis() - i * 1000);
            trades.add(trade);
        }

        return Result.success(trades);
    }
}
