package com.example.springdemo.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Cache缓存演示服务
 * 
 * 演示Spring Cache的核心特性：
 * - @Cacheable 缓存查询结果
 * - @CachePut 更新缓存
 * - @CacheEvict 清除缓存
 * - @Caching 组合缓存操作
 * - 缓存条件控制
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "demoCache")
public class CacheDemoService {
    
    private final AtomicInteger callCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<String, User> userDatabase = new ConcurrentHashMap<>();
    
    public CacheDemoService() {
        // 初始化一些测试数据
        userDatabase.put("1", new User("1", "张三", "zhangsan@example.com"));
        userDatabase.put("2", new User("2", "李四", "lisi@example.com"));
        userDatabase.put("3", new User("3", "王五", "wangwu@example.com"));
    }
    
    public void demonstrateCache() {
        log.info("\n💾 Spring Cache 缓存管理演示");
        
        // 1. 缓存查询演示
        demonstrateCacheable();
        
        // 2. 缓存更新演示
        demonstrateCachePut();
        
        // 3. 缓存清除演示
        demonstrateCacheEvict();
        
        // 4. 条件缓存演示
        demonstrateConditionalCache();
        
        // 5. 组合缓存操作
        demonstrateCaching();
        
        // 6. 自定义缓存键
        demonstrateCustomKeys();
    }
    
    private void demonstrateCacheable() {
        log.info("\n--- @Cacheable 缓存查询演示 ---");
        
        // 第一次调用，会执行方法并缓存结果
        log.info("第一次查询用户1:");
        User user1 = getUserById("1");
        log.info("返回结果: {}", user1.getName());
        
        // 第二次调用，直接从缓存返回
        log.info("第二次查询用户1（应该从缓存获取）:");
        User user2 = getUserById("1");
        log.info("返回结果: {}", user2.getName());
        
        log.info("总方法调用次数: {}", callCounter.get());
    }
    
    private void demonstrateCachePut() {
        log.info("\n--- @CachePut 缓存更新演示 ---");
        
        // 更新用户信息，同时更新缓存
        User updatedUser = updateUser("1", "张三（已更新）", "zhangsan_updated@example.com");
        log.info("更新用户: {}", updatedUser.getName());
        
        // 验证缓存已更新
        User cachedUser = getUserById("1");
        log.info("从缓存获取的更新后用户: {}", cachedUser.getName());
    }
    
    private void demonstrateCacheEvict() {
        log.info("\n--- @CacheEvict 缓存清除演示 ---");
        
        // 先查询一次，建立缓存
        getUserById("2");
        log.info("用户2已缓存");
        
        // 删除用户，清除缓存
        deleteUser("2");
        log.info("用户2已删除，缓存已清除");
        
        // 再次查询，会重新执行方法
        log.info("再次查询用户2（缓存已清除，会重新执行方法）:");
        try {
            getUserById("2");
        } catch (Exception e) {
            log.info("用户不存在: {}", e.getMessage());
        }
    }
    
    private void demonstrateConditionalCache() {
        log.info("\n--- 条件缓存演示 ---");
        
        // 测试条件缓存
        String longName = "这是一个很长的用户名称用来测试条件缓存";
        String shortName = "短名";
        
        getUserByNameConditional(longName);
        getUserByNameConditional(shortName);
        
        log.info("只有长名称（>5个字符）会被缓存");
    }
    
    private void demonstrateCaching() {
        log.info("\n--- @Caching 组合缓存操作演示 ---");
        
        User user = createUserWithMultipleCache("4", "赵六", "zhaoliu@example.com");
        log.info("创建用户并同时缓存到多个缓存: {}", user.getName());
    }
    
    private void demonstrateCustomKeys() {
        log.info("\n--- 自定义缓存键演示 ---");
        
        User user = getUserWithCustomKey("user", "1");
        log.info("使用自定义缓存键查询用户: {}", user.getName());
        
        // 再次查询，应该从缓存获取
        User cachedUser = getUserWithCustomKey("user", "1");
        log.info("从缓存获取用户: {}", cachedUser.getName());
    }
    
    /**
     * @Cacheable - 缓存查询结果
     * 如果缓存中有数据，直接返回；如果没有，执行方法并缓存结果
     */
    @Cacheable(cacheNames = "users", key = "#id")
    public User getUserById(String id) {
        int calls = callCounter.incrementAndGet();
        log.info("   💾 实际执行数据库查询 getUserById({}), 第{}次调用", id, calls);
        
        // 模拟数据库查询延迟
        simulateDelay(500);
        
        User user = userDatabase.get(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }
        
        return user;
    }
    
    /**
     * @CachePut - 更新缓存
     * 总是执行方法，并将结果更新到缓存
     */
    @CachePut(cacheNames = "users", key = "#id")
    public User updateUser(String id, String name, String email) {
        log.info("   💾 执行数据库更新 updateUser({}, {}, {})", id, name, email);
        
        User user = userDatabase.get(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }
        
        User updatedUser = new User(id, name, email);
        userDatabase.put(id, updatedUser);
        
        return updatedUser;
    }
    
    /**
     * @CacheEvict - 清除缓存
     * 执行方法后清除指定的缓存
     */
    @CacheEvict(cacheNames = "users", key = "#id")
    public void deleteUser(String id) {
        log.info("   💾 执行数据库删除 deleteUser({})", id);
        userDatabase.remove(id);
    }
    
    /**
     * 条件缓存 - 只有当名称长度大于5时才缓存
     */
    @Cacheable(cacheNames = "usersByName", key = "#name", condition = "#name.length() > 5")
    public User getUserByNameConditional(String name) {
        log.info("   💾 执行条件查询 getUserByNameConditional({})", name);
        simulateDelay(300);
        
        // 模拟根据名称查询
        return userDatabase.values().stream()
                .filter(user -> user.getName().equals(name))
                .findFirst()
                .orElse(new User("999", name, name + "@example.com"));
    }
    
    /**
     * @Caching - 组合多个缓存操作
     */
    @Caching(
        cacheable = @Cacheable(cacheNames = "users", key = "#id"),
        put = @CachePut(cacheNames = "usersByName", key = "#name")
    )
    public User createUserWithMultipleCache(String id, String name, String email) {
        log.info("   💾 执行用户创建 createUser({}, {}, {})", id, name, email);
        
        User user = new User(id, name, email);
        userDatabase.put(id, user);
        
        return user;
    }
    
    /**
     * 自定义缓存键 - 使用SpEL表达式
     */
    @Cacheable(cacheNames = "customKeys", key = "#type + '_' + #id")
    public User getUserWithCustomKey(String type, String id) {
        log.info("   💾 执行自定义键查询 getUserWithCustomKey({}, {})", type, id);
        simulateDelay(400);
        
        return getUserById(id);
    }
    
    /**
     * 清除所有缓存
     */
    @CacheEvict(cacheNames = {"users", "usersByName", "customKeys"}, allEntries = true)
    public void clearAllCache() {
        log.info("   💾 清除所有缓存");
    }
    
    /**
     * 根据条件清除缓存
     */
    @CacheEvict(cacheNames = "users", key = "#id", beforeInvocation = true)
    public void clearCacheBeforeOperation(String id) {
        log.info("   💾 方法执行前清除缓存: {}", id);
        // 这里可能会抛出异常，但由于beforeInvocation=true，缓存依然会被清除
    }
    
    private void simulateDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 显示缓存配置说明
     */
    public void showCacheConfiguration() {
        log.info("\n--- Spring Cache 配置说明 ---");
        log.info("1. @EnableCaching - 启用缓存支持");
        log.info("2. CacheManager - 缓存管理器");
        log.info("3. Cache - 具体的缓存实例");
        log.info("4. @CacheConfig - 类级别缓存配置");
        
        log.info("\n--- 缓存注解 ---");
        log.info("@Cacheable - 缓存方法返回值");
        log.info("@CachePut - 更新缓存");
        log.info("@CacheEvict - 清除缓存");
        log.info("@Caching - 组合缓存操作");
        
        log.info("\n--- 缓存属性 ---");
        log.info("cacheNames/value - 缓存名称");
        log.info("key - 缓存键（支持SpEL）");
        log.info("condition - 缓存条件（支持SpEL）");
        log.info("unless - 排除条件（支持SpEL）");
        log.info("allEntries - 清除所有条目");
        log.info("beforeInvocation - 方法执行前操作");
    }
    
    /**
     * 用户实体类
     */
    public static class User {
        private String id;
        private String name;
        private String email;
        private LocalDateTime lastModified = LocalDateTime.now();
        
        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public LocalDateTime getLastModified() { return lastModified; }
        public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
        
        @Override
        public String toString() {
            return "User{id='" + id + "', name='" + name + "', email='" + email + "'}";
        }
    }
}