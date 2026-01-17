# JVM故障诊断与调优实战指南

## 1. JVM常见问题类型

### 1.1 内存相关问题

#### 1.1.1 OutOfMemoryError (OOM)
- **java.lang.OutOfMemoryError: Java heap space**
  - 原因：堆内存不足
  - 排查步骤：
    1. 使用jstat -gc <pid>观察堆内存使用
    2. 使用jmap -dump生成堆快照
    3. 使用MAT分析堆快照
  - 解决方案：
    • 增加堆内存：-Xmx4g
    • 优化代码，减少内存使用
    • 检查是否存在内存泄漏

- **java.lang.OutOfMemoryError: Metaspace**
  - 原因：元空间不足
  - 排查步骤：
    1. 查看元空间：jstat -gc <pid>
    2. 查看类加载：jstat -class <pid>
    3. Dump类信息：jcmd <pid> GC.class_histogram
  - 解决方案：
    • 增加元空间：-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m
    • 检查动态代理使用
    • 启用类卸载

- **java.lang.OutOfMemoryError: Direct buffer memory**
  - 原因：DirectByteBuffer分配过多，NIO使用不当
  - 排查步骤：
    1. 查看直接内存：jconsole查看BufferPool
    2. 检查NIO代码
  - 解决方案：
    • 增加直接内存：-XX:MaxDirectMemorySize=512m
    • 及时释放DirectByteBuffer

- **java.lang.OutOfMemoryError: unable to create new native thread**
  - 原因：系统线程数达到上限
  - 排查步骤：
    1. 检查系统线程限制：ulimit -u
    2. 查看当前线程数：jstack <pid> | grep -c "java.lang.Thread.State"
  - 解决方案：
    • 增加系统线程限制
    • 优化线程池配置
    • 检查线程泄露

#### 1.1.2 内存泄漏
- **常见场景**：
  - 静态集合持有对象引用
  - 监听器未移除
  - 内部类持有外部类引用
  - ThreadLocal未清理
  - 数据库连接/IO流未关闭
  - 缓存无限增长

- **排查步骤**：
  1. 监控内存趋势
     jstat -gcutil <pid> 1000 100
     观察Old区是否持续增长
  2. 获取两次堆快照
     jmap -dump:format=b,file=heap1.hprof <pid>
     (运行一段时间后)
     jmap -dump:format=b,file=heap2.hprof <pid>
  3. MAT对比分析
     • 打开heap1.hprof,生成Leak Suspects报告
     • 打开heap2.hprof,对比差异
     • 查找持续增长的对象
  4. 分析对象引用链
     • Path to GC Roots
     • 找到阻止对象被回收的引用

### 1.2 GC相关问题

#### 1.2.1 GC频率过高
- **Young GC频繁**
  - 现象：Minor GC每秒多次
  - 原因：
    • 新生代太小
    • 短生命周期对象过多
  - 排查：jstat -gc <pid> 1000，观察YGC频率和Eden区使用
  - 解决：
    • 增大新生代：-Xmn2g
    • 调整Eden/Survivor比例：-XX:SurvivorRatio=8

- **Full GC频繁**
  - 现象：Full GC每分钟多次
  - 原因：
    • 老年代空间不足
    • 元空间不足
    • System.gc()调用
  - 排查：jstat -gcutil <pid> 1000，观察Old区使用率
  - 解决：
    • 增大堆大小：-Xmx4g
    • 优化对象生命周期
    • 禁用System.gc()：-XX:+DisableExplicitGC

#### 1.2.2 GC时间过长
- **现象**：单次GC耗时超过1秒
- **原因**：
  • 堆过大
  • 对象过多
  • GC算法不合适
- **排查**：
  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
  分析GC日志
- **解决**：
  • 使用G1/ZGC：-XX:+UseG1GC
  • 调整GC线程数：-XX:ParallelGCThreads=8

### 1.3 性能问题

#### 1.3.1 响应时间变慢
- **可能原因**：
  • 频繁GC导致STW
  • 数据库慢查询
  • 网络延迟
  • 锁竞争
  • 线程池满

- **诊断步骤**：
  1. 查看GC情况
     jstat -gcutil <pid> 1000
     关注FGCT(Full GC总耗时)
  2. 查看线程状态
     jstack <pid>
     统计BLOCKED、WAITING线程数
  3. 分析慢方法
     使用JProfiler/Arthas profiler
     找出耗时最长的方法

#### 1.3.2 CPU使用率过高
- **排查步骤**：
  1. 找到Java进程：top -c
  2. 找到高CPU线程：top -Hp <pid>
  3. 转换线程ID为16进制：printf '%x\n' <tid>
  4. 查看线程栈：jstack <pid> | grep -A 50 <hex_tid>

## 2. JVM监控工具详解

### 2.1 命令行工具

#### 2.1.1 jps (Java Process Status)
- **功能**：列出正在运行的Java进程
- **常用参数**：
  -l：输出主类全名
  -v：输出JVM参数
  -m：输出main方法参数

#### 2.1.2 jstat (JVM Statistics Monitoring Tool)
- **功能**：监控JVM统计信息
- **常用命令**：
  - jstat -gc <pid> 1000：每秒输出GC统计
  - jstat -gccapacity <pid>：输出GC容量信息
  - jstat -gcutil <pid>：输出GC利用率
  - jstat -class <pid>：输出类加载统计

#### 2.1.3 jmap (JVM Memory Map)
- **功能**：生成堆快照
- **常用命令**：
  - jmap -dump:format=b,file=heap.hprof <pid>：生成堆快照
  - jmap -heap <pid>：输出堆详细信息
  - jmap -histo <pid>：输出对象统计

#### 2.1.4 jstack (JVM Stack Trace)
- **功能**：输出线程栈信息
- **常用命令**：
  - jstack <pid>：输出线程栈
  - jstack -l <pid>：输出锁信息

#### 2.1.5 jinfo (JVM Configuration Info)
- **功能**：查看和修改JVM参数
- **常用命令**：
  - jinfo -flag <name> <pid>：查看参数值
  - jinfo -flag +<name> <pid>：启用参数
  - jinfo -flag -<name> <pid>：禁用参数

#### 2.1.6 jcmd (JVM Command)
- **功能**：发送诊断命令到JVM
- **常用命令**：
  - jcmd <pid> help：查看支持的命令
  - jcmd <pid> GC.run：强制执行GC
  - jcmd <pid> VM.flags：查看JVM参数

### 2.2 可视化工具

#### 2.2.1 JConsole
- **功能**：JDK自带的监控工具
- **特点**：实时监控内存、线程、类加载等信息

#### 2.2.2 VisualVM
- **功能**：JDK自带的性能分析工具
- **特点**：提供内存分析、CPU分析、线程分析等功能

#### 2.2.3 MAT (Memory Analyzer Tool)
- **功能**：专业的内存分析工具
- **特点**：分析堆快照，查找内存泄漏

#### 2.2.4 JProfiler
- **功能**：商业性能分析工具
- **特点**：全面的性能分析功能

#### 2.2.5 Arthas
- **功能**：阿里巴巴开源的诊断工具
- **特点**：在线诊断，无需重启应用

## 3. 实际案例分析

### 3.1 案例1：堆内存不足导致的OOM

**问题描述**：应用运行一段时间后出现OutOfMemoryError: Java heap space

**分析过程**：
1. 使用jstat -gc <pid>观察内存使用情况
   S0     S1     E      O      M     CCS    YGC  YGCT   FGC  FGCT   GCT
   0.00  99.99  45.50  98.50  95.2  90.5   1500  15.2   300  50.5  65.7
   发现：Old区持续98%，频繁Full GC

2. 使用jmap -dump生成堆快照进行分析
3. 发现大量订单缓存未释放

**解决方案**：
1. 增大堆大小：-Xmx4g (原2g)
2. 优化缓存策略，添加过期时间
3. 使用G1 GC：-XX:+UseG1GC

### 3.2 案例2：频繁Full GC

**问题描述**：订单系统每分钟Full GC 5次

**分析过程**：
1. 使用jstat -gcutil <pid> 1000监控
2. 发现老年代使用率持续高位
3. 检查代码发现大量大对象创建

**解决方案**：
1. 调整年轻代大小：-XX:NewRatio=1
2. 优化对象创建逻辑
3. 使用合适的GC：-XX:+UseG1GC

### 3.3 案例3：内存泄漏排查

**什么是内存泄漏**：
- 不再使用的对象无法被GC回收
- 导致内存持续增长
- 最终引发OOM

**排查步骤**：
1. 监控内存趋势：jstat -gcutil <pid> 1000 100
2. 获取两次堆快照：
   jmap -dump:format=b,file=heap1.hprof <pid>
   (运行一段时间后)
   jmap -dump:format=b,file=heap2.hprof <pid>
3. MAT对比分析：
   • 打开heap1.hprof,生成Leak Suspects报告
   • 打开heap2.hprof,对比差异
   • 查找持续增长的对象
4. 分析对象引用链：
   • Path to GC Roots
   • 找到阻止对象被回收的引用

**实战示例**：
- 问题：应用运行几天后OOM
- 现象：Old区持续增长
- 发现：静态Map持有大量对象引用未清理

### 3.4 案例4：CPU使用率过高

**问题描述**：应用CPU使用率持续90%+

**分析过程**：
1. 使用top -Hp <pid>找出高CPU线程
2. 将线程ID转换为16进制：printf '%x' <tid>
3. 使用jstack <pid> | grep -A 20 <hex_tid>查看线程栈
4. 发现某个线程在无限循环中

**解决方案**：
1. 修复代码中的无限循环
2. 添加适当的休眠时间
3. 优化算法逻辑

## 4. JVM参数调优建议

### 4.1 堆内存配置
```
-Xms4g -Xmx4g           # 初始和最大堆相同，避免扩容
-Xmn2g                  # 新生代1/2到1/3堆大小
-XX:SurvivorRatio=8     # Eden:Survivor = 8:1
```

### 4.2 GC选择
- **响应时间优先(Web应用)**：
  ```
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:G1HeapRegionSize=16m
  ```

- **吞吐量优先(批处理)**：
  ```
  -XX:+UseParallelGC
  -XX:ParallelGCThreads=8
  -XX:GCTimeRatio=99
  ```

- **超大堆/低延迟**：
  ```
  -XX:+UseZGC
  -XX:ZCollectionInterval=120
  ```

### 4.3 GC日志
```
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-Xloggc:/path/to/gc.log
-XX:+UseGCLogFileRotation
```

## 5. 监控和告警策略

### 5.1 关键指标监控
- 堆使用率 > 80%
- Full GC频率 > 1次/分钟
- GC耗时 > 1秒
- 线程数异常增长
- Metaspace使用率 > 80%

### 5.2 监控工具链
- **基础监控**：Prometheus + Grafana
- **APM系统**：SkyWalking, Pinpoint
- **日志分析**：ELK Stack
- **告警系统**：AlertManager

## 6. 预防措施和最佳实践

### 6.1 代码层面
- 避免创建不必要的对象
- 及时关闭资源（try-with-resources）
- 合理使用缓存，设置过期时间
- 使用对象池复用对象
- 避免大对象创建

### 6.2 架构层面
- 使用微服务拆分，降低单个应用内存压力
- 实施缓存分层策略
- 使用异步处理，减少同步等待
- 实现优雅降级和熔断机制

### 6.3 运维层面
- 建立完善的监控体系
- 定期进行压力测试
- 制定应急响应预案
- 定期review GC日志

## 7. 常用诊断命令汇总

### 7.1 在线监控
```
jstat -gcutil <pid> 1000          # 每秒查看GC情况
jstack <pid> > thread.txt         # 导出线程栈
jmap -clstats <pid>               # 查看类加载统计
jinfo -flags <pid>                # 查看JVM参数
jcmd <pid> GC.run                 # 强制Full GC
jcmd <pid> GC.class_histogram     # 查看类统计
```

### 7.2 离线分析
```
jmap -dump:live,format=b,file=heap.hprof <pid>  # Dump堆快照
java -jar mat.jar heap.hprof                    # 使用MAT分析
```

## 8. 总结

JVM故障诊断是一个系统性工程，需要：
1. 掌握各种监控工具的使用
2. 理解JVM内部机制
3. 积累丰富的实践经验
4. 建立完善的监控体系

通过本文档的学习，你应该能够：
- 快速定位JVM相关问题
- 使用合适的工具进行诊断
- 制定有效的解决方案
- 建立预防措施避免问题重现