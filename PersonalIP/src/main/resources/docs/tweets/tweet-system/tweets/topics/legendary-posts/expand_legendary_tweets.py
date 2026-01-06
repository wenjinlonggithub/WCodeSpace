#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为神贴专题新增20条中英双语推文
道破天机，认知升级
"""

import os
from datetime import datetime, timedelta

# 新增20个神贴主题
LEGENDARY_TOPICS = [
    ("选择比努力重要100倍", "Choice Matters 100x More Than Effort", "认知颠覆型"),
    ("信息差就是最大的财富来源", "Information Asymmetry Is the Biggest Source of Wealth", "本质洞察型"),
    ("大多数人的问题不是执行力而是方向", "Most People's Problem Isn't Execution But Direction", "认知颠覆型"),
    ("杠杆：穷人用时间富人用系统", "Leverage: Poor Use Time, Rich Use Systems", "本质洞察型"),
    ("为什么聪明人反而容易失败", "Why Smart People Often Fail", "认知颠覆型"),
    ("耐心是最被低估的竞争优势", "Patience Is the Most Underrated Competitive Advantage", "本质洞察型"),
    ("所有的焦虑都源于想要控制不可控", "All Anxiety Stems from Trying to Control the Uncontrollable", "本质洞察型"),
    ("做减法比做加法更难也更重要", "Subtraction Is Harder and More Important Than Addition", "认知颠覆型"),
    ("人工智能时代：会提问比会回答更值钱", "AI Era: Asking Questions Is More Valuable Than Answering", "趋势预测型"),
    ("注意力经济：你的关注等于你的投资", "Attention Economy: Your Focus Equals Your Investment", "趋势预测型"),
    ("成功的本质是概率游戏不是因果游戏", "Success Is a Probability Game Not a Causality Game", "本质洞察型"),
    ("边界感缺失是人际问题的根源", "Lack of Boundaries Is the Root of Relationship Problems", "本质洞察型"),
    ("机会成本：你说yes就是对其他选项说no", "Opportunity Cost: Saying Yes Means Saying No to Alternatives", "跨界融合型"),
    ("第一性原理：从结论倒推到公理", "First Principles: Reverse from Conclusion to Axiom", "跨界融合型"),
    ("黑天鹅效应：小概率事件主导人生", "Black Swan: Low-Probability Events Dominate Life", "跨界融合型"),
    ("路径依赖：为什么改变如此困难", "Path Dependency: Why Change Is So Difficult", "跨界融合型"),
    ("幸存者偏差：你看到的成功都是假象", "Survivorship Bias: The Success You See Is an Illusion", "跨界融合型"),
    ("破窗效应：小事如何导致大崩溃", "Broken Windows Theory: How Small Things Lead to Big Collapse", "跨界融合型"),
    ("认知闭合：为什么有些人拒绝真相", "Cognitive Closure: Why Some People Reject Truth", "跨界融合型"),
    ("游戏化思维：把人生当成RPG玩", "Gamification Mindset: Treat Life Like an RPG", "跨界融合型"),
]

TEMPLATE = """# {title_zh}

## 🇨🇳 中文版

{content_zh}

**核心洞察：**
💡 洞察1
💡 洞察2
💡 洞察3

**底层逻辑：**
{logic_zh}

**案例验证：**
📌 案例1：[描述]
📌 案例2：[描述]
📌 案例3：[描述]

**可行动建议：**
1️⃣ 建议1
2️⃣ 建议2
3️⃣ 建议3

这个认知改变了你什么？💭

---

## 🇬🇧 English Version

# {title_en}

{content_en}

**Core Insights:**
💡 Insight 1
💡 Insight 2
💡 Insight 3

**Underlying Logic:**
{logic_en}

**Case Studies:**
📌 Case 1: [description]
📌 Case 2: [description]
📌 Case 3: [description]

**Actionable Takeaways:**
1️⃣ Takeaway 1
2️⃣ Takeaway 2
3️⃣ Takeaway 3

How does this shift your thinking? 💭

---

## 标签 / Tags
#神贴 #LegendaryPost #深度思考 #DeepThinking #认知升级 #MentalModels #{type_tag}

## 发布建议 / Publishing Tips
- 中文发布时间 / CN Time: 晚上20:00-22:00（深度思考时段）
- 英文发布时间 / EN Time: 美国东部时间9:00-11:00（通勤阅读时段）
- 平台 / Platform: X/Twitter, LinkedIn（适合深度内容）
- 互动 / Engagement: 引发思考型问题 / Thought-provoking questions
- 特点 / Feature: 反直觉但有理有据 / Counterintuitive but evidence-based

## 创作日期 / Created
{date}

## 神贴类型 / Post Type
{post_type}
"""

# 不同类型的内容模板
CONTENT_TEMPLATES = {
    "认知颠覆型": {
        "zh": """传统观念告诉我们：[常规认知]

但真相可能恰恰相反。

**为什么？**

观察1：[数据/现象]
观察2：[数据/现象]
观察3：[数据/现象]

**深层原因：**

[揭示底层逻辑]

这彻底改变了我的视角。""",
        "en": """Conventional wisdom tells us: [common belief]

But the truth might be the exact opposite.

**Why?**

Observation 1: [data/phenomenon]
Observation 2: [data/phenomenon]
Observation 3: [data/phenomenon]

**Root Cause:**

[reveal underlying logic]

This completely shifted my perspective.""",
        "logic_zh": "用数据和逻辑推翻常规认知，建立新的思维框架",
        "logic_en": "Use data and logic to challenge conventional wisdom and build new mental models"
    },
    "本质洞察型": {
        "zh": """表面上看：[表象A] 和 [表象B] 完全不同

但本质上它们都是：[底层规律]

**证据链：**

证据1：[阐述]
证据2：[阐述]
证据3：[阐述]

**推导：**

如果理解了这个本质，你就会明白...""",
        "en": """On the surface: [Phenomenon A] and [Phenomenon B] seem different

But fundamentally they are: [underlying principle]

**Evidence Chain:**

Evidence 1: [explanation]
Evidence 2: [explanation]
Evidence 3: [explanation]

**Inference:**

Once you understand this essence, you'll realize...""",
        "logic_zh": "透过表象看本质，揭示底层规律",
        "logic_en": "See through surface to essence, reveal underlying principles"
    },
    "趋势预测型": {
        "zh": """观察到3个关键信号：

信号1：[数据/事件]
信号2：[数据/事件]
信号3：[数据/事件]

**推断：**

未来1-3年将会：[趋势预测]

**时间线：**

短期（6-12个月）：[变化]
中期（1-3年）：[变化]
长期（3-5年）：[变化]""",
        "en": """Observed 3 key signals:

Signal 1: [data/event]
Signal 2: [data/event]
Signal 3: [data/event]

**Inference:**

In the next 1-3 years: [trend prediction]

**Timeline:**

Short-term (6-12 months): [change]
Mid-term (1-3 years): [change]
Long-term (3-5 years): [change]""",
        "logic_zh": "基于数据和趋势，预见未来变化",
        "logic_en": "Based on data and trends, predict future changes"
    },
    "跨界融合型": {
        "zh": """[领域A]的经典理论可以完美解释[领域B]的现象

**原理映射：**

A领域原理：[阐述]
↓ 对应 ↓
B领域现象：[阐述]

**底层逻辑相同：**

都遵循：[通用规律]

**启示：**

如果你在B领域遇到问题，可以借鉴A领域的解决方案。""",
        "en": """Classic theory from [Domain A] perfectly explains phenomena in [Domain B]

**Principle Mapping:**

Domain A principle: [explanation]
↓ corresponds to ↓
Domain B phenomenon: [explanation]

**Same Underlying Logic:**

Both follow: [universal principle]

**Insight:**

If you face problems in Domain B, borrow solutions from Domain A.""",
        "logic_zh": "从不同领域提炼共通智慧",
        "logic_en": "Extract universal wisdom from different domains"
    }
}

# 类型标签
TYPE_TAGS = {
    "认知颠覆型": "认知颠覆 #ParadigmShift",
    "本质洞察型": "本质洞察 #EssentialInsight",
    "趋势预测型": "趋势预测 #TrendPrediction",
    "跨界融合型": "跨界融合 #CrossDomain"
}

def create_legendary_tweet(index, title_zh, title_en, post_type):
    """创建单个神贴"""

    # 从12月26日开始（接着之前的25篇）
    date = (datetime(2025, 11, 26) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名
    filename_base = title_zh.split("：")[0].strip()
    filename_base = filename_base.replace(" ", "-").replace(":", "-")
    filename = f"{date}-{filename_base}.md"

    # 获取对应类型的模板
    template = CONTENT_TEMPLATES[post_type]

    # 填充内容
    content = TEMPLATE.format(
        title_zh=title_zh,
        title_en=title_en,
        content_zh=template["zh"],
        content_en=template["en"],
        logic_zh=template["logic_zh"],
        logic_en=template["logic_en"],
        type_tag=TYPE_TAGS[post_type],
        date=date,
        post_type=post_type
    )

    return filename, content

def main():
    """主函数"""
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/legendary-posts"

    created_count = 0

    for index, (title_zh, title_en, post_type) in enumerate(LEGENDARY_TOPICS):
        filename, content = create_legendary_tweet(index, title_zh, title_en, post_type)
        filepath = os.path.join(base_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {filename} ({post_type})")

    print(f"\n🎉 神贴专题新增 {created_count} 篇推文！")

    # 统计类型分布
    from collections import Counter
    type_counts = Counter([t[2] for t in LEGENDARY_TOPICS])
    print(f"\n📊 分类统计：")
    for ptype, count in type_counts.items():
        print(f"  - {ptype}: {count}篇")

if __name__ == "__main__":
    main()
