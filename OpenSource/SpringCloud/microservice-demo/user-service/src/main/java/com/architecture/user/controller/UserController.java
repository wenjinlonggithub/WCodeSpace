package com.architecture.user.controller;

import com.architecture.user.dto.UserDTO;
import com.architecture.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * 提供用户相关的REST接口:
 * - GET /users/{id}: 根据ID查询用户
 * - GET /users/username/{username}: 根据用户名查询
 * - POST /users: 创建用户(注册)
 * - PUT /users/{id}: 更新用户信息
 *
 * @author Architecture Team
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID查询用户
     * 其他服务通过Feign调用此接口获取用户信息
     */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * 创建用户(注册)
     */
    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        return userService.updateUser(userDTO);
    }
}
