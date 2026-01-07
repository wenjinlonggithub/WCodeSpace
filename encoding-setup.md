# Windows Java 中文编码完整解决方案

## 根本问题
Windows控制台默认使用GBK/CP936编码，Java源码使用UTF-8，导致中文显示乱码。

## 🚀 最佳解决方案（推荐）

### 1. 设置Windows控制台UTF-8编码
```cmd
chcp 65001
```

### 2. 创建运行脚本
创建 `run.bat` 文件：
```batch
@echo off
chcp 65001 > nul
echo 设置控制台为UTF-8编码
javac -encoding UTF-8 src\main\java\com\architecture\designpattern\singleton\SingletonExample.java
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp src\main\java com.architecture.designpattern.singleton.SingletonExample
echo.
echo 运行Sun单例测试:
javac -encoding UTF-8 src\main\java\com\architecture\designpattern\singleton\demo\Sun.java  
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp src\main\java com.architecture.designpattern.singleton.demo.Sun
pause
```

## 其他方案

### 方法1: IDEA设置
1. File → Settings → Editor → File Encodings
2. 设置所有编码为UTF-8

### 方法2: 运行配置VM参数  
`-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8`

### 方法3: 环境变量
`JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8`

## ⚠️ 避免的错误做法
- 不要在代码中重新设置System.out（会导致冲突）
- 不要使用PrintStream包装（在Windows下效果不佳）