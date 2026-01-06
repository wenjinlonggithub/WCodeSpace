#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为个人开发专题新增20条中英双语推文
产品思维，独立开发
"""

import os
from datetime import datetime, timedelta

# 新增20个个人开发主题
INDIE_TOPICS = [
    "从Side Project到月入$10K：我的12个月历程",
    "Stripe vs Paddle：支付方案如何选择",
    "SEO优化：我如何让产品排到Google首页",
    "Landing Page设计：转化率提升5倍的秘密",
    "如何定价：我测试了20种定价策略",
    "Product Hunt发布清单：前10名的秘诀",
    "冷启动：没有粉丝如何获得前100个用户",
    "用户访谈：问什么问题最有价值",
    "A/B测试实战：数据驱动的产品迭代",
    "开源项目变现：从GitHub到收入",
    "技术选型：快速MVP vs 长期维护",
    "客服自动化：Intercom还是自建",
    "分析工具选择：Google Analytics vs Plausible",
    "邮件营销ROI：每封邮件赚$2的方法",
    "社区建设：Discord vs Circle vs Slack",
    "功能优先级：如何拒绝90%的需求",
    "退款处理：保持好评率的艺术",
    "竞品分析：我如何研究竞争对手",
    "时间管理：全职工作+Side Project的平衡",
    "独立开发者的财务规划",
]

TEMPLATE = """# {title}

## 🇨🇳 中文版

{content_zh}

**关键数据：**
📊 数据1
📊 数据2
📊 数据3

**实战步骤：**
1️⃣ 步骤1
2️⃣ 步骤2
3️⃣ 步骤3

**工具推荐：**
🛠️ 工具1：[用途]
🛠️ 工具2：[用途]
🛠️ 工具3：[用途]

**避坑指南：**
❌ 不要：[错误做法]
✅ 应该：[正确做法]

你在做什么产品？💬

---

## 🇬🇧 English Version

# {title_en}

{content_en}

**Key Metrics:**
📊 Metric 1
📊 Metric 2
📊 Metric 3

**Action Steps:**
1️⃣ Step 1
2️⃣ Step 2
3️⃣ Step 3

**Tool Recommendations:**
🛠️ Tool 1: [purpose]
🛠️ Tool 2: [purpose]
🛠️ Tool 3: [purpose]

**Avoid These Mistakes:**
❌ Don't: [wrong approach]
✅ Do: [right approach]

What are you building? 💬

---

## 标签 / Tags
#IndieHacker #独立开发 #SideProject #创业 #Startup #产品 #Product

## 发布建议 / Publishing Tips
- 最佳时间 / Best Time: 周末下午 / Weekend afternoon
- 附图 / Attach: 产品截图、数据图表 / Product screenshots, data charts
- 互动 / Engagement: 征集产品分享 / Ask others to share their products
- 平台 / Platform: X/Twitter, IndieHackers, V2EX

## 创作日期 / Created
{date}
"""

def generate_en_title(zh_title):
    """生成英文标题"""
    return zh_title

def create_indie_tweet(index, title):
    """创建单个独立开发推文"""

    # 从12月1日开始
    date = (datetime(2025, 12, 1) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名
    filename_base = title.split("：")[0].split("vs")[0].strip()
    filename_base = filename_base.replace(" ", "-").replace("/", "-").replace("$", "")
    filename = f"{date}-{filename_base}.md"

    # 内容占位符
    content_zh = f"""[问题/挑战]

我的解决方案：

**背景：**
• 起点状态
• 面临的挑战
• 目标设定

**执行过程：**

阶段1：[描述]
- 具体行动
- 遇到的问题
- 如何解决

阶段2：[描述]
- 具体行动
- 效果如何

阶段3：[描述]
- 最终结果

**经验总结：**
[关键洞察]"""

    content_en = f"""[Problem/Challenge]

My Solution:

**Background:**
• Starting point
• Challenges faced
• Goals set

**Execution Process:**

Phase 1: [description]
- Specific actions
- Problems encountered
- How solved

Phase 2: [description]
- Specific actions
- Results

Phase 3: [description]
- Final outcome

**Lessons Learned:**
[key insights]"""

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
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/indie-dev"

    created_count = 0

    for index, title in enumerate(INDIE_TOPICS):
        filename, content = create_indie_tweet(index, title)
        filepath = os.path.join(base_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {filename}")

    print(f"\n🎉 个人开发专题新增 {created_count} 篇推文！")

if __name__ == "__main__":
    main()
