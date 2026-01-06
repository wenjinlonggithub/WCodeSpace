#!/bin/bash

# 金融专题推文标题列表
declare -a TITLES=(
    "美债收益率倒挂：衰退信号还是虚惊一场"
    "黄金突破2500：下一个目标在哪里"
    "量化宽松的终结：对市场意味着什么"
    "加密货币监管：风险还是机会"
    "美元霸权的黄昏：去美元化趋势分析"
    "原油价格与通胀：被忽视的关联"
    "日本央行政策转向：全球影响几何"
    "科技股泡沫2.0：历史会重演吗"
    "房地产市场寒冬：买入还是观望"
    "ESG投资：噱头还是大势所趋"
)

count=1
for title in "${TITLES[@]}"; do
    filename="2025-10-2${count}-$(echo $title | sed 's/：.*//;s/ /-/g').md"
    
    cat > "$filename" << 'EOF'
# TITLE_PLACEHOLDER

## 中文版

[内容占位 - 根据标题展开详细分析]

核心观点：
- 观点1
- 观点2  
- 观点3

数据支撑：
- 数据1
- 数据2

风险提示：
[必须包含]

---

## English Version

# TITLE_ENGLISH

[Content placeholder - Detailed analysis based on title]

Key Points:
- Point 1
- Point 2
- Point 3

Data Support:
- Data 1
- Data 2

Risk Warning:
[Must include]

---

## 标签 / Tags
#金融 #Finance #投资 #Investment

## 发布建议 / Publishing Tips
- 最佳时间 / Best Time: 根据内容类型选择 / Based on content type
- 附图 / Attach: 相关数据图表 / Relevant charts
EOF
    
    # 替换标题
    sed -i '' "s/TITLE_PLACEHOLDER/$title/" "$filename"
    
    ((count++))
done

echo "创建了 ${#TITLES[@]} 个推文模板"
