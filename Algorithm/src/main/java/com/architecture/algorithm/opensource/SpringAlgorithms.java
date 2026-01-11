package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring框架中算法应用案例
 * 展示Spring框架中使用的各种经典算法和数据结构
 */
public class SpringAlgorithms {
    
    /**
     * 演示Spring中的依赖注入算法 - 拓扑排序
     */
    public void demonstrateDependencyInjectionAlgorithm() {
        System.out.println("1. Spring依赖注入算法 - 拓扑排序");
        
        // 模拟Spring IoC容器中的依赖关系
        Map<String, List<String>> dependencies = new HashMap<>();
        dependencies.put("userService", Arrays.asList("userRepository", "emailService"));
        dependencies.put("orderService", Arrays.asList("userService", "paymentService"));
        dependencies.put("paymentService", Arrays.asList("securityService"));
        dependencies.put("emailService", Arrays.asList("configService"));
        dependencies.put("userRepository", Arrays.asList("dataSource"));
        dependencies.put("dataSource", new ArrayList<>()); // 无依赖
        dependencies.put("configService", new ArrayList<>()); // 无依赖
        dependencies.put("securityService", Arrays.asList("configService"));
        
        List<String> injectionOrder = topologicalSort(dependencies);
        System.out.println("   Bean注入顺序: " + injectionOrder);
        
        // 演示循环依赖检测
        Map<String, List<String>> circularDependencies = new HashMap<>();
        circularDependencies.put("A", Arrays.asList("B"));
        circularDependencies.put("B", Arrays.asList("A"));
        
        try {
            topologicalSort(circularDependencies);
            System.out.println("   未检测到循环依赖 - 错误!");
        } catch (IllegalStateException e) {
            System.out.println("   正确检测到循环依赖: " + e.getMessage());
        }
    }
    
    /**
     * 拓扑排序算法 - 用于解决依赖注入顺序
     *
     * 算法历史:
     * 拓扑排序的概念最早可以追溯到19世纪末的数学研究，但其现代形式是在20世纪中期发展起来的。
     * Kahn算法由Arthur B. Kahn在1962年提出，是最早的拓扑排序算法之一。
     * 在计算机科学领域，拓扑排序广泛应用于任务调度、依赖解析、编译系统等场景。
     *
     * Spring框架中的演进:
     * - Spring早期版本使用简单的依赖解析策略
     * - 随着应用复杂度增加，引入了更完善的拓扑排序算法
     * - 现代Spring框架使用优化的算法处理复杂的依赖网络
     *
     * 其他应用场景:
     * 1. Maven/Gradle等构建工具的依赖解析
     * 2. Git的提交历史排序
     * 3. 课程安排系统(先修课程依赖)
     * 4. Excel等电子表格软件的单元格重新计算
     * 5. Docker镜像层的构建依赖
     *
     * 算法流程图:
     * 1. 初始化
     *    ↓
     * 2. 计算每个节点的入度
     *    ↓
     * 3. 将入度为0的节点加入队列
     *    ↓
     * 4. [队列是否为空?]
     *    ↙      ↘
     * 否       是
     *    ↓      ↓
     * 5. 取出节点 → 检查是否存在环
     *    ↓      ↓
     * 6. 加入结果集  ← 抛出异常
     *    ↓
     * 7. 减少邻接节点入度
     *    ↓
     * 8. 若邻接节点入度为0则入队
     *    ↓
     * 9. 返回第4步
     *
     * 依赖关系示意图:
     * dataSource → userRepository → userService → orderService
     *              ↑                                    ↑
     * configService → emailService          paymentService
     *              ↑                                    ↑
     * securityService ————————————————┘
     *
     * 算法背景:
     * 在Spring框架的IoC容器中，Bean之间往往存在复杂的依赖关系。为了正确地初始化这些Bean，
     * 容器必须确定一个合适的创建顺序，使得任何Bean在创建时其依赖的其他Bean已经存在。
     * 这个问题本质上是一个有向无环图(DAG)的拓扑排序问题。
     *
     * 解决的问题:
     * 1. 确定Bean的创建顺序，确保依赖的Bean先于被依赖的Bean创建
     * 2. 检测并报告循环依赖(如A依赖B，B又依赖A)，这在Spring中是不允许的
     * 3. 提高容器启动效率，避免不必要的重复检查
     *
     * 核心原理:
     * 1. 图的表示: 将每个Bean看作图中的节点，依赖关系看作有向边(A依赖B，则存在边B->A)
     * 2. 入度计算: 计算每个节点的入度(有多少个其他节点依赖它)
     * 3. Kahn算法:
     *    a) 找到所有入度为0的节点(没有依赖的Bean)，加入队列
     *    b) 从队列中取出一个节点，将其加入结果列表
     *    c) 移除该节点的所有出边(即减少其邻居节点的入度)
     *    d) 如果某个邻居节点的入度变为0，则将其加入队列
     *    e) 重复b-d直到队列为空
     * 4. 循环依赖检测: 如果最终结果列表的大小小于原图节点数，说明存在环
     *
     * 时间复杂度: O(V + E)，其中V是节点数(Bean数量)，E是边数(依赖关系数量)
     * 空间复杂度: O(V + E)，用于存储图和辅助数据结构
     *
     * 算法背景:
     * 在Spring框架的IoC容器中，Bean之间往往存在复杂的依赖关系。为了正确地初始化这些Bean，
     * 容器必须确定一个合适的创建顺序，使得任何Bean在创建时其依赖的其他Bean已经存在。
     * 这个问题本质上是一个有向无环图(DAG)的拓扑排序问题。
     *
     * 解决的问题:
     * 1. 确定Bean的创建顺序，确保依赖的Bean先于被依赖的Bean创建
     * 2. 检测并报告循环依赖(如A依赖B，B又依赖A)，这在Spring中是不允许的
     * 3. 提高容器启动效率，避免不必要的重复检查
     *
     * 核心原理:
     * 1. 图的表示: 将每个Bean看作图中的节点，依赖关系看作有向边(A依赖B，则存在边B->A)
     * 2. 入度计算: 计算每个节点的入度(有多少个其他节点依赖它)
     * 3. Kahn算法:
     *    a) 找到所有入度为0的节点(没有依赖的Bean)，加入队列
     *    b) 从队列中取出一个节点，将其加入结果列表
     *    c) 移除该节点的所有出边(即减少其邻居节点的入度)
     *    d) 如果某个邻居节点的入度变为0，则将其加入队列
     *    e) 重复b-d直到队列为空
     * 4. 循环依赖检测: 如果最终结果列表的大小小于原图节点数，说明存在环
     *
     * 时间复杂度: O(V + E)，其中V是节点数(Bean数量)，E是边数(依赖关系数量)
     * 空间复杂度: O(V + E)，用于存储图和辅助数据结构
     */
    private List<String> topologicalSort(Map<String, List<String>> dependencies) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Set<String>> graph = new HashMap<>();
        
        // 初始化所有节点
        for (String node : dependencies.keySet()) {
            inDegree.put(node, 0);
            graph.put(node, new HashSet<>());
        }
        
        // 构建图和入度表
        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                graph.get(to).add(from);
                inDegree.put(from, inDegree.get(from) + 1);
            }
        }
        
        // Kahn算法执行拓扑排序
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            
            for (String neighbor : graph.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // 检测循环依赖
        if (result.size() != dependencies.size()) {
            throw new IllegalStateException("检测到循环依赖，无法完成拓扑排序");
        }
        
        return result;
    }
    
    /**
     * 演示Spring中的LRU缓存算法
     */
    public void demonstrateLRUCacheAlgorithm() {
        System.out.println("\n2. Spring中的LRU缓存算法");
        
        LRUCache<String, String> lruCache = new LRUCache<>(3);
        
        lruCache.put("key1", "value1");
        lruCache.put("key2", "value2");
        lruCache.put("key3", "value3");
        
        System.out.println("   缓存状态: " + lruCache.getAllEntries());
        
        lruCache.get("key1"); // 访问key1，使其变为最近使用
        lruCache.put("key4", "value4"); // 添加新元素，淘汰最久未使用的key2
        
        System.out.println("   访问key1后添加key4: " + lruCache.getAllEntries());
    }
    
    /**
     * 演示Spring AOP中的代理算法
     */
    public void demonstrateAOPProxyAlgorithm() {
        System.out.println("\n3. Spring AOP代理算法");
        
        // 模拟JDK动态代理算法
        System.out.println("   JDK动态代理算法:");
        System.out.println("   - 代理对象实现了目标对象的接口");
        System.out.println("   - 使用反射机制调用目标方法");
        System.out.println("   - 在InvocationHandler中添加横切逻辑");
        
        // 模拟CGLIB代理算法
        System.out.println("\n   CGLIB代理算法:");
        System.out.println("   - 通过字节码技术生成目标类的子类");
        System.out.println("   - 重写父类的非final方法");
        System.out.println("   - 性能通常优于JDK动态代理");
    }
    
    /**
     * 演示Spring事件监听器算法
     */
    public void demonstrateEventListenerAlgorithm() {
        System.out.println("\n4. Spring事件监听器算法");
        
        EventPublisher publisher = new EventPublisher();
        
        // 添加不同类型的监听器
        publisher.addEventListener(new UserCreatedListener());
        publisher.addEventListener(new UserUpdatedListener());
        publisher.addEventListener(new UserDeletedListener());
        
        // 发布事件
        publisher.publishEvent(new UserCreatedEvent("user123"));
        publisher.publishEvent(new UserUpdatedEvent("user123"));
    }
    
    /**
     * LRU缓存实现 - 基于HashMap和双向链表
     */
    static class LRUCache<K, V> {
        private final int capacity;
        private final Map<K, Node<K, V>> cache;
        private Node<K, V> head;
        private Node<K, V> tail;
        
        public LRUCache(int capacity) {
            this.capacity = capacity;
            this.cache = new ConcurrentHashMap<>();
            // 创建虚拟头尾节点
            this.head = new Node<>(null, null);
            this.tail = new Node<>(null, null);
            head.next = tail;
            tail.prev = head;
        }
        
        public V get(K key) {
            Node<K, V> node = cache.get(key);
            if (node == null) {
                return null;
            }
            moveToHead(node);
            return node.value;
        }
        
        public void put(K key, V value) {
            Node<K, V> node = cache.get(key);
            if (node == null) {
                Node<K, V> newNode = new Node<>(key, value);
                cache.put(key, newNode);
                addToHead(newNode);
                
                if (cache.size() > capacity) {
                    Node<K, V> tail = removeTail();
                    cache.remove(tail.key);
                }
            } else {
                node.value = value;
                moveToHead(node);
            }
        }
        
        private void addToHead(Node<K, V> node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
        }
        
        private void removeNode(Node<K, V> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        
        private void moveToHead(Node<K, V> node) {
            removeNode(node);
            addToHead(node);
        }
        
        private Node<K, V> removeTail() {
            Node<K, V> res = tail.prev;
            removeNode(res);
            return res;
        }
        
        public Map<K, V> getAllEntries() {
            Map<K, V> result = new LinkedHashMap<>();
            Node<K, V> current = head.next;
            while (current != tail) {
                result.put(current.key, current.value);
                current = current.next;
            }
            return result;
        }
        
        static class Node<K, V> {
            K key;
            V value;
            Node<K, V> prev;
            Node<K, V> next;
            
            Node(K key, V value) {
                this.key = key;
                this.value = value;
            }
        }
    }
    
    /**
     * 事件发布者
     */
    static class EventPublisher {
        private final List<EventListener> listeners = new ArrayList<>();
        
        public void addEventListener(EventListener listener) {
            listeners.add(listener);
        }
        
        public void publishEvent(Event event) {
            for (EventListener listener : listeners) {
                if (listener.supports(event)) {
                    listener.handle(event);
                }
            }
        }
    }
    
    /**
     * 事件基类
     */
    static class Event {
        private final String type;
        private final Object source;
        
        public Event(String type, Object source) {
            this.type = type;
            this.source = source;
        }
        
        public String getType() { return type; }
        public Object getSource() { return source; }
    }
    
    /**
     * 用户创建事件
     */
    static class UserCreatedEvent extends Event {
        public UserCreatedEvent(String userId) {
            super("USER_CREATED", userId);
        }
    }
    
    /**
     * 用户更新事件
     */
    static class UserUpdatedEvent extends Event {
        public UserUpdatedEvent(String userId) {
            super("USER_UPDATED", userId);
        }
    }
    
    /**
     * 事件监听器接口
     */
    interface EventListener {
        boolean supports(Event event);
        void handle(Event event);
    }
    
    /**
     * 用户创建监听器
     */
    static class UserCreatedListener implements EventListener {
        @Override
        public boolean supports(Event event) {
            return "USER_CREATED".equals(event.getType());
        }
        
        @Override
        public void handle(Event event) {
            System.out.println("   用户创建事件处理: " + event.getSource());
        }
    }
    
    /**
     * 用户更新监听器
     */
    static class UserUpdatedListener implements EventListener {
        @Override
        public boolean supports(Event event) {
            return "USER_UPDATED".equals(event.getType());
        }
        
        @Override
        public void handle(Event event) {
            System.out.println("   用户更新事件处理: " + event.getSource());
        }
    }
    
    /**
     * 用户删除监听器
     */
    static class UserDeletedListener implements EventListener {
        @Override
        public boolean supports(Event event) {
            return "USER_DELETED".equals(event.getType());
        }
        
        @Override
        public void handle(Event event) {
            System.out.println("   用户删除事件处理: " + event.getSource());
        }
    }
    
    public void demonstrate() {
        demonstrateDependencyInjectionAlgorithm();
        demonstrateLRUCacheAlgorithm();
        demonstrateAOPProxyAlgorithm();
        demonstrateEventListenerAlgorithm();
    }
}