package com.architecture.jvm.memory;

/**
 * JVM内存管理面试题汇总
 * 
 * 涵盖内存结构、内存分配、内存溢出等核心面试问题
 */
public class MemoryInterviewQuestions {

    /**
     * ====================
     * 一、JVM内存结构问题
     * ====================
     */

    /**
     * Q1: JVM内存结构包括哪些区域？
     * 
     * A: JVM内存结构详解：
     * 
     * 1. 程序计数器 (Program Counter Register)
     *    - 作用：记录当前线程执行的字节码指令地址
     *    - 特点：线程私有，生命周期与线程相同
     *    - 大小：较小，用于存储指令地址
     *    - 异常：唯一不会发生OutOfMemoryError的区域
     * 
     * 2. Java虚拟机栈 (JVM Stack)
     *    - 作用：存储方法调用的栈帧(局部变量、操作数栈等)
     *    - 特点：线程私有，每个方法对应一个栈帧
     *    - 异常：StackOverflowError和OutOfMemoryError
     *    - 配置：-Xss参数设置栈大小
     * 
     * 3. 本地方法栈 (Native Method Stack)
     *    - 作用：为Native方法服务
     *    - 特点：与虚拟机栈类似，但服务于Native方法
     *    - 实现：HotSpot VM中与虚拟机栈合并实现
     * 
     * 4. 堆内存 (Heap)
     *    - 作用：存储对象实例和数组
     *    - 特点：所有线程共享，垃圾收集的主要区域
     *    - 分代：年轻代(Eden、Survivor0、Survivor1)、老年代
     *    - 配置：-Xms初始大小、-Xmx最大大小
     * 
     * 5. 方法区 (Method Area)
     *    - 作用：存储类信息、常量、静态变量、即时编译代码
     *    - 特点：所有线程共享，逻辑上属于堆的一部分
     *    - 实现：JDK7前为永久代，JDK8后为元空间(Metaspace)
     *    - 包含：运行时常量池
     * 
     * 6. 直接内存 (Direct Memory)
     *    - 作用：NIO操作使用的堆外内存
     *    - 特点：不受JVM堆大小限制，但受本机总内存限制
     *    - 配置：-XX:MaxDirectMemorySize
     * 
     * 内存区域分类：
     * - 线程私有：程序计数器、虚拟机栈、本地方法栈
     * - 线程共享：堆内存、方法区
     */
    public void jvmMemoryStructure() {
        System.out.println("JVM内存：PC寄存器 + 虚拟机栈 + 本地方法栈 + 堆 + 方法区 + 直接内存");
    }

    /**
     * Q2: 堆内存的分代结构是怎样的？
     * 
     * A: 堆内存分代详解：
     * 
     * 1. 年轻代 (Young Generation)
     *    组成结构：
     *    - Eden区 (Eden Space)
     *      • 作用：新对象分配区域
     *      • 大小：约占年轻代的80%
     *      • 特点：分配速度快，采用指针碰撞
     *    
     *    - Survivor0区 (From Space)
     *      • 作用：存放GC后存活的对象
     *      • 大小：约占年轻代的10%
     *      • 特点：与Survivor1区交换角色
     *    
     *    - Survivor1区 (To Space)
     *      • 作用：存放GC后存活的对象
     *      • 大小：约占年轻代的10%
     *      • 特点：任何时刻都有一个Survivor区为空
     * 
     * 2. 老年代 (Old Generation/Tenured Space)
     *    - 作用：存放长期存活的对象
     *    - 进入条件：
     *      • 大对象直接分配到老年代
     *      • 长期存活的对象(经历多次Minor GC)
     *      • 动态年龄判定晋升的对象
     *    - 特点：空间大，GC频率低但耗时长
     * 
     * 3. 分代假设 (Generational Hypothesis)
     *    - 弱分代假设：大部分对象很快死亡
     *    - 强分代假设：存活时间长的对象继续存活的概率大
     *    - 跨代引用假设：老年代对象引用年轻代对象很少
     * 
     * 4. 对象分配流程
     *    对象分配过程：
     *    1. 新对象首先尝试在Eden区分配
     *    2. Eden区空间不足时触发Minor GC
     *    3. 存活对象移动到Survivor区
     *    4. Survivor区对象年龄+1
     *    5. 年龄达到阈值(默认15)的对象晋升到老年代
     *    6. 大对象可能直接分配到老年代
     * 
     * 5. 相关JVM参数
     *    年轻代配置：
     *    -XX:NewRatio=3          # 老年代/年轻代比例
     *    -XX:SurvivorRatio=8     # Eden/Survivor比例
     *    -XX:MaxTenuringThreshold=15  # 晋升年龄阈值
     *    -XX:PretenureSizeThreshold=1MB  # 大对象阈值
     * 
     * 6. 优势和作用
     *    - 提高GC效率：针对不同区域采用不同的GC算法
     *    - 减少GC时间：年轻代GC快速清理短命对象
     *    - 优化内存分配：新对象快速分配，老对象稳定存储
     *    - 适应应用特点：符合大多数应用的对象生命周期特点
     */
    public void heapGenerationalStructure() {
        System.out.println("堆分代：年轻代(Eden+S0+S1) + 老年代 + 分代假设 + 晋升机制");
    }

    /**
     * Q3: 方法区和永久代、元空间的区别？
     * 
     * A: 方法区演进历史：
     * 
     * 1. 方法区 (Method Area)
     *    - 定义：JVM规范中定义的逻辑概念
     *    - 作用：存储类信息、常量、静态变量、JIT代码
     *    - 特点：所有线程共享的内存区域
     *    - 规范：属于JVM规范，不是具体实现
     * 
     * 2. 永久代 (PermGen) - JDK7及之前
     *    实现特点：
     *    - HotSpot VM中方法区的具体实现
     *    - 使用堆内存的一部分来实现方法区
     *    - 大小固定，容易出现OutOfMemoryError
     *    - 垃圾回收效率低，难以调优
     * 
     *    存储内容：
     *    - 类的元数据信息
     *    - 运行时常量池
     *    - 静态变量
     *    - 即时编译器编译后的代码
     * 
     *    相关参数：
     *    -XX:PermSize=128m       # 初始永久代大小
     *    -XX:MaxPermSize=256m    # 最大永久代大小
     * 
     * 3. 元空间 (Metaspace) - JDK8及之后
     *    实现改进：
     *    - 使用本地内存(Native Memory)实现
     *    - 不再受固定大小限制
     *    - 自动扩展，减少OutOfMemoryError
     *    - 改善了GC性能
     * 
     *    存储变化：
     *    - 类的元数据：迁移到元空间
     *    - 字符串常量池：迁移到堆内存
     *    - 静态变量：迁移到堆内存
     *    - 运行时常量池：迁移到堆内存
     * 
     *    相关参数：
     *    -XX:MetaspaceSize=128m      # 初始元空间大小
     *    -XX:MaxMetaspaceSize=512m   # 最大元空间大小
     *    -XX:CompressedClassSpaceSize=1g  # 压缩类空间大小
     * 
     * 4. 迁移的好处
     *    内存管理：
     *    - 避免永久代OOM：元空间可以自动扩展
     *    - 简化GC：减少Full GC的触发
     *    - 提高性能：本地内存访问更高效
     *    - 更好的监控：独立的内存区域便于监控
     * 
     *    内存布局：
     *    JDK7: 堆 + 永久代
     *    JDK8: 堆 + 元空间(本地内存)
     * 
     * 5. 实际影响
     *    开发影响：
     *    - 减少OOM：方法区溢出大幅减少
     *    - 参数调整：需要调整新的JVM参数
     *    - 监控变化：监控工具需要支持元空间
     *    - 性能提升：整体内存管理性能提升
     * 
     * 6. 常见问题
     *    永久代问题：
     *    - java.lang.OutOfMemoryError: PermGen space
     *    - 动态生成类导致的内存泄漏
     *    - CGLib、动态代理使用过多
     * 
     *    元空间问题：
     *    - java.lang.OutOfMemoryError: Metaspace
     *    - 仍然可能发生，但概率大大降低
     */
    public void methodAreaEvolution() {
        System.out.println("方法区演进：规范定义 → 永久代实现 → 元空间优化");
    }

    /**
     * ====================
     * 二、内存分配和回收问题
     * ====================
     */

    /**
     * Q4: 对象在内存中是如何分配的？
     * 
     * A: 对象内存分配详解：
     * 
     * 1. 对象分配的基本流程
     *    分配步骤：
     *    1. 检查类是否已加载、解析、初始化
     *    2. 计算对象所需内存大小
     *    3. 在堆内存中为对象分配空间
     *    4. 将分配的内存初始化为零值
     *    5. 设置对象头信息
     *    6. 执行对象构造方法(<init>)
     * 
     * 2. 内存分配策略
     *    (1) Eden区优先分配
     *    - 新对象首先尝试在Eden区分配
     *    - 使用指针碰撞或空闲列表分配
     *    - 分配速度快，支持TLAB优化
     * 
     *    (2) 大对象直接进入老年代
     *    - 大小超过PretenureSizeThreshold的对象
     *    - 避免在Eden和Survivor区之间复制
     *    - 典型的大对象：大数组、大字符串
     * 
     *    (3) 长期存活对象进入老年代
     *    - 对象年龄达到MaxTenuringThreshold
     *    - 每次Minor GC后年龄加1
     *    - 动态年龄判定可能提前晋升
     * 
     * 3. 内存分配算法
     *    (1) 指针碰撞 (Bump the Pointer)
     *    - 适用：内存规整的情况
     *    - 原理：移动指针分配连续内存
     *    - 优点：分配速度快
     *    - 收集器：Serial、ParNew等
     * 
     *    (2) 空闲列表 (Free List)
     *    - 适用：内存不规整的情况
     *    - 原理：维护空闲内存块列表
     *    - 优点：适应碎片化内存
     *    - 收集器：CMS等
     * 
     * 4. 并发分配安全
     *    问题：多线程并发分配可能冲突
     *    
     *    解决方案：
     *    (1) CAS + 重试
     *    - 使用原子操作更新指针
     *    - 失败后重试分配
     *    - 实现简单但性能可能受影响
     * 
     *    (2) TLAB (Thread Local Allocation Buffer)
     *    - 每个线程预分配一块内存区域
     *    - 线程内分配无需同步
     *    - TLAB用完后才需要同步
     *    - 参数：-XX:+UseTLAB
     * 
     * 5. 特殊分配情况
     *    (1) 栈上分配 (Stack Allocation)
     *    - 条件：逃逸分析确定对象不会逃逸
     *    - 优点：随方法结束自动销毁，无需GC
     *    - 参数：-XX:+DoEscapeAnalysis
     * 
     *    (2) 标量替换 (Scalar Replacement)
     *    - 条件：对象不会逃逸且可分解
     *    - 原理：将对象分解为基本类型变量
     *    - 优点：减少内存分配和GC压力
     * 
     * 6. 内存分配相关参数
     *    Eden区配置：
     *    -XX:SurvivorRatio=8        # Eden与Survivor比例
     *    -XX:NewRatio=2             # 年轻代与老年代比例
     *    
     *    大对象配置：
     *    -XX:PretenureSizeThreshold=1MB  # 大对象阈值
     *    
     *    TLAB配置：
     *    -XX:+UseTLAB              # 启用TLAB
     *    -XX:TLABSize=256k         # TLAB大小
     * 
     * 7. 分配性能优化
     *    优化策略：
     *    - 合理设置堆大小和分代比例
     *    - 启用TLAB减少分配竞争
     *    - 控制大对象的创建
     *    - 利用逃逸分析优化
     *    - 避免过度分配临时对象
     */
    public void objectAllocationProcess() {
        System.out.println("对象分配：Eden优先 + 大对象直入老年代 + 长期存活晋升 + TLAB优化");
    }

    /**
     * Q5: 什么情况下会发生内存溢出？如何定位和解决？
     * 
     * A: 内存溢出问题全解析：
     * 
     * 1. 堆内存溢出 (java.lang.OutOfMemoryError: Java heap space)
     *    发生原因：
     *    - 内存泄漏：对象无法被GC回收
     *    - 内存容量不足：堆大小设置过小
     *    - 程序创建了大量对象
     * 
     *    典型场景：
     *    - 集合类持有大量对象不释放
     *    - 缓存使用不当，无限制增长
     *    - 递归调用创建大量对象
     *    - 数据库查询返回大量数据
     * 
     *    解决方案：
     *    - 增加堆内存：-Xmx参数
     *    - 优化代码：及时释放不用的对象引用
     *    - 使用内存分析工具：MAT、JProfiler
     *    - 实现对象复用和池化
     * 
     * 2. 栈内存溢出 (java.lang.StackOverflowError)
     *    发生原因：
     *    - 递归调用层次过深
     *    - 方法调用链过长
     *    - 局部变量过多或过大
     * 
     *    典型场景：
     *    - 无限递归或递归层次过深
     *    - 相互调用形成死循环
     *    - 栈空间设置过小
     * 
     *    解决方案：
     *    - 检查递归逻辑，设置递归出口
     *    - 增加栈大小：-Xss参数
     *    - 改递归为循环实现
     *    - 减少局部变量使用
     * 
     * 3. 方法区/元空间溢出
     *    JDK7及之前 (PermGen space)：
     *    - 加载的类过多
     *    - 常量池过大
     *    - CGLib等动态生成类过多
     * 
     *    JDK8及之后 (Metaspace)：
     *    - 仍可能发生，但概率降低
     *    - 主要原因是类加载过多
     * 
     *    解决方案：
     *    - 增加元空间大小：-XX:MaxMetaspaceSize
     *    - 检查类加载器泄漏
     *    - 优化动态代理和反射使用
     *    - 清理不必要的jar包
     * 
     * 4. 直接内存溢出 (Direct buffer memory)
     *    发生原因：
     *    - NIO操作使用DirectByteBuffer过多
     *    - 直接内存没有及时释放
     *    - 超过MaxDirectMemorySize限制
     * 
     *    典型场景：
     *    - NIO文件操作
     *    - Netty等NIO框架使用
     *    - 缓存框架使用堆外内存
     * 
     *    解决方案：
     *    - 增加直接内存：-XX:MaxDirectMemorySize
     *    - 及时释放DirectByteBuffer
     *    - 监控直接内存使用情况
     * 
     * 5. 内存溢出定位方法
     *    (1) 日志分析
     *    - 启用GC日志：-XX:+PrintGC -XX:+PrintGCDetails
     *    - 生成Heap Dump：-XX:+HeapDumpOnOutOfMemoryError
     *    - 设置Dump路径：-XX:HeapDumpPath=/path/to/dump
     * 
     *    (2) 分析工具
     *    - Eclipse MAT：内存分析工具
     *    - JProfiler：商业性能分析工具
     *    - JVisualVM：Oracle提供的可视化工具
     *    - jmap、jhat：JDK自带工具
     * 
     *    (3) 监控指标
     *    - 堆内存使用趋势
     *    - GC频率和耗时
     *    - 对象创建速率
     *    - 内存泄漏点
     * 
     * 6. 预防措施
     *    设计层面：
     *    - 合理设计缓存策略
     *    - 避免大对象频繁创建
     *    - 实现对象池复用
     *    - 及时关闭资源
     * 
     *    配置层面：
     *    - 根据应用特点调整JVM参数
     *    - 设置合理的内存大小
     *    - 启用内存溢出时的自动处理
     * 
     *    监控层面：
     *    - 建立内存监控体系
     *    - 设置内存告警阈值
     *    - 定期进行压力测试
     */
    public void outOfMemoryProblems() {
        System.out.println("内存溢出：堆OOM + 栈溢出 + 元空间OOM + 直接内存OOM + 定位分析");
    }

    /**
     * ====================
     * 三、内存优化问题
     * ====================
     */

    /**
     * Q6: 如何进行JVM内存调优？
     * 
     * A: JVM内存调优全面指南：
     * 
     * 1. 调优目标
     *    性能指标：
     *    - 吞吐量：应用程序运行时间 / (应用程序运行时间 + GC时间)
     *    - 延迟：GC暂停时间
     *    - 内存使用：堆内存使用效率
     *    - 可用性：减少OutOfMemoryError发生
     * 
     * 2. 内存大小调优
     *    (1) 堆内存设置
     *    基本参数：
     *    -Xms2g -Xmx4g              # 初始和最大堆大小
     *    -XX:NewRatio=2             # 老年代/年轻代 = 2:1
     *    -XX:SurvivorRatio=8        # Eden/Survivor = 8:1:1
     * 
     *    设置原则：
     *    - Xms和Xmx设置为相同值避免动态扩展
     *    - 堆大小根据应用内存需求设置
     *    - 年轻代大小影响Minor GC频率
     *    - Survivor区不能过小，避免对象过早晋升
     * 
     *    (2) 非堆内存设置
     *    元空间配置：
     *    -XX:MetaspaceSize=128m      # 初始元空间大小
     *    -XX:MaxMetaspaceSize=512m   # 最大元空间大小
     * 
     *    直接内存配置：
     *    -XX:MaxDirectMemorySize=1g  # 最大直接内存
     * 
     * 3. GC策略调优
     *    (1) 选择合适的垃圾收集器
     *    - Serial GC：单线程，适合小应用
     *    - Parallel GC：多线程，适合吞吐量优先
     *    - G1 GC：低延迟，适合大堆应用
     *    - ZGC/Shenandoah：超低延迟收集器
     * 
     *    (2) GC参数调优
     *    Parallel GC参数：
     *    -XX:+UseParallelGC          # 使用Parallel收集器
     *    -XX:ParallelGCThreads=8     # GC线程数
     *    -XX:GCTimeRatio=99          # GC时间比例
     * 
     *    G1 GC参数：
     *    -XX:+UseG1GC               # 使用G1收集器
     *    -XX:MaxGCPauseMillis=200   # 最大暂停时间
     *    -XX:G1HeapRegionSize=16m   # G1区域大小
     * 
     * 4. 对象分配优化
     *    (1) TLAB优化
     *    -XX:+UseTLAB               # 启用TLAB
     *    -XX:TLABWasteTargetPercent=1  # TLAB浪费阈值
     * 
     *    (2) 大对象处理
     *    -XX:PretenureSizeThreshold=1m  # 大对象直接进老年代
     * 
     *    (3) 逃逸分析
     *    -XX:+DoEscapeAnalysis      # 启用逃逸分析
     *    -XX:+EliminateAllocations  # 标量替换
     * 
     * 5. 监控和诊断
     *    (1) GC日志
     *    -XX:+PrintGC               # 打印GC信息
     *    -XX:+PrintGCDetails        # 详细GC信息
     *    -XX:+PrintGCTimeStamps     # GC时间戳
     *    -Xloggc:/path/to/gc.log    # GC日志文件
     * 
     *    (2) Heap Dump
     *    -XX:+HeapDumpOnOutOfMemoryError  # OOM时生成Dump
     *    -XX:HeapDumpPath=/path/to/dump   # Dump文件路径
     * 
     *    (3) JVM参数
     *    -XX:+PrintCommandLineFlags       # 打印JVM参数
     *    -XX:+PrintFlagsFinal            # 打印最终参数值
     * 
     * 6. 调优流程
     *    (1) 现状分析
     *    - 收集应用性能指标
     *    - 分析GC日志
     *    - 识别性能瓶颈
     * 
     *    (2) 参数调整
     *    - 根据应用特点调整内存大小
     *    - 选择合适的垃圾收集器
     *    - 优化GC参数
     * 
     *    (3) 测试验证
     *    - 进行压力测试
     *    - 监控关键指标
     *    - 对比调优前后效果
     * 
     *    (4) 持续优化
     *    - 建立监控体系
     *    - 定期审查和调整
     *    - 跟踪应用变化
     * 
     * 7. 常见调优案例
     *    (1) 高频GC问题
     *    - 现象：Minor GC频繁
     *    - 原因：年轻代过小或对象创建过快
     *    - 解决：增大年轻代或优化对象创建
     * 
     *    (2) 长时间GC问题
     *    - 现象：Full GC时间长
     *    - 原因：老年代碎片或大对象过多
     *    - 解决：选择低延迟收集器或优化对象分配
     * 
     *    (3) 内存泄漏问题
     *    - 现象：内存使用持续增长
     *    - 原因：对象无法被GC回收
     *    - 解决：使用内存分析工具找到泄漏点
     */
    public void jvmMemoryTuning() {
        System.out.println("内存调优：大小设置 + GC策略 + 分配优化 + 监控诊断 + 持续改进");
    }
}