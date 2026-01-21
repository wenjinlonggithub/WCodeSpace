package com.architecture.user.domain;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * 数据库表: t_user
 *
 * 字段说明:
 * - id: 主键ID
 * - username: 用户名(唯一)
 * - password: 密码(BCrypt加密)
 * - email: 邮箱
 * - phone: 手机号
 * - realName: 真实姓名
 * - status: 状态(0-禁用,1-正常)
 * - roles: 角色列表(逗号分隔)
 * - createTime: 创建时间
 * - updateTime: 更新时间
 *
 * @author Architecture Team
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(加密)
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 状态: 0-禁用, 1-正常
     */
    private Integer status;

    /**
     * 角色列表(逗号分隔): ROLE_USER, ROLE_ADMIN
     */
    private String roles;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
