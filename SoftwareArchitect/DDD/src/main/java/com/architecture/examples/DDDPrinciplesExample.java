package com.architecture.examples;

import com.architecture.domain.entity.Customer;
import com.architecture.domain.entity.Product;
import com.architecture.domain.entity.ProductCategory;
import com.architecture.domain.valueobject.Address;
import com.architecture.domain.valueobject.Email;
import com.architecture.domain.valueobject.Money;
import com.architecture.domain.aggregate.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * DDD核心概念及设计模式示例
 * 
 * 本类展示了领域驱动设计(DDD)的核心概念和设计模式的实际应用
 */
@Component
public class DDDPrinciplesExample {

    /**
     * 1. 值对象(Value Object)示例
     * 
     * 值对象特点：
     * - 不可变性：一旦创建不能修改
     * - 相等性：基于值而非身份
     * - 无副作用：方法不改变对象状态
     * - 自验证：构造时验证数据有效性
     */
    public void valueObjectExample() {
        System.out.println("=== 值对象示例 ===");
        
        // 创建Money值对象
        Money price1 = new Money(new BigDecimal("100.00"), "CNY");
        Money price2 = new Money(new BigDecimal("100.00"), "CNY");
        
        // 值对象相等性比较
        System.out.println("价格相等: " + price1.equals(price2)); // true
        
        // 值对象不可变性 - 操作返回新对象
        Money totalPrice = price1.add(price2);
        System.out.println("原价格: " + price1); // CNY 100.00
        System.out.println("总价格: " + totalPrice); // CNY 200.00
        
        // Email值对象自验证
        try {
            Email validEmail = new Email("user@example.com");
            System.out.println("有效邮箱: " + validEmail);
            
            // 无效邮箱会抛出异常
            Email invalidEmail = new Email("invalid-email");
        } catch (IllegalArgumentException e) {
            System.out.println("邮箱验证失败: " + e.getMessage());
        }
        
        // Address值对象组合
        Address address = new Address("北京市", "朝阳区", "望京", "阜通东大街6号", "100102");
        System.out.println("完整地址: " + address.getFullAddress());
    }

    /**
     * 2. 实体(Entity)示例
     * 
     * 实体特点：
     * - 唯一标识：通过ID区分
     * - 可变性：状态可以改变
     * - 生命周期：有创建、修改、删除的生命周期
     * - 业务行为：包含业务逻辑方法
     */
    public void entityExample() {
        System.out.println("\n=== 实体示例 ===");
        
        // 创建Product实体
        Money productPrice = new Money(new BigDecimal("299.99"), "CNY");
        Product product = new Product(1L, "iPhone 15", "最新款iPhone", 
                                    productPrice, 100, ProductCategory.ELECTRONICS);
        
        System.out.println("商品名称: " + product.getName());
        System.out.println("初始库存: " + product.getStock());
        System.out.println("商品可用: " + product.isAvailable());
        
        // 实体业务行为 - 减少库存
        product.decreaseStock(10);
        System.out.println("减少库存后: " + product.getStock());
        
        // 实体业务行为 - 更新价格
        Money newPrice = new Money(new BigDecimal("279.99"), "CNY");
        product.updatePrice(newPrice);
        System.out.println("更新后价格: " + product.getPrice());
        
        // 创建Customer实体
        Email customerEmail = new Email("customer@example.com");
        Customer customer = new Customer(1L, "张三", customerEmail, "13800138000");
        
        System.out.println("客户等级: " + customer.getLevel().getDescription());
        
        // 实体业务行为 - 升级等级
        customer.upgradeLevel();
        System.out.println("升级后等级: " + customer.getLevel().getDescription());
    }

    /**
     * 3. 聚合根(Aggregate Root)示例
     * 
     * 聚合根特点：
     * - 一致性边界：保证聚合内数据一致性
     * - 访问入口：外部只能通过聚合根访问聚合内对象
     * - 业务不变量：维护业务规则和约束
     * - 事务边界：一个事务只能修改一个聚合
     */
    public void aggregateRootExample() {
        System.out.println("\n=== 聚合根示例 ===");
        
        // 创建聚合根 - Order
        Email customerEmail = new Email("customer@example.com");
        Customer customer = new Customer(1L, "李四", customerEmail, "13900139000");
        Address shippingAddress = new Address("上海市", "浦东新区", "陆家嘴", "世纪大道100号", "200120");
        
        Order order = new Order(1L, customer, shippingAddress);
        System.out.println("订单状态: " + order.getStatus().getDescription());
        System.out.println("订单总额: " + order.getTotalAmount());
        
        // 通过聚合根添加订单项
        Money productPrice = new Money(new BigDecimal("199.99"), "CNY");
        Product product1 = new Product(1L, "MacBook Pro", "专业笔记本", 
                                     productPrice, 50, ProductCategory.ELECTRONICS);
        
        order.addOrderItem(product1, 2);
        System.out.println("添加商品后总额: " + order.getTotalAmount());
        System.out.println("商品库存变化: " + product1.getStock());
        
        // 聚合根业务行为 - 确认订单
        order.confirm();
        System.out.println("确认后状态: " + order.getStatus().getDescription());
        
        // 聚合根业务行为 - 发货
        order.ship();
        System.out.println("发货后状态: " + order.getStatus().getDescription());
        
        // 聚合根维护业务不变量
        try {
            // 尝试取消已发货的订单会失败
            order.deliver();
            order.cancel(); // 这会抛出异常
        } catch (IllegalStateException e) {
            System.out.println("业务规则验证: " + e.getMessage());
        }
    }

    /**
     * 4. 领域服务(Domain Service)示例
     * 
     * 领域服务特点：
     * - 无状态：不保存状态信息
     * - 跨聚合：处理跨多个聚合的业务逻辑
     * - 业务概念：表达重要的业务概念
     * - 纯业务：不包含技术实现细节
     */
    public void domainServiceExample() {
        System.out.println("\n=== 领域服务示例 ===");
        
        // 价格计算服务示例
        PricingService pricingService = new PricingService();
        
        Money basePrice = new Money(new BigDecimal("1000.00"), "CNY");
        Email customerEmail = new Email("vip@example.com");
        Customer vipCustomer = new Customer(1L, "VIP客户", customerEmail, "13700137000");
        vipCustomer.upgradeLevel(); // 升级为VIP
        
        Money finalPrice = pricingService.calculateFinalPrice(basePrice, vipCustomer);
        System.out.println("基础价格: " + basePrice);
        System.out.println("VIP折扣后价格: " + finalPrice);
        
        // 库存检查服务示例
        InventoryService inventoryService = new InventoryService();
        
        Money productPrice = new Money(new BigDecimal("299.99"), "CNY");
        Product product = new Product(1L, "商品A", "描述", productPrice, 5, ProductCategory.ELECTRONICS);
        
        boolean canFulfill = inventoryService.canFulfillOrder(product, 3);
        System.out.println("可以满足订单(3件): " + canFulfill);
        
        boolean cannotFulfill = inventoryService.canFulfillOrder(product, 10);
        System.out.println("可以满足订单(10件): " + cannotFulfill);
    }

    /**
     * 5. 仓储模式(Repository Pattern)示例
     * 
     * 仓储模式特点：
     * - 抽象数据访问：隐藏数据存储细节
     * - 集合语义：像操作内存集合一样操作数据
     * - 查询封装：封装复杂查询逻辑
     * - 测试友好：便于单元测试
     */
    public void repositoryPatternExample() {
        System.out.println("\n=== 仓储模式示例 ===");
        
        // 注意：这里只是概念演示，实际使用需要依赖注入
        System.out.println("仓储模式提供了统一的数据访问接口：");
        System.out.println("- OrderRepository.findById(id)");
        System.out.println("- CustomerRepository.findAll()");
        System.out.println("- ProductRepository.findByCategory(category)");
        System.out.println("- 隐藏了具体的数据存储实现（内存、数据库、缓存等）");
    }

    /**
     * 6. 工厂模式(Factory Pattern)示例
     * 
     * 工厂模式特点：
     * - 创建封装：封装复杂的对象创建逻辑
     * - 业务语义：体现业务创建规则
     * - 参数验证：统一的创建参数验证
     * - 扩展性：便于扩展新的创建方式
     */
    public void factoryPatternExample() {
        System.out.println("\n=== 工厂模式示例 ===");
        
        OrderFactory orderFactory = new OrderFactory();
        
        // 使用工厂创建订单
        Email customerEmail = new Email("factory@example.com");
        Customer customer = new Customer(1L, "工厂客户", customerEmail, "13600136000");
        
        Order order = orderFactory.createOrder(customer, "北京市", "海淀区", "中关村", "科技大厦", "100080");
        
        System.out.println("通过工厂创建订单: " + order.getId());
        System.out.println("配送地址: " + order.getShippingAddress().getFullAddress());
    }

    // 领域服务示例类
    private static class PricingService {
        public Money calculateFinalPrice(Money basePrice, Customer customer) {
            double discount = customer.getLevel().getDiscount();
            BigDecimal finalAmount = basePrice.getAmount().multiply(BigDecimal.valueOf(discount));
            return new Money(finalAmount, basePrice.getCurrency());
        }
    }

    private static class InventoryService {
        public boolean canFulfillOrder(Product product, int requestedQuantity) {
            return product.getStock() >= requestedQuantity;
        }
    }

    // 工厂示例类
    private static class OrderFactory {
        private static Long orderIdCounter = 1L;
        
        public Order createOrder(Customer customer, String province, String city, 
                               String district, String street, String zipCode) {
            Address shippingAddress = new Address(province, city, district, street, zipCode);
            return new Order(orderIdCounter++, customer, shippingAddress);
        }
    }
}