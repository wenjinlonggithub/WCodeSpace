#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复所有包含占位符的推文
严格遵守 TWEET-QUALITY-RULES.md
"""

import os
import re
from pathlib import Path

# 需要修复的文件列表（从grep结果中提取）
FILES_TO_FIX = """
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/psychology/2025-12-15-散户如何在熊市中生存.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/psychology/2025-12-02-散户抄底的3个致命错误.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/analysis/2025-12-01-美联储缩表.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/strategy/2025-12-03-期权交易.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/analysis/2025-12-04-科技股估值.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/trend/2025-12-05-被动投资的黄金时代结束了.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/mechanism/2025-12-06-做空机制.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/trend/2025-12-07-大宗商品超级周期.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/mechanism/2025-12-08-高频交易如何吃掉你的利润.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/analysis/2025-12-09-美国国债.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/bitcoin/2025-12-10-加密货币ETF.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/events/2025-12-11-日元套利交易崩盘预警.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/analysis/2025-12-12-ESG投资的真实回报.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/strategy/2025-12-13-房地产信托REITs.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/events/2025-12-14-债券收益率曲线倒挂后会发生什么.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/analysis/2025-12-16-量化宽松的真实成本.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/trend/2025-12-17-石油美元体系的裂痕.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/strategy/2025-12-18-通胀保护债券TIPS值得买吗.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/mechanism/2025-12-19-股票回购.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/trend/2025-12-20-全球去美元化.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/2025-10-26-原油价格与通胀.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/2025-10-27-日本央行政策转向.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/2025-10-28-科技股泡沫2.0.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/2025-10-29-房地产市场寒冬.md
/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance/2025-10-210-ESG投资.md
""".strip().split('\n')

def check_file_has_placeholders(filepath):
    """检查文件是否包含占位符"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # 检查各种占位符模式
        patterns = [
            r'观点[123]',
            r'要点[123]',
            r'Point [123]',
            r'洞察[123]',
            r'Insight [123]',
            r'建议[123]',
            r'数据[123]',
            r'案例[123]',
            r'\[描述\]',
            r'\[数据\]',
            r'\[内容\]',
        ]

        for pattern in patterns:
            if re.search(pattern, content):
                return True, pattern
        return False, None

    except Exception as e:
        print(f"❌ 读取文件失败: {filepath} - {e}")
        return False, None

def main():
    """主函数"""
    print("=" * 60)
    print("📋 扫描包含占位符的推文文件")
    print("=" * 60)

    files_with_placeholders = []

    for filepath in FILES_TO_FIX:
        filepath = filepath.strip()
        if not filepath or not os.path.exists(filepath):
            continue

        has_placeholder, pattern = check_file_has_placeholders(filepath)
        if has_placeholder:
            files_with_placeholders.append((filepath, pattern))
            print(f"⚠️  {os.path.basename(filepath)}: 包含 {pattern}")

    print(f"\n📊 统计结果：")
    print(f"   总计需修复：{len(files_with_placeholders)} 个文件")

    # 按专题分类
    by_topic = {}
    for filepath, _ in files_with_placeholders:
        if 'finance' in filepath:
            topic = '金融投资'
        elif 'media-entrepreneurship' in filepath:
            topic = '自媒体出海'
        elif 'programming' in filepath:
            topic = '编程开发'
        elif 'indie-dev' in filepath:
            topic = '个人开发'
        elif 'personal-growth' in filepath:
            topic = '个人成长'
        elif 'legendary-posts' in filepath:
            topic = '神贴'
        else:
            topic = '其他'

        if topic not in by_topic:
            by_topic[topic] = []
        by_topic[topic].append(filepath)

    print(f"\n📈 分专题统计：")
    for topic, files in sorted(by_topic.items()):
        print(f"   {topic}: {len(files)} 个文件")

    # 输出文件列表供agents处理
    print(f"\n📝 待修复文件列表：")
    for filepath, _ in files_with_placeholders[:10]:  # 显示前10个
        print(f"   - {filepath}")
    if len(files_with_placeholders) > 10:
        print(f"   ... 还有 {len(files_with_placeholders) - 10} 个文件")

    return files_with_placeholders

if __name__ == "__main__":
    result = main()
