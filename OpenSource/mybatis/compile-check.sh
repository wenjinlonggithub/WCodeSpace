#!/bin/bash

# MyBatis项目编译验证脚本

echo "=========================================="
echo "MyBatis项目编译验证开始"
echo "=========================================="

# 检查Java版本
echo "检查Java版本..."
java -version

# 检查Maven版本
echo "检查Maven版本..."
mvn -version

# 清理项目
echo "清理项目..."
mvn clean

# 编译项目
echo "编译项目..."
mvn compile

# 检查编译结果
if [ $? -eq 0 ]; then
    echo "✅ 编译成功！"
else
    echo "❌ 编译失败！"
    exit 1
fi

# 运行测试
echo "运行测试..."
mvn test

# 打包项目
echo "打包项目..."
mvn package -DskipTests

echo "=========================================="
echo "MyBatis项目编译验证完成"
echo "=========================================="