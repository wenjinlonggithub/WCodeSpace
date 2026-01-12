package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 倒排索引算法的完整实现
 * 包含基本功能和性能优化
 */
public class InvertedIndexImplementation {
    
    // 词汇字典：词汇 -> 词汇信息（文档频率等）
    private final Map<String, TermInfo> termDictionary = new HashMap<>();
    // 倒排列表：词汇 -> 包含该词汇的文档ID列表
    private final Map<String, List<Posting>> invertedLists = new HashMap<>();
    // 正排索引：文档ID -> 文档内容
    private final Map<Integer, Document> forwardIndex = new HashMap<>();
    
    // 文档总数，用于TF-IDF计算
    private int totalDocuments = 0;
    
    /**
     * 文档信息类
     */
    static class Document {
        private int docId;
        private String content;
        private int length; // 文档长度
        
        public Document(int docId, String content) {
            this.docId = docId;
            this.content = content;
            this.length = content.split("\\s+").length;
        }
        
        // Getters
        public int getDocId() { return docId; }
        public String getContent() { return content; }
        public int getLength() { return length; }
    }
    
    /**
     * 词汇信息类
     */
    static class TermInfo {
        private int docFrequency;      // 文档频率：包含该词的文档数量
        private long termFrequency;    // 词项总频率：该词在整个语料库中的出现次数
        
        public TermInfo() {
            this.docFrequency = 0;
            this.termFrequency = 0;
        }
        
        public int getDocFrequency() { return docFrequency; }
        public void incrementDocFreq() { this.docFrequency++; }
        public long getTermFrequency() { return termFrequency; }
        public void incrementTermFreq() { this.termFrequency++; }
    }
    
    /**
     * 倒排记录类
     */
    static class Posting {
        private int docId;
        private int termFreq;          // 词项在文档中的频率
        private List<Integer> positions; // 词项在文档中的位置
        
        public Posting(int docId, int termFreq) {
            this.docId = docId;
            this.termFreq = termFreq;
            this.positions = new ArrayList<>();
        }
        
        public Posting(int docId, int termFreq, List<Integer> positions) {
            this.docId = docId;
            this.termFreq = termFreq;
            this.positions = positions != null ? positions : new ArrayList<>();
        }
        
        // Getters
        public int getDocId() { return docId; }
        public int getTermFreq() { return termFreq; }
        public List<Integer> getPositions() { return positions; }
    }
    
    /**
     * 添加文档到索引
     * @param docId 文档ID
     * @param content 文档内容
     */
    public void addDocument(int docId, String content) {
        if (forwardIndex.containsKey(docId)) {
            // 如果文档已存在，先删除旧文档
            removeDocument(docId);
        }
        
        // 添加到正排索引
        Document doc = new Document(docId, content);
        forwardIndex.put(docId, doc);
        totalDocuments++;
        
        // 分词并更新倒排索引
        String[] tokens = preprocess(content).split("\\s+");
        Map<String, Integer> termFreqInDoc = new HashMap<>();
        Map<String, List<Integer>> termPositions = new HashMap<>();
        
        // 统计词频和位置
        for (int pos = 0; pos < tokens.length; pos++) {
            String term = tokens[pos];
            if (term.isEmpty()) continue;
            
            termFreqInDoc.put(term, termFreqInDoc.getOrDefault(term, 0) + 1);
            
            termPositions.computeIfAbsent(term, k -> new ArrayList<>()).add(pos);
        }
        
        // 更新倒排索引
        for (Map.Entry<String, Integer> entry : termFreqInDoc.entrySet()) {
            String term = entry.getKey();
            int freq = entry.getValue();
            List<Integer> positions = termPositions.get(term);
            
            // 创建或更新词汇信息
            TermInfo termInfo = termDictionary.computeIfAbsent(term, k -> new TermInfo());
            termInfo.incrementDocFreq();
            termInfo.incrementTermFreq();
            
            // 创建倒排记录
            Posting posting = new Posting(docId, freq, positions);
            
            // 添加到倒排列表
            invertedLists.computeIfAbsent(term, k -> new ArrayList<>()).add(posting);
        }
    }
    
    /**
     * 删除文档
     * @param docId 文档ID
     */
    public void removeDocument(int docId) {
        Document doc = forwardIndex.remove(docId);
        if (doc == null) return;
        
        totalDocuments--;
        
        // 从倒排索引中移除文档
        String[] tokens = preprocess(doc.getContent()).split("\\s+");
        Map<String, Integer> termFreqInDoc = new HashMap<>();
        
        for (String token : tokens) {
            if (!token.isEmpty()) {
                termFreqInDoc.put(token, termFreqInDoc.getOrDefault(token, 0) + 1);
            }
        }
        
        for (String term : termFreqInDoc.keySet()) {
            List<Posting> postings = invertedLists.get(term);
            if (postings != null) {
                postings.removeIf(posting -> posting.docId == docId);
                
                if (postings.isEmpty()) {
                    invertedLists.remove(term);
                    termDictionary.remove(term);
                } else {
                    // 更新词汇信息
                    TermInfo termInfo = termDictionary.get(term);
                    if (termInfo != null) {
                        termInfo.docFrequency--;
                    }
                }
            }
        }
    }
    
    /**
     * 单词查询
     * @param term 查询词
     * @return 包含该词的文档ID列表
     */
    public List<Integer> searchSingleTerm(String term) {
        List<Posting> postings = invertedLists.get(preprocessTerm(term));
        if (postings == null) {
            return new ArrayList<>();
        }
        
        List<Integer> result = new ArrayList<>();
        for (Posting posting : postings) {
            result.add(posting.getDocId());
        }
        return result;
    }
    
    /**
     * AND查询：返回同时包含所有查询词的文档
     * @param terms 查询词数组
     * @return 匹配的文档ID列表
     */
    public List<Integer> searchWithAND(String... terms) {
        if (terms.length == 0) {
            return new ArrayList<>();
        }
        
        // 首先获取第一个词的结果
        List<Posting> firstPostings = invertedLists.get(preprocessTerm(terms[0]));
        if (firstPostings == null) {
            return new ArrayList<>();
        }
        
        // 转换为文档ID集合以便进行交集操作
        Set<Integer> result = new HashSet<>();
        for (Posting posting : firstPostings) {
            result.add(posting.getDocId());
        }
        
        // 与其他词的结果求交集
        for (int i = 1; i < terms.length; i++) {
            List<Posting> termPostings = invertedLists.get(preprocessTerm(terms[i]));
            if (termPostings == null) {
                return new ArrayList<>(); // 如果任意一个词不存在，则结果为空
            }
            
            Set<Integer> termDocs = new HashSet<>();
            for (Posting posting : termPostings) {
                termDocs.add(posting.getDocId());
            }
            
            result.retainAll(termDocs); // 求交集
        }
        
        return new ArrayList<>(result);
    }
    
    /**
     * OR查询：返回包含任一查询词的文档
     * @param terms 查询词数组
     * @return 匹配的文档ID列表
     */
    public List<Integer> searchWithOR(String... terms) {
        Set<Integer> result = new HashSet<>();
        
        for (String term : terms) {
            List<Posting> postings = invertedLists.get(preprocessTerm(term));
            if (postings != null) {
                for (Posting posting : postings) {
                    result.add(posting.getDocId());
                }
            }
        }
        
        return new ArrayList<>(result);
    }
    
    /**
     * 获取文档内容
     * @param docId 文档ID
     * @return 文档内容
     */
    public String getDocumentContent(int docId) {
        Document doc = forwardIndex.get(docId);
        return doc != null ? doc.getContent() : null;
    }
    
    /**
     * 计算文档与查询的相关性得分（使用TF-IDF算法）
     * @param query 查询字符串
     * @param docId 文档ID
     * @return 相关性得分
     */
    public double calculateRelevanceScore(String query, int docId) {
        String[] queryTerms = preprocess(query).split("\\s+");
        double score = 0.0;
        
        for (String term : queryTerms) {
            if (term.isEmpty()) continue;
            
            // 获取文档中该词的频率
            Posting posting = getPostingForDoc(term, docId);
            if (posting == null) continue;
            
            int termFreq = posting.getTermFreq();
            
            // 计算TF值
            Document doc = forwardIndex.get(docId);
            if (doc == null) continue;
            
            double tf = (double) termFreq / doc.getLength();
            
            // 计算IDF值
            TermInfo termInfo = termDictionary.get(term);
            if (termInfo == null) continue;
            
            double idf = Math.log((double) totalDocuments / termInfo.getDocFrequency());
            
            // TF-IDF得分
            score += tf * idf;
        }
        
        return score;
    }
    
    /**
     * 获取文档排名（按相关性得分排序）
     * @param query 查询字符串
     * @return 排名结果，包含文档ID和得分
     */
    public List<DocumentScore> searchWithRanking(String query) {
        String[] queryTerms = preprocess(query).split("\\s+");
        Set<Integer> candidateDocs = new HashSet<>();
        
        // 收集候选文档
        for (String term : queryTerms) {
            if (term.isEmpty()) continue;
            List<Posting> postings = invertedLists.get(term);
            if (postings != null) {
                for (Posting posting : postings) {
                    candidateDocs.add(posting.getDocId());
                }
            }
        }
        
        // 计算每个候选文档的得分
        List<DocumentScore> results = new ArrayList<>();
        for (int docId : candidateDocs) {
            double score = calculateRelevanceScore(query, docId);
            if (score > 0) {
                results.add(new DocumentScore(docId, score));
            }
        }
        
        // 按得分降序排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return results;
    }
    
    /**
     * 获取词汇统计信息
     */
    public TermStats getTermStats(String term) {
        TermInfo termInfo = termDictionary.get(preprocessTerm(term));
        if (termInfo == null) {
            return null;
        }
        
        List<Posting> postings = invertedLists.get(preprocessTerm(term));
        int totalFreq = 0;
        if (postings != null) {
            for (Posting p : postings) {
                totalFreq += p.getTermFreq();
            }
        }
        
        return new TermStats(term, termInfo.getDocFrequency(), totalFreq);
    }
    
    // 辅助方法
    private Posting getPostingForDoc(String term, int docId) {
        List<Posting> postings = invertedLists.get(preprocessTerm(term));
        if (postings == null) return null;
        
        for (Posting posting : postings) {
            if (posting.getDocId() == docId) {
                return posting;
            }
        }
        return null;
    }
    
    private String preprocess(String text) {
        return text.toLowerCase()
                  .replaceAll("[^a-zA-Z0-9\\s]", " ")
                  .trim();
    }
    
    private String preprocessTerm(String term) {
        return preprocess(term);
    }
    
    /**
     * 文档得分类
     */
    public static class DocumentScore {
        private int docId;
        private double score;
        
        public DocumentScore(int docId, double score) {
            this.docId = docId;
            this.score = score;
        }
        
        public int getDocId() { return docId; }
        public double getScore() { return score; }
        
        @Override
        public String toString() {
            return "DocumentScore{docId=" + docId + ", score=" + String.format("%.4f", score) + "}";
        }
    }
    
    /**
     * 词汇统计信息类
     */
    public static class TermStats {
        private String term;
        private int docFreq;      // 文档频率
        private int totalFreq;    // 总频率
        
        public TermStats(String term, int docFreq, int totalFreq) {
            this.term = term;
            this.docFreq = docFreq;
            this.totalFreq = totalFreq;
        }
        
        public String getTerm() { return term; }
        public int getDocFreq() { return docFreq; }
        public int getTotalFreq() { return totalFreq; }
        
        @Override
        public String toString() {
            return "TermStats{term='" + term + "', docFreq=" + docFreq + ", totalFreq=" + totalFreq + "}";
        }
    }
    
    /**
     * 获取索引统计信息
     */
    public IndexStats getIndexStats() {
        return new IndexStats(
            forwardIndex.size(),
            termDictionary.size(),
            invertedLists.values().stream().mapToInt(List::size).sum()
        );
    }
    
    /**
     * 索引统计信息类
     */
    public static class IndexStats {
        private int numDocuments;
        private int numTerms;
        private int numPostings;
        
        public IndexStats(int numDocuments, int numTerms, int numPostings) {
            this.numDocuments = numDocuments;
            this.numTerms = numTerms;
            this.numPostings = numPostings;
        }
        
        public int getNumDocuments() { return numDocuments; }
        public int getNumTerms() { return numTerms; }
        public int getNumPostings() { return numPostings; }
        
        @Override
        public String toString() {
            return "IndexStats{documents=" + numDocuments + ", terms=" + numTerms + ", postings=" + numPostings + "}";
        }
    }
}