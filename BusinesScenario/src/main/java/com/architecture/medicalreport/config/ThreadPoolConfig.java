package com.architecture.medicalreport.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * 线程池说明:
 * 1. reportQueryExecutor: 报表查询线程池 (CPU密集型)
 * 2. reportTaskExecutor: 异步报表生成线程池 (IO密集型)
 * 3. exportExecutor: 报表导出线程池 (IO密集型，控制并发)
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 报表查询线程池
     *
     * 场景: 并行查询多个数据源
     * 特点: CPU密集型
     * 配置: 核心线程数 = CPU核数，最大线程数 = CPU核数 * 2
     */
    @Bean("reportQueryExecutor")
    public Executor reportQueryExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("report-query-");

        // 拒绝策略: 调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("初始化报表查询线程池: corePoolSize={}, maxPoolSize={}",
                corePoolSize, maxPoolSize);

        return executor;
    }

    /**
     * 异步报表生成线程池
     *
     * 场景: 异步生成大报表、导出文件
     * 特点: IO密集型，长时间运行
     * 配置: 核心线程数 = CPU核数 * 2，最大线程数 = CPU核数 * 4
     */
    @Bean("reportTaskExecutor")
    public Executor reportTaskExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = corePoolSize * 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("report-task-");

        // 拒绝策略: 抛出异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);

        executor.initialize();

        log.info("初始化异步任务线程池: corePoolSize={}, maxPoolSize={}",
                corePoolSize, maxPoolSize);

        return executor;
    }

    /**
     * 报表导出线程池
     *
     * 场景: 大文件导出
     * 特点: IO密集型，需要限制并发数
     * 配置: 固定线程数 = 10 (避免过多并发导出压垮系统)
     */
    @Bean("exportExecutor")
    public Executor exportExecutor() {
        int corePoolSize = 10;
        int maxPoolSize = 10;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("export-");

        // 拒绝策略: 调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);

        executor.initialize();

        log.info("初始化导出线程池: corePoolSize={}, maxPoolSize={}",
                corePoolSize, maxPoolSize);

        return executor;
    }
}
