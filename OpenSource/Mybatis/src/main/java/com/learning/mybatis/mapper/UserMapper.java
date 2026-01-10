package com.learning.mybatis.mapper;

import com.learning.mybatis.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户Mapper接口
 * 
 * 演示MyBatis的Mapper接口定义和注解使用
 * 同时支持注解和XML两种配置方式
 */
@Mapper
@Repository
public interface UserMapper {
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 插入用户（XML配置）
     */
    int insert(User user);
    
    /**
     * 选择性插入用户（只插入非空字段）
     */
    int insertSelective(User user);
    
    /**
     * 注解方式插入用户
     */
    @Insert("INSERT INTO user(username, email, password, real_name, phone, age, gender, status, create_time, update_time) " +
            "VALUES(#{username}, #{email}, #{password}, #{realName}, #{phone}, #{age}, #{gender}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertWithAnnotation(User user);
    
    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results(id = "userResultMap", value = {
        @Result(property = "id", column = "id", id = true),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    User selectById(@Param("id") Long id);
    
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 查询所有用户
     */
    List<User> selectAll();
    
    /**
     * 根据条件查询用户
     */
    List<User> selectByCondition(User condition);
    
    /**
     * 分页查询用户
     */
    @Select("SELECT * FROM user LIMIT #{offset}, #{limit}")
    @ResultMap("userResultMap")
    List<User> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计所有用户数量
     */
    @Select("SELECT COUNT(*) FROM user")
    int countAll();
    
    /**
     * 根据状态统计用户数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);
    
    /**
     * 根据ID更新用户
     */
    int updateById(User user);
    
    /**
     * 选择性更新用户（只更新非空字段）
     */
    int updateSelective(User user);
    
    /**
     * 批量更新用户状态
     */
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);
    
    /**
     * 根据性别更新年龄
     */
    @Update("UPDATE user SET age = #{age}, update_time = NOW() WHERE gender = #{gender}")
    int updateAgeByGender(@Param("gender") Integer gender, @Param("age") Integer age);
    
    /**
     * 根据ID删除用户
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据状态删除用户
     */
    @Delete("DELETE FROM user WHERE status = #{status}")
    int deleteByStatus(@Param("status") Integer status);
    
    /**
     * 软删除用户（更新状态为禁用）
     */
    @Update("UPDATE user SET status = 0, update_time = NOW() WHERE id = #{id}")
    int softDelete(@Param("id") Long id);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量插入用户
     */
    int batchInsert(@Param("users") List<User> users);
    
    /**
     * 根据ID列表查询用户
     */
    List<User> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 批量删除用户
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    // ==================== 复杂查询 ====================
    
    /**
     * 查询用户及其角色信息（一对多）
     */
    User selectUserWithRoles(@Param("userId") Long userId);
    
    /**
     * 查询用户及其详细信息（一对一）
     */
    User selectUserWithProfile(@Param("userId") Long userId);
    
    /**
     * 查询用户及其订单信息（一对多）
     */
    User selectUserWithOrders(@Param("userId") Long userId);
    
    /**
     * 高级搜索 - 支持多条件组合
     */
    List<User> advancedSearch(@Param("username") String username,
                             @Param("email") String email,
                             @Param("minAge") Integer minAge,
                             @Param("maxAge") Integer maxAge,
                             @Param("gender") Integer gender,
                             @Param("status") Integer status);
    
    /**
     * 模糊搜索用户
     */
    @Select("SELECT * FROM user WHERE " +
            "(#{keyword} IS NULL OR username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%'))")
    @ResultMap("userResultMap")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    /**
     * 按年龄段统计用户分布
     */
    @Select("SELECT " +
            "CASE " +
            "  WHEN age < 18 THEN '未成年' " +
            "  WHEN age BETWEEN 18 AND 30 THEN '青年' " +
            "  WHEN age BETWEEN 31 AND 50 THEN '中年' " +
            "  ELSE '老年' " +
            "END AS age_group, " +
            "COUNT(*) AS user_count " +
            "FROM user " +
            "WHERE status = 1 " +
            "GROUP BY age_group " +
            "ORDER BY MIN(age)")
    List<java.util.Map<String, Object>> getUserAgeDistribution();
    
    /**
     * 获取用户注册趋势（按月统计）
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m') AS month, " +
            "COUNT(*) AS register_count " +
            "FROM user " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month")
    List<java.util.Map<String, Object>> getUserRegisterTrend();
}