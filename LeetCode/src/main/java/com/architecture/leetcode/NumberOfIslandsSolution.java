package com.architecture.leetcode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * LeetCode #200: Number of Islands
 * 题目描述：给定一个由'1'（陆地）和'0'（水）组成的二维网格，计算岛屿的数量
 *          岛屿由水平或垂直方向相邻的陆地连接而成，四周都是水
 * 公司：字节跳动、阿里巴巴、腾讯、美团高频题
 *
 * 解法1：深度优先搜索（DFS）
 * 时间复杂度：O(m * n)，其中m和n分别是行数和列数
 * 空间复杂度：O(m * n) - 最坏情况下递归栈的深度
 *
 * 解法2：广度优先搜索（BFS）
 * 时间复杂度：O(m * n)
 * 空间复杂度：O(min(m, n)) - 队列的最大长度
 *
 * 解法3：并查集（Union-Find）
 * 时间复杂度：O(m * n)
 * 空间复杂度：O(m * n)
 */
public class NumberOfIslandsSolution {

    /**
     * 方案1：深度优先搜索（DFS）- 推荐
     * 遍历网格，遇到'1'时，计数加1，并使用DFS将相邻的'1'都标记为'0'
     *
     * @param grid 二维字符网格
     * @return 岛屿数量
     */
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }

        int rows = grid.length;
        int cols = grid[0].length;
        int count = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == '1') {
                    count++;
                    dfs(grid, i, j);
                }
            }
        }

        return count;
    }

    /**
     * DFS：将当前陆地及其相邻的所有陆地标记为'0'
     */
    private void dfs(char[][] grid, int i, int j) {
        int rows = grid.length;
        int cols = grid[0].length;

        // 边界检查和水域检查
        if (i < 0 || i >= rows || j < 0 || j >= cols || grid[i][j] == '0') {
            return;
        }

        // 将当前陆地标记为'0'，表示已访问
        grid[i][j] = '0';

        // 递归访问四个方向
        dfs(grid, i + 1, j);  // 下
        dfs(grid, i - 1, j);  // 上
        dfs(grid, i, j + 1);  // 右
        dfs(grid, i, j - 1);  // 左
    }

    /**
     * 方案2：广度优先搜索（BFS）
     * 使用队列实现BFS，避免递归栈溢出
     *
     * @param grid 二维字符网格
     * @return 岛屿数量
     */
    public int numIslandsBFS(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }

        int rows = grid.length;
        int cols = grid[0].length;
        int count = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == '1') {
                    count++;
                    bfs(grid, i, j);
                }
            }
        }

        return count;
    }

    /**
     * BFS：使用队列遍历岛屿
     */
    private void bfs(char[][] grid, int startI, int startJ) {
        int rows = grid.length;
        int cols = grid[0].length;

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startI, startJ});
        grid[startI][startJ] = '0';

        // 四个方向：上、下、左、右
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int i = cell[0];
            int j = cell[1];

            // 遍历四个方向
            for (int[] dir : directions) {
                int newI = i + dir[0];
                int newJ = j + dir[1];

                // 检查边界和是否为陆地
                if (newI >= 0 && newI < rows && newJ >= 0 && newJ < cols
                    && grid[newI][newJ] == '1') {
                    queue.offer(new int[]{newI, newJ});
                    grid[newI][newJ] = '0';  // 标记为已访问
                }
            }
        }
    }

    /**
     * 方案3：并查集（Union-Find）
     * 适合处理动态连通性问题
     */
    public int numIslandsUnionFind(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }

        int rows = grid.length;
        int cols = grid[0].length;

        UnionFind uf = new UnionFind(grid);

        // 四个方向：下和右（避免重复连接）
        int[][] directions = {{1, 0}, {0, 1}};

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == '1') {
                    // 与下方和右方的陆地合并
                    for (int[] dir : directions) {
                        int newI = i + dir[0];
                        int newJ = j + dir[1];

                        if (newI < rows && newJ < cols && grid[newI][newJ] == '1') {
                            uf.union(i * cols + j, newI * cols + newJ);
                        }
                    }
                }
            }
        }

        return uf.getCount();
    }

    /**
     * 并查集实现
     */
    static class UnionFind {
        private int[] parent;
        private int[] rank;
        private int count;  // 连通分量数量

        public UnionFind(char[][] grid) {
            int rows = grid.length;
            int cols = grid[0].length;
            parent = new int[rows * cols];
            rank = new int[rows * cols];
            count = 0;

            // 初始化并查集
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] == '1') {
                        int id = i * cols + j;
                        parent[id] = id;
                        count++;
                    }
                }
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);  // 路径压缩
            }
            return parent[x];
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX != rootY) {
                // 按秩合并
                if (rank[rootX] < rank[rootY]) {
                    parent[rootX] = rootY;
                } else if (rank[rootX] > rank[rootY]) {
                    parent[rootY] = rootX;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX]++;
                }
                count--;
            }
        }

        public int getCount() {
            return count;
        }
    }

    public static void main(String[] args) {
        NumberOfIslandsSolution solution = new NumberOfIslandsSolution();

        // 测试用例1
        System.out.println("=== Test Case 1: DFS ===");
        char[][] grid1 = {
            {'1', '1', '1', '1', '0'},
            {'1', '1', '0', '1', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '0', '0', '0'}
        };
        System.out.println("Output (DFS): " + solution.numIslands(grid1));
        System.out.println("Explanation: 有1个岛屿\n");

        // 测试用例2
        System.out.println("=== Test Case 2: BFS ===");
        char[][] grid2 = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        System.out.println("Output (BFS): " + solution.numIslandsBFS(grid2));
        System.out.println("Explanation: 有3个岛屿\n");

        // 测试用例3
        System.out.println("=== Test Case 3: Union-Find ===");
        char[][] grid3 = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        System.out.println("Output (Union-Find): " + solution.numIslandsUnionFind(grid3));
        System.out.println("Explanation: 有3个岛屿");
    }
}
