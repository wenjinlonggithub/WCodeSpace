@echo off
echo ==========================================
echo MyBatis项目编译验证开始
echo ==========================================

echo 检查Java版本...
java -version

echo 检查Maven版本...
mvn -version

echo 清理项目...
mvn clean

echo 编译项目...
mvn compile

if %ERRORLEVEL% EQU 0 (
    echo ✅ 编译成功！
) else (
    echo ❌ 编译失败！
    echo 请检查以下常见问题：
    echo 1. 是否缺少@Slf4j注解
    echo 2. 是否安装了Lombok插件
    echo 3. 是否启用了注解处理
    echo 4. 查看详细错误信息
    pause
    exit /b 1
)

echo 检查日志功能...
echo 验证@Slf4j注解是否存在...
findstr /S /M "@Slf4j" src\main\java\com\learning\mybatis\demo\*.java
if %ERRORLEVEL% EQU 0 (
    echo ✅ 找到@Slf4j注解
) else (
    echo ⚠️ 未找到@Slf4j注解，可能影响日志功能
)

echo 运行测试...
mvn test

echo 打包项目...
mvn package -DskipTests

echo ==========================================
echo MyBatis项目编译验证完成
echo 如果遇到log.info问题，请查看LOG_FIX.md文档
echo ==========================================
pause