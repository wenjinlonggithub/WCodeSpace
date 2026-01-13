package com.okx.finance.market.controller;

import com.okx.finance.common.dto.Result;
import com.okx.finance.market.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private MarketService marketService;

    @GetMapping("/ticker/{symbol}")
    public Result<?> getTicker(@PathVariable String symbol) {
        return marketService.getTicker(symbol);
    }

    @GetMapping("/depth/{symbol}")
    public Result<?> getDepth(@PathVariable String symbol,
                              @RequestParam(defaultValue = "20") int limit) {
        return marketService.getDepth(symbol, limit);
    }

    @GetMapping("/klines/{symbol}")
    public Result<?> getKlines(@PathVariable String symbol,
                               @RequestParam String interval,
                               @RequestParam(required = false) Long startTime,
                               @RequestParam(required = false) Long endTime,
                               @RequestParam(defaultValue = "100") int limit) {
        return marketService.getKlines(symbol, interval, startTime, endTime, limit);
    }

    @GetMapping("/trades/{symbol}")
    public Result<?> getTrades(@PathVariable String symbol,
                               @RequestParam(defaultValue = "50") int limit) {
        return marketService.getTrades(symbol, limit);
    }
}
