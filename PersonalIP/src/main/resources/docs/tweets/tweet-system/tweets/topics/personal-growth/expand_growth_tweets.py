#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为个人成长专题新增20条中英双语推文
方法论，可操作性
"""

import os
from datetime import datetime, timedelta

# 新增20个个人成长主题
GROWTH_TOPICS = [
    "二八法则的真正应用：聚焦20%的高价值活动",
    "如何培养系统思维：看问题的底层能力",
    "注意力管理：比时间管理更重要的技能",
    "费曼学习法实践：如何真正掌握一个知识",
    "心流状态：如何进入高效工作模式",
    "反馈循环：快速成长的核心机制",
    "元认知能力：学习如何学习",
    "决策框架：如何做出更好的选择",
    "压力管理：将压力转化为动力",
    "人际边界：说不的艺术",
    "成长型思维vs固定型思维",
    "刻意练习：从新手到专家的路径",
    "认知负荷理论：为什么多任务是谎言",
    "身份认同：行为改变的深层驱动力",
    "信息过载时代的深度阅读法",
    "情绪智力：比IQ更重要的能力",
    "自律的本质：不是意志力而是系统",
    "长期主义：如何在短期诱惑中坚持",
    "复盘方法论：经验如何转化为智慧",
    "个人能力模型：T型人才vs π型人才",
]

TEMPLATE = """# {title}

## 🇨🇳 中文版

{content_zh}

**核心方法：**
📝 方法1
📝 方法2
📝 方法3

**实践步骤：**
**Day 1-7：** [具体行动]
**Day 8-30：** [具体行动]
**Day 31+：** [具体行动]

**常见障碍及应对：**
🚧 障碍1 → 💡 应对策略
🚧 障碍2 → 💡 应对策略
🚧 障碍3 → 💡 应对策略

**推荐书籍/资源：**
📚 资源1
📚 资源2
📚 资源3

你在实践哪个方法？💬

---

## 🇬🇧 English Version

# {title_en}

{content_en}

**Core Methods:**
📝 Method 1
📝 Method 2
📝 Method 3

**Action Plan:**
**Day 1-7:** [specific actions]
**Day 8-30:** [specific actions]
**Day 31+:** [specific actions]

**Common Obstacles & Solutions:**
🚧 Obstacle 1 → 💡 Solution
🚧 Obstacle 2 → 💡 Solution
🚧 Obstacle 3 → 💡 Solution

**Recommended Books/Resources:**
📚 Resource 1
📚 Resource 2
📚 Resource 3

Which method are you practicing? 💬

---

## 标签 / Tags
#个人成长 #PersonalGrowth #自我提升 #SelfImprovement #生产力 #Productivity

## 发布建议 / Publishing Tips
- 最佳时间 / Best Time: 周日晚上20:00（规划新一周）/ Sunday 8PM (planning new week)
- 附图 / Attach: 方法论图表、思维导图 / Framework diagrams, mind maps
- 互动 / Engagement: 征集实践经验 / Ask for practice experiences
- 平台 / Platform: X/Twitter, LinkedIn, 小红书

## 创作日期 / Created
{date}
"""

def generate_en_title(zh_title):
    """生成英文标题"""
    return zh_title

def create_growth_tweet(index, title):
    """创建单个个人成长推文"""

    # 从12月1日开始
    date = (datetime(2025, 12, 1) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名
    filename_base = title.split("：")[0].split("vs")[0].strip()
    filename_base = filename_base.replace(" ", "-").replace("/", "-")
    filename = f"{date}-{filename_base}.md"

    # 内容占位符
    content_zh = f"""[认知问题/挑战]

大多数人的误区：
❌ 误区描述

真相是：
✅ 正确理解

**为什么重要？**

原因1：[阐述]
原因2：[阐述]
原因3：[阐述]

**我的亲身经历：**

之前：[状态描述]
现在：[状态描述]
改变：[具体数据/案例]

**具体方法拆解：**

技巧1：[详细说明]
技巧2：[详细说明]
技巧3：[详细说明]

**实施建议：**
[如何开始行动]"""

    content_en = f"""[Cognitive Problem/Challenge]

Common Misconception:
❌ Misconception description

The Truth:
✅ Correct understanding

**Why It Matters:**

Reason 1: [explanation]
Reason 2: [explanation]
Reason 3: [explanation]

**My Personal Journey:**

Before: [state description]
Now: [state description]
Change: [specific data/case]

**Method Breakdown:**

Technique 1: [detailed explanation]
Technique 2: [detailed explanation]
Technique 3: [detailed explanation]

**Implementation Advice:**
[how to start taking action]"""

    # 填充模板
    content = TEMPLATE.format(
        title=title,
        title_en=generate_en_title(title),
        content_zh=content_zh,
        content_en=content_en,
        date=date
    )

    return filename, content

def main():
    """主函数"""
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/personal-growth"

    created_count = 0

    for index, title in enumerate(GROWTH_TOPICS):
        filename, content = create_growth_tweet(index, title)
        filepath = os.path.join(base_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {filename}")

    print(f"\n🎉 个人成长专题新增 {created_count} 篇推文！")

if __name__ == "__main__":
    main()
