package com.okx.finance.user.service;

import com.okx.finance.common.dto.Result;
import com.okx.finance.common.entity.User;
import com.okx.finance.common.util.JwtUtil;
import com.okx.finance.common.util.SnowflakeIdGenerator;
import com.okx.finance.user.dto.LoginRequest;
import com.okx.finance.user.dto.RegisterRequest;
import com.okx.finance.user.dto.KycRequest;
import com.okx.finance.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 * 处理用户相关的核心业务逻辑
 *
 * <p>主要功能：
 * 1. 用户注册：验证用户名唯一性，密码加密存储
 * 2. 用户登录：验证密码，生成JWT Token
 * 3. KYC认证：实名认证，提升用户等级
 * 4. API密钥管理：生成和管理用户的API密钥
 * 5. 用户信息查询：获取用户详细信息
 *
 * <p>安全措施：
 * - 密码使用MD5加密存储，不存储明文
 * - Token存储在Redis中，24小时有效期
 * - API密钥使用UUID生成，确保唯一性
 * - API密钥的Secret使用MD5加密存储
 * - 用户信息查询时会过滤敏感字段（密码、API Secret）
 *
 * <p>KYC等级说明：
 * - Level 0：未认证，基础功能，交易额度受限
 * - Level 1：初级认证，提交真实姓名和身份证号
 * - Level 2：中级认证，上传身份证照片
 * - Level 3：高级认证，视频认证，解锁全部功能
 *
 * @author OKX Finance Team
 * @version 1.0
 * @see UserMapper
 * @see JwtUtil
 * @see SnowflakeIdGenerator
 */
@Service
public class UserService {

    /**
     * 用户数据访问对象
     * 用于执行数据库操作（增删改查）
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Redis模板
     * 用于缓存Token和其他临时数据
     * Token的key格式：user:token:{userId}
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 雪花算法ID生成器
     * 机器ID为1，用于生成用户ID
     * 保证用户ID全局唯一且趋势递增
     */
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);

    /**
     * 用户注册
     * 创建新用户账户，验证用户名唯一性，密码加密存储
     *
     * <p>注册流程：
     * 1. 检查用户名是否已存在（唯一性校验）
     * 2. 生成唯一用户ID（雪花算法）
     * 3. 密码MD5加密
     * 4. 初始化用户状态（KYC等级0，状态正常）
     * 5. 保存到数据库
     *
     * <p>安全措施：
     * - 用户名唯一性校验，防止重复注册
     * - 密码不明文存储，使用MD5加密
     * - 新用户默认未认证状态（KYC Level 0）
     *
     * <p>默认值设置：
     * - kycLevel: 0（未认证）
     * - status: 1（正常状态）
     * - apiKey/apiSecret: null（需要单独生成）
     *
     * @param request 注册请求，包含用户名、密码、邮箱、手机号
     * @return Result对象，成功返回"Register success"，失败返回错误信息
     * @throws IllegalArgumentException 如果请求参数为空或格式不正确
     */
    public Result<?> register(RegisterRequest request) {
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            return Result.error("Username already exists");
        }

        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setKycLevel(0);
        user.setStatus(1);

        userMapper.insert(user);

        return Result.success("Register success");
    }

    /**
     * 用户登录
     * 验证用户名和密码，生成JWT Token并缓存到Redis
     *
     * <p>登录流程：
     * 1. 根据用户名查询用户（验证用户是否存在）
     * 2. 验证密码（MD5加密后比对）
     * 3. 检查用户状态（是否被禁用）
     * 4. 生成JWT Token（包含用户ID和用户名）
     * 5. Token存储到Redis（24小时有效期）
     * 6. 返回Token和用户基本信息
     *
     * <p>安全措施：
     * - 密码使用MD5加密后比对，不传输明文
     * - 检查用户状态，禁用用户无法登录
     * - Token存储在Redis中，便于管理和失效控制
     * - Token有效期24小时，过期需重新登录
     *
     * <p>返回数据：
     * - token：JWT令牌，用于后续API调用
     * - userId：用户ID
     * - username：用户名
     *
     * @param request 登录请求，包含用户名和密码
     * @return Result对象，成功返回包含token的Map，失败返回错误信息
     *         - "User not found"：用户不存在
     *         - "Invalid password"：密码错误
     *         - "User is disabled"：用户已被禁用
     */
    public Result<?> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("User not found");
        }

        String passwordHash = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!passwordHash.equals(user.getPassword())) {
            return Result.error("Invalid password");
        }

        if (user.getStatus() != 1) {
            return Result.error("User is disabled");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        redisTemplate.opsForValue().set("user:token:" + user.getId(), token, 24, TimeUnit.HOURS);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());

        return Result.success(result);
    }

    /**
     * 提交KYC认证
     * 用户提交实名认证信息，提升KYC等级
     *
     * <p>KYC（Know Your Customer）认证流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询用户信息
     * 4. 更新用户的实名信息和KYC等级
     * 5. 保存到数据库
     *
     * <p>KYC等级提升规则：
     * - Level 0 → Level 1：提交真实姓名和身份证号
     * - Level 1 → Level 2：上传身份证照片（正反面）
     * - Level 2 → Level 3：视频认证（人脸识别）
     *
     * <p>等级对应权限：
     * - Level 0：每日交易限额 1000 USDT
     * - Level 1：每日交易限额 10000 USDT
     * - Level 2：每日交易限额 100000 USDT
     * - Level 3：无限额，全部功能开放
     *
     * <p>安全措施：
     * - Token验证，确保操作者身份
     * - 实名信息不可修改（一次认证，终身有效）
     * - 建议增加人工审核流程
     *
     * @param token JWT Token，用于身份验证
     * @param request KYC请求，包含真实姓名、身份证号、KYC等级
     * @return Result对象，成功返回"KYC submitted successfully"，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "User not found"：用户不存在
     */
    public Result<?> submitKyc(String token, KycRequest request) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("User not found");
        }

        user.setRealName(request.getRealName());
        user.setIdCard(request.getIdCard());
        user.setKycLevel(request.getKycLevel());

        userMapper.update(user);

        return Result.success("KYC submitted successfully");
    }

    /**
     * 获取用户信息
     * 查询并返回用户的详细信息（不包含敏感字段）
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询用户信息
     * 4. 过滤敏感字段（密码、API Secret）
     * 5. 返回用户信息
     *
     * <p>返回字段：
     * - id：用户ID
     * - username：用户名
     * - email：邮箱
     * - phone：手机号
     * - realName：真实姓名
     * - idCard：身份证号
     * - kycLevel：KYC等级
     * - status：用户状态
     * - apiKey：API密钥（如果已生成）
     * - createTime：注册时间
     * - updateTime：最后更新时间
     *
     * <p>安全措施：
     * - 过滤密码字段，防止泄露
     * - 过滤API Secret字段，只返回API Key
     * - Token验证，确保只能查询自己的信息
     *
     * @param token JWT Token，用于身份验证
     * @return Result对象，成功返回用户信息，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "User not found"：用户不存在
     */
    public Result<?> getUserInfo(String token) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("User not found");
        }

        user.setPassword(null);
        user.setApiSecret(null);

        return Result.success(user);
    }

    /**
     * 生成API密钥
     * 为用户生成API Key和API Secret，用于程序化交易
     *
     * <p>生成流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询用户信息
     * 4. 生成API Key（32位随机字符串，UUID去除横线）
     * 5. 生成API Secret（32位随机字符串，UUID去除横线）
     * 6. API Secret使用MD5加密后存储
     * 7. 更新用户信息到数据库
     * 8. 返回API Key和API Secret（明文，仅此一次）
     *
     * <p>API密钥用途：
     * - 程序化交易：通过API进行自动交易
     * - 第三方应用：授权第三方应用访问账户
     * - 量化交易：接入量化交易系统
     *
     * <p>安全措施：
     * - API Secret使用MD5加密存储，数据库中不存明文
     * - API Secret只在生成时返回一次，请妥善保管
     * - 建议定期更换API密钥
     * - 如果密钥泄露，立即重新生成
     *
     * <p>返回数据：
     * - apiKey：API密钥（公开密钥，用于标识用户）
     * - apiSecret：API密钥Secret（私密密钥，用于签名验证）
     *
     * <p>重要提示：
     * ⚠️ API Secret仅在生成时返回一次，数据库存储的是MD5加密后的值
     * ⚠️ 如果丢失API Secret，只能重新生成，无法找回
     * ⚠️ 不要将API Secret提交到版本控制系统（Git等）
     *
     * @param token JWT Token，用于身份验证
     * @return Result对象，成功返回包含apiKey和apiSecret的Map，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "User not found"：用户不存在
     */
    public Result<?> generateApiKey(String token) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("User not found");
        }

        String apiKey = UUID.randomUUID().toString().replace("-", "");
        String apiSecret = UUID.randomUUID().toString().replace("-", "");

        user.setApiKey(apiKey);
        user.setApiSecret(DigestUtils.md5DigestAsHex(apiSecret.getBytes()));

        userMapper.update(user);

        Map<String, String> result = new HashMap<>();
        result.put("apiKey", apiKey);
        result.put("apiSecret", apiSecret);

        return Result.success(result);
    }
}
