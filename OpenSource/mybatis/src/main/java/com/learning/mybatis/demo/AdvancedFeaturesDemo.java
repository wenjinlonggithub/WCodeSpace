package com.learning.mybatis.demo;

import com.learning.mybatis.entity.User;
import com.learning.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * MyBatis高级特性演示
 * 
 * 演示MyBatis的高级特性：
 * - 插件机制（拦截器）
 * - 自定义类型处理器
 * - 自定义对象工厂
 * - SQL构建器
 * - 批量执行器
 * - 多数据源配置
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedFeaturesDemo {
    
    private final UserMapper userMapper;
    
    public void demonstrateAdvancedFeatures() {
        System.out.println("开始MyBatis高级特性演示");
        
        // 1. 插件机制演示
        demonstratePluginMechanism();
        
        // 2. 类型处理器演示
        demonstrateTypeHandlers();
        
        // 3. SQL构建器演示
        demonstrateSqlBuilder();
        
        // 4. 批量操作优化演示
        demonstrateBatchOperations();
        
        // 5. 性能监控和调优演示
        demonstratePerformanceMonitoring();
    }
    
    /**
     * 插件机制演示
     */
    private void demonstratePluginMechanism() {
        System.out.println("\n--- MyBatis插件机制演示 ---");
        
        System.out.println("🔌 MyBatis插件拦截点:");
        System.out.println("   1. Executor: 执行器拦截");
        System.out.println("      - query(): 查询操作拦截");
        System.out.println("      - update(): 更新操作拦截");
        System.out.println("      - commit(): 事务提交拦截");
        System.out.println("      - rollback(): 事务回滚拦截");
        
        System.out.println("\n   2. ParameterHandler: 参数处理拦截");
        System.out.println("      - getParameterObject(): 参数获取拦截");
        System.out.println("      - setParameters(): 参数设置拦截");
        
        System.out.println("\n   3. ResultSetHandler: 结果集处理拦截");
        System.out.println("      - handleResultSets(): 结果集处理拦截");
        System.out.println("      - handleOutputParameters(): 输出参数处理拦截");
        
        System.out.println("\n   4. StatementHandler: 语句处理拦截");
        System.out.println("      - prepare(): 语句准备拦截");
        System.out.println("      - parameterize(): 参数化拦截");
        System.out.println("      - batch(): 批量操作拦截");
        System.out.println("      - update(): 更新操作拦截");
        System.out.println("      - query(): 查询操作拦截");
        
        // 演示插件的实际应用
        System.out.println("\n🎯 插件应用场景:");
        System.out.println("   ✅ SQL性能监控和慢查询记录");
        System.out.println("   ✅ 数据权限控制和行级过滤");
        System.out.println("   ✅ 敏感数据自动加解密");
        System.out.println("   ✅ 分页插件实现");
        System.out.println("   ✅ 多租户数据隔离");
        System.out.println("   ✅ 审计日志记录");
        System.out.println("   ✅ 缓存增强和优化");
        
        // 执行一个查询来触发插件（如果有配置的话）
        User user = userMapper.selectById(1L);
        System.out.println("✅ 执行查询操作: " + (user != null ? user.getUsername() : "null"));
        System.out.println("   (如果配置了插件，会在日志中看到拦截信息)");
    }
    
    /**
     * 类型处理器演示
     */
    private void demonstrateTypeHandlers() {
        System.out.println("\n--- 自定义类型处理器演示 ---");
        
        System.out.println("🔄 内置类型处理器:");
        System.out.println("   - StringTypeHandler: String <-> VARCHAR");
        System.out.println("   - IntegerTypeHandler: Integer <-> INTEGER");
        System.out.println("   - DateTypeHandler: Date <-> TIMESTAMP");
        System.out.println("   - EnumTypeHandler: Enum <-> VARCHAR");
        System.out.println("   - BlobTypeHandler: byte[] <-> BLOB");
        
        System.out.println("\n🔄 自定义类型处理器场景:");
        System.out.println("   1. JSON字段处理:");
        System.out.println("      - 数据库存储: JSON字符串");
        System.out.println("      - Java对象: POJO/Map/List");
        System.out.println("      - 自动序列化和反序列化");
        
        System.out.println("\n   2. 加密字段处理:");
        System.out.println("      - 数据库存储: 加密字符串");
        System.out.println("      - Java对象: 明文字符串");
        System.out.println("      - 自动加密和解密");
        
        System.out.println("\n   3. 枚举增强处理:");
        System.out.println("      - 数据库存储: 枚举代码");
        System.out.println("      - Java对象: 枚举实例");
        System.out.println("      - 支持代码和描述映射");
        
        System.out.println("\n   4. 复杂对象处理:");
        System.out.println("      - 数据库存储: 序列化字符串");
        System.out.println("      - Java对象: 复杂POJO");
        System.out.println("      - 自定义序列化策略");
        
        // 演示枚举类型处理
        System.out.println("\n✅ 枚举类型处理演示:");
        User user = userMapper.selectById(1L);
        if (user != null) {
            System.out.println("   用户性别: " + user.getGender() + " -> " + user.getGenderDesc());
            System.out.println("   用户状态: " + user.getStatus() + " -> " + user.getStatusDesc());
            System.out.println("   说明: 数据库存储数字，Java中使用枚举描述");
        }
    }
    
    /**
     * SQL构建器演示
     */
    private void demonstrateSqlBuilder() {
        System.out.println("\n--- SQL构建器演示 ---");
        
        System.out.println("🔨 MyBatis SQL构建方式:");
        System.out.println("   1. XML配置方式:");
        System.out.println("      - 静态SQL配置");
        System.out.println("      - 动态SQL标签");
        System.out.println("      - 可读性好，维护方便");
        
        System.out.println("\n   2. 注解方式:");
        System.out.println("      - @Select/@Insert/@Update/@Delete");
        System.out.println("      - @SelectProvider/@InsertProvider等");
        System.out.println("      - 简单SQL适用");
        
        System.out.println("\n   3. SQL构建器方式:");
        System.out.println("      - 编程式SQL构建");
        System.out.println("      - 类型安全");
        System.out.println("      - 动态性强");
        
        System.out.println("\n🎯 SQL构建器优势:");
        System.out.println("   ✅ 编译时类型检查");
        System.out.println("   ✅ IDE智能提示支持");
        System.out.println("   ✅ 重构友好");
        System.out.println("   ✅ 复杂动态SQL构建");
        System.out.println("   ✅ SQL注入防护");
        
        // 演示动态SQL的强大功能
        System.out.println("\n✅ 动态SQL应用演示:");
        
        // 复杂条件查询
        User condition = User.builder()
            .username("test")
            .status(1)
            .build();
        
        List<User> users = userMapper.selectByCondition(condition);
        System.out.println("   条件查询结果: " + users.size() + "条记录");
        
        // 高级搜索
        List<User> searchResults = userMapper.advancedSearch(
            "zhang", null, 20, 40, 1, 1);
        System.out.println("   高级搜索结果: " + searchResults.size() + "条记录");
    }
    
    /**
     * 批量操作优化演示
     */
    private void demonstrateBatchOperations() {
        System.out.println("\n--- 批量操作优化演示 ---");
        
        System.out.println("⚡ 批量操作优化策略:");
        System.out.println("   1. 批量插入优化:");
        System.out.println("      - 使用VALUES(...),(...),(...) 语法");
        System.out.println("      - 减少网络往返次数");
        System.out.println("      - 提高插入性能");
        
        System.out.println("\n   2. 批量更新优化:");
        System.out.println("      - 使用CASE WHEN语法");
        System.out.println("      - 或者使用ON DUPLICATE KEY UPDATE");
        System.out.println("      - 避免N次单独更新");
        
        System.out.println("\n   3. 批量删除优化:");
        System.out.println("      - 使用IN (id1, id2, id3) 语法");
        System.out.println("      - 或者使用临时表关联删除");
        System.out.println("      - 注意删除数量限制");
        
        // 演示批量操作
        System.out.println("\n✅ 批量操作性能测试:");
        
        // 准备测试数据
        List<User> testUsers = List.of(
            User.builder()
                .username("batch_test_1")
                .email("batch1@test.com")
                .realName("批量测试1")
                .age(25)
                .status(1)
                .createTime(LocalDateTime.now())
                .build(),
            User.builder()
                .username("batch_test_2")
                .email("batch2@test.com")
                .realName("批量测试2")
                .age(26)
                .status(1)
                .createTime(LocalDateTime.now())
                .build(),
            User.builder()
                .username("batch_test_3")
                .email("batch3@test.com")
                .realName("批量测试3")
                .age(27)
                .status(1)
                .createTime(LocalDateTime.now())
                .build()
        );
        
        // 批量插入性能测试
        long startTime = System.currentTimeMillis();
        int batchResult = userMapper.batchInsert(testUsers);
        long endTime = System.currentTimeMillis();
        
        System.out.println("   批量插入" + batchResult + "条记录，耗时: " + (endTime - startTime) + "ms");
        
        // 清理测试数据
        List<Long> testIds = testUsers.stream().map(User::getId).toList();
        userMapper.deleteByIds(testIds);
        System.out.println("   清理测试数据完成");
    }
    
    /**
     * 性能监控和调优演示
     */
    private void demonstratePerformanceMonitoring() {
        System.out.println("\n--- 性能监控和调优演示 ---");
        
        System.out.println("📊 性能监控指标:");
        System.out.println("   1. SQL执行时间:");
        System.out.println("      - 慢查询识别");
        System.out.println("      - 执行时间分布");
        System.out.println("      - 性能趋势分析");
        
        System.out.println("\n   2. 缓存命中率:");
        System.out.println("      - 一级缓存命中率");
        System.out.println("      - 二级缓存命中率");
        System.out.println("      - 缓存失效频率");
        
        System.out.println("\n   3. 连接池状态:");
        System.out.println("      - 活跃连接数");
        System.out.println("      - 等待连接数");
        System.out.println("      - 连接获取时间");
        
        System.out.println("\n   4. 内存使用:");
        System.out.println("      - 结果集内存占用");
        System.out.println("      - 缓存内存占用");
        System.out.println("      - 内存泄漏检测");
        
        System.out.println("\n🎯 性能优化建议:");
        System.out.println("   ✅ 合理设计索引，优化查询性能");
        System.out.println("   ✅ 使用分页查询，避免大结果集");
        System.out.println("   ✅ 启用查询缓存，减少数据库压力");
        System.out.println("   ✅ 优化SQL语句，避免全表扫描");
        System.out.println("   ✅ 合理配置连接池参数");
        System.out.println("   ✅ 监控慢查询，及时优化");
        System.out.println("   ✅ 使用批量操作提高吞吐量");
        System.out.println("   ✅ 考虑读写分离和分库分表");
        
        // 演示性能监控
        System.out.println("\n✅ 执行性能测试:");
        
        long totalStartTime = System.currentTimeMillis();
        
        // 执行多个查询操作
        for (int i = 1; i <= 5; i++) {
            long queryStart = System.currentTimeMillis();
            User user = userMapper.selectById((long) i);
            long queryEnd = System.currentTimeMillis();
            
            System.out.println("   查询用户ID=" + i + ": 耗时" + (queryEnd - queryStart) + "ms, 结果: " + 
                    (user != null ? user.getUsername() : "null"));
        }
        
        long totalEndTime = System.currentTimeMillis();
        System.out.println("   总耗时: " + (totalEndTime - totalStartTime) + "ms");
    }
    
    /**
     * 显示高级特性配置示例
     */
    public void showAdvancedConfiguration() {
        System.out.println("\n--- 高级特性配置示例 ---");
        
        System.out.println("1. 插件配置:");
        System.out.println("   <plugins>");
        System.out.println("     <plugin interceptor=\"com.example.MyInterceptor\">");
        System.out.println("       <property name=\"someProperty\" value=\"100\"/>");
        System.out.println("     </plugin>");
        System.out.println("   </plugins>");
        
        System.out.println("\n2. 类型处理器配置:");
        System.out.println("   <typeHandlers>");
        System.out.println("     <typeHandler handler=\"com.example.JsonTypeHandler\"");
        System.out.println("                  javaType=\"com.example.JsonObject\"");
        System.out.println("                  jdbcType=\"VARCHAR\"/>");
        System.out.println("   </typeHandlers>");
        
        System.out.println("\n3. 对象工厂配置:");
        System.out.println("   <objectFactory type=\"com.example.MyObjectFactory\">");
        System.out.println("     <property name=\"someProperty\" value=\"100\"/>");
        System.out.println("   </objectFactory>");
        
        System.out.println("\n4. 环境配置:");
        System.out.println("   <environments default=\"development\">");
        System.out.println("     <environment id=\"development\">");
        System.out.println("       <transactionManager type=\"JDBC\"/>");
        System.out.println("       <dataSource type=\"POOLED\">");
        System.out.println("         <property name=\"driver\" value=\"com.mysql.cj.jdbc.Driver\"/>");
        System.out.println("         <property name=\"url\" value=\"jdbc:mysql://localhost:3306/test\"/>");
        System.out.println("       </dataSource>");
        System.out.println("     </environment>");
        System.out.println("   </environments>");
    }
    
    /**
     * 示例插件实现
     */
    @Intercepts({
        @Signature(type = Executor.class, method = "query", 
                  args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
    })
    public static class PerformanceInterceptor implements Interceptor {
        
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            long startTime = System.currentTimeMillis();
            
            try {
                Object result = invocation.proceed();
                long endTime = System.currentTimeMillis();
                
                MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
                System.out.println("🔍 SQL执行监控: " + ms.getId() + " 耗时: " + (endTime - startTime) + "ms");
                
                return result;
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                System.out.println("❌ SQL执行异常: 耗时: " + (endTime - startTime) + "ms");
                throw e;
            }
        }
        
        @Override
        public Object plugin(Object target) {
            return Plugin.wrap(target, this);
        }
        
        @Override
        public void setProperties(Properties properties) {
            // 设置插件属性
        }
    }
}