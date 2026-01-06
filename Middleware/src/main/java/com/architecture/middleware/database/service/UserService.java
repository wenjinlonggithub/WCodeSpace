package com.architecture.middleware.database.service;

import com.architecture.middleware.database.entity.User;
import com.architecture.middleware.database.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Transactional
    public void createUser(User user) {
        userMapper.insert(user);
    }

    @Transactional
    public void updateUser(User user) {
        userMapper.update(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userMapper.delete(id);
    }

    public List<User> searchUsers(String keyword) {
        return userMapper.searchByUsername(keyword);
    }
}
