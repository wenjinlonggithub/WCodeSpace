package com.example.springdemo.ioc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 * 
 * 用于演示Spring IoC容器管理的业务对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String name;
    private String email;
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s'}", id, name, email);
    }
}