package com.architecture.aicoding.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * OpenAI配置类
 * 使用线程安全的双重检查单例模式
 * 负责加载和管理OpenAI API相关配置
 */
public class OpenAIConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIConfig.class);
    private static final String CONFIG_FILE = "application.properties";

    private String apiKey;
    private String apiBaseUrl;
    private String model;
    private int maxTokens;
    private double temperature;
    private int timeout;
    private int maxRetries;
    private boolean validationEnabled;
    private boolean syntaxCheckEnabled;

    // 单例实例
    private static volatile OpenAIConfig instance;

    /**
     * 私有构造函数，防止外部实例化
     */
    private OpenAIConfig() {
        loadProperties();
    }

    /**
     * 获取单例实例（线程安全的双重检查锁定）
     * @return OpenAIConfig实例
     */
    public static OpenAIConfig getInstance() {
        if (instance == null) {
            synchronized (OpenAIConfig.class) {
                if (instance == null) {
                    instance = new OpenAIConfig();
                }
            }
        }
        return instance;
    }

    /**
     * 从配置文件加载配置
     */
    private void loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("配置文件 {} 未找到，使用默认配置", CONFIG_FILE);
                setDefaultValues();
                return;
            }

            properties.load(input);

            // 加载OpenAI配置
            this.apiKey = getProperty(properties, "openai.api.key", "");
            this.apiBaseUrl = getProperty(properties, "openai.api.base.url", "https://api.openai.com");
            this.model = getProperty(properties, "openai.api.model", "gpt-4");
            this.maxTokens = Integer.parseInt(getProperty(properties, "openai.api.max.tokens", "2000"));
            this.temperature = Double.parseDouble(getProperty(properties, "openai.api.temperature", "0.7"));
            this.timeout = Integer.parseInt(getProperty(properties, "openai.api.timeout", "30"));
            this.maxRetries = Integer.parseInt(getProperty(properties, "openai.api.max.retries", "3"));

            // 加载验证配置
            this.validationEnabled = Boolean.parseBoolean(getProperty(properties, "code.validation.enabled", "true"));
            this.syntaxCheckEnabled = Boolean.parseBoolean(getProperty(properties, "code.validation.syntax.check", "true"));

            // 检查API Key是否从环境变量加载
            if (apiKey.contains("${OPENAI_API_KEY") || apiKey.equals("your-api-key-here")) {
                String envApiKey = System.getenv("OPENAI_API_KEY");
                if (envApiKey != null && !envApiKey.isEmpty()) {
                    this.apiKey = envApiKey;
                    logger.info("从环境变量加载API Key");
                } else {
                    logger.warn("未配置有效的OpenAI API Key，请设置环境变量 OPENAI_API_KEY 或在配置文件中配置");
                }
            }

            logger.info("配置加载成功 - Model: {}, BaseURL: {}, Timeout: {}s", model, apiBaseUrl, timeout);

        } catch (IOException e) {
            logger.error("加载配置文件失败", e);
            setDefaultValues();
        } catch (NumberFormatException e) {
            logger.error("配置文件中的数值格式错误", e);
            setDefaultValues();
        }
    }

    /**
     * 获取配置项，支持默认值
     */
    private String getProperty(Properties properties, String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 设置默认配置值
     */
    private void setDefaultValues() {
        this.apiKey = "";
        this.apiBaseUrl = "https://api.openai.com";
        this.model = "gpt-4";
        this.maxTokens = 2000;
        this.temperature = 0.7;
        this.timeout = 30;
        this.maxRetries = 3;
        this.validationEnabled = true;
        this.syntaxCheckEnabled = true;
        logger.info("使用默认配置");
    }

    // Getter方法
    public String getApiKey() {
        return apiKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public boolean isSyntaxCheckEnabled() {
        return syntaxCheckEnabled;
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here");
    }
}
