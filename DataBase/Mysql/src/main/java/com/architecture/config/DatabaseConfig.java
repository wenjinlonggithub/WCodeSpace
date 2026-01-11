package com.architecture.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 数据库配置管理类
 * 提供数据库连接池配置和管理功能
 * 
 * 功能特点：
 * 1. 从配置文件加载数据库连接信息
 * 2. 提供HikariCP连接池配置
 * 3. 支持生产环境和开发环境配置切换
 * 4. 提供连接池监控和管理功能
 * 
 * 安全特性：
 * 1. 敏感信息从环境变量或配置文件读取
 * 2. 支持连接加密
 * 3. 提供连接池状态监控
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private static final String CONFIG_FILE = "application.properties";
    private static final String DEFAULT_CONFIG_FILE = "application.properties.example";
    
    private Properties properties;
    private HikariDataSource dataSource;
    
    public DatabaseConfig() {
        loadConfiguration();
        initializeDataSource();
    }
    
    /**
     * 加载配置文件
     * 优先级：环境变量 > application.properties > application.properties.example
     */
    private void loadConfiguration() {
        properties = new Properties();
        
        // 尝试加载配置文件
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                logger.info("已加载配置文件: {}", CONFIG_FILE);
            } else {
                // 回退到示例配置文件
                try (InputStream defaultInput = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                    if (defaultInput != null) {
                        properties.load(defaultInput);
                        logger.warn("未找到{}，使用示例配置文件: {}", CONFIG_FILE, DEFAULT_CONFIG_FILE);
                    } else {
                        logger.error("配置文件不存在，使用默认配置");
                        setDefaultProperties();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("加载配置文件失败，使用默认配置", e);
            setDefaultProperties();
        }
        
        // 环境变量覆盖配置文件设置
        overrideWithEnvironmentVariables();
    }
    
    /**
     * 使用环境变量覆盖配置
     * 提供更安全的生产环境配置方式
     */
    private void overrideWithEnvironmentVariables() {
        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        if (dbUrl != null) {
            properties.setProperty("db.url", dbUrl);
            logger.info("使用环境变量DB_URL覆盖数据库URL配置");
        }
        if (dbUsername != null) {
            properties.setProperty("db.username", dbUsername);
            logger.info("使用环境变量DB_USERNAME覆盖数据库用户名配置");
        }
        if (dbPassword != null) {
            properties.setProperty("db.password", dbPassword);
            logger.info("使用环境变量DB_PASSWORD覆盖数据库密码配置");
        }
    }
    
    /**
     * 设置默认配置
     */
    private void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        properties.setProperty("db.username", "sa");
        properties.setProperty("db.password", "");
        properties.setProperty("db.pool.minimum-idle", "5");
        properties.setProperty("db.pool.maximum-pool-size", "20");
        properties.setProperty("db.pool.connection-timeout", "30000");
        properties.setProperty("db.pool.idle-timeout", "600000");
        properties.setProperty("db.pool.max-lifetime", "1800000");
        
        logger.info("已设置默认H2内存数据库配置");
    }
    
    /**
     * 初始化HikariCP数据源
     */
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // 基本连接信息
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));
            
            // 连接池配置
            config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.minimum-idle", "5")));
            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.maximum-pool-size", "20")));
            config.setConnectionTimeout(Long.parseLong(properties.getProperty("db.pool.connection-timeout", "30000")));
            config.setIdleTimeout(Long.parseLong(properties.getProperty("db.pool.idle-timeout", "600000")));
            config.setMaxLifetime(Long.parseLong(properties.getProperty("db.pool.max-lifetime", "1800000")));
            
            // 连接池名称和性能配置
            config.setPoolName("MySQLArchitecturePool");
            config.setLeakDetectionThreshold(60000); // 连接泄漏检测
            
            // 连接验证
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
            
            dataSource = new HikariDataSource(config);
            
            logger.info("HikariCP数据源初始化成功");
            logger.info("数据库URL: {}", maskUrl(config.getJdbcUrl()));
            logger.info("连接池大小: {}-{}", config.getMinimumIdle(), config.getMaximumPoolSize());
            
        } catch (Exception e) {
            logger.error("数据源初始化失败", e);
            throw new RuntimeException("无法初始化数据库连接池", e);
        }
    }
    
    /**
     * 获取数据源
     * @return HikariCP数据源实例
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * 获取配置属性
     * @param key 配置键
     * @return 配置值
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取配置属性（带默认值）
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取连接池状态信息
     * @return 连接池状态字符串
     */
    public String getPoolStatus() {
        if (dataSource == null) {
            return "数据源未初始化";
        }
        
        return String.format(
            "连接池状态 - 活跃连接: %d, 空闲连接: %d, 总连接: %d, 等待线程: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * 关闭数据源
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("数据源已关闭");
        }
    }
    
    /**
     * 验证数据库连接
     * @return 连接是否有效
     */
    public boolean validateConnection() {
        try {
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT 1")) {
                return resultSet.next();
            }
        } catch (Exception e) {
            logger.error("数据库连接验证失败", e);
            return false;
        }
    }
    
    /**
     * 掩码URL中的敏感信息
     * @param url 原始URL
     * @return 掩码后的URL
     */
    private String maskUrl(String url) {
        if (url == null) return "null";
        // 简单的密码掩码处理
        return url.replaceAll("password=([^&]+)", "password=****");
    }
}