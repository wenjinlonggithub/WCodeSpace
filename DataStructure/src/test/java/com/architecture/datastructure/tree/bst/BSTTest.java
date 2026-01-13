package com.architecture.datastructure.tree.bst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BST Tests")
public class BSTTest {
    private BSTImplementation<Integer> bst;

    @BeforeEach
    void setUp() {
        bst = new BSTImplementation<>();
    }

    @Test
    @DisplayName("测试插入和查找")
    void testInsertAndSearch() {
        bst.insert(5);
        bst.insert(3);
        bst.insert(7);
        bst.insert(1);
        bst.insert(9);

        assertTrue(bst.search(5));
        assertTrue(bst.search(1));
        assertTrue(bst.search(9));
        assertFalse(bst.search(10));
    }

    @Test
    @DisplayName("测试删除")
    void testDelete() {
        bst.insert(5);
        bst.insert(3);
        bst.insert(7);

        bst.delete(3);
        assertFalse(bst.search(3));
        assertTrue(bst.search(5));
        assertTrue(bst.search(7));
    }
}
