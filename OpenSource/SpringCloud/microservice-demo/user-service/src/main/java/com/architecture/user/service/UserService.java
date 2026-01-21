package com.architecture.user.service;

import com.architecture.user.domain.User;
import com.architecture.user.dto.UserDTO;
import com.architecture.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * 事务管理:
 * - 写操作使用@Transactional保证数据一致性
 * - 读操作不需要事务
 *
 * @author Architecture Team
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * BCrypt密码加密器
     *
     * 特性:
     * - 每次加密结果不同(包含随机salt)
     * - 单向加密,无法解密
     * - 自动处理盐值,无需手动管理
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 根据ID查询用户
     *
     * 执行流程:
     * 1. 通过Mapper查询数据库
     * 2. MyBatis自动映射结果到User对象
     * 3. 转换为UserDTO(过滤password字段)
     * 4. 返回给调用方
     */
    public UserDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        return convertToDTO(user);
    }

    /**
     * 根据用户名查询用户
     *
     * 应用场景:
     * - 用户登录
     * - 检查用户名是否存在
     */
    public UserDTO getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        return convertToDTO(user);
    }

    /**
     * 根据邮箱查询用户
     */
    public UserDTO getUserByEmail(String email) {
        User user = userMapper.selectByEmail(email);
        return convertToDTO(user);
    }

    /**
     * 创建用户(注册)
     *
     * 业务流程:
     * 1. 验证用户名唯一性
     * 2. 验证邮箱唯一性
     * 3. 加密密码(BCrypt)
     * 4. 设置默认状态和角色
     * 5. 保存到数据库
     * 6. 返回用户信息(不含密码)
     *
     * 安全考虑:
     * - 密码必须加密存储
     * - 返回的DTO不包含密码
     * - 使用事务保证原子性
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserDTO userDTO) {
        // 1. 验证用户名是否已存在
        if (userMapper.countByUsername(userDTO.getUsername()) > 0) {
            throw new IllegalArgumentException("用户名已存在: " + userDTO.getUsername());
        }

        // 2. 验证邮箱是否已存在
        if (userDTO.getEmail() != null && userMapper.countByEmail(userDTO.getEmail()) > 0) {
            throw new IllegalArgumentException("邮箱已被使用: " + userDTO.getEmail());
        }

        // 3. 转换DTO为实体
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 4. 加密密码
        // 原始密码: "123456"
        // 加密后: "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi"
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 5. 设置默认值
        user.setStatus(1);  // 1:正常
        if (user.getRoles() == null) {
            user.setRoles("ROLE_USER");  // 默认角色
        }

        // 6. 保存到数据库
        // useGeneratedKeys=true会自动将生成的ID回填到user对象
        userMapper.insert(user);

        // 7. 返回用户信息(转为DTO过滤密码)
        return convertToDTO(user);
    }

    /**
     * 更新用户信息
     *
     * 注意:
     * - 不更新password(使用单独的updatePassword方法)
     * - 不更新username(用户名不允许修改)
     * - updated_at字段自动更新
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(UserDTO userDTO) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userDTO.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("用户不存在: " + userDTO.getId());
        }

        // 如果修改了邮箱,检查新邮箱是否被占用
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
            if (userMapper.countByEmail(userDTO.getEmail()) > 0) {
                throw new IllegalArgumentException("邮箱已被使用: " + userDTO.getEmail());
            }
        }

        // 更新用户信息
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        userMapper.updateById(user);

        // 返回更新后的信息
        return convertToDTO(userMapper.selectById(user.getId()));
    }

    /**
     * 更新用户密码
     *
     * 业务流程:
     * 1. 验证旧密码(可选,根据业务需求)
     * 2. 加密新密码
     * 3. 更新数据库
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 加密新密码并更新
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态: 0-禁用, 1-正常, 2-锁定
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) {
        userMapper.updateStatus(userId, status);
    }

    /**
     * 删除用户
     *
     * 注意:
     * - 物理删除,数据无法恢复
     * - 生产环境建议使用逻辑删除(status=deleted)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 验证用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功返回用户信息,失败返回null
     */
    public UserDTO login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new IllegalStateException("账号已被禁用或锁定");
        }

        return convertToDTO(user);
    }

    /**
     * User实体转DTO
     * 过滤敏感信息(密码)
     *
     * 为什么要转DTO:
     * 1. 安全: 不返回password等敏感字段
     * 2. 解耦: API接口不直接暴露数据库实体
     * 3. 灵活: DTO可以组合多个实体的字段
     */
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // password字段不会被复制(DTO中没有password字段)
        return dto;
    }
}
