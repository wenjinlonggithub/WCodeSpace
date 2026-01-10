package com.learning.mybatis.demo;

import com.learning.mybatis.entity.User;
import com.learning.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MyBatis结果映射演示
 * 
 * 演示MyBatis的结果映射特性：
 * - 基本结果映射
 * - 复杂对象映射
 * - 一对一关联映射
 * - 一对多关联映射
 * - 多对多关联映射
 * - 嵌套查询和嵌套结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultMapDemo {
    
    private final UserMapper userMapper;
    
    public void demonstrateResultMap() {
        System.out.println("开始MyBatis结果映射演示");
        
        // 1. 基础结果映射演示
        demonstrateBasicResultMap();
        
        // 2. 一对一关联映射演示
        demonstrateOneToOneMapping();
        
        // 3. 一对多关联映射演示
        demonstrateOneToManyMapping();
        
        // 4. 复杂嵌套映射演示
        demonstrateComplexNestedMapping();
        
        // 5. 自定义结果处理演示
        demonstrateCustomResultHandling();
    }
    
    /**
     * 基础结果映射演示
     */
    private void demonstrateBasicResultMap() {
        System.out.println("\n--- 基础结果映射演示 ---");
        
        // 1. 简单的字段映射
        User user = userMapper.selectById(1L);
        if (user != null) {
            System.out.println("✅ 基础映射结果:");
            System.out.println("   用户ID: " + user.getId());
            System.out.println("   用户名: " + user.getUsername());
            System.out.println("   真实姓名: " + user.getRealName());
            System.out.println("   邮箱: " + user.getEmail());
            System.out.println("   年龄: " + user.getAge());
            System.out.println("   性别: " + user.getGender() + " (" + user.getGenderDesc() + ")");
            System.out.println("   状态: " + user.getStatus() + " (" + user.getStatusDesc() + ")");
            System.out.println("   创建时间: " + user.getCreateTime());
            System.out.println("   更新时间: " + user.getUpdateTime());
        } else {
            System.out.println("⚠️ 未找到ID为1的用户");
        }
        
        // 2. 演示数据库字段名与Java属性名的映射
        System.out.println("\n✅ 字段映射说明:");
        System.out.println("   数据库字段 real_name -> Java属性 realName");
        System.out.println("   数据库字段 create_time -> Java属性 createTime");
        System.out.println("   数据库字段 update_time -> Java属性 updateTime");
        System.out.println("   MyBatis自动处理驼峰命名转换");
    }
    
    /**
     * 一对一关联映射演示
     */
    private void demonstrateOneToOneMapping() {
        System.out.println("\n--- 一对一关联映射演示 ---");
        
        // 查询用户及其详细信息
        User userWithProfile = userMapper.selectUserWithProfile(1L);
        if (userWithProfile != null) {
            System.out.println("✅ 一对一关联查询结果:");
            System.out.println("   用户: " + userWithProfile.getUsername());
            
            if (userWithProfile.getProfile() != null) {
                System.out.println("   详细信息已加载:");
                // 这里需要先实现UserProfile实体和相关映射
                System.out.println("   (UserProfile实体需要进一步实现)");
            } else {
                System.out.println("   无详细信息");
            }
        }
        
        System.out.println("\n🔍 一对一映射说明:");
        System.out.println("   - 使用<association>元素配置一对一关联");
        System.out.println("   - 可以使用嵌套查询或嵌套结果两种方式");
        System.out.println("   - 嵌套查询: 分别执行主查询和关联查询");
        System.out.println("   - 嵌套结果: 使用JOIN一次性查询所有数据");
    }
    
    /**
     * 一对多关联映射演示
     */
    private void demonstrateOneToManyMapping() {
        System.out.println("\n--- 一对多关联映射演示 ---");
        
        // 查询用户及其角色信息
        User userWithRoles = userMapper.selectUserWithRoles(1L);
        if (userWithRoles != null) {
            System.out.println("✅ 一对多关联查询结果:");
            System.out.println("   用户: " + userWithRoles.getUsername());
            
            if (userWithRoles.getRoles() != null && !userWithRoles.getRoles().isEmpty()) {
                System.out.println("   用户角色("+ userWithRoles.getRoles().size());
                userWithRoles.getRoles().forEach(role -> {
                });
            } else {
                System.out.println("   无分配角色");
            }
        }
        
        // 查询用户及其订单信息
        User userWithOrders = userMapper.selectUserWithOrders(1L);
        if (userWithOrders != null) {
            System.out.println("✅ 用户订单关联查询:");
            System.out.println("   用户: " + userWithOrders.getUsername());
            
            if (userWithOrders.getOrders() != null && !userWithOrders.getOrders().isEmpty()) {
                userWithOrders.getOrders().forEach(order -> {
                    // 这里需要先实现Order实体
                    System.out.println("     - 订单: " + order + "");
                });
            } else {
                System.out.println("   无订单记录");
            }
        }
        
        System.out.println("\n🔍 一对多映射说明:");
        System.out.println("   - 使用<collection>元素配置一对多关联");
        System.out.println("   - ofType属性指定集合中元素的类型");
        System.out.println("   - 注意N+1查询问题，合理选择懒加载策略");
        System.out.println("   - 大数据量时考虑分页和性能优化");
    }
    
    /**
     * 复杂嵌套映射演示
     */
    private void demonstrateComplexNestedMapping() {
        System.out.println("\n--- 复杂嵌套映射演示 ---");
        
        System.out.println("🔍 复杂映射场景:");
        System.out.println("   1. 多层嵌套关联");
        System.out.println("      用户 -> 订单 -> 订单项 -> 商品");
        System.out.println("      User -> Order -> OrderItem -> Product");
        
        System.out.println("\n   2. 多对多关联");
        System.out.println("      用户 <-> 角色 <-> 权限");
        System.out.println("      User <-> Role <-> Permission");
        
        System.out.println("\n   3. 自引用关联");
        System.out.println("      部门 -> 父部门 -> 子部门列表");
        System.out.println("      Department -> parentDept -> childDepts");
        
        // 演示复杂查询的性能考虑
        System.out.println("\n⚡ 性能优化建议:");
        System.out.println("   - 使用懒加载避免不必要的关联查询");
        System.out.println("   - 合理设计查询粒度，避免过度关联");
        System.out.println("   - 使用二级缓存缓存常用的关联数据");
        System.out.println("   - 考虑使用DTO减少数据传输量");
        System.out.println("   - 复杂查询可以拆分为多个简单查询");
    }
    
    /**
     * 自定义结果处理演示
     */
    private void demonstrateCustomResultHandling() {
        System.out.println("\n--- 自定义结果处理演示 ---");
        
        // 1. 类型转换演示
        System.out.println("✅ 内置类型转换:");
        System.out.println("   - String <-> Integer/Long/Double");
        System.out.println("   - String <-> Date/LocalDateTime");
        System.out.println("   - String <-> Enum");
        System.out.println("   - BLOB/CLOB <-> byte[]/String");
        
        // 2. 自定义类型处理器
        System.out.println("\n✅ 自定义类型处理器场景:");
        System.out.println("   - JSON字符串 <-> Java对象");
        System.out.println("   - 加密字段的自动加解密");
        System.out.println("   - 自定义枚举类型处理");
        System.out.println("   - 复杂数据类型的序列化");
        
        // 3. 结果映射的最佳实践
        System.out.println("\n🎯 结果映射最佳实践:");
        System.out.println("   ✅ 合理设计ResultMap，复用通用映射");
        System.out.println("   ✅ 使用继承和组合减少重复配置");
        System.out.println("   ✅ 注意字段名命名规范，利用自动映射");
        System.out.println("   ✅ 对于复杂关联，考虑DTO模式");
        System.out.println("   ✅ 及时关注查询性能，避免N+1问题");
        System.out.println("   ✅ 合理使用懒加载和缓存机制");
    }
    
    /**
     * 显示ResultMap配置示例
     */
    public void showResultMapConfiguration() {
        System.out.println("\n--- ResultMap配置示例 ---");
        
        System.out.println("1. 基础ResultMap:");
        System.out.println("   <resultMap id=\"userResultMap\" type=\"User\">");
        System.out.println("     <id property=\"id\" column=\"id\"/>");
        System.out.println("     <result property=\"username\" column=\"username\"/>");
        System.out.println("     <result property=\"realName\" column=\"real_name\"/>");
        System.out.println("     <result property=\"createTime\" column=\"create_time\"/>");
        System.out.println("   </resultMap>");
        
        System.out.println("\n2. 一对一关联ResultMap:");
        System.out.println("   <resultMap id=\"userWithProfileMap\" type=\"User\">");
        System.out.println("     <id property=\"id\" column=\"id\"/>");
        System.out.println("     <result property=\"username\" column=\"username\"/>");
        System.out.println("     <association property=\"profile\" javaType=\"UserProfile\">");
        System.out.println("       <id property=\"id\" column=\"profile_id\"/>");
        System.out.println("       <result property=\"avatar\" column=\"avatar\"/>");
        System.out.println("       <result property=\"bio\" column=\"bio\"/>");
        System.out.println("     </association>");
        System.out.println("   </resultMap>");
        
        System.out.println("\n3. 一对多关联ResultMap:");
        System.out.println("   <resultMap id=\"userWithRolesMap\" type=\"User\">");
        System.out.println("     <id property=\"id\" column=\"user_id\"/>");
        System.out.println("     <result property=\"username\" column=\"username\"/>");
        System.out.println("     <collection property=\"roles\" ofType=\"Role\">");
        System.out.println("       <id property=\"id\" column=\"role_id\"/>");
        System.out.println("       <result property=\"roleName\" column=\"role_name\"/>");
        System.out.println("       <result property=\"roleCode\" column=\"role_code\"/>");
        System.out.println("     </collection>");
        System.out.println("   </resultMap>");
        
        System.out.println("\n4. 嵌套查询方式:");
        System.out.println("   <association property=\"profile\" column=\"id\"");
        System.out.println("               select=\"selectUserProfile\"/>");
        System.out.println("   ");
        System.out.println("   <collection property=\"roles\" column=\"id\"");
        System.out.println("              select=\"selectUserRoles\"/>");
    }
}