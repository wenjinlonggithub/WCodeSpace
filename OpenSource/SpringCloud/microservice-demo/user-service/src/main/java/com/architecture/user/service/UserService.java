package com.architecture.user.service;

import com.architecture.user.domain.User;
import com.architecture.user.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * 核心业务逻辑:
 * 1. 用户信息查询: 提供给其他服务调用
 * 2. 用户创建: 注册新用户
 * 3. 用户信息更新: 修改用户资料
 * 4. 密码加密: 使用BCrypt加密密码
 *
 * 数据转换:
 * - User -> UserDTO: 隐藏敏感信息
 * - UserDTO -> User: 接收外部数据
 *
 * @author Architecture Team
 */
@Service
public class UserService {

    /**
     * 根据ID查询用户
     * 模拟数据库查询
     */
    public UserDTO getUserById(Long id) {
        // TODO: 实际应该从数据库查询
        // User user = userMapper.selectById(id);

        // 模拟数据
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPhone("13800138000");
        user.setRealName("张三");
        user.setStatus(1);
        user.setRoles("ROLE_USER");

        return convertToDTO(user);
    }

    /**
     * 根据用户名查询用户
     */
    public UserDTO getUserByUsername(String username) {
        // TODO: userMapper.selectByUsername(username);

        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setRealName("张三");
        user.setStatus(1);

        return convertToDTO(user);
    }

    /**
     * 创建用户
     */
    public UserDTO createUser(UserDTO userDTO) {
        // TODO:
        // 1. 验证用户名是否已存在
        // 2. 加密密码: BCrypt.hashpw(password)
        // 3. 保存到数据库: userMapper.insert(user)

        userDTO.setId(System.currentTimeMillis());
        userDTO.setStatus(1);
        userDTO.setRoles("ROLE_USER");

        return userDTO;
    }

    /**
     * 更新用户信息
     */
    public UserDTO updateUser(UserDTO userDTO) {
        // TODO: userMapper.updateById(user);
        return userDTO;
    }

    /**
     * User实体转DTO
     * 过滤敏感信息(密码)
     */
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
