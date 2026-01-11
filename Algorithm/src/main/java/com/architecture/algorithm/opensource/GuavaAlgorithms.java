package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Google Guava库中算法应用案例
 * 展示Guava库中使用的各种经典算法和数据结构
 */
public class GuavaAlgorithms {
    
    /**
     * 演示Guava中的缓存算法 (LRU + 过期策略)
     */
    public void demonstrateCacheAlgorithm() {
        System.out.println("1. Guava缓存算法 (LRU + 过期策略)");
        
        // 模拟Guava Cache的实现
        GuavaStyleCache<String, String> cache = new GuavaStyleCache<>(100, 300); // 100容量，300秒过期
        
        // 添加缓存项
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        
        System.out.println("   缓存状态: " + cache.getAllKeys());
        System.out.println("   获取key1: " + cache.get("key1"));
        
        // 模拟访问频率统计
        cache.get("key1"); // 增加访问频率
        cache.get("key1"); // 再次增加访问频率
        
        System.out.println("   访问key1两次后，缓存状态: " + cache.getAllKeys());
        
        // 模拟LRU驱逐
        for (int i = 4; i <= 105; i++) {
            cache.put("key" + i, "value" + i);
        }
        
        System.out.println("   添加100个新键后，缓存大小: " + cache.getSize());
    }
    
    /**
     * 演示Guava中的布隆过滤器算法
     */
    public void demonstrateBloomFilter() {
        System.out.println("\n2. Guava布隆过滤器算法");
        
        BloomFilter<String> bloomFilter = new BloomFilter<>(1000, 0.03); // 1000预期元素，3%误判率
        
        // 添加元素
        String[] items = {"apple", "banana", "orange", "pear", "grape"};
        for (String item : items) {
            bloomFilter.put(item);
        }
        
        System.out.println("   布隆过滤器添加元素: " + Arrays.toString(items));
        
        // 测试存在的元素
        for (String item : items) {
            boolean mightContain = bloomFilter.mightContain(item);
            System.out.println("   测试 '" + item + "' 是否存在: " + mightContain);
        }
        
        // 测试不存在的元素
        String[] nonExistentItems = {"watermelon", "kiwi", "peach", "mango"};
        for (String item : nonExistentItems) {
            boolean mightContain = bloomFilter.mightContain(item);
            System.out.println("   测试 '" + item + "' 是否存在: " + mightContain + " (实际不存在)");
        }
    }
    
    /**
     * 演示Guava中的图算法
     */
    public void demonstrateGraphAlgorithm() {
        System.out.println("\n3. Guava图算法");
        
        MutableGraph<String> graph = new MutableGraph<>();
        
        // 添加节点和边
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");
        graph.addEdge("A", "D");
        
        System.out.println("   图结构:");
        System.out.println("   节点: " + graph.getNodes());
        System.out.println("   边: " + graph.getEdges());
        
        // 演示广度优先搜索
        List<String> bfsResult = graph.bfs("A");
        System.out.println("   从A开始的BFS遍历: " + bfsResult);
        
        // 演示深度优先搜索
        List<String> dfsResult = graph.dfs("A");
        System.out.println("   从A开始的DFS遍历: " + dfsResult);
        
        // 检查连通性
        boolean isConnected = graph.isConnected("A", "D");
        System.out.println("   A和D是否连通: " + isConnected);
    }
    
    /**
     * 演示Guava中的集合操作算法
     */
    public void demonstrateCollectionAlgorithms() {
        System.out.println("\n4. Guava集合操作算法");
        
        // 演示不可变集合
        ImmutableCollection<String> immutableList = new ImmutableCollection<>(
            Arrays.asList("a", "b", "c", "d", "e")
        );
        
        System.out.println("   不可变集合: " + immutableList.getList());
        
        // 演示Multiset (计数集合)
        Multiset<String> multiset = new Multiset<>();
        multiset.add("apple");
        multiset.add("banana");
        multiset.add("apple"); // 重复元素
        multiset.add("orange");
        multiset.add("apple"); // 再次重复
        
        System.out.println("   Multiset内容: " + multiset.getElements());
        System.out.println("   'apple' 出现次数: " + multiset.count("apple"));
        System.out.println("   'banana' 出现次数: " + multiset.count("banana"));
        
        // 演示BiMap (双向映射)
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);
        
        System.out.println("   BiMap映射: " + biMap.getMap());
        System.out.println("   通过值反查键 '2': " + biMap.getByValue(2));
        
        // 演示Table (二维映射)
        Table<String, String, Integer> table = new Table<>();
        table.put("Sales", "Q1", 100);
        table.put("Sales", "Q2", 150);
        table.put("Marketing", "Q1", 80);
        table.put("Marketing", "Q2", 90);
        
        System.out.println("   Table内容: " + table.getTable());
        System.out.println("   Sales-Q2的值: " + table.get("Sales", "Q2"));
    }
    
    /**
     * 演示Guava中的函数式编程算法
     */
    public void demonstrateFunctionalAlgorithms() {
        System.out.println("\n5. Guava函数式编程算法");
        
        // 模拟Function接口
        Function<String, Integer> stringLengthFunction = new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                return input.length();
            }
        };
        
        List<String> words = Arrays.asList("hello", "world", "guava", "library", "functions");
        List<Integer> lengths = new ArrayList<>();
        
        for (String word : words) {
            lengths.add(stringLengthFunction.apply(word));
        }
        
        System.out.println("   单词长度映射: " + words + " -> " + lengths);
        
        // 模拟Predicate接口 (过滤)
        Predicate<String> longWordPredicate = new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input.length() > 4;
            }
        };
        
        List<String> longWords = new ArrayList<>();
        for (String word : words) {
            if (longWordPredicate.test(word)) {
                longWords.add(word);
            }
        }
        
        System.out.println("   长度大于4的单词: " + longWords);
        
        // 演示Optional (避免空指针)
        Optional<String> optional1 = Optional.of("valid value");
        Optional<String> optional2 = Optional.empty();
        
        System.out.println("   Optional有值: " + optional1.orElse("default"));
        System.out.println("   Optional空值: " + optional2.orElse("default"));
    }
    
    /**
     * 演示Guava中的哈希算法
     */
    public void demonstrateHashingAlgorithm() {
        System.out.println("\n6. Guava哈希算法");
        
        HashingAlgorithm hasher = new HashingAlgorithm();
        
        String[] inputs = {"hello", "world", "guava", "hashing", "algorithm"};
        
        System.out.println("   哈希算法比较:");
        for (String input : inputs) {
            int hash32 = hasher.hash32(input);
            long hash64 = hasher.hash64(input);
            
            System.out.println("     " + input + " -> 32位哈希: " + hash32 + ", 64位哈希: " + hash64);
        }
        
        // 演示一致性哈希
        ConsistentHash<String> consistentHash = new ConsistentHash<>(Arrays.asList("server1", "server2", "server3"), 100);
        
        String[] keys = {"user1", "user2", "user3", "user4", "user5"};
        System.out.println("   一致性哈希分布:");
        for (String key : keys) {
            String server = consistentHash.getServer(key);
            System.out.println("     " + key + " -> " + server);
        }
    }
    
    // 内部类实现
    static class GuavaStyleCache<K, V> {
        private final int capacity;
        private final long expirySeconds;
        private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
        private final Queue<K> accessQueue = new LinkedList<>();
        
        public GuavaStyleCache(int capacity, long expirySeconds) {
            this.capacity = capacity;
            this.expirySeconds = expirySeconds;
        }
        
        public V get(K key) {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            
            // 检查是否过期
            if (System.currentTimeMillis() - entry.timestamp > expirySeconds * 1000) {
                cache.remove(key);
                return null;
            }
            
            // 更新访问时间（LRU策略的一部分）
            entry.timestamp = System.currentTimeMillis();
            return entry.value;
        }
        
        public void put(K key, V value) {
            // 检查容量
            if (cache.size() >= capacity) {
                // 简单的LRU实现：移除最早添加的元素
                K oldest = accessQueue.poll();
                if (oldest != null) {
                    cache.remove(oldest);
                }
            }
            
            cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
            accessQueue.offer(key);
        }
        
        public Set<K> getAllKeys() {
            return new HashSet<>(cache.keySet());
        }
        
        public int getSize() {
            return cache.size();
        }
        
        static class CacheEntry<V> {
            V value;
            long timestamp;
            
            CacheEntry(V value, long timestamp) {
                this.value = value;
                this.timestamp = timestamp;
            }
        }
    }
    
    static class BloomFilter<T> {
        private final int[] bitArray;
        private final int numHashFunctions;
        private final int expectedElements;
        private final double fpp; // false positive probability
        
        public BloomFilter(int expectedElements, double fpp) {
            this.expectedElements = expectedElements;
            this.fpp = fpp;
            
            // 计算位数组大小和哈希函数数量
            int numBits = (int) (-expectedElements * Math.log(fpp) / (Math.log(2) * Math.log(2)));
            this.numHashFunctions = Math.max(1, (int) (numBits * Math.log(2) / expectedElements));
            
            this.bitArray = new int[(numBits + 31) / 32]; // 用int数组模拟bit array
        }
        
        public void put(T item) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = hash(item, i);
                int index = Math.abs(hash) % (bitArray.length * 32);
                
                int wordIndex = index / 32;
                int bitIndex = index % 32;
                
                bitArray[wordIndex] |= (1 << bitIndex);
            }
        }
        
        public boolean mightContain(T item) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = hash(item, i);
                int index = Math.abs(hash) % (bitArray.length * 32);
                
                int wordIndex = index / 32;
                int bitIndex = index % 32;
                
                if ((bitArray[wordIndex] & (1 << bitIndex)) == 0) {
                    return false;
                }
            }
            return true;
        }
        
        private int hash(T item, int seed) {
            return item.hashCode() ^ seed;
        }
    }
    
    static class MutableGraph<T> {
        private final Map<T, Set<T>> adjacencyList = new HashMap<>();
        
        public void addNode(T node) {
            adjacencyList.putIfAbsent(node, new HashSet<>());
        }
        
        public void addEdge(T from, T to) {
            adjacencyList.computeIfAbsent(from, k -> new HashSet<>()).add(to);
            adjacencyList.computeIfAbsent(to, k -> new HashSet<>()).add(from); // 无向图
        }
        
        public Set<T> getNodes() {
            return adjacencyList.keySet();
        }
        
        public Set<String> getEdges() {
            Set<String> edges = new HashSet<>();
            for (Map.Entry<T, Set<T>> entry : adjacencyList.entrySet()) {
                for (T neighbor : entry.getValue()) {
                    String edge = entry.getKey() + "-" + neighbor;
                    edges.add(edge);
                }
            }
            return edges;
        }
        
        public List<T> bfs(T start) {
            List<T> result = new ArrayList<>();
            Set<T> visited = new HashSet<>();
            Queue<T> queue = new LinkedList<>();
            
            queue.offer(start);
            visited.add(start);
            
            while (!queue.isEmpty()) {
                T current = queue.poll();
                result.add(current);
                
                for (T neighbor : adjacencyList.getOrDefault(current, new HashSet<>())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
            
            return result;
        }
        
        public List<T> dfs(T start) {
            List<T> result = new ArrayList<>();
            Set<T> visited = new HashSet<>();
            dfsHelper(start, visited, result);
            return result;
        }
        
        private void dfsHelper(T node, Set<T> visited, List<T> result) {
            visited.add(node);
            result.add(node);
            
            for (T neighbor : adjacencyList.getOrDefault(node, new HashSet<>())) {
                if (!visited.contains(neighbor)) {
                    dfsHelper(neighbor, visited, result);
                }
            }
        }
        
        public boolean isConnected(T node1, T node2) {
            return bfs(node1).contains(node2);
        }
    }
    
    static class ImmutableCollection<T> {
        private final List<T> list;
        
        public ImmutableCollection(List<T> originalList) {
            this.list = Collections.unmodifiableList(new ArrayList<>(originalList));
        }
        
        public List<T> getList() {
            return list;
        }
    }
    
    static class Multiset<T> {
        private final Map<T, Integer> counts = new HashMap<>();
        
        public void add(T element) {
            counts.put(element, counts.getOrDefault(element, 0) + 1);
        }
        
        public int count(T element) {
            return counts.getOrDefault(element, 0);
        }
        
        public Map<T, Integer> getElements() {
            return new HashMap<>(counts);
        }
    }
    
    static class BiMap<K, V> {
        private final Map<K, V> forwardMap = new HashMap<>();
        private final Map<V, K> reverseMap = new HashMap<>();
        
        public void put(K key, V value) {
            forwardMap.put(key, value);
            reverseMap.put(value, key);
        }
        
        public V get(K key) {
            return forwardMap.get(key);
        }
        
        public K getByValue(V value) {
            return reverseMap.get(value);
        }
        
        public Map<K, V> getMap() {
            return new HashMap<>(forwardMap);
        }
    }
    
    static class Table<R, C, V> {
        private final Map<R, Map<C, V>> table = new HashMap<>();
        
        public void put(R row, C col, V value) {
            table.computeIfAbsent(row, k -> new HashMap<>()).put(col, value);
        }
        
        public V get(R row, C col) {
            Map<C, V> rowMap = table.get(row);
            return rowMap != null ? rowMap.get(col) : null;
        }
        
        public Map<R, Map<C, V>> getTable() {
            Map<R, Map<C, V>> result = new HashMap<>();
            for (Map.Entry<R, Map<C, V>> entry : table.entrySet()) {
                result.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            return result;
        }
    }
    
    interface Function<F, T> {
        T apply(F input);
    }
    
    interface Predicate<T> {
        boolean test(T input);
    }
    
    static class Optional<T> {
        private final T value;
        private final boolean isPresent;
        
        private Optional(T value, boolean isPresent) {
            this.value = value;
            this.isPresent = isPresent;
        }
        
        public static <T> Optional<T> of(T value) {
            return new Optional<>(value, true);
        }
        
        public static <T> Optional<T> empty() {
            return new Optional<>(null, false);
        }
        
        public T orElse(T defaultValue) {
            return isPresent ? value : defaultValue;
        }
    }
    
    static class HashingAlgorithm {
        public int hash32(String input) {
            // 简化的哈希算法
            int hash = 0;
            for (char c : input.toCharArray()) {
                hash = 31 * hash + c;
            }
            return hash;
        }
        
        public long hash64(String input) {
            // 简化的64位哈希算法
            long hash = 0;
            for (char c : input.toCharArray()) {
                hash = 31 * hash + c;
            }
            return hash;
        }
    }
    
    static class ConsistentHash<T> {
        private final TreeMap<Integer, T> circle = new TreeMap<>();
        private final int numberOfReplicas;
        
        public ConsistentHash(List<T> nodes, int numberOfReplicas) {
            this.numberOfReplicas = numberOfReplicas;
            
            for (T node : nodes) {
                addNode(node);
            }
        }
        
        private void addNode(T node) {
            for (int i = 0; i < numberOfReplicas; i++) {
                String virtualNode = node.toString() + "#" + i;
                int hash = hash(virtualNode);
                circle.put(hash, node);
            }
        }
        
        public T getServer(String key) {
            if (circle.isEmpty()) {
                return null;
            }
            
            int hash = hash(key);
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            
            if (tailMap.isEmpty()) {
                return circle.get(circle.firstKey());
            } else {
                return tailMap.get(tailMap.firstKey());
            }
        }
        
        private int hash(String key) {
            return key.hashCode();
        }
    }
    
    public void demonstrate() {
        demonstrateCacheAlgorithm();
        demonstrateBloomFilter();
        demonstrateGraphAlgorithm();
        demonstrateCollectionAlgorithms();
        demonstrateFunctionalAlgorithms();
        demonstrateHashingAlgorithm();
    }
}