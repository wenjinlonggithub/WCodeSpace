package com.architecture.business.user;

import java.io.Serializable;
import java.util.List;

/**
 * 用户服务接口
 *
 * 演示 Dubbo 服务定义的最佳实践
 */
public interface UserService {

    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     * @param user 用户信息
     * @return 用户ID
     */
    Long createUser(User user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否成功
     */
    Boolean updateUser(User user);

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUser(Long userId);

    /**
     * 批量查询用户
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> batchGetUsers(List<Long> userIds);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录令牌
     */
    String login(String username, String password);

    /**
     * 用户登出
     * @param token 登录令牌
     * @return 是否成功
     */
    Boolean logout(String token);
}

/**
 * 用户实体类
 * 注意：Dubbo 传输的对象必须实现 Serializable 接口
 */
class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer status;
    private Long createTime;
    private Long updateTime;

    public User() {
    }

    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }
}
