package com.example.springdemo.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    
    private final String userId;
    private final String username;
    private final String email;
    
    public UserRegisteredEvent(Object source, String userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}