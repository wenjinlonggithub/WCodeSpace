package com.architecture.datastructure.linear.stack;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stack Tests")
public class StackTest {
    private StackImplementation<Integer> stack;

    @BeforeEach
    void setUp() {
        stack = new StackImplementation<>();
    }

    @Test
    @DisplayName("测试push和pop")
    void testPushPop() {
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
    }

    @Test
    @DisplayName("测试peek")
    void testPeek() {
        stack.push(1);
        assertEquals(1, stack.peek());
        assertEquals(1, stack.size());
    }

    @Test
    @DisplayName("测试空栈异常")
    void testEmptyStackException() {
        assertThrows(IllegalStateException.class, () -> stack.pop());
        assertThrows(IllegalStateException.class, () -> stack.peek());
    }
}
