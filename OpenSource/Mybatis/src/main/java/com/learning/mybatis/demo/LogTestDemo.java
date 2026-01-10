package com.learning.mybatis.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 日志测试服务
 * 用于验证log.info是否正常工作
 */
@Slf4j
@Service
public class LogTestDemo {
    
    public void testLogging() {
        System.out.println("这是一个测试日志信息");
        System.out.println("这是一个调试日志信息");
        System.out.println("这是一个警告日志信息");
        System.out.println("这是一个错误日志信息");
        
        // 测试带参数的日志
        String name = "MyBatis";
        int version = 3;
        System.out.println("正在测试 " + name + " 版本 " + version);
        
        // 测试异常日志
        try {
            throw new RuntimeException("测试异常");
        } catch (Exception e) {
            //System.out.println("捕获到异常", e);
        }
    }
}