#!/bin/bash

# 批量创建所有专题的中英双语推文框架

BASE_DIR="/Users/mac/Documents/ai/WorkSpace/tweets/topics"

# 定义每个专题需要创建的推文标题
declare -A TOPICS

# 自媒体出海创业专题 (需要9篇)
TOPICS[media-entrepreneurship]="
YouTube算法2025:我的频道如何突破10万订阅
TikTok变现完整指南:从0到月入$5000
Instagram Reels策略:如何在30天获得100万播放
Podcast出海:被低估的内容形式
Newsletter变现:Substack实战经验
短视频脚本公式:7个百万播放模板
内容创作者的AI工具栈2025
从自媒体到个人品牌:我的转型之路
多平台内容矩阵:如何高效管理
"

# 编程开发专题 (需要7篇)
TOPICS[programming]="
TypeScript 5.0新特性深度解析
Docker容器化最佳实践2025
GraphQL vs REST:如何选择
Redis性能优化:从理论到实践
微前端架构:适合你的项目吗
代码整洁之道:我的重构经验
开源贡献指南:从PR到Maintainer
"

# 个人开发专题 (需要10篇)
TOPICS[indie-dev]="
SaaS定价策略:我测试了10种模式
Product Hunt发布完全指南
如何获得前100个付费用户
从Side Project到Full-time:我的决策过程
技术债务管理:何时重构何时妥协
用户反馈收集系统搭建
Stripe集成踩坑记录
MVP开发checklist:不要过度工程
增长黑客实战:9个有效策略
Indie Hacker的财务规划
"

# 个人成长专题 (需要10篇)
TOPICS[personal-growth]="
深度工作法:我如何做到每天4小时深度专注
知识管理系统:从输入到输出的完整流程
如何阅读一本书:我的高效阅读法
时间管理的本质:精力管理而非时间管理
克服拖延症:我用了这5个方法
批判性思维训练:如何避免认知偏差
习惯养成科学:21天是个谎言
终身学习者的自我教育体系
目标设定与达成:OKR个人实践
专注力训练:抵御数字时代的干扰
"

# 创建推文函数
create_bilingual_tweet() {
    local topic=$1
    local title=$2
    local index=$3
    
    # 生成文件名
    local filename="2025-11-$(printf "%02d" $index)-$(echo "$title" | sed 's/：.*//;s/ /-/g;s/\//-/g').md"
    local filepath="$BASE_DIR/$topic/$filename"
    
    # 创建文件
    cat > "$filepath" << 'EOF'
# TITLE_ZH

## 中文版推文

[核心内容]

**要点总结:**
• 要点1
• 要点2  
• 要点3

**实践建议:**
1. 建议1
2. 建议2
3. 建议3

你的经验是什么？欢迎交流 💬

---

## English Tweet

# TITLE_EN

[Core Content]

**Key Takeaways:**
• Point 1
• Point 2
• Point 3

**Action Items:**
1. Tip 1
2. Tip 2
3. Tip 3

What's your experience? Let's discuss 💬

---

## 标签 / Tags
#TOPIC_TAG #双语内容 #BilingualContent

## 发布建议 / Publishing Tips
- 中文发布时间 / CN Time: 晚上20:00-22:00
- 英文发布时间 / EN Time: 美国东部时间9:00-11:00
- 平台 / Platform: X/Twitter, LinkedIn
- 互动 / Engagement: 提问引导讨论 / Ask questions to engage

## 创作日期 / Created
2025-11-XX
EOF

    # 替换占位符
    sed -i '' "s/TITLE_ZH/$title/" "$filepath"
    sed -i '' "s/TITLE_EN/$(echo $title | sed 's/：/:/g')/" "$filepath"
    sed -i '' "s/TOPIC_TAG/$(echo $topic | tr '-' ' ')/" "$filepath"
    sed -i '' "s/2025-11-XX/2025-11-$(printf "%02d" $index)/" "$filepath"
    
    echo "✅ Created: $filename"
}

# 主循环
for topic in "${!TOPICS[@]}"; do
    echo ""
    echo "=== 创建 $topic 专题推文 ==="
    index=1
    while IFS= read -r title; do
        [[ -z "$title" ]] && continue
        create_bilingual_tweet "$topic" "$title" "$index"
        ((index++))
    done <<< "${TOPICS[$topic]}"
done

echo ""
echo "🎉 所有推文框架创建完成！"
