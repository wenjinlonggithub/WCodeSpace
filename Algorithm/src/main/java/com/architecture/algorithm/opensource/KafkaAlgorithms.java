package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka中算法应用案例
 * 展示Kafka中使用的各种经典算法和数据结构
 */
public class KafkaAlgorithms {
    
     /**
     * 演示Kafka中的分区算法
     * 
     * 分区算法原理：
     * 1. 哈希分区算法：通过对消息键(Key)进行哈希计算，然后对分区数取模来决定消息进入哪个分区
     *    - 原理：partition = hash(key) % numPartitions
     *    - 优点：相同key的消息总是进入同一个分区，保证了消息顺序性
     *    - 缺点：当分区数发生变化时，会导致大量数据需要重新分布
     * 
     * 2. 轮询分区算法(Round-robin)：依次将消息分配到各个分区
     *    - 原理：按顺序循环分配消息到不同分区
     *    - 优点：能够均匀地分布消息，避免单一分区过载
     *    - 缺点：不保证相同key的消息进入同一分区
     * 
     * 3. 一致性哈希分区：使用虚拟节点的概念，减少因分区数量变化导致的数据迁移
     *    - 背景：传统哈希分区算法在节点增减时会导致大量数据重新分布，造成缓存雪崩等问题
     *    - 原理：将物理节点映射到哈希环上的多个虚拟节点，形成一个连续的哈希环，数据通过哈希值定位到环上最近的节点

     *    - 优点：当添加或删除分区时，只需要移动受影响范围内的数据，大大减少了数据迁移量；负载分布更加均匀；
     *           在分布式系统中能有效解决数据倾斜问题，提高系统整体性能和扩展性

     *    - 缺点：实现复杂度较高；需要维护哈希环结构和虚拟节点映射关系；
     *           当物理节点发生变更时，需要重新计算和调整虚拟节点在哈希环上的位置分布；
     *           在节点数量较少的情况下，可能存在数据分布不均的情况，尽管虚拟节点的设计已经大大改善了这个问题
     *           存在一定的内存开销来存储哈希环结构
     *    - 业务案例：在大型电商平台如淘宝的订单系统中，使用一致性哈希分区可以确保同一用户的订单始终落在同一分区内，
     *                这样在查询用户历史订单时效率更高。同时当系统需要扩容增加新的服务器节点时，大部分用户数据无需迁移，
     *                大大降低了扩容成本和风险。例如，在双11促销活动前增加服务器节点，只会影响一小部分用户的请求路由，
     *                不会因为整个系统的数据重新分布而影响交易流程。
     *    - 实际场景：分布式缓存系统、分布式数据库、P2P网络等需要动态伸缩的分布式系统
     *    - 解决的问题：数据倾斜问题（传统哈希可能造成某些节点负载过高）；动态扩容缩容时的数据迁移开销过大问题；系统伸缩性问题
     * 
     * Kafka为什么要使用分区算法？
     * 1. 提高并发处理能力：多个分区可以并行处理消息，提高吞吐量
     * 2. 数据分散存储：将数据分散到多个分区，避免单点瓶颈
     * 3. 保证消息顺序：对于具有相同key的消息，确保它们在同一个分区内有序
     * 4. 容错性：分区可以在不同broker上复制，提高系统容错能力
     * 
     * 解决的问题：
     * - 负载均衡：确保消息均匀分布在各个分区中
     * - 顺序保证：保证特定key的消息顺序性
     * - 扩展性：允许动态增加分区数量
     * - 高可用：通过分区副本机制保证数据不丢失
     * 
     * 本演示展示了普通哈希分区和一致性哈希分区两种算法的实现原理。
     */
    public void demonstratePartitioningAlgorithm() {
        System.out.println("1. Kafka分区算法");
        
        Partitioner partitioner = new Partitioner(4); // 4个分区
        
        // 模拟消息发送
        String[] keys = {"user1", "user2", "product1", "order1", "user3", "product2"};
        String[] values = {"data1", "data2", "data3", "data4", "data5", "data6"};
        
        System.out.println("   消息分区分配:");
        for (int i = 0; i < keys.length; i++) {
            int partition = partitioner.partition(keys[i], 4);
            System.out.println("     Key: " + keys[i] + " -> 分区 " + partition);
        }
        
        // 演示一致性哈希分区算法
        ConsistentHashPartitioner chPartitioner = new ConsistentHashPartitioner(3, 100);
        System.out.println("   一致性哈希分区 (3个分区，每个100个虚拟节点):");
        for (int i = 0; i < keys.length; i++) {
            int partition = chPartitioner.partition(keys[i]);
            System.out.println("     Key: " + keys[i] + " -> 分区 " + partition);
        }
        
        // 详细演示一致性哈希算法的优势
        demonstrateConsistentHashingAdvantages();
    }
    
    /**
     * 演示一致性哈希算法的优势
     */
    private void demonstrateConsistentHashingAdvantages() {
        System.out.println("   一致性哈希算法优势演示:");
        
        // 创建包含3个分区的一致性哈希分区器
        ConsistentHashPartitioner originalPartitioner = new ConsistentHashPartitioner(3, 50);
        
        // 模拟15个键的分布
        String[] testKeys = {
            "user:1001", "user:1002", "user:1003", "user:1004", "user:1005",
            "order:2001", "order:2002", "order:2003", "order:2004", "order:2005",
            "product:3001", "product:3002", "product:3003", "product:3004", "product:3005"
        };
        
        // 记录原始分布
        Map<String, Integer> originalDistribution = new HashMap<>();
        for (String key : testKeys) {
            originalDistribution.put(key, originalPartitioner.partition(key));
        }
        
        System.out.println("     扩容前分布情况:");
        for (Map.Entry<String, Integer> entry : originalDistribution.entrySet()) {
            System.out.println("       " + entry.getKey() + " -> 分区 " + entry.getValue());
        }
        
        // 添加第4个分区
        originalPartitioner.addNode("Partition-3");
        
        // 检查重新分配的情况
        int reassignmentCount = 0;
        System.out.println("     扩容后重新分配情况:");
        for (String key : testKeys) {
            int newPartition = originalPartitioner.partition(key);
            int originalPartition = originalDistribution.get(key);
            if (newPartition != originalPartition) {
                System.out.println("       " + key + ": 分区 " + originalPartition + " -> 分区 " + newPartition + " (重新分配)");
                reassignmentCount++;
            }
        }
        
        System.out.println("     重新分配比例: " + reassignmentCount + "/" + testKeys.length + 
                          " = " + String.format("%.2f%%", (double) reassignmentCount/testKeys.length * 100));
    }
    
    /**
     * 演示Kafka中的消费者组协调算法
     */
    public void demonstrateConsumerGroupAlgorithm() {
        System.out.println("\n2. Kafka消费者组协调算法");
        
        ConsumerGroupCoordinator coordinator = new ConsumerGroupCoordinator();
        
        // 添加消费者
        Consumer consumer1 = new Consumer("consumer1", Arrays.asList(0, 2));
        Consumer consumer2 = new Consumer("consumer2", Arrays.asList(1, 3));
        
        coordinator.addConsumer(consumer1);
        coordinator.addConsumer(consumer2);
        
        System.out.println("   消费者组成员:");
        System.out.println("     " + consumer1.getId() + " 负责分区: " + consumer1.getAssignedPartitions());
        System.out.println("     " + consumer2.getId() + " 负责分区: " + consumer2.getAssignedPartitions());
        
        // 模拟消费者离开
        System.out.println("   消费者2离开，重新分配分区...");
        coordinator.removeConsumer("consumer2");
        
        // 重新平衡
        coordinator.rebalance();
        System.out.println("   重新平衡后:");
        System.out.println("     " + consumer1.getId() + " 负责分区: " + consumer1.getAssignedPartitions());
    }
    
    /**
     * 演示Kafka中的副本同步算法
     */
    public void demonstrateReplicationAlgorithm() {
        System.out.println("\n3. Kafka副本同步算法");
        
        ReplicaManager replicaManager = new ReplicaManager("topic1", 3); // 3个副本
        
        // 模拟领导者选举
        int leader = replicaManager.electLeader();
        System.out.println("   领导者副本: " + leader);
        
        // 模拟消息同步
        replicaManager.replicateMessage("message1", leader);
        replicaManager.replicateMessage("message2", leader);
        
        System.out.println("   副本同步状态:");
        for (int i = 0; i < 3; i++) {
            System.out.println("     副本" + i + " 同步偏移量: " + replicaManager.getReplicaOffset(i));
        }
        
        // 模拟领导者故障转移
        System.out.println("   领导者副本故障，触发领导者选举...");
        replicaManager.failoverLeader(leader);
        int newLeader = replicaManager.getCurrentLeader();
        System.out.println("   新领导者副本: " + newLeader);
    }
    
    /**
     * 演示Kafka中的消息压缩算法
     */
    public void demonstrateCompressionAlgorithm() {
        System.out.println("\n4. Kafka消息压缩算法");
        
        CompressionManager compressionManager = new CompressionManager();
        
        // 模拟不同类型的消息压缩
        String[] messages = {
            "This is a sample message for compression testing.",
            "This is another message that is quite similar to the first one.",
            "This is a completely different message with unique content.",
            "This is a sample message for compression testing." // 重复消息
        };
        
        System.out.println("   压缩前消息大小:");
        int uncompressedSize = 0;
        for (int i = 0; i < messages.length; i++) {
            int size = messages[i].getBytes().length;
            uncompressedSize += size;
            System.out.println("     消息" + (i+1) + ": " + size + " bytes");
        }
        
        // 演示压缩
        int compressedSize = compressionManager.compress(messages);
        double compressionRatio = (double) compressedSize / uncompressedSize;
        
        System.out.println("   压缩后总大小: " + compressedSize + " bytes");
        System.out.println("   压缩率: " + String.format("%.2f", compressionRatio * 100) + "%");
        System.out.println("   压缩节省: " + (uncompressedSize - compressedSize) + " bytes");
    }
    
    /**
     * 演示Kafka中的偏移量管理算法
     */
    public void demonstrateOffsetManagement() {
        System.out.println("\n5. Kafka偏移量管理算法");
        
        OffsetManager offsetManager = new OffsetManager();
        
        // 模拟消费消息
        for (int i = 0; i < 10; i++) {
            offsetManager.consumeMessage(i);
        }
        
        System.out.println("   当前消费偏移量: " + offsetManager.getCurrentOffset());
        System.out.println("   已提交偏移量: " + offsetManager.getCommittedOffset());
        
        // 提交偏移量
        offsetManager.commitOffset();
        System.out.println("   提交偏移量后，已提交偏移量: " + offsetManager.getCommittedOffset());
        
        // 模拟故障恢复
        System.out.println("   模拟消费者重启，从上次提交的偏移量恢复...");
        offsetManager.recoverFromOffset(offsetManager.getCommittedOffset());
        System.out.println("   恢复后的消费偏移量: " + offsetManager.getCurrentOffset());
    }
    
    /**
     * 演示Kafka中的选举算法
     */
    public void demonstrateElectionAlgorithm() {
        System.out.println("\n6. Kafka领导者选举算法");
        
        LeaderElection election = new LeaderElection(Arrays.asList("broker1", "broker2", "broker3"));
        
        // 模拟选举过程
        String leader = election.electLeader("topic1", 0);
        System.out.println("   Topic: topic1, Partition: 0 的领导者: " + leader);
        
        // 模拟领导者失效
        System.out.println("   领导者失效，触发重新选举...");
        election.onLeaderFailure(leader);
        
        String newLeader = election.electLeader("topic1", 0);
        System.out.println("   重新选举后的领导者: " + newLeader);
    }
    
    // 内部类实现
    static class Partitioner {
        private final int numPartitions;
        
        public Partitioner(int numPartitions) {
            this.numPartitions = numPartitions;
        }
        
        public int partition(String key, int numPartitions) {
            if (key == null) {
                return 0; // 默认分区
            }
            return Math.abs(key.hashCode()) % numPartitions;
        }
    }
    
    static class ConsistentHashPartitioner {
        private final TreeMap<Integer, String> circle = new TreeMap<>();
        private final Map<String, Boolean> nodes = new HashMap<>();
        private final int numberOfReplicas;
        
        public ConsistentHashPartitioner(int numPartitions, int numberOfReplicas) {
            this.numberOfReplicas = numberOfReplicas;
            
            for (int i = 0; i < numPartitions; i++) {
                String node = "Partition-" + i;
                addNode(node);
            }
        }
        
        public void addNode(String node) {
            nodes.put(node, true);
            for (int i = 0; i < numberOfReplicas; i++) {
                String virtualNode = node + "#" + i;
                int hash = hash(virtualNode);
                circle.put(hash, node);
            }
        }
        
        public int partition(String key) {
            if (circle.isEmpty()) {
                return 0;
            }
            
            int hash = hash(key);
            SortedMap<Integer, String> tailMap = circle.tailMap(hash);
            
            if (tailMap.isEmpty()) {
                String firstNode = circle.get(circle.firstKey());
                return Integer.parseInt(firstNode.replace("Partition-", ""));
            } else {
                String targetNode = tailMap.get(tailMap.firstKey());
                return Integer.parseInt(targetNode.replace("Partition-", ""));
            }
        }
        
        private int hash(String key) {
            // 使用改进的哈希算法，提供更好的分布均匀性
            int hash = 0x811c9dc5; // FNV offset basis
            for (char c : key.toCharArray()) {
                hash ^= c;
                hash *= 0x01000193; // FNV prime
            }
            return hash;
        }
    }
    
    static class Consumer {
        private final String id;
        private List<Integer> assignedPartitions;
        
        public Consumer(String id, List<Integer> assignedPartitions) {
            this.id = id;
            this.assignedPartitions = assignedPartitions;
        }
        
        public String getId() { return id; }
        public List<Integer> getAssignedPartitions() { return assignedPartitions; }
        public void setAssignedPartitions(List<Integer> partitions) { this.assignedPartitions = partitions; }
    }
    
    static class ConsumerGroupCoordinator {
        private final Map<String, Consumer> consumers = new HashMap<>();
        
        public void addConsumer(Consumer consumer) {
            consumers.put(consumer.getId(), consumer);
        }
        
        public void removeConsumer(String consumerId) {
            consumers.remove(consumerId);
        }
        
        public void rebalance() {
            // 简化的重平衡算法
            List<String> consumerIds = new ArrayList<>(consumers.keySet());
            Collections.sort(consumerIds);
            
            int partitionIndex = 0;
            for (String consumerId : consumerIds) {
                // 为每个消费者分配分区
                List<Integer> partitions = new ArrayList<>();
                partitions.add(partitionIndex++);
                if (partitionIndex < 4) { // 假设有4个分区
                    partitions.add(partitionIndex++);
                }
                consumers.get(consumerId).setAssignedPartitions(partitions);
            }
        }
    }
    
    static class ReplicaManager {
        private final String topic;
        private final int replicationFactor;
        private final Map<Integer, Long> replicaOffsets = new ConcurrentHashMap<>();
        private int currentLeader;
        private final AtomicLong messageId = new AtomicLong(0);
        
        public ReplicaManager(String topic, int replicationFactor) {
            this.topic = topic;
            this.replicationFactor = replicationFactor;
            
            // 初始化所有副本的偏移量
            for (int i = 0; i < replicationFactor; i++) {
                replicaOffsets.put(i, 0L);
            }
            
            // 选举初始领导者
            this.currentLeader = electLeader();
        }
        
        public int electLeader() {
            // 简化的领导者选举算法（通常是第一个可用的副本）
            return 0;
        }
        
        public void replicateMessage(String message, int leader) {
            // 模拟消息复制到所有副本
            for (int i = 0; i < replicationFactor; i++) {
                long newOffset = replicaOffsets.get(i) + 1;
                replicaOffsets.put(i, newOffset);
            }
        }
        
        public long getReplicaOffset(int replicaId) {
            return replicaOffsets.get(replicaId);
        }
        
        public void failoverLeader(int oldLeader) {
            // 简化的故障转移
            this.currentLeader = (oldLeader + 1) % replicationFactor;
        }
        
        public int getCurrentLeader() {
            return currentLeader;
        }
    }
    
    static class CompressionManager {
        public int compress(String[] messages) {
            // 简化的压缩算法模拟
            // 实际的Kafka支持多种压缩算法：GZIP, Snappy, LZ4, Zstandard
            int originalSize = 0;
            for (String message : messages) {
                originalSize += message.getBytes().length;
            }
            
            // 模拟压缩（实际压缩率取决于数据特性和压缩算法）
            return (int) (originalSize * 0.6); // 假设60%的压缩率
        }
    }
    
    static class OffsetManager {
        private long currentOffset = 0;
        private long committedOffset = 0;
        
        public void consumeMessage(long offset) {
            this.currentOffset = offset + 1;
        }
        
        public void commitOffset() {
            this.committedOffset = this.currentOffset;
        }
        
        public long getCurrentOffset() { return currentOffset; }
        public long getCommittedOffset() { return committedOffset; }
        
        public void recoverFromOffset(long offset) {
            this.currentOffset = offset;
        }
    }
    
    static class LeaderElection {
        private final List<String> brokers;
        private final Map<String, String> partitionLeaders = new HashMap<>();
        
        public LeaderElection(List<String> brokers) {
            this.brokers = brokers;
        }
        
        public String electLeader(String topic, int partition) {
            String key = topic + "-" + partition;
            
            // 简化的领导者选举算法（通常是轮询或基于某些策略）
            int brokerIndex = Math.abs(key.hashCode()) % brokers.size();
            String leader = brokers.get(brokerIndex);
            
            partitionLeaders.put(key, leader);
            return leader;
        }
        
        public void onLeaderFailure(String failedLeader) {
            // 在实际实现中，这会触发重新选举
            System.out.println("     检测到领导者 " + failedLeader + " 故障");
        }
    }
    
    public void demonstrate() {
        demonstratePartitioningAlgorithm();
        demonstrateConsumerGroupAlgorithm();
        demonstrateReplicationAlgorithm();
        demonstrateCompressionAlgorithm();
        demonstrateOffsetManagement();
        demonstrateElectionAlgorithm();
    }
}