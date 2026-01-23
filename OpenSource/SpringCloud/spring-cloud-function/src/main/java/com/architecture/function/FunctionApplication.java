package com.architecture.function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Spring Cloud Function 应用入口
 *
 * Spring Cloud Function 是一个基于 Spring Boot 的函数式编程框架
 * 它将业务逻辑抽象为 Function、Consumer、Supplier 等函数式接口
 */
@SpringBootApplication
public class FunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunctionApplication.class, args);
    }

    /**
     * Function: 接收输入并返回输出
     * 示例：字符串转大写
     */
    @Bean
    public Function<String, String> uppercase() {
        return value -> {
            System.out.println("Processing: " + value);
            return value.toUpperCase();
        };
    }

    /**
     * Function: 链式处理
     * 示例：字符串反转
     */
    @Bean
    public Function<String, String> reverse() {
        return value -> new StringBuilder(value).reverse().toString();
    }

    /**
     * Supplier: 无输入，产生输出
     * 示例：生成当前时间戳
     */
    @Bean
    public Supplier<Long> timestamp() {
        return () -> System.currentTimeMillis();
    }

    /**
     * Consumer: 接收输入，无输出
     * 示例：记录日志
     */
    @Bean
    public Consumer<String> log() {
        return value -> System.out.println("Log: " + value);
    }

    /**
     * 组合 Function: 处理对象转换
     */
    @Bean
    public Function<User, UserDTO> userConverter() {
        return user -> new UserDTO(user.getId(), user.getName().toUpperCase());
    }

    /**
     * 用户实体类
     */
    public static class User {
        private Long id;
        private String name;

        public User() {}

        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /**
     * 用户 DTO
     */
    public static class UserDTO {
        private Long id;
        private String displayName;

        public UserDTO() {}

        public UserDTO(Long id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}
