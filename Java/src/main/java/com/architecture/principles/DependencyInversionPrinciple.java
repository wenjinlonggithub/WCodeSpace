package com.architecture.principles;

/**
 * 依赖倒置原则 (DIP) 示例
 * 
 * 核心思想：
 * 1. 高层模块不应该依赖低层模块，两者都应该依赖抽象
 * 2. 抽象不应该依赖细节，细节应该依赖抽象
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
    }
}