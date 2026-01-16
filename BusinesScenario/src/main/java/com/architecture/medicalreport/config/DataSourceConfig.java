package com.architecture.medicalreport.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 *
 * 数据源说明:
 * 1. masterDataSource: MySQL主库 (写操作)
 * 2. slaveDataSource:  MySQL从库 (读操作，报表查询)
 * 3. clickhouseDataSource: ClickHouse (OLAP报表查询)
 *
 * 使用方式:
 * - @ReadOnly 注解标记读操作，路由到从库
 * - 默认路由到主库
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Configuration
public class DataSourceConfig {

    /**
     * MySQL主库数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("MasterDataSource");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // MySQL连接参数优化
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");

        return new HikariDataSource(config);
    }

    /**
     * MySQL从库数据源 (只读，报表查询)
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SlaveDataSource");
        config.setMaximumPoolSize(50);  // 从库连接池更大
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setReadOnly(true);  // 只读模式

        return new HikariDataSource(config);
    }

    /**
     * ClickHouse数据源 (OLAP报表)
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.clickhouse")
    public DataSource clickhouseDataSource() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("ClickHouseDataSource");
        config.setMaximumPoolSize(30);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);

        // ClickHouse连接参数
        config.addDataSourceProperty("socket_timeout", "300000");
        config.addDataSourceProperty("compress", "1");

        return new HikariDataSource(config);
    }

    /**
     * 动态数据源路由
     */
    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource,
            @Qualifier("clickhouseDataSource") DataSource clickhouseDataSource) {

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);
        targetDataSources.put(DataSourceType.SLAVE, slaveDataSource);
        targetDataSources.put(DataSourceType.CLICKHOUSE, clickhouseDataSource);

        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(masterDataSource);

        return dataSource;
    }

    /**
     * 动态数据源实现
     */
    public static class DynamicDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            return DataSourceContextHolder.getDataSourceType();
        }
    }

    /**
     * 数据源上下文 (ThreadLocal)
     */
    public static class DataSourceContextHolder {
        private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

        public static void setDataSourceType(DataSourceType type) {
            contextHolder.set(type);
        }

        public static DataSourceType getDataSourceType() {
            DataSourceType type = contextHolder.get();
            return type != null ? type : DataSourceType.MASTER;
        }

        public static void clearDataSourceType() {
            contextHolder.remove();
        }
    }

    /**
     * 数据源类型枚举
     */
    public enum DataSourceType {
        MASTER,      // 主库
        SLAVE,       // 从库
        CLICKHOUSE   // ClickHouse
    }
}
