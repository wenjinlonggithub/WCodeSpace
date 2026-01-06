#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为金融投资专题新增20条中英双语推文
时间驱动，紧跟市场热点
"""

import os
from datetime import datetime, timedelta

# 新增20个金融投资主题
FINANCE_TOPICS = [
    ("美联储缩表：被忽视的流动性黑洞", "Fed Balance Sheet Reduction: The Overlooked Liquidity Black Hole", "analysis"),
    ("散户抄底的3个致命错误", "3 Fatal Mistakes Retail Investors Make When Buying the Dip", "psychology"),
    ("期权交易：以小博大还是送钱", "Options Trading: Leverage or Giving Money Away", "strategy"),
    ("科技股估值：市盈率已经失效了吗", "Tech Stock Valuation: Is P/E Ratio Obsolete", "analysis"),
    ("被动投资的黄金时代结束了", "The Golden Age of Passive Investing Is Over", "trend"),
    ("做空机制：市场的必要之恶", "Short Selling: A Necessary Evil for Markets", "mechanism"),
    ("大宗商品超级周期：这次不一样吗", "Commodity Supercycle: Is It Different This Time", "trend"),
    ("高频交易如何吃掉你的利润", "How High-Frequency Trading Eats Your Profits", "mechanism"),
    ("美国国债：还是避险资产吗", "US Treasuries: Still a Safe Haven", "analysis"),
    ("加密货币ETF：华尔街的特洛伊木马", "Crypto ETFs: Wall Street's Trojan Horse", "bitcoin"),
    ("日元套利交易崩盘预警", "Yen Carry Trade Collapse Warning", "events"),
    ("ESG投资的真实回报", "The Real Returns of ESG Investing", "analysis"),
    ("房地产信托REITs：被低估的现金流机器", "REITs: The Undervalued Cash Flow Machine", "strategy"),
    ("债券收益率曲线倒挂后会发生什么", "What Happens After Yield Curve Inversion", "events"),
    ("散户如何在熊市中生存", "How Retail Investors Survive Bear Markets", "psychology"),
    ("量化宽松的真实成本", "The True Cost of Quantitative Easing", "analysis"),
    ("石油美元体系的裂痕", "Cracks in the Petrodollar System", "trend"),
    ("通胀保护债券TIPS值得买吗", "Are TIPS Worth Buying", "strategy"),
    ("股票回购：股东价值还是财务游戏", "Stock Buybacks: Shareholder Value or Financial Engineering", "mechanism"),
    ("全球去美元化：进程到哪一步了", "Global De-dollarization: How Far Has It Come", "trend"),
]

# 模板
TEMPLATE = """# {title_zh}

## 🇨🇳 中文版

{content_placeholder_zh}

**核心观点：**
• 观点1
• 观点2
• 观点3

**数据支撑：**
• 数据点1
• 数据点2
• 数据点3

**投资启示：**
1. 启示1
2. 启示2
3. 启示3

⚠️ **风险提示：**
市场有风险，投资需谨慎。本文仅供参考，不构成投资建议。

你的看法是什么？💬

---

## 🇬🇧 English Version

# {title_en}

{content_placeholder_en}

**Key Points:**
• Point 1
• Point 2
• Point 3

**Data Support:**
• Data point 1
• Data point 2
• Data point 3

**Investment Insights:**
1. Insight 1
2. Insight 2
3. Insight 3

⚠️ **Risk Warning:**
Markets carry risk. This is for informational purposes only, not investment advice.

What's your take? 💬

---

## 标签 / Tags
#金融 #Finance #投资 #Investment #{category_tag}

## 发布建议 / Publishing Tips
- 最佳时间 / Best Time: {publish_time}
- 附图 / Attach: 相关图表数据 / Relevant charts and data
- 互动 / Engagement: 征求观点，引发讨论 / Solicit opinions, spark discussion
- 平台 / Platform: X/Twitter, LinkedIn

## 创作日期 / Created
{date}

## 内容分类 / Category
{category}
"""

# 内容占位符模板
CONTENT_PLACEHOLDERS = {
    "analysis": {
        "zh": "[市场现象]\n\n深入分析：\n\n从[维度1]看...\n从[维度2]看...\n从[维度3]看...",
        "en": "[Market Phenomenon]\n\nIn-depth Analysis:\n\nFrom [perspective 1]...\nFrom [perspective 2]...\nFrom [perspective 3]..."
    },
    "psychology": {
        "zh": "散户常犯的错误：\n\n错误1：[描述]\n错误2：[描述]\n错误3：[描述]\n\n背后的心理机制：",
        "en": "Common retail investor mistakes:\n\nMistake 1: [description]\nMistake 2: [description]\nMistake 3: [description]\n\nUnderlying psychology:"
    },
    "strategy": {
        "zh": "策略框架：\n\n第一步：[操作]\n第二步：[操作]\n第三步：[操作]\n\n关键点：",
        "en": "Strategy Framework:\n\nStep 1: [action]\nStep 2: [action]\nStep 3: [action]\n\nKey Points:"
    },
    "mechanism": {
        "zh": "[机制名称]的运作原理：\n\n1. [原理1]\n2. [原理2]\n3. [原理3]\n\n对投资者的影响：",
        "en": "How [mechanism] works:\n\n1. [principle 1]\n2. [principle 2]\n3. [principle 3]\n\nImpact on investors:"
    },
    "trend": {
        "zh": "观察到的趋势信号：\n\n信号1：[数据]\n信号2：[数据]\n信号3：[数据]\n\n未来展望：",
        "en": "Observed trend signals:\n\nSignal 1: [data]\nSignal 2: [data]\nSignal 3: [data]\n\nFuture outlook:"
    },
    "events": {
        "zh": "事件背景：[时间+事件]\n\n市场反应：\n- 股市：\n- 债市：\n- 商品：\n\n历史对比：",
        "en": "Event background: [time + event]\n\nMarket reaction:\n- Stocks:\n- Bonds:\n- Commodities:\n\nHistorical comparison:"
    },
    "bitcoin": {
        "zh": "比特币/加密货币视角：\n\n当前状态：[数据]\n\n关键因素：\n1. [因素1]\n2. [因素2]\n3. [因素3]",
        "en": "Bitcoin/Crypto perspective:\n\nCurrent state: [data]\n\nKey factors:\n1. [factor 1]\n2. [factor 2]\n3. [factor 3]"
    }
}

# 发布时间建议
PUBLISH_TIMES = {
    "analysis": "市场收盘后 / After market close",
    "psychology": "周末 / Weekend",
    "strategy": "周一开盘前 / Before Monday open",
    "mechanism": "任意时间 / Anytime",
    "trend": "月初 / Beginning of month",
    "events": "事件发生当天 / Day of event",
    "bitcoin": "加密市场活跃时段 / Active crypto hours"
}

# 分类标签
CATEGORY_TAGS = {
    "analysis": "市场分析 #MarketAnalysis",
    "psychology": "交易心理 #TradingPsychology",
    "strategy": "投资策略 #InvestmentStrategy",
    "mechanism": "市场机制 #MarketMechanism",
    "trend": "市场趋势 #MarketTrend",
    "events": "市场事件 #MarketEvent",
    "bitcoin": "加密货币 #Crypto"
}

def create_finance_tweet(index, title_zh, title_en, category):
    """创建单个金融推文"""

    # 从12月1日开始
    date = (datetime(2025, 12, 1) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名
    filename_base = title_zh.split("：")[0].replace(" ", "-").replace("、", "-")
    filename = f"{date}-{filename_base}.md"

    # 获取内容占位符
    placeholder = CONTENT_PLACEHOLDERS[category]

    # 填充模板
    content = TEMPLATE.format(
        title_zh=title_zh,
        title_en=title_en,
        content_placeholder_zh=placeholder["zh"],
        content_placeholder_en=placeholder["en"],
        category_tag=CATEGORY_TAGS[category],
        publish_time=PUBLISH_TIMES[category],
        date=date,
        category=category
    )

    return filename, content

def main():
    """主函数"""
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/finance"

    # 根据分类创建子目录映射
    category_dirs = {
        "analysis": "analysis",
        "psychology": "psychology",
        "strategy": "strategy",
        "mechanism": "mechanism",
        "trend": "trend",
        "events": "events",
        "bitcoin": "bitcoin"
    }

    created_count = 0

    for index, (title_zh, title_en, category) in enumerate(FINANCE_TOPICS):
        filename, content = create_finance_tweet(index, title_zh, title_en, category)

        # 确定目标目录
        target_dir = os.path.join(base_dir, category_dirs[category])
        os.makedirs(target_dir, exist_ok=True)

        filepath = os.path.join(target_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {category}/{filename}")

    print(f"\n🎉 金融投资专题新增 {created_count} 篇推文！")
    print(f"📊 分类统计：")

    # 统计每个分类的数量
    from collections import Counter
    category_counts = Counter([cat for _, _, cat in FINANCE_TOPICS])
    for cat, count in category_counts.items():
        print(f"  - {cat}: {count}篇")

if __name__ == "__main__":
    main()
