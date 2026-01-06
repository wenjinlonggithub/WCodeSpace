package com.example.springdemo.ioc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务类
 * 
 * 演示Spring IoC容器的服务层组件
 * 通过构造器注入依赖UserRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    @Getter
    private final UserRepository repository;
    
    /**
     * 保存用户
     */
    public User saveUser(User user) {
        log.info("🔄 UserService: 开始保存用户 {}", user.getName());
        
        // 检查邮箱是否已存在
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("邮箱已存在: " + user.getEmail());
        }
        
        User savedUser = repository.save(user);
        log.info("✅ UserService: 用户保存成功");
        return savedUser;
    }
    
    /**
     * 根据邮箱查找用户
     */
    public User findUserByEmail(String email) {
        log.info("🔍 UserService: 查找邮箱为 {} 的用户", email);
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户未找到: " + email));
    }
    
    /**
     * 根据ID查找用户
     */
    public User findUserById(Long id) {
        log.info("🔍 UserService: 查找ID为 {} 的用户", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户未找到: " + id));
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        log.info("📋 UserService: 获取所有用户");
        return repository.findAll();
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        log.info("🗑️ UserService: 删除用户 ID={}", id);
        if (repository.findById(id).isEmpty()) {
            throw new RuntimeException("用户不存在: " + id);
        }
        repository.deleteById(id);
        log.info("✅ UserService: 用户删除成功");
    }
    
    /**
     * 获取用户总数
     */
    public long getUserCount() {
        long count = repository.count();
        log.info("📊 UserService: 当前用户总数 {}", count);
        return count;
    }
}