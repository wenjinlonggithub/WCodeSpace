package com.architecture.designpattern.proxy;

import java.util.HashMap;
import java.util.Map;

public class ProxyExample {
    
    public void demonstratePattern() {
        System.out.println("=== 代理模式演示 ===");
        
        // 虚拟代理演示
        System.out.println("1. 虚拟代理演示:");
        Image image1 = new ImageProxy("photo1.jpg");
        Image image2 = new ImageProxy("photo2.jpg");
        
        System.out.println("第一次显示:");
        image1.display(); // 这时才真正加载
        System.out.println("第二次显示:");
        image1.display(); // 直接显示，不再加载
        
        // 保护代理演示
        System.out.println("\n2. 保护代理演示:");
        FileAccess adminAccess = new FileAccessProxy("admin", "admin123");
        FileAccess userAccess = new FileAccessProxy("user", "user123");
        
        adminAccess.readFile("config.txt");
        adminAccess.writeFile("config.txt", "new config");
        
        userAccess.readFile("data.txt");
        userAccess.writeFile("data.txt", "new data"); // 权限不足
        
        // 缓存代理演示
        System.out.println("\n3. 缓存代理演示:");
        WebService webService = new WebServiceProxy();
        
        System.out.println("第一次请求:");
        String result1 = webService.request("api/users");
        System.out.println("结果: " + result1);
        
        System.out.println("第二次请求:");
        String result2 = webService.request("api/users");
        System.out.println("结果: " + result2);
    }
}

// 1. 虚拟代理示例 - 图片加载
interface Image {
    void display();
}

class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();
    }
    
    private void loadFromDisk() {
        System.out.println("📷 从磁盘加载图片: " + filename);
        // 模拟耗时操作
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void display() {
        System.out.println("🖼️ 显示图片: " + filename);
    }
}

class ImageProxy implements Image {
    private RealImage realImage;
    private String filename;
    
    public ImageProxy(String filename) {
        this.filename = filename;
    }
    
    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}

// 2. 保护代理示例 - 文件访问控制
interface FileAccess {
    void readFile(String filename);
    void writeFile(String filename, String content);
}

class RealFileAccess implements FileAccess {
    @Override
    public void readFile(String filename) {
        System.out.println("📖 读取文件: " + filename);
    }
    
    @Override
    public void writeFile(String filename, String content) {
        System.out.println("✏️ 写入文件: " + filename + " 内容: " + content);
    }
}

class FileAccessProxy implements FileAccess {
    private RealFileAccess realFileAccess;
    private String username;
    private String password;
    
    public FileAccessProxy(String username, String password) {
        this.username = username;
        this.password = password;
        this.realFileAccess = new RealFileAccess();
    }
    
    private boolean authenticate() {
        return "admin".equals(username) && "admin123".equals(password);
    }
    
    @Override
    public void readFile(String filename) {
        System.out.println("🔐 验证用户权限: " + username);
        realFileAccess.readFile(filename);
    }
    
    @Override
    public void writeFile(String filename, String content) {
        System.out.println("🔐 验证用户权限: " + username);
        if (authenticate()) {
            realFileAccess.writeFile(filename, content);
        } else {
            System.out.println("❌ 权限不足，无法写入文件");
        }
    }
}

// 3. 缓存代理示例 - Web服务
interface WebService {
    String request(String url);
}

class RealWebService implements WebService {
    @Override
    public String request(String url) {
        System.out.println("🌐 发起网络请求: " + url);
        // 模拟网络延迟
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "来自 " + url + " 的数据";
    }
}

class WebServiceProxy implements WebService {
    private RealWebService realWebService;
    private Map<String, String> cache = new HashMap<>();
    
    public WebServiceProxy() {
        this.realWebService = new RealWebService();
    }
    
    @Override
    public String request(String url) {
        if (cache.containsKey(url)) {
            System.out.println("💾 从缓存返回数据: " + url);
            return cache.get(url);
        }
        
        String result = realWebService.request(url);
        cache.put(url, result);
        System.out.println("💾 数据已缓存");
        return result;
    }
}