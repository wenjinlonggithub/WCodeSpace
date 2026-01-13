package com.okx.finance.trading.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 撮合引擎配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "matching.engine")
public class MatchingEngineConfig {

    /**
     * 撮合线程数
     */
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 订单队列容量
     */
    private int queueCapacity = 10000;

    /**
     * Taker手续费率
     */
    private BigDecimal takerFeeRate = new BigDecimal("0.001");

    /**
     * Maker手续费率
     */
    private BigDecimal makerFeeRate = new BigDecimal("0.0008");

    /**
     * 是否启用深度缓存
     */
    private boolean enableDepthCache = true;

    /**
     * 深度缓存刷新间隔（毫秒）
     */
    private long depthCacheRefreshInterval = 1000;

    /**
     * 是否启用撮合日志
     */
    private boolean enableMatchLog = true;

    /**
     * 最大订单簿深度
     */
    private int maxDepth = 100;
}
