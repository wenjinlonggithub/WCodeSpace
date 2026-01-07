package com.architecture.designpattern.bridge;

public class BridgeExample {
    
    public void demonstratePattern() {
        System.out.println("=== 桥接模式演示 ===");
        
        // 图形绘制演示
        System.out.println("1. 图形绘制演示:");
        
        // 创建不同的绘制API
        DrawingAPI api1 = new DrawingAPI1();
        DrawingAPI api2 = new DrawingAPI2();
        
        // 使用不同API绘制圆形
        Shape circle1 = new Circle(5, 10, 15, api1);
        Shape circle2 = new Circle(20, 30, 25, api2);
        
        circle1.draw();
        circle2.draw();
        
        circle1.resizeByPercentage(2.0);
        circle2.resizeByPercentage(1.5);
        
        // 消息发送演示
        System.out.println("\n2. 消息发送演示:");
        
        // 创建不同的发送方式
        MessageSender emailSender = new EmailSender();
        MessageSender smsSender = new SMSSender();
        MessageSender pushSender = new PushNotificationSender();
        
        // 创建不同类型的消息
        Message textMessage = new TextMessage("Hello World!", emailSender);
        Message encryptedMessage = new EncryptedMessage("Secret Message", smsSender);
        Message urgentMessage = new UrgentMessage("Emergency!", pushSender);
        
        textMessage.send();
        encryptedMessage.send();
        urgentMessage.send();
        
        // 设备控制演示
        System.out.println("\n3. 设备控制演示:");
        
        // 创建不同的设备
        Device tv = new TV();
        Device radio = new Radio();
        
        // 创建不同的遥控器
        //RemoteControl basicRemote = new BasicRemoteControl(tv);
        //RemoteControl advancedRemote = new AdvancedRemoteControl(radio);
        
        //basicRemote.turnOn();
        //basicRemote.setVolume(50);
        
        //((AdvancedRemoteControl) advancedRemote).setChannel(5);
        //advancedRemote.turnOff();
    }
}

// 1. 图形绘制示例
// 实现接口（桥接的实现部分）
interface DrawingAPI {
    void drawCircle(int x, int y, int radius);
    void drawLine(int x1, int y1, int x2, int y2);
}

// 具体实现A
class DrawingAPI1 implements DrawingAPI {
    @Override
    public void drawCircle(int x, int y, int radius) {
        System.out.println("API1绘制圆形: 中心(" + x + ", " + y + "), 半径" + radius);
    }
    
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        System.out.println("API1绘制线条: 从(" + x1 + ", " + y1 + ")到(" + x2 + ", " + y2 + ")");
    }
}

// 具体实现B
class DrawingAPI2 implements DrawingAPI {
    @Override
    public void drawCircle(int x, int y, int radius) {
        System.out.println("★ API2高级绘制圆形: 中心坐标(" + x + ", " + y + "), 半径=" + radius + " ★");
    }
    
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        System.out.println("★ API2高级绘制线条: 起点(" + x1 + ", " + y1 + ") → 终点(" + x2 + ", " + y2 + ") ★");
    }
}

// 抽象类（桥接的抽象部分）
abstract class Shape {
    protected DrawingAPI drawingAPI;
    
    protected Shape(DrawingAPI drawingAPI) {
        this.drawingAPI = drawingAPI;
    }
    
    public abstract void draw();
    public abstract void resizeByPercentage(double percentage);
}

// 扩展抽象类
class Circle extends Shape {
    private int x, y, radius;
    
    public Circle(int x, int y, int radius, DrawingAPI drawingAPI) {
        super(drawingAPI);
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    @Override
    public void draw() {
        drawingAPI.drawCircle(x, y, radius);
    }
    
    @Override
    public void resizeByPercentage(double percentage) {
        radius = (int) (radius * percentage);
        System.out.println("圆形大小调整为原来的 " + percentage + " 倍");
        draw();
    }
}

// 2. 消息发送示例
// 实现接口
interface MessageSender {
    void sendMessage(String message);
}

// 具体实现
class EmailSender implements MessageSender {
    @Override
    public void sendMessage(String message) {
        System.out.println("📧 通过邮件发送: " + message);
    }
}

class SMSSender implements MessageSender {
    @Override
    public void sendMessage(String message) {
        System.out.println("📱 通过短信发送: " + message);
    }
}

class PushNotificationSender implements MessageSender {
    @Override
    public void sendMessage(String message) {
        System.out.println("🔔 通过推送通知发送: " + message);
    }
}

// 抽象消息类
abstract class Message {
    protected MessageSender messageSender;
    protected String content;
    
    public Message(String content, MessageSender messageSender) {
        this.content = content;
        this.messageSender = messageSender;
    }
    
    public abstract void send();
}

// 扩展消息类
class TextMessage extends Message {
    public TextMessage(String content, MessageSender messageSender) {
        super(content, messageSender);
    }
    
    @Override
    public void send() {
        System.out.println("[文本消息]");
        messageSender.sendMessage(content);
    }
}

class EncryptedMessage extends Message {
    public EncryptedMessage(String content, MessageSender messageSender) {
        super(content, messageSender);
    }
    
    @Override
    public void send() {
        System.out.println("[加密消息]");
        String encryptedContent = "ENCRYPTED:" + content;
        messageSender.sendMessage(encryptedContent);
    }
}

class UrgentMessage extends Message {
    public UrgentMessage(String content, MessageSender messageSender) {
        super(content, messageSender);
    }
    
    @Override
    public void send() {
        System.out.println("[紧急消息]");
        String urgentContent = "🚨 URGENT: " + content + " 🚨";
        messageSender.sendMessage(urgentContent);
    }
}

// 3. 设备控制示例
// 设备接口（实现部分）
interface Device {
    boolean isEnabled();
    void enable();
    void disable();
    int getVolume();
    void setVolume(int volume);
    int getChannel();
    void setChannel(int channel);
    void printStatus();
}

// 具体设备
class TV implements Device {
    private boolean on = false;
    private int volume = 30;
    private int channel = 1;
    
    @Override
    public boolean isEnabled() { return on; }
    
    @Override
    public void enable() {
        on = true;
        System.out.println("电视已开启");
    }
    
    @Override
    public void disable() {
        on = false;
        System.out.println("电视已关闭");
    }
    
    @Override
    public int getVolume() { return volume; }
    
    @Override
    public void setVolume(int volume) {
        if (volume > 100) volume = 100;
        if (volume < 0) volume = 0;
        this.volume = volume;
        System.out.println("电视音量设置为: " + volume);
    }
    
    @Override
    public int getChannel() { return channel; }
    
    @Override
    public void setChannel(int channel) {
        this.channel = channel;
        System.out.println("电视频道切换到: " + channel);
    }
    
    @Override
    public void printStatus() {
        System.out.println("电视状态: " + (on ? "开启" : "关闭") + 
                          ", 音量: " + volume + ", 频道: " + channel);
    }
}

class Radio implements Device {
    private boolean on = false;
    private int volume = 50;
    private int channel = 1;
    
    @Override
    public boolean isEnabled() { return on; }
    
    @Override
    public void enable() {
        on = true;
        System.out.println("收音机已开启");
    }
    
    @Override
    public void disable() {
        on = false;
        System.out.println("收音机已关闭");
    }
    
    @Override
    public int getVolume() { return volume; }
    
    @Override
    public void setVolume(int volume) {
        if (volume > 100) volume = 100;
        if (volume < 0) volume = 0;
        this.volume = volume;
        System.out.println("收音机音量设置为: " + volume);
    }
    
    @Override
    public int getChannel() { return channel; }
    
    @Override
    public void setChannel(int channel) {
        this.channel = channel;
        System.out.println("收音机频道切换到: " + channel);
    }
    
    @Override
    public void printStatus() {
        System.out.println("收音机状态: " + (on ? "开启" : "关闭") + 
                          ", 音量: " + volume + ", 频道: " + channel);
    }
}

// 遥控器抽象类（抽象部分）
class RemoteControl {
    protected Device device;
    
    public RemoteControl(Device device) {
        this.device = device;
    }
    
    public void turnOn() {
        device.enable();
    }
    
    public void turnOff() {
        device.disable();
    }
    
    public void setVolume(int volume) {
        device.setVolume(volume);
    }
}

// 高级遥控器
class AdvancedRemoteControl extends RemoteControl {
    public AdvancedRemoteControl(Device device) {
        super(device);
    }
    
    public void setChannel(int channel) {
        device.setChannel(channel);
    }
    
    public void mute() {
        System.out.println("设备静音");
        device.setVolume(0);
    }
}