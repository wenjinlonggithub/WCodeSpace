# MyBatis框架深度学习项目

## 项目简介

这是一个专门用于深度学习MyBatis框架的完整演示项目。通过实际代码示例和详细注释，帮助开发者全面掌握MyBatis的核心特性和最佳实践。

## 🎯 学习目标

- **掌握MyBatis核心概念**: 理解ORM映射、SQL映射、动态SQL等核心概念
- **熟练使用基础功能**: 掌握CRUD操作、参数传递、结果映射等基础功能
- **深入理解高级特性**: 学习缓存机制、插件开发、类型处理器等高级特性
- **掌握最佳实践**: 了解性能优化、安全防护、代码规范等最佳实践

## 📚 项目结构

```
mybatis/
├── src/main/java/com/learning/mybatis/
│   ├── MyBatisDemoApplication.java     # 主启动类
│   ├── entity/                         # 实体类
│   │   ├── User.java                   # 用户实体
│   │   ├── Role.java                   # 角色实体
│   │   ├── Permission.java             # 权限实体
│   │   ├── UserProfile.java            # 用户详细信息
│   │   ├── Order.java                  # 订单实体
│   │   └── OrderItem.java              # 订单项实体
│   ├── mapper/                         # Mapper接口
│   │   └── UserMapper.java             # 用户Mapper
│   └── demo/                           # 演示类
│       ├── BasicCrudDemo.java          # 基础CRUD演示
│       ├── DynamicSqlDemo.java         # 动态SQL演示
│       ├── ResultMapDemo.java          # 结果映射演示
│       ├── CacheDemo.java              # 缓存机制演示
│       └── AdvancedFeaturesDemo.java   # 高级特性演示
├── src/main/resources/
│   ├── mapper/
│   │   └── UserMapper.xml              # MyBatis映射文件
│   ├── application.yml                 # Spring Boot配置
│   ├── mybatis-config.xml              # MyBatis配置文件
│   └── schema.sql                      # 数据库初始化脚本
└── pom.xml                             # Maven配置文件
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17或更高版本
- **Maven**: 3.6+
- **IDE**: IntelliJ IDEA（推荐）

### 运行步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd mybatis
```

2. **编译项目**
```bash
mvn clean compile
```

3. **运行演示**
```bash
mvn spring-boot:run
```

4. **访问H2控制台**（可选）
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
用户名: sa
密码: （空）
```

5. **访问Druid监控**（可选）
```
URL: http://localhost:8080/druid
用户名: admin
密码: admin123
```

## 📖 核心特性演示

### 1. 基础CRUD操作 (`BasicCrudDemo`)

- ✅ **插入操作**: 单个插入、选择性插入、批量插入
- ✅ **查询操作**: 根据ID查询、条件查询、分页查询、统计查询
- ✅ **更新操作**: 全量更新、选择性更新、批量更新
- ✅ **删除操作**: 物理删除、软删除、批量删除

```java
// 示例：选择性插入
User user = User.builder()
    .username("zhangsan")
    .email("zhangsan@example.com")
    .realName("张三")
    .age(28)
    .status(1)
    .build();
userMapper.insertSelective(user);
```

### 2. 动态SQL (`DynamicSqlDemo`)

- ✅ **if条件判断**: 根据参数动态生成SQL条件
- ✅ **where智能拼接**: 自动处理AND/OR连接符
- ✅ **set动态更新**: 只更新非空字段
- ✅ **foreach循环**: 处理集合参数
- ✅ **choose分支选择**: 多条件分支处理

```xml
<!-- 示例：动态条件查询 -->
<select id="selectByCondition" resultMap="userResultMap">
    SELECT * FROM user
    <where>
        <if test="username != null and username != ''">
            AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
</select>
```

### 3. 结果映射 (`ResultMapDemo`)

- ✅ **基础结果映射**: 字段名与属性名映射
- ✅ **一对一关联**: association元素使用
- ✅ **一对多关联**: collection元素使用
- ✅ **多对多关联**: 复杂关联映射
- ✅ **嵌套查询**: 分步查询和嵌套结果

```xml
<!-- 示例：一对多关联映射 -->
<resultMap id="userWithRolesMap" type="User">
    <id property="id" column="user_id"/>
    <result property="username" column="username"/>
    <collection property="roles" ofType="Role">
        <id property="id" column="role_id"/>
        <result property="roleName" column="role_name"/>
    </collection>
</resultMap>
```

### 4. 缓存机制 (`CacheDemo`)

- ✅ **一级缓存**: SqlSession级别缓存演示
- ✅ **二级缓存**: namespace级别缓存演示
- ✅ **缓存失效**: 缓存失效机制和触发条件
- ✅ **缓存配置**: 缓存参数配置和优化

```java
// 示例：缓存使用演示
User user1 = userMapper.selectById(1L); // 查询数据库
User user2 = userMapper.selectById(1L); // 从一级缓存获取
System.out.println(user1 == user2); // true，同一个对象实例
```

### 5. 高级特性 (`AdvancedFeaturesDemo`)

- ✅ **插件机制**: 拦截器开发和配置
- ✅ **类型处理器**: 自定义类型转换
- ✅ **SQL构建器**: 编程式SQL构建
- ✅ **批量操作**: 性能优化技巧
- ✅ **性能监控**: 执行时间和性能分析

```java
// 示例：自定义插件
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
              args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PerformanceInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = invocation.proceed();
        long endTime = System.currentTimeMillis();
        log.info("SQL执行耗时: {}ms", endTime - startTime);
        return result;
    }
}
```

## 🔧 技术栈

- **核心框架**: MyBatis 3.5.13, Spring Boot 3.2.0
- **数据库**: H2 Database (内存数据库，便于演示)
- **连接池**: Druid 1.2.20
- **分页插件**: PageHelper 1.4.7
- **工具库**: Lombok, Jackson
- **测试框架**: JUnit 5, Spring Boot Test

## 📊 数据模型

项目包含完整的用户权限管理数据模型：

```
用户表 (user)
├── 基础信息: id, username, email, password
├── 个人信息: real_name, phone, age, gender
└── 状态信息: status, create_time, update_time

角色表 (role)
├── 角色信息: id, role_code, role_name, description
└── 状态信息: status, create_time, update_time

权限表 (permission)
├── 权限信息: id, permission_code, permission_name
├── 资源信息: resource_type, resource_url
└── 层级信息: parent_id, sort_order

关联表
├── 用户角色关联 (user_role)
├── 角色权限关联 (role_permission)
├── 用户详细信息 (user_profile) - 一对一
└── 用户订单 (orders) - 一对多
```

## 🎓 学习路径

### 初级阶段
1. 运行项目，观察控制台输出
2. 学习基础CRUD操作演示
3. 理解实体类和Mapper接口设计
4. 掌握基本的XML映射配置

### 中级阶段
1. 深入学习动态SQL的使用
2. 掌握复杂的结果映射配置
3. 理解一对一、一对多关联映射
4. 学习缓存机制的工作原理

### 高级阶段
1. 学习插件开发和自定义拦截器
2. 掌握类型处理器的开发
3. 理解MyBatis的执行流程
4. 学习性能优化和调优技巧

## 🔍 关键配置说明

### MyBatis配置 (`mybatis-config.xml`)
```xml
<settings>
    <!-- 开启驼峰命名转换 -->
    <setting name="mapUnderscoreToCamelCase" value="true"/>
    <!-- 开启二级缓存 -->
    <setting name="cacheEnabled" value="true"/>
    <!-- 延迟加载配置 -->
    <setting name="lazyLoadingEnabled" value="true"/>
</settings>
```

### Spring Boot配置 (`application.yml`)
```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.learning.mybatis.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
```

## 🚨 注意事项

1. **数据库**: 项目使用H2内存数据库，重启后数据会丢失
2. **日志**: 开启了SQL日志，可以观察实际执行的SQL语句
3. **缓存**: 演示了一级和二级缓存，注意缓存的生命周期
4. **事务**: 某些演示方法使用了@Transactional注解
5. **性能**: 批量操作演示了性能优化技巧

## 📚 扩展学习

- [MyBatis官方文档](https://mybatis.org/mybatis-3/)
- [Spring Boot MyBatis Starter](https://mybatis.org/spring-boot-starter/)
- [PageHelper分页插件](https://pagehelper.github.io/)
- [Druid连接池](https://github.com/alibaba/druid)

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个学习项目：

1. Fork项目
2. 创建特性分支: `git checkout -b feature/new-demo`
3. 提交更改: `git commit -am 'Add new demo'`
4. 推送分支: `git push origin feature/new-demo`
5. 提交Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

**开始你的MyBatis学习之旅吧！** 🚀

通过这个项目，你将全面掌握MyBatis框架的各种特性，为成为优秀的Java开发者打下坚实的基础。