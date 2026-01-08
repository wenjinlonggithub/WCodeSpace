package com.architecture.jvm.classloader;

/**
 * 类加载过程详解
 * 
 * 详细分析类加载的完整生命周期
 */
public class ClassLoadingProcess {
    
    public static void main(String[] args) {
        ClassLoadingProcess process = new ClassLoadingProcess();
        
        System.out.println("=== 类加载过程详解 ===\n");
        
        // 1. 类加载的时机
        process.explainLoadingTiming();
        
        // 2. 类加载的过程
        process.explainLoadingStages();
        
        // 3. 类初始化详解
        process.explainInitialization();
        
        // 4. 类卸载机制
        process.explainClassUnloading();
        
        // 5. 演示类加载过程
        process.demonstrateLoadingProcess();
    }
    
    /**
     * 解释类加载的时机
     */
    private void explainLoadingTiming() {
        System.out.println("=== 类加载时机 ===\n");
        
        System.out.println("1. 主动引用（会触发类初始化）");
        System.out.println("   ├── new实例化对象");
        System.out.println("   ├── 访问或设置静态变量（被final修饰的编译期常量除外）");
        System.out.println("   ├── 调用静态方法");
        System.out.println("   ├── 使用java.lang.reflect包对类进行反射调用");
        System.out.println("   ├── 初始化子类时，发现父类还没初始化，先初始化父类");
        System.out.println("   └── JVM启动时指定的主类（包含main方法的类）");
        System.out.println();
        
        System.out.println("2. 被动引用（不会触发类初始化）");
        System.out.println("   ├── 通过子类引用父类静态字段");
        System.out.println("   ├── 定义对象数组");
        System.out.println("   ├── 引用编译期常量");
        System.out.println("   └── 通过类名获取Class对象");
        System.out.println();
        
        System.out.println("3. 类加载器加载时机");
        System.out.println("   ├── 启动时加载：Bootstrap ClassLoader加载核心库");
        System.out.println("   ├── 扩展时加载：Extension ClassLoader加载扩展库");
        System.out.println("   ├── 应用时加载：Application ClassLoader加载应用类");
        System.out.println("   └── 运行时加载：动态加载需要的类");
        System.out.println();
    }
    
    /**
     * 解释类加载的各个阶段
     */
    private void explainLoadingStages() {
        System.out.println("=== 类加载过程的七个阶段 ===\n");
        
        System.out.println("1. 加载（Loading）");
        System.out.println("   作用：");
        System.out.println("   ├── 通过类的全限定名获取二进制字节流");
        System.out.println("   ├── 将字节流代表的静态存储结构转化为方法区的运行时数据结构");
        System.out.println("   └── 在内存中生成代表这个类的java.lang.Class对象");
        System.out.println("   特点：");
        System.out.println("   ├── 可以从文件、网络、数据库等任何地方获取");
        System.out.println("   ├── 数组类没有对应的二进制字节流，由JVM直接生成");
        System.out.println("   └── 加载阶段与连接阶段可能交叉进行");
        System.out.println();
        
        System.out.println("2. 验证（Verification）");
        System.out.println("   目的：确保Class文件的字节流符合JVM规范");
        System.out.println("   验证内容：");
        System.out.println("   ├── 文件格式验证：");
        System.out.println("   │   ├── 魔数验证（0xCAFEBABE）");
        System.out.println("   │   ├── 版本号验证");
        System.out.println("   │   ├── 常量池验证");
        System.out.println("   │   └── 其他格式验证");
        System.out.println("   ├── 元数据验证：");
        System.out.println("   │   ├── 语义分析");
        System.out.println("   │   ├── 继承关系验证");
        System.out.println("   │   ├── 抽象类验证");
        System.out.println("   │   └── 接口实现验证");
        System.out.println("   ├── 字节码验证：");
        System.out.println("   │   ├── 数据流分析");
        System.out.println("   │   ├── 控制流分析");
        System.out.println("   │   ├── 类型推导验证");
        System.out.println("   │   └── 栈映射帧验证");
        System.out.println("   └── 符号引用验证：");
        System.out.println("       ├── 符号引用能否找到对应的类");
        System.out.println("       ├── 访问性验证");
        System.out.println("       └── 方法的字段存在性验证");
        System.out.println();
        
        System.out.println("3. 准备（Preparation）");
        System.out.println("   作用：为类的静态变量分配内存并设置默认初始值");
        System.out.println("   要点：");
        System.out.println("   ├── 仅针对类变量（static变量），不包括实例变量");
        System.out.println("   ├── 分配的是默认零值，不是代码中的初始值");
        System.out.println("   ├── final static常量会在准备阶段直接赋值");
        System.out.println("   └── 内存分配在方法区中");
        System.out.println("   默认值示例：");
        System.out.println("   ├── int/short/byte/char -> 0");
        System.out.println("   ├── long -> 0L");
        System.out.println("   ├── float -> 0.0f");
        System.out.println("   ├── double -> 0.0d");
        System.out.println("   ├── boolean -> false");
        System.out.println("   └── reference -> null");
        System.out.println();
        
        System.out.println("4. 解析（Resolution）");
        System.out.println("   作用：将符号引用替换为直接引用");
        System.out.println("   解析类型：");
        System.out.println("   ├── 类或接口的解析");
        System.out.println("   ├── 字段解析");
        System.out.println("   ├── 方法解析");
        System.out.println("   └── 接口方法解析");
        System.out.println("   符号引用：");
        System.out.println("   ├── 以一组符号来描述所引用的目标");
        System.out.println("   ├── 与内存布局无关");
        System.out.println("   └── 存储在常量池中");
        System.out.println("   直接引用：");
        System.out.println("   ├── 直接指向目标的指针");
        System.out.println("   ├── 相对偏移量");
        System.out.println("   └── 能间接定位到目标的句柄");
        System.out.println();
        
        System.out.println("5. 初始化（Initialization）");
        System.out.println("   作用：执行类构造器<clinit>()方法");
        System.out.println("   <clinit>()方法特点：");
        System.out.println("   ├── 由编译器自动收集静态变量赋值和静态代码块合并产生");
        System.out.println("   ├── 执行顺序按照在源文件中出现的顺序");
        System.out.println("   ├── 父类的<clinit>()先于子类执行");
        System.out.println("   ├── JVM保证<clinit>()在多线程环境下正确加锁同步");
        System.out.println("   └── 接口的<clinit>()不需要先执行父接口的<clinit>()");
        System.out.println();
        
        System.out.println("6. 使用（Using）");
        System.out.println("   ├── 创建类实例");
        System.out.println("   ├── 调用类方法");
        System.out.println("   ├── 访问类字段");
        System.out.println("   └── 通过反射操作类");
        System.out.println();
        
        System.out.println("7. 卸载（Unloading）");
        System.out.println("   条件：");
        System.out.println("   ├── 该类所有的实例都已被回收");
        System.out.println("   ├── 加载该类的ClassLoader已被回收");
        System.out.println("   ├── 该类对应的java.lang.Class对象没有被引用");
        System.out.println("   └── 无法通过反射访问该类的方法");
        System.out.println();
    }
    
    /**
     * 解释类初始化详解
     */
    private void explainInitialization() {
        System.out.println("=== 类初始化详解 ===\n");
        
        System.out.println("1. <clinit>()方法的生成");
        System.out.println("   组成部分：");
        System.out.println("   ├── 静态变量的赋值语句");
        System.out.println("   ├── 静态代码块");
        System.out.println("   └── 按源码顺序合并");
        System.out.println();
        System.out.println("   示例：");
        System.out.println("   ```java");
        System.out.println("   static int a = 1;           // 赋值语句");
        System.out.println("   static {                    // 静态代码块");
        System.out.println("       a = 2;");
        System.out.println("   }");
        System.out.println("   static int b = a + 1;       // 依赖前面的赋值");
        System.out.println("   ```");
        System.out.println();
        
        System.out.println("2. 初始化顺序");
        System.out.println("   类层次初始化：");
        System.out.println("   ├── 接口初始化不要求父接口全部完成初始化");
        System.out.println("   ├── 类初始化要求父类必须完成初始化");
        System.out.println("   └── 初始化过程是递归的");
        System.out.println();
        System.out.println("   同一个类内部：");
        System.out.println("   ├── 静态变量和静态代码块按出现顺序执行");
        System.out.println("   ├── 实例变量和实例代码块按出现顺序执行");
        System.out.println("   └── 构造方法最后执行");
        System.out.println();
        
        System.out.println("3. 线程安全");
        System.out.println("   JVM保证：");
        System.out.println("   ├── <clinit>()方法在多线程环境下被正确加锁同步");
        System.out.println("   ├── 同一个类只会被初始化一次");
        System.out.println("   ├── 如果多个线程同时初始化一个类，只有一个线程执行");
        System.out.println("   └── 其他线程会阻塞等待直到初始化完成");
        System.out.println();
        
        System.out.println("4. 初始化异常");
        System.out.println("   异常处理：");
        System.out.println("   ├── 如果<clinit>()方法抛出异常，类初始化失败");
        System.out.println("   ├── 该类被标记为初始化错误状态");
        System.out.println("   ├── 后续使用该类会抛出NoClassDefFoundError");
        System.out.println("   └── 异常会传播给触发初始化的线程");
        System.out.println();
    }
    
    /**
     * 解释类卸载机制
     */
    private void explainClassUnloading() {
        System.out.println("=== 类卸载机制 ===\n");
        
        System.out.println("1. 类卸载条件");
        System.out.println("   必要条件（同时满足）：");
        System.out.println("   ├── 该类的所有实例都已被垃圾回收");
        System.out.println("   ├── 加载该类的ClassLoader已被垃圾回收");
        System.out.println("   ├── 该类对应的Class对象没有被引用");
        System.out.println("   └── 无法通过反射访问该类");
        System.out.println();
        
        System.out.println("2. 不同类加载器的卸载特性");
        System.out.println("   Bootstrap ClassLoader：");
        System.out.println("   ├── 加载的类永不卸载");
        System.out.println("   ├── 生命周期与JVM相同");
        System.out.println("   └── 包括Object、String等核心类");
        System.out.println();
        System.out.println("   Extension ClassLoader：");
        System.out.println("   ├── 加载的类基本不会卸载");
        System.out.println("   ├── 扩展库类通常在整个程序生命周期中使用");
        System.out.println("   └── 除非显式替换类加载器");
        System.out.println();
        System.out.println("   Application ClassLoader：");
        System.out.println("   ├── 应用类可能被卸载");
        System.out.println("   ├── 在某些应用服务器中支持热部署");
        System.out.println("   └── 需要满足卸载条件");
        System.out.println();
        System.out.println("   Custom ClassLoader：");
        System.out.println("   ├── 自定义类加载器加载的类更容易卸载");
        System.out.println("   ├── 常用于插件系统");
        System.out.println("   └── 支持动态加载和卸载");
        System.out.println();
        
        System.out.println("3. 类卸载的应用场景");
        System.out.println("   热部署：");
        System.out.println("   ├── Web应用的热更新");
        System.out.println("   ├── OSGi模块系统");
        System.out.println("   └── 开发环境的快速迭代");
        System.out.println();
        System.out.println("   插件系统：");
        System.out.println("   ├── Eclipse插件机制");
        System.out.println("   ├── IDE的插件管理");
        System.out.println("   └── 模块化架构");
        System.out.println();
        System.out.println("   内存管理：");
        System.out.println("   ├── 释放不再使用的类占用的内存");
        System.out.println("   ├── 防止永久代/元空间内存泄漏");
        System.out.println("   └── 长期运行的应用程序优化");
        System.out.println();
    }
    
    /**
     * 演示类加载过程
     */
    private void demonstrateLoadingProcess() {
        System.out.println("=== 类加载过程演示 ===\n");
        
        System.out.println("演示类加载过程追踪:");
        
        try {
            // 演示主动引用触发初始化
            System.out.println("\n1. 主动引用 - 访问静态变量:");
            int value = LoadingDemo.staticVar;
            System.out.println("获取到静态变量值: " + value);
            
            System.out.println("\n2. 主动引用 - 调用静态方法:");
            LoadingDemo.staticMethod();
            
            System.out.println("\n3. 主动引用 - 创建实例:");
            LoadingDemo instance = new LoadingDemo();
            
            System.out.println("\n4. 被动引用 - 通过子类访问父类静态变量:");
            int parentValue = ChildDemo.parentStaticVar;
            System.out.println("通过子类获取父类静态变量: " + parentValue);
            System.out.println("注意: 子类没有被初始化");
            
            System.out.println("\n5. 被动引用 - 定义数组:");
            LoadingDemo[] array = new LoadingDemo[10];
            System.out.println("创建数组: " + array.getClass().getName());
            System.out.println("注意: LoadingDemo类没有再次初始化");
            
        } catch (Exception e) {
            System.err.println("演示过程出错: " + e.getMessage());
        }
    }
    
    /**
     * 类加载演示类
     */
    static class LoadingDemo {
        // 静态变量
        public static int staticVar;
        public static final String CONSTANT = "编译期常量";
        
        // 实例变量
        private int instanceVar;
        
        // 静态代码块
        static {
            System.out.println("  LoadingDemo静态代码块执行 - 类初始化");
            staticVar = 100;
        }
        
        // 实例代码块
        {
            System.out.println("  LoadingDemo实例代码块执行");
            instanceVar = 200;
        }
        
        // 构造方法
        public LoadingDemo() {
            System.out.println("  LoadingDemo构造方法执行");
        }
        
        // 静态方法
        public static void staticMethod() {
            System.out.println("  LoadingDemo静态方法执行");
        }
    }
    
    /**
     * 父类演示
     */
    static class ParentDemo {
        public static int parentStaticVar = 300;
        
        static {
            System.out.println("  ParentDemo静态代码块执行");
        }
    }
    
    /**
     * 子类演示
     */
    static class ChildDemo extends ParentDemo {
        public static int childStaticVar = 400;
        
        static {
            System.out.println("  ChildDemo静态代码块执行");
        }
    }
}