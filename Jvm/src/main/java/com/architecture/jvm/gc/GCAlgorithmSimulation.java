package com.architecture.jvm.gc;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GC算法原理模拟演示
 * 
 * 模拟不同垃圾回收算法的工作原理：
 * 1. 标记-清除 (Mark-Sweep)
 * 2. 标记-复制 (Mark-Copy)  
 * 3. 标记-整理 (Mark-Compact)
 * 4. 引用计数 (Reference Counting)
 * 5. 分代收集 (Generational Collection)
 */
public class GCAlgorithmSimulation {
    
    public static void main(String[] args) {
        GCAlgorithmSimulation simulation = new GCAlgorithmSimulation();
        
        System.out.println("=== 垃圾回收算法原理演示 ===\n");
        
        // 1. 标记-清除算法演示
        simulation.demonstrateMarkSweep();
        
        // 2. 标记-复制算法演示  
        simulation.demonstrateMarkCopy();
        
        // 3. 标记-整理算法演示
        simulation.demonstrateMarkCompact();
        
        // 4. 引用计数算法演示
        simulation.demonstrateReferenceCounting();
        
        // 5. 分代收集算法演示
        simulation.demonstrateGenerationalCollection();
    }
    
    /**
     * 标记-清除算法演示
     * 
     * 原理：
     * 1. 标记阶段：从GC Roots开始，标记所有可达的对象
     * 2. 清除阶段：遍历堆，回收所有未被标记的对象
     * 
     * 优点：简单，不需要移动对象
     * 缺点：会产生内存碎片，效率较低
     */
    private void demonstrateMarkSweep() {
        System.out.println("=== 标记-清除算法演示 ===");
        
        MarkSweepHeap heap = new MarkSweepHeap(20);
        
        // 分配对象
        System.out.println("1. 分配对象阶段：");
        int obj1 = heap.allocate("Object1", 3);
        int obj2 = heap.allocate("Object2", 2);
        int obj3 = heap.allocate("Object3", 4);
        int obj4 = heap.allocate("Object4", 2);
        int obj5 = heap.allocate("Object5", 3);
        
        // 建立引用关系
        heap.addReference(obj1, obj2); // obj1 -> obj2
        heap.addReference(obj2, obj3); // obj2 -> obj3
        // obj4和obj5没有被引用，成为垃圾
        
        heap.printHeapStatus("分配完成");
        
        // 设置GC Roots
        Set<Integer> gcRoots = new HashSet<>();
        gcRoots.add(obj1); // 只有obj1作为根对象
        
        // 执行标记-清除GC
        System.out.println("\n2. 执行标记-清除GC：");
        heap.markSweepGC(gcRoots);
        
        heap.printHeapStatus("GC完成");
        
        System.out.println("标记-清除算法总结：");
        System.out.println("- 可达对象：Object1 -> Object2 -> Object3 被保留");
        System.out.println("- 垃圾对象：Object4、Object5 被回收");
        System.out.println("- 产生内存碎片，堆空间不连续\n");
    }
    
    /**
     * 标记-复制算法演示
     * 
     * 原理：
     * 1. 将内存分为两个大小相等的区域
     * 2. 只使用其中一个区域
     * 3. GC时将存活对象复制到另一个区域
     * 4. 清空原区域
     * 
     * 优点：没有内存碎片，分配效率高
     * 缺点：内存利用率只有50%
     */
    private void demonstrateMarkCopy() {
        System.out.println("=== 标记-复制算法演示 ===");
        
        MarkCopyHeap heap = new MarkCopyHeap(10); // 每个半区10个单位
        
        // 分配对象
        System.out.println("1. 分配对象阶段：");
        int obj1 = heap.allocate("Object1", 2);
        int obj2 = heap.allocate("Object2", 3);
        int obj3 = heap.allocate("Object3", 2);
        int obj4 = heap.allocate("Object4", 1);
        
        // 建立引用关系
        heap.addReference(obj1, obj2);
        heap.addReference(obj3, obj4);
        // obj2和obj4没有从根可达
        
        heap.printHeapStatus("分配完成");
        
        // 设置GC Roots
        Set<Integer> gcRoots = new HashSet<>();
        gcRoots.add(obj1);
        gcRoots.add(obj3);
        
        // 执行标记-复制GC
        System.out.println("\n2. 执行标记-复制GC：");
        heap.markCopyGC(gcRoots);
        
        heap.printHeapStatus("GC完成");
        
        System.out.println("标记-复制算法总结：");
        System.out.println("- 存活对象被复制到To空间，内存连续");
        System.out.println("- From空间被完全清空");
        System.out.println("- 没有内存碎片，但空间利用率低\n");
    }
    
    /**
     * 标记-整理算法演示
     * 
     * 原理：
     * 1. 标记阶段：标记所有存活对象
     * 2. 整理阶段：将存活对象向内存一端移动
     * 3. 清理阶段：清理边界外的内存
     * 
     * 优点：没有内存碎片，空间利用率100%
     * 缺点：需要移动对象，开销较大
     */
    private void demonstrateMarkCompact() {
        System.out.println("=== 标记-整理算法演示 ===");
        
        MarkCompactHeap heap = new MarkCompactHeap(15);
        
        // 分配对象
        System.out.println("1. 分配对象阶段：");
        int obj1 = heap.allocate("Object1", 2);
        int obj2 = heap.allocate("Object2", 3);  // 垃圾
        int obj3 = heap.allocate("Object3", 2);
        int obj4 = heap.allocate("Object4", 2);  // 垃圾
        int obj5 = heap.allocate("Object5", 3);
        
        // 建立引用关系
        heap.addReference(obj1, obj3);
        heap.addReference(obj3, obj5);
        // obj2和obj4没有被引用
        
        heap.printHeapStatus("分配完成");
        
        // 设置GC Roots
        Set<Integer> gcRoots = new HashSet<>();
        gcRoots.add(obj1);
        
        // 执行标记-整理GC
        System.out.println("\n2. 执行标记-整理GC：");
        heap.markCompactGC(gcRoots);
        
        heap.printHeapStatus("GC完成");
        
        System.out.println("标记-整理算法总结：");
        System.out.println("- 存活对象向内存一端移动，消除碎片");
        System.out.println("- 100%空间利用率");
        System.out.println("- 对象地址会发生变化，需要更新引用\n");
    }
    
    /**
     * 引用计数算法演示
     * 
     * 原理：
     * 1. 每个对象维护一个引用计数器
     * 2. 引用增加时计数+1，引用删除时计数-1
     * 3. 计数为0时立即回收对象
     * 
     * 优点：回收及时，没有暂停时间
     * 缺点：无法处理循环引用，计数器开销
     */
    private void demonstrateReferenceCounting() {
        System.out.println("=== 引用计数算法演示 ===");
        
        ReferenceCountingHeap heap = new ReferenceCountingHeap();
        
        System.out.println("1. 创建对象和引用：");
        
        // 创建对象
        int objA = heap.createObject("ObjectA");
        int objB = heap.createObject("ObjectB");
        int objC = heap.createObject("ObjectC");
        int objD = heap.createObject("ObjectD");
        
        heap.printStatus("创建对象后");
        
        // 建立引用关系
        heap.addReference(objA, objB);  // A -> B
        heap.addReference(objB, objC);  // B -> C
        heap.addReference(objC, objA);  // C -> A (循环引用)
        heap.addReference(objA, objD);  // A -> D
        
        heap.printStatus("建立引用后");
        
        System.out.println("\n2. 删除外部引用：");
        // 删除对A的外部引用（模拟局部变量超出作用域）
        heap.removeExternalReference(objA);
        heap.printStatus("删除外部引用后");
        
        System.out.println("引用计数算法总结：");
        System.out.println("- ObjectD被立即回收（计数为0）");
        System.out.println("- ObjectA、ObjectB、ObjectC形成循环引用，无法回收");
        System.out.println("- 这是引用计数算法的典型缺陷\n");
    }
    
    /**
     * 分代收集算法演示
     * 
     * 原理：
     * 1. 将对象按年龄分为年轻代和老年代
     * 2. 年轻代使用复制算法，回收频繁
     * 3. 老年代使用标记-整理算法，回收较少
     * 4. 对象从年轻代晋升到老年代
     * 
     * 优点：针对不同年龄特点优化，效率高
     * 缺点：实现复杂
     */
    private void demonstrateGenerationalCollection() {
        System.out.println("=== 分代收集算法演示 ===");
        
        GenerationalHeap heap = new GenerationalHeap();
        
        System.out.println("1. 对象分配阶段：");
        
        // 分配多批次对象，模拟实际应用
        List<Integer> longLivedObjects = new ArrayList<>();
        
        for (int generation = 1; generation <= 5; generation++) {
            System.out.printf("\n--- 第%d代对象分配 ---\n", generation);
            
            // 分配一些短期对象
            for (int i = 0; i < 5; i++) {
                heap.allocateObject("Temp_" + generation + "_" + i, false);
            }
            
            // 分配一些长期对象
            if (generation <= 3) {
                int longLived = heap.allocateObject("Long_" + generation, true);
                longLivedObjects.add(longLived);
            }
            
            heap.printStatus("分配后");
            
            // 执行年轻代GC
            if (generation >= 2) {
                System.out.println("执行Minor GC...");
                heap.minorGC();
                heap.printStatus("Minor GC后");
            }
            
            // 偶尔执行老年代GC
            if (generation == 4) {
                System.out.println("执行Major GC...");
                heap.majorGC();
                heap.printStatus("Major GC后");
            }
        }
        
        System.out.println("\n分代收集算法总结：");
        System.out.println("- 年轻代对象大部分被快速回收");
        System.out.println("- 长期存活对象晋升到老年代");
        System.out.println("- 不同代采用不同的回收策略");
        System.out.println("- 整体回收效率高，暂停时间短\n");
    }
}

/**
 * 标记-清除堆模拟
 */
class MarkSweepHeap {
    private final int capacity;
    private final HeapSlot[] heap;
    private final Map<Integer, Set<Integer>> references;
    private final AtomicInteger nextId;
    
    public MarkSweepHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new HeapSlot[capacity];
        this.references = new HashMap<>();
        this.nextId = new AtomicInteger(1);
    }
    
    public int allocate(String name, int size) {
        // 找到足够的连续空间
        for (int i = 0; i <= capacity - size; i++) {
            boolean canAllocate = true;
            for (int j = i; j < i + size; j++) {
                if (heap[j] != null) {
                    canAllocate = false;
                    break;
                }
            }
            
            if (canAllocate) {
                int id = nextId.getAndIncrement();
                for (int j = i; j < i + size; j++) {
                    heap[j] = new HeapSlot(id, name, j == i);
                }
                references.put(id, new HashSet<>());
                return id;
            }
        }
        throw new RuntimeException("内存不足，无法分配 " + size + " 个单位");
    }
    
    public void addReference(int from, int to) {
        references.get(from).add(to);
    }
    
    public void markSweepGC(Set<Integer> gcRoots) {
        // 1. 标记阶段
        Set<Integer> marked = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>(gcRoots);
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (!marked.contains(current)) {
                marked.add(current);
                queue.addAll(references.get(current));
            }
        }
        
        System.out.println("标记的对象：" + marked);
        
        // 2. 清除阶段
        Set<Integer> toRemove = new HashSet<>();
        for (int id : references.keySet()) {
            if (!marked.contains(id)) {
                toRemove.add(id);
            }
        }
        
        System.out.println("回收的对象：" + toRemove);
        
        // 清除堆中的对象
        for (int i = 0; i < capacity; i++) {
            if (heap[i] != null && toRemove.contains(heap[i].id)) {
                heap[i] = null;
            }
        }
        
        // 移除引用关系
        for (int id : toRemove) {
            references.remove(id);
        }
    }
    
    public void printHeapStatus(String phase) {
        System.out.println(phase + " - 堆状态：");
        for (int i = 0; i < capacity; i++) {
            if (heap[i] != null && heap[i].isStart) {
                System.out.printf("[%s]", heap[i].name);
            } else if (heap[i] != null) {
                System.out.print("[-]");
            } else {
                System.out.print("[_]");
            }
        }
        System.out.println();
    }
    
    private static class HeapSlot {
        final int id;
        final String name;
        final boolean isStart;
        
        HeapSlot(int id, String name, boolean isStart) {
            this.id = id;
            this.name = name;
            this.isStart = isStart;
        }
    }
}

/**
 * 标记-复制堆模拟
 */
class MarkCopyHeap {
    private final int halfSize;
    private HeapSlot[] fromSpace;
    private HeapSlot[] toSpace;
    private final Map<Integer, Set<Integer>> references;
    private final AtomicInteger nextId;
    private int fromPointer = 0;
    
    public MarkCopyHeap(int halfSize) {
        this.halfSize = halfSize;
        this.fromSpace = new HeapSlot[halfSize];
        this.toSpace = new HeapSlot[halfSize];
        this.references = new HashMap<>();
        this.nextId = new AtomicInteger(1);
    }
    
    public int allocate(String name, int size) {
        if (fromPointer + size > halfSize) {
            throw new RuntimeException("From空间不足");
        }
        
        int id = nextId.getAndIncrement();
        for (int i = fromPointer; i < fromPointer + size; i++) {
            fromSpace[i] = new HeapSlot(id, name, i == fromPointer);
        }
        fromPointer += size;
        references.put(id, new HashSet<>());
        return id;
    }
    
    public void addReference(int from, int to) {
        references.get(from).add(to);
    }
    
    public void markCopyGC(Set<Integer> gcRoots) {
        System.out.println("开始标记-复制GC...");
        
        // 标记并复制存活对象
        Set<Integer> copied = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>(gcRoots);
        int toPointer = 0;
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (!copied.contains(current)) {
                // 复制对象到to空间
                int size = getObjectSize(current);
                for (int i = toPointer; i < toPointer + size; i++) {
                    HeapSlot original = findObject(current);
                    toSpace[i] = new HeapSlot(current, original.name, i == toPointer);
                }
                toPointer += size;
                copied.add(current);
                
                // 继续标记引用的对象
                queue.addAll(references.get(current));
            }
        }
        
        System.out.println("复制的对象：" + copied);
        
        // 清空from空间
        Arrays.fill(fromSpace, null);
        
        // 交换from和to空间
        HeapSlot[] temp = fromSpace;
        fromSpace = toSpace;
        toSpace = temp;
        fromPointer = toPointer;
        
        // 移除未复制对象的引用
        Set<Integer> toRemove = new HashSet<>();
        for (int id : references.keySet()) {
            if (!copied.contains(id)) {
                toRemove.add(id);
            }
        }
        for (int id : toRemove) {
            references.remove(id);
        }
    }
    
    private int getObjectSize(int id) {
        int size = 0;
        for (HeapSlot slot : fromSpace) {
            if (slot != null && slot.id == id) {
                size++;
            }
        }
        return size;
    }
    
    private HeapSlot findObject(int id) {
        for (HeapSlot slot : fromSpace) {
            if (slot != null && slot.id == id && slot.isStart) {
                return slot;
            }
        }
        return null;
    }
    
    public void printHeapStatus(String phase) {
        System.out.println(phase + " - From空间：");
        for (int i = 0; i < halfSize; i++) {
            if (fromSpace[i] != null && fromSpace[i].isStart) {
                System.out.printf("[%s]", fromSpace[i].name);
            } else if (fromSpace[i] != null) {
                System.out.print("[-]");
            } else {
                System.out.print("[_]");
            }
        }
        System.out.println("\nTo空间：");
        for (int i = 0; i < halfSize; i++) {
            if (toSpace[i] != null && toSpace[i].isStart) {
                System.out.printf("[%s]", toSpace[i].name);
            } else if (toSpace[i] != null) {
                System.out.print("[-]");
            } else {
                System.out.print("[_]");
            }
        }
        System.out.println();
    }
    
    private static class HeapSlot {
        final int id;
        final String name;
        final boolean isStart;
        
        HeapSlot(int id, String name, boolean isStart) {
            this.id = id;
            this.name = name;
            this.isStart = isStart;
        }
    }
}

/**
 * 标记-整理堆模拟
 */
class MarkCompactHeap {
    private final int capacity;
    private final HeapSlot[] heap;
    private final Map<Integer, Set<Integer>> references;
    private final AtomicInteger nextId;
    
    public MarkCompactHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new HeapSlot[capacity];
        this.references = new HashMap<>();
        this.nextId = new AtomicInteger(1);
    }
    
    public int allocate(String name, int size) {
        for (int i = 0; i <= capacity - size; i++) {
            boolean canAllocate = true;
            for (int j = i; j < i + size; j++) {
                if (heap[j] != null) {
                    canAllocate = false;
                    break;
                }
            }
            
            if (canAllocate) {
                int id = nextId.getAndIncrement();
                for (int j = i; j < i + size; j++) {
                    heap[j] = new HeapSlot(id, name, j == i, size);
                }
                references.put(id, new HashSet<>());
                return id;
            }
        }
        throw new RuntimeException("内存不足");
    }
    
    public void addReference(int from, int to) {
        references.get(from).add(to);
    }
    
    public void markCompactGC(Set<Integer> gcRoots) {
        // 1. 标记阶段
        Set<Integer> marked = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>(gcRoots);
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (!marked.contains(current)) {
                marked.add(current);
                queue.addAll(references.get(current));
            }
        }
        
        System.out.println("标记的对象：" + marked);
        
        // 2. 整理阶段 - 将存活对象向左移动
        int writePointer = 0;
        Map<Integer, Integer> newAddresses = new HashMap<>();
        
        for (int readPointer = 0; readPointer < capacity; readPointer++) {
            if (heap[readPointer] != null && heap[readPointer].isStart 
                && marked.contains(heap[readPointer].id)) {
                
                int id = heap[readPointer].id;
                int size = heap[readPointer].size;
                newAddresses.put(id, writePointer);
                
                // 移动对象
                for (int i = 0; i < size; i++) {
                    if (writePointer + i != readPointer + i) {
                        heap[writePointer + i] = heap[readPointer + i];
                        heap[readPointer + i] = null;
                    }
                    if (heap[writePointer + i] != null) {
                        heap[writePointer + i].isStart = (i == 0);
                    }
                }
                writePointer += size;
            }
        }
        
        // 3. 清理阶段 - 清空剩余空间
        for (int i = writePointer; i < capacity; i++) {
            heap[i] = null;
        }
        
        // 移除垃圾对象的引用
        Set<Integer> toRemove = new HashSet<>();
        for (int id : references.keySet()) {
            if (!marked.contains(id)) {
                toRemove.add(id);
            }
        }
        for (int id : toRemove) {
            references.remove(id);
        }
        
        System.out.println("对象新地址：" + newAddresses);
    }
    
    public void printHeapStatus(String phase) {
        System.out.println(phase + " - 堆状态：");
        for (int i = 0; i < capacity; i++) {
            if (heap[i] != null && heap[i].isStart) {
                System.out.printf("[%s]", heap[i].name);
            } else if (heap[i] != null) {
                System.out.print("[-]");
            } else {
                System.out.print("[_]");
            }
        }
        System.out.println();
    }
    
    private static class HeapSlot {
        final int id;
        final String name;
        boolean isStart;
        final int size;
        
        HeapSlot(int id, String name, boolean isStart, int size) {
            this.id = id;
            this.name = name;
            this.isStart = isStart;
            this.size = size;
        }
    }
}

/**
 * 引用计数堆模拟
 */
class ReferenceCountingHeap {
    private final Map<Integer, RCObject> objects;
    private final Map<Integer, Set<Integer>> externalRefs;
    private final AtomicInteger nextId;
    
    public ReferenceCountingHeap() {
        this.objects = new HashMap<>();
        this.externalRefs = new HashMap<>();
        this.nextId = new AtomicInteger(1);
    }
    
    public int createObject(String name) {
        int id = nextId.getAndIncrement();
        objects.put(id, new RCObject(id, name));
        externalRefs.put(id, new HashSet<>());
        externalRefs.get(id).add(-1); // -1表示外部引用
        return id;
    }
    
    public void addReference(int from, int to) {
        objects.get(to).refCount++;
        objects.get(from).references.add(to);
    }
    
    public void removeExternalReference(int objectId) {
        externalRefs.get(objectId).remove(-1);
        decrementRefCount(objectId);
    }
    
    private void decrementRefCount(int objectId) {
        if (!objects.containsKey(objectId)) return;
        
        objects.get(objectId).refCount--;
        
        if (objects.get(objectId).refCount == 0) {
            System.out.println("回收对象：" + objects.get(objectId).name);
            
            // 递减引用的对象的计数
            for (int refId : objects.get(objectId).references) {
                decrementRefCount(refId);
            }
            
            objects.remove(objectId);
            externalRefs.remove(objectId);
        }
    }
    
    public void printStatus(String phase) {
        System.out.println(phase + "：");
        for (RCObject obj : objects.values()) {
            System.out.printf("  %s: 引用计数=%d, 引用对象=%s\n", 
                obj.name, obj.refCount, obj.references);
        }
        System.out.println();
    }
    
    private static class RCObject {
        final int id;
        final String name;
        int refCount = 1; // 初始有一个外部引用
        final Set<Integer> references = new HashSet<>();
        
        RCObject(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}

/**
 * 分代堆模拟
 */
class GenerationalHeap {
    private final List<GenerationalObject> youngGeneration;
    private final List<GenerationalObject> oldGeneration;
    private final Set<Integer> gcRoots;
    private final AtomicInteger nextId;
    private final int maxYoungAge = 3;
    
    public GenerationalHeap() {
        this.youngGeneration = new ArrayList<>();
        this.oldGeneration = new ArrayList<>();
        this.gcRoots = new HashSet<>();
        this.nextId = new AtomicInteger(1);
    }
    
    public int allocateObject(String name, boolean longLived) {
        int id = nextId.getAndIncrement();
        GenerationalObject obj = new GenerationalObject(id, name, longLived);
        youngGeneration.add(obj);
        
        if (longLived) {
            gcRoots.add(id);
        }
        
        return id;
    }
    
    public void minorGC() {
        System.out.println("执行Minor GC...");
        
        List<GenerationalObject> survivors = new ArrayList<>();
        
        for (GenerationalObject obj : youngGeneration) {
            if (gcRoots.contains(obj.id) || obj.longLived) {
                obj.age++;
                
                if (obj.age >= maxYoungAge) {
                    // 晋升到老年代
                    oldGeneration.add(obj);
                    System.out.println("  " + obj.name + " 晋升到老年代");
                } else {
                    survivors.add(obj);
                    System.out.println("  " + obj.name + " 在年轻代存活，年龄：" + obj.age);
                }
            } else {
                System.out.println("  " + obj.name + " 被回收");
            }
        }
        
        youngGeneration.clear();
        youngGeneration.addAll(survivors);
    }
    
    public void majorGC() {
        System.out.println("执行Major GC...");
        
        List<GenerationalObject> survivors = new ArrayList<>();
        
        for (GenerationalObject obj : oldGeneration) {
            if (gcRoots.contains(obj.id) || obj.longLived) {
                survivors.add(obj);
                System.out.println("  " + obj.name + " 在老年代存活");
            } else {
                System.out.println("  " + obj.name + " 从老年代回收");
            }
        }
        
        oldGeneration.clear();
        oldGeneration.addAll(survivors);
    }
    
    public void printStatus(String phase) {
        System.out.println(phase + "：");
        System.out.println("  年轻代对象数：" + youngGeneration.size());
        System.out.println("  老年代对象数：" + oldGeneration.size());
        
        System.out.print("  年轻代：");
        for (GenerationalObject obj : youngGeneration) {
            System.out.printf("%s(age:%d) ", obj.name, obj.age);
        }
        System.out.println();
        
        System.out.print("  老年代：");
        for (GenerationalObject obj : oldGeneration) {
            System.out.printf("%s(age:%d) ", obj.name, obj.age);
        }
        System.out.println("\n");
    }
    
    private static class GenerationalObject {
        final int id;
        final String name;
        final boolean longLived;
        int age = 0;
        
        GenerationalObject(int id, String name, boolean longLived) {
            this.id = id;
            this.name = name;
            this.longLived = longLived;
        }
    }
}