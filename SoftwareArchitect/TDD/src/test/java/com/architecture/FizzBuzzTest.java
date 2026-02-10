package com.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Kata：FizzBuzz
 *
 * 【什么是Kata】
 * Kata是一种编程练习，通过反复练习来掌握TDD技巧
 * FizzBuzz是最经典的TDD Kata之一
 *
 * 【问题描述】
 * 编写一个程序，输入数字n，输出规则如下：
 * - 如果n能被3整除，返回 "Fizz"
 * - 如果n能被5整除，返回 "Buzz"
 * - 如果n同时能被3和5整除，返回 "FizzBuzz"
 * - 其他情况返回数字本身的字符串形式
 *
 * 【TDD实践：小步前进】
 * FizzBuzz展示了TDD的"小步前进"原则：
 * 1. 从最简单的测试开始（返回"1"）
 * 2. 逐步添加规则（Fizz、Buzz、FizzBuzz）
 * 3. 每次只添加一个测试
 * 4. 让测试驱动实现的演进
 *
 * 【参数化测试】
 * 本示例还展示了JUnit 5的参数化测试
 * 这是一种高效的测试方式，可以用同一个测试方法验证多组数据
 */
@DisplayName("FizzBuzz Kata测试")
class FizzBuzzTest {

    private FizzBuzz fizzBuzz;

    @BeforeEach
    void setUp() {
        fizzBuzz = new FizzBuzz();
    }

    /**
     * 测试用例1：最简单的情况
     *
     * TDD的第一步：从最简单的测试开始
     * 输入1，期望输出"1"
     * 这个测试帮助我们：
     * - 定义了方法签名：convert(int) -> String
     * - 建立了最基本的行为
     */
    @Test
    @DisplayName("输入1应该返回'1'")
    void shouldReturnOneForInputOne() {
        String result = fizzBuzz.convert(1);
        assertThat(result).isEqualTo("1");
    }

    /**
     * 测试用例2：继续简单情况
     *
     * TDD原则：不要一次跳太大步
     * 先确保普通数字的处理是正确的
     */
    @Test
    @DisplayName("输入2应该返回'2'")
    void shouldReturnTwoForInputTwo() {
        String result = fizzBuzz.convert(2);
        assertThat(result).isEqualTo("2");
    }

    /**
     * 测试用例3：第一个特殊规则
     *
     * TDD的增量开发：
     * 现在添加第一个特殊规则：3的倍数返回"Fizz"
     * 这个测试会驱动我们添加条件判断
     */
    @Test
    @DisplayName("输入3应该返回'Fizz'")
    void shouldReturnFizzForInputThree() {
        String result = fizzBuzz.convert(3);
        assertThat(result).isEqualTo("Fizz");
    }

    /**
     * 测试用例4：第二个特殊规则
     *
     * 继续添加规则：5的倍数返回"Buzz"
     */
    @Test
    @DisplayName("输入5应该返回'Buzz'")
    void shouldReturnBuzzForInputFive() {
        String result = fizzBuzz.convert(5);
        assertThat(result).isEqualTo("Buzz");
    }

    /**
     * 测试用例5：组合规则
     *
     * TDD的关键时刻：
     * 15既是3的倍数，也是5的倍数
     * 这个测试会驱动我们思考条件判断的顺序
     * 必须先判断15的倍数，再判断3和5的倍数
     */
    @Test
    @DisplayName("输入15应该返回'FizzBuzz'")
    void shouldReturnFizzBuzzForInputFifteen() {
        String result = fizzBuzz.convert(15);
        assertThat(result).isEqualTo("FizzBuzz");
    }

    /**
     * 参数化测试：批量验证
     *
     * 【参数化测试的优势】
     * 1. 减少重复代码
     * 2. 更容易添加新的测试用例
     * 3. 测试报告更清晰（每组数据单独报告）
     *
     * 【TDD中的参数化测试】
     * 在基本功能实现后，可以用参数化测试来：
     * - 验证更多边界情况
     * - 确保实现的正确性
     * - 作为回归测试
     */
    @ParameterizedTest
    @CsvSource({
        "3, Fizz",      // 3的倍数
        "6, Fizz",      // 3的倍数
        "9, Fizz",      // 3的倍数
        "5, Buzz",      // 5的倍数
        "10, Buzz",     // 5的倍数
        "15, FizzBuzz", // 3和5的倍数
        "30, FizzBuzz", // 3和5的倍数
        "1, 1",         // 普通数字
        "2, 2",         // 普通数字
        "4, 4"          // 普通数字
    })
    @DisplayName("应该正确处理多种输入情况")
    void shouldHandleMultipleTestCases(int input, String expected) {
        String result = fizzBuzz.convert(input);
        assertThat(result).isEqualTo(expected);
    }
}
