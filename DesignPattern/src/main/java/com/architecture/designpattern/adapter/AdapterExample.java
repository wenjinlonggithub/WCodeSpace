package com.architecture.designpattern.adapter;

public class AdapterExample {
    
    public void demonstratePattern() {
        System.out.println("=== 适配器模式演示 ===");
        
        // 对象适配器演示
        System.out.println("1. 对象适配器:");
        Target target = new ObjectAdapter(new Adaptee());
        target.request();
        
        // 类适配器演示（通过继承）
        System.out.println("\n2. 类适配器:");
        Target classAdapter = new ClassAdapter();
        classAdapter.request();
        
        // 媒体播放器适配器演示
        System.out.println("\n3. 媒体播放器适配器:");
        MediaPlayer player = new AudioPlayer();
        
        player.play("mp3", "beyond_the_horizon.mp3");
        player.play("mp4", "alone.mp4");
        player.play("vlc", "far_far_away.vlc");
        player.play("avi", "mind_me.avi");
        
        // 数据库适配器演示
        System.out.println("\n4. 数据库适配器:");
        DatabaseClient client = new DatabaseClient();
        client.performDatabaseOperation("MySQL", "SELECT * FROM users");
        client.performDatabaseOperation("PostgreSQL", "SELECT * FROM products");
        client.performDatabaseOperation("MongoDB", "db.users.find()");
    }
}

// 目标接口
interface Target {
    void request();
}

// 需要适配的类
class Adaptee {
    public void specificRequest() {
        System.out.println("被适配者的特殊请求");
    }
}

// 对象适配器
class ObjectAdapter implements Target {
    private Adaptee adaptee;
    
    public ObjectAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
    
    @Override
    public void request() {
        System.out.println("对象适配器转换请求");
        adaptee.specificRequest();
    }
}

// 类适配器（通过继承）
class ClassAdapter extends Adaptee implements Target {
    @Override
    public void request() {
        System.out.println("类适配器转换请求");
        specificRequest();
    }
}

// 媒体播放器示例
interface MediaPlayer {
    void play(String audioType, String fileName);
}

interface AdvancedMediaPlayer {
    void playVlc(String fileName);
    void playMp4(String fileName);
}

// 高级媒体播放器实现
class VlcPlayer implements AdvancedMediaPlayer {
    @Override
    public void playVlc(String fileName) {
        System.out.println("播放vlc文件: " + fileName);
    }
    
    @Override
    public void playMp4(String fileName) {
        // 什么都不做
    }
}

class Mp4Player implements AdvancedMediaPlayer {
    @Override
    public void playVlc(String fileName) {
        // 什么都不做
    }
    
    @Override
    public void playMp4(String fileName) {
        System.out.println("播放mp4文件: " + fileName);
    }
}

// 媒体适配器
class MediaAdapter implements MediaPlayer {
    private AdvancedMediaPlayer advancedMusicPlayer;
    
    public MediaAdapter(String audioType) {
        switch (audioType.toLowerCase()) {
            case "vlc":
                advancedMusicPlayer = new VlcPlayer();
                break;
            case "mp4":
                advancedMusicPlayer = new Mp4Player();
                break;
        }
    }
    
    @Override
    public void play(String audioType, String fileName) {
        switch (audioType.toLowerCase()) {
            case "vlc":
                advancedMusicPlayer.playVlc(fileName);
                break;
            case "mp4":
                advancedMusicPlayer.playMp4(fileName);
                break;
        }
    }
}

// 音频播放器
class AudioPlayer implements MediaPlayer {
    private MediaAdapter mediaAdapter;
    
    @Override
    public void play(String audioType, String fileName) {
        if ("mp3".equalsIgnoreCase(audioType)) {
            System.out.println("播放mp3文件: " + fileName);
        } else if ("vlc".equalsIgnoreCase(audioType) || "mp4".equalsIgnoreCase(audioType)) {
            mediaAdapter = new MediaAdapter(audioType);
            mediaAdapter.play(audioType, fileName);
        } else {
            System.out.println("不支持的媒体格式: " + audioType);
        }
    }
}

// 数据库适配器示例
interface Database {
    void connect();
    void executeQuery(String query);
    void disconnect();
}

// 不同的数据库实现
class MySQLDatabase {
    public void mysqlConnect() {
        System.out.println("连接到MySQL数据库");
    }
    
    public void mysqlQuery(String query) {
        System.out.println("MySQL执行查询: " + query);
    }
    
    public void mysqlDisconnect() {
        System.out.println("断开MySQL连接");
    }
}

class PostgreSQLDatabase {
    public void pgConnect() {
        System.out.println("连接到PostgreSQL数据库");
    }
    
    public void pgExecute(String query) {
        System.out.println("PostgreSQL执行查询: " + query);
    }
    
    public void pgClose() {
        System.out.println("断开PostgreSQL连接");
    }
}

class MongoDatabase {
    public void mongoConnect() {
        System.out.println("连接到MongoDB数据库");
    }
    
    public void mongoFind(String query) {
        System.out.println("MongoDB执行查询: " + query);
    }
    
    public void mongoDisconnect() {
        System.out.println("断开MongoDB连接");
    }
}

// 数据库适配器
class MySQLAdapter implements Database {
    private MySQLDatabase mysqlDatabase;
    
    public MySQLAdapter() {
        this.mysqlDatabase = new MySQLDatabase();
    }
    
    @Override
    public void connect() {
        mysqlDatabase.mysqlConnect();
    }
    
    @Override
    public void executeQuery(String query) {
        mysqlDatabase.mysqlQuery(query);
    }
    
    @Override
    public void disconnect() {
        mysqlDatabase.mysqlDisconnect();
    }
}

class PostgreSQLAdapter implements Database {
    private PostgreSQLDatabase postgresDatabase;
    
    public PostgreSQLAdapter() {
        this.postgresDatabase = new PostgreSQLDatabase();
    }
    
    @Override
    public void connect() {
        postgresDatabase.pgConnect();
    }
    
    @Override
    public void executeQuery(String query) {
        postgresDatabase.pgExecute(query);
    }
    
    @Override
    public void disconnect() {
        postgresDatabase.pgClose();
    }
}

class MongoAdapter implements Database {
    private MongoDatabase mongoDatabase;
    
    public MongoAdapter() {
        this.mongoDatabase = new MongoDatabase();
    }
    
    @Override
    public void connect() {
        mongoDatabase.mongoConnect();
    }
    
    @Override
    public void executeQuery(String query) {
        mongoDatabase.mongoFind(query);
    }
    
    @Override
    public void disconnect() {
        mongoDatabase.mongoDisconnect();
    }
}

// 数据库客户端
class DatabaseClient {
    public void performDatabaseOperation(String dbType, String query) {
        Database database = null;
        
        switch (dbType.toLowerCase()) {
            case "mysql":
                database = new MySQLAdapter();
                break;
            case "postgresql":
                database = new PostgreSQLAdapter();
                break;
            case "mongodb":
                database = new MongoAdapter();
                break;
            default:
                System.out.println("不支持的数据库类型: " + dbType);
                return;
        }
        
        database.connect();
        database.executeQuery(query);
        database.disconnect();
        System.out.println();
    }
}