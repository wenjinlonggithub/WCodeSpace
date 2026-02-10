package com.architecture;

/**
 * FizzBuzz实现类
 *
 * 【TDD的演进过程】
 * 这个类展示了TDD如何驱动代码的演进：
 *
 * 第1步：实现最简单的测试（返回"1"）
 * public String convert(int number) {
 *     return "1";
 * }
 *
 * 第2步：让第二个测试通过（返回"2"）
 * public String convert(int number) {
 *     return String.valueOf(number);
 * }
 *
 * 第3步：添加Fizz规则
 * public String convert(int number) {
 *     if (number % 3 == 0) return "Fizz";
 *     return String.valueOf(number);
 * }
 *
 * 第4步：添加Buzz规则
 * public String convert(int number) {
 *     if (number % 3 == 0) return "Fizz";
 *     if (number % 5 == 0) return "Buzz";
 *     return String.valueOf(number);
 * }
 *
 * 第5步：添加FizzBuzz规则（重构：调整判断顺序）
 * public String convert(int number) {
 *     if (number % 15 == 0) return "FizzBuzz";
 *     if (number % 3 == 0) return "Fizz";
 *     if (number % 5 == 0) return "Buzz";
 *     return String.valueOf(number);
 * }
 *
 * 【TDD的关键洞察】
 * 1. 每一步都是由测试驱动的
 * 2. 实现总是"刚好够用"，不过度设计
 * 3. 重构在测试保护下进行（第5步调整了判断顺序）
 * 4. 最终代码简洁、清晰、可测试
 */
public class FizzBuzz {

    /**
     * 将数字转换为FizzBuzz字符串
     *
     * 【条件判断的顺序】
     * 注意：必须先判断15的倍数，再判断3和5的倍数
     * 这是因为15既是3的倍数，也是5的倍数
     * 如果先判断3或5，就永远不会返回"FizzBuzz"
     *
     * 这个顺序是由测试驱动出来的：
     * 当我们添加shouldReturnFizzBuzzForInputFifteen测试时
     * 发现原来的实现无法通过，于是调整了判断顺序
     *
     * @param number 待转换的数字
     * @return 转换后的字符串
     */
    public String convert(int number) {
        // 优先判断：同时是3和5的倍数
        if (number % 15 == 0) {
            return "FizzBuzz";
        }
        // 判断：是3的倍数
        if (number % 3 == 0) {
            return "Fizz";
        }
        // 判断：是5的倍数
        if (number % 5 == 0) {
            return "Buzz";
        }
        // 默认：返回数字本身
        return String.valueOf(number);
    }
}
