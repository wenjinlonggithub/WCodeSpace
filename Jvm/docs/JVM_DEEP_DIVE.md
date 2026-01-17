# JVM深度剖析与实战指南

## 深入理解JVM核心技术

本文档是对现有[JVM_GUIDE.md](file:///D:/develop/20/WCodeSpace/Jvm/JVM_GUIDE.md)文档的重要补充，涵盖JVM的更深层次内容，包括字节码操作、内存模型细节、垃圾收集器深入分析和性能调优实战。

---

## 1. 字节码分析与操作

### 1.1 字节码基础

Java字节码是Java源代码经过编译后产生的中间代码，它是一种平台无关的指令集合，由JVM解释执行或编译为本地代码。

#### 字节码文件结构
```
┌─────────────────┐
│ Magic Number    │  4字节  (0xCAFEBABE)
├─────────────────┤
│ Minor Version   │  2字节
├─────────────────┤
│ Major Version   │  2字节
├─────────────────┤
│ Constant Pool   │  变长
├─────────────────┤
│ Access Flags    │  2字节
├─────────────────┤
│ This Class      │  2字节
├─────────────────┤
│ Super Class     │  2字节
├─────────────────┤
│ Interfaces      │  变长
├─────────────────┤
│ Fields          │  变长
├─────────────────┤
│ Methods         │  变长
├─────────────────┤
│ Attributes      │  变长
└─────────────────┘
```

#### 操作数栈和局部变量表
- **操作数栈**：用于存放操作数和计算结果
- **局部变量表**：存放方法参数和局部变量
- **栈帧**：每个方法调用都有独立的栈帧
- **程序计数器**：指向当前执行的字节码指令

### 1.2 常见字节码指令

#### 局部变量操作指令
- **Load指令**：从局部变量表加载到操作数栈
  - `iload_0`, `iload_1`, `iload_2`, `iload_3` (int)
  - `aload_0`, `aload_1`, `aload_2`, `aload_3` (reference)
- **Store指令**：从操作数栈存储到局部变量表
  - `istore`, `astore`

#### 算术指令
- 加法：`iadd`, `ladd`, `fadd`, `dadd`
- 减法：`isub`, `lsub`, `fsub`, `dsub`
- 乘法：`imul`, `lmul`, `fmul`, `dmul`
- 除法：`idiv`, `ldiv`, `fdiv`, `ddiv`

#### 类型转换指令
- 宽化转换：`i2l`, `i2f`, `i2d`, `l2f`, `l2d`, `f2d`
- 窄化转换：`i2b`, `i2c`, `i2s`, `l2i`, `f2i`, `f2l`, `d2i`, `d2l`, `d2f`

### 1.3 方法调用字节码

#### 方法调用指令
- `invokestatic`：调用静态方法
- `invokespecial`：调用构造方法、私有方法、父类方法
- `invokevirtual`：调用实例方法（动态分派）
- `invokeinterface`：调用接口方法
- `invokedynamic`：调用动态方法（Lambda、方法引用）

#### 方法调用过程
1. 参数压入操作栈
2. 执行invoke指令
3. 创建新栈帧
4. 参数传递到局部变量表
5. 执行方法体
6. 返回结果到调用者操作栈
7. 恢复调用者栈帧

### 1.4 控制流字节码

#### 条件分支指令
- 条件跳转：`ifeq`, `ifne`, `iflt`, `ifle`, `ifgt`, `ifge`
- 比较跳转：`if_icmpeq`, `if_icmpne`, `if_icmplt`, `if_icmple`, `if_icmpgt`, `if_icmpge`
- 引用比较：`if_acmpeq`, `if_acmpne`
- 空值检查：`ifnull`, `ifnonnull`

#### 循环指令
- 无条件跳转：`goto`

#### switch语句
- `lookupswitch`：稀疏case值
- `tableswitch`：连续case值

### 1.5 字节码操作工具

#### 字节码查看工具
- `javap`：JDK自带反编译工具
  ```
  javap -c ClassName        # 查看字节码
  javap -v ClassName        # 详细信息
  javap -p ClassName        # 包含私有成员
  ```

#### 字节码操作框架
- **ASM**：轻量级字节码操作框架
  - 直接操作字节码，性能高
  - API相对复杂
  - 支持访问者模式
- **Javassist**：高级字节码操作库
  - 提供Java源码级别的API
  - 使用简单，但性能略低
  - 支持运行时修改
- **ByteBuddy**：现代字节码操作库
  - API友好，类型安全
  - 支持Java Agent
  - 广泛应用于框架开发

### 1.6 字节码操作应用场景

- 动态代理实现
- AOP切面编程
- 代码插桩和监控
- 框架和容器开发
- 性能优化和分析
- 代码混淆和加密

---

## 2. Java内存模型(JMM)深度解析

### 2.1 JMM基础概念

Java内存模型定义了程序中各个变量的访问规则，屏蔽各种硬件和操作系统的内存访问差异。

#### JMM抽象结构
```
┌─────────┐  ┌─────────┐  ┌─────────┐
│ Thread1 │  │ Thread2 │  │ Thread3 │
└────┬────┘  └────┬────┘  └────┬────┘
     │            │            │
  ┌──▼────┐   ┌──▼────┐   ┌──▼────┐
  │工作内存│   │工作内存│   │工作内存│
  │(本地) │   │(本地) │   │(本地) │
  └───┬───┘   └───┬───┘   └───┬───┘
      │  save/load  │            │
      └─────────┬───┴────────────┘
      ┌─────────▼─────────────────┐
      │      主内存 (Main Memory)  │
      │   (所有线程共享的变量)     │
      └───────────────────────────┘
```

#### 内存交互操作 (8种原子操作)
- `lock` (锁定)：锁定主内存变量
- `unlock` (解锁)：解锁主内存变量
- `read` (读取)：从主内存读取
- `load` (载入)：载入到工作内存
- `use` (使用)：传递给执行引擎
- `assign` (赋值)：执行引擎赋值到工作内存
- `store` (存储)：传送到主内存
- `write` (写入)：写入主内存

### 2.2 JMM三大特性

#### 1. 原子性 (Atomicity)
同一时刻只有一个线程执行，保证操作的完整性。

#### 2. 可见性 (Visibility)
一个线程修改了变量，其他线程能够立即看到修改。

#### 3. 有序性 (Ordering)
程序执行的顺序按照代码的先后顺序执行。

### 2.3 volatile关键字

#### volatile的两大特性
1. **保证可见性**
   - 写入立即刷新到主内存
   - 读取从主内存读取最新值

2. **禁止指令重排序**
   - 插入内存屏障
   - 保证有序性

#### volatile不保证原子性
```
volatile int count = 0;
count++;  // 非原子操作!
分解为:
  1. 读取count
  2. 加1
  3. 写回count
```

### 2.4 happens-before原则

happens-before是JMM中定义的两个操作之间的偏序关系，如果A happens-before B，则A的结果对B可见。

#### 8条happens-before规则

1. **程序次序规则 (Program Order Rule)**
   - 单线程内，按代码顺序执行
   - `int a = 1; int b = a + 1;`
   - a的赋值 happens-before b的赋值

2. **监视器锁规则 (Monitor Lock Rule)**
   - unlock happens-before 后续的lock
   - 解锁后的修改对下次加锁可见

3. **volatile变量规则 (Volatile Variable Rule)**
   - volatile写 happens-before volatile读
   - 写入的值对后续读取立即可见

4. **线程启动规则 (Thread Start Rule)**
   - Thread.start() happens-before 线程中的任何操作

5. **线程终止规则 (Thread Termination Rule)**
   - 线程中的操作 happens-before Thread.join()返回

6. **线程中断规则 (Thread Interruption Rule)**
   - interrupt() happens-before 检测到中断

7. **对象终结规则 (Finalizer Rule)**
   - 构造函数结束 happens-before finalize()方法

8. **传递性 (Transitivity)**
   - A happens-before B, B happens-before C
   - 则 A happens-before C

#### 应用示例
```
private int value;
private volatile boolean initialized = false;

// Thread 1
value = 42;              // (1)
initialized = true;      // (2) volatile写

// Thread 2
if (initialized) {       // (3) volatile读
  int x = value;         // (4) 一定能看到42!
}
```

分析:
- (1) happens-before (2) - 程序次序规则
- (2) happens-before (3) - volatile规则
- (3) happens-before (4) - 程序次序规则
- 根据传递性: (1) happens-before (4)

### 2.5 synchronized原理

#### synchronized的三大特性
1. **原子性**：同一时刻只有一个线程执行
2. **可见性**：解锁前的修改对后续加锁可见
3. **有序性**：在锁内部禁止重排序到锁外

#### synchronized的底层实现
- **方法级synchronized**：ACC_SYNCHRONIZED标志，JVM隐式调用monitorenter/monitorexit
- **代码块synchronized**：monitorenter指令 - 进入同步块，monitorexit指令 - 退出同步块

#### Monitor对象结构
```
┌───────────────────┐
│ Owner (持有线程)  │
├───────────────────┤
│ EntryList (等待队列)│
├───────────────────┤
│ WaitSet (等待集合) │
└───────────────────┘
```

#### 锁优化技术
1. **偏向锁 (Biased Locking)**
   - 适用于单线程场景
   - 减少轻量级锁的开销

2. **轻量级锁 (Lightweight Locking)**
   - 适用于竞争较少的场景
   - 使用CAS操作替代互斥量

3. **重量级锁 (Heavyweight Locking)**
   - 竞争激烈时使用
   - 调用操作系统互斥量

4. **自适应自旋 (Adaptive Spinning)**
   - 获取锁失败时自旋等待
   - 避免线程切换开销

5. **锁消除 (Lock Elimination)**
   - JIT编译器优化
   - 消除不可能竞争的锁

6. **锁粗化 (Lock Coarsening)**
   - 合并连续的加锁解锁
   - 减少加锁次数

### 2.6 指令重排序

#### 什么是重排序？
编译器和处理器为优化性能而改变指令执行顺序。

#### 三种重排序
1. **编译器优化重排序**
   - 编译器在不改变单线程语义前提下重排

2. **指令级并行重排序**
   - CPU将指令并行执行

3. **内存系统重排序**
   - 处理器使用缓存和读写缓冲区

#### 经典示例
```
// 初始: a = 0, b = 0, x = 0, y = 0

Thread 1          Thread 2
--------          --------
x = 1;            y = 1;
int r1 = y;       int r2 = x;

可能的结果:
  • r1 = 0, r2 = 1
  • r1 = 1, r2 = 0
  • r1 = 1, r2 = 1
  • r1 = 0, r2 = 0  ← 重排序导致!
```

#### 如何禁止重排序？
- 使用volatile
- 使用synchronized
- 使用Lock
- 使用final (部分禁止)

### 2.7 final语义

#### final的内存语义
1. final字段的写入，happens-before 构造函数return
2. 初次读取包含final字段的对象引用，happens-before 读取final字段

#### final的重排序规则
1. 禁止final字段写入重排序到构造函数外
2. 禁止初次读对象引用重排序到读final字段之后

#### 示例
```
class FinalExample {
  private final int x;
  private int y;
  private static FinalExample obj;

  public FinalExample() {
    x = 3;        // final字段写入
    y = 4;        // 普通字段写入
  }             // 构造函数返回

  public static void writer() {
    obj = new FinalExample();
  }

  public static void reader() {
    FinalExample o = obj;
    int a = o.x;  // 一定能看到3
    int b = o.y;  // 可能看不到4
  }
}
```

#### final的好处
- 保证对象发布的安全性
- 不可变对象天然线程安全
- JIT可以进行优化(常量折叠等)

### 2.8 内存屏障

#### 什么是内存屏障？
一种CPU指令，禁止指令重排序，强制刷新缓存/缓冲区。

#### 四种内存屏障

1. **LoadLoad Barrier**
   ```
   Load1; LoadLoad; Load2
   确保Load1先于Load2执行
   ```

2. **StoreStore Barrier**
   ```
   Store1; StoreStore; Store2
   确保Store1先于Store2，且Store1刷新到内存
   ```

3. **LoadStore Barrier**
   ```
   Load1; LoadStore; Store2
   确保Load1先于Store2
   ```

4. **StoreLoad Barrier**
   ```
   Store1; StoreLoad; Load2
   确保Store1刷新到内存后再执行Load2
   开销最大的屏障
   ```

#### volatile的内存屏障插入策略
- **volatile写操作**:
  ```
  StoreStore
  volatile写
  StoreLoad
  ```

- **volatile读操作**:
  ```
  volatile读
  LoadLoad
  LoadStore
  ```

#### 内存屏障的作用
- 禁止特定类型的处理器重排序
- 强制刷新处理器缓存
- 保证内存可见性

---

## 3. 垃圾收集器深入分析

### 3.1 基础GC算法原理

#### 1. 标记-清除算法 (Mark-Sweep)
- **原理**：
  - 标记阶段：标记所有需要回收的对象
  - 清除阶段：清除被标记的对象
- **优点**：实现简单，不需要移动对象
- **缺点**：产生内存碎片，效率不高
- **适用**：CMS收集器的老年代收集

#### 2. 标记-复制算法 (Mark-Copy)
- **原理**：
  - 将内存分为两块
  - 标记存活对象
  - 将存活对象复制到另一块内存
  - 清空原内存区域
- **优点**：没有内存碎片，实现简单
- **缺点**：内存使用率只有50%
- **适用**：年轻代收集(存活率低)

#### 3. 标记-整理算法 (Mark-Compact)
- **原理**：
  - 标记阶段：标记所有存活对象
  - 整理阶段：将存活对象移动到内存一端
  - 清除阶段：清除边界外的内存
- **优点**：没有内存碎片，内存使用率高
- **缺点**：移动对象成本高
- **适用**：老年代收集(存活率高)

#### 4. 分代收集算法 (Generational Collection)
- **原理**：
  - 根据对象生命周期特点分代管理
  - 年轻代：使用复制算法
  - 老年代：使用标记-清除或标记-整理
- **优点**：针对性强，效率高
- **缺点**：实现复杂
- **适用**：大多数应用场景

### 3.2 分代收集器对比

#### 1. Serial GC (串行收集器)
- **特点**：
  - 单线程收集
  - 收集时暂停所有用户线程(STW)
  - 年轻代使用复制算法
  - 老年代使用标记-整理算法
- **适用场景**：单核CPU、小应用、客户端应用
- **JVM参数**：`-XX:+UseSerialGC`
- **优缺点**：简单高效(单线程)，但STW时间长

#### 2. ParNew GC (并行收集器)
- **特点**：
  - Serial的多线程版本
  - 只收集年轻代
  - 可与CMS配合使用
  - 多线程并行收集
- **适用场景**：多核CPU、与CMS配合
- **JVM参数**：`-XX:+UseParNewGC`
- **优缺点**：多线程提高效率，但仍有STW

#### 3. Parallel GC (并行收集器)
- **特点**：
  - 多线程收集年轻代和老年代
  - 关注吞吐量优先
  - 可控制最大暂停时间和吞吐量
  - 自适应调节策略
- **适用场景**：后台计算、批处理应用
- **JVM参数**：
  - `-XX:+UseParallelGC`
  - `-XX:MaxGCPauseMillis=200`
  - `-XX:GCTimeRatio=99`
- **优缺点**：高吞吐量，但暂停时间不可控

#### 4. CMS GC (并发标记清除)
- **特点**：
  - 并发收集，低延迟
  - 标记-清除算法
  - 四个阶段：初始标记→并发标记→重新标记→并发清除
  - 大部分时间与用户线程并发执行
- **适用场景**：低延迟要求的应用
- **JVM参数**：`-XX:+UseConcMarkSweepGC`
- **优缺点**：
  - 优点：低延迟，并发收集
  - 缺点：内存碎片，CPU敏感，浮动垃圾
  - 状态：JDK14后已废弃

#### 5. G1 GC (垃圾优先收集器)
- **特点**：
  - 面向低延迟的收集器
  - 将堆分为多个Region
  - 可预测的停顿时间
  - 并发和并行收集
- **核心概念**：
  - Region：堆被分为若干个固定大小的区域
  - Remember Set：记录跨Region的引用
  - Collection Set：本次GC收集的Region集合
- **收集过程**：
  - 年轻代收集(Evacuation Pause)
  - 并发标记周期(Concurrent Marking)
  - 混合收集(Mixed Collection)
- **适用场景**：大堆内存、低延迟要求
- **JVM参数**：
  - `-XX:+UseG1GC`
  - `-XX:MaxGCPauseMillis=200`
  - `-XX:G1HeapRegionSize=16m`

### 3.3 现代低延迟收集器

#### 1. ZGC (Z Garbage Collector)
- **特点**：
  - 超低延迟(< 10ms)
  - 支持TB级别堆内存
  - 并发收集，STW时间固定
  - 使用彩色指针和读屏障技术
- **核心技术**：
  - 彩色指针(Colored Pointers)：在指针中存储元数据
  - 读屏障(Load Barriers)：在读取引用时进行检查
  - 并发重定位：对象移动与应用并发
- **适用场景**：大堆内存、极低延迟要求
- **JVM参数**：
  - `-XX:+UnlockExperimentalVMOptions`
  - `-XX:+UseZGC`
- **状态**：JDK15开始生产可用

#### 2. Shenandoah GC
- **特点**：
  - 低延迟收集器
  - 并发收集和压缩
  - 内存使用与延迟无关
  - 使用转发指针技术
- **核心技术**：
  - Brooks Pointers：转发指针技术
  - 并发标记和压缩
  - 连接矩阵(Connection Matrix)
- **收集阶段**：
  - 初始标记 → 并发标记 → 最终标记
  - 并发清理 → 并发回收 → 初始更新引用
  - 并发更新引用 → 最终更新引用
- **适用场景**：低延迟要求，堆大小敏感
- **JVM参数**：
  - `-XX:+UnlockExperimentalVMOptions`
  - `-XX:+UseShenandoahGC`
- **状态**：JDK15开始生产可用

#### 3. Epsilon GC
- **特点**：
  - 无操作收集器
  - 不进行任何垃圾回收
  - 分配内存直到耗尽
  - 主要用于测试和基准测试
- **适用场景**：
  - 极短生命周期应用
  - 垃圾回收性能测试
  - 内存分配压力测试
  - 延迟敏感的微服务
- **JVM参数**：
  - `-XX:+UnlockExperimentalVMOptions`
  - `-XX:+UseEpsilonGC`

### 3.4 收集器选择策略

#### 1. 应用特性分析

##### 堆内存大小
- `< 100MB`: Serial GC
- `100MB - 4GB`: Parallel GC 或 G1 GC
- `4GB - 32GB`: G1 GC
- `> 32GB`: ZGC 或 Shenandoah

##### 延迟要求
- 延迟不敏感: Parallel GC (高吞吐量)
- 中等延迟要求: G1 GC
- 低延迟要求(< 100ms): G1 GC
- 极低延迟(< 10ms): ZGC 或 Shenandoah

#### 2. 业务场景推荐

##### Web应用
- 小型Web应用: G1 GC
- 大型Web应用: G1 GC 或 ZGC
- 微服务: G1 GC

##### 批处理应用
- 数据处理: Parallel GC
- 科学计算: Parallel GC
- 大数据分析: Parallel GC 或 G1 GC

##### 实时应用
- 交易系统: ZGC 或 Shenandoah
- 游戏服务器: G1 GC 或 ZGC
- 流媒体: G1 GC

#### 3. 性能调优建议

##### G1 GC调优
- 设置合理的暂停时间目标
- 调整Region大小
- 监控并发线程数
- 关注混合GC的触发条件

##### ZGC调优
- 确保有足够的内存
- 监控分配速率
- 关注并发线程配置
- 避免大量跨代引用

#### 4. 迁移建议

##### 从CMS迁移
- 推荐迁移到G1 GC
- 调整相关JVM参数
- 进行充分的性能测试
- 监控GC行为变化

##### 从Parallel迁移
- 如果延迟敏感，考虑G1
- 如果追求吞吐量，保持Parallel
- 大堆应用考虑ZGC
- 逐步调优和验证

---

## 4. JVM性能调优实战

### 4.1 内存分配优化

#### 1. 对象池 vs 频繁创建
- **对象池**：复用对象，减少GC压力
- **频繁创建**：可能导致频繁GC

#### 2. StringBuilder vs String拼接
- **String拼接**：每次创建新对象，效率低
- **StringBuilder**：内部扩容，效率高

### 4.2 GC调优实践

#### 1. 分代垃圾回收优化
- 合理设置年轻代和老年代比例
- 调整Survivor区大小
- 设置大对象阈值

#### 2. GC调优建议
- 减少短期对象的创建
- 合理设置年轻代大小
- 选择合适的垃圾收集器
- 调整GC参数匹配应用特性

### 4.3 对象生命周期优化

#### 1. 对象创建优化
- 延迟初始化：只在需要时创建对象
- 对象复用：使用池化技术

#### 2. 资源管理优化
- 及时关闭资源（try-with-resources）
- 使用软引用和弱引用管理缓存
- 避免在循环中创建大对象
- 合理使用static修饰符

### 4.4 内存泄漏检测和预防

#### 1. 常见内存泄漏场景
- 集合类持有对象引用
- 监听器未移除
- 内部类持有外部类引用
- ThreadLocal未清理
- 数据库连接/IO流未关闭
- 缓存无限增长

#### 2. 内存泄漏检测技巧
- 定期检查堆内存增长趋势
- 使用内存分析工具（MAT、JProfiler）
- 监控长期存活的对象
- 检查集合类的大小变化

#### 3. 预防措施
- 使用try-with-resources自动关闭资源
- 避免在长期存活对象中持有短期对象引用
- 及时清理集合和缓存
- 使用弱引用管理监听器和回调

### 4.5 JIT编译优化

#### 1. 热点方法优化
- 让热点代码充分预热
- 避免在热点方法中使用反射
- 保持方法简单以便内联
- 使用final修饰符帮助优化

#### 2. 方法内联优化
- 小方法更容易被内联
- 避免过大的方法
- 使用final方法便于内联

### 4.6 调优策略总结

#### 1. 内存调优策略
- 合理设置堆大小（-Xms, -Xmx）
- 调整年轻代和老年代比例（-XX:NewRatio）
- 优化Survivor区大小（-XX:SurvivorRatio）
- 设置大对象阈值（-XX:PretenureSizeThreshold）

#### 2. GC调优策略
- 选择合适的垃圾收集器
  - Serial GC: 单核CPU，小应用
  - Parallel GC: 多核CPU，高吞吐量
  - G1 GC: 大堆内存，低延迟
  - ZGC/Shenandoah: 超大堆，极低延迟

#### 3. 应用层优化
- 对象池化技术
- 字符串优化（StringBuilder）
- 集合类选择优化
- 缓存策略优化
- 避免内存泄漏

#### 4. 监控和诊断
- 建立监控体系
- 定期分析GC日志
- 使用性能分析工具
- 进行压力测试验证

#### 5. 调优原则
- 一次只调整一个参数
- 充分测试后再应用到生产
- 记录调优过程和效果
- 持续监控和优化

---

## 5. JVM监控与故障排除

### 5.1 常用JVM监控工具

#### 命令行工具
- `jps`：查看Java进程
- `jstat`：GC统计
- `jmap`：Dump堆、查看堆信息
- `jstack`：Dump线程栈
- `jinfo`：查看/修改JVM参数
- `jcmd`：多功能诊断

#### 可视化工具
- JConsole：JDK自带监控
- VisualVM：功能强大的Profile
- JProfiler：商业Profile工具
- MAT：内存分析工具
- GCViewer：GC日志分析
- Arthas：阿里开源诊断工具

### 5.2 OOM问题排查

#### 1. 查看堆信息
```
jmap -heap <pid>
```

#### 2. Dump堆快照
```
jmap -dump:format=b,file=heap.hprof <pid>
```

#### 3. MAT分析
- 打开heap.hprof，生成Leak Suspects报告

#### 4. 查找大对象
- Histogram → 按大小排序

### 5.3 CPU飙高排查

#### 1. 找到Java进程
```
top -c
```

#### 2. 找到高CPU线程
```
top -Hp <pid>
```

#### 3. 转换线程ID为16进制
```
printf '%x\n' <tid>
```

#### 4. 查看线程栈
```
jstack <pid> | grep -A 50 <hex_tid>
```

### 5.4 频繁GC排查

#### 1. 查看GC统计
```
jstat -gcutil <pid> 1000
```

#### 2. 分析GC日志
```
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps
```

#### 3. GC可视化分析
使用GCViewer分析日志

### 5.5 内存泄漏排查

#### 1. 获取两次堆快照
```
jmap -dump:format=b,file=heap1.hprof <pid>
# 运行一段时间
jmap -dump:format=b,file=heap2.hprof <pid>
```

#### 2. MAT对比分析
- 打开heap2.hprof
- 与heap1.hprof对比
- 分析引用链
- Path to GC Roots
- 找到阻止回收的引用

### 5.6 死锁检测

#### 1. jstack检测
```
jstack <pid> | grep "deadlock"
```

#### 2. 编程检测
```
ThreadMXBean.findDeadlockedThreads()
```

---

## 6. JVM参数配置最佳实践

### 6.1 堆内存配置
```
-Xms4g                      # 初始堆大小
-Xmx4g                      # 最大堆大小
-Xmn2g                      # 新生代大小
-XX:NewRatio=2              # 老年代/新生代比例
-XX:SurvivorRatio=8         # Eden/Survivor比例
```

### 6.2 GC选择
```
# G1 GC (推荐)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m

# ZGC (超低延迟)
-XX:+UseZGC

# Parallel GC (高吞吐)
-XX:+UseParallelGC
```

### 6.3 元空间
```
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
```

### 6.4 GC日志
```
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-Xloggc:/var/log/gc.log
```

### 6.5 OOM时Dump
```
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/dumps/
```

### 6.6 完整示例 (4G堆，G1 GC)
```
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

## 7. 学习资源和参考资料

### 7.1 推荐书籍
- 《深入理解Java虚拟机》(周志明)
- 《Java性能权威指南》
- 《HotSpot实战》

### 7.2 官方文档
- [JVM规范](https://docs.oracle.com/javase/specs/jvms/se11/html/)
- [GC调优指南](https://docs.oracle.com/en/java/javase/11/gctuning/)

### 7.3 工具
- [JOL (Java Object Layout)](https://openjdk.java.net/projects/code-tools/jol/)
- [Arthas](https://arthas.aliyun.com/)
- [MAT](https://www.eclipse.org/mat/)

---

## 8. 总结

本文档涵盖了JVM的深度知识，包括：

✅ **字节码分析与操作**：深入理解字节码结构、指令和操作工具
✅ **Java内存模型**：JMM的三大特性、happens-before原则、内存屏障
✅ **垃圾收集器**：各种GC算法和收集器的原理及选择策略
✅ **性能调优**：内存分配、GC调优、JIT优化等实战技巧
✅ **监控与故障排除**：各种工具和排查方法
✅ **参数配置**：最佳实践和完整示例

这些知识对于深入理解JVM原理、优化Java应用性能以及解决生产环境中的问题都至关重要。