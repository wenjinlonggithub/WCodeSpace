package com.architecture.designpattern.strategy;

public class StrategyExample {
    
    public void demonstratePattern() {
        System.out.println("=== 策略模式演示 ===");
        
        // 支付策略演示
        System.out.println("1. 支付策略:");
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(new Item("笔记本电脑", 5000.0));
        cart.addItem(new Item("鼠标", 100.0));
        
        // 使用信用卡支付
        cart.setPaymentStrategy(new CreditCardPayment("1234-5678-9012-3456"));
        cart.checkout();
        
        // 使用支付宝支付
        cart.setPaymentStrategy(new AlipayPayment("user@example.com"));
        cart.checkout();
        
        // 排序策略演示
        System.out.println("\n2. 排序策略:");
        SortContext sortContext = new SortContext();
        int[] data = {64, 34, 25, 12, 22, 11, 90};
        
        System.out.println("原始数据: " + java.util.Arrays.toString(data.clone()));
        
        sortContext.setSortStrategy(new BubbleSort());
        sortContext.sort(data.clone());
        
        sortContext.setSortStrategy(new QuickSort());
        sortContext.sort(data.clone());
        
        // 压缩策略演示
        System.out.println("\n3. 压缩策略:");
        CompressionContext context = new CompressionContext();
        String text = "这是一个需要压缩的文本文件内容";
        
        context.setCompressionStrategy(new ZipCompression());
        context.compressFile(text);
        
        context.setCompressionStrategy(new RarCompression());
        context.compressFile(text);
    }
}

// 商品类
class Item {
    private String name;
    private double price;
    
    public Item(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
}

// 支付策略接口
interface PaymentStrategy {
    void pay(double amount);
}

// 具体支付策略
class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    
    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("使用信用卡 " + cardNumber + " 支付 " + amount + " 元");
    }
}

class AlipayPayment implements PaymentStrategy {
    private String account;
    
    public AlipayPayment(String account) {
        this.account = account;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("使用支付宝账户 " + account + " 支付 " + amount + " 元");
    }
}

class WechatPayment implements PaymentStrategy {
    private String phoneNumber;
    
    public WechatPayment(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("使用微信 " + phoneNumber + " 支付 " + amount + " 元");
    }
}

// 购物车
class ShoppingCart {
    private java.util.List<Item> items = new java.util.ArrayList<>();
    private PaymentStrategy paymentStrategy;
    
    public void addItem(Item item) {
        items.add(item);
    }
    
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    public void checkout() {
        double total = items.stream().mapToDouble(Item::getPrice).sum();
        paymentStrategy.pay(total);
    }
}

// 排序策略接口
interface SortStrategy {
    void sort(int[] array);
}

// 具体排序策略
class BubbleSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("使用冒泡排序");
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        System.out.println("排序结果: " + java.util.Arrays.toString(array));
    }
}

class QuickSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("使用快速排序");
        quickSort(array, 0, array.length - 1);
        System.out.println("排序结果: " + java.util.Arrays.toString(array));
    }
    
    private void quickSort(int[] array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);
            quickSort(array, low, pi - 1);
            quickSort(array, pi + 1, high);
        }
    }
    
    private int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int i = (low - 1);
        
        for (int j = low; j < high; j++) {
            if (array[j] <= pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
        
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        
        return i + 1;
    }
}

// 排序上下文
class SortContext {
    private SortStrategy sortStrategy;
    
    public void setSortStrategy(SortStrategy sortStrategy) {
        this.sortStrategy = sortStrategy;
    }
    
    public void sort(int[] array) {
        sortStrategy.sort(array);
    }
}

// 压缩策略接口
interface CompressionStrategy {
    void compress(String fileName);
}

// 具体压缩策略
class ZipCompression implements CompressionStrategy {
    @Override
    public void compress(String fileName) {
        System.out.println("使用ZIP格式压缩文件: " + fileName);
    }
}

class RarCompression implements CompressionStrategy {
    @Override
    public void compress(String fileName) {
        System.out.println("使用RAR格式压缩文件: " + fileName);
    }
}

class GzipCompression implements CompressionStrategy {
    @Override
    public void compress(String fileName) {
        System.out.println("使用GZIP格式压缩文件: " + fileName);
    }
}

// 压缩上下文
class CompressionContext {
    private CompressionStrategy compressionStrategy;
    
    public void setCompressionStrategy(CompressionStrategy compressionStrategy) {
        this.compressionStrategy = compressionStrategy;
    }
    
    public void compressFile(String fileName) {
        compressionStrategy.compress(fileName);
    }
}