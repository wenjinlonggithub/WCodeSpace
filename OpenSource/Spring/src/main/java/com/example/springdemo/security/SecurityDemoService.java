package com.example.springdemo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Spring Security演示服务
 * 
 * 演示Spring Security的核心功能：
 * - 方法级安全控制
 * - 角色和权限管理
 * - 安全上下文使用
 */
@Slf4j
@Service
public class SecurityDemoService {
    
    public void demonstrateSecurity() {
        log.info("\n🔒 Spring Security 核心特性演示");
        
        // 1. 获取当前认证用户信息
        demonstrateSecurityContext();
        
        // 2. 演示方法级权限控制
        demonstrateMethodSecurity();
        
        // 3. 演示角色权限验证
        demonstrateRoleBasedAccess();
    }
    
    private void demonstrateSecurityContext() {
        log.info("\n--- SecurityContext 使用演示 ---");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            log.info("当前用户: {}", auth.getName());
            log.info("用户权限: {}", auth.getAuthorities());
            log.info("认证类型: {}", auth.getClass().getSimpleName());
        } else {
            log.info("当前用户: 匿名用户");
            log.info("提示: 在实际应用中，这里会显示已认证用户的信息");
        }
    }
    
    private void demonstrateMethodSecurity() {
        log.info("\n--- 方法级安全控制演示 ---");
        
        try {
            // 演示无需权限的方法
            String publicResult = getPublicData();
            log.info("公开方法调用成功: {}", publicResult);
            
            // 演示需要权限的方法（在真实环境中会进行权限检查）
            log.info("管理员方法: 在真实环境中需要ADMIN权限才能访问");
            log.info("用户方法: 在真实环境中需要USER权限才能访问");
            
        } catch (Exception e) {
            log.error("方法调用失败: {}", e.getMessage());
        }
    }
    
    private void demonstrateRoleBasedAccess() {
        log.info("\n--- 角色权限验证演示 ---");
        
        // 模拟不同角色的权限检查
        String[] roles = {"ADMIN", "USER", "GUEST"};
        String[] permissions = {"READ", "WRITE", "DELETE"};
        
        for (String role : roles) {
            for (String permission : permissions) {
                boolean hasPermission = simulatePermissionCheck(role, permission);
                log.info("角色 {} 对 {} 权限: {}", 
                        role, permission, hasPermission ? "✅允许" : "❌拒绝");
            }
        }
    }
    
    /**
     * 公开方法 - 无需特殊权限
     */
    public String getPublicData() {
        return "这是公开数据，任何人都可以访问";
    }
    
    /**
     * 管理员方法 - 需要ADMIN角色
     * 在真实环境中使用: @PreAuthorize("hasRole('ADMIN')")
     */
    // @PreAuthorize("hasRole('ADMIN')")
    public String getAdminData() {
        return "这是管理员数据，只有ADMIN角色可以访问";
    }
    
    /**
     * 用户方法 - 需要USER或ADMIN角色
     * 在真实环境中使用: @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
     */
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getUserData() {
        return "这是用户数据，USER和ADMIN角色可以访问";
    }
    
    /**
     * 权限检查方法 - 需要特定权限
     * 在真实环境中使用: @PreAuthorize("hasAuthority('WRITE')")
     */
    // @PreAuthorize("hasAuthority('WRITE')")
    public void updateData(String data) {
        log.info("数据更新: {}", data);
    }
    
    /**
     * 模拟权限检查
     */
    private boolean simulatePermissionCheck(String role, String permission) {
        // 模拟权限矩阵
        return switch (role) {
            case "ADMIN" -> true; // 管理员拥有所有权限
            case "USER" -> "READ".equals(permission) || "WRITE".equals(permission); // 用户有读写权限
            case "GUEST" -> "READ".equals(permission); // 访客只有读权限
            default -> false;
        };
    }
    
    /**
     * 演示安全配置信息
     */
    public void showSecurityConfiguration() {
        log.info("\n--- Spring Security 配置说明 ---");
        log.info("1. @EnableWebSecurity - 启用Web安全配置");
        log.info("2. @EnableMethodSecurity - 启用方法级安全");
        log.info("3. SecurityFilterChain - 配置安全过滤器链");
        log.info("4. AuthenticationManager - 认证管理器");
        log.info("5. PasswordEncoder - 密码编码器");
        log.info("6. UserDetailsService - 用户详情服务");
        
        log.info("\n--- 常用注解 ---");
        log.info("@PreAuthorize - 方法执行前权限验证");
        log.info("@PostAuthorize - 方法执行后权限验证");
        log.info("@Secured - 角色权限验证");
        log.info("@RolesAllowed - JSR-250角色验证");
    }
}