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
        private final TreeMap<Integer, Integer> circle = new TreeMap<>();
        private final int numberOfReplicas;
        
        public ConsistentHashPartitioner(int numPartitions, int numberOfReplicas) {
            this.numberOfReplicas = numberOfReplicas;
            
            for (int i = 0; i < numPartitions; i++) {
                addPartition(i);
            }
        }
        
        private void addPartition(int partition) {
            for (int i = 0; i < numberOfReplicas; i++) {
                String virtualNode = partition + "#" + i;
                int hash = hash(virtualNode);
                circle.put(hash, partition);
            }
        }
        
        public int partition(String key) {
            if (circle.isEmpty()) {
                return 0;
            }
            
            int hash = hash(key);
            SortedMap<Integer, Integer> tailMap = circle.tailMap(hash);
            
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