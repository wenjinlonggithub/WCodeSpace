package com.okx.finance.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JSON Web Token
 *
 * <p>JWT用途：
 * 1. 用户登录后生成token，后续请求携带token验证身份
 * 2. 无状态认证，不需要在服务端存储session
 * 3. 支持分布式系统，token可在多个服务间共享
 *
 * <p>Token结构：
 * Header.Payload.Signature
 * - Header: 包含token类型和加密算法
 * - Payload: 包含用户信息（userId, username等）
 * - Signature: 签名，防止token被篡改
 *
 * <p>安全建议：
 * 1. SECRET密钥应存储在配置文件中，不应硬编码
 * 2. 定期更换密钥
 * 3. Token应通过HTTPS传输
 * 4. 敏感信息不应放在Payload中（Payload是Base64编码，可被解码）
 *
 * @author OKX Finance Team
 * @version 1.0
 */
public class JwtUtil {
    /**
     * JWT密钥
     * 用于签名和验证token
     * 生产环境应从配置文件读取，并定期更换
     */
    private static final String SECRET = "okx-finance-secret-key-for-jwt-token-generation-2024";

    /**
     * Token过期时间（毫秒）
     * 86400000ms = 24小时
     * 可根据安全需求调整，建议不超过7天
     */
    private static final long EXPIRATION = 86400000L;

    /**
     * 密钥对象
     * 使用HMAC-SHA256算法
     */
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * 生成JWT Token
     *
     * <p>生成的Token包含：
     * - userId: 用户ID
     * - username: 用户名
     * - iat: 签发时间
     * - exp: 过期时间
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public static String generateToken(Long userId, String username) {
        // 构建载荷（Payload）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 生成Token
        return Jwts.builder()
                .setClaims(claims)                                                  // 设置载荷
                .setSubject(username)                                               // 设置主题（通常是用户名）
                .setIssuedAt(new Date())                                           // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))  // 设置过期时间
                .signWith(KEY, SignatureAlgorithm.HS256)                           // 使用HS256算法签名
                .compact();                                                         // 生成最终的Token字符串
    }

    /**
     * 解析JWT Token
     *
     * <p>从Token中提取载荷信息（Claims）
     * 如果Token无效或已过期，会抛出异常
     *
     * @param token JWT Token字符串
     * @return Claims对象，包含Token中的所有信息
     * @throws io.jsonwebtoken.JwtException 如果Token无效
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)      // 设置签名密钥
                .build()
                .parseClaimsJws(token)   // 解析Token
                .getBody();              // 获取载荷
    }

    /**
     * 验证Token是否有效
     *
     * <p>检查项：
     * 1. Token格式是否正确
     * 2. 签名是否有效
     * 3. Token是否已过期
     *
     * @param token JWT Token字符串
     * @return true-有效，false-无效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // Token格式错误、签名无效等都会抛出异常
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * <p>用于在API接口中识别当前登录用户
     *
     * @param token JWT Token字符串
     * @return 用户ID
     * @throws io.jsonwebtoken.JwtException 如果Token无效
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
}
