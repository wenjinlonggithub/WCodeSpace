package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 复杂Bean类 - 用于演示复杂依赖注入
 * 
 * 🎯 演示目标:
 * 1. Bean之间的依赖关系
 * 2. 集合类型的注入
 * 3. 复杂对象的属性注入
 * 4. 内部Bean的使用
 * 
 * 📚 学习要点:
 * - 理解Spring如何处理复杂的依赖关系
 * - 掌握集合注入的XML配置方式
 * - 体验面向对象设计在IoC中的应用
 */
public class ComplexBean {
    
    private TestBean testBean;
    private String name;
    private List<String> items;
    private Map<String, Object> properties;
    private DatabaseConfig databaseConfig;
    
    public ComplexBean() {
        this.items = new ArrayList<>();
        this.properties = new HashMap<>();
        System.out.println("🏗️ ComplexBean 构造器调用");
    }
    
    // ===== 依赖注入相关的Setter方法 =====
    
    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
        System.out.println("🔗 ComplexBean 注入依赖: TestBean#" + testBean.getInstanceId());
    }
    
    public void setName(String name) {
        this.name = name;
        System.out.println("🔧 ComplexBean 注入属性: name=" + name);
    }
    
    public void setItems(List<String> items) {
        this.items = items;
        System.out.println("📋 ComplexBean 注入列表: " + items.size() + " 个元素");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("    [" + i + "] " + items.get(i));
        }
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
        System.out.println("🗂️ ComplexBean 注入Map: " + properties.size() + " 个属性");
        properties.forEach((key, value) -> {
            System.out.println("    " + key + " = " + value + " (" + value.getClass().getSimpleName() + ")");
        });
    }
    
    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        System.out.println("💾 ComplexBean 注入数据库配置: " + databaseConfig);
    }
    
    // ===== Getter方法 =====
    
    public TestBean getTestBean() {
        return testBean;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
    
    // ===== 业务方法 =====
    
    /**
     * 演示复杂Bean的业务逻辑
     */
    public void performComplexOperation() {
        System.out.println("🎭 ComplexBean 执行复杂业务操作:");
        
        // 使用注入的TestBean
        if (testBean != null) {
            System.out.println("  📞 调用依赖的TestBean业务方法:");
            testBean.doSomething();
        }
        
        // 处理集合数据
        System.out.println("  📊 处理集合数据:");
        System.out.println("    列表大小: " + items.size());
        System.out.println("    属性数量: " + properties.size());
        
        // 使用数据库配置
        if (databaseConfig != null) {
            System.out.println("  💾 使用数据库配置:");
            System.out.println("    连接URL: " + databaseConfig.getUrl());
            System.out.println("    用户名: " + databaseConfig.getUsername());
        }
    }
    
    /**
     * 显示ComplexBean的完整信息
     */
    public void showCompleteInfo() {
        System.out.println("📋 ComplexBean 完整信息:");
        System.out.println("  名称: " + name);
        System.out.println("  依赖Bean: " + (testBean != null ? testBean.toString() : "null"));
        System.out.println("  列表项: " + items);
        System.out.println("  属性映射: " + properties);
        System.out.println("  数据库配置: " + (databaseConfig != null ? databaseConfig.toString() : "null"));
    }
    
    @Override
    public String toString() {
        return "ComplexBean{" +
                "name='" + name + "'" +
                ", testBean=" + (testBean != null ? "TestBean#" + testBean.getInstanceId() : "null") +
                ", items=" + items.size() + " items" +
                ", properties=" + properties.size() + " props" +
                ", databaseConfig=" + (databaseConfig != null ? "configured" : "null") +
                "}";
    }
    
    /**
     * 内部数据库配置类
     * 用于演示内部Bean的定义和注入
     */
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private int maxConnections;
        private boolean autoCommit;
        
        public DatabaseConfig() {
            System.out.println("💾 DatabaseConfig 构造器调用");
        }
        
        // Setter方法用于属性注入
        public void setUrl(String url) {
            this.url = url;
            System.out.println("  🔧 DatabaseConfig 设置URL: " + url);
        }
        
        public void setUsername(String username) {
            this.username = username;
            System.out.println("  🔧 DatabaseConfig 设置用户名: " + username);
        }
        
        public void setPassword(String password) {
            this.password = "****"; // 隐藏密码显示\n            System.out.println("  🔧 DatabaseConfig 设置密码: ****");
        }
        
        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            System.out.println("  🔧 DatabaseConfig 设置最大连接数: " + maxConnections);
        }
        
        public void setAutoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
            System.out.println("  🔧 DatabaseConfig 设置自动提交: " + autoCommit);
        }
        
        // Getter方法
        public String getUrl() {
            return url;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public int getMaxConnections() {
            return maxConnections;
        }
        
        public boolean isAutoCommit() {
            return autoCommit;
        }
        
        @Override
        public String toString() {
            return "DatabaseConfig{" +
                    "url='" + url + "'" +
                    ", username='" + username + "'" +
                    ", maxConnections=" + maxConnections +
                    ", autoCommit=" + autoCommit +
                    "}";
        }
    }
}