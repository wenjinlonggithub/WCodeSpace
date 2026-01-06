#!/usr/bin/env python3
"""
Generate 91 Bitcoin tweet topics to reach 100 total
Current: 9 files exist
Target: 100 files
"""

import json
from datetime import datetime, timedelta

# Starting date for new files
start_date = datetime(2025, 10, 26)

# Define comprehensive Bitcoin topics across categories
bitcoin_topics = []

# Category 1: Price History & Market Cycles (18 topics)
price_topics = [
    {"title_cn": "2013年牛市", "title_en": "2013 Bull Market", "focus": "$13 to $1,100 rally, Mt. Gox era"},
    {"title_cn": "2018年熊市解析", "title_en": "2018 Bear Market Analysis", "focus": "84% crash, capitulation phase"},
    {"title_cn": "2021年牛市顶部", "title_en": "2021 Bull Market Peak", "focus": "$69,000 top, institutional FOMO"},
    {"title_cn": "减半周期回报率", "title_en": "Halving Cycle Returns", "focus": "Historical ROI per cycle"},
    {"title_cn": "比特币冬天生存指南", "title_en": "Crypto Winter Survival Guide", "focus": "Bear market strategies"},
    {"title_cn": "Stock-to-Flow模型", "title_en": "Stock-to-Flow Model", "focus": "PlanB's S2F predictions"},
    {"title_cn": "链上指标分析", "title_en": "On-Chain Metrics Analysis", "focus": "Glassnode, MVRV, SOPR"},
    {"title_cn": "巨鲸地址追踪", "title_en": "Whale Address Tracking", "focus": "Large holder behavior"},
    {"title_cn": "恐慌贪婪指数", "title_en": "Fear & Greed Index", "focus": "Sentiment indicators"},
    {"title_cn": "月线级别支撑位", "title_en": "Monthly Support Levels", "focus": "Key price levels"},
    {"title_cn": "比特币季节性规律", "title_en": "Bitcoin Seasonality", "focus": "Monthly performance patterns"},
    {"title_cn": "Mt. Gox事件影响", "title_en": "Mt. Gox Impact", "focus": "2014 collapse, ongoing payouts"},
    {"title_cn": "Silk Road比特币拍卖", "title_en": "Silk Road Bitcoin Auction", "focus": "US government sales"},
    {"title_cn": "中国94禁令", "title_en": "China 9/4 Ban", "focus": "2017 ICO ban impact"},
    {"title_cn": "COVID-19暴跌", "title_en": "COVID-19 Crash", "focus": "March 2020 $3,800 bottom"},
    {"title_cn": "圣诞节行情", "title_en": "Christmas Rally", "focus": "Year-end patterns"},
    {"title_cn": "减半前后价格行为", "title_en": "Halving Price Action", "focus": "Pre/post halving moves"},
    {"title_cn": "比特币多米诺效应", "title_en": "Bitcoin Dominance", "focus": "BTC.D metric, altseason"},
]

# Category 2: Technical Fundamentals (18 topics)
tech_topics = [
    {"title_cn": "区块链三难困境", "title_en": "Blockchain Trilemma", "focus": "Decentralization, security, scalability"},
    {"title_cn": "工作量证明详解", "title_en": "Proof of Work Explained", "focus": "PoW mechanism, energy debate"},
    {"title_cn": "UTXO模型", "title_en": "UTXO Model", "focus": "vs account model"},
    {"title_cn": "Taproot升级", "title_en": "Taproot Upgrade", "focus": "Privacy, smart contracts"},
    {"title_cn": "SegWit隔离见证", "title_en": "SegWit", "focus": "Scalability improvement"},
    {"title_cn": "比特币脚本语言", "title_en": "Bitcoin Script", "focus": "Programmability limits"},
    {"title_cn": "默克尔树原理", "title_en": "Merkle Tree", "focus": "Transaction verification"},
    {"title_cn": "难度调整算法", "title_en": "Difficulty Adjustment", "focus": "Every 2016 blocks"},
    {"title_cn": "51%攻击成本", "title_en": "51% Attack Cost", "focus": "Security economics"},
    {"title_cn": "双花问题", "title_en": "Double Spend Problem", "focus": "Byzantine Generals"},
    {"title_cn": "内存池机制", "title_en": "Mempool Mechanics", "focus": "Transaction queuing"},
    {"title_cn": "区块大小之争", "title_en": "Block Size Debate", "focus": "1MB limit, BCH fork"},
    {"title_cn": "全节点vs轻节点", "title_en": "Full Node vs SPV", "focus": "Decentralization tradeoffs"},
    {"title_cn": "时间锁定交易", "title_en": "Timelocked Transactions", "focus": "nLockTime, CLTV"},
    {"title_cn": "多重签名钱包", "title_en": "Multisig Wallets", "focus": "2-of-3, 3-of-5 setups"},
    {"title_cn": "比特币改进提案BIP", "title_en": "Bitcoin Improvement Proposals", "focus": "BIP process, key BIPs"},
    {"title_cn": "SHA-256安全性", "title_en": "SHA-256 Security", "focus": "Quantum threat"},
    {"title_cn": "Schnorr签名", "title_en": "Schnorr Signatures", "focus": "Efficiency, privacy"},
]

# Category 3: Investment Strategies (18 topics)
investment_topics = [
    {"title_cn": "定投DCA策略", "title_en": "Dollar-Cost Averaging", "focus": "Historical backtest data"},
    {"title_cn": "HODLing心理学", "title_en": "HODLing Psychology", "focus": "Long-term holding"},
    {"title_cn": "资产配置比例", "title_en": "Portfolio Allocation", "focus": "5%, 10%, 25% allocation"},
    {"title_cn": "止损止盈策略", "title_en": "Stop Loss & Take Profit", "focus": "Risk management"},
    {"title_cn": "税务优化技巧", "title_en": "Tax Optimization", "focus": "Capital gains, loss harvesting"},
    {"title_cn": "冷钱包安全", "title_en": "Cold Wallet Security", "focus": "Hardware wallets, air-gapped"},
    {"title_cn": "助记词管理", "title_en": "Seed Phrase Management", "focus": "Backup strategies"},
    {"title_cn": "交易所vs自托管", "title_en": "Exchange vs Self-Custody", "focus": "Tradeoffs"},
    {"title_cn": "比特币遗产规划", "title_en": "Bitcoin Estate Planning", "focus": "Inheritance, multi-sig"},
    {"title_cn": "抄底指标", "title_en": "Buy the Dip Indicators", "focus": "RSI, MVRV, Puell Multiple"},
    {"title_cn": "分批建仓法", "title_en": "Staged Entry Strategy", "focus": "Pyramid buying"},
    {"title_cn": "再平衡策略", "title_en": "Rebalancing Strategy", "focus": "Quarterly rebalance"},
    {"title_cn": "杠杆风险", "title_en": "Leverage Risks", "focus": "Liquidation, funding rates"},
    {"title_cn": "套利机会", "title_en": "Arbitrage Opportunities", "focus": "Exchange spreads, funding"},
    {"title_cn": "质押收益", "title_en": "Staking Yields", "focus": "Lightning routing, DeFi"},
    {"title_cn": "比特币信贷", "title_en": "Bitcoin-Backed Loans", "focus": "BlockFi, Celsius risks"},
    {"title_cn": "退休账户BTC", "title_en": "Bitcoin in Retirement Accounts", "focus": "IRA, 401k options"},
    {"title_cn": "通胀对冲效果", "title_en": "Inflation Hedge Performance", "focus": "vs CPI, M2"},
]

# Category 4: Institutional Adoption & Regulation (18 topics)
institutional_topics = [
    {"title_cn": "MicroStrategy持仓", "title_en": "MicroStrategy Holdings", "focus": "Saylor's BTC treasury"},
    {"title_cn": "BlackRock ETF", "title_en": "BlackRock ETF", "focus": "IBIT launch, inflows"},
    {"title_cn": "Grayscale GBTC", "title_en": "Grayscale GBTC", "focus": "Premium/discount, conversions"},
    {"title_cn": "Tesla比特币", "title_en": "Tesla Bitcoin", "focus": "$1.5B purchase, later sale"},
    {"title_cn": "萨尔瓦多法币化", "title_en": "El Salvador Legal Tender", "focus": "National adoption"},
    {"title_cn": "SEC监管政策", "title_en": "SEC Regulation", "focus": "ETF approvals, enforcement"},
    {"title_cn": "欧盟MiCA法规", "title_en": "EU MiCA Regulation", "focus": "Markets in Crypto-Assets"},
    {"title_cn": "中国挖矿禁令", "title_en": "China Mining Ban", "focus": "2021 crackdown, migration"},
    {"title_cn": "美国税务申报", "title_en": "US Tax Reporting", "focus": "IRS forms, wash sales"},
    {"title_cn": "养老基金配置", "title_en": "Pension Fund Allocation", "focus": "Institutional adoption"},
    {"title_cn": "银行托管服务", "title_en": "Bank Custody Services", "focus": "Fidelity, BNY Mellon"},
    {"title_cn": "PayPal整合", "title_en": "PayPal Integration", "focus": "Buy/sell, Venmo"},
    {"title_cn": "Coinbase上市", "title_en": "Coinbase IPO", "focus": "NASDAQ: COIN"},
    {"title_cn": "央行数字货币CBDC", "title_en": "CBDCs vs Bitcoin", "focus": "Competition, differences"},
    {"title_cn": "反洗钱KYC", "title_en": "AML/KYC Requirements", "focus": "Compliance burden"},
    {"title_cn": "证券化争议", "title_en": "Securities Classification", "focus": "Commodity vs security"},
    {"title_cn": "跨境支付应用", "title_en": "Cross-Border Payments", "focus": "Remittances, Strike"},
    {"title_cn": "机构级托管", "title_en": "Institutional Custody", "focus": "Multi-sig, insurance"},
]

# Category 5: Culture, Philosophy & Comparisons (19 topics)
culture_topics = [
    {"title_cn": "中本聪之谜", "title_en": "Satoshi Nakamoto Mystery", "focus": "Identity theories"},
    {"title_cn": "密码朋克运动", "title_en": "Cypherpunk Movement", "focus": "Origins, Hal Finney"},
    {"title_cn": "比特币极端主义", "title_en": "Bitcoin Maximalism", "focus": "Michael Saylor, Max Keiser"},
    {"title_cn": "不是你的钥匙", "title_en": "Not Your Keys Not Your Coins", "focus": "Self-custody ethos"},
    {"title_cn": "点对点电子现金", "title_en": "Peer-to-Peer Electronic Cash", "focus": "Original whitepaper vision"},
    {"title_cn": "去中心化的重要性", "title_en": "Importance of Decentralization", "focus": "vs Ethereum, XRP"},
    {"title_cn": "抗审查性", "title_en": "Censorship Resistance", "focus": "Freedom money"},
    {"title_cn": "健全货币原则", "title_en": "Sound Money Principles", "focus": "Austrian economics"},
    {"title_cn": "比特币vs以太坊", "title_en": "Bitcoin vs Ethereum", "focus": "Store of value vs platform"},
    {"title_cn": "比特币vs法币", "title_en": "Bitcoin vs Fiat", "focus": "Inflation, debasement"},
    {"title_cn": "Layer1 vs Layer2", "title_en": "Layer 1 vs Layer 2", "focus": "Base layer vs Lightning"},
    {"title_cn": "比特币vs山寨币", "title_en": "Bitcoin vs Altcoins", "focus": "Lindy effect, security"},
    {"title_cn": "通缩资产属性", "title_en": "Deflationary Asset Properties", "focus": "Fixed supply"},
    {"title_cn": "数字稀缺性", "title_en": "Digital Scarcity", "focus": "First time in history"},
    {"title_cn": "比特币标准", "title_en": "Bitcoin Standard", "focus": "Saifedean Ammous book"},
    {"title_cn": "能源货币论", "title_en": "Energy Money Thesis", "focus": "PoW as battery"},
    {"title_cn": "比特币与自由", "title_en": "Bitcoin & Freedom", "focus": "Financial sovereignty"},
    {"title_cn": "橙色药丸", "title_en": "Orange Pilling", "focus": "Bitcoin conversion stories"},
    {"title_cn": "21M上限哲学", "title_en": "21M Cap Philosophy", "focus": "Why 21 million"},
]

# Combine all topics
all_topics = price_topics + tech_topics + investment_topics + institutional_topics + culture_topics

# Generate file metadata
for i, topic in enumerate(all_topics[:91]):  # Only need 91 more
    file_date = start_date + timedelta(days=i)
    filename = file_date.strftime("%Y-%m-%d") + "-" + topic["title_cn"].replace(" ", "-") + ".md"
    topic["filename"] = filename
    topic["date"] = file_date.strftime("%Y-%m-%d")

# Save to JSON for reference
output = {
    "total_needed": 91,
    "topics": all_topics[:91]
}

print(json.dumps(output, ensure_ascii=False, indent=2))
print(f"\n✅ Generated {len(all_topics[:91])} Bitcoin topic outlines")
