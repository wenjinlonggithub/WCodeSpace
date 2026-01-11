# MySQL 应用场景与最佳实践

## 1. 传统 Web 应用场景

### 1.1 电商平台

**业务特点**：
- 高并发读写
- 复杂查询需求
- 强数据一致性要求
- 多表关联查询

**架构设计**：
```
                电商平台数据架构
┌─────────────────────────────────────────────┐
│                应用层                        │
├─────────────────────────────────────────────┤
│           缓存层 (Redis/Memcached)          │
├─────────────────────────────────────────────┤
│              读写分离层                      │
│  ┌─────────────┐    ┌─────────────────────┐ │
│  │   主库       │    │      从库集群        │ │
│  │  (写操作)    │    │    (读操作)         │ │
│  └─────────────┘    └─────────────────────┘ │
├─────────────────────────────────────────────┤
│              分库分表层                      │
│ ┌──────┬──────┬──────┬──────┬──────┬──────┐ │
│ │订单库1│订单库2│商品库1│商品库2│用户库1│用户库2│ │
│ └──────┴──────┴──────┴──────┴──────┴──────┘ │
└─────────────────────────────────────────────┘
```

**核心表设计**：
```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品表
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    brand_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    sales_count INT NOT NULL DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category_id),
    INDEX idx_brand (brand_id),
    INDEX idx_name (name),
    INDEX idx_price (price),
    INDEX idx_stock (stock_quantity),
    INDEX idx_sales (sales_count),
    INDEX idx_status_created (status, created_at),
    
    FULLTEXT INDEX ft_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表 (按用户ID分片)
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    payment_amount DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    shipping_fee DECIMAL(8,2) DEFAULT 0,
    status ENUM('pending', 'paid', 'shipped', 'delivered', 'cancelled') NOT NULL,
    payment_method ENUM('alipay', 'wechat', 'card', 'cod') NOT NULL,
    shipping_address_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_user_status_created (user_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单明细表
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_image VARCHAR(500),
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**分片策略**：
```java
// 订单分片规则
public class OrderShardingStrategy implements ShardingStrategy {
    
    @Override
    public String determineDatabase(Object shardingValue) {
        Long userId = (Long) shardingValue;
        // 按用户ID取模分库
        int dbIndex = (int) (userId % 4);
        return "order_db_" + dbIndex;
    }
    
    @Override
    public String determineTable(Object shardingValue) {
        Long userId = (Long) shardingValue;
        // 按用户ID取模分表
        int tableIndex = (int) (userId % 8);
        return "orders_" + tableIndex;
    }
}
```

**缓存策略**：
```java
@Service
public class ProductService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 商品详情缓存 (TTL: 1小时)
    public Product getProductById(Long productId) {
        String cacheKey = "product:" + productId;
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        if (product == null) {
            product = productMapper.selectById(productId);
            if (product != null) {
                redisTemplate.opsForValue().set(cacheKey, product, 3600, TimeUnit.SECONDS);
            }
        }
        
        return product;
    }
    
    // 热门商品列表缓存
    public List<Product> getHotProducts(int page, int size) {
        String cacheKey = "hot_products:" + page + ":" + size;
        List<Product> products = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        
        if (products == null) {
            products = productMapper.selectHotProducts(page * size, size);
            redisTemplate.opsForValue().set(cacheKey, products, 1800, TimeUnit.SECONDS);
        }
        
        return products;
    }
}
```

**性能优化实践**：
1. **索引优化**：根据查询模式设计复合索引
2. **缓存策略**：多级缓存，热点数据预加载
3. **分库分表**：按业务特点选择分片键
4. **读写分离**：查询走从库，事务走主库
5. **异步处理**：订单状态更新、库存扣减等

---

### 1.2 内容管理系统 (CMS)

**业务特点**：
- 读多写少
- 内容层次化
- 全文搜索需求
- 版本管理

**数据模型设计**：
```sql
-- 文章表
CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    summary TEXT,
    content LONGTEXT,
    cover_image VARCHAR(500),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    status ENUM('draft', 'published', 'archived') DEFAULT 'draft',
    is_featured BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category_id),
    INDEX idx_author (author_id),
    INDEX idx_slug (slug),
    INDEX idx_status (status),
    INDEX idx_featured (is_featured),
    INDEX idx_published (published_at),
    INDEX idx_view_count (view_count),
    INDEX idx_status_published (status, published_at),
    
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表 (树形结构)
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    parent_id INT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    level TINYINT DEFAULT 1,
    path VARCHAR(500) DEFAULT '',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_parent (parent_id),
    INDEX idx_slug (slug),
    INDEX idx_path (path),
    INDEX idx_level (level),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    slug VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(7) DEFAULT '#007cff',
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_slug (slug),
    INDEX idx_usage_count (usage_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章标签关联表
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL,
    tag_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (article_id, tag_id),
    INDEX idx_tag_id (tag_id),
    
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**全文搜索实现**：
```sql
-- 基于 MySQL 全文索引的搜索
SELECT 
    a.id,
    a.title,
    a.summary,
    a.published_at,
    MATCH(a.title, a.content) AGAINST(? IN NATURAL LANGUAGE MODE) AS relevance_score
FROM articles a
WHERE MATCH(a.title, a.content) AGAINST(? IN NATURAL LANGUAGE MODE)
    AND a.status = 'published'
ORDER BY relevance_score DESC, a.published_at DESC
LIMIT ?, ?;

-- 布尔模式搜索 (支持操作符)
SELECT * FROM articles 
WHERE MATCH(title, content) AGAINST('+MySQL -Oracle' IN BOOLEAN MODE)
    AND status = 'published';
```

**缓存策略**：
```java
@Service
public class ArticleService {
    
    // 文章详情页缓存
    @Cacheable(value = "article", key = "'detail:' + #slug")
    public ArticleDetailVO getArticleBySlug(String slug) {
        Article article = articleMapper.selectBySlug(slug);
        if (article != null) {
            // 增加浏览量 (异步)
            articleMapper.incrementViewCount(article.getId());
            
            // 获取相关文章
            List<Article> relatedArticles = getRelatedArticles(article);
            
            return ArticleDetailVO.builder()
                .article(article)
                .relatedArticles(relatedArticles)
                .build();
        }
        return null;
    }
    
    // 文章列表缓存
    @Cacheable(value = "articleList", key = "'category:' + #categoryId + ':page:' + #page")
    public PageResult<Article> getArticlesByCategory(Integer categoryId, int page, int size) {
        // 分页查询文章
        return articleMapper.selectByCategory(categoryId, page, size);
    }
}
```

---

## 2. 高并发场景

### 2.1 社交媒体平台

**业务特点**：
- 海量数据存储
- 实时性要求高
- 复杂的社交关系
- 个性化推荐

**核心表设计**：
```sql
-- 用户表 (分片)
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    bio TEXT,
    follower_count INT DEFAULT 0,
    following_count INT DEFAULT 0,
    post_count INT DEFAULT 0,
    level TINYINT DEFAULT 1,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_follower_count (follower_count),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 动态表 (按用户ID分片)
CREATE TABLE posts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    images JSON,
    video_url VARCHAR(500),
    location VARCHAR(200),
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_like_count (like_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 关注关系表 (分片)
CREATE TABLE user_follows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_follow (follower_id, following_id),
    INDEX idx_following (following_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 时间线表 (推模式)
CREATE TABLE user_timeline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    post_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 点赞表 (分片)
CREATE TABLE post_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Feed 流实现**：
```java
@Service
public class FeedService {
    
    // 推模式：关注用户发帖时推送到粉丝时间线
    @Async
    public void pushPostToFollowers(Long userId, Long postId) {
        List<Long> followerIds = userFollowMapper.getFollowerIds(userId);
        
        // 批量插入时间线
        List<UserTimeline> timelineItems = followerIds.stream()
            .map(followerId -> UserTimeline.builder()
                .userId(followerId)
                .postId(postId)
                .postUserId(userId)
                .createdAt(new Date())
                .build())
            .collect(Collectors.toList());
            
        if (!timelineItems.isEmpty()) {
            timelineMapper.batchInsert(timelineItems);
        }
    }
    
    // 拉模式：用户刷新时实时拉取关注用户的最新动态
    public List<PostVO> pullUserFeed(Long userId, int page, int size) {
        List<Long> followingIds = userFollowMapper.getFollowingIds(userId);
        
        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 从关注用户的最新动态中获取
        return postMapper.selectByUserIds(followingIds, page * size, size);
    }
    
    // 混合模式：大V用拉模式，普通用户用推模式
    public List<PostVO> getUserFeed(Long userId, int page, int size) {
        String cacheKey = "feed:" + userId + ":" + page;
        List<PostVO> cachedFeed = redisTemplate.opsForList()
            .range(cacheKey, 0, size - 1)
            .stream()
            .map(obj -> (PostVO) obj)
            .collect(Collectors.toList());
            
        if (!cachedFeed.isEmpty()) {
            return cachedFeed;
        }
        
        // 从数据库获取
        List<PostVO> feed = timelineMapper.selectUserFeed(userId, page * size, size);
        
        // 缓存结果
        if (!feed.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(cacheKey, feed.toArray());
            redisTemplate.expire(cacheKey, 15, TimeUnit.MINUTES);
        }
        
        return feed;
    }
}
```

**性能优化策略**：
```java
// 计数器优化 - 使用 Redis 计数器
@Component
public class CounterService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // 点赞计数
    public void incrementLikeCount(Long postId) {
        String key = "post:like:" + postId;
        redisTemplate.opsForValue().increment(key);
        
        // 异步同步到数据库
        syncCounterToDatabase(postId, "like");
    }
    
    // 定时同步计数器到数据库
    @Scheduled(fixedRate = 60000) // 每分钟同步一次
    public void syncAllCounters() {
        Set<String> likeKeys = redisTemplate.keys("post:like:*");
        
        for (String key : likeKeys) {
            Long postId = Long.parseLong(key.substring("post:like:".length()));
            Integer count = Integer.parseInt(redisTemplate.opsForValue().get(key));
            
            // 更新数据库
            postMapper.updateLikeCount(postId, count);
        }
    }
}
```

---

### 2.2 实时游戏后台

**业务特点**：
- 毫秒级响应要求
- 高频读写操作
- 实时排行榜
- 游戏数据统计

**核心表设计**：
```sql
-- 玩家表
CREATE TABLE players (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(100),
    level INT DEFAULT 1,
    experience BIGINT DEFAULT 0,
    gold_coins BIGINT DEFAULT 0,
    diamond_coins INT DEFAULT 0,
    last_login_at TIMESTAMP,
    total_online_time INT DEFAULT 0, -- 秒
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_level (level),
    INDEX idx_experience (experience),
    INDEX idx_last_login (last_login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 游戏记录表 (按时间分表)
CREATE TABLE game_records_202501 (
    id BIGINT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_type ENUM('pvp', 'pve', 'tournament') NOT NULL,
    result ENUM('win', 'lose', 'draw') NOT NULL,
    score INT DEFAULT 0,
    duration INT NOT NULL, -- 秒
    experience_gained INT DEFAULT 0,
    coins_gained INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_player_id (player_id),
    INDEX idx_game_type (game_type),
    INDEX idx_created_at (created_at),
    INDEX idx_player_created (player_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 排行榜表 (内存表用于实时计算)
CREATE TABLE leaderboard (
    id INT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(50) NOT NULL,
    score BIGINT NOT NULL,
    rank_position INT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_score (score DESC),
    INDEX idx_rank (rank_position),
    INDEX idx_player_id (player_id)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

-- 玩家物品表
CREATE TABLE player_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    
    UNIQUE KEY uk_player_item (player_id, item_id),
    INDEX idx_player_id (player_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**实时排行榜实现**：
```java
@Service
public class LeaderboardService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LEADERBOARD_KEY = "leaderboard:global";
    
    // 更新玩家分数
    public void updatePlayerScore(Long playerId, String username, Long score) {
        // 更新 Redis 有序集合
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, 
            JSON.toJSONString(Map.of("playerId", playerId, "username", username)), score);
        
        // 异步更新数据库
        updateLeaderboardAsync(playerId, username, score);
    }
    
    // 获取排行榜
    public List<LeaderboardEntry> getTopPlayers(int count) {
        Set<ZSetOperations.TypedTuple<Object>> entries = redisTemplate.opsForZSet()
            .reverseRangeWithScores(LEADERBOARD_KEY, 0, count - 1);
            
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        
        for (ZSetOperations.TypedTuple<Object> entry : entries) {
            Map<String, Object> playerData = JSON.parseObject(entry.getValue().toString(), Map.class);
            
            leaderboard.add(LeaderboardEntry.builder()
                .rank(rank++)
                .playerId((Long) playerData.get("playerId"))
                .username((String) playerData.get("username"))
                .score(entry.getScore().longValue())
                .build());
        }
        
        return leaderboard;
    }
    
    // 获取玩家排名
    public Long getPlayerRank(Long playerId) {
        Long rank = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, playerId);
        return rank != null ? rank + 1 : null;
    }
}
```

**游戏数据缓存策略**：
```java
@Service
public class PlayerDataService {
    
    // 玩家数据缓存 (会话期间保持)
    @Cacheable(value = "playerData", key = "'player:' + #playerId")
    public PlayerData getPlayerData(Long playerId) {
        Player player = playerMapper.selectById(playerId);
        List<PlayerItem> items = playerItemMapper.selectByPlayerId(playerId);
        
        return PlayerData.builder()
            .player(player)
            .items(items)
            .build();
    }
    
    // 热点数据预加载
    @PostConstruct
    public void preloadHotData() {
        // 预加载活跃玩家数据
        List<Long> activePlayerIds = playerMapper.getActivePlayerIds(1000);
        
        for (Long playerId : activePlayerIds) {
            getPlayerData(playerId);
        }
    }
    
    // 玩家状态实时更新
    public void updatePlayerStatus(Long playerId, PlayerStatus status) {
        // 更新缓存
        String cacheKey = "player:status:" + playerId;
        redisTemplate.opsForValue().set(cacheKey, status, 30, TimeUnit.MINUTES);
        
        // 广播给游戏服务器
        messageProducer.sendPlayerStatusUpdate(playerId, status);
    }
}
```

---

## 3. 数据分析场景

### 3.1 商业智能 (BI) 系统

**业务特点**：
- 大量历史数据
- 复杂聚合查询
- 多维度分析
- 报表生成

**数据仓库设计**：
```sql
-- 销售事实表 (星型模式)
CREATE TABLE fact_sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date_key INT NOT NULL,
    product_key INT NOT NULL,
    customer_key INT NOT NULL,
    store_key INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    cost_amount DECIMAL(15,2) NOT NULL,
    profit_amount DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    
    INDEX idx_date_key (date_key),
    INDEX idx_product_key (product_key),
    INDEX idx_customer_key (customer_key),
    INDEX idx_store_key (store_key),
    INDEX idx_date_product (date_key, product_key),
    INDEX idx_date_customer (date_key, customer_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日期维度表
CREATE TABLE dim_date (
    date_key INT PRIMARY KEY,
    full_date DATE NOT NULL,
    year INT NOT NULL,
    quarter TINYINT NOT NULL,
    month TINYINT NOT NULL,
    week TINYINT NOT NULL,
    day_of_month TINYINT NOT NULL,
    day_of_week TINYINT NOT NULL,
    day_of_year SMALLINT NOT NULL,
    month_name VARCHAR(20),
    day_name VARCHAR(20),
    is_weekend BOOLEAN DEFAULT FALSE,
    is_holiday BOOLEAN DEFAULT FALSE,
    
    INDEX idx_full_date (full_date),
    INDEX idx_year_month (year, month),
    INDEX idx_year_quarter (year, quarter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 产品维度表
CREATE TABLE dim_product (
    product_key INT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    category_level1 VARCHAR(100),
    category_level2 VARCHAR(100),
    category_level3 VARCHAR(100),
    brand VARCHAR(100),
    supplier VARCHAR(100),
    unit VARCHAR(20),
    standard_cost DECIMAL(10,2),
    effective_date DATE,
    expiry_date DATE,
    
    INDEX idx_product_id (product_id),
    INDEX idx_category_l1 (category_level1),
    INDEX idx_category_l2 (category_level2),
    INDEX idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 客户维度表
CREATE TABLE dim_customer (
    customer_key INT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    customer_type ENUM('individual', 'enterprise'),
    gender ENUM('M', 'F'),
    age_group VARCHAR(20),
    city VARCHAR(50),
    province VARCHAR(50),
    country VARCHAR(50),
    registration_date DATE,
    customer_level ENUM('bronze', 'silver', 'gold', 'platinum'),
    
    INDEX idx_customer_id (customer_id),
    INDEX idx_customer_type (customer_type),
    INDEX idx_city (city),
    INDEX idx_level (customer_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**复杂分析查询**：
```sql
-- 销售趋势分析
SELECT 
    d.year,
    d.month,
    d.month_name,
    COUNT(DISTINCT f.customer_key) as customer_count,
    SUM(f.quantity) as total_quantity,
    SUM(f.total_amount) as total_sales,
    SUM(f.profit_amount) as total_profit,
    AVG(f.total_amount) as avg_order_value
FROM fact_sales f
JOIN dim_date d ON f.date_key = d.date_key
WHERE d.year = 2024
GROUP BY d.year, d.month, d.month_name
ORDER BY d.year, d.month;

-- 产品类别销售排行
SELECT 
    p.category_level1,
    p.category_level2,
    SUM(f.total_amount) as sales_amount,
    SUM(f.quantity) as sales_quantity,
    COUNT(DISTINCT f.customer_key) as customer_count,
    RANK() OVER (ORDER BY SUM(f.total_amount) DESC) as sales_rank
FROM fact_sales f
JOIN dim_product p ON f.product_key = p.product_key
JOIN dim_date d ON f.date_key = d.date_key
WHERE d.year = 2024
GROUP BY p.category_level1, p.category_level2
HAVING SUM(f.total_amount) > 10000
ORDER BY sales_amount DESC;

-- 客户购买行为分析 (RFM模型)
SELECT 
    customer_key,
    customer_name,
    DATEDIFF(CURDATE(), MAX(d.full_date)) as recency, -- 最近购买天数
    COUNT(*) as frequency, -- 购买频次
    SUM(f.total_amount) as monetary, -- 购买金额
    CASE 
        WHEN DATEDIFF(CURDATE(), MAX(d.full_date)) <= 30 THEN 'High'
        WHEN DATEDIFF(CURDATE(), MAX(d.full_date)) <= 90 THEN 'Medium'
        ELSE 'Low'
    END as recency_score,
    CASE 
        WHEN COUNT(*) >= 10 THEN 'High'
        WHEN COUNT(*) >= 5 THEN 'Medium'
        ELSE 'Low'
    END as frequency_score,
    CASE 
        WHEN SUM(f.total_amount) >= 10000 THEN 'High'
        WHEN SUM(f.total_amount) >= 5000 THEN 'Medium'
        ELSE 'Low'
    END as monetary_score
FROM fact_sales f
JOIN dim_date d ON f.date_key = d.date_key
JOIN dim_customer c ON f.customer_key = c.customer_key
WHERE d.year = 2024
GROUP BY customer_key, customer_name
ORDER BY monetary DESC;
```

**数据预聚合优化**：
```sql
-- 月度汇总表
CREATE TABLE agg_monthly_sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    year_month VARCHAR(7) NOT NULL, -- 格式: 2024-01
    product_category VARCHAR(100),
    customer_segment VARCHAR(50),
    total_orders INT DEFAULT 0,
    total_customers INT DEFAULT 0,
    total_quantity DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(18,2) DEFAULT 0,
    total_profit DECIMAL(18,2) DEFAULT 0,
    avg_order_value DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_month_category_segment (year_month, product_category, customer_segment),
    INDEX idx_year_month (year_month),
    INDEX idx_category (product_category),
    INDEX idx_segment (customer_segment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预聚合存储过程
DELIMITER $$
CREATE PROCEDURE AggregateMonthlyData(IN target_year_month VARCHAR(7))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 删除已存在的数据
    DELETE FROM agg_monthly_sales WHERE year_month = target_year_month;
    
    -- 插入聚合数据
    INSERT INTO agg_monthly_sales (
        year_month, product_category, customer_segment,
        total_orders, total_customers, total_quantity, total_amount, total_profit, avg_order_value
    )
    SELECT 
        DATE_FORMAT(d.full_date, '%Y-%m') as year_month,
        p.category_level1 as product_category,
        c.customer_level as customer_segment,
        COUNT(*) as total_orders,
        COUNT(DISTINCT f.customer_key) as total_customers,
        SUM(f.quantity) as total_quantity,
        SUM(f.total_amount) as total_amount,
        SUM(f.profit_amount) as total_profit,
        AVG(f.total_amount) as avg_order_value
    FROM fact_sales f
    JOIN dim_date d ON f.date_key = d.date_key
    JOIN dim_product p ON f.product_key = p.product_key
    JOIN dim_customer c ON f.customer_key = c.customer_key
    WHERE DATE_FORMAT(d.full_date, '%Y-%m') = target_year_month
    GROUP BY p.category_level1, c.customer_level;
    
    COMMIT;
END$$
DELIMITER ;
```

---

### 3.2 日志分析系统

**业务特点**：
- 海量日志数据
- 实时写入需求
- 时间序列查询
- 数据保留策略

**日志表设计**：
```sql
-- 访问日志表 (按日期分表)
CREATE TABLE access_logs_20250101 (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp TIMESTAMP(3) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_id BIGINT NULL,
    method ENUM('GET', 'POST', 'PUT', 'DELETE', 'OPTIONS') NOT NULL,
    url VARCHAR(2000) NOT NULL,
    query_string VARCHAR(4000),
    status_code SMALLINT NOT NULL,
    response_size INT DEFAULT 0,
    response_time INT NOT NULL, -- 毫秒
    user_agent VARCHAR(1000),
    referer VARCHAR(2000),
    session_id VARCHAR(100),
    
    INDEX idx_timestamp (timestamp),
    INDEX idx_ip_address (ip_address),
    INDEX idx_user_id (user_id),
    INDEX idx_status_code (status_code),
    INDEX idx_url (url(100)),
    INDEX idx_timestamp_status (timestamp, status_code),
    INDEX idx_response_time (response_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 错误日志表
CREATE TABLE error_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp TIMESTAMP(3) NOT NULL,
    level ENUM('ERROR', 'WARN', 'FATAL') NOT NULL,
    logger_name VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    exception_class VARCHAR(200),
    stack_trace TEXT,
    user_id BIGINT NULL,
    session_id VARCHAR(100),
    ip_address VARCHAR(45),
    request_url VARCHAR(2000),
    server_hostname VARCHAR(100),
    
    INDEX idx_timestamp (timestamp),
    INDEX idx_level (level),
    INDEX idx_logger_name (logger_name),
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp_level (timestamp, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 性能统计表
CREATE TABLE performance_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp TIMESTAMP NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    tags JSON,
    hostname VARCHAR(100),
    
    INDEX idx_timestamp (timestamp),
    INDEX idx_metric_name (metric_name),
    INDEX idx_timestamp_metric (timestamp, metric_name),
    INDEX idx_hostname (hostname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**实时统计查询**：
```sql
-- 实时访问量统计 (按小时)
SELECT 
    DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00') as hour_bucket,
    COUNT(*) as request_count,
    COUNT(DISTINCT ip_address) as unique_ips,
    COUNT(DISTINCT user_id) as unique_users,
    AVG(response_time) as avg_response_time,
    SUM(CASE WHEN status_code >= 400 THEN 1 ELSE 0 END) as error_count,
    SUM(response_size) as total_bytes
FROM access_logs_20250101
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00')
ORDER BY hour_bucket;

-- Top 访问页面
SELECT 
    SUBSTRING_INDEX(url, '?', 1) as page_url,
    COUNT(*) as hit_count,
    COUNT(DISTINCT ip_address) as unique_visitors,
    AVG(response_time) as avg_response_time,
    MAX(response_time) as max_response_time
FROM access_logs_20250101
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
    AND status_code = 200
GROUP BY SUBSTRING_INDEX(url, '?', 1)
ORDER BY hit_count DESC
LIMIT 20;

-- 异常检测 (响应时间异常)
SELECT 
    DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:00') as time_bucket,
    COUNT(*) as request_count,
    AVG(response_time) as avg_response_time,
    MAX(response_time) as max_response_time,
    STDDEV(response_time) as stddev_response_time
FROM access_logs_20250101
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 2 HOUR)
GROUP BY DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:00')
HAVING avg_response_time > 1000 OR max_response_time > 10000
ORDER BY avg_response_time DESC;
```

**自动化数据归档**：
```sql
-- 创建归档存储过程
DELIMITER $$
CREATE PROCEDURE ArchiveOldLogs(IN archive_days INT)
BEGIN
    DECLARE table_name VARCHAR(50);
    DECLARE archive_date DATE;
    DECLARE done INT DEFAULT FALSE;
    
    -- 游标：获取需要归档的表
    DECLARE table_cursor CURSOR FOR
        SELECT TABLE_NAME 
        FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME LIKE 'access_logs_%'
            AND TABLE_NAME <= CONCAT('access_logs_', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL archive_days DAY), '%Y%m%d'));
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN table_cursor;
    
    archive_loop: LOOP
        FETCH table_cursor INTO table_name;
        
        IF done THEN
            LEAVE archive_loop;
        END IF;
        
        -- 导出到归档表或文件
        SET @sql = CONCAT('CREATE TABLE ', table_name, '_archive AS SELECT * FROM ', table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 删除原表
        SET @sql = CONCAT('DROP TABLE ', table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
    END LOOP;
    
    CLOSE table_cursor;
END$$
DELIMITER ;
```

---

## 4. 企业级应用场景

### 4.1 ERP 系统

**业务特点**：
- 复杂业务逻辑
- 强事务一致性
- 多模块集成
- 审计要求

**核心业务表设计**：
```sql
-- 供应商表
CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    payment_terms VARCHAR(100),
    credit_limit DECIMAL(15,2) DEFAULT 0,
    status ENUM('active', 'inactive', 'blacklisted') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_code (code),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 采购订单表
CREATE TABLE purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(30) UNIQUE NOT NULL,
    supplier_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery_date DATE,
    total_amount DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    status ENUM('draft', 'submitted', 'approved', 'rejected', 'cancelled', 'completed') DEFAULT 'draft',
    approval_flow_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_order_no (order_no),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_order_date (order_date),
    INDEX idx_status (status),
    
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 采购订单明细表
CREATE TABLE purchase_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    specification VARCHAR(500),
    unit VARCHAR(20) NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    received_quantity DECIMAL(10,3) DEFAULT 0,
    quality_status ENUM('pending', 'passed', 'failed') DEFAULT 'pending',
    delivery_date DATE,
    notes TEXT,
    
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_product_code (product_code),
    
    FOREIGN KEY (order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 库存表
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id INT NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    available_quantity DECIMAL(10,3) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(10,3) NOT NULL DEFAULT 0,
    in_transit_quantity DECIMAL(10,3) NOT NULL DEFAULT 0,
    reorder_point DECIMAL(10,3) DEFAULT 0,
    max_stock_level DECIMAL(10,3) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_warehouse_product (warehouse_id, product_id),
    INDEX idx_product_code (product_code),
    INDEX idx_available_quantity (available_quantity),
    INDEX idx_reorder_point (reorder_point)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 库存变动记录表
CREATE TABLE inventory_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id INT NOT NULL,
    product_id BIGINT NOT NULL,
    transaction_type ENUM('in', 'out', 'adjust', 'transfer') NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit_cost DECIMAL(10,2),
    reference_type ENUM('purchase', 'sale', 'adjustment', 'transfer', 'production') NOT NULL,
    reference_id BIGINT,
    reference_no VARCHAR(30),
    operator_id BIGINT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    
    INDEX idx_warehouse_product (warehouse_id, product_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_reference (reference_type, reference_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**复杂业务逻辑实现**：
```java
@Service
@Transactional
public class PurchaseOrderService {
    
    // 采购订单提交审批
    public void submitForApproval(Long orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !order.getStatus().equals("draft")) {
            throw new BusinessException("订单状态不正确");
        }
        
        // 启动审批流程
        ApprovalFlow flow = approvalService.startFlow("purchase_order", orderId, order.getTotalAmount());
        
        // 更新订单状态
        order.setStatus("submitted");
        order.setApprovalFlowId(flow.getId());
        purchaseOrderMapper.updateById(order);
        
        // 记录操作日志
        auditService.logOperation("purchase_order", orderId, "submit_approval", 
            "订单提交审批，审批流程ID：" + flow.getId());
    }
    
    // 采购入库处理
    @Transactional(rollbackFor = Exception.class)
    public void receiveGoods(Long orderId, List<ReceiveGoodsItem> receiveItems) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !order.getStatus().equals("approved")) {
            throw new BusinessException("采购订单状态不正确");
        }
        
        List<PurchaseOrderItem> orderItems = purchaseOrderItemMapper.selectByOrderId(orderId);
        Map<Long, PurchaseOrderItem> itemMap = orderItems.stream()
            .collect(Collectors.toMap(PurchaseOrderItem::getProductId, Function.identity()));
        
        for (ReceiveGoodsItem receiveItem : receiveItems) {
            PurchaseOrderItem orderItem = itemMap.get(receiveItem.getProductId());
            if (orderItem == null) {
                throw new BusinessException("商品不在采购订单中：" + receiveItem.getProductId());
            }
            
            // 检查入库数量是否超过订单数量
            BigDecimal totalReceived = orderItem.getReceivedQuantity().add(receiveItem.getQuantity());
            if (totalReceived.compareTo(orderItem.getQuantity()) > 0) {
                throw new BusinessException("入库数量超过订单数量");
            }
            
            // 更新库存
            inventoryService.addStock(receiveItem.getWarehouseId(), 
                receiveItem.getProductId(), receiveItem.getQuantity(), receiveItem.getUnitCost());
            
            // 更新订单项已收货数量
            orderItem.setReceivedQuantity(totalReceived);
            purchaseOrderItemMapper.updateById(orderItem);
            
            // 记录库存变动
            InventoryTransaction transaction = InventoryTransaction.builder()
                .warehouseId(receiveItem.getWarehouseId())
                .productId(receiveItem.getProductId())
                .transactionType("in")
                .quantity(receiveItem.getQuantity())
                .unitCost(receiveItem.getUnitCost())
                .referenceType("purchase")
                .referenceId(orderId)
                .referenceNo(order.getOrderNo())
                .operatorId(getCurrentUserId())
                .notes("采购入库")
                .build();
            
            inventoryTransactionMapper.insert(transaction);
        }
        
        // 检查是否完全收货
        boolean fullyReceived = orderItems.stream()
            .allMatch(item -> item.getReceivedQuantity().equals(item.getQuantity()));
        
        if (fullyReceived) {
            order.setStatus("completed");
            purchaseOrderMapper.updateById(order);
        }
    }
    
    // 库存预警检查
    @Scheduled(fixedRate = 3600000) // 每小时检查一次
    public void checkStockAlert() {
        List<Inventory> lowStockItems = inventoryMapper.selectLowStockItems();
        
        for (Inventory inventory : lowStockItems) {
            // 发送库存预警
            alertService.sendStockAlert(inventory);
            
            // 自动创建采购建议
            createPurchaseSuggestion(inventory);
        }
    }
}
```

---

### 4.2 财务管理系统

**业务特点**：
- 严格的数据准确性
- 复杂的会计规则
- 多币种支持
- 审计追踪

**核心财务表设计**：
```sql
-- 科目表
CREATE TABLE chart_of_accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_code VARCHAR(20) UNIQUE NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    account_type ENUM('asset', 'liability', 'equity', 'revenue', 'expense') NOT NULL,
    parent_id INT DEFAULT 0,
    level TINYINT NOT NULL,
    path VARCHAR(500) NOT NULL,
    is_leaf BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    currency_code VARCHAR(3) DEFAULT 'CNY',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_account_code (account_code),
    INDEX idx_account_type (account_type),
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 会计凭证表
CREATE TABLE vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_no VARCHAR(20) UNIQUE NOT NULL,
    voucher_date DATE NOT NULL,
    period VARCHAR(7) NOT NULL, -- 格式: 2024-01
    voucher_type ENUM('receipt', 'payment', 'transfer', 'adjustment') NOT NULL,
    total_debit_amount DECIMAL(15,2) NOT NULL,
    total_credit_amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'CNY',
    exchange_rate DECIMAL(10,6) DEFAULT 1,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reference_no VARCHAR(50),
    description TEXT,
    creator_id BIGINT NOT NULL,
    reviewer_id BIGINT,
    status ENUM('draft', 'submitted', 'approved', 'posted', 'cancelled') DEFAULT 'draft',
    posted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_voucher_no (voucher_no),
    INDEX idx_voucher_date (voucher_date),
    INDEX idx_period (period),
    INDEX idx_voucher_type (voucher_type),
    INDEX idx_reference (reference_type, reference_id),
    INDEX idx_status (status),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 会计分录表
CREATE TABLE journal_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    line_number SMALLINT NOT NULL,
    account_id INT NOT NULL,
    account_code VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    debit_amount DECIMAL(15,2) DEFAULT 0,
    credit_amount DECIMAL(15,2) DEFAULT 0,
    currency_code VARCHAR(3) DEFAULT 'CNY',
    foreign_currency_amount DECIMAL(15,2),
    exchange_rate DECIMAL(10,6) DEFAULT 1,
    cost_center_id INT,
    project_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_voucher_id (voucher_id),
    INDEX idx_account_id (account_id),
    INDEX idx_account_code (account_code),
    INDEX idx_cost_center (cost_center_id),
    INDEX idx_project_id (project_id),
    
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES chart_of_accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 总账余额表 (按期间汇总)
CREATE TABLE general_ledger_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    period VARCHAR(7) NOT NULL,
    account_id INT NOT NULL,
    account_code VARCHAR(20) NOT NULL,
    opening_balance DECIMAL(15,2) DEFAULT 0,
    debit_amount DECIMAL(15,2) DEFAULT 0,
    credit_amount DECIMAL(15,2) DEFAULT 0,
    closing_balance DECIMAL(15,2) DEFAULT 0,
    currency_code VARCHAR(3) DEFAULT 'CNY',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_period_account (period, account_id),
    INDEX idx_period (period),
    INDEX idx_account_id (account_id),
    INDEX idx_account_code (account_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预算表
CREATE TABLE budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_year YEAR NOT NULL,
    department_id INT NOT NULL,
    account_id INT NOT NULL,
    account_code VARCHAR(20) NOT NULL,
    budget_amount DECIMAL(15,2) NOT NULL,
    spent_amount DECIMAL(15,2) DEFAULT 0,
    remaining_amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'CNY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_year_dept_account (budget_year, department_id, account_id),
    INDEX idx_budget_year (budget_year),
    INDEX idx_department_id (department_id),
    INDEX idx_account_id (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**会计凭证处理**：
```java
@Service
@Transactional
public class VoucherService {
    
    // 创建销售收入凭证
    public void createSalesVoucher(Long salesOrderId, BigDecimal salesAmount, BigDecimal taxAmount) {
        SalesOrder salesOrder = salesOrderMapper.selectById(salesOrderId);
        
        // 创建凭证头
        Voucher voucher = Voucher.builder()
            .voucherNo(generateVoucherNo("receipt"))
            .voucherDate(LocalDate.now())
            .period(YearMonth.now().toString())
            .voucherType("receipt")
            .totalDebitAmount(salesAmount.add(taxAmount))
            .totalCreditAmount(salesAmount.add(taxAmount))
            .referenceType("sales_order")
            .referenceId(salesOrderId)
            .referenceNo(salesOrder.getOrderNo())
            .description("销售收入 - " + salesOrder.getOrderNo())
            .creatorId(getCurrentUserId())
            .status("draft")
            .build();
        
        voucherMapper.insert(voucher);
        
        // 创建分录 - 借：应收账款
        JournalEntry debitEntry = JournalEntry.builder()
            .voucherId(voucher.getId())
            .lineNumber(1)
            .accountId(getAccountId("1122")) // 应收账款
            .accountCode("1122")
            .description("销售应收 - " + salesOrder.getCustomerName())
            .debitAmount(salesAmount.add(taxAmount))
            .creditAmount(BigDecimal.ZERO)
            .build();
        
        journalEntryMapper.insert(debitEntry);
        
        // 创建分录 - 贷：主营业务收入
        JournalEntry creditEntry1 = JournalEntry.builder()
            .voucherId(voucher.getId())
            .lineNumber(2)
            .accountId(getAccountId("6001")) // 主营业务收入
            .accountCode("6001")
            .description("销售收入 - " + salesOrder.getOrderNo())
            .debitAmount(BigDecimal.ZERO)
            .creditAmount(salesAmount)
            .build();
        
        journalEntryMapper.insert(creditEntry1);
        
        // 创建分录 - 贷：应交税费
        if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            JournalEntry creditEntry2 = JournalEntry.builder()
                .voucherId(voucher.getId())
                .lineNumber(3)
                .accountId(getAccountId("2221")) // 应交税费-应交增值税
                .accountCode("2221")
                .description("销项税额")
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(taxAmount)
                .build();
            
            journalEntryMapper.insert(creditEntry2);
        }
        
        // 验证借贷平衡
        validateVoucherBalance(voucher.getId());
    }
    
    // 期末结账处理
    @Transactional(rollbackFor = Exception.class)
    public void closePeriod(String period) {
        // 1. 检查期间是否已结账
        if (isperiodClosed(period)) {
            throw new BusinessException("期间已结账：" + period);
        }
        
        // 2. 检查是否有未过账凭证
        List<Voucher> unpostedVouchers = voucherMapper.selectUnpostedByPeriod(period);
        if (!unpostedVouchers.isEmpty()) {
            throw new BusinessException("存在未过账凭证，无法结账");
        }
        
        // 3. 计算期间余额
        List<AccountBalance> balances = calculatePeriodBalances(period);
        
        // 4. 更新总账余额表
        for (AccountBalance balance : balances) {
            GeneralLedgerBalance ledgerBalance = GeneralLedgerBalance.builder()
                .period(period)
                .accountId(balance.getAccountId())
                .accountCode(balance.getAccountCode())
                .openingBalance(balance.getOpeningBalance())
                .debitAmount(balance.getDebitAmount())
                .creditAmount(balance.getCreditAmount())
                .closingBalance(balance.getClosingBalance())
                .build();
            
            generalLedgerBalanceMapper.insertOrUpdate(ledgerBalance);
        }
        
        // 5. 生成损益结转凭证
        generateProfitLossVoucher(period);
        
        // 6. 标记期间为已结账
        markPeriodAsClosed(period);
        
        // 7. 记录结账日志
        auditService.logOperation("period_closing", null, "close_period", 
            "期间结账：" + period);
    }
    
    // 生成财务报表数据
    public BalanceSheetData generateBalanceSheet(String period) {
        List<GeneralLedgerBalance> balances = generalLedgerBalanceMapper.selectByPeriod(period);
        
        BalanceSheetData balanceSheet = new BalanceSheetData();
        
        for (GeneralLedgerBalance balance : balances) {
            ChartOfAccount account = chartOfAccountMapper.selectById(balance.getAccountId());
            
            switch (account.getAccountType()) {
                case "asset":
                    balanceSheet.addAsset(account.getAccountName(), balance.getClosingBalance());
                    break;
                case "liability":
                    balanceSheet.addLiability(account.getAccountName(), balance.getClosingBalance());
                    break;
                case "equity":
                    balanceSheet.addEquity(account.getAccountName(), balance.getClosingBalance());
                    break;
            }
        }
        
        return balanceSheet;
    }
}
```

---

## 5. 总结与最佳实践建议

### 5.1 架构选择指导

**单体应用场景**：
- 业务相对简单
- 团队规模较小
- 快速开发需求
- 数据一致性要求高

**分布式架构场景**：
- 大规模高并发
- 复杂业务逻辑
- 团队规模较大
- 系统可扩展性要求高

### 5.2 性能优化策略

**数据库层面**：
1. 合理的索引设计
2. 分库分表策略
3. 读写分离架构
4. 连接池优化

**应用层面**：
1. 多级缓存设计
2. 异步处理优化
3. 批量操作优化
4. 数据库连接管理

**系统层面**：
1. 硬件资源优化
2. 操作系统调优
3. 网络优化配置
4. 监控告警体系

### 5.3 安全性保障

**数据安全**：
1. 敏感数据加密存储
2. 访问权限控制
3. 审计日志记录
4. 数据备份策略

**应用安全**：
1. SQL 注入防护
2. 接口访问控制
3. 安全认证机制
4. 漏洞扫描检测

### 5.4 运维管理

**监控体系**：
1. 性能指标监控
2. 业务指标监控
3. 错误日志监控
4. 资源使用监控

**自动化运维**：
1. 自动化部署
2. 数据库维护
3. 备份恢复策略
4. 容量规划管理

通过以上各种应用场景的分析和实践，可以看出 MySQL 在不同业务场景下都有其独特的应用方式和优化策略。选择合适的架构设计和优化方案，能够有效支撑业务发展和系统性能要求。