package com.architecture.algorithm.graph;

import java.util.*;

/**
 * 图算法实现类
 * 根据README中的描述，实现图遍历、最短路径、最小生成树等算法
 */
public class GraphAlgorithms {
    
    /**
     * 图的邻接表表示
     */
    public static class Graph {
        private int vertices;
        private List<List<Edge>> adjList;
        
        public Graph(int vertices) {
            this.vertices = vertices;
            this.adjList = new ArrayList<>();
            for (int i = 0; i < vertices; i++) {
                adjList.add(new ArrayList<>());
            }
        }
        
        public void addEdge(int src, int dest, int weight) {
            adjList.get(src).add(new Edge(dest, weight));
        }
        
        public void addUndirectedEdge(int src, int dest, int weight) {
            adjList.get(src).add(new Edge(dest, weight));
            adjList.get(dest).add(new Edge(src, weight));
        }
        
        public List<Edge> getAdjacent(int vertex) {
            return adjList.get(vertex);
        }
        
        public int getVertices() {
            return vertices;
        }
    }
    
    /**
     * 边的表示
     */
    static class Edge {
        int destination;
        int weight;
        
        public Edge(int destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }
    
    /**
     * 深度优先搜索（DFS）- 递归实现
     * 时间复杂度: O(V + E)
     * 空间复杂度: O(V)
     */
    public void dfsRecursive(Graph graph, int startVertex) {
        boolean[] visited = new boolean[graph.getVertices()];
        System.out.print("DFS遍历结果: ");
        dfsRecursiveHelper(graph, startVertex, visited);
        System.out.println();
    }
    
    private void dfsRecursiveHelper(Graph graph, int vertex, boolean[] visited) {
        visited[vertex] = true;
        System.out.print(vertex + " ");
        
        for (Edge edge : graph.getAdjacent(vertex)) {
            if (!visited[edge.destination]) {
                dfsRecursiveHelper(graph, edge.destination, visited);
            }
        }
    }
    
    /**
     * 深度优先搜索（DFS）- 迭代实现
     * 时间复杂度: O(V + E)
     * 空间复杂度: O(V)
     */
    public void dfsIterative(Graph graph, int startVertex) {
        boolean[] visited = new boolean[graph.getVertices()];
        Stack<Integer> stack = new Stack<>();
        
        stack.push(startVertex);
        System.out.print("DFS迭代遍历结果: ");
        
        while (!stack.isEmpty()) {
            int vertex = stack.pop();
            
            if (!visited[vertex]) {
                visited[vertex] = true;
                System.out.print(vertex + " ");
                
                // 将相邻顶点加入栈中
                for (Edge edge : graph.getAdjacent(vertex)) {
                    if (!visited[edge.destination]) {
                        stack.push(edge.destination);
                    }
                }
            }
        }
        System.out.println();
    }
    
    /**
     * 广度优先搜索（BFS）
     * 时间复杂度: O(V + E)
     * 空间复杂度: O(V)
     */
    public void bfs(Graph graph, int startVertex) {
        boolean[] visited = new boolean[graph.getVertices()];
        Queue<Integer> queue = new LinkedList<>();
        
        visited[startVertex] = true;
        queue.offer(startVertex);
        System.out.print("BFS遍历结果: ");
        
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            System.out.print(vertex + " ");
            
            for (Edge edge : graph.getAdjacent(vertex)) {
                if (!visited[edge.destination]) {
                    visited[edge.destination] = true;
                    queue.offer(edge.destination);
                }
            }
        }
        System.out.println();
    }
    
    /**
     * Dijkstra最短路径算法
     * 时间复杂度: O((V + E) log V)
     * 空间复杂度: O(V)
     */
    public int[] dijkstra(Graph graph, int startVertex) {
        int vertices = graph.getVertices();
        int[] distances = new int[vertices];
        boolean[] visited = new boolean[vertices];
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.distance - b.distance);
        
        // 初始化距离数组
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[startVertex] = 0;
        pq.offer(new Node(startVertex, 0));
        
        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            int u = currentNode.vertex;
            
            if (visited[u]) continue;
            visited[u] = true;
            
            // 更新相邻顶点的距离
            for (Edge edge : graph.getAdjacent(u)) {
                int v = edge.destination;
                int weight = edge.weight;
                
                if (!visited[v] && distances[u] != Integer.MAX_VALUE && 
                    distances[u] + weight < distances[v]) {
                    distances[v] = distances[u] + weight;
                    pq.offer(new Node(v, distances[v]));
                }
            }
        }
        
        return distances;
    }
    
    /**
     * Floyd-Warshall最短路径算法
     * 时间复杂度: O(V³)
     * 空间复杂度: O(V²)
     */
    public int[][] floydWarshall(int[][] graphMatrix) {
        int V = graphMatrix.length;
        int[][] dist = new int[V][V];
        
        // 初始化距离矩阵
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                dist[i][j] = graphMatrix[i][j];
            }
        }
        
        // Floyd-Warshall核心算法
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (dist[i][k] != Integer.MAX_VALUE && 
                        dist[k][j] != Integer.MAX_VALUE &&
                        dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        
        return dist;
    }
    
    /**
     * Kruskal最小生成树算法
     * 时间复杂度: O(E log E)
     * 空间复杂度: O(V)
     */
    public List<Edge> kruskalMST(int vertices, List<KruskalEdge> edges) {
        // 按权重排序边
        edges.sort((a, b) -> a.weight - b.weight);
        
        UnionFind uf = new UnionFind(vertices);
        List<Edge> mst = new ArrayList<>();
        
        for (KruskalEdge edge : edges) {
            int rootSrc = uf.find(edge.src);
            int rootDest = uf.find(edge.dest);
            
            // 如果两个顶点不在同一集合中，则不会形成环
            if (rootSrc != rootDest) {
                mst.add(new Edge(edge.dest, edge.weight));
                uf.union(rootSrc, rootDest);
            }
        }
        
        return mst;
    }
    
    /**
     * Prim最小生成树算法
     * 时间复杂度: O((V + E) log V)
     * 空间复杂度: O(V)
     */
    public List<Edge> primMST(Graph graph) {
        int vertices = graph.getVertices();
        boolean[] inMST = new boolean[vertices];
        PriorityQueue<MstNode> pq = new PriorityQueue<>((a, b) -> a.weight - b.weight);
        List<Edge> mst = new ArrayList<>();
        
        // 从顶点0开始
        pq.offer(new MstNode(0, -1, 0));
        
        while (!pq.isEmpty()) {
            MstNode current = pq.poll();
            int u = current.vertex;
            
            if (inMST[u]) continue;
            inMST[u] = true;
            
            // 如果不是起始节点，添加到MST中
            if (current.parent != -1) {
                mst.add(new Edge(u, current.weight));
            }
            
            // 添加所有未访问的相邻节点
            for (Edge edge : graph.getAdjacent(u)) {
                if (!inMST[edge.destination]) {
                    pq.offer(new MstNode(edge.destination, u, edge.weight));
                }
            }
        }
        
        return mst;
    }
    
    /**
     * 拓扑排序（Kahn算法）
     * 时间复杂度: O(V + E)
     * 空间复杂度: O(V)
     */
    public List<Integer> topologicalSort(Graph graph) {
        int vertices = graph.getVertices();
        int[] indegree = new int[vertices];
        
        // 计算每个顶点的入度
        for (int i = 0; i < vertices; i++) {
            for (Edge edge : graph.getAdjacent(i)) {
                indegree[edge.destination]++;
            }
        }
        
        Queue<Integer> queue = new LinkedList<>();
        // 将入度为0的顶点加入队列
        for (int i = 0; i < vertices; i++) {
            if (indegree[i] == 0) {
                queue.offer(i);
            }
        }
        
        List<Integer> topoOrder = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            topoOrder.add(vertex);
            
            // 减少相邻顶点的入度
            for (Edge edge : graph.getAdjacent(vertex)) {
                indegree[edge.destination]--;
                if (indegree[edge.destination] == 0) {
                    queue.offer(edge.destination);
                }
            }
        }
        
        // 检查是否有环
        if (topoOrder.size() != vertices) {
            throw new RuntimeException("图中存在环，无法进行拓扑排序");
        }
        
        return topoOrder;
    }
    
    // 辅助类
    static class Node {
        int vertex;
        int distance;
        
        Node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }
    
    static class KruskalEdge {
        int src, dest, weight;
        
        KruskalEdge(int src, int dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }
    
    static class MstNode {
        int vertex, parent, weight;
        
        MstNode(int vertex, int parent, int weight) {
            this.vertex = vertex;
            this.parent = parent;
            this.weight = weight;
        }
    }
    
    static class UnionFind {
        private int[] parent;
        private int[] rank;
        
        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }
        
        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // 路径压缩
            }
            return parent[x];
        }
        
        void union(int x, int y) {
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
            }
        }
    }
    
    public static void main(String[] args) {
        GraphAlgorithms ga = new GraphAlgorithms();
        
        // 创建示例图
        Graph graph = new Graph(6);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 5, 1);
        
        System.out.println("图遍历算法演示:");
        ga.dfsRecursive(graph, 0);
        ga.dfsIterative(graph, 0);
        ga.bfs(graph, 0);
        
        // Dijkstra算法演示
        System.out.println("\nDijkstra最短路径算法:");
        Graph weightedGraph = new Graph(6);
        weightedGraph.addUndirectedEdge(0, 1, 4);
        weightedGraph.addUndirectedEdge(0, 2, 3);
        weightedGraph.addUndirectedEdge(1, 2, 1);
        weightedGraph.addUndirectedEdge(1, 3, 2);
        weightedGraph.addUndirectedEdge(2, 3, 4);
        weightedGraph.addUndirectedEdge(3, 4, 2);
        weightedGraph.addUndirectedEdge(4, 5, 6);
        
        int[] distances = ga.dijkstra(weightedGraph, 0);
        System.out.println("从顶点0到各顶点的最短距离:");
        for (int i = 0; i < distances.length; i++) {
            System.out.println("顶点0到顶点" + i + ": " + distances[i]);
        }
        
        // Floyd-Warshall算法演示
        System.out.println("\nFloyd-Warshall最短路径算法:");
        int[][] graphMatrix = {
            {0, 3, 6, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE},
            {Integer.MAX_VALUE, 0, 2, 1, Integer.MAX_VALUE, Integer.MAX_VALUE},
            {Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 4, Integer.MAX_VALUE},
            {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 8, 4},
            {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 2},
            {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0}
        };
        
        int[][] allDistances = ga.floydWarshall(graphMatrix);
        System.out.println("所有顶点对之间的最短距离:");
        for (int i = 0; i < allDistances.length; i++) {
            for (int j = 0; j < allDistances[i].length; j++) {
                if (allDistances[i][j] == Integer.MAX_VALUE) {
                    System.out.printf("%7s ", "INF");
                } else {
                    System.out.printf("%7d ", allDistances[i][j]);
                }
            }
            System.out.println();
        }
        
        // 最小生成树演示
        System.out.println("\n最小生成树算法:");
        List<KruskalEdge> edges = new ArrayList<>();
        edges.add(new KruskalEdge(0, 1, 4));
        edges.add(new KruskalEdge(0, 7, 8));
        edges.add(new KruskalEdge(1, 2, 8));
        edges.add(new KruskalEdge(1, 7, 11));
        edges.add(new KruskalEdge(2, 3, 7));
        edges.add(new KruskalEdge(2, 8, 2));
        edges.add(new KruskalEdge(2, 5, 4));
        edges.add(new KruskalEdge(3, 4, 9));
        edges.add(new KruskalEdge(3, 5, 14));
        edges.add(new KruskalEdge(4, 5, 10));
        edges.add(new KruskalEdge(5, 6, 2));
        edges.add(new KruskalEdge(6, 7, 1));
        edges.add(new KruskalEdge(6, 8, 6));
        edges.add(new KruskalEdge(7, 8, 7));
        
        List<Edge> kruskalMST = ga.kruskalMST(9, edges);
        System.out.println("Kruskal MST边数: " + kruskalMST.size());
        
        // 使用之前创建的weightedGraph进行Prim算法演示
        List<Edge> primMST = ga.primMST(weightedGraph);
        System.out.println("Prim MST边数: " + primMST.size());
        
        // 拓扑排序演示
        System.out.println("\n拓扑排序:");
        Graph dag = new Graph(6);
        dag.addEdge(5, 2, 0);
        dag.addEdge(5, 0, 0);
        dag.addEdge(4, 0, 0);
        dag.addEdge(4, 1, 0);
        dag.addEdge(2, 3, 0);
        dag.addEdge(3, 1, 0);
        
        List<Integer> topoOrder = ga.topologicalSort(dag);
        System.out.println("拓扑排序结果: " + topoOrder);
    }
}