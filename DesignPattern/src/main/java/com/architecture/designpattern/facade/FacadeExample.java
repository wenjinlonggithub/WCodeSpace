package com.architecture.designpattern.facade;

public class FacadeExample {
    
    public void demonstratePattern() {
        System.out.println("=== 外观模式演示 ===");
        
        // 家庭影院系统演示
        System.out.println("1. 家庭影院系统演示:");
        HomeTheaterFacade homeTheater = new HomeTheaterFacade();
        
        System.out.println("开始看电影:");
        homeTheater.watchMovie("复仇者联盟");
        
        System.out.println("\n电影结束:");
        homeTheater.endMovie();
        
        // 计算机启动演示
        System.out.println("\n2. 计算机启动演示:");
        ComputerFacade computer = new ComputerFacade();
        
        System.out.println("启动计算机:");
        computer.start();
        
        System.out.println("\n关闭计算机:");
        computer.shutdown();
        
        // 银行服务演示
        System.out.println("\n3. 银行服务演示:");
        BankServiceFacade bankService = new BankServiceFacade();
        
        String accountNumber = "123456789";
        double amount = 1000.0;
        
        System.out.println("处理取款业务:");
        boolean result = bankService.withdraw(accountNumber, amount);
        System.out.println("取款结果: " + (result ? "成功" : "失败"));
        
        System.out.println("\n处理存款业务:");
        result = bankService.deposit(accountNumber, amount);
        System.out.println("存款结果: " + (result ? "成功" : "失败"));
    }
}

// 1. 家庭影院系统示例
// 子系统类
class Amplifier {
    public void on() {
        System.out.println("🔊 功放开启");
    }
    
    public void off() {
        System.out.println("🔊 功放关闭");
    }
    
    public void setVolume(int level) {
        System.out.println("🔊 音量设置为: " + level);
    }
    
    public void setSurroundSound() {
        System.out.println("🔊 环绕声模式开启");
    }
}

class DVDPlayer {
    public void on() {
        System.out.println("📀 DVD播放器开启");
    }
    
    public void off() {
        System.out.println("📀 DVD播放器关闭");
    }
    
    public void play(String movie) {
        System.out.println("📀 播放电影: " + movie);
    }
    
    public void stop() {
        System.out.println("📀 停止播放");
    }
    
    public void eject() {
        System.out.println("📀 弹出光盘");
    }
}

class Projector {
    public void on() {
        System.out.println("📽️ 投影仪开启");
    }
    
    public void off() {
        System.out.println("📽️ 投影仪关闭");
    }
    
    public void wideScreenMode() {
        System.out.println("📽️ 设置为宽屏模式");
    }
}

class TheaterLights {
    public void on() {
        System.out.println("💡 灯光开启");
    }
    
    public void off() {
        System.out.println("💡 灯光关闭");
    }
    
    public void dim(int level) {
        System.out.println("💡 灯光调暗到: " + level + "%");
    }
}

class Screen {
    public void up() {
        System.out.println("🎭 屏幕升起");
    }
    
    public void down() {
        System.out.println("🎭 屏幕放下");
    }
}

class PopcornPopper {
    public void on() {
        System.out.println("🍿 爆米花机开启");
    }
    
    public void off() {
        System.out.println("🍿 爆米花机关闭");
    }
    
    public void pop() {
        System.out.println("🍿 开始制作爆米花");
    }
}

// 外观类
class HomeTheaterFacade {
    private Amplifier amp;
    private DVDPlayer dvd;
    private Projector projector;
    private TheaterLights lights;
    private Screen screen;
    private PopcornPopper popper;
    
    public HomeTheaterFacade() {
        this.amp = new Amplifier();
        this.dvd = new DVDPlayer();
        this.projector = new Projector();
        this.lights = new TheaterLights();
        this.screen = new Screen();
        this.popper = new PopcornPopper();
    }
    
    public void watchMovie(String movie) {
        System.out.println("🎬 准备看电影...");
        popper.on();
        popper.pop();
        lights.dim(10);
        screen.down();
        projector.on();
        projector.wideScreenMode();
        amp.on();
        amp.setVolume(5);
        amp.setSurroundSound();
        dvd.on();
        dvd.play(movie);
        System.out.println("🎬 电影开始，请享受观影时光!");
    }
    
    public void endMovie() {
        System.out.println("🎬 关闭影院系统...");
        popper.off();
        lights.on();
        screen.up();
        projector.off();
        amp.off();
        dvd.stop();
        dvd.eject();
        dvd.off();
        System.out.println("🎬 影院系统已关闭");
    }
}

// 2. 计算机启动示例
// 子系统类
class CPU {
    public void freeze() {
        System.out.println("💻 CPU冻结");
    }
    
    public void jump(long position) {
        System.out.println("💻 CPU跳转到位置: " + position);
    }
    
    public void execute() {
        System.out.println("💻 CPU执行指令");
    }
}

class Memory {
    public void load(long position, byte[] data) {
        System.out.println("💾 内存加载数据到位置: " + position);
    }
}

class HardDrive {
    public byte[] read(long lba, int size) {
        System.out.println("🗃️ 硬盘读取数据: LBA=" + lba + ", 大小=" + size);
        return new byte[size];
    }
}

// 计算机外观类
class ComputerFacade {
    private CPU processor;
    private Memory ram;
    private HardDrive hd;
    
    public ComputerFacade() {
        this.processor = new CPU();
        this.ram = new Memory();
        this.hd = new HardDrive();
    }
    
    public void start() {
        System.out.println("🚀 计算机启动中...");
        processor.freeze();
        ram.load(0, hd.read(0, 1024));
        processor.jump(0);
        processor.execute();
        System.out.println("✅ 计算机启动完成");
    }
    
    public void shutdown() {
        System.out.println("⏹️ 计算机关闭中...");
        // 执行关闭步骤
        System.out.println("💾 保存数据...");
        System.out.println("🔌 断开电源...");
        System.out.println("✅ 计算机已关闭");
    }
}

// 3. 银行服务示例
// 子系统类
class AccountService {
    public boolean validateAccount(String accountNumber) {
        System.out.println("🏦 验证账户: " + accountNumber);
        return accountNumber.length() == 9; // 简单验证
    }
    
    public double getBalance(String accountNumber) {
        System.out.println("💰 查询账户余额: " + accountNumber);
        return 5000.0; // 模拟余额
    }
}

class SecurityService {
    public boolean authenticateUser(String accountNumber) {
        System.out.println("🔐 用户身份验证: " + accountNumber);
        return true; // 模拟验证成功
    }
}

class TransactionService {
    public boolean processWithdraw(String accountNumber, double amount) {
        System.out.println("💸 处理取款: 账户" + accountNumber + ", 金额" + amount);
        return true; // 模拟交易成功
    }
    
    public boolean processDeposit(String accountNumber, double amount) {
        System.out.println("💰 处理存款: 账户" + accountNumber + ", 金额" + amount);
        return true; // 模拟交易成功
    }
}

class NotificationService {
    public void sendSMS(String accountNumber, String message) {
        System.out.println("📱 发送短信到账户 " + accountNumber + ": " + message);
    }
    
    public void sendEmail(String accountNumber, String message) {
        System.out.println("📧 发送邮件到账户 " + accountNumber + ": " + message);
    }
}

// 银行服务外观类
class BankServiceFacade {
    private AccountService accountService;
    private SecurityService securityService;
    private TransactionService transactionService;
    private NotificationService notificationService;
    
    public BankServiceFacade() {
        this.accountService = new AccountService();
        this.securityService = new SecurityService();
        this.transactionService = new TransactionService();
        this.notificationService = new NotificationService();
    }
    
    public boolean withdraw(String accountNumber, double amount) {
        System.out.println("🏦 开始取款流程...");
        
        // 验证账户
        if (!accountService.validateAccount(accountNumber)) {
            System.out.println("❌ 账户验证失败");
            return false;
        }
        
        // 身份验证
        if (!securityService.authenticateUser(accountNumber)) {
            System.out.println("❌ 身份验证失败");
            return false;
        }
        
        // 检查余额
        double balance = accountService.getBalance(accountNumber);
        if (balance < amount) {
            System.out.println("❌ 余额不足");
            return false;
        }
        
        // 处理交易
        boolean success = transactionService.processWithdraw(accountNumber, amount);
        if (success) {
            notificationService.sendSMS(accountNumber, "取款成功: " + amount + "元");
            System.out.println("✅ 取款完成");
            return true;
        }
        
        return false;
    }
    
    public boolean deposit(String accountNumber, double amount) {
        System.out.println("🏦 开始存款流程...");
        
        // 验证账户
        if (!accountService.validateAccount(accountNumber)) {
            System.out.println("❌ 账户验证失败");
            return false;
        }
        
        // 身份验证
        if (!securityService.authenticateUser(accountNumber)) {
            System.out.println("❌ 身份验证失败");
            return false;
        }
        
        // 处理交易
        boolean success = transactionService.processDeposit(accountNumber, amount);
        if (success) {
            notificationService.sendEmail(accountNumber, "存款成功: " + amount + "元");
            System.out.println("✅ 存款完成");
            return true;
        }
        
        return false;
    }
}