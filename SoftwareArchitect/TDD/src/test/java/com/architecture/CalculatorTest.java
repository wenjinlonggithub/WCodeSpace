package com.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD示例：计算器测试类
 *
 * TDD核心原理：测试驱动开发（Test-Driven Development）
 *
 * 【开发流程】
 * 1. 红灯（Red）：先写测试，此时测试会失败（因为功能还未实现）
 * 2. 绿灯（Green）：编写最简单的代码让测试通过
 * 3. 重构（Refactor）：在测试保护下优化代码结构
 *
 * 【TDD的优势】
 * - 确保代码可测试性：先写测试迫使我们思考API设计
 * - 提供安全网：重构时测试能快速发现问题
 * - 文档作用：测试用例展示了代码的使用方式
 * - 减少过度设计：只实现测试需要的功能
 *
 * 【本示例说明】
 * 这个测试类演示了如何使用TDD方法开发一个计算器类
 * 每个测试方法都应该在对应的功能实现之前编写
 */
@DisplayName("计算器测试")
class CalculatorTest {

    private Calculator calculator;

    /**
     * 测试前准备
     * 在每个测试方法执行前都会运行，确保每个测试都有一个全新的Calculator实例
     */
    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    /**
     * 测试用例1：加法 - 两个正数相加
     *
     * TDD步骤：
     * 1. 先写这个测试（红灯阶段）
     * 2. 实现Calculator.add()方法让测试通过（绿灯阶段）
     * 3. 如果需要，重构代码（重构阶段）
     */
    @Test
    @DisplayName("应该正确计算两个正数的和")
    void shouldAddTwoPositiveNumbers() {
        // Arrange（准备）：设置测试数据
        int a = 2;
        int b = 3;

        // Act（执行）：调用被测试的方法
        int result = calculator.add(a, b);

        // Assert（断言）：验证结果是否符合预期
        assertThat(result).isEqualTo(5);
    }

    /**
     * 测试用例2：加法 - 负数相加
     *
     * 边界条件测试：确保加法对负数也能正确处理
     */
    @Test
    @DisplayName("应该正确计算两个负数的和")
    void shouldAddNegativeNumbers() {
        int result = calculator.add(-2, -3);
        assertThat(result).isEqualTo(-5);
    }

    /**
     * 测试用例3：减法
     *
     * TDD原则：每次只添加一个新功能的测试
     */
    @Test
    @DisplayName("应该正确计算两个数的差")
    void shouldSubtractTwoNumbers() {
        int result = calculator.subtract(5, 3);
        assertThat(result).isEqualTo(2);
    }

    /**
     * 测试用例4：乘法
     */
    @Test
    @DisplayName("应该正确计算两个数的积")
    void shouldMultiplyTwoNumbers() {
        int result = calculator.multiply(4, 3);
        assertThat(result).isEqualTo(12);
    }

    /**
     * 测试用例5：除法
     */
    @Test
    @DisplayName("应该正确计算两个数的商")
    void shouldDivideTwoNumbers() {
        int result = calculator.divide(10, 2);
        assertThat(result).isEqualTo(5);
    }

    /**
     * 测试用例6：异常情况测试
     *
     * TDD重要原则：不仅要测试正常情况，还要测试异常情况
     * 这确保了代码的健壮性
     */
    @Test
    @DisplayName("除数为0时应该抛出异常")
    void shouldThrowExceptionWhenDividingByZero() {
        assertThatThrownBy(() -> calculator.divide(10, 0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("不能除以零");
    }
}
