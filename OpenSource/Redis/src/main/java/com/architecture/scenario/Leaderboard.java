package com.architecture.scenario;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.util.List;

/**
 * Redis排行榜实现
 *
 * 应用场景：
 * - 游戏积分排行榜
 * - 热门文章排行
 * - 销售排行榜
 * - 用户活跃度排名
 *
 * 核心数据结构：Sorted Set（ZSet）
 * - member：用户ID或物品ID
 * - score：分数
 *
 * 时间复杂度：
 * - 添加/更新：O(logN)
 * - 查询排名：O(logN)
 * - 范围查询：O(logN + M)
 */
public class Leaderboard {

    private Jedis jedis;
    private String leaderboardKey;

    public Leaderboard(Jedis jedis, String leaderboardKey) {
        this.jedis = jedis;
        this.leaderboardKey = leaderboardKey;
    }

    /**
     * 添加或更新分数
     */
    public void addScore(String userId, double score) {
        jedis.zadd(leaderboardKey, score, userId);
    }

    /**
     * 增加分数
     */
    public double incrScore(String userId, double increment) {
        return jedis.zincrby(leaderboardKey, increment, userId);
    }

    /**
     * 获取用户分数
     */
    public Double getScore(String userId) {
        return jedis.zscore(leaderboardKey, userId);
    }

    /**
     * 获取用户排名（从0开始，0表示第一名）
     * 降序排名（分数从高到低）
     */
    public Long getRank(String userId) {
        return jedis.zrevrank(leaderboardKey, userId);
    }

    /**
     * 获取用户排名（从1开始）
     */
    public Long getRankWithOne(String userId) {
        Long rank = getRank(userId);
        return rank == null ? null : rank + 1;
    }

    /**
     * 获取TOP N排行榜
     * @param topN 前N名
     * @return 用户列表及分数
     */
    public List<Tuple> getTopN(int topN) {
        // ZREVRANGE：按分数从高到低返回
        return jedis.zrevrangeWithScores(leaderboardKey, 0, topN - 1);
    }

    /**
     * 获取指定排名范围的用户
     * @param start 开始排名（从0开始）
     * @param end 结束排名
     */
    public List<Tuple> getRangeByRank(long start, long end) {
        return jedis.zrevrangeWithScores(leaderboardKey, start, end);
    }

    /**
     * 获取指定分数范围的用户
     * @param minScore 最小分数
     * @param maxScore 最大分数
     */
    public List<Tuple> getRangeByScore(double minScore, double maxScore) {
        return jedis.zrevrangeByScoreWithScores(leaderboardKey, maxScore, minScore);
    }

    /**
     * 获取排行榜总人数
     */
    public Long getTotal() {
        return jedis.zcard(leaderboardKey);
    }

    /**
     * 删除用户
     */
    public boolean removeUser(String userId) {
        return jedis.zrem(leaderboardKey, userId) > 0;
    }

    /**
     * 获取用户周围的排名
     * @param userId 用户ID
     * @param range 上下范围
     */
    public List<Tuple> getUserAround(String userId, int range) {
        Long rank = getRank(userId);
        if (rank == null) {
            return null;
        }

        long start = Math.max(0, rank - range);
        long end = rank + range;

        return getRangeByRank(start, end);
    }

    /**
     * 批量添加分数
     */
    public void batchAddScore(java.util.Map<String, Double> scoreMap) {
        jedis.zadd(leaderboardKey, scoreMap);
    }

    /**
     * 清空排行榜
     */
    public void clear() {
        jedis.del(leaderboardKey);
    }

    /**
     * 打印排行榜
     */
    public void printTopN(int topN) {
        List<Tuple> top = getTopN(topN);
        System.out.println("=== TOP " + topN + " 排行榜 ===");
        int rank = 1;
        for (Tuple tuple : top) {
            System.out.printf("第%d名: %s - %.2f分\n",
                    rank++,
                    tuple.getElement(),
                    tuple.getScore());
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            Leaderboard leaderboard = new Leaderboard(jedis, "game:score:leaderboard");

            // 清空排行榜
            leaderboard.clear();

            // 添加用户分数
            System.out.println("=== 添加用户分数 ===");
            leaderboard.addScore("user:1001", 1500);
            leaderboard.addScore("user:1002", 2000);
            leaderboard.addScore("user:1003", 1800);
            leaderboard.addScore("user:1004", 2200);
            leaderboard.addScore("user:1005", 1900);
            leaderboard.addScore("user:1006", 2100);
            leaderboard.addScore("user:1007", 1700);
            leaderboard.addScore("user:1008", 2300);
            leaderboard.addScore("user:1009", 1600);
            leaderboard.addScore("user:1010", 2400);

            // 打印TOP 5
            leaderboard.printTopN(5);

            // 查询指定用户信息
            System.out.println("\n=== 查询user:1005信息 ===");
            String userId = "user:1005";
            Double score = leaderboard.getScore(userId);
            Long rank = leaderboard.getRankWithOne(userId);
            System.out.printf("用户: %s, 分数: %.2f, 排名: 第%d名\n", userId, score, rank);

            // 增加分数
            System.out.println("\n=== user:1005 获得500分 ===");
            leaderboard.incrScore(userId, 500);
            score = leaderboard.getScore(userId);
            rank = leaderboard.getRankWithOne(userId);
            System.out.printf("用户: %s, 分数: %.2f, 排名: 第%d名\n", userId, score, rank);

            // 查看用户周围的排名
            System.out.println("\n=== user:1005 周围的排名 ===");
            List<Tuple> around = leaderboard.getUserAround(userId, 2);
            if (around != null) {
                for (Tuple tuple : around) {
                    String uid = tuple.getElement();
                    Long r = leaderboard.getRankWithOne(uid);
                    System.out.printf("第%d名: %s - %.2f分%s\n",
                            r,
                            uid,
                            tuple.getScore(),
                            uid.equals(userId) ? " (本人)" : "");
                }
            }

            // 统计信息
            System.out.println("\n=== 统计信息 ===");
            System.out.println("总人数: " + leaderboard.getTotal());
        }
    }
}
