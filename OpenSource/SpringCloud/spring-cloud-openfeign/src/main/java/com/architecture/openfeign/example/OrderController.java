package com.architecture.openfeign.example;

import com.architecture.openfeign.UserDTO;
import com.architecture.openfeign.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单服务 - 使用Feign调用用户服务
 *
 * 业务场景：
 * 用户下单时，需要验证用户信息、查询用户等级等
 *
 * Feign优势体现：
 * 1. 不需要手动构建HTTP请求
 * 2. 自动负载均衡到多个用户服务实例
 * 3. 自动熔断降级
 * 4. 统一异常处理
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 创建订单
     *
     * 业务流程：
     * 1. 调用用户服务验证用户
     * 2. 查询用户等级（决定折扣）
     * 3. 创建订单
     * 4. 扣减库存
     * 5. 创建支付单
     */
    @PostMapping
    public String createOrder(@RequestBody CreateOrderRequest request) {
        // 1. 验证用户（Feign调用）
        UserDTO user = userServiceClient.getUserById(request.getUserId());

        if (user == null || "未知用户".equals(user.getUsername())) {
            return "用户不存在或用户服务不可用";
        }

        // 2. 根据用户等级计算折扣
        double discount = calculateDiscount(user);

        // 3. 创建订单逻辑
        String orderId = "ORDER-" + System.currentTimeMillis();

        return String.format(
            "订单创建成功！订单号: %s, 用户: %s, 折扣: %.2f",
            orderId, user.getUsername(), discount
        );
    }

    /**
     * 查询订单（需要用户信息）
     */
    @GetMapping("/{orderId}")
    public OrderDetailDTO getOrderDetail(@PathVariable String orderId) {
        // 模拟查询订单
        Long userId = 1001L;

        // Feign调用获取用户信息
        UserDTO user = userServiceClient.getUserById(userId);

        return new OrderDetailDTO(
            orderId,
            user.getUsername(),
            user.getEmail(),
            "商品A x 2",
            299.00
        );
    }

    /**
     * 批量查询用户订单
     *
     * 场景：管理后台查看所有用户的订单
     */
    @GetMapping("/all")
    public List<UserDTO> getAllUsersWithOrders() {
        // Feign调用获取所有用户
        List<UserDTO> users = userServiceClient.getAllUsers();

        // 为每个用户查询订单（实际应该用批量接口）
        return users;
    }

    /**
     * 根据用户等级计算折扣
     */
    private double calculateDiscount(UserDTO user) {
        // 简化逻辑：根据用户年龄计算（实际应该根据会员等级）
        if (user.getAge() >= 60) {
            return 0.8;  // 8折
        } else if (user.getAge() >= 18) {
            return 0.9;  // 9折
        }
        return 1.0;  // 无折扣
    }
}

/**
 * 创建订单请求
 */
class CreateOrderRequest {
    private Long userId;
    private String productId;
    private Integer quantity;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

/**
 * 订单详情DTO
 */
class OrderDetailDTO {
    private String orderId;
    private String userName;
    private String userEmail;
    private String productInfo;
    private Double totalAmount;

    public OrderDetailDTO(String orderId, String userName, String userEmail,
                          String productInfo, Double totalAmount) {
        this.orderId = orderId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.productInfo = productInfo;
        this.totalAmount = totalAmount;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getProductInfo() { return productInfo; }
    public Double getTotalAmount() { return totalAmount; }
}

/**
 * 完整业务流程示例：
 *
 * 用户下单 -> 订单服务
 *   │
 *   ├─> Feign调用用户服务 -> 验证用户
 *   ├─> Feign调用商品服务 -> 检查库存
 *   ├─> Feign调用库存服务 -> 扣减库存
 *   └─> Feign调用支付服务 -> 创建支付单
 *
 * 容错机制：
 * - 用户服务不可用 -> 返回默认用户，记录日志
 * - 库存服务超时 -> 重试3次，失败则回滚
 * - 支付服务异常 -> 熔断降级，返回友好提示
 *
 * 性能优化：
 * - 批量接口：一次调用获取多个用户信息
 * - 缓存：缓存用户信息，减少调用次数
 * - 异步调用：非关键信息异步获取
 */
