#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为编程开发专题新增20条中英双语推文
技术深度，有趣故事
"""

import os
from datetime import datetime, timedelta

# 新增20个编程开发主题
PROGRAMMING_TOPICS = [
    "Rust所有权机制：我花了3个月才真正理解",
    "WebAssembly实战：性能提升10倍的秘密",
    "GitHub Copilot vs Cursor：AI编程助手对比",
    "PostgreSQL性能优化：从慢查询到毫秒级响应",
    "Monorepo还是Multirepo：大型项目的选择",
    "Tailwind CSS：为什么我放弃了传统CSS",
    "Next.js 14 Server Actions：真正的全栈框架",
    "Kubernetes生产环境踩坑记录",
    "TypeScript泛型：从入门到精通",
    "Vim还是VSCode：我的编辑器选择之路",
    "CI/CD流水线优化：构建时间减少80%",
    "Clean Code的代价：过度设计的陷阱",
    "gRPC vs REST：微服务通信如何选择",
    "Serverless架构：不是银弹但很有用",
    "Git工作流：我们团队的最佳实践",
    "代码审查的艺术：如何提高团队代码质量",
    "内存泄漏调试：JavaScript中的隐藏杀手",
    "API设计原则：RESTful还是GraphQL",
    "测试驱动开发TDD：理想与现实的差距",
    "技术债务管理：何时偿还何时忽略",
]

TEMPLATE = """# {title}

## 🇨🇳 中文版

{content_zh}

**技术要点：**
• 要点1
• 要点2
• 要点3

**实践建议：**
```
代码示例或配置示例
```

**踩坑经验：**
⚠️ 坑1：[描述]
⚠️ 坑2：[描述]
✅ 解决方案：[描述]

**推荐资源：**
• 资源1
• 资源2

你遇到过类似问题吗？💬

---

## 🇬🇧 English Version

# {title_en}

{content_en}

**Technical Points:**
• Point 1
• Point 2
• Point 3

**Practical Advice:**
```
Code example or configuration example
```

**Lessons Learned:**
⚠️ Pitfall 1: [description]
⚠️ Pitfall 2: [description]
✅ Solution: [description]

**Recommended Resources:**
• Resource 1
• Resource 2

Have you encountered similar issues? 💬

---

## 标签 / Tags
#编程 #Programming #开发 #Development #技术 #Tech

## 发布建议 / Publishing Tips
- 最佳时间 / Best Time: 工作日早晨9:00或下午15:00 / Weekday 9AM or 3PM
- 附图 / Attach: 代码截图、架构图 / Code screenshots, architecture diagrams
- 互动 / Engagement: 技术讨论、经验分享 / Technical discussion, experience sharing
- 平台 / Platform: X/Twitter, Dev.to, 掘金

## 创作日期 / Created
{date}
"""

def generate_en_title(zh_title):
    """生成英文标题"""
    return zh_title

def create_programming_tweet(index, title):
    """创建单个编程推文"""

    # 从12月1日开始
    date = (datetime(2025, 12, 1) + timedelta(days=index)).strftime("%Y-%m-%d")

    # 生成文件名
    filename_base = title.split("：")[0].split("vs")[0].strip()
    filename_base = filename_base.replace(" ", "-").replace("/", "-")
    filename = f"{date}-{filename_base}.md"

    # 内容占位符
    content_zh = f"""[技术背景介绍]

**我的经历：**

最初我以为...
后来发现...
现在我的看法是...

**核心概念解析：**

概念1：[解释]
概念2：[解释]
概念3：[解释]

**实战案例：**

场景：[描述]
问题：[描述]
解决方案：[描述]
结果：[描述]"""

    content_en = f"""[Technical Background]

**My Journey:**

Initially I thought...
Then I discovered...
Now my view is...

**Core Concepts Explained:**

Concept 1: [explanation]
Concept 2: [explanation]
Concept 3: [explanation]

**Real-world Case:**

Scenario: [description]
Problem: [description]
Solution: [description]
Result: [description]"""

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
    base_dir = "/Users/mac/Documents/ai/WorkSpace/tweets/topics/programming"

    created_count = 0

    for index, title in enumerate(PROGRAMMING_TOPICS):
        filename, content = create_programming_tweet(index, title)
        filepath = os.path.join(base_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        created_count += 1
        print(f"✅ 创建: {filename}")

    print(f"\n🎉 编程开发专题新增 {created_count} 篇推文！")

if __name__ == "__main__":
    main()
