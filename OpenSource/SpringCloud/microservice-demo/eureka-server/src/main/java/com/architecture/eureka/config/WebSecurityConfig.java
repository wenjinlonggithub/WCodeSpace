package com.architecture.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Eureka Server 安全配置
 *
 * 功能说明:
 * 1. 保护Eureka控制台,防止未授权访问
 * 2. 允许服务注册和心跳请求(无需认证)
 * 3. 配置CSRF保护策略
 *
 * 注意事项:
 * - 生产环境必须开启安全认证
 * - 客户端注册时需要在URL中携带用户名密码
 * - 集群模式下各节点间也需要认证
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF,因为Eureka客户端不支持
            .csrf().disable()
            // 开启授权配置
            .authorizeRequests()
            // 允许访问Actuator健康检查端点
            .antMatchers("/actuator/**").permitAll()
            // 其他请求需要认证
            .anyRequest().authenticated()
            .and()
            // 使用HTTP Basic认证
            .httpBasic();

        return http.build();
    }
}
