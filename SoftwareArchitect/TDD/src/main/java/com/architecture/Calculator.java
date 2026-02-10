package com.architecture;

/**
 * 计算器实现类
 *
 * 【TDD开发过程回顾】
 * 这个类是按照TDD方法论开发的：
 * 1. 先在CalculatorTest中编写测试用例
 * 2. 运行测试，看到红灯（测试失败）
 * 3. 编写最简单的实现让测试通过（绿灯）
 * 4. 在测试保护下进行重构
 *
 * 【TDD的"最简实现"原则】
 * 在绿灯阶段，我们只需要让测试通过，不需要考虑复杂的实现
 * 例如：add方法就是简单的 a + b，不需要过度设计
 *
 * 【测试先行的好处】
 * - 这些方法的签名（参数、返回值）是由测试需求决定的
 * - API设计更符合使用者的需求
 * - 每个方法都有对应的测试保护
 */
public class Calculator {

    /**
     * 加法运算
     *
     * TDD实现步骤：
     * 1. 先写测试 shouldAddTwoPositiveNumbers
     * 2. 实现这个方法：return a + b;
     * 3. 测试通过（绿灯）
     * 4. 添加更多测试用例（如负数测试）
     * 5. 确认实现仍然正确
     *
     * @param a 第一个加数
     * @param b 第二个加数
     * @return 两数之和
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * 减法运算
     *
     * @param a 被减数
     * @param b 减数
     * @return 两数之差
     */
    public int subtract(int a, int b) {
        return a - b;
    }

    /**
     * 乘法运算
     *
     * @param a 第一个乘数
     * @param b 第二个乘数
     * @return 两数之积
     */
    public int multiply(int a, int b) {
        return a * b;
    }

    /**
     * 除法运算
     *
     * TDD如何处理异常情况：
     * 1. 先写测试 shouldThrowExceptionWhenDividingByZero
     * 2. 测试期望抛出 ArithmeticException
     * 3. 在实现中添加除零检查
     * 4. 测试通过
     *
     * 这种方式确保了异常处理逻辑也被测试覆盖
     *
     * @param a 被除数
     * @param b 除数
     * @return 两数之商
     * @throws ArithmeticException 当除数为0时抛出
     */
    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("不能除以零");
        }
        return a / b;
    }
}
