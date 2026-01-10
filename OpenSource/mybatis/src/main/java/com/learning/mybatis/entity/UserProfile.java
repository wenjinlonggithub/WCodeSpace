package com.learning.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详细信息实体类
 * 
 * 演示一对一关联映射
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    /**
     * 详细信息ID
     */
    private Long id;
    
    /**
     * 用户ID（外键）
     */
    private Long userId;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 生日
     */
    private LocalDate birthday;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 关联的用户对象（一对一）
     */
    private User user;
    
    /**
     * 计算年龄
     */
    public Integer getAge() {
        if (birthday == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthday.getYear();
    }
    
    /**
     * 判断是否有头像
     */
    public boolean hasAvatar() {
        return avatar != null && !avatar.trim().isEmpty();
    }
    
    /**
     * 获取简短的个人简介
     */
    public String getShortBio() {
        if (bio == null || bio.length() <= 50) {
            return bio;
        }
        return bio.substring(0, 50) + "...";
    }
}