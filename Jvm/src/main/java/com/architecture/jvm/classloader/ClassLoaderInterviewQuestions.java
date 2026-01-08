package com.architecture.jvm.classloader;

/**
 * 类加载机制面试题汇总
 * 
 * 涵盖类加载过程、双亲委派、自定义类加载器等核心面试问题
 */
public class ClassLoaderInterviewQuestions {

    /**
     * ====================
     * 一、类加载基础问题
     * ====================
     */

    /**
     * Q1: 什么是类加载？类加载的过程包括哪些阶段？
     * 
     * A: 类加载完整过程解析：
     * 
     * 1. 类加载定义
     *    概念：
     *    - JVM将类的字节码文件加载到内存中
     *    - 并将其转换为运行时数据结构
     *    - 在堆中生成对应的Class对象
     *    - 作为访问方法区数据结构的入口
     * 
     * 2. 类的生命周期
     *    完整阶段：
     *    ```
     *    加载 → 验证 → 准备 → 解析 → 初始化 → 使用 → 卸载
     *     Loading  Verification Preparation Resolution Initialization Using Unloading
     *    ```
     * 
     *    前五个阶段统称为"类加载"过程
     * 
     * 3. 各阶段详解
     *    
     *    (1) 加载（Loading）
     *    任务：
     *    - 通过类的全限定名获取二进制字节流
     *    - 将字节流转换为方法区的运行时数据结构
     *    - 在堆中生成Class对象作为数据访问入口
     * 
     *    来源：
     *    - 本地文件系统
     *    - ZIP/JAR文件
     *    - 网络（Applet）
     *    - 数据库存储
     *    - 运行时动态生成（动态代理）
     * 
     *    (2) 验证（Verification）
     *    目的：确保Class文件的字节流符合JVM规范
     * 
     *    验证层次：
     *    ├── 文件格式验证
     *    │   ├── 魔数验证（0xCAFEBABE）
     *    │   ├── 版本号检查
     *    │   └── 常量池验证
     *    ├── 元数据验证
     *    │   ├── 语义分析
     *    │   ├── 继承关系验证
     *    │   └── 抽象类验证
     *    ├── 字节码验证
     *    │   ├── 数据流分析
     *    │   ├── 控制流分析
     *    │   └── 类型推导验证
     *    └── 符号引用验证
     *        ├── 符号引用的存在性验证
     *        └── 访问性验证
     * 
     *    (3) 准备（Preparation）
     *    任务：
     *    - 为类的静态变量分配内存
     *    - 设置变量的默认初始值
     *    - 内存分配在方法区中
     * 
     *    注意事项：
     *    - 仅处理static变量，不包括实例变量
     *    - 设置的是默认零值，不是代码赋值
     *    - final static常量直接赋初值
     * 
     *    (4) 解析（Resolution）
     *    任务：将符号引用替换为直接引用
     * 
     *    解析对象：
     *    - 类或接口解析
     *    - 字段解析
     *    - 类方法解析
     *    - 接口方法解析
     * 
     *    引用类型：
     *    - 符号引用：与内存布局无关的引用
     *    - 直接引用：可直接定位目标的指针或偏移量
     * 
     *    (5) 初始化（Initialization）
     *    任务：执行类构造器<clinit>()方法
     * 
     *    <clinit>()特点：
     *    - 由静态变量赋值和静态代码块合成
     *    - 按源码顺序执行
     *    - 父类<clinit>()先于子类执行
     *    - JVM保证线程安全
     * 
     * 4. 阶段关系
     *    时序关系：
     *    - 加载、验证、准备、初始化严格按顺序开始
     *    - 解析可能在初始化之后（运行时解析）
     *    - 这些阶段通常交叉进行
     * 
     * 5. 类加载时机
     *    主动引用（触发初始化）：
     *    - new实例化对象
     *    - 访问静态变量或静态方法
     *    - 反射调用
     *    - 子类初始化时先初始化父类
     *    - JVM启动时的主类
     * 
     *    被动引用（不触发初始化）：
     *    - 通过数组定义引用类
     *    - 引用编译时常量
     *    - 通过子类引用父类静态字段
     */
    public void classLoadingProcess() {
        System.out.println("类加载：加载→验证→准备→解析→初始化→使用→卸载");
    }

    /**
     * Q2: 什么是双亲委派模型？为什么要使用双亲委派？
     * 
     * A: 双亲委派模型详解：
     * 
     * 1. 双亲委派模型定义
     *    工作机制：
     *    - 如果一个类加载器收到类加载请求
     *    - 它首先不会自己尝试加载这个类
     *    - 而是把请求委派给父类加载器
     *    - 每一个层次的类加载器都是如此
     *    - 只有当父加载器无法完成加载时，子加载器才尝试自己加载
     * 
     * 2. 类加载器层次结构
     *    ```
     *    Bootstrap ClassLoader (启动类加载器)
     *            ↑
     *    Extension ClassLoader (扩展类加载器)  
     *            ↑
     *    Application ClassLoader (应用类加载器)
     *            ↑
     *    Custom ClassLoader (自定义类加载器)
     *    ```
     * 
     * 3. 各层加载器职责
     *    
     *    (1) Bootstrap ClassLoader（启动类加载器）
     *    - 实现：C++实现，是JVM的一部分
     *    - 职责：加载<JAVA_HOME>/lib目录中的核心类库
     *    - 范围：如java.lang.*、java.util.*等
     *    - 特点：在Java中表现为null
     * 
     *    (2) Extension ClassLoader（扩展类加载器）
     *    - 实现：Java实现，sun.misc.Launcher$ExtClassLoader
     *    - 职责：加载<JAVA_HOME>/lib/ext目录中的类库
     *    - 范围：扩展类库和第三方库
     *    - 特点：开发者可以直接使用
     * 
     *    (3) Application ClassLoader（应用类加载器）
     *    - 实现：Java实现，sun.misc.Launcher$AppClassLoader
     *    - 职责：加载classpath上指定的类库
     *    - 范围：用户自己的应用程序类
     *    - 特点：程序默认的类加载器
     * 
     * 4. 双亲委派实现
     *    核心代码逻辑：
     *    ```java
     *    protected synchronized Class<?> loadClass(String name, boolean resolve) 
     *        throws ClassNotFoundException {
     *        
     *        // 1. 检查类是否已经加载过
     *        Class c = findLoadedClass(name);
     *        if (c == null) {
     *            try {
     *                // 2. 委派给父加载器
     *                if (parent != null) {
     *                    c = parent.loadClass(name, false);
     *                } else {
     *                    // 3. 父加载器为null，委派给Bootstrap ClassLoader
     *                    c = findBootstrapClassOrNull(name);
     *                }
     *            } catch (ClassNotFoundException e) {
     *                // 4. 父加载器无法加载，自己尝试加载
     *                c = findClass(name);
     *            }
     *        }
     *        
     *        if (resolve) {
     *            resolveClass(c);
     *        }
     *        return c;
     *    }
     *    ```
     * 
     * 5. 双亲委派的好处
     *    
     *    (1) 安全性保证
     *    - 防止核心类库被篡改
     *    - 恶意代码无法替换系统类
     *    - 保证Java核心API的完整性
     * 
     *    示例：
     *    ```java
     *    // 假如用户自定义了java.lang.String类
     *    // 由于双亲委派，这个类永远不会被加载
     *    // 因为Bootstrap ClassLoader会优先加载JDK中的String
     *    ```
     * 
     *    (2) 避免重复加载
     *    - 父加载器已加载的类，子加载器不会再加载
     *    - 保证类在JVM中的唯一性
     *    - 节省内存空间
     * 
     *    (3) 类的一致性
     *    - 同一个类由同一个加载器加载
     *    - 保证类型转换的正确性
     *    - 维护类的层次关系
     * 
     * 6. 破坏双亲委派的场景
     *    
     *    (1) 历史原因
     *    - JDK 1.2之前没有双亲委派模型
     *    - 为了兼容性，loadClass()可以被重写
     * 
     *    (2) SPI机制
     *    - 基础类需要调用用户代码
     *    - 使用线程上下文类加载器
     *    - 例如JDBC、JNDI等
     * 
     *    (3) 热部署需求
     *    - OSGi：模块化热部署框架
     *    - 网状结构的类加载器关系
     *    - 更复杂的类搜索和依赖管理
     * 
     * 7. 线程上下文类加载器
     *    解决SPI问题：
     *    ```java
     *    // 设置线程上下文类加载器
     *    Thread.currentThread().setContextClassLoader(appClassLoader);
     *    
     *    // 在SPI中使用
     *    ClassLoader ccl = Thread.currentThread().getContextClassLoader();
     *    Class<?> serviceClass = ccl.loadClass("com.example.ServiceImpl");
     *    ```
     * 
     * 8. 实际应用
     *    
     *    Web容器中的应用：
     *    - 每个Web应用有独立的WebApp ClassLoader
     *    - 可以实现应用间的类隔离
     *    - 支持热部署和热替换
     * 
     *    模块化系统：
     *    - OSGi Bundle ClassLoader
     *    - 实现模块间的版本控制
     *    - 支持动态安装和卸载
     */
    public void parentDelegationModel() {
        System.out.println("双亲委派：层次结构 + 委派机制 + 安全保证 + 避免重复");
    }

    /**
     * Q3: 如何实现自定义类加载器？有哪些应用场景？
     * 
     * A: 自定义类加载器详解：
     * 
     * 1. 自定义类加载器的必要性
     *    标准类加载器的限制：
     *    - 只能从固定路径加载类
     *    - 无法实现类的热部署
     *    - 不支持加密的类文件
     *    - 无法从网络等特殊来源加载
     * 
     * 2. 实现方式
     *    
     *    (1) 继承ClassLoader抽象类
     *    ```java
     *    public class CustomClassLoader extends ClassLoader {
     *        
     *        // 重写findClass方法（推荐）
     *        @Override
     *        protected Class<?> findClass(String name) throws ClassNotFoundException {
     *            try {
     *                // 1. 获取类文件的字节数组
     *                byte[] classData = getClassData(name);
     *                
     *                // 2. 调用defineClass方法定义类
     *                return defineClass(name, classData, 0, classData.length);
     *                
     *            } catch (IOException e) {
     *                throw new ClassNotFoundException("Cannot load class: " + name, e);
     *            }
     *        }
     *        
     *        // 获取类文件字节码的具体实现
     *        private byte[] getClassData(String className) throws IOException {
     *            // 从文件系统、网络、数据库等加载字节码
     *            // 可以在这里实现解密、解压等操作
     *            String path = className.replace('.', '/') + ".class";
     *            return Files.readAllBytes(Paths.get(classPath, path));
     *        }
     *    }
     *    ```
     * 
     *    (2) 重写loadClass方法（破坏双亲委派）
     *    ```java
     *    @Override
     *    public Class<?> loadClass(String name) throws ClassNotFoundException {
     *        Class<?> clazz = findLoadedClass(name);
     *        
     *        if (clazz == null) {
     *            // 自定义加载逻辑，可以改变委派顺序
     *            if (name.startsWith("com.mycompany.")) {
     *                // 自己的类优先加载
     *                clazz = findClass(name);
     *            } else {
     *                // 系统类委派给父加载器
     *                clazz = super.loadClass(name);
     *            }
     *        }
     *        
     *        return clazz;
     *    }
     *    ```
     * 
     * 3. 实现细节
     *    
     *    (1) defineClass方法
     *    作用：
     *    - 将字节数组转换为Class实例
     *    - 进行字节码验证
     *    - 设置类的保护域
     * 
     *    参数说明：
     *    ```java
     *    protected final Class<?> defineClass(
     *        String name,          // 类的全限定名
     *        byte[] b,            // 类文件的字节数组
     *        int off,             // 字节数组的起始偏移量
     *        int len              // 字节数组的长度
     *    )
     *    ```
     * 
     *    (2) resolveClass方法
     *    - 链接指定的类
     *    - 确保类被正确初始化
     *    - 通常在defineClass后调用
     * 
     *    (3) findLoadedClass方法
     *    - 检查类是否已经被当前类加载器加载过
     *    - 避免重复加载
     * 
     * 4. 应用场景
     *    
     *    (1) 热部署和热替换
     *    Web应用热部署：
     *    ```java
     *    public class HotDeployClassLoader extends ClassLoader {
     *        private Map<String, Long> classModifyTime = new HashMap<>();
     *        
     *        @Override
     *        protected Class<?> findClass(String name) throws ClassNotFoundException {
     *            File classFile = new File(classPath, name.replace('.', '/') + ".class");
     *            
     *            // 检查文件是否被修改
     *            if (isClassModified(name, classFile)) {
     *                // 重新加载类
     *                return reloadClass(name, classFile);
     *            }
     *            
     *            return super.findClass(name);
     *        }
     *    }
     *    ```
     * 
     *    (2) 类文件加密
     *    加密类加载器：
     *    ```java
     *    public class EncryptedClassLoader extends ClassLoader {
     *        private String encryptionKey;
     *        
     *        @Override
     *        protected Class<?> findClass(String name) throws ClassNotFoundException {
     *            try {
     *                // 1. 读取加密的类文件
     *                byte[] encryptedData = readEncryptedClassFile(name);
     *                
     *                // 2. 解密
     *                byte[] classData = decrypt(encryptedData, encryptionKey);
     *                
     *                // 3. 定义类
     *                return defineClass(name, classData, 0, classData.length);
     *                
     *            } catch (Exception e) {
     *                throw new ClassNotFoundException("Cannot decrypt class: " + name, e);
     *            }
     *        }
     *    }
     *    ```
     * 
     *    (3) 网络类加载
     *    从网络加载类：
     *    ```java
     *    public class NetworkClassLoader extends ClassLoader {
     *        private String serverUrl;
     *        
     *        @Override
     *        protected Class<?> findClass(String name) throws ClassNotFoundException {
     *            try {
     *                String classUrl = serverUrl + "/" + name.replace('.', '/') + ".class";
     *                byte[] classData = downloadClass(classUrl);
     *                return defineClass(name, classData, 0, classData.length);
     *                
     *            } catch (IOException e) {
     *                throw new ClassNotFoundException("Cannot download class: " + name, e);
     *            }
     *        }
     *    }
     *    ```
     * 
     *    (4) 插件系统
     *    插件类加载器：
     *    ```java
     *    public class PluginClassLoader extends URLClassLoader {
     *        private String pluginId;
     *        
     *        public PluginClassLoader(String pluginId, URL[] urls) {
     *            super(urls, getSystemClassLoader().getParent());
     *            this.pluginId = pluginId;
     *        }
     *        
     *        // 实现插件隔离
     *        @Override
     *        protected Class<?> loadClass(String name, boolean resolve) 
     *            throws ClassNotFoundException {
     *            
     *            // 插件类优先从自己的URL路径加载
     *            if (isPluginClass(name)) {
     *                Class<?> clazz = findLoadedClass(name);
     *                if (clazz == null) {
     *                    clazz = findClass(name);
     *                }
     *                if (resolve) {
     *                    resolveClass(clazz);
     *                }
     *                return clazz;
     *            }
     *            
     *            return super.loadClass(name, resolve);
     *        }
     *    }
     *    ```
     * 
     * 5. 最佳实践
     *    
     *    (1) 设计原则
     *    - 优先重写findClass而不是loadClass
     *    - 保持双亲委派机制（除非特殊需求）
     *    - 正确处理类的卸载
     *    - 考虑线程安全问题
     * 
     *    (2) 性能优化
     *    - 缓存已加载的类
     *    - 异步加载非关键类
     *    - 延迟加载机制
     *    - 预加载热点类
     * 
     *    (3) 安全考虑
     *    - 验证类文件来源
     *    - 设置适当的安全策略
     *    - 防止恶意类的加载
     *    - 实现权限检查
     * 
     * 6. 常见问题
     *    
     *    (1) 类型转换异常
     *    原因：不同类加载器加载的同名类被认为是不同类型
     *    解决：确保相关类由同一个类加载器加载
     * 
     *    (2) 内存泄漏
     *    原因：类加载器和加载的类无法被垃圾回收
     *    解决：及时释放类加载器引用，实现类卸载
     * 
     *    (3) 依赖问题
     *    原因：类的依赖类无法被找到
     *    解决：合理设计类加载器的委派关系
     */
    public void customClassLoader() {
        System.out.println("自定义类加载器：继承ClassLoader + 重写findClass + 应用场景");
    }

    /**
     * ====================
     * 二、类加载进阶问题
     * ====================
     */

    /**
     * Q4: 类的唯一性是如何确定的？不同类加载器加载同一个类会怎样？
     * 
     * A: 类唯一性机制详解：
     * 
     * 1. 类唯一性的定义
     *    唯一性标识：
     *    - 类的完整标识 = 类的全限定名 + 加载它的类加载器
     *    - 两个类相等的条件：同一个Class文件 + 同一个类加载器
     *    - 即使是同样的字节码，不同类加载器加载后也是不同的类
     * 
     * 2. 类相等的判断标准
     *    
     *    (1) Class对象的equals()方法
     *    ```java
     *    Class<?> class1 = loader1.loadClass("com.example.MyClass");
     *    Class<?> class2 = loader2.loadClass("com.example.MyClass");
     *    
     *    // 即使类名相同，不同加载器加载的类不相等
     *    boolean isEqual = class1.equals(class2); // false
     *    ```
     * 
     *    (2) isInstance()方法
     *    ```java
     *    Object obj1 = class1.newInstance();
     *    Object obj2 = class2.newInstance();
     *    
     *    boolean result = class1.isInstance(obj2); // false
     *    boolean result2 = class2.isInstance(obj1); // false
     *    ```
     * 
     *    (3) isAssignableFrom()方法
     *    ```java
     *    boolean canAssign = class1.isAssignableFrom(class2); // false
     *    ```
     * 
     *    (4) instanceof关键字
     *    ```java
     *    boolean isInstanceOf = obj1 instanceof class2; // 编译错误
     *    ```
     * 
     * 3. 实际影响
     *    
     *    (1) 类型转换异常
     *    ```java
     *    // ClassCastException示例
     *    Object obj1 = loader1.loadClass("com.example.MyClass").newInstance();
     *    Class<?> clazz2 = loader2.loadClass("com.example.MyClass");
     *    
     *    try {
     *        // 即使是同一个类文件，不同加载器加载的类无法互相转换
     *        clazz2.cast(obj1); // ClassCastException
     *    } catch (ClassCastException e) {
     *        System.out.println("类型转换失败：" + e.getMessage());
     *    }
     *    ```
     * 
     *    (2) 单例模式失效
     *    ```java
     *    // 单例类
     *    public class Singleton {
     *        private static Singleton instance = new Singleton();
     *        public static Singleton getInstance() { return instance; }
     *    }
     *    
     *    // 不同类加载器加载会产生多个"单例"实例
     *    Singleton s1 = (Singleton) loader1.loadClass("Singleton")
     *                                      .getMethod("getInstance")
     *                                      .invoke(null);
     *    Singleton s2 = (Singleton) loader2.loadClass("Singleton")
     *                                      .getMethod("getInstance")
     *                                      .invoke(null);
     *    // s1 和 s2 是不同的实例！
     *    ```
     * 
     * 4. 类隔离机制
     *    
     *    (1) 应用隔离
     *    Web容器中的类隔离：
     *    ```
     *    Tomcat类加载器架构：
     *    Bootstrap ClassLoader
     *           ↑
     *    System ClassLoader
     *           ↑
     *    Common ClassLoader
     *          ↙    ↘
     *    Catalina   Shared
     *    ClassLoader ClassLoader
     *                   ↑
     *               WebApp ClassLoader
     *                (为每个应用创建)
     *    ```
     * 
     *    好处：
     *    - 不同Web应用可以使用不同版本的同一个库
     *    - 应用间互不干扰
     *    - 支持热部署
     * 
     *    (2) 版本隔离
     *    ```java
     *    // 加载不同版本的同一个库
     *    URLClassLoader loader1 = new URLClassLoader(
     *        new URL[]{ new URL("file:///lib/gson-1.0.jar") });
     *    URLClassLoader loader2 = new URLClassLoader(
     *        new URL[]{ new URL("file:///lib/gson-2.0.jar") });
     *    
     *    Class<?> gson1 = loader1.loadClass("com.google.gson.Gson");
     *    Class<?> gson2 = loader2.loadClass("com.google.gson.Gson");
     *    
     *    // 同时使用两个版本的Gson
     *    Object gsonInstance1 = gson1.newInstance();
     *    Object gsonInstance2 = gson2.newInstance();
     *    ```
     * 
     * 5. 类的可见性规则
     *    
     *    可见性原则：
     *    - 子类加载器可以看到父类加载器加载的类
     *    - 父类加载器看不到子类加载器加载的类
     *    - 同级类加载器之间无法互相看到对方加载的类
     * 
     *    示例：
     *    ```java
     *    // 父类加载器加载的类
     *    Class<?> parentClass = parentLoader.loadClass("com.example.Parent");
     *    
     *    // 子类加载器可以访问父类
     *    Class<?> childClass = childLoader.loadClass("com.example.Child");
     *    // 在Child类中可以引用Parent类
     *    
     *    // 但Parent类无法引用Child类
     *    // 会抛出ClassNotFoundException
     *    ```
     * 
     * 6. 实际应用场景
     *    
     *    (1) 插件架构
     *    ```java
     *    public class PluginManager {
     *        private Map<String, PluginClassLoader> pluginLoaders = new HashMap<>();
     *        
     *        public void loadPlugin(String pluginId, String pluginPath) {
     *            PluginClassLoader loader = new PluginClassLoader(pluginPath);
     *            pluginLoaders.put(pluginId, loader);
     *            
     *            // 每个插件有独立的类命名空间
     *            Class<?> pluginClass = loader.loadClass("com.plugin.PluginMain");
     *            // 实例化插件
     *        }
     *        
     *        public void unloadPlugin(String pluginId) {
     *            PluginClassLoader loader = pluginLoaders.remove(pluginId);
     *            // 卸载插件类加载器
     *        }
     *    }
     *    ```
     * 
     *    (2) 模块化系统
     *    OSGi Bundle隔离：
     *    - 每个Bundle有独立的类加载器
     *    - 通过Export-Package/Import-Package控制类的可见性
     *    - 支持同一个类的多个版本并存
     * 
     * 7. 最佳实践
     *    
     *    (1) 避免类型转换问题
     *    - 使用接口进行交互
     *    - 通过反射调用方法
     *    - 序列化/反序列化传递数据
     * 
     *    (2) 共享类的设计
     *    - 将公共接口放在父类加载器中
     *    - 使用依赖注入减少直接类依赖
     *    - 设计良好的API边界
     * 
     *    (3) 内存管理
     *    - 及时释放不需要的类加载器
     *    - 避免类加载器泄漏
     *    - 监控类加载器的生命周期
     */
    public void classUniqueness() {
        System.out.println("类唯一性：类名+类加载器 + 类型转换 + 隔离机制 + 可见性规则");
    }

    /**
     * Q5: 什么是SPI机制？它是如何破坏双亲委派模型的？
     * 
     * A: SPI机制与双亲委派详解：
     * 
     * 1. SPI机制简介
     *    SPI (Service Provider Interface)：
     *    - 服务提供接口，是JDK内置的服务提供发现机制
     *    - 为接口寻找服务实现的机制
     *    - 将装配的控制权移到程序之外
     *    - 实现模块化设计和插件式架构
     * 
     * 2. SPI工作原理
     *    
     *    (1) 基本组成
     *    ```
     *    Service Interface    (服务接口)
     *           ↑
     *    Service Provider     (服务提供者)
     *           ↑  
     *    Service Loader       (服务加载器)
     *           ↑
     *    Service Consumer     (服务消费者)
     *    ```
     * 
     *    (2) 实现步骤
     *    1. 定义服务接口
     *    ```java
     *    // 1. 定义服务接口
     *    public interface DatabaseDriver {
     *        Connection connect(String url);
     *    }
     *    ```
     * 
     *    2. 实现服务提供者
     *    ```java
     *    // 2. 实现服务
     *    public class MySQLDriver implements DatabaseDriver {
     *        @Override
     *        public Connection connect(String url) {
     *            // MySQL连接实现
     *            return new MySQLConnection(url);
     *        }
     *    }
     *    ```
     * 
     *    3. 配置服务提供者
     *    ```
     *    // 3. 在META-INF/services/目录下创建文件
     *    // 文件名：com.example.DatabaseDriver
     *    // 文件内容：
     *    com.mysql.MySQLDriver
     *    com.oracle.OracleDriver
     *    ```
     * 
     *    4. 使用ServiceLoader加载
     *    ```java
     *    // 4. 使用ServiceLoader
     *    ServiceLoader<DatabaseDriver> drivers = 
     *        ServiceLoader.load(DatabaseDriver.class);
     *    
     *    for (DatabaseDriver driver : drivers) {
     *        Connection conn = driver.connect(url);
     *    }
     *    ```
     * 
     * 3. SPI与双亲委派的冲突
     *    
     *    (1) 问题描述
     *    冲突场景：
     *    - SPI接口在核心库中（rt.jar），由Bootstrap ClassLoader加载
     *    - SPI实现在应用或第三方库中，由Application ClassLoader加载
     *    - Bootstrap ClassLoader无法访问Application ClassLoader加载的类
     * 
     *    具体示例：
     *    ```java
     *    // JDBC SPI接口在rt.jar中
     *    java.sql.Driver driver;  // Bootstrap ClassLoader加载
     *    
     *    // MySQL驱动实现在mysql-connector.jar中
     *    com.mysql.jdbc.Driver;  // Application ClassLoader加载
     *    
     *    // Bootstrap ClassLoader无法找到MySQL驱动实现
     *    ```
     * 
     * 4. 线程上下文类加载器解决方案
     *    
     *    (1) 机制原理
     *    ```java
     *    public class ServiceLoader<S> implements Iterable<S> {
     *        
     *        public static <S> ServiceLoader<S> load(Class<S> service) {
     *            // 获取线程上下文类加载器
     *            ClassLoader cl = Thread.currentThread().getContextClassLoader();
     *            return ServiceLoader.load(service, cl);
     *        }
     *        
     *        public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
     *            return new ServiceLoader<>(service, loader);
     *        }
     *    }
     *    ```
     * 
     *    (2) 线程上下文类加载器设置
     *    ```java
     *    // JVM启动时设置
     *    Thread.currentThread().setContextClassLoader(
     *        ClassLoader.getSystemClassLoader());
     *    
     *    // 在需要时临时设置
     *    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
     *    try {
     *        Thread.currentThread().setContextClassLoader(customLoader);
     *        // 执行需要特定类加载器的操作
     *        ServiceLoader.load(SomeService.class);
     *    } finally {
     *        Thread.currentThread().setContextClassLoader(originalLoader);
     *    }
     *    ```
     * 
     * 5. SPI的典型应用
     *    
     *    (1) JDBC驱动加载
     *    ```java
     *    // JDBC 4.0之前
     *    Class.forName("com.mysql.jdbc.Driver");
     *    
     *    // JDBC 4.0之后使用SPI
     *    // DriverManager自动通过SPI加载驱动
     *    Connection conn = DriverManager.getConnection(url, user, password);
     *    ```
     * 
     *    DriverManager的实现：
     *    ```java
     *    static {
     *        loadInitialDrivers();
     *    }
     *    
     *    private static void loadInitialDrivers() {
     *        // 使用SPI加载驱动
     *        ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
     *        Iterator<Driver> driversIterator = loadedDrivers.iterator();
     *        
     *        while(driversIterator.hasNext()) {
     *            driversIterator.next();
     *        }
     *    }
     *    ```
     * 
     *    (2) 日志框架
     *    ```java
     *    // SLF4J通过SPI选择具体的日志实现
     *    ServiceLoader<SLF4JServiceProvider> serviceLoader = 
     *        ServiceLoader.load(SLF4JServiceProvider.class);
     *    ```
     * 
     *    (3) XML解析器
     *    ```java
     *    // JAXP通过SPI选择XML解析器实现
     *    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     *    // 内部通过SPI机制选择具体的解析器
     *    ```
     * 
     * 6. SPI机制的优缺点
     *    
     *    (1) 优点
     *    - 解耦：接口与实现分离
     *    - 扩展性：支持插件式架构
     *    - 标准化：JDK内置支持
     *    - 动态性：运行时发现服务
     * 
     *    (2) 缺点
     *    - 性能开销：需要扫描classpath
     *    - 类加载顺序：破坏双亲委派模型
     *    - 调试困难：隐式的依赖关系
     *    - 线程安全：需要考虑并发问题
     * 
     * 7. 现代替代方案
     *    
     *    (1) Spring的条件装配
     *    ```java
     *    @Component
     *    @ConditionalOnClass(MySQLDriver.class)
     *    public class MySQLDriverAutoConfiguration {
     *        // 自动配置
     *    }
     *    ```
     * 
     *    (2) Java 9的模块系统
     *    ```java
     *    // module-info.java
     *    module myapp {
     *        uses com.example.DatabaseDriver;
     *        provides com.example.DatabaseDriver 
     *            with com.mysql.MySQLDriver;
     *    }
     *    ```
     * 
     *    (3) OSGi的服务注册
     *    ```java
     *    // 注册服务
     *    bundleContext.registerService(
     *        DatabaseDriver.class.getName(), 
     *        new MySQLDriver(), 
     *        properties);
     *    
     *    // 获取服务
     *    ServiceReference<DatabaseDriver> ref = 
     *        bundleContext.getServiceReference(DatabaseDriver.class);
     *    ```
     * 
     * 8. 最佳实践
     *    
     *    (1) SPI设计原则
     *    - 接口设计要简单清晰
     *    - 避免过度依赖SPI
     *    - 考虑性能影响
     *    - 提供默认实现
     * 
     *    (2) 使用建议
     *    - 在框架和中间件中使用SPI
     *    - 业务代码尽量避免直接使用
     *    - 结合依赖注入框架使用
     *    - 注意类加载器的管理
     */
    public void spiMechanism() {
        System.out.println("SPI机制：服务发现 + 破坏双亲委派 + 线程上下文类加载器 + 典型应用");
    }
}