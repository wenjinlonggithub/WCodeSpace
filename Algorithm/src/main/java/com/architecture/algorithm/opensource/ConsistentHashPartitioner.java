package com.architecture.algorithm.opensource;

import java.util.*;

/**
 * 一致性哈希分区算法实现
 * 
 * 一致性哈希算法是一种特殊的哈希算法，主要用于解决分布式缓存系统的伸缩性问题。
 * 它能够在增加或删除节点时尽可能少地改变数据与节点之间的映射关系，
 * 从而最小化数据迁移量。
 */
public class ConsistentHashPartitioner {
    
    /**
     * 虚拟节点映射表，存储哈希值到物理节点的映射
     */
    private final TreeMap<Integer, String> circle = new TreeMap<>();
    
    /**
     * 物理节点列表
     */
    private final Set<String> physicalNodes = new HashSet<>();
    
    /**
     * 每个物理节点对应的虚拟节点数量
     */
    private final int numberOfVirtualNodes;
    
    /**
     * 构造函数
     * 
     * @param physicalNodes 物理节点列表
     * @param numberOfVirtualNodes 每个物理节点对应的虚拟节点数量
     */
    public ConsistentHashPartitioner(List<String> physicalNodes, int numberOfVirtualNodes) {
        this.numberOfVirtualNodes = numberOfVirtualNodes;
        
        // 添加所有物理节点及其虚拟节点到哈希环
        for (String node : physicalNodes) {
            addPhysicalNode(node);
        }
    }
    
    /**
     * 添加物理节点到哈希环
     * 
     * 步骤1: 将物理节点添加到物理节点集合
     * 步骤2: 为该物理节点创建指定数量的虚拟节点
     * 步骤3: 计算每个虚拟节点的哈希值并添加到哈希环
     * 
     * @param node 物理节点名称
     */
    public void addPhysicalNode(String node) {
        physicalNodes.add(node);
        
        // 为物理节点创建虚拟节点
        for (int i = 0; i < numberOfVirtualNodes; i++) {
            String virtualNode = node + "#" + i; // 虚拟节点命名格式：物理节点#编号
            int hash = hash(virtualNode);        // 计算虚拟节点的哈希值
            circle.put(hash, node);              // 将哈希值和物理节点映射关系存入哈希环
        }
    }
    
    /**
     * 删除物理节点
     * 
     * 步骤1: 从物理节点集合中移除该节点
     * 步骤2: 从哈希环中移除该节点对应的所有虚拟节点
     * 
     * @param node 要删除的物理节点名称
     */
    public void removePhysicalNode(String node) {
        if (!physicalNodes.contains(node)) {
            return; // 节点不存在，直接返回
        }
        
        physicalNodes.remove(node);
        
        // 删除该物理节点对应的所有虚拟节点
        Iterator<Map.Entry<Integer, String>> iterator = circle.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, String> entry = iterator.next();
            if (entry.getValue().equals(node)) {
                iterator.remove();
            }
        }
    }
    
    /**
     * 获取指定键对应的分区节点
     * 
     * 一致性哈希算法的核心逻辑：
     * 
     * 步骤1: 计算待分配键的哈希值
     * 步骤2: 在哈希环中查找大于等于该哈希值的第一个节点
     * 步骤3: 如果没有找到，则返回哈希环中的第一个节点（形成环状结构）
     * 
     * 这种方式确保了即使节点数量发生变化，也只会有一小部分数据需要重新分配
     * 
     * @param key 待分配的键
     * @return 分配到的物理节点名称
     */
    public String getNodeForKey(String key) {
        if (circle.isEmpty()) {
            return null; // 哈希环为空，返回null
        }
        
        // 计算键的哈希值
        int hash = hash(key);
        
        // 查找大于等于该哈希值的第一个节点
        SortedMap<Integer, String> tailMap = circle.tailMap(hash);
        
        if (tailMap.isEmpty()) {
            // 如果没有找到大于等于该哈希值的节点，则返回哈希环中的第一个节点
            // 这样就形成了环状结构
            return circle.get(circle.firstKey());
        } else {
            // 返回找到的第一个节点
            return tailMap.get(tailMap.firstKey());
        }
    }
    
    /**
     * 计算字符串的哈希值
     * 
     * 使用改进的哈希算法，避免简单hashCode可能导致的分布不均问题
     * 
     * @param str 输入字符串
     * @return 哈希值
     */
    private int hash(String str) {
        // 使用FNV-1a哈希算法的简化版本，提供更好的分布均匀性
        int hash = 0x811c9dc5; // FNV offset basis
        for (char c : str.toCharArray()) {
            hash ^= c;
            hash *= 0x01000193; // FNV prime
        }
        return hash;
    }
    
    /**
     * 获取哈希环中所有节点的分布情况
     * 
     * @return 节点分布信息
     */
    public String getDistributionInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("一致性哈希环分布信息:\n");
        sb.append("物理节点数量: ").append(physicalNodes.size()).append("\n");
        sb.append("虚拟节点数量: ").append(circle.size()).append("\n");
        sb.append("哈希环详情:\n");
        
        for (Map.Entry<Integer, String> entry : circle.entrySet()) {
            sb.append("  Hash: ").append(entry.getKey())
              .append(" -> Node: ").append(entry.getValue()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 计算负载均衡指标
     * 
     * @param keys 待分配的键列表
     * @return 负载分布统计信息
     */
    public String calculateLoadBalance(List<String> keys) {
        Map<String, Integer> distribution = new HashMap<>();
        
        // 统计每个节点分配到的键数量
        for (String key : keys) {
            String node = getNodeForKey(key);
            distribution.put(node, distribution.getOrDefault(node, 0) + 1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("负载分布统计:\n");
        
        int totalKeys = keys.size();
        double average = (double) totalKeys / physicalNodes.size();
        
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            String node = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalKeys * 100;
            double deviation = Math.abs(count - average) / average * 100;
            
            sb.append("  ").append(node).append(": ")
              .append(count).append("个键 (").append(String.format("%.2f", percentage))
              .append("%), 偏差: ").append(String.format("%.2f", deviation)).append("%\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取物理节点列表
     * 
     * @return 物理节点列表
     */
    public Set<String> getPhysicalNodes() {
        return new HashSet<>(physicalNodes);
    }
    
    /**
     * 获取哈希环大小
     * 
     * @return 哈希环中节点的数量
     */
    public int getCircleSize() {
        return circle.size();
    }
}