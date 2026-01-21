package com.architecture.user.mapper;

import com.architecture.user.domain.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户数据访问层
 *
 * MyBatis Mapper接口:
 * 1. 使用注解方式编写SQL
 * 2. 支持动态SQL
 * 3. 自动映射结果集
 *
 * 最佳实践:
 * - 简单查询使用@Select注解
 * - 复杂查询使用XML配置
 * - 批量操作考虑性能优化
 *
 * @author Architecture Team
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     *
     * SQL执行流程:
     * 1. MyBatis动态代理生成实现类
     * 2. 解析@Select注解中的SQL
     * 3. 替换#{id}参数
     * 4. 执行JDBC查询
     * 5. 自动映射结果到User对象
     */
    @Select("SELECT id, username, password, email, phone, real_name, " +
            "status, roles, created_at, updated_at " +
            "FROM users WHERE id = #{id}")
    @Results(id = "userResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "status", column = "status"),
            @Result(property = "roles", column = "roles"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     *
     * 应用场景:
     * - 用户登录验证
     * - 检查用户名是否已存在
     */
    @Select("SELECT id, username, password, email, phone, real_name, " +
            "status, roles, created_at, updated_at " +
            "FROM users WHERE username = #{username}")
    @ResultMap("userResultMap")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT id, username, password, email, phone, real_name, " +
            "status, roles, created_at, updated_at " +
            "FROM users WHERE email = #{email}")
    @ResultMap("userResultMap")
    User selectByEmail(@Param("email") String email);

    /**
     * 插入新用户
     *
     * 关键特性:
     * - useGeneratedKeys=true: 获取自增主键
     * - keyProperty="id": 将主键值回填到user.id
     *
     * 执行后user对象的id字段会被自动赋值
     */
    @Insert("INSERT INTO users (username, password, email, phone, real_name, " +
            "status, roles, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{realName}, " +
            "#{status}, #{roles}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(User user);

    /**
     * 更新用户信息
     *
     * 注意:
     * - 不更新password字段(单独接口处理)
     * - 不更新username字段(不允许修改)
     * - 自动更新updated_at为当前时间
     */
    @Update("UPDATE users SET " +
            "email = #{email}, " +
            "phone = #{phone}, " +
            "real_name = #{realName}, " +
            "status = #{status}, " +
            "roles = #{roles}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateById(User user);

    /**
     * 更新用户密码
     *
     * 安全要求:
     * - 密码必须经过BCrypt加密
     * - 独立的密码更新接口便于审计
     */
    @Update("UPDATE users SET password = #{password}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新用户状态
     *
     * 状态说明:
     * - 0: 禁用
     * - 1: 正常
     * - 2: 锁定
     */
    @Update("UPDATE users SET status = #{status}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除用户(物理删除)
     *
     * 注意:
     * - 生产环境建议使用逻辑删除
     * - 物理删除无法恢复
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查用户名是否存在
     *
     * 应用场景:
     * - 注册时验证用户名唯一性
     * - 返回值>0表示已存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE email = #{email}")
    int countByEmail(@Param("email") String email);
}
