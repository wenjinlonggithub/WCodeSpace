package com.architecture.designpattern.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 经典策略模式示例
 * 包含：计算器策略、支付策略、折扣策略等常见应用场景
 */
public class ClassicExamples {

    // ================ 计算器策略示例 ================
    
    /**
     * 计算策略接口
     */
    interface CalculationStrategy extends Strategy<CalculationInput, Double> {
    }
    
    /**
     * 计算输入参数
     */
    static class CalculationInput {
        private final double a;
        private final double b;
        
        public CalculationInput(double a, double b) {
            this.a = a;
            this.b = b;
        }
        
        public double getA() { return a; }
        public double getB() { return b; }
        
        @Override
        public String toString() {
            return String.format("%.2f, %.2f", a, b);
        }
    }
    
    /**
     * 加法策略
     */
    static class AdditionStrategy implements CalculationStrategy {
        @Override
        public Double execute(CalculationInput input) {
            double result = input.getA() + input.getB();
            System.out.println(String.format("加法运算: %.2f + %.2f = %.2f", 
                input.getA(), input.getB(), result));
            return result;
        }
        
        @Override
        public String getName() {
            return "ADD";
        }
        
        @Override
        public String getDescription() {
            return "加法运算策略";
        }
    }
    
    /**
     * 减法策略
     */
    static class SubtractionStrategy implements CalculationStrategy {
        @Override
        public Double execute(CalculationInput input) {
            double result = input.getA() - input.getB();
            System.out.println(String.format("减法运算: %.2f - %.2f = %.2f", 
                input.getA(), input.getB(), result));
            return result;
        }
        
        @Override
        public String getName() {
            return "SUB";
        }
        
        @Override
        public String getDescription() {
            return "减法运算策略";
        }
    }
    
    /**
     * 乘法策略
     */
    static class MultiplicationStrategy implements CalculationStrategy {
        @Override
        public Double execute(CalculationInput input) {
            double result = input.getA() * input.getB();
            System.out.println(String.format("乘法运算: %.2f × %.2f = %.2f", 
                input.getA(), input.getB(), result));
            return result;
        }
        
        @Override
        public String getName() {
            return "MUL";
        }
        
        @Override
        public String getDescription() {
            return "乘法运算策略";
        }
    }
    
    /**
     * 除法策略
     */
    static class DivisionStrategy implements CalculationStrategy {
        @Override
        public Double execute(CalculationInput input) {
            if (input.getB() == 0) {
                throw new ArithmeticException("除数不能为0");
            }
            double result = input.getA() / input.getB();
            System.out.println(String.format("除法运算: %.2f ÷ %.2f = %.2f", 
                input.getA(), input.getB(), result));
            return result;
        }
        
        @Override
        public String getName() {
            return "DIV";
        }
        
        @Override
        public String getDescription() {
            return "除法运算策略";
        }
    }
    
    /**
     * 计算器类
     */
    static class Calculator {
        private final StrategyRegistry<CalculationInput, Double> strategies;
        
        public Calculator() {
            this.strategies = new StrategyRegistry<>();
            initializeStrategies();
        }
        
        private void initializeStrategies() {
            strategies.register(new AdditionStrategy());
            strategies.register(new SubtractionStrategy());
            strategies.register(new MultiplicationStrategy());
            strategies.register(new DivisionStrategy());
        }
        
        public double calculate(String operation, double a, double b) {
            CalculationStrategy strategy = (CalculationStrategy) strategies.get(operation);
            if (strategy == null) {
                throw new UnsupportedOperationException("不支持的运算: " + operation);
            }
            return strategy.execute(new CalculationInput(a, b));
        }
        
        public void printSupportedOperations() {
            System.out.println("支持的运算操作:");
            for (String name : strategies.getAllNames()) {
                CalculationStrategy strategy = (CalculationStrategy) strategies.get(name);
                System.out.println("- " + name + ": " + strategy.getDescription());
            }
        }
    }

    // ================ 支付策略示例 ================
    
    /**
     * 支付请求
     */
    static class PaymentRequest {
        private final String orderId;
        private final BigDecimal amount;
        private final String currency;
        private final Map<String, Object> metadata;
        
        public PaymentRequest(String orderId, BigDecimal amount, String currency) {
            this.orderId = orderId;
            this.amount = amount;
            this.currency = currency;
            this.metadata = new ConcurrentHashMap<>();
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        // 添加元数据
        public PaymentRequest addMetadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }
        
        @Override
        public String toString() {
            return String.format("PaymentRequest{orderId='%s', amount=%s %s}", 
                orderId, amount, currency);
        }
    }
    
    /**
     * 支付结果
     */
    static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;
        private final BigDecimal actualAmount;
        
        public PaymentResult(boolean success, String transactionId, String message, BigDecimal actualAmount) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
            this.actualAmount = actualAmount;
        }
        
        // 静态工厂方法
        public static PaymentResult success(String transactionId, BigDecimal amount) {
            return new PaymentResult(true, transactionId, "支付成功", amount);
        }
        
        public static PaymentResult failure(String message) {
            return new PaymentResult(false, null, message, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public String getMessage() { return message; }
        public BigDecimal getActualAmount() { return actualAmount; }
        
        @Override
        public String toString() {
            return String.format("PaymentResult{success=%s, transactionId='%s', message='%s', amount=%s}", 
                success, transactionId, message, actualAmount);
        }
    }
    
    /**
     * 支付策略接口
     */
    interface PaymentStrategy extends Strategy<PaymentRequest, PaymentResult> {
        /**
         * 支持的最小金额
         */
        BigDecimal getMinAmount();
        
        /**
         * 支持的最大金额
         */
        BigDecimal getMaxAmount();
        
        /**
         * 支持的货币
         */
        java.util.Set<String> getSupportedCurrencies();
        
        /**
         * 手续费率
         */
        BigDecimal getFeeRate();
    }
    
    /**
     * 抽象支付策略
     */
    abstract static class AbstractPaymentStrategy extends AbstractStrategy<PaymentRequest, PaymentResult> 
            implements PaymentStrategy {
        
        protected final BigDecimal minAmount;
        protected final BigDecimal maxAmount;
        protected final java.util.Set<String> supportedCurrencies;
        protected final BigDecimal feeRate;
        
        protected AbstractPaymentStrategy(String name, String description, BigDecimal minAmount, 
                BigDecimal maxAmount, java.util.Set<String> supportedCurrencies, BigDecimal feeRate) {
            super(name, description);
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.supportedCurrencies = supportedCurrencies;
            this.feeRate = feeRate;
        }
        
        @Override
        protected void beforeExecute(PaymentRequest input) {
            System.out.println(String.format("开始处理%s支付: %s", getName(), input));
            validatePayment(input);
        }
        
        @Override
        protected void afterExecute(PaymentRequest input, PaymentResult result) {
            System.out.println(String.format("%s支付处理完成: %s", getName(), result));
        }
        
        protected void validatePayment(PaymentRequest request) {
            // 验证金额范围
            if (request.getAmount().compareTo(minAmount) < 0) {
                throw new IllegalArgumentException(
                    String.format("支付金额%.2f低于最小限额%.2f", request.getAmount(), minAmount));
            }
            if (request.getAmount().compareTo(maxAmount) > 0) {
                throw new IllegalArgumentException(
                    String.format("支付金额%.2f超过最大限额%.2f", request.getAmount(), maxAmount));
            }
            
            // 验证货币
            if (!supportedCurrencies.contains(request.getCurrency())) {
                throw new IllegalArgumentException(
                    String.format("不支持的货币类型: %s", request.getCurrency()));
            }
        }
        
        protected BigDecimal calculateActualAmount(PaymentRequest request) {
            BigDecimal fee = request.getAmount().multiply(feeRate);
            return request.getAmount().add(fee).setScale(2, RoundingMode.HALF_UP);
        }
        
        @Override
        public BigDecimal getMinAmount() { return minAmount; }
        @Override
        public BigDecimal getMaxAmount() { return maxAmount; }
        @Override
        public java.util.Set<String> getSupportedCurrencies() { return supportedCurrencies; }
        @Override
        public BigDecimal getFeeRate() { return feeRate; }
    }
    
    /**
     * 支付宝支付策略
     */
    static class AlipayStrategy extends AbstractPaymentStrategy {
        
        public AlipayStrategy() {
            super("ALIPAY", "支付宝支付", 
                new BigDecimal("0.01"), 
                new BigDecimal("50000.00"),
                java.util.Set.of("CNY", "USD"), 
                new BigDecimal("0.006")); // 0.6% 手续费
        }
        
        @Override
        protected PaymentResult doExecute(PaymentRequest request) {
            try {
                // 模拟支付宝API调用
                Thread.sleep(100); // 模拟网络延迟
                
                String transactionId = "ALIPAY_" + System.currentTimeMillis();
                BigDecimal actualAmount = calculateActualAmount(request);
                
                // 模拟95%的成功率
                if (Math.random() < 0.95) {
                    return PaymentResult.success(transactionId, actualAmount);
                } else {
                    return PaymentResult.failure("支付宝网络异常，请稍后重试");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PaymentResult.failure("支付处理被中断");
            }
        }
    }
    
    /**
     * 微信支付策略
     */
    static class WechatPayStrategy extends AbstractPaymentStrategy {
        
        public WechatPayStrategy() {
            super("WECHAT", "微信支付", 
                new BigDecimal("0.01"), 
                new BigDecimal("200000.00"),
                java.util.Set.of("CNY"), 
                new BigDecimal("0.006")); // 0.6% 手续费
        }
        
        @Override
        protected PaymentResult doExecute(PaymentRequest request) {
            try {
                // 模拟微信支付API调用
                Thread.sleep(150); // 模拟网络延迟
                
                String transactionId = "WXPAY_" + System.currentTimeMillis();
                BigDecimal actualAmount = calculateActualAmount(request);
                
                // 模拟98%的成功率
                if (Math.random() < 0.98) {
                    return PaymentResult.success(transactionId, actualAmount);
                } else {
                    return PaymentResult.failure("微信支付系统繁忙，请稍后重试");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PaymentResult.failure("支付处理被中断");
            }
        }
    }
    
    /**
     * 银行卡支付策略
     */
    static class BankCardStrategy extends AbstractPaymentStrategy {
        
        public BankCardStrategy() {
            super("BANK_CARD", "银行卡支付", 
                new BigDecimal("1.00"), 
                new BigDecimal("500000.00"),
                java.util.Set.of("CNY", "USD", "EUR"), 
                new BigDecimal("0.008")); // 0.8% 手续费
        }
        
        @Override
        protected PaymentResult doExecute(PaymentRequest request) {
            try {
                // 模拟银行接口调用
                Thread.sleep(300); // 模拟银行系统延迟
                
                String transactionId = "BANK_" + System.currentTimeMillis();
                BigDecimal actualAmount = calculateActualAmount(request);
                
                // 模拟92%的成功率
                if (Math.random() < 0.92) {
                    return PaymentResult.success(transactionId, actualAmount);
                } else {
                    return PaymentResult.failure("银行卡余额不足或其他银行系统错误");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PaymentResult.failure("支付处理被中断");
            }
        }
    }
    
    /**
     * 支付处理器
     */
    static class PaymentProcessor {
        private final StrategyRegistry<PaymentRequest, PaymentResult> paymentStrategies;
        private final ConditionalStrategySelector<PaymentRequest, PaymentResult> autoSelector;
        
        public PaymentProcessor() {
            this.paymentStrategies = new StrategyRegistry<>();
            this.autoSelector = new ConditionalStrategySelector<>();
            initializeStrategies();
            setupAutoSelection();
        }
        
        private void initializeStrategies() {
            paymentStrategies.register(new AlipayStrategy());
            paymentStrategies.register(new WechatPayStrategy());
            paymentStrategies.register(new BankCardStrategy());
        }
        
        private void setupAutoSelection() {
            // 根据金额自动选择支付方式
            autoSelector
                .when(req -> req.getAmount().compareTo(new BigDecimal("10000")) > 0, 
                     paymentStrategies.get("BANK_CARD"))
                .when(req -> req.getAmount().compareTo(new BigDecimal("1000")) > 0, 
                     paymentStrategies.get("ALIPAY"))
                .otherwise(paymentStrategies.get("WECHAT"));
        }
        
        /**
         * 指定策略支付
         */
        public PaymentResult pay(String strategyName, PaymentRequest request) {
            PaymentStrategy strategy = (PaymentStrategy) paymentStrategies.get(strategyName);
            if (strategy == null) {
                return PaymentResult.failure("不支持的支付方式: " + strategyName);
            }
            return strategy.execute(request);
        }
        
        /**
         * 自动选择策略支付
         */
        public PaymentResult autoPayment(PaymentRequest request) {
            return autoSelector.execute(request);
        }
        
        /**
         * 获取支持的支付方式
         */
        public void printSupportedMethods() {
            System.out.println("支持的支付方式:");
            for (String name : paymentStrategies.getAllNames()) {
                PaymentStrategy strategy = (PaymentStrategy) paymentStrategies.get(name);
                System.out.println(String.format("- %s: %s (手续费: %.1f%%, 限额: %.2f - %.2f %s)", 
                    name, strategy.getDescription(), 
                    strategy.getFeeRate().multiply(new BigDecimal("100")),
                    strategy.getMinAmount(), strategy.getMaxAmount(),
                    String.join(",", strategy.getSupportedCurrencies())));
            }
        }
    }

    // ================ 演示方法 ================
    
    /**
     * 演示计算器策略
     */
    public static void demonstrateCalculator() {
        System.out.println("========== 计算器策略模式演示 ==========");
        
        Calculator calculator = new Calculator();
        
        // 显示支持的操作
        calculator.printSupportedOperations();
        System.out.println();
        
        // 执行各种计算
        try {
            calculator.calculate("ADD", 10.5, 5.2);
            calculator.calculate("SUB", 15.8, 3.3);
            calculator.calculate("MUL", 4.5, 2.0);
            calculator.calculate("DIV", 20.0, 4.0);
            
            // 测试异常情况
            calculator.calculate("DIV", 10.0, 0.0);
        } catch (Exception e) {
            System.out.println("计算错误: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 演示支付策略
     */
    public static void demonstratePayment() {
        System.out.println("========== 支付策略模式演示 ==========");
        
        PaymentProcessor processor = new PaymentProcessor();
        
        // 显示支持的支付方式
        processor.printSupportedMethods();
        System.out.println();
        
        // 创建不同金额的支付请求
        PaymentRequest smallOrder = new PaymentRequest("ORDER001", new BigDecimal("299.99"), "CNY");
        PaymentRequest mediumOrder = new PaymentRequest("ORDER002", new BigDecimal("2999.99"), "CNY");
        PaymentRequest largeOrder = new PaymentRequest("ORDER003", new BigDecimal("19999.99"), "CNY");
        
        // 指定策略支付
        System.out.println("=== 指定策略支付 ===");
        PaymentResult result1 = processor.pay("ALIPAY", smallOrder);
        PaymentResult result2 = processor.pay("WECHAT", mediumOrder);
        PaymentResult result3 = processor.pay("BANK_CARD", largeOrder);
        
        System.out.println();
        
        // 自动策略选择支付
        System.out.println("=== 自动策略选择支付 ===");
        PaymentResult autoResult1 = processor.autoPayment(smallOrder);
        PaymentResult autoResult2 = processor.autoPayment(mediumOrder);
        PaymentResult autoResult3 = processor.autoPayment(largeOrder);
        
        System.out.println();
    }
    
    /**
     * 主演示方法
     */
    public static void main(String[] args) {
        demonstrateCalculator();
        demonstratePayment();
    }
}