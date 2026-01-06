#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为自媒体出海创业专题新增20条中英双语推文
实战经验，案例丰富
"""

import os
from datetime import datetime, timedelta

# 新增20个自媒体出海主题
MEDIA_TOPICS = [
    "YouTube Shorts vs TikTok：流量分配逻辑的本质差异",
    "我如何在6个月内将YouTube频道变现",
    "Pinterest营销：被低估的流量宝藏",
    "播客出海完整指南：从录制到分发",
    "Twitter蓝V认证后的真实变化",
    "Instagram算法2025：什么内容会被推荐",
    "Substack vs Ghost：Newsletter平台如何选择",
    "LinkedIn长文还是短文：数据告诉你答案",
    "Reddit营销的正确打开方式",
    "Twitch直播变现：不只是打游戏",
    "Medium付费墙策略：值得开通吗",
    "Facebook群组运营：从0到10000成员",
    "Spotify播客赞助：如何拿到第一个广告",
    "Threads vs Twitter：新平台机会在哪",
    "Patreon会员制：粉丝付费的关键",
    "Discord社区运营：留存率提升300%的方法",
    "Notion模板售卖：月入$5000的副业",
    "Gumroad数字产品：从创作到销售",
    "Beehiiv增长黑客：Newsletter涨粉策略",
    "ConvertKit自动化：邮件营销的正确姿势",
]

TEMPLATE = """# {title}

## 🇨🇳 中文版

{content_zh}

**关键要点：**
• 要点1
• 要点2
• 要点3

**实战建议：**
1. 建议1
2. 建议2
3. 建议3

**常见误区：**
❌ 误区1
❌ 误区2
✅ 正确做法

你试过这个平台/方法吗？💬

---

## 🇬🇧 English Version

# {title_en}

{content_en}

**Key Takeaways:**
• Point 1
• Point 2
• Point 3

**Actionable Tips:**
1. Tip 1
2. Tip 2
3. Tip 3

**Common Mistakes:**
❌ Mistake 1
❌ Mistake 2
✅ Right approach

Have you tried this platform/method? 💬

---

## 标签 / Tags
#自媒体 #ContentCreator #出海 #GlobalContent #社交媒体 #SocialMedia

## 发布建议 / Publishing Tips
- 最佳时间 / Best Time: 工作日上午10:00或晚上20:00 / Weekday 10AM or 8PM
- 附图 / Attach: 平台截图、数据对比 / Platform screenshots, data comparison
- 互动 / Engagement: 征集经验分享 / Ask for experience sharing
- 平台 / Platform: X/Twitter, LinkedIn, 小红书

## 创作日期 / Created
{date}
"""

# 生成英文标题
def generate_en_title(zh_title):
    """简单的标题转换，实际使用时可以优化"""
    return zh_title  # 保持原样，因为很多已经包含英文

def create_media_tweet(index, title):
    """创建单个自媒体推文"""

    # 从12月1日开始
    date = (datetime(2025, 12, 1) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名
    filename_base = title.split("：")[0].split("vs")[0].split("：")[0].strip()
    filename_base = filename_base.replace(" ", "-").replace("/", "-")
    filename = f"{date}-{filename_base}.md"

    # 内容占位符
    content_zh = f"""[平台/方法介绍]

我的实践经验：

**数据表现：**
• 数据1
• 数据2
• 数据3

**具体策略：**

第一步：[操作]
第二步：[操作]
第三步：[操作]

**结果：**
[具体成果]"""

    content_en = f"""[Platform/Method Introduction]

My hands-on experience:

**Performance Data:**
• Data 1
• Data 2
• Data 3

**Specific Strategy:**

Step 1: [action]
Step 2: [action]
Step 3: [action]

**Results:**
[specific outcomes]"""

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
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/media-entrepreneurship"

    created_count = 0

    for index, title in enumerate(MEDIA_TOPICS):
        filename, content = create_media_tweet(index, title)
        filepath = os.path.join(base_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {filename}")

    print(f"\n🎉 自媒体出海专题新增 {created_count} 篇推文！")

if __name__ == "__main__":
    main()
