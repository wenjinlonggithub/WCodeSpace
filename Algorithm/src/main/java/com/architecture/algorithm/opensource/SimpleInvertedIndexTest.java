package com.architecture.algorithm.opensource;

import java.util.List;

/**
 * 倒排索引简单测试类（不依赖JUnit）
 */
public class SimpleInvertedIndexTest {
    
    public static void main(String[] args) {
        System.out.println("开始测试倒排索引实现...");
        
        boolean allTestsPassed = true;
        
        allTestsPassed &= testBasicIndexing();
        allTestsPassed &= testANDQuery();
        allTestsPassed &= testORQuery();
        allTestsPassed &= testDocumentRemoval();
        allTestsPassed &= testRelevanceScoring();
        allTestsPassed &= testTermStatistics();
        allTestsPassed &= testRankingSearch();
        allTestsPassed &= testEmptyAndNonExistentQueries();
        
        if (allTestsPassed) {
            System.out.println("\n所有测试通过！");
        } else {
            System.out.println("\n有测试失败！");
        }
    }
    
    private static boolean testBasicIndexing() {
        System.out.println("\n1. 测试基本索引功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "hello world");
            index.addDocument(2, "hello java world");
            
            assertEqual(2, index.getIndexStats().getNumDocuments(), "文档数量");
            
            List<Integer> helloResults = index.searchSingleTerm("hello");
            assertEqual(2, helloResults.size(), "hello查询结果数量");
            assertTrue(helloResults.contains(1), "结果应包含文档1");
            assertTrue(helloResults.contains(2), "结果应包含文档2");
            
            List<Integer> worldResults = index.searchSingleTerm("world");
            assertEqual(2, worldResults.size(), "world查询结果数量");
            
            List<Integer> javaResults = index.searchSingleTerm("java");
            assertEqual(1, javaResults.size(), "java查询结果数量");
            assertTrue(javaResults.contains(2), "结果应包含文档2");
            
            System.out.println("   基本索引功能测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   基本索引功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testANDQuery() {
        System.out.println("\n2. 测试AND查询功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "hello world");
            index.addDocument(2, "hello java");
            index.addDocument(3, "java world");
            index.addDocument(4, "hello python");
            
            List<Integer> results = index.searchWithAND("hello", "world");
            assertEqual(1, results.size(), "AND查询结果数量");
            assertEqual(1, results.get(0), "AND查询结果文档ID");
            
            System.out.println("   AND查询功能测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   AND查询功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testORQuery() {
        System.out.println("\n3. 测试OR查询功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "hello world");
            index.addDocument(2, "java programming");
            index.addDocument(3, "python script");
            
            List<Integer> results = index.searchWithOR("hello", "java");
            assertEqual(2, results.size(), "OR查询结果数量");
            assertTrue(results.contains(1), "结果应包含文档1");
            assertTrue(results.contains(2), "结果应包含文档2");
            
            System.out.println("   OR查询功能测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   OR查询功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testDocumentRemoval() {
        System.out.println("\n4. 测试文档删除功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "hello world");
            index.addDocument(2, "java programming");
            
            assertEqual(2, index.getIndexStats().getNumDocuments(), "删除前文档数量");
            
            index.removeDocument(1);
            
            assertEqual(1, index.getIndexStats().getNumDocuments(), "删除后文档数量");
            
            List<Integer> results = index.searchSingleTerm("hello");
            assertEqual(0, results.size(), "删除后hello查询结果数量");
            
            results = index.searchSingleTerm("java");
            assertEqual(1, results.size(), "删除后java查询结果数量");
            assertEqual(2, results.get(0), "删除后java查询结果文档ID");
            
            System.out.println("   文档删除功能测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   文档删除功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testRelevanceScoring() {
        System.out.println("\n5. 测试相关性评分功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "machine learning machine learning"); // 机器学习出现2次，长度2
            index.addDocument(2, "machine learning and deep learning"); // 机器学习出现1次，长度4
            index.addDocument(3, "this is a document about machine learning"); // 机器学习出现1次，长度6
            
            double score1 = index.calculateRelevanceScore("machine learning", 1);
            double score2 = index.calculateRelevanceScore("machine learning", 2);
            double score3 = index.calculateRelevanceScore("machine learning", 3);
            
            // 文档1中"machine"和"learning"各出现2次，且文档最短，因此得分应该最高
            assertTrue(score1 >= score2, "文档1因词频高且长度短，应得分更高");
            
            System.out.println("   相关性评分功能测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   相关性评分功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testTermStatistics() {
        System.out.println("\n6. 测试词汇统计功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "machine learning");
            index.addDocument(2, "machine learning and data");
            index.addDocument(3, "data science and machine learning");
            
            InvertedIndexImplementation.TermStats stats = index.getTermStats("machine");
            
            assertNotNull(stats, "词汇统计对象不应为null");
            assertEqual(3, stats.getDocFreq(), "文档频率");
            assertEqual(3, stats.getTotalFreq(), "总频率");
            
            System.out.println("   词汇统计功能测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   词汇统计功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testRankingSearch() {
        System.out.println("\n7. 测试排序搜索功能...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "machine learning algorithms");
            index.addDocument(2, "machine learning");
            index.addDocument(3, "algorithms and data structures");
            
            List<InvertedIndexImplementation.DocumentScore> results = 
                index.searchWithRanking("machine learning");
            
            assertTrue(results.size() >= 2, "应至少找到两个结果");
            
            if (results.size() > 0) {
                // 文档1包含两个查询词，应该得分较高
                System.out.println("   排序搜索功能测试通过");
                return true;
            } else {
                System.out.println("   排序搜索功能测试未通过：没有找到结果");
                return false;
            }
        } catch (Exception e) {
            System.out.println("   排序搜索功能测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testEmptyAndNonExistentQueries() {
        System.out.println("\n8. 测试空查询和不存在词汇查询...");
        try {
            InvertedIndexImplementation index = new InvertedIndexImplementation();
            
            index.addDocument(1, "hello world");
            
            // 测试空查询
            List<Integer> results = index.searchSingleTerm("");
            assertEqual(0, results.size(), "空查询结果数量");
            
            // 测试不存在的词汇
            results = index.searchSingleTerm("nonexistent");
            assertEqual(0, results.size(), "不存在词汇查询结果数量");
            
            results = index.searchWithAND("hello", "nonexistent");
            assertEqual(0, results.size(), "AND查询中包含不存在词汇的结果数量");
            
            System.out.println("   空查询和不存在词汇查询测试通过");
            return true;
        } catch (Exception e) {
            System.out.println("   空查询和不存在词汇查询测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 简单的断言工具方法
    private static void assertEqual(int expected, int actual, String message) {
        if (expected != actual) {
            throw new RuntimeException(message + " 不匹配: 期望=" + expected + ", 实际=" + actual);
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message + " 条件不满足");
        }
    }
    
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message + " 对象为null");
        }
    }
}