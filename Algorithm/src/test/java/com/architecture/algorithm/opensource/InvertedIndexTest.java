package com.architecture.algorithm.opensource;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * 倒排索引算法测试类
 */
public class InvertedIndexTest {
    
    @Test
    public void testBasicIndexing() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        // 添加文档
        index.addDocument(1, "hello world");
        index.addDocument(2, "hello java world");
        
        // 验证文档被正确添加
        assertEquals(2, index.getIndexStats().getNumDocuments());
        
        // 验证单词查询
        List<Integer> helloResults = index.searchSingleTerm("hello");
        assertEquals(2, helloResults.size());
        assertTrue(helloResults.contains(1));
        assertTrue(helloResults.contains(2));
        
        List<Integer> worldResults = index.searchSingleTerm("world");
        assertEquals(2, worldResults.size());
        assertTrue(worldResults.contains(1));
        assertTrue(worldResults.contains(2));
        
        List<Integer> javaResults = index.searchSingleTerm("java");
        assertEquals(1, javaResults.size());
        assertTrue(javaResults.contains(2));
    }
    
    @Test
    public void testANDQuery() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        index.addDocument(1, "hello world");
        index.addDocument(2, "hello java");
        index.addDocument(3, "java world");
        index.addDocument(4, "hello python");
        
        // 查询同时包含"hello"和"world"的文档
        List<Integer> results = index.searchWithAND("hello", "world");
        assertEquals(1, results.size());
        assertEquals(1, results.get(0));  // 只有文档1同时包含hello和world
    }
    
    @Test
    public void testORQuery() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        index.addDocument(1, "hello world");
        index.addDocument(2, "java programming");
        index.addDocument(3, "python script");
        
        // 查询包含"hello"或"java"的文档
        List<Integer> results = index.searchWithOR("hello", "java");
        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
    }
    
    @Test
    public void testDocumentRemoval() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        index.addDocument(1, "hello world");
        index.addDocument(2, "java programming");
        
        assertEquals(2, index.getIndexStats().getNumDocuments());
        
        // 删除文档
        index.removeDocument(1);
        
        assertEquals(1, index.getIndexStats().getNumDocuments());
        
        // 验证删除后的查询结果
        List<Integer> results = index.searchSingleTerm("hello");
        assertEquals(0, results.size());  // hello应该不再存在于任何文档中
        
        results = index.searchSingleTerm("java");
        assertEquals(1, results.size());
        assertEquals(2, results.get(0));
    }
    
    @Test
    public void testRelevanceScoring() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        // 添加不同长度的文档，用于测试TF-IDF
        index.addDocument(1, "machine learning machine learning"); // 机器学习出现2次，长度2
        index.addDocument(2, "machine learning and deep learning"); // 机器学习出现1次，长度4
        index.addDocument(3, "this is a document about machine learning"); // 机器学习出现1次，长度6
        
        // 测试相关性评分
        double score1 = index.calculateRelevanceScore("machine learning", 1);
        double score2 = index.calculateRelevanceScore("machine learning", 2);
        double score3 = index.calculateRelevanceScore("machine learning", 3);
        
        // 文档1中"machine"和"learning"各出现2次，且文档最短，因此得分应该最高
        assertTrue(score1 > score2, "文档1因词频高且长度短，应得分更高");
    }
    
    @Test
    public void testTermStatistics() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        index.addDocument(1, "machine learning");
        index.addDocument(2, "machine learning and data");
        index.addDocument(3, "data science and machine learning");
        
        InvertedIndexImplementation.TermStats stats = index.getTermStats("machine");
        
        assertNotNull(stats);
        assertEquals(3, stats.getDocFreq());    // 出现在3个文档中
        assertEquals(3, stats.getTotalFreq());  // 总共出现3次
    }
    
    @Test
    public void testRankingSearch() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        index.addDocument(1, "machine learning algorithms");
        index.addDocument(2, "machine learning");
        index.addDocument(3, "algorithms and data structures");
        
        List<InvertedIndexImplementation.DocumentScore> results = 
            index.searchWithRanking("machine learning");
        
        // 应该至少找到两个文档（1和2）
        assertTrue(results.size() >= 2);
        
        // 第一个结果应该是文档1（因为包含两个查询词）
        assertEquals(1, results.get(0).getDocId());
    }
    
    @Test
    public void testEmptyQueries() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        // 测试空查询
        List<Integer> results = index.searchSingleTerm("");
        assertEquals(0, results.size());
        
        results = index.searchWithAND();
        assertEquals(0, results.size());
        
        results = index.searchWithOR();
        assertEquals(0, results.size());
    }
    
    @Test
    public void testNonExistentTerms() {
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        index.addDocument(1, "hello world");
        
        // 查询不存在的词
        List<Integer> results = index.searchSingleTerm("nonexistent");
        assertEquals(0, results.size());
        
        results = index.searchWithAND("hello", "nonexistent");
        assertEquals(0, results.size());
    }
}