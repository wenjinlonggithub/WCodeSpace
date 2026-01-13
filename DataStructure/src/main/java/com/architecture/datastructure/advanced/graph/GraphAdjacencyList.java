package com.architecture.datastructure.advanced.graph;

import java.util.*;

/**
 * 图 - 邻接表实现
 * 适合稀疏图
 */
public class GraphAdjacencyList {
    private Map<Integer, List<Integer>> adjList;
    private boolean isDirected;

    public GraphAdjacencyList(boolean isDirected) {
        this.adjList = new HashMap<>();
        this.isDirected = isDirected;
    }

    public void addVertex(int vertex) {
        adjList.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(int from, int to) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());
        adjList.get(from).add(to);
        if (!isDirected) {
            adjList.get(to).add(from);
        }
    }

    /** DFS遍历 */
    public List<Integer> dfs(int start) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        dfsHelper(start, visited, result);
        return result;
    }

    private void dfsHelper(int vertex, Set<Integer> visited, List<Integer> result) {
        visited.add(vertex);
        result.add(vertex);

        if (adjList.containsKey(vertex)) {
            for (int neighbor : adjList.get(vertex)) {
                if (!visited.contains(neighbor)) {
                    dfsHelper(neighbor, visited, result);
                }
            }
        }
    }

    /** BFS遍历 */
    public List<Integer> bfs(int start) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            result.add(vertex);

            if (adjList.containsKey(vertex)) {
                for (int neighbor : adjList.get(vertex)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        return result;
    }

    public Map<Integer, List<Integer>> getAdjList() {
        return adjList;
    }
}
