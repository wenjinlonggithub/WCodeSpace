package com.architecture.index;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * B+树索引结构模拟实现
 * 演示MySQL中B+树索引的核心原理：
 * 1. 非叶子节点只存储键值，不存储数据
 * 2. 叶子节点存储完整数据，并通过链表连接
 * 3. 所有叶子节点在同一层
 * 4. 支持范围查询和点查询
 */
public class BPlusTree<K extends Comparable<K>, V> {
    
    // B+树的度（每个节点最多包含的子节点数）
    private static final int DEFAULT_DEGREE = 4;
    
    private Node<K, V> root;
    private final int degree;
    private int height;
    private long nodeIdCounter = 0;
    
    // 读写锁保护B+树结构
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 叶子节点链表的头尾指针
    private LeafNode<K, V> firstLeaf;
    private LeafNode<K, V> lastLeaf;
    
    public BPlusTree() {
        this(DEFAULT_DEGREE);
    }
    
    public BPlusTree(int degree) {
        if (degree < 3) {
            throw new IllegalArgumentException("B+树的度必须至少为3");
        }
        this.degree = degree;
        this.root = new LeafNode<>(generateNodeId());
        this.firstLeaf = (LeafNode<K, V>) root;
        this.lastLeaf = this.firstLeaf;
        this.height = 1;
    }
    
    /**
     * 节点抽象类
     */
    abstract static class Node<K extends Comparable<K>, V> {
        protected List<K> keys;
        protected Node<K, V> parent;
        protected final long nodeId;
        protected boolean isRoot;
        
        public Node(long nodeId) {
            this.nodeId = nodeId;
            this.keys = new ArrayList<>();
            this.parent = null;
            this.isRoot = false;
        }
        
        public abstract boolean isLeaf();
        public abstract int getSize();
        public abstract void print(int level);
        
        public long getNodeId() { return nodeId; }
        public List<K> getKeys() { return new ArrayList<>(keys); }
        public Node<K, V> getParent() { return parent; }
        public void setParent(Node<K, V> parent) { this.parent = parent; }
        public boolean isRoot() { return isRoot; }
        public void setRoot(boolean root) { isRoot = root; }
    }
    
    /**
     * 内部节点（非叶子节点）
     */
    static class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
        private List<Node<K, V>> children;
        
        public InternalNode(long nodeId) {
            super(nodeId);
            this.children = new ArrayList<>();
        }
        
        @Override
        public boolean isLeaf() {
            return false;
        }
        
        @Override
        public int getSize() {
            return keys.size();
        }
        
        public void addChild(Node<K, V> child) {
            children.add(child);
            child.setParent(this);
        }
        
        public void insertChild(int index, Node<K, V> child) {
            children.add(index, child);
            child.setParent(this);
        }
        
        public void removeChild(Node<K, V> child) {
            children.remove(child);
            child.setParent(null);
        }
        
        public List<Node<K, V>> getChildren() {
            return new ArrayList<>(children);
        }
        
        public Node<K, V> getChild(int index) {
            return children.get(index);
        }
        
        public int getChildIndex(Node<K, V> child) {
            return children.indexOf(child);
        }
        
        @Override
        public void print(int level) {
            String indent = "  ".repeat(level);
            System.out.printf("%s📁 InternalNode[%d]: keys=%s%n", 
                indent, nodeId, keys);
            
            for (Node<K, V> child : children) {
                child.print(level + 1);
            }
        }
    }
    
    /**
     * 叶子节点
     */
    static class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
        private List<V> values;
        private LeafNode<K, V> next;
        private LeafNode<K, V> prev;
        
        public LeafNode(long nodeId) {
            super(nodeId);
            this.values = new ArrayList<>();
        }
        
        @Override
        public boolean isLeaf() {
            return true;
        }
        
        @Override
        public int getSize() {
            return keys.size();
        }
        
        public void addEntry(K key, V value) {
            int insertIndex = Collections.binarySearch(keys, key);
            if (insertIndex < 0) {
                insertIndex = -(insertIndex + 1);
            }
            keys.add(insertIndex, key);
            values.add(insertIndex, value);
        }
        
        public void removeEntry(K key) {
            int index = Collections.binarySearch(keys, key);
            if (index >= 0) {
                keys.remove(index);
                values.remove(index);
            }
        }
        
        public V getValue(K key) {
            int index = Collections.binarySearch(keys, key);
            return index >= 0 ? values.get(index) : null;
        }
        
        public List<V> getValues() {
            return new ArrayList<>(values);
        }
        
        public V getValue(int index) {
            return values.get(index);
        }
        
        public LeafNode<K, V> getNext() { return next; }
        public void setNext(LeafNode<K, V> next) { this.next = next; }
        public LeafNode<K, V> getPrev() { return prev; }
        public void setPrev(LeafNode<K, V> prev) { this.prev = prev; }
        
        @Override
        public void print(int level) {
            String indent = "  ".repeat(level);
            System.out.printf("%s🍃 LeafNode[%d]: ", indent, nodeId);
            for (int i = 0; i < keys.size(); i++) {
                System.out.printf("[%s:%s] ", keys.get(i), values.get(i));
            }
            System.out.println();
        }
    }
    
    /**
     * 生成唯一节点ID
     */
    private synchronized long generateNodeId() {
        return ++nodeIdCounter;
    }
    
    /**
     * 插入键值对
     */
    public void insert(K key, V value) {
        lock.writeLock().lock();
        try {
            System.out.printf("🔍 插入: [%s:%s]%n", key, value);
            
            LeafNode<K, V> leafNode = findLeafNode(key);
            leafNode.addEntry(key, value);
            
            // 检查是否需要分裂
            if (leafNode.getSize() >= degree) {
                splitLeafNode(leafNode);
            }
            
            System.out.printf("✅ 插入完成，树高度: %d%n", height);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 查找键对应的值
     */
    public V search(K key) {
        lock.readLock().lock();
        try {
            System.out.printf("🔍 查找: %s%n", key);
            
            LeafNode<K, V> leafNode = findLeafNode(key);
            V value = leafNode.getValue(key);
            
            System.out.printf("%s 查找结果: %s%n", 
                value != null ? "✅" : "❌", 
                value != null ? value : "未找到");
            
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 范围查询
     */
    public List<V> rangeQuery(K startKey, K endKey) {
        lock.readLock().lock();
        try {
            System.out.printf("🔍 范围查询: [%s, %s]%n", startKey, endKey);
            
            List<V> result = new ArrayList<>();
            LeafNode<K, V> current = findLeafNode(startKey);
            
            // 从起始叶子节点开始遍历
            while (current != null) {
                for (int i = 0; i < current.getSize(); i++) {
                    K key = current.keys.get(i);
                    
                    if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                        result.add(current.getValue(i));
                    } else if (key.compareTo(endKey) > 0) {
                        System.out.printf("✅ 范围查询完成，找到 %d 条记录%n", result.size());
                        return result;
                    }
                }
                current = current.getNext();
            }
            
            System.out.printf("✅ 范围查询完成，找到 %d 条记录%n", result.size());
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 删除键值对
     */
    public boolean delete(K key) {
        lock.writeLock().lock();
        try {
            System.out.printf("🗑️ 删除: %s%n", key);
            
            LeafNode<K, V> leafNode = findLeafNode(key);
            if (leafNode.getValue(key) == null) {
                System.out.println("❌ 键不存在");
                return false;
            }
            
            leafNode.removeEntry(key);
            
            // 检查是否需要合并或重新分配
            if (leafNode.getSize() < (degree - 1) / 2 && !leafNode.isRoot()) {
                handleUnderflow(leafNode);
            }
            
            System.out.println("✅ 删除完成");
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 查找应该包含给定键的叶子节点
     */
    private LeafNode<K, V> findLeafNode(K key) {
        Node<K, V> current = root;
        int nodeAccess = 0;
        
        while (!current.isLeaf()) {
            nodeAccess++;
            InternalNode<K, V> internalNode = (InternalNode<K, V>) current;
            
            // 在keys中找到第一个大于key的位置
            int childIndex = 0;
            for (K nodeKey : internalNode.keys) {
                if (key.compareTo(nodeKey) < 0) {
                    break;
                }
                childIndex++;
            }
            
            current = internalNode.getChild(childIndex);
        }
        
        System.out.printf("📊 访问了 %d 个内部节点到达叶子节点[%d]%n", 
            nodeAccess, current.getNodeId());
        
        return (LeafNode<K, V>) current;
    }
    
    /**
     * 分裂叶子节点
     */
    private void splitLeafNode(LeafNode<K, V> leafNode) {
        System.out.printf("🔄 分裂叶子节点[%d]%n", leafNode.getNodeId());
        
        LeafNode<K, V> newLeafNode = new LeafNode<>(generateNodeId());
        int midIndex = leafNode.getSize() / 2;
        
        // 移动后半部分数据到新节点
        List<K> keysToMove = new ArrayList<>(leafNode.keys.subList(midIndex, leafNode.keys.size()));
        List<V> valuesToMove = new ArrayList<>(leafNode.values.subList(midIndex, leafNode.values.size()));
        
        leafNode.keys.subList(midIndex, leafNode.keys.size()).clear();
        leafNode.values.subList(midIndex, leafNode.values.size()).clear();
        
        newLeafNode.keys.addAll(keysToMove);
        newLeafNode.values.addAll(valuesToMove);
        
        // 更新叶子节点链表
        newLeafNode.setNext(leafNode.getNext());
        newLeafNode.setPrev(leafNode);
        if (leafNode.getNext() != null) {
            leafNode.getNext().setPrev(newLeafNode);
        } else {
            lastLeaf = newLeafNode;
        }
        leafNode.setNext(newLeafNode);
        
        // 获取分裂键（新节点的第一个键）
        K splitKey = newLeafNode.keys.get(0);
        
        // 向父节点插入分裂键
        insertIntoParent(leafNode, splitKey, newLeafNode);
    }
    
    /**
     * 向父节点插入键
     */
    private void insertIntoParent(Node<K, V> leftNode, K key, Node<K, V> rightNode) {
        if (leftNode.isRoot()) {
            // 创建新的根节点
            InternalNode<K, V> newRoot = new InternalNode<>(generateNodeId());
            newRoot.keys.add(key);
            newRoot.addChild(leftNode);
            newRoot.addChild(rightNode);
            newRoot.setRoot(true);
            
            leftNode.setRoot(false);
            this.root = newRoot;
            this.height++;
            
            System.out.printf("🌳 创建新根节点[%d]，树高度增加到 %d%n", 
                newRoot.getNodeId(), height);
        } else {
            InternalNode<K, V> parent = (InternalNode<K, V>) leftNode.getParent();
            
            // 找到插入位置
            int insertIndex = parent.getChildIndex(leftNode) + 1;
            parent.keys.add(insertIndex - 1, key);
            parent.insertChild(insertIndex, rightNode);
            
            // 检查父节点是否需要分裂
            if (parent.getSize() >= degree) {
                splitInternalNode(parent);
            }
        }
    }
    
    /**
     * 分裂内部节点
     */
    private void splitInternalNode(InternalNode<K, V> internalNode) {
        System.out.printf("🔄 分裂内部节点[%d]%n", internalNode.getNodeId());
        
        InternalNode<K, V> newInternalNode = new InternalNode<>(generateNodeId());
        int midIndex = internalNode.getSize() / 2;
        
        // 分裂键
        K splitKey = internalNode.keys.get(midIndex);
        
        // 移动右半部分的键和子节点
        List<K> keysToMove = new ArrayList<>(internalNode.keys.subList(midIndex + 1, internalNode.keys.size()));
        List<Node<K, V>> childrenToMove = new ArrayList<>(
            internalNode.children.subList(midIndex + 1, internalNode.children.size()));
        
        internalNode.keys.subList(midIndex, internalNode.keys.size()).clear();
        internalNode.children.subList(midIndex + 1, internalNode.children.size()).clear();
        
        newInternalNode.keys.addAll(keysToMove);
        for (Node<K, V> child : childrenToMove) {
            newInternalNode.addChild(child);
        }
        
        // 向父节点插入分裂键
        insertIntoParent(internalNode, splitKey, newInternalNode);
    }
    
    /**
     * 处理节点下溢
     */
    private void handleUnderflow(Node<K, V> node) {
        if (node.isRoot()) {
            if (node.getSize() == 0 && !node.isLeaf()) {
                // 根节点为空的内部节点，降低树高度
                InternalNode<K, V> internalNode = (InternalNode<K, V>) node;
                Node<K, V> newRoot = internalNode.getChild(0);
                newRoot.setRoot(true);
                newRoot.setParent(null);
                this.root = newRoot;
                this.height--;
                System.out.printf("🌳 根节点为空，树高度降低到 %d%n", height);
            }
            return;
        }
        
        // 尝试从兄弟节点借用或合并
        // 这里简化实现，实际应该包含完整的重新分配和合并逻辑
        System.out.printf("⚠️ 节点[%d]发生下溢，需要重新分配或合并%n", node.getNodeId());
    }
    
    /**
     * 打印B+树结构
     */
    public void printTree() {
        lock.readLock().lock();
        try {
            System.out.println("\n🌳 B+树结构:");
            System.out.println("-".repeat(50));
            if (root != null) {
                root.print(0);
            }
            
            System.out.println("\n🔗 叶子节点链表:");
            LeafNode<K, V> current = firstLeaf;
            while (current != null) {
                System.out.printf("[节点%d: %s]", current.getNodeId(), current.keys);
                if (current.getNext() != null) {
                    System.out.print(" -> ");
                }
                current = current.getNext();
            }
            System.out.println();
            System.out.println("-".repeat(50));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取树的统计信息
     */
    public TreeStats getStats() {
        lock.readLock().lock();
        try {
            return new TreeStats(height, countNodes(), countLeafNodes());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 计算树中节点总数
     */
    private int countNodes() {
        return countNodesRecursive(root);
    }
    
    private int countNodesRecursive(Node<K, V> node) {
        if (node.isLeaf()) {
            return 1;
        }
        
        int count = 1;
        InternalNode<K, V> internalNode = (InternalNode<K, V>) node;
        for (Node<K, V> child : internalNode.getChildren()) {
            count += countNodesRecursive(child);
        }
        return count;
    }
    
    /**
     * 计算叶子节点数
     */
    private int countLeafNodes() {
        int count = 0;
        LeafNode<K, V> current = firstLeaf;
        while (current != null) {
            count++;
            current = current.getNext();
        }
        return count;
    }
    
    /**
     * B+树统计信息
     */
    public static class TreeStats {
        private final int height;
        private final int totalNodes;
        private final int leafNodes;
        
        public TreeStats(int height, int totalNodes, int leafNodes) {
            this.height = height;
            this.totalNodes = totalNodes;
            this.leafNodes = leafNodes;
        }
        
        @Override
        public String toString() {
            return String.format("TreeStats[高度=%d, 总节点=%d, 叶子节点=%d, 内部节点=%d]",
                height, totalNodes, leafNodes, totalNodes - leafNodes);
        }
        
        public int getHeight() { return height; }
        public int getTotalNodes() { return totalNodes; }
        public int getLeafNodes() { return leafNodes; }
        public int getInternalNodes() { return totalNodes - leafNodes; }
    }
    
    /**
     * 演示B+树的工作原理
     */
    public static void demonstrateBPlusTree() {
        System.out.println("🌳 B+树索引原理演示");
        System.out.println("=".repeat(50));
        
        // 创建一个度为4的B+树
        BPlusTree<Integer, String> bPlusTree = new BPlusTree<>(4);
        
        // 1. 插入数据
        System.out.println("\n📝 插入数据演示:");
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17, 15, 25, 35, 40};
        String[] values = {"value10", "value20", "value5", "value6", "value12", 
                          "value30", "value7", "value17", "value15", "value25", "value35", "value40"};
        
        for (int i = 0; i < keys.length; i++) {
            bPlusTree.insert(keys[i], values[i]);
            if ((i + 1) % 4 == 0) {
                bPlusTree.printTree();
            }
        }
        
        // 2. 最终树结构
        System.out.println("\n📊 最终树结构:");
        bPlusTree.printTree();
        System.out.println(bPlusTree.getStats());
        
        // 3. 点查询演示
        System.out.println("\n🔍 点查询演示:");
        bPlusTree.search(15);
        bPlusTree.search(25);
        bPlusTree.search(100);  // 不存在的键
        
        // 4. 范围查询演示
        System.out.println("\n🔍 范围查询演示:");
        List<String> rangeResult = bPlusTree.rangeQuery(10, 25);
        System.out.println("范围查询[10, 25]结果: " + rangeResult);
        
        // 5. 删除操作演示
        System.out.println("\n🗑️ 删除操作演示:");
        bPlusTree.delete(15);
        bPlusTree.delete(25);
        bPlusTree.printTree();
        
        System.out.println("\n✅ B+树演示完成");
    }
    
    public static void main(String[] args) {
        demonstrateBPlusTree();
    }
}