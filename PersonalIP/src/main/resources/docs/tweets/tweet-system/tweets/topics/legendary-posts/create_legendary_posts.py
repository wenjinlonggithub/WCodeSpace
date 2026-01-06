#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量创建神贴（Legendary Posts）中英双语推文
每篇都是道破天机的洞察
"""

import os
from datetime import datetime, timedelta

# 神贴标题列表 - 每个都是深刻洞察
LEGENDARY_POSTS = [
    # 认知颠覆型
    ("努力陷阱：为什么努力不一定成功", "The Effort Trap: Why Hard Work Doesn't Guarantee Success"),
    ("追随热情是最糟糕的建议", "Follow Your Passion Is The Worst Advice"),
    ("竞争的本质是避免竞争", "The Essence of Competition Is Avoiding Competition"),
    ("为什么大多数人高估1年低估10年", "Why People Overestimate 1 Year and Underestimate 10 Years"),
    ("失败不是成功之母，反思才是", "Failure Isn't the Mother of Success, Reflection Is"),

    # 本质洞察型
    ("所有生意的本质都是信息不对称", "All Business Is Fundamentally Information Asymmetry"),
    ("财富的本质是资源调配权", "Wealth Is Fundamentally Resource Allocation Power"),
    ("学习的本质是改变神经连接", "Learning Is Fundamentally Rewiring Neural Connections"),
    ("定价权才是护城河的本质", "Pricing Power Is the True Moat"),
    ("注意力是21世纪唯一稀缺的资源", "Attention Is the Only Scarce Resource in the 21st Century"),

    # 趋势预测型
    ("AI不会取代工作，但会重新定义工作", "AI Won't Replace Jobs, It Will Redefine Them"),
    ("下一个十年属于创作者经济", "The Next Decade Belongs to the Creator Economy"),
    ("远程工作将重构城市格局", "Remote Work Will Reshape Urban Geography"),
    ("信任的货币化：未来最大的商机", "Monetizing Trust: The Biggest Opportunity Ahead"),
    ("个人品牌将成为新的阶级分野", "Personal Branding Will Be the New Class Divide"),

    # 跨界融合型
    ("投资、创业、恋爱的底层逻辑是一样的", "Investing, Entrepreneurship, and Romance Share the Same Logic"),
    ("热力学第二定律与人生选择", "The Second Law of Thermodynamics and Life Choices"),
    ("生物进化论可以解释商业竞争", "Evolutionary Biology Explains Business Competition"),
    ("为什么免费是最贵的", "Why Free Is the Most Expensive"),
    ("复利不只适用于金钱", "Compound Interest Isn't Just About Money"),

    # 深度哲学型
    ("风险不是波动，是永久性损失", "Risk Isn't Volatility, It's Permanent Loss"),
    ("反脆弱：如何从随机性中受益", "Antifragile: How to Benefit from Randomness"),
    ("时间的非线性本质", "The Non-Linear Nature of Time"),
    ("为什么最好的技术不一定赢", "Why the Best Technology Doesn't Always Win"),
    ("网络效应的真正力量", "The True Power of Network Effects"),
]

# 中英双语模板
TEMPLATE = """# {title_zh}

## 🇨🇳 中文版

{content_zh_placeholder}

**核心洞察：**
• 洞察1
• 洞察2
• 洞察3

**底层逻辑：**
{logic_placeholder_zh}

**可行动的建议：**
1. 建议1
2. 建议2
3. 建议3

这个认知改变了你什么？💭

---

## 🇬🇧 English Version

# {title_en}

{content_en_placeholder}

**Core Insights:**
• Insight 1
• Insight 2
• Insight 3

**Underlying Logic:**
{logic_placeholder_en}

**Actionable Takeaways:**
1. Takeaway 1
2. Takeaway 2
3. Takeaway 3

How does this shift your thinking? 💭

---

## 标签 / Tags
#神贴 #LegendaryPost #深度思考 #DeepThinking #认知升级 #MentalModels

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

def create_legendary_post(index, title_zh, title_en, post_type):
    """创建单个神贴文件"""

    # 生成日期（从11月1日开始）
    date = (datetime(2025, 11, 1) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名（使用中文标题的关键词）
    filename_base = title_zh.split("：")[0].replace(" ", "-").replace("、", "-")
    filename = f"{date}-{filename_base}.md"

    # 根据不同类型设置不同的内容占位符
    content_placeholders = {
        "认知颠覆型": {
            "zh": "[大家都认为...]\n\n但真相可能是...\n\n为什么？",
            "en": "[Everyone believes...]\n\nBut the truth might be...\n\nWhy?",
            "logic_zh": "用数据和逻辑推翻常规认知，建立新的思维框架",
            "logic_en": "Use data and logic to challenge conventional wisdom and build new mental models"
        },
        "本质洞察型": {
            "zh": "[表面现象A] 和 [表面现象B] 看似不同\n\n但本质上都是...",
            "en": "[Surface phenomenon A] and [Surface phenomenon B] seem different\n\nBut fundamentally they are...",
            "logic_zh": "透过表象看本质，揭示底层规律",
            "logic_en": "See through surface to essence, reveal underlying principles"
        },
        "趋势预测型": {
            "zh": "观察到3个关键信号：\n1. 信号1\n2. 信号2\n3. 信号3\n\n推断...",
            "en": "Observed 3 key signals:\n1. Signal 1\n2. Signal 2\n3. Signal 3\n\nConclusion...",
            "logic_zh": "基于数据和趋势，预见未来变化",
            "logic_en": "Based on data and trends, predict future changes"
        },
        "跨界融合型": {
            "zh": "[领域A]的原理可以完美解释[领域B]的现象\n\n因为底层逻辑是...",
            "en": "[Domain A] principles perfectly explain [Domain B] phenomena\n\nBecause the underlying logic is...",
            "logic_zh": "从不同领域提炼共通智慧",
            "logic_en": "Extract universal wisdom from different domains"
        }
    }

    placeholder = content_placeholders[post_type]

    # 填充模板
    content = TEMPLATE.format(
        title_zh=title_zh,
        title_en=title_en,
        content_zh_placeholder=placeholder["zh"],
        content_en_placeholder=placeholder["en"],
        logic_placeholder_zh=placeholder["logic_zh"],
        logic_placeholder_en=placeholder["logic_en"],
        date=date,
        post_type=post_type
    )

    return filename, content

def main():
    """主函数"""
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/legendary-posts"

    # 确保目录存在
    os.makedirs(base_dir, exist_ok=True)

    # 定义每种类型的数量
    post_types = ["认知颠覆型"] * 5 + ["本质洞察型"] * 5 + ["趋势预测型"] * 5 + ["跨界融合型"] * 10

    created_count = 0

    for index, (title_zh, title_en) in enumerate(LEGENDARY_POSTS):
        post_type = post_types[index]
        filename, content = create_legendary_post(index, title_zh, title_en, post_type)

        filepath = os.path.join(base_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {filename} ({post_type})")

    print(f"\n🎉 成功创建 {created_count} 篇神贴！")
    print(f"📊 分类统计：")
    print(f"  - 认知颠覆型: 5篇")
    print(f"  - 本质洞察型: 5篇")
    print(f"  - 趋势预测型: 5篇")
    print(f"  - 跨界融合型: 10篇")

if __name__ == "__main__":
    main()
