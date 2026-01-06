package com.example.springdemo.ioc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户仓储实现类
 * 
 * 演示Spring IoC容器管理的Repository组件
 * 使用@Repository注解标记为数据访问层组件
 */
@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    // 使用内存存储模拟数据库
    private final Map<Long, User> userDatabase = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        userDatabase.put(user.getId(), user);
        log.info("💾 保存用户: {}", user);
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        User user = userDatabase.get(id);
        log.info("🔍 根据ID({})查找用户: {}", id, user != null ? "找到" : "未找到");
        return Optional.ofNullable(user);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        User user = userDatabase.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        log.info("📧 根据邮箱({})查找用户: {}", email, user != null ? "找到" : "未找到");
        return Optional.ofNullable(user);
    }
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>(userDatabase.values());
        log.info("📋 查找所有用户，共{}个", users.size());
        return users;
    }
    
    @Override
    public void deleteById(Long id) {
        User removed = userDatabase.remove(id);
        log.info("🗑️ 删除用户ID({}): {}", id, removed != null ? "成功" : "用户不存在");
    }
    
    @Override
    public long count() {
        long count = userDatabase.size();
        log.info("📊 用户总数: {}", count);
        return count;
    }
}