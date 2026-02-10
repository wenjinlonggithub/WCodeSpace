package com.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD示例：字符串工具类测试
 *
 * 【TDD中的测试设计原则】
 * 1. 单一职责：每个测试只验证一个行为
 * 2. 独立性：测试之间不应该相互依赖
 * 3. 可重复：每次运行结果应该一致
 * 4. 自验证：测试应该自动判断通过或失败
 * 5. 及时性：测试应该在编写代码之前编写
 *
 * 【边界条件测试】
 * 好的TDD实践要求测试各种边界情况：
 * - 空字符串
 * - null值
 * - 特殊字符
 * - 极端值
 */
@DisplayName("字符串工具类测试")
class StringUtilsTest {

    private StringUtils stringUtils;

    @BeforeEach
    void setUp() {
        stringUtils = new StringUtils();
    }

    /**
     * 测试用例1：字符串反转 - 正常情况
     *
     * TDD思路：
     * 1. 先写这个测试，明确需求：reverse("hello") 应该返回 "olleh"
     * 2. 运行测试，看到红灯
     * 3. 实现reverse方法
     * 4. 测试通过，绿灯
     */
    @Test
    @DisplayName("应该正确反转字符串")
    void shouldReverseString() {
        String result = stringUtils.reverse("hello");
        assertThat(result).isEqualTo("olleh");
    }

    /**
     * 测试用例2：字符串反转 - 边界条件（空字符串）
     *
     * TDD强调边界测试：
     * 在实现功能后，要思考各种边界情况
     * 空字符串是最常见的边界条件之一
     */
    @Test
    @DisplayName("反转空字符串应该返回空字符串")
    void shouldReturnEmptyStringWhenReversingEmptyString() {
        String result = stringUtils.reverse("");
        assertThat(result).isEmpty();
    }

    /**
     * 测试用例3：回文检测
     *
     * TDD的增量开发：
     * 每个新功能都从测试开始
     * 这个测试定义了isPalindrome方法的行为
     */
    @Test
    @DisplayName("应该正确判断字符串是否为回文")
    void shouldCheckIfStringIsPalindrome() {
        // 测试正面案例：是回文
        assertThat(stringUtils.isPalindrome("racecar")).isTrue();

        // 测试负面案例：不是回文
        assertThat(stringUtils.isPalindrome("hello")).isFalse();
    }

    /**
     * 测试用例4：统计元音字母
     *
     * 测试驱动的需求明确：
     * 通过这个测试，我们明确了：
     * - 方法名：countVowels
     * - 参数：String
     * - 返回值：int
     * - 行为："hello world" 中有3个元音（e, o, o）
     */
    @Test
    @DisplayName("应该正确统计字符串中的元音字母数量")
    void shouldCountVowelsInString() {
        int count = stringUtils.countVowels("hello world");
        assertThat(count).isEqualTo(3); // e, o, o
    }

    /**
     * 测试用例5：首字母大写
     *
     * TDD的文档作用：
     * 这个测试清楚地展示了capitalizeWords方法的预期行为
     * 比任何文档都更准确、更可执行
     */
    @Test
    @DisplayName("应该将每个单词的首字母大写")
    void shouldCapitalizeFirstLetterOfEachWord() {
        String result = stringUtils.capitalizeWords("hello world");
        assertThat(result).isEqualTo("Hello World");
    }
}
