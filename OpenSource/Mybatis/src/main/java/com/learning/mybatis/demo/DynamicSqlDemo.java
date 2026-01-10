package com.learning.mybatis.demo;

import com.learning.mybatis.entity.User;
import com.learning.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * MyBatis动态SQL演示
 * 
 * 演示MyBatis动态SQL的核心特性：
 * - if条件判断
 * - choose/when/otherwise分支
 * - where条件组合
 * - set动态更新
 * - foreach循环
 * - trim自定义裁剪
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicSqlDemo {
    
    private final UserMapper userMapper;
    
    public void demonstrateDynamicSql() {
        System.out.println("开始MyBatis动态SQL演示");
        
        // 1. 条件查询演示
        demonstrateConditionalQuery();
        
        // 2. 动态更新演示
        demonstrateDynamicUpdate();
        
        // 3. 批量操作演示
        demonstrateBatchOperations();
        
        // 4. 复杂条件查询演示
        demonstrateComplexQuery();
        
        // 5. 动态排序和分页演示
        demonstrateDynamicSortAndPaging();
    }
    
    /**
     * 条件查询演示
     */
    private void demonstrateConditionalQuery() {
        System.out.println("\n--- 动态条件查询演示 ---");
        
        // 1. 使用if条件查询
        System.out.println("1. 使用if条件查询:");
        
        // 只传入用户名
        List<User> users1 = userMapper.selectByCondition(
            User.builder().username("zhang").build());
        System.out.println("   根据用户名查询结果: " + users1.size() + "条");
        
        // 传入用户名和状态
        List<User> users2 = userMapper.selectByCondition(
            User.builder()
                .username("zhang")
                .status(1)
                .build());
        System.out.println("   根据用户名+状态查询结果: " + users2.size() + "条");
        
        // 传入年龄范围
        List<User> users3 = userMapper.selectByCondition(
            User.builder()
                .age(25)
                .gender(1)
                .build());
        System.out.println("   根据年龄+性别查询结果: " + users3.size() + "条");
        
        // 2. 使用高级搜索
        System.out.println("\n2. 使用高级搜索:");
        List<User> advancedResults = userMapper.advancedSearch(
            "zhang", // 用户名
            null,    // 邮箱（为空）
            20,      // 最小年龄
            35,      // 最大年龄
            1,       // 性别
            1        // 状态
        );
        System.out.println("   高级搜索结果: " + advancedResults.size() + "条");
    }
    
    /**
     * 动态更新演示
     */
    private void demonstrateDynamicUpdate() {
        System.out.println("\n--- 动态更新演示 ---");
        
        // 先创建测试用户
        User testUser = User.builder()
            .username("dynamic_test")
            .email("dynamic@test.com")
            .realName("动态测试用户")
            .age(30)
            .status(1)
            .createTime(LocalDateTime.now())
            .build();
        
        userMapper.insertSelective(testUser);
        System.out.println("✅ 创建测试用户: " + testUser.getId());
        
        // 1. 选择性更新（只更新非空字段）
        User updateUser1 = User.builder()
            .id(testUser.getId())
            .age(31)
            .updateTime(LocalDateTime.now())
            .build();
        
        int updateResult1 = userMapper.updateSelective(updateUser1);
        System.out.println("✅ 选择性更新结果: " + updateResult1 + "条");
        
        // 2. 更新多个字段
        User updateUser2 = User.builder()
            .id(testUser.getId())
            .email("updated@test.com")
            .phone("13800000000")
            .updateTime(LocalDateTime.now())
            .build();
        
        int updateResult2 = userMapper.updateSelective(updateUser2);
        System.out.println("✅ 多字段更新结果: " + updateResult2 + "条");
        
        // 验证更新结果
        User updatedUser = userMapper.selectById(testUser.getId());
        System.out.println("📋 更新后的用户信息: age=" + updatedUser.getAge() + ", email=" + updatedUser.getEmail() + ", phone=" + updatedUser.getPhone());
    }
    
    /**
     * 批量操作演示
     */
    private void demonstrateBatchOperations() {
        System.out.println("\n--- 批量操作演示 ---");
        
        // 1. 批量插入
        List<User> batchUsers = List.of(
            User.builder()
                .username("batch_user_1")
                .email("batch1@test.com")
                .realName("批量用户1")
                .age(25)
                .status(1)
                .createTime(LocalDateTime.now())
                .build(),
            User.builder()
                .username("batch_user_2")
                .email("batch2@test.com")
                .realName("批量用户2")
                .age(26)
                .status(1)
                .createTime(LocalDateTime.now())
                .build(),
            User.builder()
                .username("batch_user_3")
                .email("batch3@test.com")
                .realName("批量用户3")
                .age(27)
                .status(0)
                .createTime(LocalDateTime.now())
                .build()
        );
        
        int batchInsertResult = userMapper.batchInsert(batchUsers);
        System.out.println("✅ 批量插入结果: " + batchInsertResult + "条");
        
        // 2. 根据ID列表查询
        List<Long> userIds = batchUsers.stream().map(User::getId).toList();
        List<User> batchQueryResult = userMapper.selectByIds(userIds);
        System.out.println("✅ 批量查询结果: " + batchQueryResult.size() + "条");
        
        // 3. 批量更新状态
        List<Long> enableUserIds = batchUsers.stream()
            .limit(2) // 只更新前两个用户
            .map(User::getId)
            .toList();
        
        int batchUpdateResult = userMapper.updateStatusBatch(enableUserIds, 1);
        System.out.println("✅ 批量更新状态结果: " + batchUpdateResult + "条");
        
        // 4. 批量删除（清理测试数据）
        int batchDeleteResult = userMapper.deleteByIds(userIds);
        System.out.println("✅ 批量删除结果: " + batchDeleteResult + "条");
    }
    
    /**
     * 复杂条件查询演示
     */
    private void demonstrateComplexQuery() {
        System.out.println("\n--- 复杂条件查询演示 ---");
        
        // 1. 模糊搜索
        List<User> searchResult1 = userMapper.searchUsers("zhang");
        System.out.println("✅ 关键词搜索 'zhang': " + searchResult1.size() + "条");
        
        List<User> searchResult2 = userMapper.searchUsers("test");
        System.out.println("✅ 关键词搜索 'test': " + searchResult2.size() + "条");
        
        List<User> searchResult3 = userMapper.searchUsers(null);
        System.out.println("✅ 关键词搜索 null (查询所有): " + searchResult3.size() + "条");
        
        // 2. 统计查询
        List<Map<String, Object>> ageDistribution = userMapper.getUserAgeDistribution();
        System.out.println("✅ 用户年龄分布统计:");
        ageDistribution.forEach(map -> 
            System.out.println("   " + map.get("age_group") + " : " + map.get("user_count") + "人"));
        
        // 3. 注册趋势统计
        List<Map<String, Object>> registerTrend = userMapper.getUserRegisterTrend();
        System.out.println("✅ 用户注册趋势统计:");
        registerTrend.forEach(map -> 
            System.out.println("   " + map.get("month") + " : " + map.get("register_count") + "人注册"));
    }
    
    /**
     * 动态排序和分页演示
     */
    private void demonstrateDynamicSortAndPaging() {
        System.out.println("\n--- 动态排序和分页演示 ---");
        
        // 1. 分页查询
        List<User> page1 = userMapper.selectWithPagination(0, 5);
        System.out.println("✅ 第1页查询结果: " + page1.size() + "条");
        
        List<User> page2 = userMapper.selectWithPagination(5, 5);
        System.out.println("✅ 第2页查询结果: " + page2.size() + "条");
        
        // 2. 统计查询
        int totalCount = userMapper.countAll();
        int enabledCount = userMapper.countByStatus(1);
        int disabledCount = userMapper.countByStatus(0);
        
        System.out.println("✅ 用户统计:");
        System.out.println("   总用户数: " + totalCount + "");
        System.out.println("   启用用户: " + enabledCount + "");
        System.out.println("   禁用用户: " + disabledCount + "");
    }
    
    /**
     * 显示动态SQL的核心概念
     */
    public void showDynamicSqlConcepts() {
        System.out.println("\n--- 动态SQL核心概念 ---");
        
        System.out.println("1. if元素 - 条件判断");
        System.out.println("   <if test=\"username != null and username != ''\">...</if>");
        
        System.out.println("\n2. choose/when/otherwise元素 - 多分支选择");
        System.out.println("   <choose>");
        System.out.println("     <when test=\"title != null\">...</when>");
        System.out.println("     <when test=\"author != null\">...</when>");
        System.out.println("     <otherwise>...</otherwise>");
        System.out.println("   </choose>");
        
        System.out.println("\n3. where元素 - 智能WHERE子句");
        System.out.println("   <where>");
        System.out.println("     <if test=\"state != null\">state = #{state}</if>");
        System.out.println("     <if test=\"title != null\">AND title like #{title}</if>");
        System.out.println("   </where>");
        
        System.out.println("\n4. set元素 - 智能SET子句");
        System.out.println("   <set>");
        System.out.println("     <if test=\"username != null\">username=#{username},</if>");
        System.out.println("     <if test=\"password != null\">password=#{password},</if>");
        System.out.println("   </set>");
        
        System.out.println("\n5. foreach元素 - 循环遍历");
        System.out.println("   <foreach item=\"item\" index=\"index\" collection=\"list\"");
        System.out.println("           open=\"(\" separator=\",\" close=\")\">");
        System.out.println("     #{item}");
        System.out.println("   </foreach>");
        
        System.out.println("\n6. trim元素 - 自定义裁剪");
        System.out.println("   <trim prefix=\"WHERE\" prefixOverrides=\"AND |OR \">");
        System.out.println("     <if test=\"state != null\">AND state = #{state}</if>");
        System.out.println("     <if test=\"title != null\">AND title like #{title}</if>");
        System.out.println("   </trim>");
        
        System.out.println("\n--- 动态SQL最佳实践 ---");
        System.out.println("✅ 合理使用if条件，避免过度复杂的嵌套");
        System.out.println("✅ 使用where元素自动处理AND/OR连接符");
        System.out.println("✅ 使用set元素自动处理UPDATE语句的逗号");
        System.out.println("✅ foreach处理集合参数时注意SQL注入防护");
        System.out.println("✅ 动态SQL要保证可读性和维护性");
        System.out.println("✅ 合理使用缓存，避免重复编译相同的SQL");
    }
}