package com.architecture.business.user;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * 演示 Dubbo 服务提供者的实现
 *
 * 配置方式：
 * 1. XML 配置：
 *    <dubbo:service interface="com.architecture.business.user.UserService"
 *                   ref="userService" version="1.0.0" timeout="3000" />
 *
 * 2. 注解配置：
 *    @DubboService(version = "1.0.0", timeout = 3000)
 *
 * 3. API 配置：
 *    ServiceConfig<UserService> service = new ServiceConfig<>();
 *    service.setInterface(UserService.class);
 *    service.setRef(new UserServiceImpl());
 */
public class UserServiceImpl implements UserService {

    // 模拟数据库，使用内存存储
    private static final Map<Long, User> userDatabase = new ConcurrentHashMap<>();
    private static final Map<String, User> usernameIndex = new ConcurrentHashMap<>();
    private static final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    static {
        // 初始化测试数据
        User user1 = new User(1L, "alice", "alice@example.com");
        user1.setPassword("123456");
        user1.setPhone("13800138001");
        user1.setStatus(1);
        user1.setCreateTime(System.currentTimeMillis());

        User user2 = new User(2L, "bob", "bob@example.com");
        user2.setPassword("123456");
        user2.setPhone("13800138002");
        user2.setStatus(1);
        user2.setCreateTime(System.currentTimeMillis());

        userDatabase.put(1L, user1);
        userDatabase.put(2L, user2);
        usernameIndex.put("alice", user1);
        usernameIndex.put("bob", user2);
    }

    @Override
    public User getUserById(Long userId) {
        System.out.println("[UserService] 查询用户: userId=" + userId);

        User user = userDatabase.get(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        // 返回时不包含密码
        User result = cloneUserWithoutPassword(user);
        System.out.println("[UserService] 查询结果: " + result);
        return result;
    }

    @Override
    public User getUserByUsername(String username) {
        System.out.println("[UserService] 查询用户: username=" + username);

        User user = usernameIndex.get(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }

        return cloneUserWithoutPassword(user);
    }

    @Override
    public Long createUser(User user) {
        System.out.println("[UserService] 创建用户: " + user);

        // 验证用户名是否已存在
        if (usernameIndex.containsKey(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }

        // 生成用户ID
        Long userId = generateUserId();
        user.setId(userId);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(1);

        // 保存用户
        userDatabase.put(userId, user);
        usernameIndex.put(user.getUsername(), user);

        System.out.println("[UserService] 用户创建成功: userId=" + userId);
        return userId;
    }

    @Override
    public Boolean updateUser(User user) {
        System.out.println("[UserService] 更新用户: " + user);

        User existingUser = userDatabase.get(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在: " + user.getId());
        }

        // 更新用户信息
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        existingUser.setUpdateTime(System.currentTimeMillis());

        System.out.println("[UserService] 用户更新成功");
        return true;
    }

    @Override
    public Boolean deleteUser(Long userId) {
        System.out.println("[UserService] 删除用户: userId=" + userId);

        User user = userDatabase.remove(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        usernameIndex.remove(user.getUsername());

        System.out.println("[UserService] 用户删除成功");
        return true;
    }

    @Override
    public List<User> batchGetUsers(List<Long> userIds) {
        System.out.println("[UserService] 批量查询用户: userIds=" + userIds);

        List<User> users = userIds.stream()
            .map(userDatabase::get)
            .filter(Objects::nonNull)
            .map(this::cloneUserWithoutPassword)
            .collect(Collectors.toList());

        System.out.println("[UserService] 查询到 " + users.size() + " 个用户");
        return users;
    }

    @Override
    public String login(String username, String password) {
        System.out.println("[UserService] 用户登录: username=" + username);

        User user = usernameIndex.get(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成登录令牌
        String token = generateToken(user.getId());
        tokenStore.put(token, user.getId());

        System.out.println("[UserService] 登录成功: token=" + token);
        return token;
    }

    @Override
    public Boolean logout(String token) {
        System.out.println("[UserService] 用户登出: token=" + token);

        Long userId = tokenStore.remove(token);
        if (userId == null) {
            return false;
        }

        System.out.println("[UserService] 登出成功");
        return true;
    }

    private User cloneUserWithoutPassword(User user) {
        User clone = new User();
        clone.setId(user.getId());
        clone.setUsername(user.getUsername());
        clone.setEmail(user.getEmail());
        clone.setPhone(user.getPhone());
        clone.setStatus(user.getStatus());
        clone.setCreateTime(user.getCreateTime());
        clone.setUpdateTime(user.getUpdateTime());
        return clone;
    }

    private Long generateUserId() {
        return (long) (userDatabase.size() + 1);
    }

    private String generateToken(Long userId) {
        return "TOKEN_" + userId + "_" + System.currentTimeMillis();
    }
}

/**
 * 服务提供者最佳实践：
 *
 * 1. 接口设计：
 *    - 参数和返回值必须实现 Serializable
 *    - 避免使用重载方法（不同参数类型的同名方法）
 *    - 使用明确的方法名
 *    - 考虑向后兼容性
 *
 * 2. 异常处理：
 *    - 抛出业务异常时使用 RuntimeException
 *    - 异常信息要清晰明确
 *    - 避免抛出不可序列化的异常
 *
 * 3. 性能优化：
 *    - 避免返回大对象
 *    - 使用批量接口减少调用次数
 *    - 合理设置超时时间
 *    - 使用异步调用（适用场景）
 *
 * 4. 安全性：
 *    - 敏感信息不要返回（如密码）
 *    - 做好参数校验
 *    - 实现访问控制
 *
 * 5. 版本管理：
 *    - 使用 version 进行版本控制
 *    - 新增方法不影响老版本
 *    - 使用 @deprecated 标记废弃方法
 */
