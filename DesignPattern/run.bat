@echo off
chcp 65001 > nul
echo 设置控制台为UTF-8编码
echo.

echo 编译单例模式示例...
javac -encoding UTF-8 src\main\java\com\architecture\designpattern\singleton\SingletonExample.java

echo 运行单例模式示例:
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp src\main\java com.architecture.designpattern.singleton.SingletonExample

echo.
echo ================================
echo.

echo 编译Sun单例测试...
javac -encoding UTF-8 src\main\java\com\architecture\designpattern\singleton\demo\Sun.java

echo 运行Sun单例多线程测试:
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp src\main\java com.architecture.designpattern.singleton.demo.Sun

echo.
pause