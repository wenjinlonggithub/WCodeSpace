package com.architecture;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础测试类
 * 用于测试项目的基本功能
 */
public class AppTest {

    @Test
    public void testAppNotNull() {
        // 确保App类可以被实例化
        App app = new App();
        assertNotNull(app);
    }

    @Test
    public void testMainMethodExists() {
        // 确保main方法存在（通过反射检查）
        try {
            App.class.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }
}
