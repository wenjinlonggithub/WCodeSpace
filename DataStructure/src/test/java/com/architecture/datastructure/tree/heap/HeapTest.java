package com.architecture.datastructure.tree.heap;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Min Heap Tests")
public class HeapTest {
    private MinHeapImplementation<Integer> heap;

    @BeforeEach
    void setUp() {
        heap = new MinHeapImplementation<>();
    }

    @Test
    @DisplayName("测试插入和提取最小值")
    void testInsertAndExtractMin() {
        heap.insert(5);
        heap.insert(3);
        heap.insert(7);
        heap.insert(1);

        assertEquals(1, heap.extractMin());
        assertEquals(3, heap.extractMin());
        assertEquals(5, heap.extractMin());
        assertEquals(7, heap.extractMin());
    }

    @Test
    @DisplayName("测试peek")
    void testPeek() {
        heap.insert(10);
        heap.insert(5);
        heap.insert(15);

        assertEquals(5, heap.peek());
        assertEquals(3, heap.size());
    }
}
