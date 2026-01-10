package com.learning.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 * 
 * 演示MyBatis的对象映射功能
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码（加密后）
     */
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 性别：1-男，2-女，0-未知
     */
    private Integer gender;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 用户角色列表（一对多关联）
     */
    private List<Role> roles;
    
    /**
     * 用户详细信息（一对一关联）
     */
    private UserProfile profile;
    
    /**
     * 用户订单列表（一对多关联）
     */
    private List<Order> orders;
    
    /**
     * 性别枚举
     */
    public enum Gender {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");
        
        private final int code;
        private final String desc;
        
        Gender(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() { return code; }
        public String getDesc() { return desc; }
        
        public static Gender fromCode(int code) {
            for (Gender gender : values()) {
                if (gender.code == code) {
                    return gender;
                }
            }
            return UNKNOWN;
        }
    }
    
    /**
     * 状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "正常");
        
        private final int code;
        private final String desc;
        
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() { return code; }
        public String getDesc() { return desc; }
        
        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return DISABLED;
        }
    }
    
    /**
     * 获取性别描述
     */
    public String getGenderDesc() {
        return Gender.fromCode(this.gender != null ? this.gender : 0).getDesc();
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        return Status.fromCode(this.status != null ? this.status : 0).getDesc();
    }
    
    /**
     * 判断是否为男性
     */
    public boolean isMale() {
        return Gender.MALE.getCode() == (this.gender != null ? this.gender : 0);
    }
    
    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return Status.ENABLED.getCode() == (this.status != null ? this.status : 0);
    }
    
    /**
     * 隐藏敏感信息的toString方法
     */
    public String toSafeString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", realName='" + realName + '\'' +
                ", phone='" + (phone != null ? phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : null) + '\'' +
                ", age=" + age +
                ", gender=" + getGenderDesc() +
                ", status=" + getStatusDesc() +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}