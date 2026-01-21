package com.architecture.user.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户DTO - 数据传输对象
 * 用于服务间调用,不包含敏感信息(如密码)
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private Integer status;
    private String roles;
}
