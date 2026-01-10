package com.architecture.principles;

/**
 * 依赖倒置原则 (Dependency Inversion Principle - DIP) 详解
 * 
 * 核心思想：
 * 1. 高层模块不应该依赖低层模块，两者都应该依赖抽象
 * 2. 抽象不应该依赖细节，细节应该依赖抽象
 * 
 * 核心原理：
 * - 控制反转 (IoC): 将对象的创建和依赖关系的管理交给外部容器
 * - 依赖注入 (DI): 通过构造函数、setter或接口注入依赖
 * - 面向接口编程: 依赖抽象而非具体实现
 * 
 * 优势：
 * 1. 降低耦合度 - 模块间松散耦合
 * 2. 提高可测试性 - 易于mock和单元测试
 * 3. 增强可扩展性 - 新增实现无需修改现有代码
 * 4. 提升可维护性 - 修改实现不影响其他模块
 * 5. 支持多态 - 运行时动态选择实现
 * 
 * 应用场景：
 * - 数据访问层 (DAO/Repository模式)
 * - 服务层解耦
 * - 第三方服务集成
 * - 配置管理
 * - 缓存策略
 * - 消息队列
 * - 支付网关
 * 
 * 常见面试题：
 * Q1: DIP与IoC、DI的关系？
 * A: DIP是设计原则，IoC是设计思想，DI是实现技术
 * 
 * Q2: 如何在Spring中实现DIP？
 * A: 通过@Autowired、@Component、@Service等注解实现依赖注入
 * 
 * Q3: DIP的缺点？
 * A: 增加代码复杂度，过度抽象可能导致理解困难
 */

// ============= 违反DIP的例子 =============

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
        this.database = new MySQLDatabase(); // 紧耦合
    }
    
    public void saveUser(String userData) {
        database.save(userData);
    }
    
    public String getUser(String userId) {
        return database.read(userId);
    }
}

// ============= 遵循DIP的例子 =============

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