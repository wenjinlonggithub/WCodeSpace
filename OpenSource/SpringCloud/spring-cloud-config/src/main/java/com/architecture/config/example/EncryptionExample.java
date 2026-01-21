package com.architecture.config.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Config加密解密示例
 *
 * 核心原理：
 * 1. 对称加密 - 使用encrypt.key配置密钥
 * 2. 非对称加密 - 使用RSA公钥/私钥
 * 3. 自动解密 - Config Server自动解密{cipher}前缀的值
 *
 * 加密流程：
 * POST /encrypt
 * Body: 明文
 * Response: {cipher}密文
 *
 * 解密流程：
 * POST /decrypt
 * Body: 密文
 * Response: 明文
 *
 * 业务场景：
 * - 数据库密码加密
 * - API密钥保护
 * - 敏感配置安全存储
 */
@Component
public class EncryptionExample {

    /**
     * 加密后的数据库密码
     * 在配置文件中格式：{cipher}AQA8K3sT...
     * Config Client获取时会自动解密
     */
    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${api.secret.key:}")
    private String apiSecretKey;

    /**
     * 获取解密后的密码
     * 应用程序无感知加密过程
     */
    public String getDecryptedPassword() {
        return dbPassword;
    }

    /**
     * 业务使用示例
     */
    public void connectToDatabase() {
        System.out.println("Connecting to database with password: " +
                          maskPassword(dbPassword));
        // 实际业务逻辑：使用解密后的密码连接数据库
    }

    private String maskPassword(String password) {
        if (password == null || password.length() < 4) {
            return "***";
        }
        return password.substring(0, 2) + "***" +
               password.substring(password.length() - 2);
    }
}
