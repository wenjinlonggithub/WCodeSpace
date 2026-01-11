package com.architecture.algorithm.opensource;

import java.util.*;

/**
 * 一致性哈希分区算法演示类
 * 
 * 展示一致性哈希算法的核心概念、实现细节和应用场景
 */
public class ConsistentHashDemo {
    
    public static void main(String[] args) {
        demonstrateConsistentHashing();
    }
    
    /**
     * 演示一致性哈希算法的工作原理
     */
    public static void consistentHashDemo() {
        demonstrateConsistentHashing();
    }
    
    /**
     * 演示一致性哈希算法的工作原理
     */
    public static void demonstrateConsistentHashing() {
        System.out.println("=== 一致性哈希分区算法演示 ===\n");
        
        // 创建物理节点列表
        List<String> initialNodes = Arrays.asList("Node-1", "Node-2", "Node-3");
        System.out.println("1. 初始化物理节点: " + initialNodes);
        
        // 创建一致性哈希分区器，每个物理节点创建100个虚拟节点
        ConsistentHashPartitioner partitioner = new ConsistentHashPartitioner(initialNodes, 100);
        System.out.println("   每个物理节点创建100个虚拟节点\n");
        
        // 模拟一批键的分配
        List<String> keys = Arrays.asList(
            "user:1001", "user:1002", "user:1003", "user:1004", "user:1005",
            "order:2001", "order:2002", "order:2003", "order:2004", "order:2005",
            "product:3001", "product:3002", "product:3003", "product:3004", "product:3005"
        );
        
        System.out.println("2. 键分配测试:");
        System.out.println("   待分配的键列表: " + keys);
        
        // 分配键到节点
        Map<String, String> keyToNode = new HashMap<>();
        for (String key : keys) {
            String node = partitioner.getNodeForKey(key);
            keyToNode.put(key, node);
            System.out.println("   Key: " + key + " -> " + node);
        }
        
        System.out.println();
        
        // 显示负载分布情况
        System.out.println(partitioner.calculateLoadBalance(keys));
        
        // 演示添加新节点的影响
        System.out.println("3. 添加新节点 Node-4 后的重新分配:");
        String newNode = "Node-4";
        partitioner.addPhysicalNode(newNode);
        
        // 检查哪些键被重新分配
        List<String> reassignCount = new ArrayList<>();
        for (String key : keys) {
            String newNodeAssignment = partitioner.getNodeForKey(key);
            if (!newNodeAssignment.equals(keyToNode.get(key))) {
                reassignCount.add(key);
                System.out.println("   Key: " + key + " 从 " + keyToNode.get(key) + " 重新分配到 " + newNodeAssignment);
            }
        }
        
        System.out.println("   重新分配的键数量: " + reassignCount.size() + "/" + keys.size());
        System.out.println("   重新分配比例: " + String.format("%.2f%%", (double) reassignCount.size() / keys.size() * 100) + "\n");
        
        // 演示删除节点的影响
        System.out.println("4. 删除节点 Node-2 后的重新分配:");
        String removedNode = "Node-2";
        partitioner.removePhysicalNode(removedNode);
        
        // 检查哪些键被重新分配
        int removedReassignCount = 0;
        for (String key : keys) {
            String newNodeAssignment = partitioner.getNodeForKey(key);
            // 注意：此时需要重新计算原始分配，因为节点已改变
            String originalAssignment = keyToNode.get(key);
            if (originalAssignment.equals(removedNode)) {
                // 这些键原本就在被删除的节点上，所以会被重新分配
                System.out.println("   Key: " + key + " (原在 " + originalAssignment + ") 重新分配到 " + newNodeAssignment);
                removedReassignCount++;
            } else if (!newNodeAssignment.equals(originalAssignment)) {
                // 这些键虽然不在被删除的节点上，但也可能因为环的变化而被重新分配
                System.out.println("   Key: " + key + " 从 " + originalAssignment + " 重新分配到 " + newNodeAssignment);
                removedReassignCount++;
            }
        }
        
        System.out.println("   因删除节点而重新分配的键数量: " + removedReassignCount);
        System.out.println("   重新分配比例: " + String.format("%.2f%%", (double) removedReassignCount / keys.size() * 100) + "\n");
        
        // 显示最终的负载分布
        System.out.println("5. 最终负载分布:");
        System.out.println(partitioner.calculateLoadBalance(keys));
        
        // 展示哈希环的详细信息
        System.out.println("6. 哈希环详细信息:");
        System.out.println(partitioner.getDistributionInfo());
        
        // 对比普通哈希算法
        demonstrateComparisonWithRegularHashing(keys, partitioner.getPhysicalNodes());
    }
    
    /**
     * 对比一致性哈希算法与普通哈希算法
     * 
     * @param keys 待分配的键列表
     * @param nodes 物 ph节点列表
     */
    private static void demonstrateComparisonWithRegularHashing(List<String> keys, Set<String> nodes) {
        System.out.println("7. 一致性哈希 vs 普通哈希算法对比:");
        
        // 普通哈希算法
        System.out.println("   普通哈希算法 (hash(key) % nodeCount):");
        List<String> nodeList = new ArrayList<>(nodes);
        
        Map<String, String> regularHashAssignments = new HashMap<>();
        for (String key : keys) {
            int index = Math.abs(key.hashCode()) % nodeList.size();
            String node = nodeList.get(index);
            regularHashAssignments.put(key, node);
            System.out.println("     " + key + " -> " + node);
        }
        
        // 模拟添加节点后的普通哈希算法分配
        System.out.println("\n   添加新节点后普通哈希算法的重新分配:");
        List<String> extendedNodeList = new ArrayList<>(nodeList);
        extendedNodeList.add("Node-4");
        
        int regularReassignCount = 0;
        for (String key : keys) {
            int newIndex = Math.abs(key.hashCode()) % extendedNodeList.size();
            String newNode = extendedNodeList.get(newIndex);
            String oldNode = regularHashAssignments.get(key);
            
            if (!newNode.equals(oldNode)) {
                System.out.println("     " + key + " 从 " + oldNode + " 重新分配到 " + newNode);
                regularReassignCount++;
            }
        }
        
        System.out.println("   普通哈希算法重新分配比例: " + 
                          String.format("%.2f%%", (double) regularReassignCount / keys.size() * 100));
        
        System.out.println("\n   结论:");
        System.out.println("   - 一致性哈希算法在添加/删除节点时，只有少量键需要重新分配");
        System.out.println("   - 普通哈希算法在节点数量变化时，几乎所有键都需要重新分配");
        System.out.println("   - 一致性哈希算法显著减少了数据迁移量，提高了系统伸缩性");
    }
}