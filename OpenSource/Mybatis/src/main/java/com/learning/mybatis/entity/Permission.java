package com.learning.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体类
 * 
 * 演示权限管理和多对多关联
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限代码
     */
    private String permissionCode;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 资源类型：menu-菜单，button-按钮，api-接口
     */
    private String resourceType;
    
    /**
     * 资源URL
     */
    private String resourceUrl;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
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
     * 拥有此权限的角色列表（多对多关联）
     */
    private List<Role> roles;
    
    /**
     * 父权限对象
     */
    private Permission parent;
    
    /**
     * 子权限列表
     */
    private List<Permission> children;
    
    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        MENU("menu", "菜单"),
        BUTTON("button", "按钮"),
        API("api", "接口");
        
        private final String code;
        private final String desc;
        
        ResourceType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() { return code; }
        public String getDesc() { return desc; }
        
        public static ResourceType fromCode(String code) {
            for (ResourceType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * 获取资源类型描述
     */
    public String getResourceTypeDesc() {
        ResourceType type = ResourceType.fromCode(this.resourceType);
        return type != null ? type.getDesc() : "未知";
    }
    
    /**
     * 判断是否为顶级权限
     */
    public boolean isTopLevel() {
        return parentId == null || parentId == 0;
    }
    
    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}