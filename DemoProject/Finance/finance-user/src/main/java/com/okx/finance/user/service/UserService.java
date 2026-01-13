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

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);

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
