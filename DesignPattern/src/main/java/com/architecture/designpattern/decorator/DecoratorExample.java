package com.architecture.designpattern.decorator;

public class DecoratorExample {
    
    public void demonstratePattern() {
        System.out.println("=== 装饰者模式演示 ===");
        
        // 基础咖啡
        System.out.println("1. 基础咖啡:");
        Beverage espresso = new Espresso();
        System.out.println(espresso.getDescription() + " $" + espresso.cost());
        
        // 加调料的咖啡
        System.out.println("\n2. 加调料的咖啡:");
        Beverage darkRoast = new DarkRoast();
        darkRoast = new Mocha(darkRoast);
        darkRoast = new Mocha(darkRoast);
        darkRoast = new Whip(darkRoast);
        System.out.println(darkRoast.getDescription() + " $" + darkRoast.cost());
        
        // 另一杯咖啡
        System.out.println("\n3. 另一杯咖啡:");
        Beverage houseBlend = new HouseBlend();
        houseBlend = new Soy(houseBlend);
        houseBlend = new Mocha(houseBlend);
        houseBlend = new Whip(houseBlend);
        System.out.println(houseBlend.getDescription() + " $" + houseBlend.cost());
        
        // I/O装饰者演示
        System.out.println("\n=== I/O装饰者模式演示 ===");
        DataSource plainText = new PlainText("Hello World!");
        System.out.println("原始数据: " + plainText.readData());
        
        DataSource encrypted = new EncryptionDecorator(plainText);
        System.out.println("加密后: " + encrypted.readData());
        
        DataSource compressed = new CompressionDecorator(encrypted);
        System.out.println("压缩后: " + compressed.readData());
    }
}

// 饮料组件
abstract class Beverage {
    protected String description = "未知饮料";
    
    public String getDescription() {
        return description;
    }
    
    public abstract double cost();
}

// 具体饮料
class Espresso extends Beverage {
    public Espresso() {
        description = "意式浓缩咖啡";
    }
    
    @Override
    public double cost() {
        return 1.99;
    }
}

class HouseBlend extends Beverage {
    public HouseBlend() {
        description = "招牌混合咖啡";
    }
    
    @Override
    public double cost() {
        return 0.89;
    }
}

class DarkRoast extends Beverage {
    public DarkRoast() {
        description = "深焙咖啡";
    }
    
    @Override
    public double cost() {
        return 0.99;
    }
}

// 调料装饰者
abstract class CondimentDecorator extends Beverage {
    protected Beverage beverage;
    
    public abstract String getDescription();
}

// 具体装饰者
class Mocha extends CondimentDecorator {
    public Mocha(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", 摩卡";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.20;
    }
}

class Whip extends CondimentDecorator {
    public Whip(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", 奶泡";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.10;
    }
}

class Soy extends CondimentDecorator {
    public Soy(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", 豆浆";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.15;
    }
}

// I/O装饰者示例
interface DataSource {
    void writeData(String data);
    String readData();
}

// 基础数据源
class PlainText implements DataSource {
    private String data;
    
    public PlainText(String data) {
        this.data = data;
    }
    
    @Override
    public void writeData(String data) {
        this.data = data;
    }
    
    @Override
    public String readData() {
        return data;
    }
}

// 基础装饰者
abstract class DataSourceDecorator implements DataSource {
    protected DataSource dataSource;
    
    public DataSourceDecorator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void writeData(String data) {
        dataSource.writeData(data);
    }
    
    @Override
    public String readData() {
        return dataSource.readData();
    }
}

// 加密装饰者
class EncryptionDecorator extends DataSourceDecorator {
    public EncryptionDecorator(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public void writeData(String data) {
        super.writeData(encrypt(data));
    }
    
    @Override
    public String readData() {
        return decrypt(super.readData());
    }
    
    private String encrypt(String data) {
        // 简单加密（实际应用中应使用真正的加密算法）
        return "encrypted(" + data + ")";
    }
    
    private String decrypt(String data) {
        // 简单解密
        if (data.startsWith("encrypted(") && data.endsWith(")")) {
            return data.substring(10, data.length() - 1);
        }
        return data;
    }
}

// 压缩装饰者
class CompressionDecorator extends DataSourceDecorator {
    public CompressionDecorator(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public void writeData(String data) {
        super.writeData(compress(data));
    }
    
    @Override
    public String readData() {
        return decompress(super.readData());
    }
    
    private String compress(String data) {
        // 简单压缩模拟
        return "compressed(" + data + ")";
    }
    
    private String decompress(String data) {
        // 简单解压缩
        if (data.startsWith("compressed(") && data.endsWith(")")) {
            return data.substring(11, data.length() - 1);
        }
        return data;
    }
}