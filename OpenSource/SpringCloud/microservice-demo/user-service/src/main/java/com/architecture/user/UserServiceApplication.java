package com.architecture.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * User Service 启动类
 *
 * 核心功能:
 * 1. 用户注册: 用户信息验证、密码加密、数据持久化
 * 2. 用户登录: 身份验证、JWT Token生成
 * 3. 用户信息管理: 查询、修改用户信息
 * 4. 权限管理: 角色分配、权限验证
 *
 * 技术要点:
 * - 密码加密: 使用BCrypt加密存储
 * - Token生成: 使用JWT生成访问令牌
 * - 数据持久化: MyBatis + MySQL
 * - 服务注册: 注册到Eureka供其他服务调用
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.architecture.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("==================================================");
        System.out.println("User Service 启动成功!");
        System.out.println("服务端口: 8001");
        System.out.println("==================================================");
    }
}
