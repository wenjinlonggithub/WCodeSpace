package com.architecture.datastructure.advanced.graph;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Graph Tests")
public class GraphTest {

    @Test
    @DisplayName("测试DFS遍历")
    void testDFS() {
        GraphAdjacencyList graph = new GraphAdjacencyList(false);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 4);

        List<Integer> result = graph.dfs(0);
        assertEquals(5, result.size());
        assertTrue(result.contains(0));
        assertTrue(result.contains(4));
    }

    @Test
    @DisplayName("测试BFS遍历")
    void testBFS() {
        GraphAdjacencyList graph = new GraphAdjacencyList(true);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);

        List<Integer> result = graph.bfs(0);
        assertEquals(List.of(0, 1, 2, 3), result);
    }
}
