package com.example.springdemo.ioc;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 * 
 * 演示Spring IoC容器的接口与实现分离
 */
public interface UserRepository {
    
    /**
     * 保存用户
     */
    User save(User user);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 查找所有用户
     */
    List<User> findAll();
    
    /**
     * 删除用户
     */
    void deleteById(Long id);
    
    /**
     * 获取用户总数
     */
    long count();
}