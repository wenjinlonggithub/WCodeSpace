package com.architecture.designpattern.template;

public class TemplateMethodExample {
    
    public void demonstratePattern() {
        System.out.println("=== 模板方法模式演示 ===");
        
        // 饮料制作演示
        System.out.println("1. 饮料制作演示:");
        
        Beverage tea = new Tea();
        Beverage coffee = new Coffee();
        
        System.out.println("制作茶:");
        tea.prepareRecipe();
        
        System.out.println("\n制作咖啡:");
        coffee.prepareRecipe();
        
        // 数据挖掘演示
        System.out.println("\n2. 数据挖掘演示:");
        
        DataMiner csvMiner = new CSVDataMiner();
        DataMiner dbMiner = new DatabaseDataMiner();
        DataMiner pdfMiner = new PDFDataMiner();
        
        System.out.println("CSV数据挖掘:");
        csvMiner.mineData("data.csv");
        
        System.out.println("\n数据库数据挖掘:");
        dbMiner.mineData("user_table");
        
        System.out.println("\nPDF数据挖掘:");
        pdfMiner.mineData("report.pdf");
    }
}

// 1. 饮料制作示例
abstract class Beverage {
    
    // 模板方法 - 定义算法骨架
    public final void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        if (customerWantsCondiments()) {
            addCondiments();
        }
    }
    
    // 具体方法 - 在基类中实现
    private void boilWater() {
        System.out.println("🔥 烧开水");
    }
    
    private void pourInCup() {
        System.out.println("☕ 倒入杯中");
    }
    
    // 抽象方法 - 子类必须实现
    protected abstract void brew();
    protected abstract void addCondiments();
    
    // 钩子方法 - 子类可以选择覆盖
    protected boolean customerWantsCondiments() {
        return true;
    }
}

class Tea extends Beverage {
    @Override
    protected void brew() {
        System.out.println("🍃 用沸水浸泡茶叶");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("🍋 加柠檬");
    }
    
    @Override
    protected boolean customerWantsCondiments() {
        return getUserInput().toLowerCase().startsWith("y");
    }
    
    private String getUserInput() {
        // 模拟用户输入
        return "yes";
    }
}

class Coffee extends Beverage {
    @Override
    protected void brew() {
        System.out.println("☕ 用沸水冲泡咖啡");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("🥛 加糖和牛奶");
    }
    
    @Override
    protected boolean customerWantsCondiments() {
        return getUserInput().toLowerCase().startsWith("y");
    }
    
    private String getUserInput() {
        // 模拟用户输入
        return "no";
    }
}

// 2. 数据挖掘示例
abstract class DataMiner {
    
    // 模板方法
    public final void mineData(String path) {
        openFile(path);
        byte[] rawData = extractData();
        Data data = parseData(rawData);
        Data analysis = analyzeData(data);
        sendReport(analysis);
        closeFile();
    }
    
    // 通用步骤
    private void openFile(String path) {
        System.out.println("📁 打开文件: " + path);
    }
    
    private void closeFile() {
        System.out.println("📁 关闭文件");
    }
    
    private void sendReport(Data analysis) {
        System.out.println("📊 发送分析报告: " + analysis.getResult());
    }
    
    // 抽象方法 - 子类实现
    protected abstract byte[] extractData();
    protected abstract Data parseData(byte[] rawData);
    
    // 钩子方法 - 默认实现，子类可覆盖
    protected Data analyzeData(Data data) {
        System.out.println("🔍 执行默认数据分析");
        return new Data("默认分析结果: " + data.getContent());
    }
}

class CSVDataMiner extends DataMiner {
    @Override
    protected byte[] extractData() {
        System.out.println("📊 从CSV文件提取数据");
        return "csv,data,here".getBytes();
    }
    
    @Override
    protected Data parseData(byte[] rawData) {
        System.out.println("📊 解析CSV格式数据");
        return new Data("CSV解析后的数据");
    }
}

class DatabaseDataMiner extends DataMiner {
    @Override
    protected byte[] extractData() {
        System.out.println("🗄️ 从数据库提取数据");
        return "db_data_here".getBytes();
    }
    
    @Override
    protected Data parseData(byte[] rawData) {
        System.out.println("🗄️ 解析数据库查询结果");
        return new Data("数据库解析后的数据");
    }
    
    @Override
    protected Data analyzeData(Data data) {
        System.out.println("🔍 执行高级数据库数据分析");
        return new Data("高级分析结果: " + data.getContent());
    }
}

class PDFDataMiner extends DataMiner {
    @Override
    protected byte[] extractData() {
        System.out.println("📄 从PDF文件提取数据");
        return "pdf_content_here".getBytes();
    }
    
    @Override
    protected Data parseData(byte[] rawData) {
        System.out.println("📄 解析PDF格式数据");
        return new Data("PDF解析后的数据");
    }
}

// 数据类
class Data {
    private String content;
    
    public Data(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getResult() {
        return content;
    }
}