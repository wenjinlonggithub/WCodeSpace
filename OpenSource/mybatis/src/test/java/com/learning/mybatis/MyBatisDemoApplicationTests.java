package com.learning.mybatis;

import com.learning.mybatis.entity.User;
import com.learning.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis基础功能测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MyBatisDemoApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        // 验证Spring上下文加载成功
        assertNotNull(userMapper);
    }

    @Test
    void testBasicCrud() {
        // 测试基础CRUD操作
        
        // 1. 插入测试
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .realName("测试用户")
                .age(25)
                .gender(1)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        int insertResult = userMapper.insertSelective(user);
        assertEquals(1, insertResult);
        assertNotNull(user.getId());
        
        // 2. 查询测试
        User foundUser = userMapper.selectById(user.getId());
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
        
        // 3. 更新测试
        foundUser.setAge(26);
        foundUser.setUpdateTime(LocalDateTime.now());
        int updateResult = userMapper.updateSelective(foundUser);
        assertEquals(1, updateResult);
        
        // 验证更新结果
        User updatedUser = userMapper.selectById(user.getId());
        assertEquals(26, updatedUser.getAge());
        
        // 4. 删除测试
        int deleteResult = userMapper.deleteById(user.getId());
        assertEquals(1, deleteResult);
        
        // 验证删除结果
        User deletedUser = userMapper.selectById(user.getId());
        assertNull(deletedUser);
    }

    @Test
    void testCount() {
        // 测试统计功能
        int totalCount = userMapper.countAll();
        assertTrue(totalCount >= 0);
        
        int enabledCount = userMapper.countByStatus(1);
        assertTrue(enabledCount >= 0);
    }
}