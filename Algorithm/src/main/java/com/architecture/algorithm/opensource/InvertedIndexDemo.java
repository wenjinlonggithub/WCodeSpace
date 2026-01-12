package com.architecture.algorithm.opensource;

import java.util.List;

/**
 * 倒排索引算法演示类
 * 展示倒排索引的各种功能和应用场景
 */
public class InvertedIndexDemo {
    
    public static void main(String[] args) {
        demonstrateBasicFunctionality();
        demonstrateAdvancedFeatures();
        demonstratePerformanceComparison();
    }
    
    /**
     * 演示基本功能
     */
    public static void demonstrateBasicFunctionality() {
        System.out.println("=== 倒排索引基本功能演示 ===");
        
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        // 添加文档
        index.addDocument(1, "the quick brown fox jumps over the lazy dog");
        index.addDocument(2, "a quick brown fox runs fast in the forest");
        index.addDocument(3, "lazy dogs sleep all day while cats play");
        index.addDocument(4, "the fast runner jumps over obstacles");
        
        System.out.println("文档添加完成");
        System.out.println("索引统计: " + index.getIndexStats());
        
        // 单词查询
        System.out.println("\n--- 单词查询 ---");
        List<Integer> quickResults = index.searchSingleTerm("quick");
        System.out.println("查询 'quick': " + quickResults);
        
        List<Integer> foxResults = index.searchSingleTerm("fox");
        System.out.println("查询 'fox': " + foxResults);
        
        // AND查询
        System.out.println("\n--- AND查询 ---");
        List<Integer> andResults = index.searchWithAND("quick", "fox");
        System.out.println("查询 'quick AND fox': " + andResults);
        
        // OR查询
        System.out.println("\n--- OR查询 ---");
        List<Integer> orResults = index.searchWithOR("dog", "cat");
        System.out.println("查询 'dog OR cat': " + orResults);
        
        // 查看文档内容
        System.out.println("\n--- 文档内容 ---");
        for (int docId : andResults) {
            System.out.println("文档 " + docId + ": " + index.getDocumentContent(docId));
        }
    }
    
    /**
     * 演示高级功能
     */
    public static void demonstrateAdvancedFeatures() {
        System.out.println("\n\n=== 倒排索引高级功能演示 ===");
        
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        // 添加更多文档用于高级功能演示
        index.addDocument(1, "machine learning algorithms are fundamental to artificial intelligence");
        index.addDocument(2, "deep learning is a subset of machine learning");
        index.addDocument(3, "artificial neural networks learn from data");
        index.addDocument(4, "data science combines statistics and computer science");
        index.addDocument(5, "machine learning models require training data");
        
        System.out.println("高级功能演示数据准备完成");
        
        // 相关性评分
        System.out.println("\n--- 相关性评分 ---");
        String query = "machine learning";
        List<InvertedIndexImplementation.DocumentScore> rankedResults = index.searchWithRanking(query);
        
        System.out.println("查询: '" + query + "'");
        System.out.println("相关性排序结果:");
        for (InvertedIndexImplementation.DocumentScore result : rankedResults) {
            System.out.printf("  文档%d (得分: %.4f): %s%n", 
                result.getDocId(), 
                result.getScore(), 
                index.getDocumentContent(result.getDocId()));
        }
        
        // 词汇统计
        System.out.println("\n--- 词汇统计 ---");
        String[] termsToCheck = {"machine", "learning", "data", "algorithms"};
        for (String term : termsToCheck) {
            InvertedIndexImplementation.TermStats stats = index.getTermStats(term);
            if (stats != null) {
                System.out.printf("词汇 '%s': 文档频率=%d, 总频率=%d%n", 
                    term, stats.getDocFreq(), stats.getTotalFreq());
            }
        }
        
        // 多词AND查询
        System.out.println("\n--- 多词AND查询 ---");
        List<Integer> multiAndResults = index.searchWithAND("machine", "learning", "data");
        System.out.println("查询 'machine AND learning AND data': " + multiAndResults);
        
        if (!multiAndResults.isEmpty()) {
            for (int docId : multiAndResults) {
                System.out.println("  文档" + docId + ": " + index.getDocumentContent(docId));
            }
        }
    }
    
    /**
     * 演示性能对比
     */
    public static void demonstratePerformanceComparison() {
        System.out.println("\n\n=== 性能对比演示 ===");
        
        InvertedIndexImplementation index = new InvertedIndexImplementation();
        
        // 生成大量测试数据
        System.out.println("正在生成测试数据...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= 1000; i++) {
            String content = generateRandomDocument(i);
            index.addDocument(i, content);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("索引构建耗时: " + (endTime - startTime) + "ms");
        System.out.println("最终索引统计: " + index.getIndexStats());
        
        // 性能测试
        System.out.println("\n--- 查询性能测试 ---");
        
        String[] testQueries = {
            "test",           // 单词查询
            "test data",      // AND查询
            "hello world"     // 可能无结果的查询
        };
        
        for (String query : testQueries) {
            // 测试查询时间
            startTime = System.currentTimeMillis();
            List<InvertedIndexImplementation.DocumentScore> results = index.searchWithRanking(query);
            endTime = System.currentTimeMillis();
            
            System.out.printf("查询 '%s': 找到 %d 个结果, 耗时 %d ms%n", 
                query, results.size(), (endTime - startTime));
            
            if (results.size() > 0) {
                System.out.println("  前3个结果:");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    InvertedIndexImplementation.DocumentScore result = results.get(i);
                    System.out.printf("    文档%d (得分: %.4f)%n", 
                        result.getDocId(), result.getScore());
                }
            }
        }
    }
    
    /**
     * 生成随机文档内容用于测试
     */
    private static String generateRandomDocument(int docId) {
        StringBuilder sb = new StringBuilder();
        String[] words = {
            "test", "data", "algorithm", "search", "index", "document", "word", "text",
            "information", "retrieval", "database", "query", "result", "performance",
            "system", "application", "software", "computer", "science", "technology",
            "programming", "code", "function", "method", "class", "object", "variable",
            "array", "string", "integer", "float", "boolean", "true", "false", "null",
            "hello", "world", "example", "sample", "demo", "prototype", "version",
            "release", "production", "development", "testing", "debugging", "optimization"
        };
        
        // 每个文档约50个词
        for (int i = 0; i < 50; i++) {
            if (i > 0) sb.append(" ");
            sb.append(words[(docId + i) % words.length]);
        }
        
        return sb.toString();
    }
}