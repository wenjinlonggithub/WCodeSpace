package com.architecture.order.client;

import com.architecture.order.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务降级处理
 *
 * 降级触发条件:
 * 1. 服务调用超时
 * 2. 服务调用异常
 * 3. 熔断器打开状态
 *
 * 降级策略:
 * 1. 返回默认值/缓存数据
 * 2. 返回降级提示
 * 3. 异步补偿
 *
 * @author architecture
 */
@Component
public class UserServiceFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceFallback.class);

    @Override
    public UserDTO getUserById(Long id) {
        log.warn("用户服务调用失败，执行降级逻辑，userId={}", id);

        // 降级策略1: 返回默认用户信息
        UserDTO defaultUser = new UserDTO();
        defaultUser.setId(id);
        defaultUser.setUsername("系统用户");
        defaultUser.setNickname("临时用户");
        defaultUser.setEmail("fallback@example.com");

        return defaultUser;
    }

    @Override
    public Boolean userExists(Long id) {
        log.warn("用户服务调用失败，执行降级逻辑，userId={}", id);

        // 降级策略2: 默认返回true，允许继续执行
        // 注意: 这里需要根据业务需求决定返回true还是false
        return true;
    }
}
