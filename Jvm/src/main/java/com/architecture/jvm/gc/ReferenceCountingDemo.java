package com.architecture.jvm.gc;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 引用计数算法详细演示
 * 
 * 展示引用计数算法的工作原理、优缺点和实际应用场景
 * 
 * 引用计数算法原理：
 * 1. 每个对象维护一个引用计数器，记录指向该对象的引用数量
 * 2. 创建引用时，计数器+1；删除引用时，计数器-1
 * 3. 当计数器为0时，对象立即被回收
 * 4. 递归回收：被回收对象引用的其他对象的计数器也要相应减1
 * 
 * 优点：
 * - 内存回收及时，一旦对象不可达立即回收
 * - 没有暂停时间，回收过程是增量进行的
 * - 内存使用量相对稳定
 * 
 * 缺点：
 * - 无法处理循环引用
 * - 每次引用操作都需要更新计数器，开销较大
 * - 需要额外的内存存储引用计数器
 * - 递归回收可能导致较长的暂停
 */
public class ReferenceCountingDemo {
    
    public static void main(String[] args) {
        ReferenceCountingDemo demo = new ReferenceCountingDemo();
        
        System.out.println("=== 引用计数算法详细演示 ===\n");
        
        // 1. 基本引用计数操作
        demo.demonstrateBasicOperations();
        
        // 2. 循环引用问题
        demo.demonstrateCircularReference();
        
        // 3. 复杂引用网络
        demo.demonstrateComplexReferenceNetwork();
        
        // 4. 引用计数优化策略
        demo.demonstrateOptimizations();
        
        // 5. 与其他GC算法的对比
        demo.demonstrateComparison();
    }
    
    /**
     * 演示基本的引用计数操作
     */
    private void demonstrateBasicOperations() {
        System.out.println("=== 1. 基本引用计数操作演示 ===");
        
        ReferenceCountingHeap heap = new ReferenceCountingHeap();
        
        System.out.println("步骤1：创建对象A");
        int objectA = heap.createObject("ObjectA", 1024);
        heap.printHeapStatus();
        
        System.out.println("\n步骤2：创建对象B，并让A引用B");
        int objectB = heap.createObject("ObjectB", 512);
        heap.addReference(objectA, objectB);
        heap.printHeapStatus();
        
        System.out.println("\n步骤3：创建局部引用指向B");
        heap.addLocalReference(objectB, "local_var");
        heap.printHeapStatus();
        
        System.out.println("\n步骤4：删除局部引用");
        heap.removeLocalReference(objectB, "local_var");
        heap.printHeapStatus();
        
        System.out.println("\n步骤5：删除A对B的引用");
        heap.removeReference(objectA, objectB);
        heap.printHeapStatus();
        
        System.out.println("\n步骤6：删除对A的最后引用");
        heap.removeLastReference(objectA);
        heap.printHeapStatus();
        
        System.out.println("\n基本操作总结：");
        System.out.println("- 对象B在步骤4后立即被回收（计数变为0）");
        System.out.println("- 对象A在步骤6后被回收");
        System.out.println("- 内存回收及时，无延迟\n");
    }
    
    /**
     * 演示循环引用问题
     */
    private void demonstrateCircularReference() {
        System.out.println("=== 2. 循环引用问题演示 ===");
        
        ReferenceCountingHeap heap = new ReferenceCountingHeap();
        
        System.out.println("步骤1：创建对象A和B");
        int objectA = heap.createObject("ObjectA", 1024);
        int objectB = heap.createObject("ObjectB", 1024);
        heap.printHeapStatus();
        
        System.out.println("\n步骤2：建立A -> B的引用");
        heap.addReference(objectA, objectB);
        heap.printHeapStatus();
        
        System.out.println("\n步骤3：建立B -> A的引用（形成循环）");
        heap.addReference(objectB, objectA);
        heap.printHeapStatus();
        
        System.out.println("\n步骤4：删除外部对A的引用");
        heap.removeLastReference(objectA);
        heap.printHeapStatus();
        
        System.out.println("\n步骤5：删除外部对B的引用");
        heap.removeLastReference(objectB);
        heap.printHeapStatus();
        
        System.out.println("\n循环引用问题总结：");
        System.out.println("- A和B互相引用，形成循环引用");
        System.out.println("- 即使外部引用都删除了，它们的引用计数仍然大于0");
        System.out.println("- 这些对象无法被回收，造成内存泄漏");
        System.out.println("- 这是引用计数算法的根本缺陷\n");
        
        // 演示循环引用检测算法
        demonstrateCycleDetection(heap);
    }
    
    /**
     * 演示循环引用检测算法
     */
    private void demonstrateCycleDetection(ReferenceCountingHeap heap) {
        System.out.println("=== 2.1 循环引用检测算法 ===");
        
        System.out.println("使用辅助标记算法检测循环引用：");
        
        // 实现一个简单的循环检测
        Set<Integer> suspiciousObjects = heap.findSuspiciousObjects();
        System.out.println("可疑对象（引用计数>0但可能不可达）：" + suspiciousObjects);
        
        if (!suspiciousObjects.isEmpty()) {
            System.out.println("执行可达性检测...");
            Set<Integer> actuallyReachable = heap.findReachableObjects();
            System.out.println("实际可达对象：" + actuallyReachable);
            
            Set<Integer> circularObjects = new HashSet<>(suspiciousObjects);
            circularObjects.removeAll(actuallyReachable);
            
            if (!circularObjects.isEmpty()) {
                System.out.println("检测到循环引用对象：" + circularObjects);
                System.out.println("这些对象将被特殊处理回收");
                heap.forceRecycleCyclicObjects(circularObjects);
                heap.printHeapStatus();
            }
        }
        
        System.out.println("循环引用检测总结：");
        System.out.println("- 需要额外的算法检测循环引用");
        System.out.println("- 检测过程会带来额外开销");
        System.out.println("- 失去了引用计数算法的简单性优势\n");
    }
    
    /**
     * 演示复杂引用网络
     */
    private void demonstrateComplexReferenceNetwork() {
        System.out.println("=== 3. 复杂引用网络演示 ===");
        
        ReferenceCountingHeap heap = new ReferenceCountingHeap();
        
        System.out.println("构建复杂的对象引用网络：");
        
        // 创建多个对象
        int root = heap.createObject("Root", 2048);
        int nodeA = heap.createObject("NodeA", 1024);
        int nodeB = heap.createObject("NodeB", 1024);
        int nodeC = heap.createObject("NodeC", 1024);
        int leafD = heap.createObject("LeafD", 512);
        int leafE = heap.createObject("LeafE", 512);
        int leafF = heap.createObject("LeafF", 512);
        
        // 建立引用关系
        System.out.println("\n建立引用关系：");
        heap.addReference(root, nodeA);     // Root -> NodeA
        heap.addReference(root, nodeB);     // Root -> NodeB
        heap.addReference(nodeA, nodeC);    // NodeA -> NodeC
        heap.addReference(nodeA, leafD);    // NodeA -> LeafD
        heap.addReference(nodeB, leafE);    // NodeB -> LeafE
        heap.addReference(nodeC, leafF);    // NodeC -> LeafF
        heap.addReference(nodeB, nodeC);    // NodeB -> NodeC (NodeC被多重引用)
        
        heap.printHeapStatus();
        heap.printReferenceNetwork();
        
        System.out.println("\n模拟删除部分引用：");
        System.out.println("删除 Root -> NodeA 引用");
        heap.removeReference(root, nodeA);
        heap.printHeapStatus();
        
        System.out.println("\n删除 NodeB -> NodeC 引用");
        heap.removeReference(nodeB, nodeC);
        heap.printHeapStatus();
        
        System.out.println("\n删除 Root -> NodeB 引用");
        heap.removeReference(root, nodeB);
        heap.printHeapStatus();
        
        System.out.println("\n删除对Root的外部引用");
        heap.removeLastReference(root);
        heap.printHeapStatus();
        
        System.out.println("复杂引用网络总结：");
        System.out.println("- 多重引用的对象需要所有引用都删除才能回收");
        System.out.println("- 引用删除的顺序影响回收的时机");
        System.out.println("- 级联回收可能在某一时刻集中发生\n");
    }
    
    /**
     * 演示引用计数优化策略
     */
    private void demonstrateOptimizations() {
        System.out.println("=== 4. 引用计数优化策略演示 ===");
        
        // 4.1 延迟回收优化
        demonstrateDeferredReclamation();
        
        // 4.2 限制递归深度
        demonstrateRecursionLimiting();
        
        // 4.3 引用计数压缩
        demonstrateCountCompression();
    }
    
    /**
     * 演示延迟回收优化
     */
    private void demonstrateDeferredReclamation() {
        System.out.println("--- 4.1 延迟回收优化 ---");
        
        DeferredRCHeap heap = new DeferredRCHeap();
        
        System.out.println("创建对象并建立引用关系：");
        int obj1 = heap.createObject("Obj1", 1024);
        int obj2 = heap.createObject("Obj2", 1024);
        int obj3 = heap.createObject("Obj3", 1024);
        
        heap.addReference(obj1, obj2);
        heap.addReference(obj2, obj3);
        
        System.out.println("删除引用（延迟回收）：");
        heap.removeReference(obj1, obj2);  // 不立即回收，加入延迟队列
        heap.removeLastReference(obj1);    // 不立即回收，加入延迟队列
        
        heap.printStatus("引用删除后（延迟回收）");
        
        System.out.println("\n批量执行延迟回收：");
        heap.processDeferredReclamation();
        
        heap.printStatus("批量回收后");
        
        System.out.println("延迟回收优化总结：");
        System.out.println("- 避免频繁的小回收操作");
        System.out.println("- 批量处理提高效率");
        System.out.println("- 可能延迟内存释放\n");
    }
    
    /**
     * 演示递归深度限制
     */
    private void demonstrateRecursionLimiting() {
        System.out.println("--- 4.2 递归深度限制优化 ---");
        
        RecursionLimitedRCHeap heap = new RecursionLimitedRCHeap(3);
        
        System.out.println("创建深层引用链：");
        List<Integer> chain = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int obj = heap.createObject("Obj" + i, 512);
            chain.add(obj);
            if (i > 0) {
                heap.addReference(chain.get(i-1), obj);
            }
        }
        
        heap.printStatus("创建引用链后");
        
        System.out.println("\n删除链头引用（触发递归回收）：");
        heap.removeLastReference(chain.get(0));
        
        heap.printStatus("限制递归深度回收后");
        
        System.out.println("\n继续处理剩余回收：");
        heap.processRemainingReclamation();
        
        heap.printStatus("完全回收后");
        
        System.out.println("递归限制优化总结：");
        System.out.println("- 避免深度递归导致的栈溢出");
        System.out.println("- 分批处理大量连锁回收");
        System.out.println("- 控制回收过程的暂停时间\n");
    }
    
    /**
     * 演示引用计数压缩
     */
    private void demonstrateCountCompression() {
        System.out.println("--- 4.3 引用计数压缩优化 ---");
        
        CompressedRCHeap heap = new CompressedRCHeap();
        
        System.out.println("创建大量引用的对象：");
        int popular = heap.createObject("PopularObject", 2048);
        
        // 添加大量引用
        for (int i = 0; i < 1000; i++) {
            int ref = heap.createObject("Ref" + i, 64);
            heap.addReference(ref, popular);
        }
        
        heap.printCompressionStatus("大量引用添加后");
        
        System.out.println("\n删除部分引用：");
        for (int i = 0; i < 500; i++) {
            heap.simulateReferenceRemoval();
        }
        
        heap.printCompressionStatus("部分引用删除后");
        
        System.out.println("引用计数压缩总结：");
        System.out.println("- 使用较小的位数存储常见的小计数");
        System.out.println("- 大计数使用溢出表存储");
        System.out.println("- 减少内存开销\n");
    }
    
    /**
     * 演示与其他GC算法的对比
     */
    private void demonstrateComparison() {
        System.out.println("=== 5. 与其他GC算法对比演示 ===");
        
        GCAlgorithmComparator comparator = new GCAlgorithmComparator();
        
        System.out.println("创建相同的测试场景：");
        comparator.setupTestScenario();
        
        System.out.println("\n引用计数算法执行：");
        long rcTime = comparator.runReferenceCountingGC();
        
        System.out.println("\n标记-清除算法执行：");
        long msTime = comparator.runMarkSweepGC();
        
        System.out.println("\n性能对比结果：");
        comparator.printComparison(rcTime, msTime);
        
        System.out.println("\n算法特点总结：");
        System.out.println("引用计数算法：");
        System.out.println("  优点：及时回收、无暂停、内存使用稳定");
        System.out.println("  缺点：循环引用问题、更新开销、额外内存");
        System.out.println("  适用场景：实时系统、内存敏感应用");
        
        System.out.println("\n标记-清除算法：");
        System.out.println("  优点：处理循环引用、实现简单");
        System.out.println("  缺点：暂停时间、内存碎片");
        System.out.println("  适用场景：一般应用、内存充足环境");
        
        System.out.println("\n现代JVM选择：");
        System.out.println("- 主流JVM不使用纯引用计数算法");
        System.out.println("- 通常使用可达性分析+分代收集");
        System.out.println("- 引用计数用于特定场景（如Python、Swift）\n");
    }
}

/**
 * 引用计数堆实现
 */
class ReferenceCountingHeap {
    private final Map<Integer, RCObject> objects;
    private final Map<Integer, Set<String>> localReferences;
    private final AtomicInteger nextId;
    private long totalMemoryUsed;
    
    public ReferenceCountingHeap() {
        this.objects = new HashMap<>();
        this.localReferences = new HashMap<>();
        this.nextId = new AtomicInteger(1);
        this.totalMemoryUsed = 0;
    }
    
    public int createObject(String name, int size) {
        int id = nextId.getAndIncrement();
        RCObject obj = new RCObject(id, name, size);
        objects.put(id, obj);
        localReferences.put(id, new HashSet<>());
        localReferences.get(id).add("creation_ref");
        totalMemoryUsed += size;
        
        System.out.printf("创建对象 %s (ID:%d, 大小:%d bytes, 引用计数:%d)\n", 
            name, id, size, obj.refCount);
        return id;
    }
    
    public void addReference(int fromId, int toId) {
        if (!objects.containsKey(fromId) || !objects.containsKey(toId)) {
            return;
        }
        
        RCObject fromObj = objects.get(fromId);
        RCObject toObj = objects.get(toId);
        
        fromObj.references.add(toId);
        toObj.refCount++;
        
        System.out.printf("添加引用 %s -> %s (目标引用计数:%d)\n",
            fromObj.name, toObj.name, toObj.refCount);
    }
    
    public void addLocalReference(int objectId, String varName) {
        if (!objects.containsKey(objectId)) return;
        
        localReferences.get(objectId).add(varName);
        objects.get(objectId).refCount++;
        
        System.out.printf("添加局部引用 %s -> %s (引用计数:%d)\n",
            varName, objects.get(objectId).name, objects.get(objectId).refCount);
    }
    
    public void removeLocalReference(int objectId, String varName) {
        if (!objects.containsKey(objectId)) return;
        
        if (localReferences.get(objectId).remove(varName)) {
            decrementRefCount(objectId);
        }
    }
    
    public void removeReference(int fromId, int toId) {
        if (!objects.containsKey(fromId) || !objects.containsKey(toId)) {
            return;
        }
        
        RCObject fromObj = objects.get(fromId);
        if (fromObj.references.remove(toId)) {
            decrementRefCount(toId);
        }
    }
    
    public void removeLastReference(int objectId) {
        if (!objects.containsKey(objectId)) return;
        
        localReferences.get(objectId).clear();
        decrementRefCount(objectId);
    }
    
    private void decrementRefCount(int objectId) {
        if (!objects.containsKey(objectId)) return;
        
        RCObject obj = objects.get(objectId);
        obj.refCount--;
        
        System.out.printf("减少引用计数 %s (新计数:%d)\n", obj.name, obj.refCount);
        
        if (obj.refCount == 0) {
            recycleObject(objectId);
        }
    }
    
    private void recycleObject(int objectId) {
        RCObject obj = objects.get(objectId);
        System.out.printf("*** 回收对象 %s (释放%d bytes内存) ***\n", 
            obj.name, obj.size);
        
        // 递归减少引用对象的计数
        for (int refId : new ArrayList<>(obj.references)) {
            decrementRefCount(refId);
        }
        
        totalMemoryUsed -= obj.size;
        objects.remove(objectId);
        localReferences.remove(objectId);
    }
    
    public Set<Integer> findSuspiciousObjects() {
        Set<Integer> suspicious = new HashSet<>();
        for (RCObject obj : objects.values()) {
            if (obj.refCount > 0 && localReferences.get(obj.id).isEmpty()) {
                suspicious.add(obj.id);
            }
        }
        return suspicious;
    }
    
    public Set<Integer> findReachableObjects() {
        Set<Integer> reachable = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        
        // 从有外部引用的对象开始
        for (Map.Entry<Integer, Set<String>> entry : localReferences.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                queue.add(entry.getKey());
            }
        }
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (reachable.add(current) && objects.containsKey(current)) {
                queue.addAll(objects.get(current).references);
            }
        }
        
        return reachable;
    }
    
    public void forceRecycleCyclicObjects(Set<Integer> cyclicObjects) {
        System.out.println("强制回收循环引用对象...");
        for (int id : cyclicObjects) {
            if (objects.containsKey(id)) {
                RCObject obj = objects.get(id);
                System.out.printf("强制回收 %s\n", obj.name);
                totalMemoryUsed -= obj.size;
                objects.remove(id);
                localReferences.remove(id);
            }
        }
    }
    
    public void printHeapStatus() {
        System.out.println("--- 堆状态 ---");
        System.out.printf("活动对象数: %d, 总内存使用: %d bytes\n", 
            objects.size(), totalMemoryUsed);
        
        for (RCObject obj : objects.values()) {
            System.out.printf("  %s: 引用计数=%d, 大小=%d bytes\n",
                obj.name, obj.refCount, obj.size);
        }
        System.out.println();
    }
    
    public void printReferenceNetwork() {
        System.out.println("--- 引用网络 ---");
        for (RCObject obj : objects.values()) {
            if (!obj.references.isEmpty()) {
                System.out.print(obj.name + " -> [");
                for (int refId : obj.references) {
                    if (objects.containsKey(refId)) {
                        System.out.print(objects.get(refId).name + " ");
                    }
                }
                System.out.println("]");
            }
        }
        System.out.println();
    }
    
    private static class RCObject {
        final int id;
        final String name;
        final int size;
        int refCount = 1; // 创建时有一个引用
        final Set<Integer> references = new HashSet<>();
        
        RCObject(int id, String name, int size) {
            this.id = id;
            this.name = name;
            this.size = size;
        }
    }
}

/**
 * 延迟回收堆
 */
class DeferredRCHeap {
    private final Map<Integer, RCObject> objects = new HashMap<>();
    private final Queue<Integer> deferredQueue = new LinkedList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    
    public int createObject(String name, int size) {
        int id = nextId.getAndIncrement();
        objects.put(id, new RCObject(id, name, size));
        return id;
    }
    
    public void addReference(int from, int to) {
        objects.get(to).refCount++;
        objects.get(from).references.add(to);
    }
    
    public void removeReference(int from, int to) {
        if (objects.get(from).references.remove(to)) {
            deferredQueue.offer(to);
        }
    }
    
    public void removeLastReference(int id) {
        deferredQueue.offer(id);
    }
    
    public void processDeferredReclamation() {
        System.out.printf("处理延迟回收队列，待处理对象数: %d\n", deferredQueue.size());
        
        while (!deferredQueue.isEmpty()) {
            int id = deferredQueue.poll();
            if (objects.containsKey(id)) {
                RCObject obj = objects.get(id);
                obj.refCount--;
                
                if (obj.refCount == 0) {
                    System.out.printf("批量回收对象 %s\n", obj.name);
                    
                    // 将引用的对象也加入延迟队列
                    for (int refId : obj.references) {
                        deferredQueue.offer(refId);
                    }
                    
                    objects.remove(id);
                }
            }
        }
    }
    
    public void printStatus(String phase) {
        System.out.printf("%s - 对象数: %d, 延迟队列: %d\n", 
            phase, objects.size(), deferredQueue.size());
    }
    
    private static class RCObject {
        final int id;
        final String name;
        final int size;
        int refCount = 1;
        final Set<Integer> references = new HashSet<>();
        
        RCObject(int id, String name, int size) {
            this.id = id;
            this.name = name;
            this.size = size;
        }
    }
}

/**
 * 递归限制堆
 */
class RecursionLimitedRCHeap {
    private final Map<Integer, RCObject> objects = new HashMap<>();
    private final Queue<Integer> recursionQueue = new LinkedList<>();
    private final int maxRecursionDepth;
    private final AtomicInteger nextId = new AtomicInteger(1);
    
    public RecursionLimitedRCHeap(int maxRecursionDepth) {
        this.maxRecursionDepth = maxRecursionDepth;
    }
    
    public int createObject(String name, int size) {
        int id = nextId.getAndIncrement();
        objects.put(id, new RCObject(id, name, size));
        return id;
    }
    
    public void addReference(int from, int to) {
        objects.get(to).refCount++;
        objects.get(from).references.add(to);
    }
    
    public void removeLastReference(int id) {
        decrementRefCount(id, 0);
    }
    
    private void decrementRefCount(int id, int depth) {
        if (!objects.containsKey(id)) return;
        
        RCObject obj = objects.get(id);
        obj.refCount--;
        
        if (obj.refCount == 0) {
            System.out.printf("回收对象 %s (递归深度:%d)\n", obj.name, depth);
            
            if (depth < maxRecursionDepth) {
                // 继续递归回收
                for (int refId : obj.references) {
                    decrementRefCount(refId, depth + 1);
                }
            } else {
                // 达到递归限制，加入队列
                System.out.printf("达到递归限制，%s的引用对象加入队列\n", obj.name);
                recursionQueue.addAll(obj.references);
            }
            
            objects.remove(id);
        }
    }
    
    public void processRemainingReclamation() {
        System.out.printf("处理剩余回收，队列大小: %d\n", recursionQueue.size());
        
        while (!recursionQueue.isEmpty()) {
            int id = recursionQueue.poll();
            decrementRefCount(id, 0);
        }
    }
    
    public void printStatus(String phase) {
        System.out.printf("%s - 对象数: %d, 队列: %d\n", 
            phase, objects.size(), recursionQueue.size());
    }
    
    private static class RCObject {
        final int id;
        final String name;
        final int size;
        int refCount = 1;
        final Set<Integer> references = new HashSet<>();
        
        RCObject(int id, String name, int size) {
            this.id = id;
            this.name = name;
            this.size = size;
        }
    }
}

/**
 * 压缩引用计数堆
 */
class CompressedRCHeap {
    private final Map<Integer, CompressedRCObject> objects = new HashMap<>();
    private final Map<Integer, Integer> overflowTable = new HashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final int maxCompressedCount = 255; // 8位最大值
    
    public int createObject(String name, int size) {
        int id = nextId.getAndIncrement();
        objects.put(id, new CompressedRCObject(id, name, size));
        return id;
    }
    
    public void addReference(int from, int to) {
        if (!objects.containsKey(to)) return;
        
        CompressedRCObject obj = objects.get(to);
        if (obj.compressedRefCount < maxCompressedCount) {
            obj.compressedRefCount++;
        } else {
            // 使用溢出表
            int overflowCount = overflowTable.getOrDefault(to, maxCompressedCount);
            overflowTable.put(to, overflowCount + 1);
        }
    }
    
    public void simulateReferenceRemoval() {
        // 模拟删除一个引用
        if (!objects.isEmpty()) {
            int firstId = objects.keySet().iterator().next();
            CompressedRCObject obj = objects.get(firstId);
            
            if (overflowTable.containsKey(firstId)) {
                int overflowCount = overflowTable.get(firstId);
                if (overflowCount > maxCompressedCount) {
                    overflowTable.put(firstId, overflowCount - 1);
                } else {
                    overflowTable.remove(firstId);
                    obj.compressedRefCount = (byte) (overflowCount - 1);
                }
            } else if (obj.compressedRefCount > 0) {
                obj.compressedRefCount--;
            }
        }
    }
    
    public void printCompressionStatus(String phase) {
        System.out.printf("%s:\n", phase);
        System.out.printf("对象数: %d, 溢出表条目: %d\n", 
            objects.size(), overflowTable.size());
        
        long compressedMemory = objects.size() * 1; // 每个对象1字节计数
        long overflowMemory = overflowTable.size() * 8; // 每个溢出条目8字节
        System.out.printf("引用计数内存: 压缩表 %d bytes + 溢出表 %d bytes = %d bytes\n",
            compressedMemory, overflowMemory, compressedMemory + overflowMemory);
        
        if (!overflowTable.isEmpty()) {
            System.out.println("溢出对象示例:");
            overflowTable.entrySet().stream().limit(3).forEach(entry -> {
                CompressedRCObject obj = objects.get(entry.getKey());
                System.out.printf("  %s: 实际引用数 %d (压缩:%d + 溢出:%d)\n",
                    obj.name, entry.getValue(), obj.compressedRefCount, 
                    entry.getValue() - maxCompressedCount);
            });
        }
        System.out.println();
    }
    
    private static class CompressedRCObject {
        final int id;
        final String name;
        final int size;
        byte compressedRefCount = 1; // 使用1字节存储计数
        
        CompressedRCObject(int id, String name, int size) {
            this.id = id;
            this.name = name;
            this.size = size;
        }
    }
}

/**
 * GC算法比较器
 */
class GCAlgorithmComparator {
    private List<TestObject> testObjects;
    
    public void setupTestScenario() {
        testObjects = new ArrayList<>();
        
        // 创建1000个对象的测试场景
        for (int i = 0; i < 1000; i++) {
            testObjects.add(new TestObject(i, "Object" + i));
        }
        
        // 建立随机引用关系
        Random random = new Random(42); // 固定种子保证一致性
        for (TestObject obj : testObjects) {
            int refCount = random.nextInt(5); // 0-4个引用
            for (int j = 0; j < refCount; j++) {
                int targetIndex = random.nextInt(testObjects.size());
                obj.references.add(targetIndex);
            }
        }
        
        System.out.printf("创建%d个对象的测试场景\n", testObjects.size());
    }
    
    public long runReferenceCountingGC() {
        long startTime = System.nanoTime();
        
        // 模拟引用计数GC
        Map<Integer, Integer> refCounts = new HashMap<>();
        
        // 初始化引用计数
        for (int i = 0; i < testObjects.size(); i++) {
            refCounts.put(i, 1); // 初始外部引用
        }
        
        // 计算引用计数
        for (TestObject obj : testObjects) {
            for (int refId : obj.references) {
                refCounts.put(refId, refCounts.get(refId) + 1);
            }
        }
        
        // 模拟删除外部引用并回收
        int recycled = 0;
        Queue<Integer> toProcess = new LinkedList<>();
        
        for (int i = 0; i < testObjects.size(); i += 2) { // 删除一半的外部引用
            toProcess.offer(i);
        }
        
        while (!toProcess.isEmpty()) {
            int current = toProcess.poll();
            int currentCount = refCounts.get(current) - 1;
            refCounts.put(current, currentCount);
            
            if (currentCount == 0) {
                recycled++;
                // 递减引用对象的计数
                for (int refId : testObjects.get(current).references) {
                    toProcess.offer(refId);
                }
            }
        }
        
        long endTime = System.nanoTime();
        
        System.out.printf("引用计数GC: 回收%d个对象\n", recycled);
        return endTime - startTime;
    }
    
    public long runMarkSweepGC() {
        long startTime = System.nanoTime();
        
        // 模拟标记-清除GC
        Set<Integer> marked = new HashSet<>();
        Queue<Integer> markQueue = new LinkedList<>();
        
        // 从根对象开始标记
        for (int i = 0; i < testObjects.size(); i += 2) { // 一半作为根对象
            markQueue.offer(i);
        }
        
        // 标记阶段
        while (!markQueue.isEmpty()) {
            int current = markQueue.poll();
            if (marked.add(current)) {
                markQueue.addAll(testObjects.get(current).references);
            }
        }
        
        // 清除阶段
        int recycled = 0;
        for (int i = 0; i < testObjects.size(); i++) {
            if (!marked.contains(i)) {
                recycled++;
            }
        }
        
        long endTime = System.nanoTime();
        
        System.out.printf("标记-清除GC: 回收%d个对象\n", recycled);
        return endTime - startTime;
    }
    
    public void printComparison(long rcTime, long msTime) {
        System.out.printf("性能比较:\n");
        System.out.printf("  引用计数: %d 纳秒 (%.2f ms)\n", rcTime, rcTime / 1_000_000.0);
        System.out.printf("  标记-清除: %d 纳秒 (%.2f ms)\n", msTime, msTime / 1_000_000.0);
        
        if (rcTime < msTime) {
            System.out.printf("  引用计数快 %.2f 倍\n", (double) msTime / rcTime);
        } else {
            System.out.printf("  标记-清除快 %.2f 倍\n", (double) rcTime / msTime);
        }
    }
    
    private static class TestObject {
        final int id;
        final String name;
        final Set<Integer> references = new HashSet<>();
        
        TestObject(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}