# MyBatis 面试题大全

## 📋 目录
- [基础概念](#基础概念)
- [核心原理](#核心原理)
- [配置相关](#配置相关)
- [SQL映射](#sql映射)
- [缓存机制](#缓存机制)
- [插件机制](#插件机制)
- [性能优化](#性能优化)
- [实际应用](#实际应用)
- [高级特性](#高级特性)
- [对比分析](#对比分析)

---

## 基础概念

### 1. 什么是MyBatis？它解决了什么问题？

**答案：**
MyBatis是一个优秀的持久层框架，它支持自定义SQL、存储过程以及高级映射。

**解决的问题：**
- **JDBC繁琐性**：简化了JDBC的使用，减少了样板代码
- **SQL与Java代码分离**：将SQL语句从Java代码中分离出来
- **结果集映射**：自动将查询结果映射为Java对象
- **参数处理**：简化了参数设置和类型转换
- **连接管理**：提供了连接池管理和事务处理

### 2. MyBatis的核心组件有哪些？

**答案：**
- **SqlSessionFactory**：会话工厂，用于创建SqlSession
- **SqlSession**：执行SQL的会话，包含了执行SQL所需的所有方法
- **Mapper**：映射器，包含SQL语句和映射规则
- **Configuration**：配置信息，包含MyBatis的所有配置
- **Executor**：执行器，负责SQL的执行和缓存维护
- **StatementHandler**：语句处理器，负责SQL语句的预编译和执行
- **ParameterHandler**：参数处理器，负责参数的设置
- **ResultSetHandler**：结果集处理器，负责结果集的映射

### 3. MyBatis的工作流程是什么？

**答案：**
1. **加载配置**：读取MyBatis配置文件和映射文件
2. **创建SqlSessionFactory**：根据配置创建会话工厂
3. **创建SqlSession**：从工厂获取会话对象
4. **获取Mapper**：通过SqlSession获取Mapper代理对象
5. **执行SQL**：调用Mapper方法执行SQL
6. **处理结果**：将查询结果映射为Java对象
7. **关闭会话**：释放资源

---

## 核心原理

### 4. MyBatis是如何实现SQL与Java代码分离的？

**答案：**
- **XML映射文件**：将SQL语句写在XML文件中
- **注解方式**：使用@Select、@Insert等注解
- **动态代理**：为Mapper接口创建代理对象
- **反射机制**：通过反射调用对应的SQL语句

### 5. 解释MyBatis的动态代理机制

**答案：**
```java
// MyBatis使用JDK动态代理为Mapper接口创建代理对象
public class MapperProxy<T> implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1. 获取方法对应的MapperMethod
        // 2. 根据方法类型执行相应的SQL操作
        // 3. 返回执行结果
    }
}
```

**关键点：**
- 使用JDK动态代理技术
- 代理对象拦截方法调用
- 根据方法签名找到对应的SQL语句
- 执行SQL并返回结果

### 6. MyBatis的SQL执行流程是什么？

**答案：**
1. **解析SQL**：解析XML中的SQL语句，处理动态SQL
2. **创建Statement**：根据SQL类型创建PreparedStatement
3. **设置参数**：通过ParameterHandler设置SQL参数
4. **执行SQL**：通过StatementHandler执行SQL语句
5. **处理结果**：通过ResultSetHandler处理结果集
6. **映射对象**：将结果映射为Java对象

---

## 配置相关

### 7. MyBatis的配置文件结构是什么？

**答案：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 属性配置 -->
    <properties resource="database.properties"/>
    
    <!-- 设置 -->
    <settings>
        <setting name="cacheEnabled" value="true"/>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>
    
    <!-- 类型别名 -->
    <typeAliases>
        <typeAlias type="com.example.User" alias="User"/>
    </typeAliases>
    
    <!-- 类型处理器 -->
    <typeHandlers>
        <typeHandler handler="com.example.MyTypeHandler"/>
    </typeHandlers>
    
    <!-- 插件 -->
    <plugins>
        <plugin interceptor="com.example.MyInterceptor"/>
    </plugins>
    
    <!-- 环境配置 -->
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
    </environments>
    
    <!-- 映射器 -->
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>
</configuration>
```

### 8. MyBatis有哪些重要的配置项？

**答案：**
- **cacheEnabled**：是否启用二级缓存
- **lazyLoadingEnabled**：是否启用延迟加载
- **mapUnderscoreToCamelCase**：下划线转驼峰命名
- **defaultExecutorType**：默认执行器类型
- **defaultStatementTimeout**：默认语句超时时间
- **autoMappingBehavior**：自动映射行为
- **jdbcTypeForNull**：NULL值的JDBC类型

---

## SQL映射

### 9. MyBatis的动态SQL有哪些标签？

**答案：**
- **if**：条件判断
- **choose/when/otherwise**：多条件选择
- **where**：动态WHERE子句
- **set**：动态SET子句
- **foreach**：循环遍历
- **trim**：去除多余的字符
- **bind**：创建变量

**示例：**
```xml
<select id="findUsers" resultType="User">
    SELECT * FROM user
    <where>
        <if test="name != null">
            AND name = #{name}
        </if>
        <if test="age != null">
            AND age = #{age}
        </if>
    </where>
</select>
```

### 10. #{} 和 ${} 的区别是什么？

**答案：**

| 特性 | #{} | ${} |
|------|-----|-----|
| **预编译** | 支持 | 不支持 |
| **SQL注入** | 防止 | 存在风险 |
| **类型处理** | 自动 | 字符串替换 |
| **使用场景** | 参数值 | 表名、列名 |

**示例：**
```xml
<!-- 安全的参数传递 -->
<select id="findById" resultType="User">
    SELECT * FROM user WHERE id = #{id}
</select>

<!-- 动态表名（注意SQL注入风险） -->
<select id="findFromTable" resultType="User">
    SELECT * FROM ${tableName} WHERE id = #{id}
</select>
```

---

## 缓存机制

### 11. MyBatis的缓存机制是什么？

**答案：**

**一级缓存（SqlSession级别）：**
- 默认开启，无法关闭
- 作用域：单个SqlSession
- 存储：HashMap
- 失效：增删改操作、手动清空、SqlSession关闭

**二级缓存（Mapper级别）：**
- 需要手动开启
- 作用域：同一个Mapper
- 跨SqlSession共享
- 可配置缓存策略

### 12. 如何配置和使用二级缓存？

**答案：**
```xml
<!-- 1. 在mybatis-config.xml中开启 -->
<settings>
    <setting name="cacheEnabled" value="true"/>
</settings>

<!-- 2. 在Mapper.xml中配置 -->
<cache 
    eviction="LRU"
    flushInterval="60000"
    size="512"
    readOnly="true"/>

<!-- 3. 实体类实现Serializable -->
public class User implements Serializable {
    // ...
}
```

### 13. 缓存的失效策略有哪些？

**答案：**
- **LRU**：最近最少使用
- **FIFO**：先进先出
- **SOFT**：软引用
- **WEAK**：弱引用

---

## 插件机制

### 14. MyBatis的插件机制是如何工作的？

**答案：**
MyBatis插件基于拦截器模式，可以拦截四大对象的方法：

**可拦截的对象：**
- **Executor**：执行器
- **StatementHandler**：语句处理器
- **ParameterHandler**：参数处理器
- **ResultSetHandler**：结果集处理器

**实现步骤：**
```java
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, 
                      RowBounds.class, ResultHandler.class})
})
public class MyInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 前置处理
        Object result = invocation.proceed();
        // 后置处理
        return result;
    }
}
```

### 15. 常见的MyBatis插件有哪些？

**答案：**
- **PageHelper**：分页插件
- **MyBatis-Plus**：增强工具
- **通用Mapper**：通用CRUD
- **性能监控插件**：SQL执行时间统计
- **数据权限插件**：行级数据过滤

---

## 性能优化

### 16. MyBatis性能优化的方法有哪些？

**答案：**

**SQL优化：**
- 避免N+1查询问题
- 使用合适的索引
- 优化复杂查询
- 使用批量操作

**缓存优化：**
- 合理使用一级缓存
- 配置二级缓存
- 使用Redis等外部缓存

**连接池优化：**
- 配置合适的连接池大小
- 设置连接超时时间
- 监控连接池状态

**代码优化：**
- 使用ResultMap避免反射
- 合理使用延迟加载
- 避免大结果集查询

### 17. 如何解决N+1查询问题？

**答案：**

**问题描述：**
查询N个对象时，执行了1+N次SQL查询

**解决方案：**

1. **使用关联查询**
```xml
<select id="findUsersWithRoles" resultMap="userRoleMap">
    SELECT u.*, r.* FROM user u 
    LEFT JOIN user_role ur ON u.id = ur.user_id
    LEFT JOIN role r ON ur.role_id = r.id
</select>
```

2. **使用延迟加载**
```xml
<association property="roles" 
             select="findRolesByUserId" 
             column="id" 
             fetchType="lazy"/>
```

3. **批量查询**
```xml
<select id="findRolesByUserIds" resultType="Role">
    SELECT * FROM role WHERE user_id IN
    <foreach collection="userIds" item="id" open="(" close=")" separator=",">
        #{id}
    </foreach>
</select>
```

---

## 实际应用

### 18. MyBatis在实际项目中的最佳实践是什么？

**答案：**

**项目结构：**
```
src/main/java/
├── mapper/          # Mapper接口
├── entity/          # 实体类
├── service/         # 业务层
└── config/          # 配置类

src/main/resources/
├── mapper/          # XML映射文件
├── mybatis-config.xml
└── application.yml
```

**编码规范：**
- Mapper接口与XML文件同名
- 使用合适的命名规范
- 合理使用动态SQL
- 避免复杂的嵌套查询

**异常处理：**
- 统一异常处理
- 事务回滚策略
- 连接泄漏监控

### 19. MyBatis与Spring集成的要点是什么？

**答案：**

**依赖配置：**
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.2.0</version>
</dependency>
```

**配置要点：**
```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
```

**注解配置：**
```java
@MapperScan("com.example.mapper")
@Configuration
public class MyBatisConfig {
    // 配置类
}
```

---

## 高级特性

### 20. MyBatis的类型处理器是什么？如何自定义？

**答案：**

**作用：**
在Java类型和JDBC类型之间进行转换

**自定义类型处理器：**
```java
@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes(JdbcType.TIMESTAMP)
public class LocalDateTimeTypeHandler implements TypeHandler<LocalDateTime> {
    
    @Override
    public void setParameter(PreparedStatement ps, int i, 
                           LocalDateTime parameter, JdbcType jdbcType) 
                           throws SQLException {
        if (parameter != null) {
            ps.setTimestamp(i, Timestamp.valueOf(parameter));
        } else {
            ps.setNull(i, Types.TIMESTAMP);
        }
    }
    
    @Override
    public LocalDateTime getResult(ResultSet rs, String columnName) 
                                 throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
```

### 21. MyBatis的批量执行器是什么？

**答案：**

**执行器类型：**
- **SIMPLE**：简单执行器（默认）
- **REUSE**：重用执行器
- **BATCH**：批量执行器

**批量执行器使用：**
```java
SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
try {
    UserMapper mapper = session.getMapper(UserMapper.class);
    for (User user : users) {
        mapper.insert(user);
    }
    session.commit();
} finally {
    session.close();
}
```

---

## 对比分析

### 22. MyBatis与Hibernate的区别是什么？

**答案：**

| 特性 | MyBatis | Hibernate |
|------|---------|-----------|
| **学习曲线** | 较平缓 | 较陡峭 |
| **SQL控制** | 完全控制 | 自动生成 |
| **性能优化** | 手动优化 | 自动优化 |
| **缓存机制** | 简单 | 复杂强大 |
| **适用场景** | 复杂SQL | 标准CRUD |
| **开发效率** | 中等 | 较高 |

### 23. MyBatis与JPA的对比？

**答案：**

**MyBatis优势：**
- SQL可控性强
- 学习成本低
- 适合复杂查询
- 性能调优灵活

**JPA优势：**
- 标准化规范
- 面向对象
- 自动建表
- 跨数据库

### 24. 什么时候选择MyBatis？

**答案：**

**适合MyBatis的场景：**
- 复杂的SQL查询
- 需要精确控制SQL
- 遗留系统集成
- 性能要求较高
- 团队SQL能力强

**不适合MyBatis的场景：**
- 简单的CRUD操作
- 快速原型开发
- 团队ORM经验不足
- 需要跨数据库支持

---

## 🎯 面试技巧

### 回答要点：
1. **理论结合实践**：不仅要说原理，还要举实际例子
2. **对比分析**：与其他框架对比，突出优缺点
3. **性能考虑**：从性能角度分析问题和解决方案
4. **最佳实践**：分享实际项目中的经验
5. **源码理解**：适当展示对源码的理解

### 常见陷阱：
- 不要只背概念，要理解原理
- 注意SQL注入等安全问题
- 考虑并发和事务问题
- 关注性能和内存使用

---

## 📚 推荐学习资源

- **官方文档**：https://mybatis.org/mybatis-3/
- **源码分析**：GitHub上的MyBatis源码
- **实战项目**：Spring Boot + MyBatis项目
- **性能测试**：JMeter + MyBatis性能测试
- **社区讨论**：Stack Overflow、掘金等技术社区

---

*持续更新中... 如有问题或建议，欢迎提出！*