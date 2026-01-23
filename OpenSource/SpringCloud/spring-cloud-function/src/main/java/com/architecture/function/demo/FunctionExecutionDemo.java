package com.architecture.function.demo;

import com.architecture.function.core.FunctionInvoker;
import com.architecture.function.core.SimpleFunctionCatalog;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * 完整执行流程演示
 *
 * 通过 CommandLineRunner 在应用启动后自动执行
 * 展示 Spring Cloud Function 的完整工作流程
 */
@Component
public class FunctionExecutionDemo implements CommandLineRunner {

    private final SimpleFunctionCatalog functionCatalog;
    private final FunctionInvoker functionInvoker;

    public FunctionExecutionDemo(SimpleFunctionCatalog functionCatalog,
                                  FunctionInvoker functionInvoker) {
        this.functionCatalog = functionCatalog;
        this.functionInvoker = functionInvoker;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n\n");
        System.out.println("████████████████████████████████████████████████████████████████");
        System.out.println("█                                                              █");
        System.out.println("█      Spring Cloud Function 完整执行流程演示                  █");
        System.out.println("█                                                              █");
        System.out.println("████████████████████████████████████████████████████████████████");
        System.out.println("\n");

        // 等待所有 Bean 初始化完成
        Thread.sleep(1000);

        // ========== 演示1: 查看已注册的函数 ==========
        demonstrateFunctionRegistry();

        // ========== 演示2: 单个函数执行 ==========
        demonstrateSingleFunction();

        // ========== 演示3: 函数组合执行 ==========
        demonstrateFunctionComposition();

        // ========== 演示4: 业务函数执行 ==========
        demonstrateBusinessFunction();

        // ========== 演示5: 编程式调用 ==========
        demonstrateProgrammaticInvocation();

        System.out.println("\n");
        System.out.println("████████████████████████████████████████████████████████████████");
        System.out.println("█                                                              █");
        System.out.println("█      演示完成！可以通过 HTTP 调用测试                         █");
        System.out.println("█      例如: POST http://localhost:8080/uppercase              █");
        System.out.println("█                                                              █");
        System.out.println("████████████████████████████████████████████████████████████████");
        System.out.println("\n\n");
    }

    /**
     * 演示1: 查看已注册的函数
     */
    private void demonstrateFunctionRegistry() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  演示1: 查看已注册的函数列表                               │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        System.out.println("已注册的函数：");
        functionCatalog.getFunctionNames().forEach(name -> {
            SimpleFunctionCatalog.FunctionType type = functionCatalog.getFunctionType(name);
            System.out.println("  - " + name + " [" + type + "]");
        });

        System.out.println("\n✓ 演示1完成\n");
        pause();
    }

    /**
     * 演示2: 单个函数执行
     */
    private void demonstrateSingleFunction() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  演示2: 单个函数执行流程                                   │");
        System.out.println("│  函数: uppercase                                           │");
        System.out.println("│  输入: \"hello world\"                                       │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        FunctionInvoker.InvocationResult<Object> result =
            functionInvoker.invoke("uppercase", "hello world", String.class, Object.class);

        if (result.isSuccess()) {
            System.out.println("✓ 执行成功");
            System.out.println("  输入: hello world");
            System.out.println("  输出: " + result.getResult());
            System.out.println("  耗时: " + result.getExecutionTime() + "ms");
        }

        System.out.println("\n✓ 演示2完成\n");
        pause();
    }

    /**
     * 演示3: 函数组合执行
     */
    private void demonstrateFunctionComposition() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  演示3: 函数组合执行流程                                   │");
        System.out.println("│  函数链: uppercase | reverse                               │");
        System.out.println("│  输入: \"hello\"                                             │");
        System.out.println("│  流程: hello → HELLO → OLLEH                               │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        FunctionInvoker.InvocationResult<Object> result =
            functionInvoker.invoke("uppercase|reverse", "hello", String.class, Object.class);

        if (result.isSuccess()) {
            System.out.println("✓ 执行成功");
            System.out.println("  输入: hello");
            System.out.println("  中间结果: HELLO (uppercase)");
            System.out.println("  最终输出: " + result.getResult() + " (reverse)");
            System.out.println("  耗时: " + result.getExecutionTime() + "ms");
        }

        System.out.println("\n✓ 演示3完成\n");
        pause();
    }

    /**
     * 演示4: 业务函数执行
     */
    private void demonstrateBusinessFunction() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  演示4: 业务函数执行 - 积分计算                            │");
        System.out.println("│  函数: calculatePoints                                     │");
        System.out.println("│  输入: 250.0 (消费金额)                                    │");
        System.out.println("│  规则: 每1元=1积分，每满100元奖励10积分                    │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        FunctionInvoker.InvocationResult<Object> result =
            functionInvoker.invoke("calculatePoints", 250.0, Double.class, Object.class);

        if (result.isSuccess()) {
            System.out.println("✓ 执行成功");
            System.out.println("  消费金额: 250.0元");
            System.out.println("  基础积分: 250");
            System.out.println("  奖励积分: 20 (250/100 * 10)");
            System.out.println("  总积分: " + result.getResult());
            System.out.println("  耗时: " + result.getExecutionTime() + "ms");
        }

        System.out.println("\n✓ 演示4完成\n");
        pause();
    }

    /**
     * 演示5: 编程式调用
     */
    private void demonstrateProgrammaticInvocation() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  演示5: 编程式调用（不通过执行引擎）                       │");
        System.out.println("│  直接从 FunctionCatalog 获取函数并调用                     │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        // 直接从 Catalog 获取函数
        Function<String, String> uppercase = functionCatalog.lookup("uppercase");

        // 直接调用
        String result = uppercase.apply("direct call");

        System.out.println("✓ 执行成功");
        System.out.println("  调用方式: 编程式直接调用");
        System.out.println("  输入: direct call");
        System.out.println("  输出: " + result);

        System.out.println("\n✓ 演示5完成\n");
        pause();
    }

    private void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
