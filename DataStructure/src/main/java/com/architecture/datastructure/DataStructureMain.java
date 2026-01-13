package com.architecture.datastructure;

/**
 * 数据结构学习主类 - Main Entry Point for Data Structure Learning
 *
 * 本项目包含20+数据结构的完整实现，包括：
 * This project contains 20+ data structure implementations, including:
 *
 * 1. 线性结构 (Linear Structures)
 *    - Array, LinkedList, Stack, Queue, Deque
 *
 * 2. 哈希结构 (Hash-based Structures)
 *    - HashMap, HashSet, LinkedHashMap
 *
 * 3. 树形结构 (Tree Structures)
 *    - BinaryTree, BST, AVL, RedBlack, BTree, Heap
 *
 * 4. 高级结构 (Advanced Structures)
 *    - Trie, Graph, BitSet, SkipList, UnionFind
 *
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
public class DataStructureMain {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║      数据结构实战项目 - Data Structure Project       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("📚 项目内容 / Project Contents:");
        System.out.println("  ├─ 20+ 数据结构完整实现 / Complete Implementations");
        System.out.println("  ├─ 详细原理注释说明 / Detailed Principle Explanations");
        System.out.println("  ├─ 实际应用场景演示 / Practical Use Case Demonstrations");
        System.out.println("  ├─ 100+ 面试题解析 / Interview Questions & Solutions");
        System.out.println("  └─ 完整单元测试覆盖 / Comprehensive Unit Test Coverage");
        System.out.println();

        System.out.println("🗂️  数据结构分类 / Data Structure Categories:");
        System.out.println();

        System.out.println("  1️⃣  线性结构 (Linear Structures)");
        System.out.println("      • Array         - 动态数组");
        System.out.println("      • LinkedList    - 链表（单向/双向/循环）");
        System.out.println("      • Stack         - 栈");
        System.out.println("      • Queue         - 队列（普通/循环）");
        System.out.println("      • Deque         - 双端队列");
        System.out.println();

        System.out.println("  2️⃣  哈希结构 (Hash-based Structures)");
        System.out.println("      • HashMap       - 哈希表");
        System.out.println("      • HashSet       - 哈希集合");
        System.out.println("      • LinkedHashMap - 有序哈希表");
        System.out.println();

        System.out.println("  3️⃣  树形结构 (Tree Structures)");
        System.out.println("      • BinaryTree    - 二叉树");
        System.out.println("      • BST           - 二叉搜索树");
        System.out.println("      • AVL Tree      - 平衡二叉树");
        System.out.println("      • Red-Black     - 红黑树");
        System.out.println("      • B-Tree        - B树/B+树");
        System.out.println("      • Heap          - 堆（最大/最小堆）");
        System.out.println();

        System.out.println("  4️⃣  高级结构 (Advanced Structures)");
        System.out.println("      • Trie          - 字典树");
        System.out.println("      • Graph         - 图（邻接表/邻接矩阵）");
        System.out.println("      • BitSet        - 位集合");
        System.out.println("      • SkipList      - 跳表");
        System.out.println("      • Union-Find    - 并查集");
        System.out.println();

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("💡 提示: 每个数据结构都包含实现代码、演示案例、面试题解析和单元测试");
        System.out.println("📖 文档: 详见 docs/ 目录下的 Markdown 文档");
        System.out.println("🧪 测试: 运行 mvn test 执行所有单元测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * 时间复杂度枚举 - Time Complexity Enumeration
     *
     * 用于标注算法和操作的时间复杂度
     * Used for annotating time complexity of algorithms and operations
     */
    public enum TimeComplexity {
        /** O(1) - 常数时间复杂度 / Constant Time */
        O1("O(1)", "常数时间 / Constant Time"),

        /** O(log n) - 对数时间复杂度 / Logarithmic Time */
        OLOGN("O(log n)", "对数时间 / Logarithmic Time"),

        /** O(n) - 线性时间复杂度 / Linear Time */
        ON("O(n)", "线性时间 / Linear Time"),

        /** O(n log n) - 线性对数时间复杂度 / Linearithmic Time */
        ONLOGN("O(n log n)", "线性对数时间 / Linearithmic Time"),

        /** O(n²) - 平方时间复杂度 / Quadratic Time */
        ON2("O(n²)", "平方时间 / Quadratic Time"),

        /** O(n³) - 立方时间复杂度 / Cubic Time */
        ON3("O(n³)", "立方时间 / Cubic Time"),

        /** O(2^n) - 指数时间复杂度 / Exponential Time */
        O2N("O(2^n)", "指数时间 / Exponential Time"),

        /** O(n!) - 阶乘时间复杂度 / Factorial Time */
        ON_FACTORIAL("O(n!)", "阶乘时间 / Factorial Time");

        private final String notation;
        private final String description;

        TimeComplexity(String notation, String description) {
            this.notation = notation;
            this.description = description;
        }

        public String getNotation() {
            return notation;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return notation + " - " + description;
        }
    }

    /**
     * 空间复杂度枚举 - Space Complexity Enumeration
     *
     * 用于标注算法和数据结构的空间复杂度
     * Used for annotating space complexity
     */
    public enum SpaceComplexity {
        /** O(1) - 常数空间复杂度 / Constant Space */
        O1("O(1)", "常数空间 / Constant Space"),

        /** O(log n) - 对数空间复杂度 / Logarithmic Space */
        OLOGN("O(log n)", "对数空间 / Logarithmic Space"),

        /** O(n) - 线性空间复杂度 / Linear Space */
        ON("O(n)", "线性空间 / Linear Space"),

        /** O(n²) - 平方空间复杂度 / Quadratic Space */
        ON2("O(n²)", "平方空间 / Quadratic Space");

        private final String notation;
        private final String description;

        SpaceComplexity(String notation, String description) {
            this.notation = notation;
            this.description = description;
        }

        public String getNotation() {
            return notation;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return notation + " - " + description;
        }
    }
}
