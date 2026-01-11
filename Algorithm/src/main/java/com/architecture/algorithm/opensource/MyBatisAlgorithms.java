package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis框架中算法应用案例
 * 展示MyBatis框架中使用的各种经典算法和数据结构
 */
public class MyBatisAlgorithms {
    
    /**
     * 演示MyBatis中的动态SQL解析算法
     */
    public void demonstrateDynamicSqlParsing() {
        System.out.println("1. MyBatis动态SQL解析算法");
        
        // 模拟MyBatis动态SQL解析过程
        String dynamicSql = "SELECT * FROM users WHERE 1=1 " +
                           "<if test='name != null'>AND name = #{name}</if>" +
                           "<if test='age > 0'>AND age > #{age}</if>";
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");
        params.put("age", 25);
        
        String parsedSql = parseDynamicSql(dynamicSql, params);
        System.out.println("   原始动态SQL: " + dynamicSql);
        System.out.println("   解析后SQL: " + parsedSql);
    }
    
    /**
     * 简化版动态SQL解析算法
     */
    private String parseDynamicSql(String sql, Map<String, Object> params) {
        // 模拟<if>标签解析
        Pattern ifPattern = Pattern.compile("<if[^>]*test=['\"]([^'\"]+)['\"][^>]*>(.*?)</if>", 
                                          Pattern.DOTALL);
        Matcher matcher = ifPattern.matcher(sql);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1);
            String content = matcher.group(2);
            
            // 简化条件判断逻辑
            boolean conditionMet = evaluateCondition(condition, params);
            if (conditionMet) {
                matcher.appendReplacement(result, content.trim());
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 简化版条件评估算法
     */
    private boolean evaluateCondition(String condition, Map<String, Object> params) {
        // 模拟简单的条件判断逻辑
        if (condition.contains("!= null")) {
            String param = condition.split("!=")[0].trim();
            return params.get(param) != null;
        } else if (condition.contains("> 0")) {
            String param = condition.split(">")[0].trim();
            Object value = params.get(param);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() > 0;
            }
        }
        return false;
    }
    
    /**
     * 演示MyBatis中的SQL映射算法
     */
    public void demonstrateSqlMappingAlgorithm() {
        System.out.println("\n2. MyBatis SQL映射算法");
        
        // 模拟MyBatis的结果映射过程
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("username", "john_doe");
        row.put("email", "john@example.com");
        row.put("created_at", "2023-01-01 10:00:00");
        
        User user = mapRowToUser(row);
        System.out.println("   映射结果: " + user);
    }
    
    /**
     * 简化版结果映射算法
     */
    private User mapRowToUser(Map<String, Object> row) {
        User user = new User();
        
        // 使用反射或手动映射字段
        user.setId((Integer) row.get("id"));
        user.setUsername((String) row.get("username"));
        user.setEmail((String) row.get("email"));
        user.setCreatedAt((String) row.get("created_at"));
        
        return user;
    }
    
    /**
     * 演示MyBatis中的缓存算法
     */
    public void demonstrateCacheAlgorithm() {
        System.out.println("\n3. MyBatis缓存算法");
        
        // 演示一级缓存（会话级别）
        FirstLevelCache firstLevelCache = new FirstLevelCache();
        firstLevelCache.putObject("user:1", new User(1, "john", "john@example.com", "2023-01-01"));
        System.out.println("   一级缓存存储用户: john");
        
        User cachedUser = (User) firstLevelCache.getObject("user:1");
        System.out.println("   从一级缓存获取用户: " + cachedUser.getUsername());
        
        // 演示二级缓存（Mapper级别）
        SecondLevelCache secondLevelCache = new SecondLevelCache();
        secondLevelCache.put("users", "SELECT * FROM users WHERE id = 1", cachedUser);
        System.out.println("   二级缓存存储查询结果");
        
        User cachedUser2 = (User) secondLevelCache.get("users", "SELECT * FROM users WHERE id = 1");
        System.out.println("   从二级缓存获取用户: " + cachedUser2.getUsername());
    }
    
    /**
     * 演示MyBatis中的插件算法
     */
    public void demonstratePluginAlgorithm() {
        System.out.println("\n4. MyBatis插件算法");
        
        // 模拟MyBatis拦截器链执行
        InterceptorChain interceptorChain = new InterceptorChain();
        interceptorChain.addInterceptor(new PaginationInterceptor());
        interceptorChain.addInterceptor(new PerformanceInterceptor());
        interceptorChain.addInterceptor(new AuditInterceptor());
        
        // 创建一个模拟的SQL执行上下文
        ExecutionContext context = new ExecutionContext(
            "SELECT * FROM users LIMIT 10", 
            Arrays.asList("id", "name", "email")
        );
        
        try {
            Object result = interceptorChain.pluginAll(context);
            System.out.println("   拦截器链执行完成，结果: " + result);
        } catch (Exception e) {
            System.out.println("   拦截器链执行异常: " + e.getMessage());
        }
    }
    
    /**
     * 演示MyBatis中的类型处理器算法
     */
    public void demonstrateTypeHandlerAlgorithm() {
        System.out.println("\n5. MyBatis类型处理器算法");
        
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        
        // 注册不同类型处理器
        registry.register(Integer.class, new IntegerTypeHandler());
        registry.register(String.class, new StringTypeHandler());
        registry.register(Boolean.class, new BooleanTypeHandler());
        
        // 演示类型转换
        Object intValue = registry.getTypeHandler(Integer.class).getResult("123");
        Object strValue = registry.getTypeHandler(String.class).getResult("hello");
        Object boolValue = registry.getTypeHandler(Boolean.class).getResult("true");
        
        System.out.println("   Integer转换: \"123\" -> " + intValue + " (类型: " + 
                          intValue.getClass().getSimpleName() + ")");
        System.out.println("   String转换: 任意值 -> " + strValue + " (类型: " + 
                          strValue.getClass().getSimpleName() + ")");
        System.out.println("   Boolean转换: \"true\" -> " + boolValue + " (类型: " + 
                          boolValue.getClass().getSimpleName() + ")");
    }
    
    // 内部类定义
    static class User {
        private Integer id;
        private String username;
        private String email;
        private String createdAt;
        
        public User() {}
        
        public User(Integer id, String username, String email, String createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.createdAt = createdAt;
        }
        
        // Getters and setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        @Override
        public String toString() {
            return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
        }
    }
    
    // 一级缓存实现
    static class FirstLevelCache {
        private final Map<Object, Object> cache = new HashMap<>();
        
        public void putObject(Object key, Object value) {
            cache.put(key, value);
        }
        
        public Object getObject(Object key) {
            return cache.get(key);
        }
        
        public void removeObject(Object key) {
            cache.remove(key);
        }
        
        public void clear() {
            cache.clear();
        }
    }
    
    // 二级缓存实现
    static class SecondLevelCache {
        private final Map<String, Map<String, Object>> namespaceCache = new HashMap<>();
        
        public void put(String namespace, String key, Object value) {
            namespaceCache.computeIfAbsent(namespace, k -> new HashMap<>()).put(key, value);
        }
        
        public Object get(String namespace, String key) {
            Map<String, Object> cache = namespaceCache.get(namespace);
            return cache != null ? cache.get(key) : null;
        }
        
        public void clear(String namespace) {
            if (namespaceCache.containsKey(namespace)) {
                namespaceCache.get(namespace).clear();
            }
        }
    }
    
    // 拦截器接口
    interface Interceptor {
        Object intercept(ExecutionContext invocation) throws Exception;
        Object plugin(Object target);
    }
    
    // 拦截器链
    static class InterceptorChain {
        private final List<Interceptor> interceptors = new ArrayList<>();
        
        public void addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
        }
        
        public Object pluginAll(ExecutionContext target) throws Exception {
            Object current = target;
            for (Interceptor interceptor : interceptors) {
                current = interceptor.plugin(current);
            }
            return current;
        }
    }
    
    // 执行上下文
    static class ExecutionContext {
        private final String sql;
        private final List<String> columns;
        
        public ExecutionContext(String sql, List<String> columns) {
            this.sql = sql;
            this.columns = columns;
        }
        
        public String getSql() { return sql; }
        public List<String> getColumns() { return columns; }
        
        @Override
        public String toString() {
            return "ExecutionContext{sql='" + sql + "', columns=" + columns + "}";
        }
    }
    
    // 分页拦截器
    static class PaginationInterceptor implements Interceptor {
        @Override
        public Object intercept(ExecutionContext invocation) throws Exception {
            System.out.println("   [分页拦截器] 处理SQL: " + invocation.getSql());
            // 实际的分页逻辑...
            return "PAGINATED_RESULT";
        }
        
        @Override
        public Object plugin(Object target) {
            System.out.println("   [分页拦截器] 开始执行");
            try {
                return intercept((ExecutionContext) target);
            } catch (Exception e) {
                System.out.println("   [分页拦截器] 执行异常: " + e.getMessage());
                return null;
            }
        }
    }
    
    // 性能拦截器
    static class PerformanceInterceptor implements Interceptor {
        @Override
        public Object intercept(ExecutionContext invocation) throws Exception {
            long startTime = System.currentTimeMillis();
            System.out.println("   [性能拦截器] 开始执行SQL");
            Object result = "PERFORMANCE_RESULT";
            long endTime = System.currentTimeMillis();
            System.out.println("   [性能拦截器] SQL执行耗时: " + (endTime - startTime) + "ms");
            return result;
        }
        
        @Override
        public Object plugin(Object target) {
            System.out.println("   [性能拦截器] 开始执行");
            try {
                return intercept((ExecutionContext) target);
            } catch (Exception e) {
                System.out.println("   [性能拦截器] 执行异常: " + e.getMessage());
                return null;
            }
        }
    }
    
    // 审计拦截器
    static class AuditInterceptor implements Interceptor {
        @Override
        public Object intercept(ExecutionContext invocation) throws Exception {
            System.out.println("   [审计拦截器] 记录SQL执行: " + invocation.getSql());
            // 记录审计日志...
            return "AUDIT_RESULT";
        }
        
        @Override
        public Object plugin(Object target) {
            System.out.println("   [审计拦截器] 开始执行");
            try {
                return intercept((ExecutionContext) target);
            } catch (Exception e) {
                System.out.println("   [审计拦截器] 执行异常: " + e.getMessage());
                return null;
            }
        }
    }
    
    // 类型处理器接口
    interface TypeHandler<T> {
        T getResult(String value);
        String getString(T value);
    }
    
    // 类型处理器注册表
    static class TypeHandlerRegistry {
        private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<>();
        
        public <T> void register(Class<T> type, TypeHandler<T> handler) {
            typeHandlers.put(type, handler);
        }
        
        @SuppressWarnings("unchecked")
        public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
            return (TypeHandler<T>) typeHandlers.get(type);
        }
    }
    
    // 具体类型处理器
    static class IntegerTypeHandler implements TypeHandler<Integer> {
        @Override
        public Integer getResult(String value) {
            return Integer.parseInt(value);
        }
        
        @Override
        public String getString(Integer value) {
            return String.valueOf(value);
        }
    }
    
    static class StringTypeHandler implements TypeHandler<String> {
        @Override
        public String getResult(String value) {
            return value; // 直接返回
        }
        
        @Override
        public String getString(String value) {
            return value;
        }
    }
    
    static class BooleanTypeHandler implements TypeHandler<Boolean> {
        @Override
        public Boolean getResult(String value) {
            return Boolean.parseBoolean(value);
        }
        
        @Override
        public String getString(Boolean value) {
            return String.valueOf(value);
        }
    }
    
    public void demonstrate() {
        demonstrateDynamicSqlParsing();
        demonstrateSqlMappingAlgorithm();
        demonstrateCacheAlgorithm();
        demonstratePluginAlgorithm();
        demonstrateTypeHandlerAlgorithm();
    }
}