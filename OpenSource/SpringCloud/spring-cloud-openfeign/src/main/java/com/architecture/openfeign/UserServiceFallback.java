package com.architecture.openfeign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Feign降级处理
 *
 * 核心原理：
 * - 当服务调用失败时，自动执行降级逻辑
 * - 返回默认值或缓存数据
 * - 防止级联故障
 *
 * 触发场景：
 * 1. 服务不可用（连接超时）
 * 2. 服务响应超时（读取超时）
 * 3. 服务抛出异常
 * 4. 熔断器打开
 *
 * 业务场景：
 * - 用户服务不可用时，返回默认用户信息
 * - 商品服务超时时，返回缓存的商品列表
 * - 推荐服务失败时，返回热门商品
 */
@Component
public class UserServiceFallback implements UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceFallback.class);

    /**
     * 查询用户降级
     *
     * 策略：
     * 1. 记录错误日志
     * 2. 返回默认用户对象
     * 3. 可从缓存获取数据（Redis）
     */
    @Override
    public UserDTO getUserById(Long id) {
        logger.error("用户服务不可用，执行降级逻辑，用户ID: {}", id);

        // 返回默认用户
        return new UserDTO(id, "未知用户", "unknown@example.com", 0);
    }

    /**
     * 查询所有用户降级
     */
    @Override
    public List<UserDTO> getAllUsers() {
        logger.error("用户服务不可用，返回空列表");
        return new ArrayList<>();
    }

    /**
     * 创建用户降级
     */
    @Override
    public UserDTO createUser(UserDTO user) {
        logger.error("用户服务不可用，创建用户失败: {}", user.getUsername());
        throw new RuntimeException("用户服务暂时不可用，请稍后再试");
    }

    /**
     * 更新用户降级
     */
    @Override
    public UserDTO updateUser(Long id, UserDTO user) {
        logger.error("用户服务不可用，更新用户失败，用户ID: {}", id);
        throw new RuntimeException("用户服务暂时不可用，请稍后再试");
    }

    /**
     * 删除用户降级
     */
    @Override
    public void deleteUser(Long id) {
        logger.error("用户服务不可用，删除用户失败，用户ID: {}", id);
        throw new RuntimeException("用户服务暂时不可用，请稍后再试");
    }

    /**
     * 搜索用户降级
     */
    @Override
    public List<UserDTO> searchUsers(String keyword, String token) {
        logger.error("用户服务不可用，搜索失败，关键词: {}", keyword);
        return new ArrayList<>();
    }
}
