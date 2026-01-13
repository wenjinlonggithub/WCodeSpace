package com.architecture.datastructure.advanced.unionfind;

/**
 * 并查集 - Union-Find (Disjoint Set)
 * 用于处理动态连通性问题
 */
public class UnionFindImplementation {
    private int[] parent;
    private int[] rank;
    private int count;

    public UnionFindImplementation(int n) {
        parent = new int[n];
        rank = new int[n];
        count = n;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 1;
        }
    }

    /** 查找根节点 - 路径压缩优化 */
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // 路径压缩
        }
        return parent[x];
    }

    /** 合并两个集合 - 按秩合并优化 */
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX == rootY) return;

        // 按秩合并：将较小的树连接到较大的树
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

    /** 判断是否连通 */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /** 获取集合数量 */
    public int getCount() {
        return count;
    }
}
