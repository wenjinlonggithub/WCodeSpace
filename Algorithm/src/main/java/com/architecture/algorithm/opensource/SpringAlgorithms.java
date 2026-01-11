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
     * 注意：此算法演示了基本的依赖解析逻辑，在实际Spring框架中，
     * 循环依赖问题主要通过三级缓存机制解决，特别是对于单例Bean的循环依赖。
     * 三级缓存包括：
     * 1. singletonObjects: 存储完全初始化的单例Bean
     * 2. earlySingletonObjects: 存储早期暴露的Bean实例（尚未完成属性填充）
     * 3. singletonFactories: 存储Bean工厂，用于创建早期引用
     * 
     * 因此，虽然拓扑排序理论上不能处理有环图，但Spring通过三级缓存机制
     * 巧妙地解决了特定场景下的循环依赖问题（主要是构造器循环依赖仍无法解决）。
     * 此算法主要用于演示目的，展示了如果没有特殊处理，如何检测循环依赖。
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
     * 
     * LRU (Least Recently Used) 缓存算法是一种常用的页面置换算法，在Spring框架中广泛应用。
     * LRU算法基于时间局部性原理，认为最近访问过的数据在未来仍有较高概率被访问。
     * 
     * 算法历史:
     * LRU算法的概念最早出现在计算机内存管理领域，随着计算机系统的发展逐渐成为一种重要的缓存策略。
     * 20世纪60年代，LRU算法被正式提出并广泛应用于操作系统和数据库管理系统中。
     * 在现代Web开发中，LRU算法被广泛应用于各种缓存系统，包括Spring Cache抽象实现。
     * 
     * Spring框架中的演进:
     * - Spring早期版本通过简单的ConcurrentHashMap实现缓存
     * - 随着性能要求提高，引入了更高效的LRU缓存算法
     * - 现代Spring Cache支持多种缓存提供者，许多都基于LRU算法实现
     * 
     * 解决的问题:
     * 1. 内存管理：限制缓存大小，防止内存溢出
     * 2. 数据新鲜度：确保经常访问的数据保留在缓存中
     * 3. 性能优化：减少昂贵的数据获取操作
     * 4. 资源控制：合理分配有限的缓存资源
     * 
     * 核心原理:
     * 1. 维护一个有序列表，记录元素的访问顺序
     * 2. 访问元素时将其移动到列表头部（标记为最新使用）
     * 3. 添加新元素时，如果缓存已满则移除列表尾部元素（最久未使用）
     * 4. 使用哈希表实现O(1)的查找时间复杂度
     * 
     * 实现方式:
     * 本示例使用HashMap + 双向链表的组合实现:
     * - HashMap: 提供O(1)的键值查找
     * - 双向链表: 维护访问顺序，支持O(1)的插入和删除
     * - 虚拟头尾节点: 简化边界条件处理
     * 
     * 核心流程:
     * 1. 初始化
     *    ↓
     * 2. [get/put操作?]
     *    ↙         ↘
     * get操作     put操作
     *    ↓           ↓
     * 3. 查找节点   [缓存是否已存在?]
     *    ↓           ↙           ↘
     * 4. [节点存在?]  否          是
     *    ↙      ↘     ↓           ↓
     * 5. 否      是   新增节点    更新值
     *    ↓      ↓     ↓           ↓
     * 6. 返回null  移动到头部  [容量超限?]  移动到头部
     *             ↓           ↓           ↓
     *           返回值      是           ↓
     *                     ↓            ↓
     *                   删除尾节点     ↓
     *                                ↓
     *                              返回旧值
     * 
     * 时间复杂度: O(1) - 所有操作都是常数时间
     * 空间复杂度: O(capacity) - 取决于缓存容量
     * 
     * 其他应用场景:
     * 1. CPU缓存替换策略
     * 2. 数据库查询缓存
     * 3. Web浏览器页面缓存
     * 4. Redis等缓存系统的过期策略
     * 5. 操作系统页面置换算法
     * 6. 文件系统缓冲区管理
     * 
     * 优势:
     * - 实现简单，易于理解和维护
     * - 时间复杂度低，性能优秀
     * - 符合数据访问的局部性原理
     * 
     * 劣势:
     * - 对于特定访问模式可能不是最优选择
     * - 需要额外的指针维护双向链表结构
     * 
     * 变体算法:
     * - LFU (Least Frequently Used): 基于访问频率
     * - FIFO (First In First Out): 基于进入顺序
     * - MRU (Most Recently Used): 最近最多使用
     * 
     * 在Spring中的具体应用:
     * - Spring Cache抽象提供了@Cacheable注解，底层可使用LRU策略
     * - Spring Session管理用户会话
     * - Spring Security权限缓存
     * - Spring Boot自动配置结果缓存
     * 
     * 缓存策略对比:
     * LRU适用于大多数常见场景，特别是当访问模式具有明显的时间局部性时表现优异。
     * 相比LFU，LRU实现更简单，不需要维护访问计数器；
     * 相比FIFO，LRU考虑了访问模式，更加智能。
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