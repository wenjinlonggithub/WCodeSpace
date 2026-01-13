#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
代码注释自动添加工具
用于为Java类批量添加基本的JavaDoc注释模板

使用方法：
python add_comments.py <文件路径或目录路径>

功能：
1. 为没有注释的类添加类级注释模板
2. 为没有注释的方法添加方法注释模板
3. 为没有注释的字段添加字段注释模板
4. 保持已有注释不变

注意：
- 生成的是模板，需要手动填充具体内容
- 请在使用前备份代码
"""

import os
import re
import sys
from pathlib import Path


class JavaCommentAdder:
    """Java代码注释添加器"""

    def __init__(self):
        self.class_template = """/**
 * {class_name}
 * TODO: 添加类的功能说明
 *
 * @author OKX Finance Team
 * @version 1.0
 */"""

        self.method_template = """    /**
     * {method_name}
     * TODO: 添加方法说明
     *
{params}     * @return TODO: 添加返回值说明
     */"""

        self.field_template = """    /**
     * {field_name}
     * TODO: 添加字段说明
     */"""

    def has_comment_above(self, lines, line_index):
        """检查指定行上方是否有注释"""
        if line_index == 0:
            return False

        # 向上查找，跳过空行
        for i in range(line_index - 1, -1, -1):
            line = lines[i].strip()
            if not line:
                continue
            # 检查是否是注释结束符
            if line.endswith('*/'):
                return True
            # 如果遇到其他内容，说明没有注释
            return False
        return False

    def extract_params(self, method_line):
        """提取方法参数"""
        # 匹配参数列表
        match = re.search(r'\((.*?)\)', method_line)
        if not match:
            return []

        params_str = match.group(1).strip()
        if not params_str:
            return []

        # 分割参数
        params = []
        for param in params_str.split(','):
            param = param.strip()
            if param:
                # 提取参数名（最后一个单词）
                parts = param.split()
                if len(parts) >= 2:
                    params.append(parts[-1])
        return params

    def add_class_comment(self, lines, line_index, class_name):
        """为类添加注释"""
        comment = self.class_template.format(class_name=class_name)
        return comment.split('\n')

    def add_method_comment(self, lines, line_index, method_line):
        """为方法添加注释"""
        # 提取方法名
        match = re.search(r'(\w+)\s*\(', method_line)
        if not match:
            return []

        method_name = match.group(1)

        # 提取参数
        params = self.extract_params(method_line)
        params_doc = ''
        for param in params:
            params_doc += f'     * @param {param} TODO: 添加参数说明\n'

        comment = self.method_template.format(
            method_name=method_name,
            params=params_doc
        )
        return comment.split('\n')

    def add_field_comment(self, lines, line_index, field_line):
        """为字段添加注释"""
        # 提取字段名
        match = re.search(r'(\w+)\s*[;=]', field_line)
        if not match:
            return []

        field_name = match.group(1)
        comment = self.field_template.format(field_name=field_name)
        return comment.split('\n')

    def process_file(self, file_path):
        """处理单个文件"""
        print(f"Processing: {file_path}")

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
        except Exception as e:
            print(f"Error reading file: {e}")
            return False

        new_lines = []
        i = 0
        modified = False

        while i < len(lines):
            line = lines[i]
            stripped = line.strip()

            # 检查是否是类定义
            if re.match(r'(public|private|protected)?\s*(static)?\s*(final)?\s*class\s+\w+', stripped):
                if not self.has_comment_above(new_lines, len(new_lines)):
                    # 提取类名
                    match = re.search(r'class\s+(\w+)', stripped)
                    if match:
                        class_name = match.group(1)
                        comment_lines = self.add_class_comment(new_lines, i, class_name)
                        for comment_line in comment_lines:
                            new_lines.append(comment_line + '\n')
                        modified = True

            # 检查是否是方法定义
            elif re.match(r'(public|private|protected)\s+.*\s+\w+\s*\([^)]*\)\s*(\{|throws)', stripped):
                if not self.has_comment_above(new_lines, len(new_lines)):
                    comment_lines = self.add_method_comment(new_lines, i, stripped)
                    if comment_lines:
                        for comment_line in comment_lines:
                            new_lines.append(comment_line + '\n')
                        modified = True

            # 检查是否是字段定义（排除方法中的局部变量）
            elif re.match(r'(public|private|protected)\s+.*\s+\w+\s*[;=]', stripped):
                if not self.has_comment_above(new_lines, len(new_lines)):
                    comment_lines = self.add_field_comment(new_lines, i, stripped)
                    if comment_lines:
                        for comment_line in comment_lines:
                            new_lines.append(comment_line + '\n')
                        modified = True

            new_lines.append(line)
            i += 1

        if modified:
            try:
                # 备份原文件
                backup_path = str(file_path) + '.bak'
                with open(backup_path, 'w', encoding='utf-8') as f:
                    f.writelines(lines)

                # 写入新内容
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.writelines(new_lines)

                print(f"  ✓ Added comments (backup: {backup_path})")
                return True
            except Exception as e:
                print(f"  ✗ Error writing file: {e}")
                return False
        else:
            print(f"  - No changes needed")
            return True

    def process_directory(self, dir_path):
        """处理目录下的所有Java文件"""
        java_files = Path(dir_path).rglob('*.java')
        success_count = 0
        total_count = 0

        for java_file in java_files:
            total_count += 1
            if self.process_file(java_file):
                success_count += 1

        print(f"\n处理完成: {success_count}/{total_count} 个文件")


def main():
    if len(sys.argv) < 2:
        print("用法: python add_comments.py <文件路径或目录路径>")
        print("示例: python add_comments.py ./src/main/java")
        sys.exit(1)

    path = sys.argv[1]
    adder = JavaCommentAdder()

    if os.path.isfile(path):
        adder.process_file(path)
    elif os.path.isdir(path):
        adder.process_directory(path)
    else:
        print(f"错误: 路径不存在: {path}")
        sys.exit(1)


if __name__ == '__main__':
    main()
