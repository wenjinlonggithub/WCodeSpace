package com.learning.mybatis.demo;

import com.learning.mybatis.entity.User;
import com.learning.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

/**
 * MyBatis缓存机制演示
 * 
 * 演示MyBatis的缓存特性：
 * - 一级缓存（SqlSession级别）
 * - 二级缓存（namespace级别）
 * - 缓存失效机制
 * - 缓存配置和优化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDemo {
    
    private final UserMapper userMapper;
    private final SqlSessionFactory sqlSessionFactory;
    
    public void demonstrateCache() {
        System.out.println("开始MyBatis缓存机制演示");
        
        // 1. 一级缓存演示
        demonstrateFirstLevelCache();
        
        // 2. 二级缓存演示
        demonstrateSecondLevelCache();
        
        // 3. 缓存失效演示
        demonstrateCacheInvalidation();
        
        // 4. 缓存配置说明
        demonstrateCacheConfiguration();
    }
    
    /**
     * 一级缓存演示
     */
    private void demonstrateFirstLevelCache() {
        System.out.println("\n--- 一级缓存（SqlSession级别）演示 ---");
        
        // 使用同一个SqlSession进行多次查询
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            
            System.out.println("🔍 同一个SqlSession中的查询:");
            
            // 第一次查询
            long startTime1 = System.currentTimeMillis();
            User user1 = mapper.selectById(1L);
            long endTime1 = System.currentTimeMillis();
            System.out.println("✅ 第一次查询用户(ID=1): 耗时" + (endTime1 - startTime1) + "ms, 用户: " + 
                    (user1 != null ? user1.getUsername() : "null"));
            
            // 第二次查询相同数据（应该从一级缓存获取）
            long startTime2 = System.currentTimeMillis();
            User user2 = mapper.selectById(1L);
            long endTime2 = System.currentTimeMillis();
            System.out.println("✅ 第二次查询用户(ID=1): 耗时" + (endTime2 - startTime2) + "ms, 用户: " + 
                    (user2 != null ? user2.getUsername() : "null"));
            
            // 验证是否是同一个对象实例
            System.out.println("🔍 对象实例比较: user1 == user2 ? " + (user1 == user2));
            System.out.println("   说明: 一级缓存返回的是同一个对象实例");
            
            // 查询不同的数据
            User user3 = mapper.selectById(2L);
            System.out.println("✅ 查询不同用户(ID=2): " + (user3 != null ? user3.getUsername() : "null"));
            
        } catch (Exception e) {
            //System.out.println("一级缓存演示出错", e);
        }
        
        System.out.println("\n🔍 一级缓存特点:");
        System.out.println("   - 作用域: 单个SqlSession");
        System.out.println("   - 默认开启，无法关闭");
        System.out.println("   - 存储: HashMap<CacheKey, Object>");
        System.out.println("   - 生命周期: SqlSession关闭后失效");
        System.out.println("   - 失效条件: 执行增删改操作、手动清空、SqlSession关闭");
    }
    
    /**
     * 二级缓存演示
     */
    private void demonstrateSecondLevelCache() {
        System.out.println("\n--- 二级缓存（namespace级别）演示 ---");
        
        System.out.println("🔍 不同SqlSession之间的查询:");
        
        // 第一个SqlSession
        User user1 = null;
        try (SqlSession sqlSession1 = sqlSessionFactory.openSession()) {
            UserMapper mapper1 = sqlSession1.getMapper(UserMapper.class);
            
            long startTime1 = System.currentTimeMillis();
            user1 = mapper1.selectById(1L);
            long endTime1 = System.currentTimeMillis();
            
            System.out.println("✅ SqlSession1查询用户: 耗时" + (endTime1 - startTime1) + "ms, 用户: " + 
                    (user1 != null ? user1.getUsername() : "null"));
            
            // 提交事务，数据才会进入二级缓存
            sqlSession1.commit();
        }
        
        // 第二个SqlSession（如果二级缓存开启，应该从缓存获取）
        try (SqlSession sqlSession2 = sqlSessionFactory.openSession()) {
            UserMapper mapper2 = sqlSession2.getMapper(UserMapper.class);
            
            long startTime2 = System.currentTimeMillis();
            User user2 = mapper2.selectById(1L);
            long endTime2 = System.currentTimeMillis();
            
            System.out.println("✅ SqlSession2查询用户: 耗时" + (endTime2 - startTime2) + "ms, 用户: " + 
                    (user2 != null ? user2.getUsername() : "null"));
            
            // 注意：二级缓存返回的是不同的对象实例
            if (user1 != null && user2 != null) {
                System.out.println("🔍 对象实例比较: user1 == user2 ? " + (user1 == user2));
                System.out.println("🔍 对象内容比较: user1.equals(user2) ? " + (user1.equals(user2)));
                System.out.println("   说明: 二级缓存返回的是反序列化后的新对象");
            }
        }
        
        System.out.println("\n🔍 二级缓存特点:");
        System.out.println("   - 作用域: namespace（通常是一个Mapper）");
        System.out.println("   - 需要显式配置开启");
        System.out.println("   - 存储: 可配置不同的缓存实现");
        System.out.println("   - 生命周期: 应用程序生命周期");
        System.out.println("   - 事务提交后数据才进入二级缓存");
        System.out.println("   - 返回反序列化后的新对象实例");
    }
    
    /**
     * 缓存失效演示
     */
    private void demonstrateCacheInvalidation() {
        System.out.println("\n--- 缓存失效机制演示 ---");
        
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            
            // 1. 查询数据（缓存）
            User user1 = mapper.selectById(1L);
            System.out.println("✅ 第一次查询: " + (user1 != null ? user1.getUsername() : "null"));
            
            // 2. 再次查询（应该从缓存获取）
            User user2 = mapper.selectById(1L);
            System.out.println("✅ 第二次查询: " + (user2 != null ? user2.getUsername() : "null") + " (从缓存获取)");
            System.out.println("🔍 对象相同: " + (user1 == user2));
            
            // 3. 执行更新操作（会清空一级缓存）
            if (user1 != null) {
                User updateUser = User.builder()
                    .id(user1.getId())
                    .age(user1.getAge() + 1)
                    .build();
                
                int updateResult = mapper.updateSelective(updateUser);
                System.out.println("✅ 执行更新操作: 影响" + updateResult + "行");
                System.out.println("   说明: 更新操作会清空当前namespace的所有缓存");
            }
            
            // 4. 更新后再次查询（需要重新从数据库获取）
            User user3 = mapper.selectById(1L);
            System.out.println("✅ 更新后查询: " + (user3 != null ? user3.getUsername() : "null"));
            System.out.println("🔍 对象相同: " + (user1 == user3) + " (缓存已失效，重新查询)");
            
        } catch (Exception e) {
            //System.out.println("缓存失效演示出错", e);
        }
        
        System.out.println("\n🔍 缓存失效场景:");
        System.out.println("   - 执行INSERT/UPDATE/DELETE操作");
        System.out.println("   - 手动调用clearCache()方法");
        System.out.println("   - SqlSession关闭（一级缓存）");
        System.out.println("   - 缓存配置的过期时间到达");
        System.out.println("   - 缓存空间不足时LRU淘汰");
    }
    
    /**
     * 缓存配置演示
     */
    private void demonstrateCacheConfiguration() {
        System.out.println("\n--- 缓存配置说明 ---");
        
        System.out.println("🔧 一级缓存配置:");
        System.out.println("   - 无需配置，默认开启");
        System.out.println("   - 可通过localCacheScope设置作用域");
        System.out.println("   - SESSION: SqlSession级别（默认）");
        System.out.println("   - STATEMENT: 语句级别（等于关闭）");
        
        System.out.println("\n🔧 二级缓存配置:");
        System.out.println("   1. 全局开启:");
        System.out.println("      <setting name=\"cacheEnabled\" value=\"true\"/>");
        System.out.println("   ");
        System.out.println("   2. Mapper中启用:");
        System.out.println("      <cache />");
        System.out.println("   ");
        System.out.println("   3. 自定义配置:");
        System.out.println("      <cache eviction=\"LRU\"");
        System.out.println("             flushInterval=\"60000\"");
        System.out.println("             size=\"1024\"");
        System.out.println("             readOnly=\"false\"/>");
        
        System.out.println("\n🔧 缓存属性说明:");
        System.out.println("   - eviction: 淘汰策略(LRU/FIFO/SOFT/WEAK)");
        System.out.println("   - flushInterval: 刷新间隔(毫秒)");
        System.out.println("   - size: 缓存对象数量");
        System.out.println("   - readOnly: 只读标志");
        System.out.println("   - type: 自定义缓存实现类");
        
        System.out.println("\n🎯 缓存使用建议:");
        System.out.println("   ✅ 一级缓存适合事务内的重复查询");
        System.out.println("   ✅ 二级缓存适合读多写少的场景");
        System.out.println("   ✅ 注意缓存与事务的配合使用");
        System.out.println("   ✅ 避免在高并发写操作中使用二级缓存");
        System.out.println("   ✅ 合理设置缓存大小和过期时间");
        System.out.println("   ✅ 考虑使用外部缓存(Redis)替代二级缓存");
        
        System.out.println("\n⚠️ 缓存注意事项:");
        System.out.println("   - 缓存对象需要实现Serializable接口");
        System.out.println("   - 注意缓存雪崩和缓存穿透问题");
        System.out.println("   - 分布式环境下二级缓存数据一致性");
        System.out.println("   - 大对象缓存可能影响内存使用");
        System.out.println("   - 缓存命中率监控和调优");
    }
    
    /**
     * 显示缓存监控信息
     */
    public void showCacheStatistics() {
        System.out.println("\n--- 缓存监控指标 ---");
        System.out.println("📊 关键指标:");
        System.out.println("   - 缓存命中率: 命中次数 / 总查询次数");
        System.out.println("   - 缓存大小: 当前缓存中的对象数量");
        System.out.println("   - 缓存内存使用: 缓存占用的内存大小");
        System.out.println("   - 缓存失效次数: 缓存被清空的次数");
        System.out.println("   - 平均查询时间: 包含缓存命中和未命中");
        
        System.out.println("\n🎯 优化策略:");
        System.out.println("   - 命中率过低: 检查查询模式和缓存配置");
        System.out.println("   - 内存占用过高: 调整缓存大小或淘汰策略");
        System.out.println("   - 频繁失效: 优化更新操作的粒度");
        System.out.println("   - 查询变慢: 检查是否存在缓存雪崩");
    }
}