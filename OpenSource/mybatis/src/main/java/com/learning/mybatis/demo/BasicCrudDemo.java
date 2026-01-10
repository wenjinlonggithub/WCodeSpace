package com.learning.mybatis.demo;

import com.learning.mybatis.entity.User;
import com.learning.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis基础CRUD操作演示
 * 
 * 演示MyBatis的基础功能：
 * - 增删改查操作
 * - 参数传递
 * - 结果映射
 * - 注解与XML两种配置方式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasicCrudDemo {
    
    private final UserMapper userMapper;
    
    public void demonstrateBasicCrud() {
        System.out.println("开始MyBatis基础CRUD操作演示");
        
        // 1. 创建操作演示
        demonstrateCreate();
        
        // 2. 查询操作演示
        demonstrateRead();
        
        // 3. 更新操作演示
        demonstrateUpdate();
        
        // 4. 删除操作演示
        demonstrateDelete();
        
        // 5. 批量操作演示
        demonstrateBatchOperations();
    }
    
    /**
     * 创建操作演示
     */
    @Transactional
    private void demonstrateCreate() {
        System.out.println("\n--- CREATE操作演示 ---");
        
        // 1. 单个插入
        User user = User.builder()
                .username("zhangsan")
                .email("zhangsan@example.com")
                .password("encrypted_password_123")
                .realName("张三")
                .phone("13812345678")
                .age(28)
                .gender(1) // 男
                .status(1) // 正常
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        int result = userMapper.insert(user);
        System.out.println("✅ 插入用户成功，影响行数: " + result + "，生成ID: " + user.getId());
        
        // 2. 选择性插入（只插入非空字段）
        User user2 = User.builder()
                .username("lisi")
                .email("lisi@example.com")
                .realName("李四")
                .age(25)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        
        int result2 = userMapper.insertSelective(user2);
        System.out.println("✅ 选择性插入用户成功，影响行数: " + result2 + "，生成ID: " + user2.getId());
        
        // 3. 使用注解方式插入
        User user3 = User.builder()
                .username("wangwu")
                .email("wangwu@example.com")
                .realName("王五")
                .age(30)
                .gender(1)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        int result3 = userMapper.insertWithAnnotation(user3);
        System.out.println("✅ 注解方式插入用户成功，影响行数: " + result3 + "，生成ID: " + user3.getId());
    }
    
    /**
     * 查询操作演示
     */
    private void demonstrateRead() {
        System.out.println("\n--- READ操作演示 ---");
        
        // 1. 根据ID查询
        Long userId = 1L;
        User user = userMapper.selectById(userId);
        if (user != null) {
            System.out.println("✅ 根据ID查询用户: " + user.toSafeString());
        } else {
            System.out.println("⚠️ 未找到ID为" + userId + "的用户");
        }
        
        // 2. 根据用户名查询
        String username = "zhangsan";
        User userByName = userMapper.selectByUsername(username);
        if (userByName != null) {
            System.out.println("✅ 根据用户名查询用户: " + userByName.toSafeString());
        }
        
        // 3. 查询所有用户
        List<User> allUsers = userMapper.selectAll();
        System.out.println("✅ 查询所有用户，共" + allUsers.size() + "个用户:");
        allUsers.forEach(u -> System.out.println("   - " + u.toSafeString()));
        
        // 4. 根据条件查询
        User condition = User.builder()
                .status(1) // 正常状态
                .gender(1) // 男性
                .build();
        List<User> usersByCondition = userMapper.selectByCondition(condition);
        System.out.println("✅ 根据条件查询用户，共" + usersByCondition.size() + "个用户:");
        
        // 5. 分页查询
        int offset = 0;
        int limit = 10;
        List<User> pagedUsers = userMapper.selectWithPagination(offset, limit);
        System.out.println("✅ 分页查询用户(offset=" + offset + ", limit=" + limit + "), 共" + pagedUsers.size() + "个用户:");
        
        // 6. 统计查询
        int totalCount = userMapper.countAll();
        int enabledCount = userMapper.countByStatus(1);
        System.out.println("✅ 统计查询 - 总用户数: " + totalCount + ", 启用用户数: " + enabledCount);
    }
    
    /**
     * 更新操作演示
     */
    @Transactional
    private void demonstrateUpdate() {
        System.out.println("\n--- UPDATE操作演示 ---");
        
        // 1. 根据主键更新
        User updateUser = User.builder()
                .id(1L)
                .username("zhangsan_updated")
                .email("zhangsan_new@example.com")
                .age(29)
                .updateTime(LocalDateTime.now())
                .build();
        
        int updateResult = userMapper.updateById(updateUser);
        System.out.println("✅ 根据ID更新用户，影响行数: " + updateResult);
        
        // 2. 选择性更新（只更新非空字段）
        User selectiveUpdate = User.builder()
                .id(2L)
                .phone("13987654321")
                .updateTime(LocalDateTime.now())
                .build();
        
        int selectiveResult = userMapper.updateSelective(selectiveUpdate);
        System.out.println("✅ 选择性更新用户，影响行数: " + selectiveResult);
        
        // 3. 批量更新
        int batchUpdateResult = userMapper.updateStatusBatch(List.of(1L, 2L, 3L), 1);
        System.out.println("✅ 批量更新用户状态，影响行数: " + batchUpdateResult);
        
        // 4. 根据条件更新
        int conditionUpdateResult = userMapper.updateAgeByGender(1, 35); // 将所有男性用户年龄更新为35
        System.out.println("✅ 根据条件更新用户年龄，影响行数: " + conditionUpdateResult);
    }
    
    /**
     * 删除操作演示
     */
    @Transactional
    private void demonstrateDelete() {
        System.out.println("\n--- DELETE操作演示 ---");
        
        // 为演示准备一些测试数据
        User tempUser1 = User.builder()
                .username("temp1")
                .email("temp1@example.com")
                .realName("临时用户1")
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insertSelective(tempUser1);
        
        User tempUser2 = User.builder()
                .username("temp2")
                .email("temp2@example.com")
                .realName("临时用户2")
                .status(0)
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insertSelective(tempUser2);
        
        System.out.println("✅ 创建临时用户用于删除演示");
        
        // 1. 根据ID删除
        int deleteResult = userMapper.deleteById(tempUser1.getId());
        System.out.println("✅ 根据ID删除用户，影响行数: " + deleteResult);
        
        // 2. 根据条件删除
        int conditionDeleteResult = userMapper.deleteByStatus(0); // 删除禁用用户
        System.out.println("✅ 根据状态删除用户，影响行数: " + conditionDeleteResult);
        
        // 3. 软删除演示（更新状态而不是物理删除）
        User softDeleteUser = User.builder()
                .username("soft_delete_test")
                .email("soft@example.com")
                .realName("软删除测试")
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insertSelective(softDeleteUser);
        
        int softDeleteResult = userMapper.softDelete(softDeleteUser.getId());
        System.out.println("✅ 软删除用户，影响行数: " + softDeleteResult);
        
        // 验证软删除
        User softDeletedUser = userMapper.selectById(softDeleteUser.getId());
        if (softDeletedUser != null && softDeletedUser.getStatus() == 0) {
            System.out.println("✅ 软删除成功，用户状态已更新为禁用");
        }
    }
    
    /**
     * 批量操作演示
     */
    @Transactional
    private void demonstrateBatchOperations() {
        System.out.println("\n--- 批量操作演示 ---");
        
        // 1. 批量插入
        List<User> batchUsers = List.of(
            User.builder()
                .username("batch1")
                .email("batch1@example.com")
                .realName("批量用户1")
                .age(20)
                .status(1)
                .createTime(LocalDateTime.now())
                .build(),
            User.builder()
                .username("batch2")
                .email("batch2@example.com")
                .realName("批量用户2")
                .age(21)
                .status(1)
                .createTime(LocalDateTime.now())
                .build(),
            User.builder()
                .username("batch3")
                .email("batch3@example.com")
                .realName("批量用户3")
                .age(22)
                .status(1)
                .createTime(LocalDateTime.now())
                .build()
        );
        
        int batchInsertResult = userMapper.batchInsert(batchUsers);
        System.out.println("✅ 批量插入用户，影响行数: " + batchInsertResult);
        
        // 2. 批量查询
        List<Long> userIds = batchUsers.stream()
                .map(User::getId)
                .toList();
        List<User> batchQueryResult = userMapper.selectByIds(userIds);
        System.out.println("✅ 批量查询用户，查询到" + batchQueryResult.size() + "个用户");
        
        // 3. 批量删除
        int batchDeleteResult = userMapper.deleteByIds(userIds);
        System.out.println("✅ 批量删除用户，影响行数: " + batchDeleteResult);
    }
    
    /**
     * 显示MyBatis基础配置说明
     */
    public void showBasicConfiguration() {
        System.out.println("\n--- MyBatis基础配置说明 ---");
        System.out.println("1. Mapper接口 - 定义数据访问方法");
        System.out.println("2. Mapper XML - SQL映射配置文件");
        System.out.println("3. @Mapper注解 - 标识Mapper接口");
        System.out.println("4. @MapperScan注解 - 批量扫描Mapper");
        System.out.println("5. ResultType - 简单结果类型映射");
        System.out.println("6. ResultMap - 复杂结果映射");
        System.out.println("7. ParameterType - 参数类型指定");
        
        System.out.println("\n--- 常用注解 ---");
        System.out.println("@Select - 查询语句");
        System.out.println("@Insert - 插入语句");
        System.out.println("@Update - 更新语句");
        System.out.println("@Delete - 删除语句");
        System.out.println("@Param - 参数命名");
        System.out.println("@Results - 结果映射");
        System.out.println("@One - 一对一关联");
        System.out.println("@Many - 一对多关联");
    }
}