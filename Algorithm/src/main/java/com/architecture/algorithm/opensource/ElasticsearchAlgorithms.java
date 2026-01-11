package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Elasticsearch中算法应用案例
 * 展示Elasticsearch中使用的各种经典算法和数据结构
 */
public class ElasticsearchAlgorithms {
    
    /**
     * 演示Elasticsearch中的倒排索引算法
     */
    public void demonstrateInvertedIndex() {
        System.out.println("1. Elasticsearch倒排索引算法");
        
        InvertedIndex index = new InvertedIndex();
        
        // 添加文档
        index.addDocument(1, "the quick brown fox jumps over the lazy dog");
        index.addDocument(2, "a quick brown fox runs fast");
        index.addDocument(3, "lazy dogs sleep all day");
        
        System.out.println("   倒排索引构建完成:");
        System.out.println("   " + index.getIndex());
        
        // 执行搜索
        List<Integer> results = index.search("quick brown");
        System.out.println("   搜索 'quick brown' 结果: " + results);
    }
    
    /**
     * 演示Elasticsearch中的评分算法 (TF-IDF)
     */
    public void demonstrateTFIDF() {
        System.out.println("\n2. Elasticsearch评分算法 (TF-IDF)");
        
        TFIDFCalculator tfidf = new TFIDFCalculator();
        
        // 模拟文档集合
        List<String> documents = Arrays.asList(
            "the quick brown fox jumps over the lazy dog",
            "a quick brown fox runs fast", 
            "lazy dogs sleep all day",
            "fox runs quickly through the forest"
        );
        
        // 计算TF-IDF分数
        Map<String, Double> scores = tfidf.calculateTFIDF(documents.get(0), documents);
        System.out.println("   文档1的TF-IDF分数:");
        scores.entrySet().stream()
              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
              .limit(5)
              .forEach(entry -> 
                  System.out.println("     " + entry.getKey() + ": " + String.format("%.4f", entry.getValue())));
    }
    
    /**
     * 演示Elasticsearch中的分片算法
     */
    public void demonstrateShardingAlgorithm() {
        System.out.println("\n3. Elasticsearch分片算法");
        
        ShardingStrategy sharding = new ShardingStrategy(5); // 5个分片
        
        // 模拟文档路由
        String[] documentIds = {"doc1", "doc2", "doc3", "doc4", "doc5", "doc6", "doc7", "doc8"};
        
        System.out.println("   文档路由到分片:");
        for (String docId : documentIds) {
            int shard = sharding.calculateShard(docId);
            System.out.println("     " + docId + " -> 分片 " + shard);
        }
        
        // 演示一致性哈希算法
        ConsistentHashSharding consistentHash = new ConsistentHashSharding(3, 150);
        System.out.println("   一致性哈希分片 (3个节点，每个节点150个虚拟节点):");
        for (String docId : documentIds) {
            String node = consistentHash.getNode(docId);
            System.out.println("     " + docId + " -> 节点 " + node);
        }
    }
    
    /**
     * 演示Elasticsearch中的聚合算法
     */
    public void demonstrateAggregationAlgorithm() {
        System.out.println("\n4. Elasticsearch聚合算法");
        
        AggregationEngine aggregationEngine = new AggregationEngine();
        
        // 添加测试数据
        aggregationEngine.addDataPoint(new DataPoint("sales", 100, "2023-01"));
        aggregationEngine.addDataPoint(new DataPoint("sales", 150, "2023-01"));
        aggregationEngine.addDataPoint(new DataPoint("sales", 200, "2023-02"));
        aggregationEngine.addDataPoint(new DataPoint("marketing", 80, "2023-01"));
        aggregationEngine.addDataPoint(new DataPoint("marketing", 120, "2023-02"));
        
        // 执行聚合操作
        Map<String, Integer> sumAgg = aggregationEngine.sumAggregation("category", "value");
        System.out.println("   按类别求和聚合:");
        sumAgg.forEach((category, sum) -> 
            System.out.println("     " + category + ": " + sum));
        
        Map<String, Integer> countAgg = aggregationEngine.countAggregation("category");
        System.out.println("   按类别计数聚合:");
        countAgg.forEach((category, count) -> 
            System.out.println("     " + category + ": " + count));
    }
    
    /**
     * 演示Elasticsearch中的相似度算法
     */
    public void demonstrateSimilarityAlgorithm() {
        System.out.println("\n5. Elasticsearch相似度算法 (BM25)");
        
        BM25Similarity bm25 = new BM25Similarity();
        
        String query = "quick brown fox";
        String doc1 = "the quick brown fox jumps over the lazy dog";
        String doc2 = "a quick brown fox runs fast";
        String doc3 = "lazy dogs sleep all day";
        
        double score1 = bm25.calculateScore(query, doc1, Arrays.asList(doc1, doc2, doc3));
        double score2 = bm25.calculateScore(query, doc2, Arrays.asList(doc1, doc2, doc3));
        double score3 = bm25.calculateScore(query, doc3, Arrays.asList(doc1, doc2, doc3));
        
        System.out.println("   查询: \"" + query + "\"");
        System.out.println("   文档1 \"" + doc1.substring(0, 15) + "...\" 得分: " + String.format("%.4f", score1));
        System.out.println("   文档2 \"" + doc2.substring(0, 15) + "...\" 得分: " + String.format("%.4f", score2));
        System.out.println("   文档3 \"" + doc3.substring(0, 15) + "...\" 得分: " + String.format("%.4f", score3));
    }
    
    /**
     * 演示Elasticsearch中的熔断器算法
     */
    public void demonstrateCircuitBreakerAlgorithm() {
        System.out.println("\n6. Elasticsearch熔断器算法");
        
        CircuitBreaker circuitBreaker = new CircuitBreaker(5, 60000); // 5次失败，60秒重置
        
        // 模拟请求
        for (int i = 0; i < 8; i++) {
            boolean success = (i < 3); // 前3次成功，之后失败
            System.out.println("   请求 " + (i+1) + ": " + (success ? "成功" : "失败"));
            
            if (success) {
                circuitBreaker.recordSuccess();
            } else {
                circuitBreaker.recordFailure();
            }
            
            CircuitState state = circuitBreaker.getState();
            System.out.println("     熔断器状态: " + state);
            
            if (state == CircuitState.OPEN) {
                System.out.println("     熔断器开启，拒绝后续请求...");
                break;
            }
        }
    }
    
    // 内部类实现
    static class InvertedIndex {
        private final Map<String, Set<Integer>> index = new HashMap<>();
        private final Map<Integer, String> documents = new HashMap<>();
        
        public void addDocument(int docId, String content) {
            documents.put(docId, content);
            String[] words = content.toLowerCase().split("\\s+");
            
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", ""); // 移除非字母字符
                if (!word.isEmpty()) {
                    index.computeIfAbsent(word, k -> new HashSet<>()).add(docId);
                }
            }
        }
        
        public List<Integer> search(String query) {
            String[] terms = query.toLowerCase().split("\\s+");
            Set<Integer> result = null;
            
            for (String term : terms) {
                term = term.replaceAll("[^a-zA-Z]", "");
                if (!term.isEmpty()) {
                    Set<Integer> termResults = index.get(term);
                    if (termResults != null) {
                        if (result == null) {
                            result = new HashSet<>(termResults);
                        } else {
                            result.retainAll(termResults); // 交集操作
                        }
                    } else {
                        result = new HashSet<>(); // 如果某个词不存在，结果为空
                        break;
                    }
                }
            }
            
            return result != null ? new ArrayList<>(result) : new ArrayList<>();
        }
        
        public Map<String, Set<Integer>> getIndex() {
            return new HashMap<>(index);
        }
    }
    
    static class TFIDFCalculator {
        public Map<String, Double> calculateTFIDF(String document, List<String> corpus) {
            Map<String, Double> scores = new HashMap<>();
            String[] words = document.toLowerCase().split("\\s+");
            
            // 计算文档频率
            Map<String, Integer> docFreq = new HashMap<>();
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                if (!word.isEmpty()) {
                    docFreq.put(word, docFreq.getOrDefault(word, 0) + 1);
                }
            }
            
            int totalWords = words.length;
            
            // 计算每个词的TF-IDF
            for (String word : docFreq.keySet()) {
                double tf = (double) docFreq.get(word) / totalWords;
                
                // 计算IDF
                int docsWithWord = 0;
                for (String doc : corpus) {
                    if (doc.toLowerCase().contains(word)) {
                        docsWithWord++;
                    }
                }
                
                double idf = Math.log((double) corpus.size() / (1 + docsWithWord));
                
                scores.put(word, tf * idf);
            }
            
            return scores;
        }
    }
    
    static class ShardingStrategy {
        private final int numShards;
        
        public ShardingStrategy(int numShards) {
            this.numShards = numShards;
        }
        
        public int calculateShard(String id) {
            return Math.abs(id.hashCode()) % numShards;
        }
    }
    
    static class ConsistentHashSharding {
        private final TreeMap<Integer, String> circle = new TreeMap<>();
        private final int numberOfReplicas;
        
        public ConsistentHashSharding(int numberOfNodes, int numberOfReplicas) {
            this.numberOfReplicas = numberOfReplicas;
            
            for (int i = 0; i < numberOfNodes; i++) {
                String node = "node" + i;
                addNode(node);
            }
        }
        
        private void addNode(String node) {
            for (int i = 0; i < numberOfReplicas; i++) {
                String virtualNode = node + "#" + i;
                int hash = hash(virtualNode);
                circle.put(hash, node);
            }
        }
        
        public String getNode(String key) {
            if (circle.isEmpty()) {
                return null;
            }
            
            int hash = hash(key);
            SortedMap<Integer, String> tailMap = circle.tailMap(hash);
            
            if (tailMap.isEmpty()) {
                // 如果找不到大于等于hash的节点，返回第一个节点
                return circle.get(circle.firstKey());
            } else {
                return tailMap.get(tailMap.firstKey());
            }
        }
        
        private int hash(String key) {
            return key.hashCode();
        }
    }
    
    static class AggregationEngine {
        private final List<DataPoint> dataPoints = new ArrayList<>();
        
        public void addDataPoint(DataPoint point) {
            dataPoints.add(point);
        }
        
        public Map<String, Integer> sumAggregation(String groupField, String valueField) {
            Map<String, Integer> result = new HashMap<>();
            
            for (DataPoint point : dataPoints) {
                String groupValue = point.getCategory(); // 假设groupField是category
                int value = point.getValue(); // 假设valueField是value
                
                result.put(groupValue, result.getOrDefault(groupValue, 0) + value);
            }
            
            return result;
        }
        
        public Map<String, Integer> countAggregation(String groupField) {
            Map<String, Integer> result = new HashMap<>();
            
            for (DataPoint point : dataPoints) {
                String groupValue = point.getCategory();
                result.put(groupValue, result.getOrDefault(groupValue, 0) + 1);
            }
            
            return result;
        }
    }
    
    static class DataPoint {
        private String category;
        private int value;
        private String date;
        
        public DataPoint(String category, int value, String date) {
            this.category = category;
            this.value = value;
            this.date = date;
        }
        
        public String getCategory() { return category; }
        public int getValue() { return value; }
        public String getDate() { return date; }
    }
    
    static class BM25Similarity {
        private static final double K1 = 1.2; // 控制文档频率饱和度的参数
        private static final double B = 0.75; // 控制文档长度归一化的参数
        
        public double calculateScore(String query, String document, List<String> corpus) {
            String[] queryTerms = query.toLowerCase().split("\\s+");
            String[] docTerms = document.toLowerCase().split("\\s+");
            
            double score = 0.0;
            
            // 计算文档长度
            int docLength = docTerms.length;
            double avgDocLength = corpus.stream().mapToInt(d -> d.split("\\s+").length).average().orElse(1.0);
            
            for (String term : queryTerms) {
                term = term.replaceAll("[^a-zA-Z]", "");
                if (term.isEmpty()) continue;
                
                // 计算项频率
                final String finalTerm2 = term; // 使变量成为有效的最终变量
                long termFreq = Arrays.stream(docTerms)
                                    .map(t -> t.replaceAll("[^a-zA-Z]", ""))
                                    .filter(t -> t.equals(finalTerm2))
                                    .count();
                
                // 计算包含该词的文档数
                final String finalTerm = term; // 使变量成为有效的最终变量
                long docsContainingTerm = corpus.stream()
                                              .filter(d -> d.toLowerCase().contains(finalTerm))
                                              .count();
                
                // 计算IDF
                double idf = Math.log(1.0 + (corpus.size() - docsContainingTerm + 0.5) / (docsContainingTerm + 0.5));
                
                // 计算分子部分
                double numerator = termFreq * (K1 + 1);
                // 计算分母部分
                double denominator = termFreq + K1 * (1 - B + B * docLength / avgDocLength);
                
                score += idf * (numerator / denominator);
            }
            
            return score;
        }
    }
    
    enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }
    
    static class CircuitBreaker {
        private final int failureThreshold;
        private final long timeoutMillis;
        private int failureCount = 0;
        private long lastFailureTime = 0;
        private CircuitState state = CircuitState.CLOSED;
        
        public CircuitBreaker(int failureThreshold, long timeoutMillis) {
            this.failureThreshold = failureThreshold;
            this.timeoutMillis = timeoutMillis;
        }
        
        public void recordFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            
            if (failureCount >= failureThreshold) {
                state = CircuitState.OPEN;
            }
        }
        
        public void recordSuccess() {
            failureCount = 0;
            state = CircuitState.CLOSED;
        }
        
        public CircuitState getState() {
            if (state == CircuitState.OPEN && 
                System.currentTimeMillis() - lastFailureTime > timeoutMillis) {
                state = CircuitState.HALF_OPEN; // 进入半开状态，尝试恢复
            }
            return state;
        }
    }
    
    public void demonstrate() {
        demonstrateInvertedIndex();
        demonstrateTFIDF();
        demonstrateShardingAlgorithm();
        demonstrateAggregationAlgorithm();
        demonstrateSimilarityAlgorithm();
        demonstrateCircuitBreakerAlgorithm();
    }
}