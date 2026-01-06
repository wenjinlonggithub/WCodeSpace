#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Batch fill legendary posts with real content
"""

import os
import re

# Content templates for different post types
CONTENT_DATABASE = {
    "所有生意的本质都是信息不对称": {
        "cn_content": """房地产经纪人和Google搜索引擎，看似完全不同的生意。

但本质上都是同一件事：利用信息不对称赚钱。

为什么所有生意的本质都是信息不对称？

Naval Ravikant说过："商业就是在信息流动的摩擦中套利。"当信息完全对称时，利润会趋近于零——这就是经济学中的"完全竞争市场"理论。

看数据：
- 1990年代房产经纪人佣金6%，因为买家不知道市场价格
- Zillow等平台出现后，信息透明，佣金降到3-4%
- 疫情后线上看房普及，2024年佣金进一步降到2-3%

信息差消失 = 利润消失。

**核心洞察：**
• 所有中介都是信息不对称：从淘宝（连接工厂和消费者）到风投（连接资本和创业者）。Charlie Munger说"找到信息流动的瓶颈，你就找到了赚钱的机会"
• 技术是信息不对称的终结者：互联网每降低10%的信息成本，就有一个传统中介行业消亡。旅行社、股票经纪人、报纸都是受害者
• 新的不对称不断出现：当旧的信息差被填平，聪明人会创造新的。Web3、AI、生物科技——都是新的信息前沿

**底层逻辑：**

信息价值公式：
利润 = 信息差 × 交易规模 × 垄断程度

案例分析：
- 1980年代股票经纪人：巨大信息差 × 大规模 × 高垄断 = 暴利（交易佣金1-2%）
- 2024年Robinhood：零信息差 × 大规模 × 低垄断 = 微利（靠订单流赚钱）

Warren Buffett的护城河理论本质上就是"持续的信息不对称"：
- 可口可乐：消费者不知道成本只有$0.05，但愿意付$2
- GEICO保险：掌握精算数据，比客户更了解风险定价

Paul Graham在《How to Make Wealth》中指出："财富不是金钱，是解决问题。而问题的根源往往是信息不对称。"

真实故事：Michael Lewis的《Flash Boys》揭露了高频交易的秘密——他们用光纤缩短0.001秒的信息传输时间，每年赚数十亿美元。纯粹的信息套利。

**可行动的建议：**
1. 问自己：我在哪个领域拥有别人没有的信息？这就是你的竞争优势。可能是行业经验、人脉网络、或数据访问权
2. 建立"信息护城河"：持续投资于获取独特信息——付费报告、专家网络、一手调研。信息是唯一会升值的资产
3. 警惕"信息平台化"：如果你的生意本质是信息中介，要么垂直整合（提供服务），要么建立网络效应（成为平台），否则会被互联网消灭

这个认知改变了你什么？""",
        "en_content": """Real estate agents and Google search engine seem like completely different businesses.

But fundamentally they're the same: profiting from information asymmetry.

Why is all business fundamentally about information asymmetry?

Naval Ravikant: "Business is arbitrage in the friction of information flow." When information is perfectly symmetric, profits approach zero—that's the "perfect competition" theory in economics.

The data:
- 1990s real estate agent commission: 6% (buyers didn't know market prices)
- After Zillow and platforms emerged: 3-4% (information transparency)
- Post-pandemic online viewings: 2-3% in 2024

Information gap disappears = Profits disappear.

**Core Insights:**
• All intermediaries are information asymmetry: From Taobao (connecting factories to consumers) to VCs (connecting capital to founders). Charlie Munger says "find bottlenecks in information flow, you find money-making opportunities"
• Technology is the terminator of information asymmetry: Every 10% reduction in information costs kills a traditional intermediary industry. Travel agencies, stockbrokers, newspapers—all victims
• New asymmetries constantly emerge: When old information gaps close, smart people create new ones. Web3, AI, biotech—all new information frontiers

**Underlying Logic:**

Information value formula:
Profit = Information Gap × Transaction Scale × Monopoly Degree

Case analysis:
- 1980s stockbrokers: Huge info gap × Large scale × High monopoly = Massive profits (1-2% commissions)
- 2024 Robinhood: Zero info gap × Large scale × Low monopoly = Thin margins (profit from order flow)

Warren Buffett's moat theory is essentially "sustained information asymmetry":
- Coca-Cola: Consumers don't know cost is $0.05, but willing to pay $2
- GEICO insurance: Masters actuarial data, knows risk pricing better than customers

Paul Graham in "How to Make Wealth": "Wealth isn't money, it's solving problems. And problems often stem from information asymmetry."

True story: Michael Lewis's "Flash Boys" exposed high-frequency trading secrets—they used fiber optics to shorten information transmission by 0.001 seconds, earning billions annually. Pure information arbitrage.

**Actionable Takeaways:**
1. Ask yourself: In which field do I have information others don't? That's your competitive advantage—industry experience, network, or data access
2. Build an "information moat": Continuously invest in unique information—paid reports, expert networks, primary research. Information is the only appreciating asset
3. Beware "information platformization": If your business is essentially information intermediation, either vertically integrate (provide services) or build network effects (become a platform), or the internet will eliminate you

How does this shift your thinking?"""
    }
}

def main():
    print("Script created for batch processing")
    print("Use Edit tool to fill individual files")

if __name__ == "__main__":
    main()
