package com.architecture.datastructure.tree.binarytree;

import com.architecture.datastructure.common.TreeNode;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Binary Tree Tests")
public class BinaryTreeTest {

    @Test
    @DisplayName("测试前序遍历")
    void testPreorder() {
        TreeNode<Integer> root = new TreeNode<>(1);
        root.left = new TreeNode<>(2);
        root.right = new TreeNode<>(3);
        root.left.left = new TreeNode<>(4);
        root.left.right = new TreeNode<>(5);

        BinaryTreeImplementation<Integer> tree = new BinaryTreeImplementation<>(root);
        List<Integer> result = tree.preorderTraversal();

        assertEquals(List.of(1, 2, 4, 5, 3), result);
    }

    @Test
    @DisplayName("测试中序遍历")
    void testInorder() {
        TreeNode<Integer> root = new TreeNode<>(1);
        root.left = new TreeNode<>(2);
        root.right = new TreeNode<>(3);

        BinaryTreeImplementation<Integer> tree = new BinaryTreeImplementation<>(root);
        List<Integer> result = tree.inorderTraversal();

        assertEquals(List.of(2, 1, 3), result);
    }

    @Test
    @DisplayName("测试层序遍历")
    void testLevelOrder() {
        TreeNode<Integer> root = new TreeNode<>(1);
        root.left = new TreeNode<>(2);
        root.right = new TreeNode<>(3);
        root.left.left = new TreeNode<>(4);

        BinaryTreeImplementation<Integer> tree = new BinaryTreeImplementation<>(root);
        List<Integer> result = tree.levelOrderTraversal();

        assertEquals(List.of(1, 2, 3, 4), result);
    }

    @Test
    @DisplayName("测试树高度")
    void testHeight() {
        TreeNode<Integer> root = new TreeNode<>(1);
        root.left = new TreeNode<>(2);
        root.left.left = new TreeNode<>(3);

        BinaryTreeImplementation<Integer> tree = new BinaryTreeImplementation<>(root);
        assertEquals(3, tree.height());
    }
}
