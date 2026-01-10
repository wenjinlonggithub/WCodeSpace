package com.architecture.principles;

import java.util.HashMap;
import java.util.Map;

/**
 * 依赖倒置原则 (Dependency Inversion Principle - DIP) 深度解析
 * 
 * ==================== 为什么叫"依赖倒置"？ ====================
 * 
 * 【历史背景】
 * 1990年代，面向对象编程兴起，但软件系统变得越来越复杂。
 * 传统的"自顶向下"设计导致了严重的依赖问题：
 * - 高层业务逻辑直接依赖底层技术实现
 * - 系统像"倒金字塔"一样不稳定
 * - 任何底层变化都会影响整个系统
 * 
 * 【"倒置"的含义】
 * 传统依赖方向：高层 → 低层 (Business → Database)
 * 倒置后依赖方向：高层 ← 抽象 → 低层 (Business ← Interface → Database)
 * 
 * 这就是"倒置"的核心：依赖关系被"翻转"了！
 * 
 * ==================== 软件工程历史演进 ====================
 * 
 * 【1960-1970年代：结构化编程】
 * - 问题：意大利面条式代码，goto语句满天飞
 * - 解决：引入函数、模块化
 * - 局限：仍然是自顶向下的依赖
 * 
 * 【1980-1990年代：面向对象编程】
 * - 问题：类之间紧耦合，继承滥用
 * - 解决：封装、继承、多态
 * - 局限：依赖关系仍然混乱
 * 
 * 【1990年代：设计模式兴起】
 * - GoF设计模式（1994年）
 * - 开始关注对象间的协作关系
 * - 但缺乏系统性的设计原则
 * 
 * 【1995年：Robert C. Martin提出DIP】
 * - 作为SOLID原则的一部分
 * - 系统性解决依赖关系问题
 * - 为后来的IoC容器奠定理论基础
 * 
 * 【2000年代：IoC容器爆发】
 * - Spring Framework (2003年)
 * - 依赖注入成为主流
 * - 企业级应用的标准架构
 * 
 * ==================== 传统依赖关系的问题 ====================
 * 
 * 【问题1：脆弱性 (Fragility)】
 * 底层改动导致上层崩溃，就像多米诺骨牌效应
 * 
 * 【问题2：僵化性 (Rigidity)】
 * 难以修改和扩展，每次改动都牵一发而动全身
 * 
 * 【问题3：不可移植性 (Immobility)】
 * 代码无法重用，因为紧耦合到特定实现
 * 
 * 【问题4：粘滞性 (Viscosity)】
 * 做正确的事比做错误的事更困难
 * 
 * ==================== DIP的演进路径 ====================
 * 
 * 【第一阶段：认识问题】
 * 传统方式 → 发现耦合问题 → 寻求解决方案
 * 
 * 【第二阶段：引入抽象】
 * 具体依赖 → 接口抽象 → 依赖接口而非实现
 * 
 * 【第三阶段：控制反转】
 * 主动创建 → 被动接收 → 外部注入依赖
 * 
 * 【第四阶段：容器化管理】
 * 手动注入 → 自动注入 → IoC容器管理
 * 
 * 【第五阶段：现代化实现】
 * 配置文件 → 注解驱动 → 约定优于配置
 * 
 * ==================== 解决的核心问题 ====================
 * 
 * 【业务问题】
 * 1. 需求变更频繁，系统难以适应
 * 2. 不同环境需要不同实现（开发/测试/生产）
 * 3. 第三方服务更换成本高
 * 4. 单元测试困难，无法mock依赖
 * 
 * 【技术问题】
 * 1. 编译时依赖导致的循环依赖
 * 2. 部署时的版本冲突
 * 3. 运行时的性能问题
 * 4. 维护时的影响范围不可控
 * 
 * 【团队问题】
 * 1. 不同团队开发的模块难以集成
 * 2. 代码审查时影响范围难以评估
 * 3. 新人理解系统架构困难
 * 4. 重构风险高，不敢轻易改动
 */

// ============= 历史演进的代码示例 =============

// 【1960年代：结构化编程风格】
class StructuredProgrammingExample {
    // 所有逻辑都在一个地方，自顶向下
    public void processOrder() {
        // 直接调用底层函数
        connectToDatabase();
        validateOrder();
        calculatePrice();
        saveToDatabase();
        sendEmail();
        printReceipt();
    }
    
    private void connectToDatabase() { /* 直接MySQL连接 */ }
    private void validateOrder() { /* 硬编码验证规则 */ }
    private void calculatePrice() { /* 固定计算逻辑 */ }
    private void saveToDatabase() { /* 直接SQL操作 */ }
    private void sendEmail() { /* 直接SMTP调用 */ }
    private void printReceipt() { /* 直接打印机调用 */ }
    
    // 问题：任何底层改动都要修改这个函数
    // 无法测试、无法重用、难以维护
}

// 【1980年代：早期面向对象风格】
class EarlyOOPExample {
    private MySQLConnection database;
    private SMTPMailer mailer;
    private HPPrinter printer;
    
    public EarlyOOPExample() {
        // 仍然是硬编码依赖
        this.database = new MySQLConnection();
        this.mailer = new SMTPMailer();
        this.printer = new HPPrinter();
    }
    
    public void processOrder() {
        database.connect();
        // ... 业务逻辑
        mailer.send("订单确认邮件");
        printer.print("订单收据");
    }
    
    // 问题：虽然有了对象，但依然紧耦合
    // 换个数据库？要修改代码重新编译
}

// 【1990年代：设计模式时期】
class DesignPatternExample {
    // 开始使用工厂模式
    private DatabaseConnection database;
    private Mailer mailer;
    
    public DesignPatternExample() {
        // 使用工厂创建对象
        this.database = DatabaseFactory.createConnection();
        this.mailer = MailerFactory.createMailer();
    }
    
    public void processOrder() {
        database.connect();
        database.save("订单数据");
        mailer.send("订单确认邮件");
    }
    
    // 进步：通过工厂模式降低了一些耦合
    // 问题：仍然需要主动获取依赖，配置复杂
}

// 【2000年代：DIP + IoC容器时期】
class ModernDIPExample {
    private final DatabaseConnection database;
    private final Mailer mailer;
    private final Printer printer;
    
    // 依赖注入：不再主动创建，被动接收
    public ModernDIPExample(DatabaseConnection database, 
                           Mailer mailer, 
                           Printer printer) {
        this.database = database;
        this.mailer = mailer;
        this.printer = printer;
    }
    
    public void processOrder() {
        // 业务逻辑与具体实现完全解耦
        database.save("订单数据");
        mailer.send("订单确认邮件");
        printer.print("订单收据");
    }
    
    // 优势：完全解耦，易测试，易扩展
}

// ============= 依赖关系的"倒置"可视化 =============

/*
传统依赖关系（自顶向下）：
┌─────────────────┐
│   OrderService  │ ──┐
└─────────────────┘   │
                      ▼
┌─────────────────┐   │
│  PaymentService │ ──┤
└─────────────────┘   │
                      ▼
┌─────────────────┐   │
│ DatabaseService │ ──┘
└─────────────────┘

问题：上层直接依赖下层，下层变化影响上层

倒置后的依赖关系：
┌─────────────────┐
│   OrderService  │
└─────────────────┘
         │ 依赖
         ▼
┌─────────────────┐
│   <<Interface>> │ ◄─── 抽象层
│  PaymentGateway │
└─────────────────┘
         ▲ 实现
         │
┌─────────────────┐
│ AlipayPayment   │ ◄─── 具体实现
└─────────────────┘

优势：高层和低层都依赖抽象，彼此独立变化
*/

// 【违反DIP的餐厅】- 直接依赖具体厨师
class ZhangChef {
    public void cookSichuanFood() {
        System.out.println("张师傅做川菜：麻婆豆腐、宫保鸡丁");
    }
}

class LiChef {
    public void cookCantonFood() {
        System.out.println("李师傅做粤菜：白切鸡、蒸蛋");
    }
}

// 餐厅直接依赖具体厨师 - 问题很大！
class BadRestaurant {
    private ZhangChef zhangChef;  // 只能做川菜
    private LiChef liChef;        // 只能做粤菜
    
    public BadRestaurant() {
        this.zhangChef = new ZhangChef();  // 写死了依赖
        this.liChef = new LiChef();        // 写死了依赖
    }
    
    public void serveSichuanFood() {
        zhangChef.cookSichuanFood();  // 张师傅离职就完蛋了
    }
    
    public void serveCantonFood() {
        liChef.cookCantonFood();      // 李师傅离职也完蛋了
    }
}

// ============= 违反DIP的技术例子 =============

// 低层模块 - 具体实现
class MySQLDatabase {
    public void save(String data) {
        System.out.println("Saving data to MySQL: " + data);
    }
    
    public String read(String id) {
        return "Data from MySQL with id: " + id;
    }
}

// 高层模块直接依赖低层模块 - 违反DIP
class BadUserService {
    private MySQLDatabase database; // 直接依赖具体实现
    
    public BadUserService() {
        this.database = new MySQLDatabase(); // 紧耦合 - 问题所在！
    }
    
    public void saveUser(String userData) {
        database.save(userData);
    }
    
    public String getUser(String userId) {
        return database.read(userId);
    }
    
    // 问题演示：如果要换成PostgreSQL怎么办？
    // 1. 必须修改这个类的代码
    // 2. 重新编译
    // 3. 重新测试
    // 4. 如果有100个这样的Service类，就要改100次！
}

// ============= 遵循DIP的餐厅例子 =============

// 【遵循DIP的餐厅】- 依赖抽象接口
interface Chef {
    void cook(String dishName);
    String getSpecialty();
}

// 具体厨师实现接口
class SichuanChef implements Chef {
    @Override
    public void cook(String dishName) {
        System.out.println("川菜师傅制作：" + dishName + " (麻辣鲜香)");
    }
    
    @Override
    public String getSpecialty() {
        return "川菜";
    }
}

class CantonChef implements Chef {
    @Override
    public void cook(String dishName) {
        System.out.println("粤菜师傅制作：" + dishName + " (清淡鲜美)");
    }
    
    @Override
    public String getSpecialty() {
        return "粤菜";
    }
}

class FrenchChef implements Chef {
    @Override
    public void cook(String dishName) {
        System.out.println("法式主厨制作：" + dishName + " (精致浪漫)");
    }
    
    @Override
    public String getSpecialty() {
        return "法式料理";
    }
}

// 智能餐厅 - 依赖抽象，不依赖具体厨师
class SmartRestaurant {
    private Chef chef;  // 依赖抽象接口
    
    // 构造函数注入 - 可以传入任何实现Chef接口的厨师
    public SmartRestaurant(Chef chef) {
        this.chef = chef;
    }
    
    // Setter注入 - 可以随时更换厨师
    public void setChef(Chef chef) {
        this.chef = chef;
        System.out.println("餐厅现在有" + chef.getSpecialty() + "师傅了！");
    }
    
    public void serveDish(String dishName) {
        System.out.println("客人点餐：" + dishName);
        chef.cook(dishName);
        System.out.println("上菜完成！");
    }
    
    // 优势演示：
    // 1. 厨师离职？没问题，换个同类型厨师就行
    // 2. 想增加新菜系？没问题，招个新厨师就行
    // 3. 想临时换厨师？没问题，setChef就行
    // 4. 餐厅代码完全不用改！
}

// 抽象层 - 定义接口
interface Database {
    void save(String data);
    String read(String id);
    void delete(String id);
}

// 低层模块 - 具体实现依赖抽象
class MySQLDatabaseImpl implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving data to MySQL: " + data);
    }
    
    @Override
    public String read(String id) {
        return "Data from MySQL with id: " + id;
    }
    
    @Override
    public void delete(String id) {
        System.out.println("Deleting data from MySQL with id: " + id);
    }
}

class PostgreSQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving data to PostgreSQL: " + data);
    }
    
    @Override
    public String read(String id) {
        return "Data from PostgreSQL with id: " + id;
    }
    
    @Override
    public void delete(String id) {
        System.out.println("Deleting data from PostgreSQL with id: " + id);
    }
}

class MongoDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving data to MongoDB: " + data);
    }
    
    @Override
    public String read(String id) {
        return "Data from MongoDB with id: " + id;
    }
    
    @Override
    public void delete(String id) {
        System.out.println("Deleting data from MongoDB with id: " + id);
    }
}

// 高层模块依赖抽象 - 遵循DIP
class UserService {
    private Database database; // 依赖抽象接口
    
    // 依赖注入 - 构造函数注入
    public UserService(Database database) {
        this.database = database;
    }
    
    // Setter注入
    public void setDatabase(Database database) {
        this.database = database;
    }
    
    public void saveUser(String userData) {
        // 业务逻辑
        System.out.println("Processing user data...");
        database.save(userData);
        System.out.println("User saved successfully");
    }
    
    public String getUser(String userId) {
        System.out.println("Retrieving user with id: " + userId);
        return database.read(userId);
    }
    
    public void deleteUser(String userId) {
        System.out.println("Deleting user with id: " + userId);
        database.delete(userId);
    }
}

// 简单的依赖注入容器
class DIContainer {
    public static UserService createUserService(String databaseType) {
        Database database;
        
        switch (databaseType.toLowerCase()) {
            case "mysql":
                database = new MySQLDatabaseImpl();
                break;
            case "postgresql":
                database = new PostgreSQLDatabase();
                break;
            case "mongodb":
                database = new MongoDatabase();
                break;
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
        
        return new UserService(database);
    }
}

// 更复杂的例子 - 多层依赖倒置
interface Logger {
    void log(String message);
}

interface EmailService {
    void sendEmail(String to, String subject, String body);
}

class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

class FileLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("[FILE LOG] " + message);
    }
}

class SMTPEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Sending email via SMTP to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}

class MockEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Mock email sent to: " + to);
    }
}

// 高层业务服务依赖多个抽象
class NotificationService {
    private Logger logger;
    private EmailService emailService;
    private Database database;
    
    public NotificationService(Logger logger, EmailService emailService, Database database) {
        this.logger = logger;
        this.emailService = emailService;
        this.database = database;
    }
    
    public void sendWelcomeNotification(String userId, String email) {
        logger.log("Sending welcome notification to user: " + userId);
        
        String userData = database.read(userId);
        logger.log("Retrieved user data: " + userData);
        
        emailService.sendEmail(email, "Welcome!", "Welcome to our platform!");
        
        logger.log("Welcome notification sent successfully");
    }
}

public class DependencyInversionPrinciple {
    public static void main(String[] args) {
        System.out.println("=== Dependency Inversion Principle Demo ===");
        
        // ============= 历史演进演示 =============
        System.out.println("\n=== 软件工程历史演进 ===");
        
        System.out.println("\n--- 1960年代：结构化编程 ---");
        StructuredProgrammingExample structuredExample = new StructuredProgrammingExample();
        structuredExample.processOrder();
        System.out.println("问题：所有逻辑耦合在一起，难以维护和测试");
        
        System.out.println("\n--- 1980年代：早期面向对象 ---");
        EarlyOOPExample oopExample = new EarlyOOPExample();
        oopExample.processOrder();
        System.out.println("问题：虽然有了对象，但仍然硬编码依赖");
        
        System.out.println("\n--- 1990年代：设计模式时期 ---");
        DesignPatternExample patternExample = new DesignPatternExample();
        patternExample.processOrder();
        System.out.println("进步：工厂模式降低了耦合，但配置仍然复杂");
        
        System.out.println("\n--- 2000年代：DIP + IoC容器 ---");
        // 模拟现代DI容器
        DatabaseConnection modernDb = new PostgreSQLConnection();
        Mailer modernMailer = new GmailMailer();
        Printer modernPrinter = new PDFPrinter();
        
        ModernDIPExample modernExample = new ModernDIPExample(modernDb, modernMailer, modernPrinter);
        modernExample.processOrder();
        System.out.println("优势：完全解耦，依赖外部注入，易于测试和扩展");
        
        // ============= 生活化案例演示 =============
        System.out.println("\n=== 餐厅管理案例 ===");
        
        // 创建不同类型的厨师
        Chef sichuanChef = new SichuanChef();
        Chef cantonChef = new CantonChef();
        Chef frenchChef = new FrenchChef();
        
        // 智能餐厅可以使用任何厨师
        SmartRestaurant restaurant = new SmartRestaurant(sichuanChef);
        restaurant.serveDish("麻婆豆腐");
        
        // 轻松更换厨师 - 这就是DIP的威力！
        restaurant.setChef(cantonChef);
        restaurant.serveDish("白切鸡");
        
        restaurant.setChef(frenchChef);
        restaurant.serveDish("法式鹅肝");
        
        System.out.println("\n--- 对比违反DIP的餐厅 ---");
        BadRestaurant badRestaurant = new BadRestaurant();
        badRestaurant.serveSichuanFood();
        badRestaurant.serveCantonFood();
        System.out.println("问题：如果要增加法式料理，必须修改BadRestaurant类代码！");
        
        // ============= 技术案例演示 =============
        
        // 使用不同的数据库实现
        System.out.println("\n--- Using MySQL ---");
        UserService mysqlUserService = DIContainer.createUserService("mysql");
        mysqlUserService.saveUser("John Doe");
        System.out.println(mysqlUserService.getUser("123"));
        
        System.out.println("\n--- Using PostgreSQL ---");
        UserService postgresUserService = DIContainer.createUserService("postgresql");
        postgresUserService.saveUser("Jane Smith");
        System.out.println(postgresUserService.getUser("456"));
        
        System.out.println("\n--- Using MongoDB ---");
        UserService mongoUserService = DIContainer.createUserService("mongodb");
        mongoUserService.saveUser("Bob Johnson");
        System.out.println(mongoUserService.getUser("789"));
        
        System.out.println("\n--- Complex Dependency Injection ---");
        // 组合不同的实现
        Logger logger = new ConsoleLogger();
        EmailService emailService = new SMTPEmailService();
        Database database = new MySQLDatabaseImpl();
        
        NotificationService notificationService = new NotificationService(logger, emailService, database);
        notificationService.sendWelcomeNotification("user123", "user@example.com");
        
        System.out.println("\n--- Using Different Implementations ---");
        // 使用不同的实现组合
        Logger fileLogger = new FileLogger();
        EmailService mockEmailService = new MockEmailService();
        Database mongoDb = new MongoDatabase();
        
        NotificationService testNotificationService = new NotificationService(fileLogger, mockEmailService, mongoDb);
        testNotificationService.sendWelcomeNotification("testuser", "test@example.com");
        
        // ============= 业务实践案例 =============
        System.out.println("\n=== 业务实践案例 ===");
        
        // 案例1: 支付系统 - 多种支付方式
        System.out.println("\n--- 支付系统案例 ---");
        PaymentProcessor alipayProcessor = new PaymentProcessor(new AlipayService());
        PaymentProcessor wechatProcessor = new PaymentProcessor(new WechatPayService());
        
        alipayProcessor.processPayment("100.00", "订单001");
        wechatProcessor.processPayment("200.00", "订单002");
        
        // 案例2: 缓存系统 - 多级缓存
        System.out.println("\n--- 缓存系统案例 ---");
        CacheManager redisCache = new CacheManager(new RedisCache());
        CacheManager memoryCache = new CacheManager(new MemoryCache());
        
        redisCache.cacheUserData("user123", "用户数据");
        memoryCache.cacheUserData("user456", "用户数据");
        
        // 案例3: 消息系统 - 多种消息队列
        System.out.println("\n--- 消息系统案例 ---");
        MessagePublisher kafkaPublisher = new MessagePublisher(new KafkaMessageQueue());
        MessagePublisher rabbitPublisher = new MessagePublisher(new RabbitMQMessageQueue());
        
        kafkaPublisher.publishMessage("用户注册事件", "user_registered");
        rabbitPublisher.publishMessage("订单创建事件", "order_created");
        
        // ============= Spring框架实际应用案例 =============
        System.out.println("\n=== Spring框架DIP应用 ===");
        
        // 模拟Spring的@Autowired依赖注入
        System.out.println("\n--- 模拟Spring依赖注入 ---");
        SpringLikeContainer container = new SpringLikeContainer();
        
        // 注册bean
        container.registerBean("userRepository", new MySQLUserRepository());
        container.registerBean("emailService", new SMTPEmailService());
        container.registerBean("logger", new ConsoleLogger());
        
        // 创建服务并自动注入依赖
        SpringUserService springUserService = container.createBean(SpringUserService.class);
        springUserService.registerUser("张三", "zhangsan@example.com");
        
        System.out.println("\n--- 切换到不同实现 ---");
        // 轻松切换实现
        container.registerBean("userRepository", new MongoUserRepository());
        container.registerBean("emailService", new MockEmailService());
        
        SpringUserService newService = container.createBean(SpringUserService.class);
        newService.registerUser("李四", "lisi@example.com");
    }
}

// ============= Spring框架模拟实现 =============

// 用户仓储接口
interface UserRepository {
    void save(User user);
    User findById(String id);
}

// 用户实体
class User {
    private String name;
    private String email;
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "'}";
    }
}

// MySQL实现
class MySQLUserRepository implements UserRepository {
    @Override
    public void save(User user) {
        System.out.println("MySQL保存用户: " + user);
    }
    
    @Override
    public User findById(String id) {
        return new User("MySQL用户" + id, "mysql@example.com");
    }
}

// MongoDB实现
class MongoUserRepository implements UserRepository {
    @Override
    public void save(User user) {
        System.out.println("MongoDB保存用户: " + user);
    }
    
    @Override
    public User findById(String id) {
        return new User("Mongo用户" + id, "mongo@example.com");
    }
}

// 模拟Spring的Service类
class SpringUserService {
    private UserRepository userRepository;  // 依赖注入
    private EmailService emailService;      // 依赖注入
    private Logger logger;                  // 依赖注入
    
    // 模拟@Autowired构造函数注入
    public SpringUserService(UserRepository userRepository, 
                           EmailService emailService, 
                           Logger logger) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.logger = logger;
    }
    
    public void registerUser(String name, String email) {
        logger.log("开始注册用户: " + name);
        
        User user = new User(name, email);
        userRepository.save(user);
        
        emailService.sendEmail(email, "欢迎注册", "欢迎加入我们的平台！");
        
        logger.log("用户注册完成: " + name);
    }
}

// 简单的IoC容器模拟
class SpringLikeContainer {
    private Map<String, Object> beans = new HashMap<>();
    
    public void registerBean(String name, Object bean) {
        beans.put(name, bean);
        System.out.println("注册Bean: " + name + " -> " + bean.getClass().getSimpleName());
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> type) {
        return (T) beans.get(name);
    }
    
    // 模拟Spring的依赖注入
    public SpringUserService createBean(Class<SpringUserService> clazz) {
        UserRepository userRepository = getBean("userRepository", UserRepository.class);
        EmailService emailService = getBean("emailService", EmailService.class);
        Logger logger = getBean("logger", Logger.class);
        
        System.out.println("自动注入依赖创建SpringUserService");
        return new SpringUserService(userRepository, emailService, logger);
    }
}

// ============= 业务实践案例实现 =============

// 支付系统案例
interface PaymentService {
    boolean pay(String amount, String orderId);
    String getPaymentStatus(String orderId);
}

class AlipayService implements PaymentService {
    @Override
    public boolean pay(String amount, String orderId) {
        System.out.println("支付宝支付: " + amount + "元, 订单: " + orderId);
        return true;
    }
    
    @Override
    public String getPaymentStatus(String orderId) {
        return "支付宝支付成功";
    }
}

class WechatPayService implements PaymentService {
    @Override
    public boolean pay(String amount, String orderId) {
        System.out.println("微信支付: " + amount + "元, 订单: " + orderId);
        return true;
    }
    
    @Override
    public String getPaymentStatus(String orderId) {
        return "微信支付成功";
    }
}

class PaymentProcessor {
    private PaymentService paymentService;
    
    public PaymentProcessor(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    public void processPayment(String amount, String orderId) {
        System.out.println("开始处理支付...");
        boolean success = paymentService.pay(amount, orderId);
        if (success) {
            System.out.println("支付处理完成: " + paymentService.getPaymentStatus(orderId));
        }
    }
}

// 缓存系统案例
interface Cache {
    void put(String key, String value);
    String get(String key);
    void remove(String key);
}

class RedisCache implements Cache {
    @Override
    public void put(String key, String value) {
        System.out.println("Redis缓存存储: " + key + " = " + value);
    }
    
    @Override
    public String get(String key) {
        return "Redis中的数据: " + key;
    }
    
    @Override
    public void remove(String key) {
        System.out.println("从Redis中删除: " + key);
    }
}

class MemoryCache implements Cache {
    @Override
    public void put(String key, String value) {
        System.out.println("内存缓存存储: " + key + " = " + value);
    }
    
    @Override
    public String get(String key) {
        return "内存中的数据: " + key;
    }
    
    @Override
    public void remove(String key) {
        System.out.println("从内存中删除: " + key);
    }
}

class CacheManager {
    private Cache cache;
    
    public CacheManager(Cache cache) {
        this.cache = cache;
    }
    
    public void cacheUserData(String userId, String userData) {
        System.out.println("缓存用户数据...");
        cache.put("user:" + userId, userData);
        System.out.println("用户数据缓存完成");
    }
    
    public String getUserData(String userId) {
        return cache.get("user:" + userId);
    }
}

// 消息队列案例
interface MessageQueue {
    void sendMessage(String message, String topic);
    void subscribe(String topic);
}

class KafkaMessageQueue implements MessageQueue {
    @Override
    public void sendMessage(String message, String topic) {
        System.out.println("Kafka发送消息到主题[" + topic + "]: " + message);
    }
    
    @Override
    public void subscribe(String topic) {
        System.out.println("订阅Kafka主题: " + topic);
    }
}

class RabbitMQMessageQueue implements MessageQueue {
    @Override
    public void sendMessage(String message, String topic) {
        System.out.println("RabbitMQ发送消息到队列[" + topic + "]: " + message);
    }
    
    @Override
    public void subscribe(String topic) {
        System.out.println("订阅RabbitMQ队列: " + topic);
    }
}

class MessagePublisher {
    private MessageQueue messageQueue;
    
    public MessagePublisher(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }
    
    public void publishMessage(String message, String topic) {
        System.out.println("准备发布消息...");
        messageQueue.sendMessage(message, topic);
        System.out.println("消息发布完成");
    }
}

// ============= 历史演进所需的接口和实现 =============

// 现代DIP示例所需的接口
interface DatabaseConnection {
    void save(String data);
    void connect();
}

interface Mailer {
    void send(String message);
}

interface Printer {
    void print(String content);
}

// 具体实现类
class MySQLConnection implements DatabaseConnection {
    @Override
    public void save(String data) {
        System.out.println("MySQL保存数据: " + data);
    }
    
    @Override
    public void connect() {
        System.out.println("连接到MySQL数据库");
    }
}

class PostgreSQLConnection implements DatabaseConnection {
    @Override
    public void save(String data) {
        System.out.println("PostgreSQL保存数据: " + data);
    }
    
    @Override
    public void connect() {
        System.out.println("连接到PostgreSQL数据库");
    }
}

class SMTPMailer implements Mailer {
    @Override
    public void send(String message) {
        System.out.println("SMTP发送邮件: " + message);
    }
}

class GmailMailer implements Mailer {
    @Override
    public void send(String message) {
        System.out.println("Gmail发送邮件: " + message);
    }
}

class HPPrinter implements Printer {
    @Override
    public void print(String content) {
        System.out.println("HP打印机打印: " + content);
    }
}

class PDFPrinter implements Printer {
    @Override
    public void print(String content) {
        System.out.println("PDF打印: " + content);
    }
}

// 工厂类（设计模式时期使用）
class DatabaseFactory {
    public static DatabaseConnection createConnection() {
        // 硬编码配置，仍然有耦合
        return new MySQLConnection();
    }
}

class MailerFactory {
    public static Mailer createMailer() {
        return new SMTPMailer();
    }
}