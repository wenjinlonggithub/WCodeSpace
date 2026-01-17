# JVM深度解析完整指南

## 📚 目录结构

本项目提供了JVM从底层原理到实战调优的完整代码示例和详细讲解。

```
Jvm/src/main/java/com/architecture/jvm/
├── runtime/                  # 运行时数据区
│   ├── RuntimeDataAreaDemo.java          # 运行时数据区完整演示
│   ├── VirtualMachineStackDemo.java      # 虚拟机栈深度解析
│   ├── HeapMemoryDeepDive.java           # 堆内存深度分析
│   ├── MetaspaceDemo.java                # 元空间详解
│   └── DirectMemoryDemo.java             # 直接内存演示
│
├── classloader/              # 类加载机制
│   ├── ClassLoaderExample.java           # 类加载器基础
│   ├── ClassLoadingProcess.java          # 类加载过程
│   ├── ParentDelegationModelDemo.java    # 双亲委派模型
│   ├── HotDeploymentDemo.java            # 热部署实现
│   └── ClassLoaderInterviewQuestions.java # 面试题
│
├── execution/                # 执行引擎
│   └── JITCompilationDemo.java           # JIT编译器深度演示
│
├── gc/                       # 垃圾回收
│   ├── GarbageCollectionExample.java     # GC演示
│   ├── GCAlgorithmSimulation.java        # GC算法模拟
│   ├── GCAlgorithmComparison.java        # GC算法对比
│   ├── ReferenceCountingDemo.java        # 引用计数
│   └── GCInterviewQuestions.java         # GC面试题
│
├── memory/                   # 内存管理
│   ├── MemoryManagementExample.java      # 内存管理示例
│   ├── MemoryAreaAnalysis.java           # 内存区域分析
│   ├── ObjectMemoryLayoutDemo.java       # 对象内存布局
│   └── MemoryInterviewQuestions.java     # 内存面试题
│
├── jmm/                      # Java内存模型
│   └── JavaMemoryModelDemo.java          # JMM深度演示
│
├── bytecode/                 # 字节码
│   ├── BytecodeAnalysisExample.java      # 字节码分析
│   └── BytecodeManipulationExample.java  # 字节码操作
│
└── tuning/                   # 性能调优
    ├── JVMMonitoringTools.java           # JVM监控工具
    ├── PerformanceTuningExample.java     # 性能调优示例
    └── ProductionTuningCases.java        # 生产环境调优实战
```

---

## 🎯 核心知识点

### 1. 运行时数据区 (Runtime Data Area)

#### 1.1 程序计数器 (PC Register)
- **特点**: 线程私有、唯一不会OOM的区域
- **作用**: 记录当前线程执行的字节码指令地址
- **实现**: 每个线程独立的PC，支持多线程并发执行

#### 1.2 Java虚拟机栈 (VM Stack)
- **栈帧结构**:
  - 局部变量表 (Local Variable Table)
  - 操作数栈 (Operand Stack)
  - 动态链接 (Dynamic Linking)
  - 方法返回地址 (Return Address)
- **异常**: StackOverflowError, OutOfMemoryError
- **应用**: 方法调用、局部变量存储

#### 1.3 堆内存 (Heap)
- **分代结构**:
  ```
  Heap
  ├── Young Generation (新生代)
  │   ├── Eden (8/10)
  │   ├── Survivor0 (1/10)
  │   └── Survivor1 (1/10)
  └── Old Generation (老年代 2/3)
  ```
- **对象分配流程**:
  1. 新对象优先在Eden区分配
  2. Eden满触发Minor GC
  3. 存活对象移到Survivor
  4. 年龄达阈值晋升老年代
  5. 大对象直接进老年代
- **TLAB优化**: 线程本地分配缓冲，减少同步开销

#### 1.4 方法区/元空间 (Metaspace)
- **JDK版本变化**:
  - JDK 7-: 永久代 (PermGen) - 堆内存
  - JDK 8+: 元空间 (Metaspace) - 本地内存
- **存储内容**:
  - 类元数据
  - 运行时常量池
  - 静态变量 (JDK 7+在堆中)
- **优势**: 动态扩展，不易OOM

#### 1.5 直接内存 (Direct Memory)
- **特点**: 堆外内存，不受GC直接管理
- **应用**: NIO的DirectByteBuffer
- **优势**: 零拷贝，提高IO性能
- **劣势**: 分配成本高，难以追踪

---

### 2. 类加载机制

#### 2.1 类加载过程
```
加载 (Loading)
  ↓
链接 (Linking)
  ├── 验证 (Verification)
  ├── 准备 (Preparation)
  └── 解析 (Resolution)
  ↓
初始化 (Initialization)
```

#### 2.2 双亲委派模型
```
Bootstrap ClassLoader (启动类加载器)
  ↑ parent
Extension ClassLoader (扩展类加载器)
  ↑ parent
Application ClassLoader (应用程序类加载器)
  ↑ parent
Custom ClassLoader (自定义类加载器)
```

**优点**:
- 避免类的重复加载
- 保护核心类库
- 保证类加载的一致性

**打破双亲委派的场景**:
- Tomcat类加载器
- OSGi模块化框架
- SPI机制
- 热部署

#### 2.3 热部署原理
1. 使用自定义ClassLoader加载类
2. 类文件修改时创建新ClassLoader
3. 新ClassLoader加载修改后的类
4. 丢弃旧ClassLoader等待GC回收

---

### 3. 执行引擎

#### 3.1 解释执行 vs JIT编译
- **解释器**: 逐行解释字节码，启动快但执行慢
- **JIT编译器**: 将热点代码编译为本地代码，执行快

#### 3.2 HotSpot的两个编译器
- **C1 (Client Compiler)**: 快速编译，优化较少
- **C2 (Server Compiler)**: 慢速编译，激进优化

#### 3.3 分层编译 (Tiered Compilation)
```
Level 0: 解释执行
Level 1: C1编译，无profiling
Level 2: C1编译 + 调用计数器
Level 3: C1编译 + 完整profiling
Level 4: C2编译，完全优化
```

#### 3.4 编译优化技术
- **逃逸分析** (Escape Analysis)
  - 栈上分配
  - 标量替换
  - 锁消除
- **方法内联** (Method Inlining)
- **公共子表达式消除** (CSE)
- **循环展开** (Loop Unrolling)
- **死代码消除** (Dead Code Elimination)

---

### 4. 垃圾回收

#### 4.1 GC算法
1. **标记-清除** (Mark-Sweep)
   - 标记存活对象，清除未标记对象
   - 产生内存碎片

2. **复制算法** (Copying)
   - 内存分两块，存活对象复制到另一块
   - 无碎片，但空间利用率低
   - 用于新生代

3. **标记-整理** (Mark-Compact)
   - 标记后整理内存，移动对象
   - 无碎片，但移动成本高
   - 用于老年代

4. **分代收集** (Generational Collection)
   - 新生代用复制算法
   - 老年代用标记-整理

#### 4.2 GC收集器对比

| 收集器 | 类型 | 算法 | 目标 | 适用场景 |
|--------|------|------|------|----------|
| Serial | 单线程 | 复制 | 吞吐量 | 客户端应用 |
| ParNew | 多线程 | 复制 | 吞吐量 | 配合CMS |
| Parallel Scavenge | 多线程 | 复制 | 吞吐量 | 后台计算 |
| Serial Old | 单线程 | 标记-整理 | 吞吐量 | 客户端应用 |
| Parallel Old | 多线程 | 标记-整理 | 吞吐量 | 后台计算 |
| CMS | 多线程 | 标记-清除 | 低延迟 | Web应用 |
| G1 | 多线程 | 整体标记-整理 | 可控延迟 | 大堆应用 |
| ZGC | 多线程 | 着色指针 | 超低延迟 | 超大堆 |
| Shenandoah | 多线程 | 并发整理 | 超低延迟 | 超大堆 |

#### 4.3 引用类型
- **强引用**: 普通引用，永不回收
- **软引用**: 内存不足时回收
- **弱引用**: GC时回收
- **虚引用**: 对象回收时收到通知

---

### 5. 对象内存布局

```
┌──────────────────────────┐
│ 对象头 (Object Header)    │
├──────────────────────────┤
│ Mark Word (8字节)         │ 哈希码、GC年龄、锁标志
├──────────────────────────┤
│ Class Pointer (4/8字节)   │ 类型指针
├──────────────────────────┤
│ Array Length (4字节)      │ 仅数组对象
├──────────────────────────┤
│ 实例数据 (Instance Data)  │
├──────────────────────────┤
│ 对齐填充 (Padding)         │ 补齐到8字节倍数
└──────────────────────────┘
```

#### Mark Word状态
- **无锁**: hashCode + GC年龄 + 01
- **偏向锁**: 线程ID + epoch + 101
- **轻量级锁**: 栈中锁记录指针 + 00
- **重量级锁**: Monitor指针 + 10
- **GC标记**: 11

#### 压缩指针 (Compressed Oops)
- 将64位指针压缩为32位
- 适用于堆 < 32GB
- 节省约30%内存

---

### 6. Java内存模型 (JMM)

#### 6.1 JMM抽象结构
```
Thread1     Thread2     Thread3
   ↓           ↓           ↓
工作内存    工作内存    工作内存
   ↓           ↓           ↓
         主内存 (Main Memory)
```

#### 6.2 三大特性
1. **原子性**: synchronized, Lock
2. **可见性**: volatile, synchronized, Lock
3. **有序性**: volatile, synchronized, happens-before

#### 6.3 happens-before规则
1. 程序次序规则
2. 监视器锁规则
3. volatile变量规则
4. 线程启动规则
5. 线程终止规则
6. 线程中断规则
7. 对象终结规则
8. 传递性

#### 6.4 volatile原理
- **保证可见性**: 写入立即刷新到主内存
- **禁止重排序**: 插入内存屏障
- **不保证原子性**: count++非原子

#### 6.5 内存屏障
- LoadLoad Barrier
- StoreStore Barrier
- LoadStore Barrier
- StoreLoad Barrier

---

### 7. 性能调优实战

#### 7.1 OOM问题排查
```bash
# 1. 查看堆信息
jmap -heap <pid>

# 2. Dump堆快照
jmap -dump:format=b,file=heap.hprof <pid>

# 3. MAT分析
# 打开heap.hprof，生成Leak Suspects报告

# 4. 查找大对象
# Histogram → 按大小排序
```

#### 7.2 CPU飙高排查
```bash
# 1. 找到Java进程
top -c

# 2. 找到高CPU线程
top -Hp <pid>

# 3. 转换线程ID为16进制
printf '%x\n' <tid>

# 4. 查看线程栈
jstack <pid> | grep -A 50 <hex_tid>
```

#### 7.3 频繁GC排查
```bash
# 1. 查看GC统计
jstat -gcutil <pid> 1000

# 2. 分析GC日志
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps

# 3. GC可视化分析
使用GCViewer分析日志
```

#### 7.4 内存泄漏排查
```bash
# 1. 获取两次堆快照
jmap -dump:format=b,file=heap1.hprof <pid>
# 运行一段时间
jmap -dump:format=b,file=heap2.hprof <pid>

# 2. MAT对比分析
# 打开heap2.hprof
# 与heap1.hprof对比

# 3. 分析引用链
# Path to GC Roots
# 找到阻止回收的引用
```

#### 7.5 死锁检测
```bash
# 1. jstack检测
jstack <pid> | grep "deadlock"

# 2. 编程检测
ThreadMXBean.findDeadlockedThreads()
```

---

## 🛠️ JVM参数配置

### 堆内存配置
```bash
-Xms4g                      # 初始堆大小
-Xmx4g                      # 最大堆大小
-Xmn2g                      # 新生代大小
-XX:NewRatio=2              # 老年代/新生代比例
-XX:SurvivorRatio=8         # Eden/Survivor比例
```

### GC选择
```bash
# G1 GC (推荐)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m

# ZGC (超低延迟)
-XX:+UseZGC

# Parallel GC (高吞吐)
-XX:+UseParallelGC
```

### 元空间
```bash
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
```

### GC日志
```bash
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-Xloggc:/var/log/gc.log
```

### OOM时Dump
```bash
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/dumps/
```

### 完整示例 (4G堆，G1 GC)
```bash
java \
  -Xms4g -Xmx4g \
  -Xmn2g \
  -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+PrintGCDetails \
  -Xloggc:/var/log/gc.log \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/dumps/ \
  -jar app.jar
```

---

## 📊 监控工具

### 命令行工具
| 工具 | 功能 |
|------|------|
| jps | 查看Java进程 |
| jstat | GC统计 |
| jmap | Dump堆、查看堆信息 |
| jstack | Dump线程栈 |
| jinfo | 查看/修改JVM参数 |
| jcmd | 多功能诊断 |

### 可视化工具
- **JConsole**: JDK自带监控
- **VisualVM**: 功能强大的Profile
- **JProfiler**: 商业Profile工具
- **MAT**: 内存分析工具
- **GCViewer**: GC日志分析
- **Arthas**: 阿里开源诊断工具

---

## 💡 最佳实践

### 1. 内存配置
- ✓ 堆初始值和最大值设置相同
- ✓ 新生代占堆的1/2到1/3
- ✓ 元空间设置合理上限
- ✗ 堆不要设置过大 (影响GC时间)

### 2. GC选择
- Web应用: G1 GC
- 批处理: Parallel GC
- 低延迟: ZGC/Shenandoah
- 小堆: Serial GC

### 3. 监控告警
- 堆使用率 > 80%
- Full GC频率 > 1次/分钟
- GC耗时 > 1秒
- 线程数异常增长

### 4. 代码优化
- 对象复用 (池化)
- 避免大对象
- 及时释放资源
- 使用合适的集合大小

---

## 🎓 学习路径

1. **基础阶段**:
   - 运行时数据区
   - 类加载机制
   - 对象创建和访问

2. **进阶阶段**:
   - 垃圾回收算法
   - GC收集器对比
   - 内存模型

3. **高级阶段**:
   - JIT编译优化
   - 字节码操作
   - 性能调优

4. **实战阶段**:
   - 线上问题排查
   - 参数调优
   - 监控体系搭建

---

## 📖 推荐资源

### 书籍
- 《深入理解Java虚拟机》(周志明)
- 《Java性能权威指南》
- 《HotSpot实战》

### 官方文档
- [JVM规范](https://docs.oracle.com/javase/specs/jvms/se11/html/)
- [GC调优指南](https://docs.oracle.com/en/java/javase/11/gctuning/)

### 工具
- [JOL (Java Object Layout)](https://openjdk.java.net/projects/code-tools/jol/)
- [Arthas](https://arthas.aliyun.com/)
- [MAT](https://www.eclipse.org/mat/)

---

## 🚀 快速开始

```bash
# 1. 克隆项目
cd Jvm

# 2. 运行示例
# 运行时数据区演示
java -cp target/classes com.architecture.jvm.runtime.RuntimeDataAreaDemo

# JIT编译演示
java -cp target/classes com.architecture.jvm.execution.JITCompilationDemo

# 对象内存布局
java -cp target/classes com.architecture.jvm.memory.ObjectMemoryLayoutDemo

# JMM演示
java -cp target/classes com.architecture.jvm.jmm.JavaMemoryModelDemo

# 性能调优案例
java -cp target/classes com.architecture.jvm.tuning.ProductionTuningCases
```

---

## 📝 总结

本项目提供了从JVM底层原理到生产实战的完整知识体系：

✅ **15+** 核心演示类
✅ **1000+** 行详细注释
✅ **50+** 实战案例
✅ **100+** 知识点覆盖

涵盖：运行时数据区、类加载、执行引擎、垃圾回收、内存模型、对象布局、性能调优等全部核心内容。

每个演示类都包含：
- 📖 原理讲解
- 💻 代码示例
- 🎯 实战案例
- 💡 最佳实践

**开始你的JVM深度学习之旅吧！** 🎉

---

## 📚 扩展阅读

为了更全面地掌握JVM知识，我们还提供了以下扩展文档：

- [JVM深度剖析与实战指南](./docs/JVM_DEEP_DIVE.md) - 涵盖字节码分析、内存模型细节、垃圾收集器深入分析和性能调优实战
- [JVM故障诊断与调优实战指南](./docs/JVM_TROUBLESHOOTING_GUIDE.md) - 详细介绍常见JVM问题类型、故障排查方法和实际案例分析
- [JVM性能监控与最佳实践](./docs/JVM_MONITORING_BEST_PRACTICES.md) - 深入讲解JVM性能监控、指标设定和监控体系建设

---

## 👨‍💻 Author
Architecture Team

## 📄 License
MIT License
